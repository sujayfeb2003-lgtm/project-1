package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.data.local.entity.UserStatsEntity
import com.example.model.Course
import com.example.ui.components.EnrolledCourseCard
import com.example.ui.components.RealTimeMetricsGrid
import com.example.ui.components.StreakAndXpHeader
import com.example.ui.components.WeeklyStudyChart
import com.example.ui.theme.OnPrimaryContainerLight
import com.example.ui.theme.PrimaryContainerLight
import com.example.ui.theme.TertiaryLight

@Composable
fun LearnHomeScreen(
  stats: UserStatsEntity,
  enrollments: List<CourseEnrollmentEntity>,
  allCourses: List<Course>,
  onResumeLesson: (courseId: String, lessonId: String) -> Unit,
  onExploreCatalog: () -> Unit,
  onStartQuiz: (course: Course) -> Unit,
  onViewCertificate: (course: Course) -> Unit,
  modifier: Modifier = Modifier
) {
  val enrolledCourses = enrollments.mapNotNull { enrollment ->
    allCourses.find { it.id == enrollment.courseId }?.let { course ->
      Pair(course, enrollment)
    }
  }

  // Active / Most recently accessed course
  val activePair = enrolledCourses.firstOrNull() ?: allCourses.firstOrNull()?.let { course ->
    Pair(course, CourseEnrollmentEntity(courseId = course.id, progressPercent = 67f))
  }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("learn_home_screen")
  ) {
    // 1. Streak & XP Header
    item {
      StreakAndXpHeader(stats = stats)
    }

    // 2. Real-time Progress Sleek Card (Inspired directly by Sleek Interface Theme)
    if (activePair != null) {
      val (course, enrollment) = activePair
      val nextLessonId = enrollment.lastPlayedLessonId
        ?: course.modules.firstOrNull()?.lessons?.firstOrNull()?.id
        ?: ""

      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sleek_realtime_progress_card")
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "REAL-TIME PROGRESS",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${enrollment.progressPercent.toInt()}% Complete",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Progress Bar (Sleek Style)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val segments = 3
              val activeSegments = if (enrollment.progressPercent >= 66f) 2 else if (enrollment.progressPercent >= 33f) 1 else 0
              for (i in 0 until segments) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                      if (i <= activeSegments) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Lesson 4: User Personas & Empathy Mapping. You are doing great! Finish this module to unlock the next certificate.",
              fontSize = 13.sp,
              lineHeight = 19.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 3. Mid-Lesson / Interactive Quiz Hero Card (Sleek Theme Callout)
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = PrimaryContainerLight
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sleek_interactive_quiz_card")
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              verticalAlignment = Alignment.Top,
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(TertiaryLight)
              ) {
                Icon(
                  imageVector = Icons.Filled.School,
                  contentDescription = null,
                  tint = Color(0xFFD0BCFF),
                  modifier = Modifier.size(26.dp)
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Mid-Lesson Quiz",
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp,
                  color = OnPrimaryContainerLight
                )
                Text(
                  text = "Quick checkpoint: User Research Methods & In-video quiz",
                  fontSize = 12.sp,
                  color = OnPrimaryContainerLight.copy(alpha = 0.8f),
                  modifier = Modifier.padding(top = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Button(
                onClick = { onResumeLesson(course.id, nextLessonId) },
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = TertiaryLight,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("take_interactive_quiz_btn")
              ) {
                Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Take Interactive Quiz", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }
    }

    // 4. Real-time Metrics Grid
    item {
      RealTimeMetricsGrid(stats = stats)
    }

    // 5. Enrolled Courses Section
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "My Enrolled Courses",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        FilledTonalButton(
          onClick = onExploreCatalog,
          shape = RoundedCornerShape(50.dp)
        ) {
          Text("Explore Catalog", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
        }
      }
    }

    if (enrolledCourses.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp).fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Filled.Explore,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No enrolled courses yet",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Browse interactive masterclasses to get started",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = onExploreCatalog,
              shape = RoundedCornerShape(50.dp)
            ) {
              Text("Browse Courses")
            }
          }
        }
      }
    } else {
      items(enrolledCourses, key = { it.first.id }) { (course, enrollment) ->
        val lessonId = enrollment.lastPlayedLessonId
          ?: course.modules.firstOrNull()?.lessons?.firstOrNull()?.id
          ?: ""

        EnrolledCourseCard(
          course = course,
          enrollment = enrollment,
          onResumeClick = { onResumeLesson(course.id, lessonId) },
          onCardClick = { onResumeLesson(course.id, lessonId) }
        )
      }
    }

    // 6. Weekly Learning Progress Chart
    item {
      WeeklyStudyChart()
    }
  }
}
