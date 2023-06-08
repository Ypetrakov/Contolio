package com.example.serverttest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serverttest.adapters.DataAdapter
import com.example.serverttest.database.AppDatabase
import com.example.serverttest.database.entity.Connections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DataFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_data, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewConnections)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadConnectionsData()
        return view
    }

    private fun loadConnectionsData() {
        // Run the database query on a background thread
        lifecycleScope.launch(Dispatchers.IO) {
            val dataList: List<Connections> = (activity?.application as SessionApplication).database
                .connectionDataDoa()
                .getAllConnectionData()

            withContext(Dispatchers.Main) {
                recyclerView.adapter = DataAdapter(dataList)
            }
        }
    }


}