package com.example.serverttest.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.serverttest.R

class ServerAdapter(
    private var servers: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServerAdapter.ServerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerAdapter.ServerViewHolder, position: Int) {
        val ipAddress = servers[position]
        holder.bind(ipAddress)
    }

    override fun getItemCount(): Int {
        return servers.size
    }

    fun updateData(newServers: List<String>) {
        val diffResult = DiffUtil.calculateDiff(ServerDiffCallback(servers, newServers))
        servers = newServers
        diffResult.dispatchUpdatesTo(this)
    }

    private class ServerDiffCallback(
        private val oldList: List<String>,
        private val newList: List<String>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    inner class ServerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ipTextView: TextView = itemView.findViewById(R.id.itemServerIpTextView)
        fun bind(ipAddress: String) {
            ipTextView.text = ipAddress
            itemView.setOnClickListener {
                onItemClick(ipAddress)
                Log.d("IP", ipAddress)
            }
        }
    }
}