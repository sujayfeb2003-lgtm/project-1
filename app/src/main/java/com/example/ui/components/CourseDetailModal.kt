package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Course
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseDetailBottomSheet(
  course: Course,
  isEnrolled: Boolean,
  onDismiss: () -> Unit,
  onStartCourse: () -> Unit,
  onStartQuiz: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val accentColor = Color(course.accentColorHex)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("course_detail_bottom_sheet")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Header with Category, Rating, and Close
      item {
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
              text = "${course.category} • ${course.level}",
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${course.rating} (520+ Reviews)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }

      // 2. Title & Description
      item {
        Text(
          text = course.title,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          letterSpacing = (-0.2).sp,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 26.sp
        )
        Text(
          text = course.description,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 19.sp,
          modifier = Modifier.padding(top = 8.dp)
        )
      }

      // 3. Tags Flow
      item {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          course.tags.forEach { tag ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(50.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
              Text(
                text = "#$tag",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // 4. Quick Specs (Lessons, Minutes, Checkpoints)
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Lessons", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "${course.totalLessonsCount}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Duration", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "${course.totalDurationMinutes} mins", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Instructor", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = course.instructor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }

      // 5. Syllabus Modules & Lessons Preview
      item {
        Text(
          text = "Curriculum & Interactive Checkpoints",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      course.modules.forEach { module ->
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = module.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = module.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
              )

              module.lessons.forEachIndexed { idx, lesson ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(vertical = 4.dp)
                ) {
                  Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                      .size(22.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer)
                  ) {
                    Text(
                      text = "${idx + 1}",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = lesson.title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                  )
                  Text(
                    text = "${lesson.checkpoints.size} checkpoints",
                    fontSize = 10.sp,
                    color = WarningAmber,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }
      }

      // 6. Action Buttons
      item {
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
          Button(
            onClick = onStartCourse,
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("start_course_sheet_btn")
          ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isEnrolled) "Resume Video" else "Start Masterclass", fontWeight = FontWeight.Bold)
          }

          FilledTonalButton(
            onClick = onStartQuiz,
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.height(48.dp).testTag("quiz_course_sheet_btn")
          ) {
            Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Quiz", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
