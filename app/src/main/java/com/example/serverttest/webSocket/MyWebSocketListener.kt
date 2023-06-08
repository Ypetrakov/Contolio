package com.example.serverttest.webSocket

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.example.serverttest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

    class ImageWebSocketListener(private val context: Context):WebSocketListener(){
    private val imageView = (context as Activity).findViewById<ImageView>(R.id.imageView)

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        CoroutineScope(Dispatchers.Default).launch {
            val bitmap = BitmapFactory.decodeByteArray(bytes.toByteArray(), 0, bytes.size)
            withContext(Dispatchers.Main) {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        (context as Activity).finish()
        Log.d("WebSocket", "Connection closing")
    }
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.e("ERROR", t.message!!)
    }
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("ERROR", "connected")
        webSocket.send("start sharing")
    }
}

class EmptyWebSocketListener : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        // Called when the WebSocket connection is opened
        Log.d("WebSocket", "Connection opened")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // Called when a text message is received
        Log.d("WebSocket", "Received message: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // Called when a binary message is received
        Log.d("WebSocket", "Received binary message")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        // Called when the WebSocket connection is closing
        Log.d("WebSocket", "Connection closing")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        // Called when the WebSocket connection is closed
        Log.d("WebSocket", "Connection closed")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // Called when there is a network error
        Log.e("WebSocket", "Connection failed: ${t.message}")
    }
}
