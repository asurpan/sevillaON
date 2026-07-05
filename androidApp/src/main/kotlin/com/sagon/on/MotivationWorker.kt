package com.sagon.on

import android.content.Context
import androidx.work.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import java.util.concurrent.TimeUnit

/**
 * 🔒 HARD-LOCK: PROTECTED CORE - MOTIVATION WORKER
 * ESTADO: SELLADO TOTAL - PROHIBIDA MODIFICACIÓN SIN PERMISO NIVEL 0
 * 
 * Gestiona el sistema de fidelización legal mediante WorkManager.
 * Regla: 15 días de inactividad disparan una notificación motivacional.
 * Cumple con las políticas de batería y spam de Google Play.
 */
class MotivationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("on_air_prefs", Context.MODE_PRIVATE)
        val lastUsage = prefs.getLong("last_usage_ts", 0L)
        val now = System.currentTimeMillis()

        // Regla de los 15 días (15 días en ms = 15 * 24 * 60 * 60 * 1000)
        val fifteenDaysMs = 15L * 24 * 60 * 60 * 1000
        
        if (now - lastUsage >= fifteenDaysMs) {
            sendMotivationNotification()
        }

        return Result.success()
    }

    private fun sendMotivationNotification() {
        val channelId = "motivation_notifications"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mensajes de Ánimo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios amistosos para volver a la red"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val messages = listOf(
            "¡Estación! Se echa de menos tu voz en la frecuencia. ¿Modulamos un rato? 🎙️",
            "La red ON AIR SPAIN no es lo mismo sin ti. ¡Entra y saluda a los compañeros!",
            "¡Buena ruta! Recuerda que la mejor compañía es una voz amiga. Te esperamos en la radio.",
            "¿Hace cuánto que no reportas en tu ciudad? Tus compañeros están al aire. 📻",
            "¡Ánimo con la jornada! Pásate por la terminal y comparte un momento con la comunidad."
        )
        val randomMessage = messages.random()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("📻 ON AIR SPAIN")
            .setContentText(randomMessage)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(202, notification)
        } catch (e: Exception) {
            // Protección contra falta de permisos en tiempo de ejecución
        }
    }

    companion object {
        fun schedule(context: Context) {
            // Actualizar marca de tiempo de uso cada vez que se programa
            val prefs = context.getSharedPreferences("on_air_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_usage_ts", System.currentTimeMillis()).apply()

            val workRequest = PeriodicWorkRequestBuilder<MotivationWorker>(15, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "motivation_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
