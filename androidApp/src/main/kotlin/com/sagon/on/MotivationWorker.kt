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

        // Recordatorio de 24 horas de inactividad
        val oneDayMs = 24L * 60 * 60 * 1000
        
        // 🛡️ REGLA DE CORTESÍA: Solo enviar en horario diurno (10:00 a 21:00) para no molestar ni ser pesado
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (now - lastUsage >= oneDayMs && currentHour in 10..21) {
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
                "Recordatorios de Ciudad",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios para explorar tu ciudad y modular con compañeros"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val messages = listOf(
            "🌆 ¡Tu ciudad te espera en 3D! Entra a la radio y vuela por la ciudad en vivo. 📻",
            "👂 MODO OREJA: ¿Estás en público? El Modo Oreja silencia el altavoz y te avisa con un tono discreto cuando te hablen.",
            "🚀 ¿Hace cuánto no sobrevuelas tu ciudad? Entra a ON AIR SPAIN y modula con los compañeros.",
            "🎧 MODO OREJA: Te permite estar atento a la emisora en privado pegando el móvil a tu oreja sin molestar a nadie.",
            "🎙️ ¡Estación! Explora el mapa en vivo de tu ciudad y saluda a los operadores conectados.",
            "📻 ¡Se echa de menos tu voz en la frecuencia! Entra a la ciudad y comparte ruta con la comunidad.",
            "🏙️ Tienes estaciones activas en tu zona. ¡Abre la app, activa el Modo Oreja en público y sobrevuela la ciudad!"
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
            val prefs = context.getSharedPreferences("on_air_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_usage_ts", System.currentTimeMillis()).apply()

            val workRequest = PeriodicWorkRequestBuilder<MotivationWorker>(2, TimeUnit.DAYS)
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
