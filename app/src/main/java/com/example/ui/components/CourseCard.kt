package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.model.Course
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun EnrolledCourseCard(
  course: Course,
  enrollment: CourseEnrollmentEntity,
  onResumeClick: () -> Unit,
  onCardClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accentColor = Color(course.accentColorHex)

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("enrolled_course_card_${course.id}")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Category & Level Chips
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(50.dp)
          ) {
            Text(
              text = course.category,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${course.level}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }

        if (enrollment.isCompleted) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = SuccessGreen,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Completed",
              color = SuccessGreen,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          Text(
            text = "${enrollment.progressPercent.toInt()}% Complete",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = course.title,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = course.tagline,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Real-time Progress Bar
      LinearProgressIndicator(
        progress = { (enrollment.progressPercent / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(50.dp)),
        color = if (enrollment.isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      )

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.AccessTime,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${course.totalDurationMinutes}m total",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = onResumeClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          ),
          shape = RoundedCornerShape(50.dp),
          modifier = Modifier.testTag("resume_lesson_btn_${course.id}")
        ) {
          Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (enrollment.isCompleted) "Review" else "Resume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun CatalogCourseCard(
  course: Course,
  isEnrolled: Boolean,
  progressPercent: Float,
  onCardClick: () -> Unit,
  onStartClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accentColor = Color(course.accentColorHex)

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("catalog_course_${course.id}")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Header Banner
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(50.dp)
        ) {
          Text(
            text = course.category,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = WarningAmber,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "${course.rating}",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = course.title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = course.tagline,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Instructor and lessons count
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "By ${course.instructor}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.OndemandVideo,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${course.totalLessonsCount} Lessons",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      if (isEnrolled && progressPercent > 0f) {
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
          progress = { (progressPercent / 100f).coerceIn(0f, 1f) },
          modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50.dp)),
          color = MaterialTheme.colorScheme.primary,
          trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      FilledTonalButton(
        onClick = onStartClick,
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.fillMaxWidth().testTag("enroll_start_btn_${course.id}")
      ) {
        Icon(
          imageVector = if (isEnrolled) Icons.Filled.PlayArrow else Icons.Outlined.Quiz,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isEnrolled) "Continue Course" else "Enroll & Start Learning",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }
  }
}
