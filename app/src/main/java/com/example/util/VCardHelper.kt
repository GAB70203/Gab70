package com.example.util

import android.net.Uri
import com.example.data.model.ContactEntity
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object VCardHelper {

    fun generateVCard(contact: ContactEntity): String {
        val sb = StringBuilder()
        sb.append("BEGIN:VCARD\n")
        sb.append("VERSION:3.0\n")
        sb.append("N:${escapeVCard(contact.lastName)};${escapeVCard(contact.firstName)};;;\n")
        sb.append("FN:${escapeVCard(contact.fullName)}\n")
        if (contact.company.isNotBlank()) {
            sb.append("ORG:${escapeVCard(contact.company)}\n")
        }
        if (contact.jobTitle.isNotBlank()) {
            sb.append("TITLE:${escapeVCard(contact.jobTitle)}\n")
        }
        if (contact.phone.isNotBlank()) {
            sb.append("TEL;TYPE=CELL:${contact.phone.trim()}\n")
        }
        if (contact.phoneWork.isNotBlank()) {
            sb.append("TEL;TYPE=WORK:${contact.phoneWork.trim()}\n")
        }
        if (contact.email.isNotBlank()) {
            sb.append("EMAIL;TYPE=INTERNET:${contact.email.trim()}\n")
        }
        if (contact.website.isNotBlank()) {
            sb.append("URL:${contact.website.trim()}\n")
        }
        if (contact.address.isNotBlank()) {
            sb.append("ADR;TYPE=WORK:;;${escapeVCard(contact.address)};;;;\n")
        }
        if (contact.bio.isNotBlank()) {
            sb.append("NOTE:${escapeVCard(contact.bio)}\n")
        }
        if (contact.linkedin.isNotBlank()) {
            sb.append("X-SOCIALPROFILE;type=linkedin:${contact.linkedin.trim()}\n")
        }
        if (contact.whatsapp.isNotBlank()) {
            sb.append("X-SOCIALPROFILE;type=whatsapp:${contact.whatsapp.trim()}\n")
        }
        if (contact.telegram.isNotBlank()) {
            sb.append("X-SOCIALPROFILE;type=telegram:${contact.telegram.trim()}\n")
        }
        if (contact.github.isNotBlank()) {
            sb.append("X-SOCIALPROFILE;type=github:${contact.github.trim()}\n")
        }
        if (contact.instagram.isNotBlank()) {
            sb.append("X-SOCIALPROFILE;type=instagram:${contact.instagram.trim()}\n")
        }
        sb.append("END:VCARD")
        return sb.toString()
    }

    private fun escapeVCard(value: String): String {
        return value.replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n")
    }

    private fun unescapeVCard(value: String): String {
        return value.replace("\\n", "\n")
            .replace("\\;", ";")
            .replace("\\,", ",")
            .replace("\\\\", "\\")
    }

    fun generateDeepLink(contact: ContactEntity): String {
        fun encode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.name())
        return "contactqr://card?" +
                "fn=${encode(contact.firstName)}&" +
                "ln=${encode(contact.lastName)}&" +
                "jt=${encode(contact.jobTitle)}&" +
                "co=${encode(contact.company)}&" +
                "ph=${encode(contact.phone)}&" +
                "phw=${encode(contact.phoneWork)}&" +
                "em=${encode(contact.email)}&" +
                "web=${encode(contact.website)}&" +
                "adr=${encode(contact.address)}&" +
                "bio=${encode(contact.bio)}&" +
                "li=${encode(contact.linkedin)}&" +
                "wa=${encode(contact.whatsapp)}&" +
                "tg=${encode(contact.telegram)}&" +
                "gh=${encode(contact.github)}&" +
                "ig=${encode(contact.instagram)}&" +
                "auto=${contact.autoSaveToContacts}"
    }

    fun parseQrPayload(payload: String): ContactEntity? {
        val trimmed = payload.trim()
        return when {
            trimmed.startsWith("contactqr://") || trimmed.contains("contactqr.app/card") -> {
                parseDeepLink(trimmed)
            }
            trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) -> {
                parseVCard(trimmed)
            }
            trimmed.startsWith("MECARD:", ignoreCase = true) -> {
                parseMeCard(trimmed)
            }
            else -> {
                // If it's a raw URL or text, try to extract whatever possible
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    ContactEntity(
                        website = trimmed,
                        firstName = "Web",
                        lastName = "Link",
                        bio = "Scanned from QR code"
                    )
                } else {
                    null
                }
            }
        }
    }

    fun parseDeepLink(uriString: String): ContactEntity? {
        return try {
            val uri = Uri.parse(uriString)
            fun param(key: String): String = uri.getQueryParameter(key) ?: ""

            ContactEntity(
                isMyProfile = false,
                firstName = param("fn"),
                lastName = param("ln"),
                jobTitle = param("jt"),
                company = param("co"),
                phone = param("ph"),
                phoneWork = param("phw"),
                email = param("em"),
                website = param("web"),
                address = param("adr"),
                bio = param("bio"),
                linkedin = param("li"),
                whatsapp = param("wa"),
                telegram = param("tg"),
                github = param("gh"),
                instagram = param("ig"),
                autoSaveToContacts = uri.getQueryParameter("auto")?.toBooleanStrictOrNull() ?: true,
                scannedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseVCard(vcardText: String): ContactEntity? {
        return try {
            var firstName = ""
            var lastName = ""
            var fullName = ""
            var company = ""
            var jobTitle = ""
            var phone = ""
            var phoneWork = ""
            var email = ""
            var website = ""
            var address = ""
            var bio = ""
            var linkedin = ""
            var whatsapp = ""
            var telegram = ""
            var github = ""
            var instagram = ""

            val lines = vcardText.lines()
            for (rawLine in lines) {
                val line = rawLine.trim()
                val colonIdx = line.indexOf(':')
                if (colonIdx <= 0) continue

                val prefix = line.substring(0, colonIdx).uppercase()
                val value = unescapeVCard(line.substring(colonIdx + 1).trim())

                when {
                    prefix == "FN" -> fullName = value
                    prefix.startsWith("N") -> {
                        val parts = value.split(';')
                        if (parts.isNotEmpty()) lastName = parts[0]
                        if (parts.size > 1) firstName = parts[1]
                    }
                    prefix.startsWith("ORG") -> company = value
                    prefix.startsWith("TITLE") -> jobTitle = value
                    prefix.startsWith("TEL") -> {
                        if (prefix.contains("WORK")) {
                            phoneWork = value
                        } else if (phone.isEmpty()) {
                            phone = value
                        } else {
                            phoneWork = value
                        }
                    }
                    prefix.startsWith("EMAIL") -> email = value
                    prefix.startsWith("URL") -> website = value
                    prefix.startsWith("ADR") -> {
                        val adrParts = value.split(';').filter { it.isNotBlank() }
                        address = adrParts.joinToString(", ")
                    }
                    prefix.startsWith("NOTE") -> bio = value
                    prefix.contains("LINKEDIN") -> linkedin = value
                    prefix.contains("WHATSAPP") -> whatsapp = value
                    prefix.contains("TELEGRAM") -> telegram = value
                    prefix.contains("GITHUB") -> github = value
                    prefix.contains("INSTAGRAM") -> instagram = value
                }
            }

            if (firstName.isBlank() && lastName.isBlank() && fullName.isNotBlank()) {
                val split = fullName.split(" ", limit = 2)
                firstName = split.getOrElse(0) { "" }
                lastName = split.getOrElse(1) { "" }
            }

            ContactEntity(
                isMyProfile = false,
                firstName = firstName,
                lastName = lastName,
                company = company,
                jobTitle = jobTitle,
                phone = phone,
                phoneWork = phoneWork,
                email = email,
                website = website,
                address = address,
                bio = bio,
                linkedin = linkedin,
                whatsapp = whatsapp,
                telegram = telegram,
                github = github,
                instagram = instagram,
                autoSaveToContacts = true,
                scannedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseMeCard(meCard: String): ContactEntity? {
        return try {
            val content = meCard.removePrefix("MECARD:").removeSuffix(";")
            val fields = content.split(";")
            var name = ""
            var phone = ""
            var email = ""
            var url = ""
            var adr = ""
            var note = ""

            for (field in fields) {
                if (field.startsWith("N:")) name = field.removePrefix("N:")
                if (field.startsWith("TEL:")) phone = field.removePrefix("TEL:")
                if (field.startsWith("EMAIL:")) email = field.removePrefix("EMAIL:")
                if (field.startsWith("URL:")) url = field.removePrefix("URL:")
                if (field.startsWith("ADR:")) adr = field.removePrefix("ADR:")
                if (field.startsWith("NOTE:")) note = field.removePrefix("NOTE:")
            }

            val nameParts = name.split(",", limit = 2)
            val lastName = nameParts.getOrElse(0) { "" }
            val firstName = nameParts.getOrElse(1) { "" }

            ContactEntity(
                isMyProfile = false,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phone = phone.trim(),
                email = email.trim(),
                website = url.trim(),
                address = adr.trim(),
                bio = note.trim(),
                autoSaveToContacts = true,
                scannedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
