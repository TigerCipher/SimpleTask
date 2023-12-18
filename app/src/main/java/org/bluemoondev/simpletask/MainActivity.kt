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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

data class Task(val id: Int, val name: String, var isCompleted: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleTaskTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    AddItemButton(onClick = { onAddTask() })
                }
            }
        }
    }
}

private fun onAddTask(){
    // TODO
}

@Composable
fun AddItemButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box (modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add task")
        }
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
        Task(id = 1, name = "Task 1", isCompleted = false),
        Task(id = 2, name = "Task 2", isCompleted = true),
        Task(id = 3, name = "Task 3", isCompleted = false)
    )

    TaskList(
        tasks = tasks,
        onTaskComplete = { /*TODO*/ },
        onTaskDelete = { /*TODO*/ },
        onTaskEdit = { /*TODO*/ }
    )
}