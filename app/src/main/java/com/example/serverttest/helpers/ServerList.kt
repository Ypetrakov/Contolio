package com.example.serverttest

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.Collections


fun getIpAddress(context: Context): String {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return if (wifiManager.isWifiEnabled && wifiManager.connectionInfo != null) {
        val ipAddress = wifiManager.connectionInfo.ipAddress

        // Use the IP address as needed
        // For example, display it in a TextView
        String.format(
            "%d.%d.%d.%d ",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
    } else {
        "// Wi-Fi is not enabled or not connected"
    }
}

fun findServersAsync(context: Context): Deferred<List<String>> {
    val localIpRange = getIpAddress(context).substringBeforeLast(".")
    val timeoutMillis = 1000 // Set a timeout value for socket connection attempts

    val availableServers = Collections.synchronizedList(mutableListOf<String>()) // Shared list to store available servers

    return CoroutineScope(Dispatchers.IO).async {
        val jobs = mutableListOf<Job>()

        for (i in 1..255) {
            val ipAddress = "$localIpRange.$i"
            val job = launch {
                try {
                    val socket = Socket()
                    withContext(Dispatchers.IO) {
                        socket.connect(InetSocketAddress(ipAddress, 8080), timeoutMillis)
                    }
                    Log.d("ServerFound", ipAddress)
                    withContext(Dispatchers.IO) {
                        socket.close()
                    }
                    if (checkEndpoint(ipAddress)) {
                        availableServers.add(ipAddress) // Add discovered server to the shared list
                    }

                } catch (_: IOException) {

                }
            }

            jobs.add(job)
        }

        jobs.joinAll()
        availableServers // Return the list of available servers
    }
}

suspend fun checkEndpoint(ipAddress: String): Boolean {
    val url = URL("http://$ipAddress:8080/") // Assuming the server is running on port 8080
    val connection = withContext(Dispatchers.IO) {
        url.openConnection()
    } as HttpURLConnection

    return try {
        connection.connectTimeout = 1000 // Set a timeout value for the HTTP connection
        connection.requestMethod = "GET"

        val responseCode = connection.responseCode
        val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = withContext(Dispatchers.IO) {
            reader.readLine()
        }

        response == "Controlio" // Check if the response matches the expected value

    } catch (e: IOException) {
        false // Handle connection or IO exception
    } finally {
        connection.disconnect()
    }
}

fun isServerReachable(ipAddress: String): Boolean {
    var isReachable = false
    val thread = Thread {
        isReachable = try {
            val socket = Socket()
            val socketAddress = InetSocketAddress(ipAddress, 8080)
            socket.connect(socketAddress, 1000)
            socket.close()
            true
        } catch (e: IOException) {
            false
        }
    }
    thread.start()
    thread.join()
    return isReachable
}

