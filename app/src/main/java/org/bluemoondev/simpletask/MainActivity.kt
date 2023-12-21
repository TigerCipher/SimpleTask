package org.bluemoondev.simpletask

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.bluemoondev.simpletask.ui.theme.SimpleTaskTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random


class TaskViewModel(application: Application) : AndroidViewModel(application) {
    fun insertTask(task: Task, application: Application) {
        viewModelScope.launch {
            TaskDatabase.getDatabase(application).taskDao().insertTask(task)
        }
    }

    fun updateTask(task: Task, application: Application) {
        viewModelScope.launch {
            TaskDatabase.getDatabase(application).taskDao().updateTask(task)
        }
    }

    fun getTasks(application: Application): LiveData<List<Task>> {
        return TaskDatabase.getDatabase(application).taskDao().getAllTasks().asLiveData()
    }

//    fun getIncompleteTaskCount(application: Application): LiveData<Int> {
//        return TaskDatabase.getDatabase(application).taskDao().getIncompleteTaskCount().asLiveData()
//    }
//
//    fun getIncompleteTasks(application: Application): LiveData<List<Task>> {
//        return TaskDatabase.getDatabase(application).taskDao().getIncompleteTasks().asLiveData()
//    }
}

class App {
    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        var PERMISSIONS = arrayOf(
            android.Manifest.permission.POST_NOTIFICATIONS,
        )

        fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        fun hasPermission(context: Context, permission: String): Boolean {
            return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }



}

class MainActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.POST_NOTIFICATIONS
        } else {
            TODO("VERSION.SDK_INT < TIRAMISU")
        }
        if(ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){
            val requestPermsLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { isGranted ->
                if(isGranted){
                    Log.d("PERMISSION", "Granted")
                } else {
                    Log.d("PERMISSION", "Denied")
                }
            }
//            requestPermissions(arrayOf(perm), 0)
            requestPermsLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if(App.hasPermissions(application, App.PERMISSIONS)){
            Log.d("PERMISSION", "Has permissions")
        } else {
            Log.d("PERMISSION", "Does not have permissions")
        }

        val calendar = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TaskNotificationReceiver::class.java)
        intent.putExtra("type", "daily")
        val uniqueId = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getBroadcast(this, uniqueId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)

//        if(alarmManager.canScheduleExactAlarms()){
//            Log.d("ALARM", "Can schedule exact alarms")
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        } else {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
//        }

        val timeRemaining = calendar.timeInMillis - System.currentTimeMillis()
        Log.d("ALARM", "Time remaining: ${timeRemaining / 1000 / 60 / 60 / 24} days, ${timeRemaining / 1000 / 60 / 60 % 24} hours, ${timeRemaining / 1000 / 60 % 60} minutes, ${timeRemaining / 1000 % 60} seconds")
//        sendBroadcast(intent)
//        setupHourlyNotifications()

        setContent {
            val isDialogOpen = remember { mutableStateOf(false) }
            SimpleTaskTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(modifier = Modifier.fillMaxSize()){
                        val tasks by taskViewModel.getTasks(application).observeAsState(emptyList())
                        val taskStates = tasks.map { remember { mutableStateOf(it) } }
                        TaskList(tasks = taskStates, onTaskDelete = {
                        }, onTaskEdit = { /*TODO*/ },
                            onTaskCompleted = {
                                taskViewModel.updateTask(taskStates[it].value, application)
                            })
                        FloatingActionButton(
                            onClick = { isDialogOpen.value = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add task")
                        }
                        AddTaskDialog(isDialogOpen){
                            task -> taskViewModel.insertTask(task.value, application)
                        }

                        Button(
                            onClick = { sendTestNotification() },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text("Send Test Notification")
                        }
                    }
                }
            }
        }
    }

    private fun sendTestNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "test_channel"
        val channel = NotificationChannel(notificationChannelId, "Test Notifications", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Test Notification")
            .setContentText("This is a test notification.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        notificationManager.notify(0, notification)
    }

    private fun setupHourlyNotifications() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TaskNotificationReceiver::class.java)
        intent.putExtra("type", "hourly")
        val uniqueId = System.currentTimeMillis().toInt() + Random.nextInt(5, 100)
        val pendingIntent = PendingIntent.getBroadcast(this, uniqueId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_HOUR,
            pendingIntent
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDateDialog(isDialogOpen: MutableState<Boolean>, onDateSelect: (Long) -> Unit) {
    if (isDialogOpen.value) {
        val dateState = rememberDatePickerState()
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false},
            title =  { Text(text = "Select Date") },
            text = {
                DatePicker(state = dateState)
                onDateSelect(dateState.selectedDateMillis ?: 0L)
            },
            confirmButton = {
                Button(onClick = {
                    isDialogOpen.value = false
                }) {
                    Text("Select")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(isDialogOpen: MutableState<Boolean>, onTaskAdd: (MutableState<Task>) -> Unit) {
    if (isDialogOpen.value) {
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false },
            title = { Text(text = "Add New Task") },
            text = {
                Column {
                    var name by remember { mutableStateOf("") }
                    var description by remember { mutableStateOf("") }
                    var deadline by remember { mutableLongStateOf(0L) }
                    var hour by remember { mutableIntStateOf(0) }
                    var minute by remember { mutableIntStateOf(0) }
                    val isDatePickerOpen = remember { mutableStateOf(false) }
                    val timeState = rememberTimePickerState()
                    val dateFormatter = remember { SimpleDateFormat("MM/dd/yyy", Locale.getDefault())}

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name*") }
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") }
                    )
                    Text(text = "Deadline")
                    TimeInput(state = timeState)
                    hour = timeState.hour
                    minute = timeState.minute

                    Row {
                        Button(
                            onClick = {
                                isDatePickerOpen.value = true
                            }
                        ) {
                            Text("Select Date*")
                        }

                        Text(
                            text = if (deadline == 0L) "No deadline selected" else dateFormatter.format(
                                Date(deadline)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                    }


                    SelectDateDialog(isDialogOpen = isDatePickerOpen, onDateSelect = { deadline = it})

                    if(name.isEmpty() || deadline == 0L){
                        Text(text = "Please fill out all required fields", color = Color.Red)
                    } else {
                        Button(
                            onClick = {
                                onTaskAdd(mutableStateOf(Task(name = name, description = description,
                                    deadline = deadline, hour = hour, minute = minute,
                                    isCompleted = false)))
                                isDialogOpen.value = false
                            }
                        ) {
                            Text("Add Task")
                        }
                    }

                }
            },
            confirmButton = { }
        )
    }
}

@Composable
fun TaskDetailsDialog(task: Task, isDialogOpen: MutableState<Boolean>){
    if (isDialogOpen.value) {
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false },
            title = { Text(text = task.name) },
            text = {
                Column {
                    Text(text = task.description)
                    val deadlineDate = Date(task.deadline)
                    val dateFormatter = remember { SimpleDateFormat("MM/dd/yyy", Locale.getDefault())}
//                    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault())}
                    Text(text = "Deadline: ${dateFormatter.format(deadlineDate)} at ${task.hour}:${task.minute}")
                }
            },
            confirmButton = { }
        )
    }
}

@Composable
fun TaskItem(taskState: MutableState<Task>, onTaskDelete: () -> Unit, onTaskEdit: () -> Unit, onTaskCompleted: () -> Unit) {
    val task = taskState.value
    val isDialogOpen = remember { mutableStateOf(false)}
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { isDialogOpen.value = true })
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val checkedState = remember { mutableStateOf(taskState.value.isCompleted) }
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = { isChecked ->
                taskState.value.isCompleted = isChecked
                checkedState.value = !checkedState.value
                onTaskCompleted()
            }
        )
        Text(
            text = task.name,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        IconButton(onClick = {
            onTaskDelete()
        }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete task")
        }
        IconButton(onClick = { onTaskEdit() }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit task")
        }

        TaskDetailsDialog(task = task, isDialogOpen = isDialogOpen)
    }
}

@Composable
fun TaskList(tasks: List<State<Task>>, onTaskDelete: (Int) -> Unit, onTaskEdit: (Int) -> Unit, onTaskCompleted: (Int) -> Unit) {
    LazyColumn {
        itemsIndexed(tasks) { index, task ->
            val mutableTask = remember { mutableStateOf(task.value) }
            TaskItem(
                taskState = mutableTask,
                onTaskDelete = {
                               onTaskDelete(index)
//                    tasks.value = tasks.value.filterNot { it == task }
                },
                onTaskEdit = { onTaskEdit(index) },
                onTaskCompleted = { onTaskCompleted(index) }
            )
        }
    }
}