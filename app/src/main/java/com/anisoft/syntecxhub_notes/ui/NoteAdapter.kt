package com.anisoft.syntecxhub_notes.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anisoft.syntecxhub_notes.data.Note
import com.anisoft.syntecxhub_notes.databinding.ItemNoteBinding
import com.anisoft.syntecxhub_notes.utils.asPreview
import com.anisoft.syntecxhub_notes.utils.toReadableDate

/**
 * RecyclerView [ListAdapter] rendering notes with [DiffUtil] for smooth, granular updates.
 */
class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onPinClick: (Note) -> Unit,
    private val onLongClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) = with(binding) {
            textTitle.text = note.title.ifBlank { root.context.getString(android.R.string.untitled) }
            textContent.text = note.content.asPreview()
            textDate.text = note.updatedAt.toReadableDate()

            if (note.category.isBlank()) {
                textCategory.visibility = android.view.View.GONE
            } else {
                textCategory.visibility = android.view.View.VISIBLE
                textCategory.text = note.category
            }

            imagePin.setImageResource(
                if (note.isPinned) {
                    android.R.drawable.btn_star_big_on
                } else {
                    android.R.drawable.btn_star_big_off
                }
            )

            root.setOnClickListener { onNoteClick(note) }
            root.setOnLongClickListener {
                onLongClick(note)
                true
            }
            imagePin.setOnClickListener { onPinClick(note) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
                oldItem == newItem
        }
    }
}
