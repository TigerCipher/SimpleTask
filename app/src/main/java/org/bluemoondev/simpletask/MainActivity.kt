package org.bluemoondev.simpletask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import org.bluemoondev.simpletask.ui.theme.SimpleTaskTheme
import kotlin.random.Random

data class Task(val id: Int, val name: String, val description: String, val deadline: String, var isCompleted: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDialogOpen = remember { mutableStateOf(false) }
            val tasks = remember { mutableStateOf(listOf<Task>()) }
            SimpleTaskTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(modifier = Modifier.fillMaxSize()){
                        TaskList(tasks = tasks.value, onTaskComplete = { /*TODO*/ }, onTaskDelete = { /*TODO*/ }, onTaskEdit = { /*TODO*/ })
                        FloatingActionButton(
                            onClick = { isDialogOpen.value = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add task")
                        }
                        AddTaskDialog(isDialogOpen){
                            task -> tasks.value = tasks.value + task
                        }
                    }
                }
            }
        }
    }
}

private fun onAddTask(){
    // TODO
}


@Composable
fun AddTaskDialog(isDialogOpen: MutableState<Boolean>, onTaskAdd: (Task) -> Unit) {
    if (isDialogOpen.value) {
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false },
            title = { Text(text = "Add New Task") },
            text = {
                Column {
                    var name by remember { mutableStateOf("") }
                    var description by remember { mutableStateOf("") }
                    var deadline by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Task Name") }
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Task Description") }
                    )
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Task Deadline") }
                    )

                    Button(
                        onClick = {
                            onTaskAdd(Task(id = Random.nextInt(), name = name, description = description, deadline = deadline, isCompleted = false))
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
fun TaskItem(task: Task, onTaskComplete: (Task) -> Unit, onTaskDelete: () -> Unit, onTaskEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .draggable(
                state = rememberDraggableState { delta ->
                    if (delta > 0) onTaskEdit() // If dragged to the right
                    else if (delta < 0) onTaskDelete() // If dragged to the left
                },
                orientation = Orientation.Horizontal,
                startDragImmediately = true
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { isChecked ->
                task.isCompleted = isChecked
                onTaskComplete(task)
            }
        )
        Text(
            text = task.name,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        IconButton(onClick = { onTaskDelete() }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete task")
        }
        IconButton(onClick = { onTaskEdit() }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit task")
        }
    }
}

@Composable
fun TaskList(tasks: List<Task>, onTaskComplete: (Task) -> Unit, onTaskDelete: (Int) -> Unit, onTaskEdit: (Int) -> Unit) {
    LazyColumn {
        items(tasks) { task ->
            TaskItem(
                task = task,
                onTaskComplete = onTaskComplete,
                onTaskDelete = { onTaskDelete(task.id) },
                onTaskEdit = { onTaskEdit(task.id) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskListPreview() {
    val tasks = listOf(
        Task(id = 1, name = "Task 1", description = "Description 1", deadline = "Deadline 1", isCompleted = false),
        Task(id = 1, name = "Task 2", description = "Description 2", deadline = "Deadline 2", isCompleted = true),
        Task(id = 1, name = "Task 3", description = "Description 3", deadline = "Deadline 3", isCompleted = false),
    )

    TaskList(
        tasks = tasks,
        onTaskComplete = { /*TODO*/ },
        onTaskDelete = { /*TODO*/ },
        onTaskEdit = { /*TODO*/ }
    )
}