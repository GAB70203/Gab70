package com.example.util

import android.Manifest
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.model.ContactEntity
import java.io.File
import java.io.FileOutputStream

object PhoneBookHelper {

    fun hasWriteContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Programmatically inserts the contact directly into the device's Contacts provider.
     * Requires WRITE_CONTACTS permission.
     */
    fun saveDirectlyToPhoneBook(context: Context, contact: ContactEntity): Boolean {
        if (!hasWriteContactsPermission(context)) return false

        return try {
            val ops = ArrayList<ContentProviderOperation>()

            // Step 1: Raw Contact insertion
            val rawContactInsertIndex = ops.size
            ops.add(
                ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_TYPE, null)
                    .withValue(RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            // Step 2: Name
            ops.add(
                ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.GIVEN_NAME, contact.firstName.trim())
                    .withValue(StructuredName.FAMILY_NAME, contact.lastName.trim())
                    .withValue(StructuredName.DISPLAY_NAME, contact.fullName)
                    .build()
            )

            // Step 3: Primary Phone
            if (contact.phone.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, contact.phone.trim())
                        .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                        .build()
                )
            }

            // Step 4: Work Phone
            if (contact.phoneWork.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, contact.phoneWork.trim())
                        .withValue(Phone.TYPE, Phone.TYPE_WORK)
                        .build()
                )
            }

            // Step 5: Email
            if (contact.email.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                        .withValue(Email.ADDRESS, contact.email.trim())
                        .withValue(Email.TYPE, Email.TYPE_WORK)
                        .build()
                )
            }

            // Step 6: Organization & Title
            if (contact.company.isNotBlank() || contact.jobTitle.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                        .withValue(Organization.COMPANY, contact.company.trim())
                        .withValue(Organization.TITLE, contact.jobTitle.trim())
                        .withValue(Organization.TYPE, Organization.TYPE_WORK)
                        .build()
                )
            }

            // Step 7: Website
            if (contact.website.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE)
                        .withValue(Website.URL, contact.website.trim())
                        .withValue(Website.TYPE, Website.TYPE_HOMEPAGE)
                        .build()
                )
            }

            // Step 8: Postal Address
            if (contact.address.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(StructuredPostal.FORMATTED_ADDRESS, contact.address.trim())
                        .withValue(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK)
                        .build()
                )
            }

            // Step 9: Notes / Bio & Social handles
            val notesBuilder = StringBuilder()
            if (contact.bio.isNotBlank()) {
                notesBuilder.append(contact.bio).append("\n\n")
            }
            if (contact.linkedin.isNotBlank()) notesBuilder.append("LinkedIn: ").append(contact.linkedin).append("\n")
            if (contact.telegram.isNotBlank()) notesBuilder.append("Telegram: ").append(contact.telegram).append("\n")
            if (contact.github.isNotBlank()) notesBuilder.append("GitHub: ").append(contact.github).append("\n")
            if (contact.instagram.isNotBlank()) notesBuilder.append("Instagram: ").append(contact.instagram).append("\n")

            val finalNotes = notesBuilder.toString().trim()
            if (finalNotes.isNotBlank()) {
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
                        .withValue(Note.NOTE, finalNotes)
                        .build()
                )
            }

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Fallback to system Contact insert Intent (Works with zero permissions).
     * Opens the device's native Contacts app with fields pre-filled.
     */
    fun openSystemContactInsertIntent(context: Context, contact: ContactEntity) {
        try {
            val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                putExtra(ContactsContract.Intents.Insert.NAME, contact.fullName)
                if (contact.phone.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.PHONE, contact.phone)
                    putExtra(
                        ContactsContract.Intents.Insert.PHONE_TYPE,
                        CommonDataKinds.Phone.TYPE_MOBILE
                    )
                }
                if (contact.phoneWork.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, contact.phoneWork)
                    putExtra(
                        ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
                        CommonDataKinds.Phone.TYPE_WORK
                    )
                }
                if (contact.email.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.EMAIL, contact.email)
                    putExtra(
                        ContactsContract.Intents.Insert.EMAIL_TYPE,
                        CommonDataKinds.Email.TYPE_WORK
                    )
                }
                if (contact.company.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.COMPANY, contact.company)
                }
                if (contact.jobTitle.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.JOB_TITLE, contact.jobTitle)
                }
                if (contact.address.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.POSTAL, contact.address)
                }
                if (contact.bio.isNotBlank()) {
                    putExtra(ContactsContract.Intents.Insert.NOTES, contact.bio)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Export vCard (.vcf) and share or open it.
     */
    fun shareVCard(context: Context, contact: ContactEntity) {
        try {
            val vcardContent = VCardHelper.generateVCard(contact)
            val fileName = "${contact.fullName.replace(" ", "_")}.vcf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { it.write(vcardContent.toByteArray()) }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/x-vcard"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Contact: ${contact.fullName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share vCard Contact").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            // Fallback to text share
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, VCardHelper.generateVCard(contact))
                putExtra(Intent.EXTRA_SUBJECT, "Contact: ${contact.fullName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Contact").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
