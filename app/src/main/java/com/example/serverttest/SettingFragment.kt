package com.example.serverttest

import android.database.Observable
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serverttest.adapters.ShortcutAdapter
import com.example.serverttest.data.SharedPreferencesUtil
import com.example.serverttest.databinding.ShortcutsViewBinding
import com.example.serverttest.webSocket.WebSocketManager

class SettingFragment : Fragment() {
    private lateinit var binding: ShortcutsViewBinding
    private lateinit var storedShortcuts: MutableMap<String, String>
    private lateinit var shortcutAdapter: ShortcutAdapter
    private lateinit var buttonCreateShortcut: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ShortcutsViewBinding.inflate(inflater, container, false)
        val recyclerViewShortcuts = binding.recyclerViewShortcuts
        buttonCreateShortcut = binding.buttonCreateShortcut
        val textView = binding.textViewShortcuts
        textView.text = textView.text.toString() + "   (tap to delete)"
        storedShortcuts =
            SharedPreferencesUtil.getMap(requireContext()) as MutableMap<String, String>
        buttonCreateShortcut.visibility = Button.VISIBLE
        buttonCreateShortcut.setOnClickListener{
            showCreateShortcutPopup()
        }
        shortcutAdapter =
            ShortcutAdapter(storedShortcuts) { name: String -> showRemoveConfirmationDialog(name) }
        recyclerViewShortcuts.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewShortcuts.adapter = shortcutAdapter

        return binding.root
    }

    private fun showCreateShortcutPopup() {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_create_shortcut, null)

        val editTextName = popupView.findViewById<EditText>(R.id.editTextName)
        val editTextDescription = popupView.findViewById<EditText>(R.id.editTextDescription)
        val buttonCreate = popupView.findViewById<Button>(R.id.buttonCreate)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set a dismiss listener to handle the popup window dismissal
        popupWindow.setOnDismissListener {
            // Clear the input fields when the popup window is dismissed
            editTextName.text.clear()
            editTextDescription.text.clear()
        }

        // Set a click listener for the create button
        buttonCreate.setOnClickListener {
            val name = editTextName.text.toString()
            val description = editTextDescription.text.toString()

            // Perform validation or other operations before creating the shortcut
            if (name.isNotEmpty() && description.isNotEmpty() && shortcutValidator(name)) {
                // Add the new shortcut to the list
                storedShortcuts[name] = description
                SharedPreferencesUtil.saveMap(requireContext(), storedShortcuts)
                shortcutAdapter.updateMap(storedShortcuts)
                shortcutAdapter.notifyItemInserted(storedShortcuts.keys.indexOf(name))

                // Dismiss the popup window
                popupWindow.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter valid name and description",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

    }
    private fun shortcutValidator(shortcut: String): Boolean{
        for(part in shortcut.split("+")){
            if (part.length != 1 && part !in listOf("win", "ctrl", "alt")){
                return false
            }
        }
        return true
    }
    private fun showRemoveConfirmationDialog(name: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Confirmation")
        alertDialogBuilder.setMessage("Are you sure you want to remove this item? (${storedShortcuts[name]})")
        alertDialogBuilder.setPositiveButton("Yes") { dialog, _ ->
            storedShortcuts.remove(name)
            SharedPreferencesUtil.saveMap(requireContext(), storedShortcuts)
            shortcutAdapter.updateMap(storedShortcuts)
            shortcutAdapter.notifyItemRemoved(storedShortcuts.keys.toList().indexOf(name))
            Log.d("Shprt", storedShortcuts.toString())
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


}