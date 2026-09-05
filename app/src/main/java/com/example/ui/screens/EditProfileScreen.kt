package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ContactEntity
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoOnBackground
import com.example.ui.theme.BentoOnPrimary
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSecondaryContainer
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTile
import com.example.ui.viewmodel.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentProfile: ContactEntity?,
    viewModel: ContactViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var firstName by remember(currentProfile) { mutableStateOf(currentProfile?.firstName ?: "") }
    var lastName by remember(currentProfile) { mutableStateOf(currentProfile?.lastName ?: "") }
    var jobTitle by remember(currentProfile) { mutableStateOf(currentProfile?.jobTitle ?: "") }
    var company by remember(currentProfile) { mutableStateOf(currentProfile?.company ?: "") }
    var phone by remember(currentProfile) { mutableStateOf(currentProfile?.phone ?: "") }
    var phoneWork by remember(currentProfile) { mutableStateOf(currentProfile?.phoneWork ?: "") }
    var email by remember(currentProfile) { mutableStateOf(currentProfile?.email ?: "") }
    var website by remember(currentProfile) { mutableStateOf(currentProfile?.website ?: "") }
    var address by remember(currentProfile) { mutableStateOf(currentProfile?.address ?: "") }
    var bio by remember(currentProfile) { mutableStateOf(currentProfile?.bio ?: "") }
    var linkedin by remember(currentProfile) { mutableStateOf(currentProfile?.linkedin ?: "") }
    var whatsapp by remember(currentProfile) { mutableStateOf(currentProfile?.whatsapp ?: "") }
    var telegram by remember(currentProfile) { mutableStateOf(currentProfile?.telegram ?: "") }
    var github by remember(currentProfile) { mutableStateOf(currentProfile?.github ?: "") }
    var instagram by remember(currentProfile) { mutableStateOf(currentProfile?.instagram ?: "") }
    var autoSave by remember(currentProfile) { mutableStateOf(currentProfile?.autoSaveToContacts ?: true) }

    Scaffold(
        modifier = modifier.testTag("edit_profile_screen"),
        containerColor = BentoBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit My Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BentoOnBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (firstName.isBlank() && lastName.isBlank()) {
                                Toast.makeText(context, "Enter at least a first or last name", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            val updated = (currentProfile ?: ContactEntity(isMyProfile = true)).copy(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                jobTitle = jobTitle.trim(),
                                company = company.trim(),
                                phone = phone.trim(),
                                phoneWork = phoneWork.trim(),
                                email = email.trim(),
                                website = website.trim(),
                                address = address.trim(),
                                bio = bio.trim(),
                                linkedin = linkedin.trim(),
                                whatsapp = whatsapp.trim(),
                                telegram = telegram.trim(),
                                github = github.trim(),
                                instagram = instagram.trim(),
                                autoSaveToContacts = autoSave
                            )
                            viewModel.updateMyProfile(updated)
                            Toast.makeText(context, "Profile and QR Code updated!", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        modifier = Modifier.testTag("save_profile_top_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = BentoPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoBackground,
                    titleContentColor = BentoOnBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Basic Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            ProfileField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "First Name",
                icon = Icons.Default.Person,
                testTag = "first_name_input"
            )

            ProfileField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Last Name",
                icon = Icons.Default.Person,
                testTag = "last_name_input"
            )

            ProfileField(
                value = jobTitle,
                onValueChange = { jobTitle = it },
                label = "Job Title / Role",
                icon = Icons.Default.Badge,
                testTag = "job_title_input"
            )

            ProfileField(
                value = company,
                onValueChange = { company = it },
                label = "Company / Organization",
                icon = Icons.Default.Apartment,
                testTag = "company_input"
            )

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Phone & Email Contacts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            ProfileField(
                value = phone,
                onValueChange = { phone = it },
                label = "Mobile Phone",
                icon = Icons.Default.Phone,
                testTag = "phone_input"
            )

            ProfileField(
                value = phoneWork,
                onValueChange = { phoneWork = it },
                label = "Office / Work Phone",
                icon = Icons.Default.Phone,
                testTag = "phone_work_input"
            )

            ProfileField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                icon = Icons.Default.Email,
                testTag = "email_input"
            )

            ProfileField(
                value = website,
                onValueChange = { website = it },
                label = "Website / Portfolio URL",
                icon = Icons.Default.Language,
                testTag = "website_input"
            )

            ProfileField(
                value = address,
                onValueChange = { address = it },
                label = "Address / Location (Street, City)",
                icon = Icons.Default.LocationOn,
                testTag = "address_input"
            )

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Bio & Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            ProfileField(
                value = bio,
                onValueChange = { bio = it },
                label = "Bio / Summary",
                icon = Icons.Default.Notes,
                testTag = "bio_input",
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Social & Direct Links",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            ProfileField(
                value = linkedin,
                onValueChange = { linkedin = it },
                label = "LinkedIn Profile (URL or username)",
                icon = Icons.Default.AlternateEmail,
                testTag = "linkedin_input"
            )

            ProfileField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = "WhatsApp Number (+1...)",
                icon = Icons.Default.Phone,
                testTag = "whatsapp_input"
            )

            ProfileField(
                value = telegram,
                onValueChange = { telegram = it },
                label = "Telegram Username (@username)",
                icon = Icons.Default.AlternateEmail,
                testTag = "telegram_input"
            )

            ProfileField(
                value = github,
                onValueChange = { github = it },
                label = "GitHub Profile (URL)",
                icon = Icons.Default.Language,
                testTag = "github_input"
            )

            ProfileField(
                value = instagram,
                onValueChange = { instagram = it },
                label = "Instagram (@username)",
                icon = Icons.Default.AlternateEmail,
                testTag = "instagram_input"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Auto Save switch in QR (Bento Tile)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BentoTile),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Direct Save",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Prompts automatic address book saving when opening the landing page",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = autoSave,
                        onCheckedChange = { autoSave = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BentoPrimary,
                            checkedTrackColor = BentoSecondaryContainer,
                            uncheckedThumbColor = BentoTextMuted,
                            uncheckedTrackColor = BentoTile
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (firstName.isBlank() && lastName.isBlank()) {
                        Toast.makeText(context, "Enter at least a first or last name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val updated = (currentProfile ?: ContactEntity(isMyProfile = true)).copy(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        jobTitle = jobTitle.trim(),
                        company = company.trim(),
                        phone = phone.trim(),
                        phoneWork = phoneWork.trim(),
                        email = email.trim(),
                        website = website.trim(),
                        address = address.trim(),
                        bio = bio.trim(),
                        linkedin = linkedin.trim(),
                        whatsapp = whatsapp.trim(),
                        telegram = telegram.trim(),
                        github = github.trim(),
                        instagram = instagram.trim(),
                        autoSaveToContacts = autoSave
                    )
                    viewModel.updateMyProfile(updated)
                    Toast.makeText(context, "Profile and QR Code updated successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoPrimary,
                    contentColor = BentoOnPrimary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Profile & Update QR Code", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    testTag: String,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = BentoTextMuted) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BentoPrimary,
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BentoPrimary,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = BentoTile,
            unfocusedContainerColor = BentoTile
        ),
        singleLine = singleLine,
        maxLines = maxLines
    )
}
