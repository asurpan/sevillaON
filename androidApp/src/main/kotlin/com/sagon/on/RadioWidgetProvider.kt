package com.sagon.on

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat

/**
 * 📟 RADIO WIDGET PROVIDER: ACCESO TÁCTICO PTT DESDE EL ESCRITORIO
 * Muestra el estado del canal, LED y botón PTT instantáneo.
 */
class RadioWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_PTT = "com.sagon.on.ACTION_WIDGET_PTT"
        const val ACTION_WIDGET_APP = "com.sagon.on.ACTION_WIDGET_APP"
        const val ACTION_WIDGET_UPDATE = "com.sagon.on.ACTION_WIDGET_UPDATE"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, RadioWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_WIDGET_PTT -> {
                // 1. Iniciar RadioService en primer plano para asegurar audio
                try {
                    val serviceIntent = Intent(context, RadioService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(context, serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (_: Exception) {}

                // 2. Abrir la actividad principal de la radio al instante lista en PTT
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    action = "OPEN_CHAT"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(appIntent)
            }
            ACTION_WIDGET_APP -> {
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(appIntent)
            }
            ACTION_WIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, RadioWidgetProvider::class.java))
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val prefs = context.getSharedPreferences("on_air_prefs", Context.MODE_PRIVATE)
        val city = prefs.getString("last_city", "SEVILLA") ?: "SEVILLA"
        val cleanCity = city.split("-")[0].uppercase()
        val channel = prefs.getString("last_channel", cleanCity) ?: cleanCity

        val views = RemoteViews(context.packageName, R.layout.widget_radio)
        views.setTextViewText(R.id.widget_city, cleanCity)
        views.setTextViewText(R.id.widget_channel, "CANAL $channel")

        // Intent para PTT (botón grande)
        val pttIntent = Intent(context, RadioWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_PTT
        }
        val pttPendingIntent = PendingIntent.getBroadcast(
            context, 101, pttIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_ptt, pttPendingIntent)

        // Intent para abrir App (al pulsar en cualquier otra parte del widget)
        val appIntent = Intent(context, RadioWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_APP
        }
        val appPendingIntent = PendingIntent.getBroadcast(
            context, 102, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
