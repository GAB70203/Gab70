package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactEntity
import com.example.data.repository.ContactRepository
import com.example.util.PhoneBookHelper
import com.example.util.VCardHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactViewModel(
    private val repository: ContactRepository
) : ViewModel() {

    val myProfile: StateFlow<ContactEntity?> = repository.myProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val scannedContacts: StateFlow<List<ContactEntity>> = repository.scannedContacts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeLandingContact = MutableStateFlow<ContactEntity?>(null)
    val activeLandingContact: StateFlow<ContactEntity?> = _activeLandingContact.asStateFlow()

    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultProfileIfNeeded()
        }
    }

    fun updateMyProfile(updated: ContactEntity) {
        viewModelScope.launch {
            repository.updateContact(updated.copy(isMyProfile = true))
            _uiEvents.emit("Personal profile updated successfully!")
        }
    }

    fun loadContactForLanding(id: Long) {
        viewModelScope.launch {
            if (id == -1L) {
                // View my own profile as landing page
                val my = repository.getMyProfileDirect()
                _activeLandingContact.value = my
            } else {
                val contact = repository.getContactByIdDirect(id)
                _activeLandingContact.value = contact
            }
        }
    }

    fun setActiveLandingContact(contact: ContactEntity) {
        _activeLandingContact.value = contact
    }

    fun handleScannedQrPayload(
        context: Context,
        rawPayload: String,
        onNavigateToLanding: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val parsedContact = VCardHelper.parseQrPayload(rawPayload)
            if (parsedContact == null) {
                _uiEvents.emit("QR code format not recognized as a valid contact.")
                return@launch
            }

            var savedInRubrica = false
            // Check auto-save feature
            if (parsedContact.autoSaveToContacts) {
                if (PhoneBookHelper.hasWriteContactsPermission(context)) {
                    savedInRubrica = PhoneBookHelper.saveDirectlyToPhoneBook(context, parsedContact)
                    if (savedInRubrica) {
                        _uiEvents.emit("✅ Contact ${parsedContact.fullName} automatically saved to contacts!")
                    }
                }
            }

            val contactToSave = parsedContact.copy(
                isMyProfile = false,
                isSavedInPhoneBook = savedInRubrica,
                scannedAt = System.currentTimeMillis()
            )

            val newId = repository.saveContact(contactToSave)
            val finalContact = contactToSave.copy(id = newId)
            _activeLandingContact.value = finalContact

            onNavigateToLanding(newId)
        }
    }

    fun saveContactToPhoneBook(
        context: Context,
        contact: ContactEntity,
        onRequirePermission: () -> Unit
    ) {
        viewModelScope.launch {
            if (PhoneBookHelper.hasWriteContactsPermission(context)) {
                val success = PhoneBookHelper.saveDirectlyToPhoneBook(context, contact)
                if (success) {
                    if (contact.id > 0) {
                        repository.markSavedInPhoneBook(contact.id, true)
                        _activeLandingContact.value = _activeLandingContact.value?.copy(isSavedInPhoneBook = true)
                    }
                    _uiEvents.emit("✅ ${contact.fullName} saved directly to contacts!")
                } else {
                    // Fallback to system intent
                    PhoneBookHelper.openSystemContactInsertIntent(context, contact)
                }
            } else {
                onRequirePermission()
            }
        }
    }

    fun saveViaSystemIntent(context: Context, contact: ContactEntity) {
        PhoneBookHelper.openSystemContactInsertIntent(context, contact)
    }

    fun shareContactVCard(context: Context, contact: ContactEntity) {
        PhoneBookHelper.shareVCard(context, contact)
    }

    fun deleteScannedContact(id: Long) {
        viewModelScope.launch {
            repository.deleteContactById(id)
            _uiEvents.emit("Contact removed from archive.")
        }
    }

    fun createDemoScannedContact(
        context: Context,
        onNavigateToLanding: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val demo = ContactEntity(
                isMyProfile = false,
                firstName = "Elena",
                lastName = "Moretti",
                jobTitle = "Marketing Director & Brand Strategist",
                company = "NextGen Digital Agency",
                phone = "+39 338 5544332",
                phoneWork = "+39 02 44556677",
                email = "elena.moretti@nextgendigital.it",
                website = "https://nextgendigital.it",
                address = "Piazza del Duomo 15, 20121 Milano, Italy",
                bio = "Helping leading brands build a memorable digital presence through omnichannel strategies and visual storytelling.",
                linkedin = "https://linkedin.com/in/elena-moretti-marketing",
                whatsapp = "+393385544332",
                telegram = "@elenamoretti",
                instagram = "@elena.nextgen",
                github = "",
                autoSaveToContacts = true,
                isSavedInPhoneBook = false,
                scannedAt = System.currentTimeMillis()
            )

            var savedInRubrica = false
            if (PhoneBookHelper.hasWriteContactsPermission(context)) {
                savedInRubrica = PhoneBookHelper.saveDirectlyToPhoneBook(context, demo)
                if (savedInRubrica) {
                    _uiEvents.emit("✅ Contact Elena Moretti automatically saved to contacts!")
                }
            }

            val savedContact = demo.copy(isSavedInPhoneBook = savedInRubrica)
            val newId = repository.saveContact(savedContact)
            _activeLandingContact.value = savedContact.copy(id = newId)

            onNavigateToLanding(newId)
        }
    }
}

class ContactViewModelFactory(private val repository: ContactRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
