package com.example.keyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.keyapp.model.Key
import com.example.keyapp.databinding.ItemKeyBinding

class KeysAdapter(
    private val onItemClick: (ByteArray) -> Unit,
    private val onDeleteClick: (Key) -> Unit
) : RecyclerView.Adapter<KeysAdapter.KeyViewHolder>() {

    private var keys = emptyList<Key>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyViewHolder {
        val binding = ItemKeyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KeyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeyViewHolder, position: Int) {
        val key = keys[position]
        holder.bind(key)
    }

    override fun getItemCount() = keys.size

    fun setKeys(keys: List<Key>) {
        this.keys = keys
        notifyDataSetChanged()
    }

    inner class KeyViewHolder(private val binding: ItemKeyBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(key: Key) {
            binding.keyTextView.text = key.keyData.toMacAddress()
            binding.root.setOnClickListener {
                onItemClick(key.keyData)
            }
            binding.deleteButton.setOnClickListener {
                onDeleteClick(key)
            }
        }
    }
}

