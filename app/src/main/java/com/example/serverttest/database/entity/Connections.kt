package com.example.serverttest.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "connections")
data class Connections(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "your_ip") val yourIp: String,
    @ColumnInfo(name = "connection_ip") val connectionIp: String,
    @ColumnInfo(name = "start_time") val timeStart: String,
    @ColumnInfo(name = "end_time") val timeEnd: String,
    @ColumnInfo(name = "session_time") val sessionTime:String,
    @ColumnInfo(name = "location") val location: String,
    @ColumnInfo(name = "files") val files: String
)

