package com.example.timetable.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.*
import com.example.timetable.activities.MainActivity
import com.example.timetable.model.Week
import com.example.timetable.utils.DbHelper
import com.example.timetable.utils.TimeUtils
import com.example.timetable.utils.WidgetUtils
import java.text.SimpleDateFormat
import java.util.*

class AttendanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val nowMinutes = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
            val schedule = WidgetUtils.getTodaySchedule(context)
            
            // Find current ongoing or next class
            val activeSlot = schedule.find { slot ->
                val start = TimeUtils.timeToMinutes(slot.fromTime)
                val end = TimeUtils.timeToMinutes(slot.toTime)
                nowMinutes in start until end
            } ?: schedule.find { slot ->
                TimeUtils.timeToMinutes(slot.fromTime) > nowMinutes
            }

            AttendanceWidgetContent(activeSlot)
        }
    }

    @Composable
    private fun AttendanceWidgetContent(slot: Week?) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(8.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                
                // Refresh Button
                Image(
                    provider = ImageProvider(android.R.drawable.ic_menu_rotate),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .size(20.dp)
                        .clickable(actionRunCallback<RefreshAction>()),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                )
                
                Spacer(modifier = GlanceModifier.width(8.dp))
                
                // Open App Button
                Image(
                    provider = ImageProvider(android.R.drawable.ic_menu_send),
                    contentDescription = "Open App",
                    modifier = GlanceModifier
                        .size(20.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                )
            }

            Column(
                modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (slot == null) {
                    Text(
                        text = "No more classes today!",
                        style = TextStyle(
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                } else {
                    Text(
                        text = "Mark Attendance",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.primary
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = slot.subject ?: "",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            color = GlanceTheme.colors.onSurface
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "${TimeUtils.formatTo12Hour(slot.fromTime)} - ${TimeUtils.formatTo12Hour(slot.toTime)}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                    
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        AttendanceButton(text = "P", type = "attended", slotId = slot.id, subject = slot.subject ?: "")
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        AttendanceButton(text = "A", type = "missed", slotId = slot.id, subject = slot.subject ?: "")
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        AttendanceButton(text = "C", type = "skipped", slotId = slot.id, subject = slot.subject ?: "")
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                }
            }
        }
    }

    @Composable
    private fun AttendanceButton(text: String, type: String, slotId: Int, subject: String) {
        val slotIdKey = ActionParameters.Key<Int>("slotId")
        val subjectKey = ActionParameters.Key<String>("subject")
        val typeKey = ActionParameters.Key<String>("type")

        Box(
            modifier = GlanceModifier
                .size(44.dp)
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(22.dp)
                .clickable(actionRunCallback<MarkAttendanceAction>(
                    actionParametersOf(
                        slotIdKey to slotId,
                        subjectKey to subject,
                        typeKey to type
                    )
                )),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GlanceTheme.colors.onPrimaryContainer
                )
            )
        }
    }
}

class MarkAttendanceAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val slotId = parameters[ActionParameters.Key<Int>("slotId")] ?: return
        val subject = parameters[ActionParameters.Key<String>("subject")] ?: return
        val type = parameters[ActionParameters.Key<String>("type")] ?: return
        
        val db = DbHelper(context)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.updateAttendance(slotId, subject, type, date)
        
        // Refresh all widgets
        ScheduleWidget().updateAll(context)
        DeadlinesWidget().updateAll(context)
        AttendanceWidget().updateAll(context)
    }
}

class AttendanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AttendanceWidget()
}
