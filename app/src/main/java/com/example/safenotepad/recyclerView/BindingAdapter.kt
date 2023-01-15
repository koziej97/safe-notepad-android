package com.example.safenotepad.recyclerView

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safenotepad.data.database.Note

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Note>?) {
    val adapter = recyclerView.adapter as NotesListAdapter
    adapter.submitList(data)
}