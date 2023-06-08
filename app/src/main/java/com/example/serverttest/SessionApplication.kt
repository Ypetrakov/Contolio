package com.example.serverttest

import android.app.Application
import com.example.serverttest.database.AppDatabase

class SessionApplication: Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}