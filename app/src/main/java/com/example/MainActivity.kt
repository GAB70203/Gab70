package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.db.AppDatabase
import com.example.data.repository.ContactRepository
import com.example.ui.screens.ContactLandingScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.MyCardQrScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.ScannedContactsScreen
import com.example.ui.screens.WebViewScreen
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoOnBackground
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTile
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ContactViewModel
import com.example.ui.viewmodel.ContactViewModelFactory
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: ContactRepository
    private var pendingDeepLinkPayload: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(applicationContext)
        repository = ContactRepository(database.contactDao())

        // Check if opened via deep link
        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val factory = remember { ContactViewModelFactory(repository) }
                    val contactViewModel: ContactViewModel = viewModel(factory = factory)

                    MainAppNavigation(
                        viewModel = contactViewModel,
                        pendingPayload = pendingDeepLinkPayload,
                        onClearPendingPayload = { pendingDeepLinkPayload = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri: Uri? = intent?.data
        if (uri != null) {
            val scheme = uri.scheme?.lowercase()
            if (scheme == "contactqr") {
                pendingDeepLinkPayload = uri.toString()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String) {
    data object MyCard : Screen("my_card", "My QR")
    data object QrScanner : Screen("qr_scanner", "Scanner")
    data object ScannedContacts : Screen("scanned_contacts", "Received")
    data object LandingPage : Screen("landing_page", "Landing Page")
    data object EditProfile : Screen("edit_profile", "Edit Profile")
    data object WebAppView : Screen("web_app_view", "Web App View")
}

@Composable
fun MainAppNavigation(
    viewModel: ContactViewModel,
    pendingPayload: String?,
    onClearPendingPayload: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }

    val myProfile by viewModel.myProfile.collectAsState()
    val scannedContacts by viewModel.scannedContacts.collectAsState()
    val activeLandingContact by viewModel.activeLandingContact.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    // Listen for UI events / snackbars
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle deep link if app launched from external QR scan
    LaunchedEffect(pendingPayload) {
        if (!pendingPayload.isNullOrBlank()) {
            viewModel.handleScannedQrPayload(context, pendingPayload) { contactId ->
                viewModel.loadContactForLanding(contactId)
                navController.navigate(Screen.LandingPage.route)
            }
            onClearPendingPayload()
        }
    }

    val showBottomBar = currentRoute in listOf(
        Screen.MyCard.route,
        Screen.QrScanner.route,
        Screen.ScannedContacts.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = BentoTile,
                    tonalElevation = 0.dp
                ) {
                    val navItems = listOf(
                        Triple(Screen.MyCard, Icons.Default.QrCode, "My QR"),
                        Triple(Screen.QrScanner, Icons.Default.QrCodeScanner, "Scanner"),
                        Triple(Screen.ScannedContacts, Icons.Default.Contacts, "Received")
                    )

                    navItems.forEach { (screen, icon, label) ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(label, fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BentoPrimary,
                                unselectedIconColor = BentoTextMuted,
                                selectedTextColor = BentoOnBackground,
                                unselectedTextColor = BentoTextMuted,
                                indicatorColor = BentoSecondaryContainer
                            ),
                            modifier = Modifier.testTag("nav_tab_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MyCard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. My Card & QR Screen
            composable(Screen.MyCard.route) {
                MyCardQrScreen(
                    contact = myProfile,
                    viewModel = viewModel,
                    onNavigateToLandingPreview = { profileContact ->
                        viewModel.setActiveLandingContact(profileContact)
                        navController.navigate(Screen.LandingPage.route)
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToScanner = {
                        navController.navigate(Screen.QrScanner.route)
                    },
                    onNavigateToWebApp = {
                        navController.navigate(Screen.WebAppView.route)
                    }
                )
            }

            // 2. QR Scanner Screen
            composable(Screen.QrScanner.route) {
                QrScannerScreen(
                    viewModel = viewModel,
                    onNavigateToLanding = { contactId ->
                        viewModel.loadContactForLanding(contactId)
                        navController.navigate(Screen.LandingPage.route)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 3. Scanned Contacts Archive
            composable(Screen.ScannedContacts.route) {
                ScannedContactsScreen(
                    contacts = scannedContacts,
                    viewModel = viewModel,
                    onNavigateToLanding = { contactItem ->
                        viewModel.setActiveLandingContact(contactItem)
                        navController.navigate(Screen.LandingPage.route)
                    },
                    onNavigateToScanner = {
                        navController.navigate(Screen.QrScanner.route)
                    }
                )
            }

            // 4. Contact Landing Page Screen (Arrived at via QR scan or preview)
            composable(Screen.LandingPage.route) {
                ContactLandingScreen(
                    contact = activeLandingContact,
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 5. Edit Profile Screen
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    currentProfile = myProfile,
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 6. Web App View Screen (Embedded responsive web edition)
            composable(Screen.WebAppView.route) {
                WebViewScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
