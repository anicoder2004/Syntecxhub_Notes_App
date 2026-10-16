package com.anisoft.syntecxhub_notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.anisoft.syntecxhub_notes.R
import com.anisoft.syntecxhub_notes.data.Note
import com.anisoft.syntecxhub_notes.databinding.FragmentNoteDetailBinding
import com.anisoft.syntecxhub_notes.utils.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment used to create a new note or edit an existing one.
 */
class NoteDetailFragment : Fragment() {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        MainViewModel.factory(requireContext().applicationContext)
    }

    private var noteId: Long = Constants.NO_ID
    private var existingNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteId = arguments?.getLong(Constants.EXTRA_NOTE_ID, Constants.NO_ID) ?: Constants.NO_ID

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.toolbar.setTitle(
            if (noteId == Constants.NO_ID) R.string.new_note else R.string.edit_note
        )
        binding.toolbar.inflateMenu(R.menu.menu_note_detail)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete) {
                deleteNote()
                true
            } else {
                false
            }
        }
        binding.toolbar.menu.findItem(R.id.action_delete).isVisible = noteId != Constants.NO_ID

        binding.fabSave.setOnClickListener { saveNote() }

        if (noteId != Constants.NO_ID) {
            viewModel.loadNote(noteId) { note ->
                note?.let { populate(it) }
            }
        }
    }

    private fun populate(note: Note) {
        existingNote = note
        binding.editTitle.setText(note.title)
        binding.editContent.setText(note.content)
        binding.editCategory.setText(note.category)
        binding.toolbar.menu.findItem(R.id.action_delete).isVisible = true
    }

    private fun saveNote() {
        val title = binding.editTitle.text?.toString()?.trim().orEmpty()
        val content = binding.editContent.text?.toString()?.trim().orEmpty()
        val category = binding.editCategory.text?.toString()?.trim().orEmpty()

        if (title.isEmpty() && content.isEmpty()) {
            Snackbar.make(binding.root, R.string.empty_note_warning, Snackbar.LENGTH_SHORT).show()
            return
        }

        val now = System.currentTimeMillis()
        val note = existingNote?.copy(
            title = title,
            content = content,
            category = category,
            updatedAt = now
        ) ?: Note(
            title = title,
            content = content,
            category = category,
            createdAt = now,
            updatedAt = now
        )

        viewModel.saveNote(note) {
            parentFragmentManager.popBackStack()
        }
    }

    private fun deleteNote() {
        val note = existingNote ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_note)
            .setMessage(R.string.delete_note_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteNote(note)
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(noteId: Long): NoteDetailFragment =
            NoteDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(Constants.EXTRA_NOTE_ID, noteId)
                }
            }
    }
}
