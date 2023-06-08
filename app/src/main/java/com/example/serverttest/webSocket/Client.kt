package com.example.serverttest.webSocket

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface

const val NORMAL_CLOSURE_STATUS = 1000

object WebSocketManager {
    private val client = OkHttpClient()
    private var screenWebSocket: WebSocket? = null
    private var mouseWebSocket: WebSocket? = null
    private var commandWebSocket: WebSocket? = null
    private var keyboardWebSocket: WebSocket? = null
    private lateinit var ip: String

    fun connectSockets(context: Context, ip: String) {
        this.ip = ip
        connectToCommand(context, ip)
        connectToMouse(context, ip)
        connectToScreen(context, ip)
        connectToKeyboard(context, ip)
        sendCommand("setIpAndMac ${getIPAddress()}!${getMacAddress()}")
    }

    fun closeSockets() {
        sendCommand("setIpAndMac  ! ")
        screenWebSocket?.close(NORMAL_CLOSURE_STATUS, "Closing screen WebSocket")
        mouseWebSocket?.close(NORMAL_CLOSURE_STATUS, "Closing mouse WebSocket")
        commandWebSocket?.close(NORMAL_CLOSURE_STATUS, "Closing command WebSocket")
        keyboardWebSocket?.close(NORMAL_CLOSURE_STATUS, "Closing command WebSocket")

    }

    private fun connectToScreen(context: Context, ip: String) {
        val request = Request.Builder()
            .url("ws://$ip:8080/screen")
            .build()
        val listener = ImageWebSocketListener(context)
        screenWebSocket = client.newWebSocket(request, listener)
    }

    private fun connectToMouse(context: Context, ip: String) {
        val request = Request.Builder()
            .url("ws://$ip:8080/mouse")
            .build()
        val listener = EmptyWebSocketListener()
        mouseWebSocket = client.newWebSocket(request, listener)
    }

    private fun connectToCommand(context: Context, ip: String) {
        val request = Request.Builder()
            .url("ws://$ip:8080/command")
            .build()
        val listener = EmptyWebSocketListener()
        commandWebSocket = client.newWebSocket(request, listener)
    }

    private fun connectToKeyboard(context: Context, ip: String) {
        val request = Request.Builder()
            .url("ws://$ip:8080/keyboard")
            .build()
        val listener = EmptyWebSocketListener()
        keyboardWebSocket = client.newWebSocket(request, listener)
    }

    fun sendJoystickData(x: Float, y: Float, type: String) {
        val json = JSONObject().apply {
            put("x", x)
            put("y", y)
            put("type", type)
        }
        mouseWebSocket?.send(json.toString())
    }

    fun sendCommand(command: String) {
        commandWebSocket?.send(command)
    }

    fun sendKeyboardData(text: String) {
        keyboardWebSocket?.send(text)
    }

    fun sendFile(file: File) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("http://$ip:8080/upload")
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // Handle the response here
                // Note: This code runs on a background thread, not the main thread
                response.use {
                    if (response.isSuccessful) {
                        // Process the successful response
                    } else {
                        // Handle unsuccessful response
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle the failure here
                // Note: This code runs on a background thread, not the main thread
                e.printStackTrace()
            }
        })
    }
}
fun getIPAddress(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is InetAddress && address.hostAddress.indexOf(
                        ':'
                    ) < 0
                ) {
                    return address.hostAddress
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun getMacAddress(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val mac = networkInterface.hardwareAddress
            if (mac != null && mac.isNotEmpty()) {
                val stringBuilder = StringBuilder()
                for (byte in mac) {
                    stringBuilder.append(String.format("%02X:", byte))
                }
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.deleteCharAt(stringBuilder.length - 1)
                }
                return stringBuilder.toString()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

