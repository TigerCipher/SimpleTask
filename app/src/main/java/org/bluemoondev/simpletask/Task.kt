package org.bluemoondev.simpletask

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(@PrimaryKey(autoGenerate = true) val id: Int = 0,
                val name: String,
                val description: String,
                val deadline: Long, // Date in milliseconds
                val hour: Int,
                val minute: Int,
                var isCompleted: Boolean)
