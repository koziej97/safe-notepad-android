package com.example.safenotepad.recyclerView

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safenotepad.R
import com.example.safenotepad.data.database.Note
import com.example.safenotepad.databinding.ItemNoteBinding

class NotesListAdapter internal constructor(private val mListener: NotesItemClickListener):
    ListAdapter<Note, RecyclerView.ViewHolder>(DiffCallback) {

    class NoteViewHolder(
        private var binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note, listener: NotesItemClickListener) {
            binding.note = note
            binding.clickListener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): NoteViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding: ItemNoteBinding = DataBindingUtil.inflate(
                    layoutInflater, R.layout.item_note,
                    parent, false
                )
                return NoteViewHolder(binding)

            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as NoteViewHolder
        val repository = getItem(position)
        holder.bind(repository, mListener)
    }
}

interface NotesItemClickListener {
    fun chosenNote(note : Note) {
        Log.d("Note", "Chosen note: " + note.id)}
}
