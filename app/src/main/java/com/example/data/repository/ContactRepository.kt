package com.example.data.repository

import com.example.data.db.ContactDao
import com.example.data.model.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    val myProfile: Flow<ContactEntity?> = contactDao.getMyProfile()
    val scannedContacts: Flow<List<ContactEntity>> = contactDao.getAllScannedContacts()

    suspend fun initializeDefaultProfileIfNeeded() {
        val existing = contactDao.getMyProfileDirect()
        if (existing == null) {
            val defaultProfile = ContactEntity(
                isMyProfile = true,
                firstName = "Gabriele",
                lastName = "Marzano",
                jobTitle = "Product Manager & Tech Consultant",
                company = "Marzano Innovation Lab",
                phone = "+39 347 9812345",
                phoneWork = "+39 02 7654321",
                email = "gabrielemarzano01@gmail.com",
                website = "https://gabrielemarzano.dev",
                address = "Via Monte Napoleone 8, 20121 Milano, Italia",
                bio = "Passionate about digital innovation, mobile product development, and tech solutions for businesses and professionals.",
                linkedin = "https://linkedin.com/in/gabriele-marzano",
                whatsapp = "+393479812345",
                telegram = "@gabrielemarzano",
                github = "https://github.com/gabrielemarzano",
                instagram = "@gabriele.tech",
                autoSaveToContacts = true
            )
            contactDao.insert(defaultProfile)
        }
    }

    fun getContactById(id: Long): Flow<ContactEntity?> = contactDao.getContactById(id)

    suspend fun getMyProfileDirect(): ContactEntity? = contactDao.getMyProfileDirect()

    suspend fun getContactByIdDirect(id: Long): ContactEntity? = contactDao.getContactByIdDirect(id)

    suspend fun saveContact(contact: ContactEntity): Long = contactDao.insert(contact)

    suspend fun updateContact(contact: ContactEntity) = contactDao.update(contact)

    suspend fun deleteContact(contact: ContactEntity) = contactDao.delete(contact)

    suspend fun deleteContactById(id: Long) = contactDao.deleteById(id)

    suspend fun markSavedInPhoneBook(id: Long, saved: Boolean) = contactDao.updateSavedInPhoneBook(id, saved)
}
