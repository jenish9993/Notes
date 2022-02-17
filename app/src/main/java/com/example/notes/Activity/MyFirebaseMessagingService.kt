package com.example.notes.Activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.notes.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    val channelId = "notification_channel"
    val channelName = "com.example.notes.Activity"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.notification != null){
            genetareNotificaton(remoteMessage.notification!!.title!!,remoteMessage.notification!!.body!!)
        }
    }

    fun genetareNotificaton(title:String,message:String){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)

        var builder:NotificationCompat.Builder = NotificationCompat.Builder(applicationContext,channelId)
            .setSmallIcon(R.drawable.ic_baseline_notes_24)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setContent(getRemoteView(title,message))

        val notificaionManager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_HIGH)
            notificaionManager.createNotificationChannel(notificationChannel)
        }
        notificaionManager.notify(0,builder.build())
    }

    private fun getRemoteView(title: String, message: String): RemoteViews? {

        val remoteView = RemoteViews("com.example.notes.Activity",R.layout.notification_layout)

        remoteView.setTextViewText(R.id.txtTitleNotificaton,title)
        remoteView.setTextViewText(R.id.txtMesageNotificaton,message)
        remoteView.setImageViewResource(R.id.app_logo,R.drawable.ic_baseline_notes_24)

        return remoteView
    }
}