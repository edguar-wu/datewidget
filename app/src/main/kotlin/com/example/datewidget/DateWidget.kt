package com.example.datewidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.Calendar

class DateWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
        scheduleMidnightAlarm(context)
    }

    override fun onEnabled(context: Context) {
        scheduleMidnightAlarm(context)
    }

    override fun onDisabled(context: Context) {
        cancelMidnightAlarm(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == ACTION_MIDNIGHT_UPDATE || action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIMEZONE_CHANGED || action == Intent.ACTION_TIME_CHANGED) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DateWidget::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
            scheduleMidnightAlarm(context)
        }
    }

    companion object {
        const val ACTION_MIDNIGHT_UPDATE = "com.example.datewidget.MIDNIGHT_UPDATE"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val cal = Calendar.getInstance()
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            val dowLabels = arrayOf("", "日", "一", "二", "三", "四", "五", "六")
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            views.setTextViewText(R.id.tv_month, "${month}月")
            views.setTextViewText(R.id.tv_day, day.toString())
            views.setTextViewText(R.id.tv_dow, "週${dowLabels[dow]}")
            appWidgetManager.updateAppWidget(widgetId, views)
        }

        fun scheduleMidnightAlarm(context: Context) {
            val midnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 5)
                set(Calendar.MILLISECOND, 0)
            }
            val intent = Intent(context, DateWidget::class.java).apply {
                action = ACTION_MIDNIGHT_UPDATE
            }
            val pi = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // setWindow fires within 60s of midnight — no SCHEDULE_EXACT_ALARM permission needed
            am.setWindow(AlarmManager.RTC_WAKEUP, midnight.timeInMillis, 60_000L, pi)
        }

        fun cancelMidnightAlarm(context: Context) {
            val intent = Intent(context, DateWidget::class.java).apply {
                action = ACTION_MIDNIGHT_UPDATE
            }
            val pi = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pi)
        }
    }
}
