package com.example.timetable.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.example.timetable.activities.MainActivity
import com.example.timetable.utils.WidgetUtils

class DeadlinesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val deadlines = WidgetUtils.getUpcomingDeadlines(context)
            DeadlinesWidgetContent(deadlines)
        }
    }

    @Composable
    private fun DeadlinesWidgetContent(deadlines: List<WidgetUtils.DeadlineItem>) {
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
                    text = "Next 7 Days",
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
                
                Spacer(modifier = GlanceModifier.width(8.dp))
                
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

            if (deadlines.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No upcoming deadlines!",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(deadlines) { item ->
                        DeadlineItemUI(item)
                    }
                }
            }
        }
    }

    @Composable
    private fun DeadlineItemUI(item: WidgetUtils.DeadlineItem) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(if (item.isAssignment) GlanceTheme.colors.secondaryContainer else GlanceTheme.colors.errorContainer)
                .cornerRadius(12.dp)
                .padding(10.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val circleColor = if (item.color != 0) Color(item.color) else Color.Gray
            Box(
                modifier = GlanceModifier
                    .size(10.dp)
                    .background(ColorProvider(circleColor))
                    .cornerRadius(5.dp)
            ) {}
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = item.title,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (item.isAssignment) GlanceTheme.colors.onSecondaryContainer else GlanceTheme.colors.onErrorContainer
                    ),
                    maxLines = 1
                )
                Text(
                    text = "${if (item.isAssignment) "Assignment" else "Exam"} • ${item.date}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = if (item.isAssignment) GlanceTheme.colors.onSecondaryContainer else GlanceTheme.colors.onErrorContainer
                    )
                )
            }
        }
    }
}

class DeadlinesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DeadlinesWidget()
}
