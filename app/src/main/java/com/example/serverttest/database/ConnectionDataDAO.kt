package com.example.serverttest.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.serverttest.database.entity.Connections

@Dao
interface ConnectionDataDAO{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(connectionData: Connections)

    @Query("SELECT * FROM connections")
    fun getAllConnectionData(): List<Connections>
}


