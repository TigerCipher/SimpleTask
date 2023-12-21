package org.bluemoondev.simpletask

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch

class TaskNotificationReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        if(!App.hasPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)){
            return
        }
        GlobalScope.launch(Dispatchers.IO) {
            if(intent.getStringExtra("type") == "daily"){
                Log.d("TaskNotificationReceiver", "Received daily notification broadcast")
            }else if(intent.getStringExtra("type") == "hourly"){
                Log.d("TaskNotificationReceiver", "Received hourly notification broadcast")
            }else{
                Log.d("TaskNotificationReceiver", "Received unknown notification broadcast")
            }
            val taskDao = TaskDatabase.getDatabase(context).taskDao()
            val incompleteTaskCount = taskDao.getIncompleteTaskCount()
            val currentTime = System.currentTimeMillis()
            if(incompleteTaskCount > 0) {
                Log.d("TaskNotificationReceiver", "You have $incompleteTaskCount tasks to complete")
                sendNotification(context, "You have $incompleteTaskCount tasks to complete")
            }
//            taskDao.getIncompleteTasks().forEach { task ->
//                val daysUntilDeadline = (task.deadline - currentTime) / (1000 * 60 * 60 * 24)
//                if(daysUntilDeadline in 0..3){
//                    sendNotification(context, task, "Task ${task.name} is due in $daysUntilDeadline days")
//                }
//            }
        }

    }

    private fun sendNotification(context: Context, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifChannelId = "task_channel"
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notifChannelId, "Tasks", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notif = NotificationCompat.Builder(context, notifChannelId)
            .setContentTitle("Tasks")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(0, notif)
    }

    private fun sendNotification(context: Context, task: Task, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifChannelId = "task_channel"
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notifChannelId, "Tasks", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notif = NotificationCompat.Builder(context, notifChannelId)
            .setContentTitle("Task due soon")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(task.id, notif)
    }
}