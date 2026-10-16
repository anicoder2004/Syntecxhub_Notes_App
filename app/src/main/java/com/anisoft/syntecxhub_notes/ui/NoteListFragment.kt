package com.anisoft.syntecxhub_notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anisoft.syntecxhub_notes.R
import com.anisoft.syntecxhub_notes.data.Note
import com.anisoft.syntecxhub_notes.databinding.FragmentNoteListBinding
import com.anisoft.syntecxhub_notes.utils.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Displays the list of notes with real-time search, pin/archive toggles and swipe-to-delete.
 */
class NoteListFragment : Fragment() {

    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        MainViewModel.factory(requireContext().applicationContext)
    }

    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setTitle(R.string.app_name)
        binding.toolbar.inflateMenu(R.menu.menu_note_list)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_show_archived) {
                viewModel.toggleArchiveFilter()
                true
            } else {
                false
            }
        }

        setupRecyclerView()
        setupSearch()
        setupFab()
        observeViewModel()
    }

    private fun syncArchiveMenu(archived: Boolean) {
        val item = binding.toolbar.menu.findItem(R.id.action_show_archived) ?: return
        item.isChecked = archived
        item.title = getString(if (archived) R.string.show_active else R.string.show_archived)
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { note -> openNoteDetail(note.id) },
            onPinClick = { note -> viewModel.togglePin(note) },
            onLongClick = { note -> showNoteOptions(note) }
        )
        binding.recyclerNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerNotes.adapter = adapter

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = adapter.currentList[viewHolder.bindingAdapterPosition]
                viewModel.deleteNote(note)
                Snackbar.make(binding.root, R.string.note_deleted, Snackbar.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerNotes)
    }

    private fun setupSearch() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.setQuery(text?.toString().orEmpty())
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener { openNoteDetail(Constants.NO_ID) }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.notes.collect { notes ->
                        adapter.submitList(notes)
                        binding.emptyView.visibility =
                            if (notes.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.showArchived.collect { archived ->
                        binding.toolbar.subtitle =
                            getString(if (archived) R.string.archived_notes else R.string.active_notes)
                        syncArchiveMenu(archived)
                    }
                }
            }
        }
    }

    private fun openNoteDetail(noteId: Long) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NoteDetailFragment.newInstance(noteId))
            .addToBackStack(null)
            .commit()
    }

    private fun showNoteOptions(note: Note) {
        val options = arrayOf(
            getString(if (note.isPinned) R.string.unpin else R.string.pin),
            getString(if (note.isArchived) R.string.unarchive else R.string.archive),
            getString(R.string.delete)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(note.title.ifBlank { getString(R.string.untitled_note) })
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.togglePin(note)
                    1 -> viewModel.toggleArchive(note)
                    2 -> confirmDelete(note)
                }
            }
            .show()
    }

    private fun confirmDelete(note: Note) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_note)
            .setMessage(R.string.delete_note_message)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteNote(note) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
