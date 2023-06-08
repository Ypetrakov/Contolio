package com.example.serverttest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serverttest.adapters.ShortcutAdapter
import com.example.serverttest.data.SharedPreferencesUtil
import com.example.serverttest.database.AppDatabase
import com.example.serverttest.database.entity.Connections
import com.example.serverttest.databinding.ActivityMainBinding
import com.example.serverttest.databinding.PopupTextReceiverBinding
import com.example.serverttest.helpers.LocationHelper
import com.example.serverttest.webSocket.WebSocketManager
import com.example.serverttest.webSocket.getIPAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FILE_PICKER_REQUEST_CODE = 1
private var keyboardRequest = 1


class MainActivity : AppCompatActivity() {
    private var yourIp = ""
    private var connectionIp = ""
    private lateinit var timeStart: Date
    private lateinit var timeEnd: Date
    private var sessionTime = ""
    private var location = ""
    private var files = ""

    private val targetWidth = 1920
    private val targetHeight = 1080
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivityMainBinding
    private var savedText: String = ""
    private lateinit var storedShortcuts: Map<String, String>
    private lateinit var locationHelper: LocationHelper
    private lateinit var database: AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        storedShortcuts = SharedPreferencesUtil.getMap(this)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                yourIp = getIPAddress()!!
            }
        }
        database = AppDatabase.getDatabase(applicationContext)

        setupButtons()
        setupImage()
        setupVolume()
        setupFileSender()
        locationHelper = LocationHelper(this)
        locationHelper.getCurrentLocationString { locationString ->
            location = locationString
        }
        timeStart = Date()
        connectionIp = intent.getStringExtra("ipAddress").toString()

        setContentView(binding.root)
        WebSocketManager.connectSockets(this, intent.getStringExtra("ipAddress")!!)
    }
    override fun onDestroy() {
        timeEnd = Date()
        sessionTime = (timeEnd.time - timeStart.time).toString()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        try {
            val connection = getNewItemEntry(
                yourIp,
                connectionIp,
                dateFormat.format(timeStart.time),
                dateFormat.format(timeEnd.time),
                sessionTime,
                location,
                files
            )
            Log.d("Connection", connection.toString())

            runBlocking {
                try {
                    withContext(Dispatchers.IO) {
                        database.connectionDataDoa().insert(connection)
                    }
                    Log.d("data", "send")
                } catch (e: Exception) {
                    Log.e("InsertError", e.message ?: "Unknown error occurred while inserting data.")
                }
            }
        } catch (e: ParseException) {
            // Handle any exception that occurs while formatting the date
            e.printStackTrace()
        }

        WebSocketManager.closeSockets()
        super.onDestroy()
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyboardRequest == 1) {
            if (keyCode == 67) {
                // Send "clear" to the WebSocket
                WebSocketManager.sendKeyboardData("clear")
                return true
            }
            if (keyCode == 66) {
                // Send "clear" to the WebSocket
                WebSocketManager.sendKeyboardData("enter")
                return true
            }
            val unicodeChar = event.unicodeChar.toChar().toString()
            // Send the character to the WebSocket
            WebSocketManager.sendKeyboardData(unicodeChar)
        }
        return super.onKeyDown(keyCode, event)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val filePath = getFilePathFromUri(uri)
                val file = File(filePath!!)
                files += "${file.name} "
                WebSocketManager.sendFile(file)
            }
        }
    }

    private fun getNewItemEntry(
        yourIp: String,
        connectionIp: String,
        timeStart: String,
        timeEnd: String,
        sessionTime: String,
        location: String,
        files: String
    ): Connections {
        return Connections(
            yourIp = yourIp,
            connectionIp = connectionIp,
            timeStart = timeStart,
            timeEnd = timeEnd,
            sessionTime = sessionTime,
            location = location,
            files = files
        )
    }


    private fun getFilePathFromUri(uri: Uri): String? {
        val documentFile = DocumentFile.fromSingleUri(this, uri)
        val displayName = documentFile?.name ?: return null

        var filePath: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            filePath = cacheDir.absolutePath + File.separator + displayName
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(filePath?.let { File(it) }).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } else {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                filePath = cursor.getString(columnIndex)
            }
        }

        return filePath
    }


    private fun setupButtons() {
        binding.mouseLeftClick.setOnClickListener {
            WebSocketManager.sendCommand("leftClickPress")
            Log.d("Command", "leftClickPress")
        }
        binding.mouseRightClick.setOnClickListener {
            WebSocketManager.sendCommand("rightClickPress")
            Log.d("Command", "rightClickPress")
        }

        binding.keyboard.setOnClickListener {
            openKeyboard()
        }
        binding.pasteText.setOnClickListener {
            openKeyboardToPaste()
        }
        binding.shortcuts.setOnClickListener {
            setupShortcuts()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupImage() {
        val image = binding.imageView

        image.setOnTouchListener { view, event ->

            val imageWidth = view.width
            val imageHeight = view.height

            val touchX = event.x
            val touchY = event.y

            val scaleX = imageWidth.toFloat() / imageWidth
            val scaleY = imageHeight.toFloat() / imageHeight

            val targetX = (touchX * scaleX * targetWidth / imageWidth)
            val targetY = (touchY * scaleY * targetHeight / imageHeight)
            WebSocketManager.sendJoystickData(targetX, targetY, "touch")
            true
        }
    }

    private fun openKeyboard() {
        keyboardRequest = 1
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun openKeyboardToPaste() {
        keyboardRequest = 2
        val popupViewBinding = PopupTextReceiverBinding.inflate(layoutInflater)
        val editText = popupViewBinding.editTextPaste
        val sendButton = popupViewBinding.buttonSend
        val closeButton = popupViewBinding.buttonClose
        val hideButton = popupViewBinding.buttonHide
        val popUpWindow = PopupWindow(
            popupViewBinding.root,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        popUpWindow.isFocusable = true
        editText.setText(savedText)

        sendButton.setOnClickListener {
            val textToPaste = editText.text.toString()
            if (textToPaste != "") {
                WebSocketManager.sendKeyboardData("paste_text $textToPaste")
                editText.text.clear()
                popUpWindow.dismiss()
            }
        }
        closeButton.setOnClickListener {
            editText.text.clear()
            popUpWindow.dismiss()
        }
        hideButton.setOnClickListener {
            popUpWindow.dismiss()
        }
        popUpWindow.setOnDismissListener {
            savedText = editText.text.toString()
        }
        popUpWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)


    }

    private fun setupVolume() {
        binding.volumeDown.setOnClickListener {
            WebSocketManager.sendCommand("decreaseVolume")
        }
        binding.volumeUp.setOnClickListener {
            WebSocketManager.sendCommand("increaseVolume")
        }
    }

    private fun setupFileSender() {
        binding.fileSender.setOnClickListener { launchFilePicker() }

    }

    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    private fun setupShortcuts() {
        val shortcutsView = layoutInflater.inflate(R.layout.shortcuts_view, binding.root, false)
        val recyclerViewShortcuts =
            shortcutsView.findViewById<RecyclerView>(R.id.recyclerViewShortcuts)
        val buttonCreateShortcut = shortcutsView.findViewById<Button>(R.id.buttonCreateShortcut)
        val shortcutAdapter =
            ShortcutAdapter(storedShortcuts as MutableMap<String, String>) { name: String ->
                WebSocketManager.sendKeyboardData("shortcut $name")
            }
        recyclerViewShortcuts.layoutManager = LinearLayoutManager(this)
        recyclerViewShortcuts.adapter = shortcutAdapter

        val shortcutsPopupWindow = PopupWindow(
            shortcutsView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        shortcutsPopupWindow.isFocusable = true

        shortcutsPopupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

}