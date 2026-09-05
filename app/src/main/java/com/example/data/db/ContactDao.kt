package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE isMyProfile = 1 LIMIT 1")
    fun getMyProfile(): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE isMyProfile = 1 LIMIT 1")
    suspend fun getMyProfileDirect(): ContactEntity?

    @Query("SELECT * FROM contacts WHERE isMyProfile = 0 ORDER BY scannedAt DESC")
    fun getAllScannedContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    fun getContactById(id: Long): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactByIdDirect(id: Long): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE contacts SET isSavedInPhoneBook = :saved WHERE id = :id")
    suspend fun updateSavedInPhoneBook(id: Long, saved: Boolean)
}
