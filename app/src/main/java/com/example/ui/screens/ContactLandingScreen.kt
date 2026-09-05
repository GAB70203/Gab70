package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactEntity
import com.example.ui.theme.AccentGitHub
import com.example.ui.theme.AccentInstagram
import com.example.ui.theme.AccentLinkedIn
import com.example.ui.theme.AccentTelegram
import com.example.ui.theme.AccentWhatsApp
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoOnBackground
import com.example.ui.theme.BentoOnPrimary
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryActive
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTile
import com.example.ui.theme.BentoTileActive
import com.example.ui.viewmodel.ContactViewModel
import com.example.util.PhoneBookHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactLandingScreen(
    contact: ContactEntity?,
    viewModel: ContactViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isSavedLocally by remember(contact?.id, contact?.isSavedInPhoneBook) {
        mutableStateOf(contact?.isSavedInPhoneBook ?: false)
    }

    // Permission launcher for saving contacts directly
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (contact != null) {
            if (isGranted) {
                val success = PhoneBookHelper.saveDirectlyToPhoneBook(context, contact)
                if (success) {
                    isSavedLocally = true
                    viewModel.saveContactToPhoneBook(context, contact) {}
                    Toast.makeText(context, "✅ Contact saved successfully to contacts!", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.saveViaSystemIntent(context, contact)
                }
            } else {
                Toast.makeText(context, "Opening system Contacts app...", Toast.LENGTH_SHORT).show()
                viewModel.saveViaSystemIntent(context, contact)
            }
        }
    }

    // Auto-save logic if enabled and permission already granted
    LaunchedEffect(contact?.id) {
        if (contact != null && contact.autoSaveToContacts && !contact.isSavedInPhoneBook && !contact.isMyProfile) {
            if (PhoneBookHelper.hasWriteContactsPermission(context)) {
                val autoSaved = PhoneBookHelper.saveDirectlyToPhoneBook(context, contact)
                if (autoSaved) {
                    isSavedLocally = true
                    viewModel.saveContactToPhoneBook(context, contact) {}
                }
            }
        }
    }

    if (contact == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Contact Landing Page") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No contact details available.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.testTag("contact_landing_screen"),
        containerColor = BentoBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (contact.isMyProfile) "My Landing Page Preview" else "Contact Landing Page",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("landing_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = BentoOnBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.shareContactVCard(context, contact) },
                        modifier = Modifier.testTag("landing_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share contact",
                            tint = BentoOnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoBackground,
                    titleContentColor = BentoOnBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BentoBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bento Avatar: w-28 h-28 (112dp), bg #EADDFF, border-4 white, shadow-sm, inner #6750A4
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(BentoPrimaryContainer)
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(BentoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.initials,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoOnPrimary
                            )
                        }
                    }

                    // Verified badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = BentoPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Name
                Text(
                    text = contact.fullName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnBackground,
                    textAlign = TextAlign.Center
                )

                // Job Title & Company
                val jobAndCompany = listOf(contact.jobTitle, contact.company).filter { it.isNotBlank() }.joinToString(" • ")
                if (jobAndCompany.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = jobAndCompany,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = BentoTextMuted,
                        textAlign = TextAlign.Center
                    )
                }

                if (contact.isMyProfile) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = BentoSecondaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Your Personal Profile",
                            color = BentoOnPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Quick Action Bar (Call, WhatsApp, Email, Web, Map)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (contact.phone.isNotBlank()) {
                        QuickActionButton(
                            icon = Icons.Default.Call,
                            label = "Call",
                            containerColor = BentoSecondaryContainer,
                            contentColor = BentoOnPrimaryContainer,
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone.trim()}"))
                                context.startActivity(intent)
                            }
                        )
                    }

                    if (contact.whatsapp.isNotBlank() || contact.phone.isNotBlank()) {
                        val waNumber = if (contact.whatsapp.isNotBlank()) contact.whatsapp else contact.phone
                        QuickActionButton(
                            icon = Icons.Default.AlternateEmail,
                            label = "WhatsApp",
                            containerColor = AccentWhatsApp.copy(alpha = 0.16f),
                            contentColor = AccentWhatsApp,
                            onClick = {
                                openWhatsApp(context, waNumber)
                            }
                        )
                    }

                    if (contact.email.isNotBlank()) {
                        QuickActionButton(
                            icon = Icons.Default.Email,
                            label = "Email",
                            containerColor = BentoPrimaryContainer,
                            contentColor = BentoPrimary,
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contact.email.trim()}"))
                                context.startActivity(intent)
                            }
                        )
                    }

                    if (contact.website.isNotBlank()) {
                        QuickActionButton(
                            icon = Icons.Default.Language,
                            label = "Website",
                            containerColor = BentoPrimaryContainer,
                            contentColor = BentoPrimary,
                            onClick = {
                                openUrl(context, contact.website)
                            }
                        )
                    }

                    if (contact.address.isNotBlank()) {
                        QuickActionButton(
                            icon = Icons.Default.LocationOn,
                            label = "Map",
                            containerColor = BentoTile,
                            contentColor = BentoOnBackground,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(contact.address)}"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // BENTO GRID TILES CONTAINER
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Col-span-2: Telefono Tile
                if (contact.phone.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone.trim()}"))
                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = BentoTile
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone",
                                    tint = BentoOnPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PHONE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = BentoTextMuted
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = contact.phone,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoOnBackground
                                )
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(contact.phone))
                                Toast.makeText(context, "Phone number copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy number",
                                    tint = BentoTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 2-Column Row: Email & Sito
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Col 1: Email
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = contact.email.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contact.email.trim()}"))
                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = BentoTile
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "EMAIL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = BentoTextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (contact.email.isNotBlank()) contact.email else "None",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoOnBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Col 2: Sito
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = contact.website.isNotBlank()) {
                                openUrl(context, contact.website)
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = BentoTile
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Website",
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "WEBSITE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = BentoTextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val siteDisplay = if (contact.website.isNotBlank()) {
                                contact.website.removePrefix("https://").removePrefix("http://")
                            } else {
                                "None"
                            }
                            Text(
                                text = siteDisplay,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoOnBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Col-span-2: Ufficio / Sede
                if (contact.address.isNotBlank() || contact.company.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = contact.address.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(contact.address)}"))
                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = BentoTile
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Office",
                                    tint = BentoOnPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "OFFICE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = BentoTextMuted
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val locationText = listOf(contact.address, contact.company).filter { it.isNotBlank() }.joinToString(" • ")
                                Text(
                                    text = locationText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoOnBackground,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (contact.address.isNotBlank()) {
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(contact.address))
                                    Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy address",
                                        tint = BentoTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Col-span-2: Telefono Ufficio / Lavoro
                if (contact.phoneWork.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneWork.trim()}"))
                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = BentoTile
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = "Work phone",
                                    tint = BentoOnPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "WORK PHONE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = BentoTextMuted
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = contact.phoneWork,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoOnBackground
                                )
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(contact.phoneWork))
                                Toast.makeText(context, "Work phone copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy work phone",
                                    tint = BentoTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Col-span-2: Bio & Presentazione Tile
                if (contact.bio.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = BentoTile
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "ABOUT & BIO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = BentoTextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = contact.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoOnBackground,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Col-span-2: Social Profiles Bento Tile
                val hasSocial = contact.linkedin.isNotBlank() ||
                        contact.whatsapp.isNotBlank() ||
                        contact.telegram.isNotBlank() ||
                        contact.github.isNotBlank() ||
                        contact.instagram.isNotBlank()

                if (hasSocial) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = BentoTile
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "SOCIAL CHANNELS & NETWORKS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = BentoTextMuted
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (contact.linkedin.isNotBlank()) {
                                SocialChannelItem(
                                    name = "LinkedIn",
                                    handle = contact.linkedin,
                                    badgeColor = AccentLinkedIn,
                                    onClick = { openUrl(context, "https://linkedin.com/in/${contact.linkedin.trim()}") }
                                )
                            }

                            if (contact.whatsapp.isNotBlank()) {
                                SocialChannelItem(
                                    name = "WhatsApp",
                                    handle = contact.whatsapp,
                                    badgeColor = AccentWhatsApp,
                                    onClick = { openWhatsApp(context, contact.whatsapp) }
                                )
                            }

                            if (contact.telegram.isNotBlank()) {
                                SocialChannelItem(
                                    name = "Telegram",
                                    handle = "@${contact.telegram.trim().removePrefix("@")}",
                                    badgeColor = AccentTelegram,
                                    onClick = {
                                        val tgHandle = contact.telegram.removePrefix("@")
                                        openUrl(context, "https://t.me/$tgHandle")
                                    }
                                )
                            }

                            if (contact.github.isNotBlank()) {
                                SocialChannelItem(
                                    name = "GitHub",
                                    handle = contact.github,
                                    badgeColor = Color(0xFF24292E),
                                    onClick = { openUrl(context, "https://github.com/${contact.github.trim()}") }
                                )
                            }

                            if (contact.instagram.isNotBlank()) {
                                SocialChannelItem(
                                    name = "Instagram",
                                    handle = "@${contact.instagram.trim().removePrefix("@")}",
                                    badgeColor = Color(0xFFE1306C),
                                    onClick = {
                                        val igHandle = contact.instagram.removePrefix("@")
                                        openUrl(context, "https://instagram.com/$igHandle")
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // PRIMARY ACTION: SALVA IN RUBRICA (BENTO CTA)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_to_contacts_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (PhoneBookHelper.hasWriteContactsPermission(context)) {
                                val ok = PhoneBookHelper.saveDirectlyToPhoneBook(context, contact)
                                if (ok) {
                                    isSavedLocally = true
                                    viewModel.saveContactToPhoneBook(context, contact) {}
                                    Toast.makeText(context, "✅ Contact saved directly to contacts!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveViaSystemIntent(context, contact)
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.WRITE_CONTACTS)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("save_to_contacts_primary_button"),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPrimary,
                            contentColor = BentoOnPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = "+",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isSavedLocally) "Saved to Contacts (Resave)" else "Save to Contacts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color = BentoOnPrimary
                        )
                    }

                    // Secondary Supporting Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.saveViaSystemIntent(context, contact) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("save_system_intent_button"),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, BentoPrimary.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary)
                        ) {
                            Text("Open Contacts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.shareContactVCard(context, contact) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("export_vcard_button"),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.5.dp, BentoPrimary.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary)
                        ) {
                            Text("Export .vcf", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Scansionato via QR Code Footer
                    Text(
                        text = "SCANNED VIA QR CODE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = BentoTextMuted.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = BentoOnBackground
        )
    }
}

@Composable
fun ContactDetailRow(
    icon: ImageVector,
    title: String,
    value: String,
    onAction: (() -> Unit)?,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onAction != null, onClick = { onAction?.invoke() })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy to clipboard",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SocialChannelItem(
    name: String,
    handle: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.first().uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BentoOnBackground
            )
            Text(
                text = handle,
                style = MaterialTheme.typography.bodySmall,
                color = BentoPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val fixedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
    }
}

private fun openWhatsApp(context: Context, phoneOrHandle: String) {
    try {
        val cleanNumber = phoneOrHandle.replace("+", "").replace(" ", "").replace("-", "")
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open WhatsApp", Toast.LENGTH_SHORT).show()
    }
}
