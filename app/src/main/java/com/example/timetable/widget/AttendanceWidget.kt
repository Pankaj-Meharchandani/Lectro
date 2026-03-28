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
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.text.*
import com.example.timetable.activities.MainActivity
import com.example.timetable.model.Week
import com.example.timetable.utils.DbHelper
import com.example.timetable.utils.TimeUtils
import com.example.timetable.utils.WidgetUtils
import java.text.SimpleDateFormat
import java.util.*

import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

class AttendanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val index = prefs[intPreferencesKey("currentIndex")] ?: 0
            val unmarkedClasses = WidgetUtils.getUnmarkedClasses(context)
            
            // Adjust index if out of bounds (e.g. after marking a class)
            val activeIndex = if (unmarkedClasses.isEmpty()) 0 else index % unmarkedClasses.size
            val currentSlot = if (unmarkedClasses.isNotEmpty()) unmarkedClasses[activeIndex] else null

            AttendanceWidgetContent(
                slot = currentSlot,
                count = unmarkedClasses.size,
                currentIndex = activeIndex
            )
        }
    }

    @Composable
    private fun AttendanceWidgetContent(slot: Week?, count: Int, currentIndex: Int) {
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
                if (count > 1) {
                    Text(
                        text = "${currentIndex + 1} / $count",
                        style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant)
                    )
                }
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

            Row(
                modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Arrow
                if (count > 1) {
                    Box(
                        modifier = GlanceModifier
                            .size(32.dp)
                            .background(GlanceTheme.colors.primaryContainer)
                            .cornerRadius(16.dp)
                            .clickable(actionRunCallback<NavigateAction>(
                                actionParametersOf(ActionParameters.Key<Int>("delta") to -1)
                            )),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "‹",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onPrimaryContainer
                            )
                        )
                    }
                }

                Column(
                    modifier = GlanceModifier.defaultWeight().padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (slot == null) {
                        Text(
                            text = "Attendance marked for today!",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = GlanceTheme.colors.primary
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "You're all set.",
                            style = TextStyle(
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    } else {
                        Text(
                            text = slot.subject ?: "",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = GlanceTheme.colors.onSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "${TimeUtils.formatTo12Hour(slot.fromTime)} - ${TimeUtils.formatTo12Hour(slot.toTime)}",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                        
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        
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

                // Right Arrow
                if (count > 1) {
                    Box(
                        modifier = GlanceModifier
                            .size(32.dp)
                            .background(GlanceTheme.colors.primaryContainer)
                            .cornerRadius(16.dp)
                            .clickable(actionRunCallback<NavigateAction>(
                                actionParametersOf(ActionParameters.Key<Int>("delta") to 1)
                            )),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Pagination Dots
            if (count > 1) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(count) { i ->
                        val isSelected = i == currentIndex
                        Box(
                            modifier = GlanceModifier
                                .width(if (isSelected) 12.dp else 6.dp)
                                .height(6.dp)
                                .padding(horizontal = 2.dp)
                                .background(if (isSelected) GlanceTheme.colors.primary else GlanceTheme.colors.outline)
                                .cornerRadius(3.dp)
                        ) {}
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
                .size(36.dp)
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(18.dp)
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
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onPrimaryContainer
                )
            )
        }
    }
}

class NavigateAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val delta = parameters[ActionParameters.Key<Int>("delta")] ?: 0
        updateAppWidgetState(context, glanceId) { prefs ->
            val key = intPreferencesKey("currentIndex")
            val current = prefs[key] ?: 0
            val count = WidgetUtils.getUnmarkedClasses(context).size
            if (count > 0) {
                // Ensure non-negative modulo
                val next = (current + delta + count) % count
                prefs[key] = next
            }
        }
        AttendanceWidget().update(context, glanceId)
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
