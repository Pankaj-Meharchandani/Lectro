package com.example.timetable.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.example.timetable.activities.MainActivity
import com.example.timetable.model.Week
import com.example.timetable.utils.TimeUtils
import com.example.timetable.utils.WidgetUtils
import java.util.*

class ScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val schedule = WidgetUtils.getTodaySchedule(context)
            ScheduleWidgetContent(schedule)
        }
    }

    @Composable
    private fun ScheduleWidgetContent(schedule: List<Week>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(8.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Classes",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onSurface
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
            }

            if (schedule.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No classes today!",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(schedule) { slot ->
                        ScheduleItem(slot)
                    }
                }
            }
        }
    }

    @Composable
    private fun ScheduleItem(slot: Week) {
        val now = Calendar.getInstance()
        val timeParts = slot.fromTime?.split(":")
        val isOngoing = if (timeParts?.size == 2) {
            val start = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
            }
            val toParts = slot.toTime?.split(":")
            val end = Calendar.getInstance().apply {
                if (toParts?.size == 2) {
                    set(Calendar.HOUR_OF_DAY, toParts[0].toInt())
                    set(Calendar.MINUTE, toParts[1].toInt())
                }
            }
            now.after(start) && now.before(end)
        } else false

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(if (isOngoing) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.secondaryContainer)
                .cornerRadius(8.dp)
                .padding(8.dp)
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val circleColor = if (slot.color != 0) Color(slot.color) else Color.Gray
                Box(
                    modifier = GlanceModifier
                        .size(12.dp)
                        .background(ColorProvider(circleColor))
                        .cornerRadius(6.dp)
                ) {}
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = slot.subject ?: "",
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (isOngoing) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSecondaryContainer
                    ),
                    maxLines = 1
                )
            }
            Text(
                text = "${TimeUtils.formatTo12Hour(slot.fromTime)} - ${TimeUtils.formatTo12Hour(slot.toTime)}",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = if (isOngoing) GlanceTheme.colors.onPrimaryContainer.apply {  } else GlanceTheme.colors.onSecondaryContainer
                ),
                modifier = GlanceModifier.padding(start = 20.dp)
            )
            if (!slot.room.isNullOrBlank()) {
                Text(
                    text = "Room: ${slot.room}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = if (isOngoing) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSecondaryContainer
                    ),
                    modifier = GlanceModifier.padding(start = 20.dp)
                )
            }
        }
    }
}

class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}
