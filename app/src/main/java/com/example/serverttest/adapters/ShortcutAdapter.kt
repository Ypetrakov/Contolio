package com.example.serverttest.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.serverttest.R
import com.example.serverttest.data.SharedPreferencesUtil

class ShortcutAdapter(
    private var shortcuts: MutableMap<String, String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ShortcutAdapter.ShortcutViewHolder>() {

    inner class ShortcutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shortcutName: TextView = itemView.findViewById(R.id.shortcut_name)
        private val shortcutDescription: TextView = itemView.findViewById(R.id.shortcut_description)

        fun bind(name: String, description: String) {
            shortcutName.text = name
            shortcutDescription.text = description
            itemView.setOnClickListener {
                onItemClick(name)
            }
        }
    }

    private class ServerDiffCallback(
        private var oldMap: Map<String, String>,
        private var newMap: Map<String, String>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldMap.size
        }

        override fun getNewListSize(): Int {
            return newMap.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldKeys = oldMap.keys.toList()
            val newKeys = newMap.keys.toList()
            return oldKeys[oldItemPosition] == newKeys[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldValues = oldMap.values.toList()
            val newValues = newMap.values.toList()
            return oldValues[oldItemPosition] == newValues[newItemPosition]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_shortcut, parent, false)
        return ShortcutViewHolder(view)
    }

    override fun getItemCount(): Int {
        return shortcuts.size
    }

    override fun onBindViewHolder(holder: ShortcutViewHolder, position: Int) {
        val name = shortcuts.keys.toList()[position]
        val description = shortcuts[name] ?: ""
        holder.bind(name, description)
    }
    fun updateMap(newMap: MutableMap<String, String>) {
        val diffCallback = ServerDiffCallback(shortcuts, newMap)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        shortcuts = newMap
        diffResult.dispatchUpdatesTo(this)
    }


}