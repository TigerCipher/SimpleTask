package org.bluemoondev.simpletask

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(@PrimaryKey(autoGenerate = true) val id: Int = 0, val name: String, val description: String, val deadline: String, var isCompleted: Boolean)