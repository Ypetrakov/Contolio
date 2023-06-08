package com.example.serverttest

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serverttest.adapters.ServerAdapter
import com.example.serverttest.databinding.ActivityServerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.security.MessageDigest

class ServerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServerBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshButton: Button
    private lateinit var updateImage: ImageView
    private lateinit var serverAdapter: ServerAdapter
    private var isFragmentAdded = false

    private var serversList: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        refreshButton = binding.refreshButton
        updateImage = binding.updatingServers
        updateImage.visibility = View.INVISIBLE

        recyclerView = binding.serverRecyclerView

        recyclerView.layoutManager = LinearLayoutManager(this)
        serverAdapter = ServerAdapter(serversList) { ipAddress ->
            startSharing(ipAddress)
        }
        recyclerView.adapter = serverAdapter
        binding.refreshButton.setOnClickListener {
            reloadServers()
        }

        binding.showSettings.setOnClickListener{
            val fragmentManager = supportFragmentManager
            if (isFragmentAdded) {
                val fragment = fragmentManager.findFragmentById(R.id.server_fragment)
                if (fragment != null) {
                    fragmentManager.beginTransaction().remove(fragment).commit()
                    isFragmentAdded = false
                }
            } else {
                fragmentManager.beginTransaction().add(R.id.server_fragment, SettingFragment::class.java, null).commit()
                isFragmentAdded = true
            }
        }

        binding.showDataBase.setOnClickListener{
            val fragmentManager = supportFragmentManager
            if (isFragmentAdded) {
                val fragment = fragmentManager.findFragmentById(R.id.server_fragment)
                if (fragment != null) {
                    fragmentManager.beginTransaction().remove(fragment).commit()
                    isFragmentAdded = false
                }
            } else {
                fragmentManager.beginTransaction().add(R.id.server_fragment, DataFragment::class.java, null).commit()
                isFragmentAdded = true
            }
        }

    }

    private fun reloadServers() {
        refreshButton.visibility = View.INVISIBLE
        updateImage.visibility = View.VISIBLE
        serverAdapter.updateData(emptyList())
        val deferredServers = findServersAsync(this)
        CoroutineScope(Dispatchers.Main).launch {
            val servers = deferredServers.await()
            serverAdapter.updateData(servers)
            refreshButton.visibility = View.VISIBLE
            updateImage.visibility = View.INVISIBLE
        }
    }

    private fun startSharing(ipAddress: String) {
        if (isServerReachable(ipAddress)) {
            val passwordDialog = createPasswordDialog(ipAddress)
            passwordDialog.show()
        } else {
            Toast.makeText(this, "It seems like the server is not reachable", Toast.LENGTH_SHORT)
                .show()
            reloadServers()
        }
    }

    private fun createPasswordDialog(ipAddress: String): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Password")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val password = input.text.toString()
            validatePassword(password, ipAddress)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        return builder.create()
    }

    private fun validatePassword(password: String, ipAddress: String) {
        retrieveStoredPassword(ipAddress) { storedPassword ->
            val enteredPasswordHash = encryptSHA256(password)
            val isPasswordValid = storedPassword == enteredPasswordHash
            Log.d("PAss", isPasswordValid.toString())
            Log.d("Pass", enteredPasswordHash)
            Log.d("Pass", storedPassword)
            if (isPasswordValid) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("ipAddress", ipAddress)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show()
                reloadServers()
            }
        }
    }

    private fun encryptSHA256(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedPassword = md.digest(password.toByteArray())
        return Base64.encodeToString(hashedPassword, Base64.DEFAULT).trim() // Trim any trailing newlines or whitespaces
    }

    private fun retrieveStoredPassword(ipAddress: String, onPasswordRetrieved: (String) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url("http://$ipAddress:8080/pass")
                .build()

            try {
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    withContext(Dispatchers.Main) {
                        onPasswordRetrieved(responseBody)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


}