package com.ahmedmadhoun.wallpaperapp.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ahmedmadhoun.wallpaperapp.R
import com.ahmedmadhoun.wallpaperapp.ui.MainActivity

class NotificationManagerUtil(var context: Context?) {

    private val NOTIFICATION_ID = 115

    fun showNotification(id: Int, title: String?, message: String?, intent: Intent?) {
        val pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val WALLPAPER_CHANNEL_ID = "WALLPAPER_CHANNEL_ID"
        val mBuilder = NotificationCompat.Builder(context!!, WALLPAPER_CHANNEL_ID)
        val notification: Notification
        notification = mBuilder
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(false)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .build()


        val notificationManager = (context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(WALLPAPER_CHANNEL_ID, "Wallpapers Channel", NotificationManager.IMPORTANCE_HIGH).apply {
                setShowBadge(true)
                description = message
                enableLights(true)
                enableVibration(true)
                notificationManager.createNotificationChannel(this)
            }
        }

        notificationManager.notify(id, notification)
    }

}