package com.anisoft.syntecxhub_notes.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anisoft.syntecxhub_notes.data.Note
import com.anisoft.syntecxhub_notes.data.NoteDatabase
import com.anisoft.syntecxhub_notes.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds UI state for the note list and mediates all repository interaction.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(private val repository: NoteRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val notes: StateFlow<List<Note>> =
        combine(query, _showArchived) { q, archived -> q to archived }
            .flatMapLatest { (q, archived) -> repository.observeNotes(q, archived) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun setQuery(newQuery: String) {
        query.value = newQuery
    }

    fun toggleArchiveFilter() {
        _showArchived.value = !_showArchived.value
    }

    /** Loads a single note for editing; invokes [onResult] on the main thread. */
    fun loadNote(id: Long, onResult: (Note?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getNote(id))
        }
    }

    fun saveNote(note: Note, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveNote(note)
            onSaved()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.saveNote(
                note.copy(
                    isPinned = !note.isPinned,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleArchive(note: Note) {
        viewModelScope.launch {
            repository.saveNote(
                note.copy(
                    isArchived = !note.isArchived,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val dao = NoteDatabase.getInstance(context).noteDao()
                MainViewModel(NoteRepository(dao))
            }
        }
    }
}
