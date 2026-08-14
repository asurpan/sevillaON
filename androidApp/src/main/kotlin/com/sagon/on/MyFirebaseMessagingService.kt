package com.sagon.on

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * 🔔 FIREBASE MESSAGING SERVICE: GUARDIÁN DE NOTIFICACIONES PUSH
 * Gestiona los mensajes entrantes para despertar la app y avisar al usuario.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Suscribir automáticamente al tema global para notificaciones masivas gratuitas
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("comunidad_on_air")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Refuerzo de suscripción al recibir
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("comunidad_on_air")

        // Prioridad a notificaciones de consola directas
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "📻 ON AIR", it.body ?: "Alguien está modulando...")
            return
        }

        // Procesamiento de datos (para filtrado por ciudad)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "📻 ON AIR"
            val message = remoteMessage.data["message"] ?: "¡Nueva actividad en la frecuencia!"
            val targetCity = remoteMessage.data["target_city"]

            if (targetCity != null) {
                val prefs = getSharedPreferences("on_air_prefs", android.content.Context.MODE_PRIVATE)
                val userCity = prefs.getString("last_city", "") ?: ""
                
                if (userCity.uppercase() == targetCity.uppercase()) {
                    sendNotification(title, message)
                }
            } else {
                // DESPERTADOR GLOBAL
                sendNotification(title, message)
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "on_air_alerts"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // 🎨 LOGO REDONDO: Avatar de la notificación
        val largeIcon = android.graphics.BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat) 
            .setLargeIcon(largeIcon) // 🛡️ CIRCULAR AUTOMÁTICO
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 100, 50, 100)) // 🛡️ BEEP-BEEP VIBRATION
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX) // 🚀 PRIORIDAD MÁXIMA PARA DESPERTAR
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Radio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de voz entrante y mensajes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 100)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
