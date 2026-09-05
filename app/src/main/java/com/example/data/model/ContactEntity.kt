package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val isMyProfile: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val phone: String = "",
    val phoneWork: String = "",
    val email: String = "",
    val website: String = "",
    val address: String = "",
    val bio: String = "",
    val linkedin: String = "",
    val whatsapp: String = "",
    val telegram: String = "",
    val github: String = "",
    val instagram: String = "",
    val autoSaveToContacts: Boolean = true,
    val isSavedInPhoneBook: Boolean = false,
    val scannedAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = when {
            firstName.isNotBlank() && lastName.isNotBlank() -> "$firstName $lastName"
            firstName.isNotBlank() -> firstName
            lastName.isNotBlank() -> lastName
            else -> "Contact"
        }

    val initials: String
        get() {
            val f = firstName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
            val l = lastName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
            val result = "$f$l"
            return if (result.isNotEmpty()) result else "CQ"
        }
}
