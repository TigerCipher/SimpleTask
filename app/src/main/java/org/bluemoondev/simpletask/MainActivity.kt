package org.bluemoondev.simpletask

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
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
import java.util.Date
import java.util.Locale
import kotlin.random.Random


class TaskViewModel(application: Application) : AndroidViewModel(application) {
//    private val db = TaskDatabase.getDatabase(application)

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
        try{
            Log.d("TaskViewModel", "Getting tasks as live data")
            return TaskDatabase.getDatabase(application).taskDao().getAllTasks().asLiveData()
        } catch (e: Exception) {
            Log.d("TaskViewModel", "Failed to get tasks as live data")
            Log.e("TaskViewModel", "Error: ${e.message}")
        }
        Log.d("TaskViewModel", "Returning list that will probably crash")
//        return db.taskDao().getAllTasks().asLiveData()
        return MutableLiveData<List<Task>>()
    }
}

class MainActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        setContent {
            val isDialogOpen = remember { mutableStateOf(false) }
            SimpleTaskTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(modifier = Modifier.fillMaxSize()){
                        Log.d("MainActivity", "Box")
                        val tasks by taskViewModel.getTasks(application).observeAsState(emptyList())
                        Log.d("MainActivity", "Post task creation")
                        val taskStates = tasks.map { remember { mutableStateOf(it) } }
                        TaskList(tasks = taskStates, onTaskDelete = {
                           Log.d("MainActivity", "Task deleted")
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
                    }
                }
            }
        }
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
                    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyy", Locale.getDefault())}

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
            },
            confirmButton = { }
        )
    }
}

@Composable
fun TaskItem(taskState: MutableState<Task>, onTaskDelete: () -> Unit, onTaskEdit: () -> Unit, onTaskCompleted: () -> Unit) {
    val task = taskState.value
    Row(
        modifier = Modifier
            .fillMaxWidth()
//            .draggable(
//                state = rememberDraggableState { delta ->
//                    if (delta > 0) onTaskEdit() // If dragged to the right
//                    else if (delta < 0) onTaskDelete() // If dragged to the left
//                },
//                orientation = Orientation.Horizontal,
//                startDragImmediately = true
//            )
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