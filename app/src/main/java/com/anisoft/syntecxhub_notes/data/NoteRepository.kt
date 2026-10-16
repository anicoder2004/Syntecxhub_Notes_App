package com.anisoft.syntecxhub_notes.data

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth that abstracts the [NoteDao] away from the UI layer.
 */
class NoteRepository(private val noteDao: NoteDao) {

    fun observeNotes(query: String, showArchived: Boolean): Flow<List<Note>> {
        val trimmed = query.trim()
        return when {
            trimmed.isEmpty() && showArchived -> noteDao.observeArchivedNotes()
            trimmed.isEmpty() -> noteDao.observeActiveNotes()
            else -> noteDao.searchNotes(trimmed, showArchived)
        }
    }

    suspend fun getNote(id: Long): Note? = noteDao.getNoteById(id)

    /** Inserts a new note or updates an existing one, returning the row id. */
    suspend fun saveNote(note: Note): Long =
        if (note.id == 0L) {
            noteDao.insert(note)
        } else {
            noteDao.update(note)
            note.id
        }

    suspend fun deleteNote(note: Note) = noteDao.delete(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteById(id)
}
