package com.anisoft.syntecxhub_notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single note row in the "notes" table.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
