package com.example.timetable.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
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
                
                // Refresh Button
                Image(
                    provider = ImageProvider(android.R.drawable.ic_menu_rotate),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .size(24.dp)
                        .clickable(actionRunCallback<RefreshAction>()),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                )
                
                Spacer(modifier = GlanceModifier.width(12.dp))
                
                // Open App Button
                Image(
                    provider = ImageProvider(android.R.drawable.ic_menu_send),
                    contentDescription = "Open App",
                    modifier = GlanceModifier
                        .size(24.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                )
            }

            if (schedule.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No classes today!",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                    )
                }
            } else {
                val nowMinutes = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
                
                // Find ongoing class
                val ongoingSlot = schedule.find { slot ->
                    val start = TimeUtils.timeToMinutes(slot.fromTime)
                    val end = TimeUtils.timeToMinutes(slot.toTime)
                    nowMinutes in start until end
                }
                
                // If no ongoing, find next class
                val highlightedId = ongoingSlot?.id ?: schedule.find { slot ->
                    TimeUtils.timeToMinutes(slot.fromTime) > nowMinutes
                }?.id

                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(schedule) { slot ->
                        ScheduleItem(slot, isHighlighted = slot.id == highlightedId)
                    }
                }
            }
        }
    }

    @Composable
    private fun ScheduleItem(slot: Week, isHighlighted: Boolean) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(if (isHighlighted) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.secondaryContainer)
                .cornerRadius(12.dp)
                .padding(10.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val circleColor = if (slot.color != 0) Color(slot.color) else Color.Gray
                Box(
                    modifier = GlanceModifier
                        .size(10.dp)
                        .background(ColorProvider(circleColor))
                        .cornerRadius(5.dp)
                ) {}
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = slot.subject ?: "",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isHighlighted) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSecondaryContainer
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp))
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${TimeUtils.formatTo12Hour(slot.fromTime)} - ${TimeUtils.formatTo12Hour(slot.toTime)}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = if (isHighlighted) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSecondaryContainer
                    ),
                    modifier = GlanceModifier.padding(start = 18.dp)
                )
                if (!slot.room.isNullOrBlank()) {
                    Text(
                        text = " • Room: ${slot.room}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = if (isHighlighted) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        ScheduleWidget().updateAll(context)
        DeadlinesWidget().updateAll(context)
        AttendanceWidget().updateAll(context)
    }
}

class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}
