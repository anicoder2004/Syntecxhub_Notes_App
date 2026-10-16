package com.anisoft.syntecxhub_notes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object exposing CRUD operations and reactive Flow queries for [Note].
 */
@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun observeActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun observeArchivedNotes(): Flow<List<Note>>

    @Query(
        """
        SELECT * FROM notes
        WHERE isArchived = :archived
          AND (
                title LIKE '%' || :query || '%'
                OR content LIKE '%' || :query || '%'
                OR category LIKE '%' || :query || '%'
              )
        ORDER BY isPinned DESC, updatedAt DESC
        """
    )
    fun searchNotes(query: String, archived: Boolean): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
