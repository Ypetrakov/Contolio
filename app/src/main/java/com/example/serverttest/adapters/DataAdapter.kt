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
import com.example.serverttest.database.entity.Connections

class DataAdapter(
    private var connections: List<Connections>
) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewYourIp: TextView = itemView.findViewById(R.id.textViewYourIp)
        val textViewConnectionIp: TextView = itemView.findViewById(R.id.textViewConnectionIp)
        val textViewTimeStart: TextView = itemView.findViewById(R.id.textViewTimeStart)
        val textViewTimeEnd: TextView = itemView.findViewById(R.id.textViewTimeEnd)
        val textViewSessionTime: TextView = itemView.findViewById(R.id.textViewSessionTime)
        val textViewLocation: TextView = itemView.findViewById(R.id.textViewLocation)
        val textViewFiles: TextView = itemView.findViewById(R.id.textViewFiles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val view = LayoutInflater.from(parent.context).
            inflate(R.layout.item_connection, parent, false)

        return DataViewHolder(view)
    }

    override fun getItemCount(): Int {
        return connections.size
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val connection = connections[position]

        holder.textViewYourIp.text = "Your IP: ${connection.yourIp}"
        holder.textViewConnectionIp.text = "Connection IP: ${connection.connectionIp}"
        holder.textViewTimeStart.text = "Start Time: ${connection.timeStart}"
        holder.textViewTimeEnd.text = "End Time: ${connection.timeEnd}"
        holder.textViewSessionTime.text = "Session Time: ${connection.sessionTime}"
        holder.textViewLocation.text = "Location: ${connection.location}"
        holder.textViewFiles.text = "Files: ${connection.files}"
    }



}