package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.UserStatsEntity
import com.example.model.Course
import com.example.model.UserBadge
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.RealTimeMetricsGrid
import com.example.ui.components.StreakAndXpHeader
import com.example.ui.components.WeeklyStudyChart
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun ProgressAnalyticsScreen(
  stats: UserStatsEntity,
  enrollments: List<CourseEnrollmentEntity>,
  quizAttempts: List<QuizAttemptEntity>,
  badges: List<UserBadge>,
  courses: List<Course>,
  onViewCertificate: (Course) -> Unit,
  onStartQuiz: (Course) -> Unit,
  modifier: Modifier = Modifier
) {
  val completedCourses = enrollments.filter { it.isCompleted }.mapNotNull { e ->
    courses.find { it.id == e.courseId }
  }

  val totalEnrollments = enrollments.size
  val averageProgress = if (totalEnrollments > 0) {
    enrollments.map { it.progressPercent }.average().toFloat()
  } else 0f

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("progress_analytics_screen")
  ) {
    // 1. Header Title
    item {
      Column {
        Text(
          text = "Learning Analytics & Achievements",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.3).sp,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Real-time statistics, milestones, and verified certificates",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 2. Streak & XP Header
    item {
      StreakAndXpHeader(stats = stats)
    }

    // 3. Real-time Metrics Grid
    item {
      RealTimeMetricsGrid(stats = stats)
    }

    // 4. Overall Progress Overview Card
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(18.dp)
        ) {
          CircularProgressRing(
            progress = averageProgress / 100f,
            size = 76.dp,
            strokeWidth = 8.dp,
            primaryColor = MaterialTheme.colorScheme.primary
          )

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Overall Curriculum Progress",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "$totalEnrollments Active Courses • ${completedCourses.size} Graduated",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Avg Completion Rate: ${averageProgress.toInt()}%",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    // 5. Weekly Activity Chart
    item {
      WeeklyStudyChart()
    }

    // 6. Earned Certificates Showcase
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Verified Certificates",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
          color = WarningAmber.copy(alpha = 0.15f),
          shape = RoundedCornerShape(50.dp)
        ) {
          Text(
            text = "${completedCourses.size} Earned",
            color = WarningAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }
    }

    if (completedCourses.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.WorkspacePremium,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "No certificates yet",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Complete all lessons and pass the final quiz to earn your official certificate.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    } else {
      items(completedCourses) { course ->
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewCertificate(course) }
            .testTag("certificate_item_${course.id}")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(WarningAmber.copy(alpha = 0.2f))
            ) {
              Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = course.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Certified by ${course.instructor}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            FilledTonalButton(
              onClick = { onViewCertificate(course) },
              shape = RoundedCornerShape(50.dp)
            ) {
              Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 7. Badges & Milestones Grid
    item {
      Text(
        text = "Badges & Milestones",
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = MaterialTheme.colorScheme.onBackground
      )
    }

    items(badges) { badge ->
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth().testTag("badge_${badge.id}")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(14.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(if (badge.isUnlocked) MaterialTheme.colorScheme.primaryContainer else Color(0x33000000))
          ) {
            Icon(
              imageVector = if (badge.isUnlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
              contentDescription = null,
              tint = if (badge.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = badge.title,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            Text(
              text = badge.description,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (badge.isUnlocked) {
            Surface(
              color = SuccessGreen.copy(alpha = 0.15f),
              shape = RoundedCornerShape(50.dp)
            ) {
              Text(
                text = "UNLOCKED",
                color = SuccessGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
        }
      }
    }

    // 8. Recent Quiz Attempts Log
    item {
      Text(
        text = "Recent Quiz History",
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = MaterialTheme.colorScheme.onBackground
      )
    }

    if (quizAttempts.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "No quiz attempts recorded yet. Take an interactive quiz to test your comprehension!",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(quizAttempts.take(5)) { attempt ->
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(14.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(if (attempt.passed) SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f))
              ) {
                Icon(
                  imageVector = if (attempt.passed) Icons.Filled.CheckCircle else Icons.Filled.Quiz,
                  contentDescription = null,
                  tint = if (attempt.passed) SuccessGreen else WarningAmber,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Score: ${attempt.percentage}% (${attempt.score}/${attempt.totalQuestions})",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (attempt.passed) "Passed successfully" else "Retake recommended",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              color = if (attempt.passed) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
              shape = RoundedCornerShape(50.dp)
            ) {
              Text(
                text = if (attempt.passed) "PASSED" else "RETRY",
                color = if (attempt.passed) SuccessGreen else WarningAmber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
        }
      }
    }
  }
}
