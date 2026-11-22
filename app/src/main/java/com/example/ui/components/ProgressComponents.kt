package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserStatsEntity
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun StreakAndXpHeader(
  stats: UserStatsEntity,
  modifier: Modifier = Modifier
) {
  val level = (stats.totalXp / 200) + 1
  val currentLevelXp = stats.totalXp % 200
  val levelProgress = currentLevelXp.toFloat() / 200f

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Streak Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      modifier = Modifier.weight(1f).testTag("streak_card")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(14.dp)
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFEDD5))
        ) {
          Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = "Streak",
            tint = Color(0xFFEA580C),
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "${stats.streakDays} Day Streak",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Keep learning today!",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Level & XP Card
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      modifier = Modifier.weight(1f).testTag("xp_card")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(14.dp)
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
          Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = "XP",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Lvl $level Scholar",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${stats.totalXp} XP",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { levelProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
          )
        }
      }
    }
  }
}

@Composable
fun CircularProgressRing(
  progress: Float,
  modifier: Modifier = Modifier,
  size: Dp = 80.dp,
  strokeWidth: Dp = 8.dp,
  primaryColor: Color = MaterialTheme.colorScheme.primary,
  trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 800),
    label = "progress_ring"
  )

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier.size(size)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val strokePx = strokeWidth.toPx()
      val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
      val topLeft = Offset(strokePx / 2, strokePx / 2)

      // Background Track
      drawArc(
        color = trackColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokePx)
      )

      // Progress Arc
      drawArc(
        color = primaryColor,
        startAngle = -90f,
        sweepAngle = animatedProgress * 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
      )
    }

    Text(
      text = "${(animatedProgress * 100).toInt()}%",
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun RealTimeMetricsGrid(
  stats: UserStatsEntity,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    MetricStatItem(
      icon = Icons.Filled.School,
      title = "Lessons",
      value = "${stats.completedLessonsCount}",
      accentColor = MaterialTheme.colorScheme.primary,
      modifier = Modifier.weight(1f)
    )
    MetricStatItem(
      icon = Icons.Filled.Quiz,
      title = "Quizzes",
      value = "${stats.passedQuizzesCount}",
      accentColor = SuccessGreen,
      modifier = Modifier.weight(1f)
    )
    MetricStatItem(
      icon = Icons.Filled.Timer,
      title = "Study Time",
      value = "${stats.totalMinutesStudied}m",
      accentColor = WarningAmber,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun MetricStatItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  value: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    modifier = modifier
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(12.dp).fillMaxWidth()
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(accentColor.copy(alpha = 0.15f))
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun WeeklyStudyChart(
  dailyMinutes: List<Int> = listOf(25, 40, 15, 60, 45, 30, 50),
  modifier: Modifier = Modifier
) {
  val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  val maxMinutes = (dailyMinutes.maxOrNull() ?: 60).coerceAtLeast(60).toFloat()
  val primaryColor = MaterialTheme.colorScheme.primary

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Text(
            text = "Weekly Study Activity",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Total ${dailyMinutes.sum()} mins this week",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(50.dp)
        ) {
          Text(
            text = "+18% vs last week",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Bar Chart Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(120.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val barWidth = 24.dp.toPx()
          val chartHeight = size.height - 20.dp.toPx()
          val spacing = (size.width - (barWidth * days.size)) / (days.size + 1)

          dailyMinutes.forEachIndexed { index, minutes ->
            val barHeight = (minutes / maxMinutes) * chartHeight
            val xOffset = spacing + index * (barWidth + spacing)
            val yOffset = chartHeight - barHeight

            // Background subtle bar slot
            drawRoundRect(
              color = primaryColor.copy(alpha = 0.12f),
              topLeft = Offset(xOffset, 0f),
              size = Size(barWidth, chartHeight),
              cornerRadius = CornerRadius(12f, 12f)
            )

            // Active bar with sleek solid primary fill
            drawRoundRect(
              color = primaryColor,
              topLeft = Offset(xOffset, yOffset),
              size = Size(barWidth, barHeight),
              cornerRadius = CornerRadius(12f, 12f)
            )
          }
        }
      }

      // Day labels
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        days.forEach { day ->
          Text(
            text = day,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
