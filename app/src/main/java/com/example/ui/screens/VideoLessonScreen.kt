package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.LessonProgressEntity
import com.example.model.Lesson
import com.example.model.VideoCheckpoint
import com.example.ui.components.VideoPlayerComposable
import com.example.ui.theme.CodeBgDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.LessonTab
import com.example.ui.viewmodel.VideoPlayerState

@Composable
fun VideoLessonScreen(
  playerState: VideoPlayerState,
  lessonProgressList: List<LessonProgressEntity>,
  bookmarks: List<BookmarkEntity>,
  onTogglePlay: () -> Unit,
  onSeek: (Int) -> Unit,
  onSeekRelative: (Int) -> Unit,
  onSetSpeed: (Float) -> Unit,
  onToggleFullscreen: () -> Unit,
  onAnswerCheckpoint: (Int) -> Unit,
  onResumeFromCheckpoint: () -> Unit,
  onNextLesson: () -> Unit,
  onSelectLessonTab: (LessonTab) -> Unit,
  onSelectAnotherLesson: (lessonId: String) -> Unit,
  onStartCourseQuiz: () -> Unit,
  onAddBookmark: (note: String) -> Unit,
  onDeleteBookmark: (Int) -> Unit,
  onBack: () -> Unit,
  formatTime: (Int) -> String,
  modifier: Modifier = Modifier
) {
  val course = playerState.currentCourse ?: return
  val lesson = playerState.currentLesson ?: return
  var showAddNoteDialog by remember { mutableStateOf(false) }
  var noteInput by remember { mutableStateOf("") }

  val tabs = listOf(
    Pair(LessonTab.CHECKPOINTS, "Checkpoints"),
    Pair(LessonTab.TRANSCRIPT, "Transcript"),
    Pair(LessonTab.CODE, "Code & Notes"),
    Pair(LessonTab.NOTES, "Bookmarks"),
    Pair(LessonTab.OUTLINE, "Outline")
  )

  Column(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("video_lesson_screen")
  ) {
    // 1. Top Header with Back button
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      IconButton(onClick = onBack, modifier = Modifier.testTag("lesson_back_button")) {
        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = course.title,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1
        )
        Text(
          text = lesson.title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1
        )
      }
    }

    // 2. Interactive Video Player Composable
    VideoPlayerComposable(
      state = playerState,
      onTogglePlay = onTogglePlay,
      onSeek = onSeek,
      onSeekRelative = onSeekRelative,
      onSetSpeed = onSetSpeed,
      onToggleFullscreen = onToggleFullscreen,
      onAnswerCheckpoint = onAnswerCheckpoint,
      onResumeFromCheckpoint = onResumeFromCheckpoint,
      onNextLesson = onNextLesson,
      onAddBookmarkClicked = { showAddNoteDialog = true },
      formatTime = formatTime,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )

    // 3. Interactive Lesson Tabs
    ScrollableTabRow(
      selectedTabIndex = tabs.indexOfFirst { it.first == playerState.activeLessonTab }.coerceAtLeast(0),
      edgePadding = 12.dp,
      containerColor = MaterialTheme.colorScheme.background,
      contentColor = MaterialTheme.colorScheme.primary,
      modifier = Modifier.fillMaxWidth()
    ) {
      tabs.forEach { (tab, label) ->
        val isSelected = playerState.activeLessonTab == tab
        Tab(
          selected = isSelected,
          onClick = { onSelectLessonTab(tab) },
          text = {
            Text(
              text = label,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 13.sp
            )
          },
          modifier = Modifier.testTag("lesson_tab_${tab.name}")
        )
      }
    }

    // 4. Tab Content Area
    Box(modifier = Modifier.weight(1f).padding(12.dp)) {
      when (playerState.activeLessonTab) {
        LessonTab.CHECKPOINTS -> {
          CheckpointsTabContent(
            checkpoints = lesson.checkpoints,
            currentSeconds = playerState.currentTimestampSeconds,
            onSeekToCheckpoint = { onSeek(it); onTogglePlay() },
            formatTime = formatTime
          )
        }

        LessonTab.TRANSCRIPT -> {
          TranscriptTabContent(
            transcripts = lesson.transcripts,
            currentSeconds = playerState.currentTimestampSeconds,
            onJumpToTimestamp = { onSeek(it) },
            formatTime = formatTime
          )
        }

        LessonTab.CODE -> {
          CodeAndTakeawaysContent(
            lesson = lesson,
            onCopyCode = {}
          )
        }

        LessonTab.NOTES -> {
          BookmarksTabContent(
            bookmarks = bookmarks,
            currentSeconds = playerState.currentTimestampSeconds,
            onAddNoteClicked = { showAddNoteDialog = true },
            onJumpToBookmark = { onSeek(it) },
            onDeleteBookmark = onDeleteBookmark,
            formatTime = formatTime
          )
        }

        LessonTab.OUTLINE -> {
          CourseOutlineContent(
            course = course,
            currentLessonId = lesson.id,
            progressList = lessonProgressList,
            onSelectLesson = onSelectAnotherLesson,
            onStartQuiz = onStartCourseQuiz
          )
        }
      }
    }

    // 5. Bottom Navigation Bar for Course
    Surface(
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        FilledTonalButton(onClick = onStartCourseQuiz, modifier = Modifier.testTag("take_course_quiz_btn")) {
          Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Course Quiz")
        }

        Button(onClick = onNextLesson, modifier = Modifier.testTag("bottom_next_lesson_btn")) {
          Text("Next Lesson ▶")
        }
      }
    }
  }

  // Add Note Dialog
  if (showAddNoteDialog) {
    AlertDialog(
      onDismissRequest = { showAddNoteDialog = false },
      title = {
        Text("Add Timestamp Note (${formatTime(playerState.currentTimestampSeconds)})")
      },
      text = {
        OutlinedTextField(
          value = noteInput,
          onValueChange = { noteInput = it },
          placeholder = { Text("Write your observation or key insight...") },
          modifier = Modifier.fillMaxWidth().testTag("bookmark_note_input")
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onAddBookmark(noteInput)
            noteInput = ""
            showAddNoteDialog = false
          },
          modifier = Modifier.testTag("save_bookmark_button")
        ) {
          Text("Save Note")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddNoteDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun CheckpointsTabContent(
  checkpoints: List<VideoCheckpoint>,
  currentSeconds: Int,
  onSeekToCheckpoint: (Int) -> Unit,
  formatTime: (Int) -> String
) {
  if (checkpoints.isEmpty()) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Text("No checkpoints in this lesson", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    return
  }

  LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(
        text = "Interactive Checkpoints in this Video",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Video automatically pauses at these timestamps for a real-time comprehension check.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    items(checkpoints) { cp ->
      val isPast = currentSeconds >= cp.timestampSeconds

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSeekToCheckpoint(cp.timestampSeconds) }
          .testTag("checkpoint_item_${cp.timestampSeconds}")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(14.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(if (isPast) SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f))
          ) {
            Icon(
              imageVector = if (isPast) Icons.Filled.Check else Icons.Filled.Flag,
              contentDescription = null,
              tint = if (isPast) SuccessGreen else WarningAmber,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = cp.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = formatTime(cp.timestampSeconds),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Text(
              text = cp.question,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TranscriptTabContent(
  transcripts: List<com.example.model.TranscriptSnippet>,
  currentSeconds: Int,
  onJumpToTimestamp: (Int) -> Unit,
  formatTime: (Int) -> String
) {
  LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    items(transcripts) { item ->
      val isCurrent = currentSeconds in item.startSecond..item.endSecond

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onJumpToTimestamp(item.startSecond) }
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = item.speaker,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = formatTime(item.startSecond),
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = item.text,
            fontSize = 13.sp,
            color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
          )
        }
      }
    }
  }
}

@Composable
private fun CodeAndTakeawaysContent(
  lesson: Lesson,
  onCopyCode: () -> Unit
) {
  LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    if (lesson.codePreview != null) {
      item {
        Text("Lesson Code Snippet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CodeBgDark)
            .padding(12.dp)
        ) {
          Text(
            text = lesson.codePreview,
            color = Color(0xFF38BDF8),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 17.sp
          )
        }
      }
    }

    item {
      Text("Key Takeaways", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }

    items(lesson.takeaways) { takeaway ->
      Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = null,
          tint = SuccessGreen,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = takeaway,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 18.sp
        )
      }
    }
  }
}

@Composable
private fun BookmarksTabContent(
  bookmarks: List<BookmarkEntity>,
  currentSeconds: Int,
  onAddNoteClicked: () -> Unit,
  onJumpToBookmark: (Int) -> Unit,
  onDeleteBookmark: (Int) -> Unit,
  formatTime: (Int) -> String
) {
  LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Your Timestamped Notes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        FilledTonalButton(
          onClick = onAddNoteClicked,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("add_note_button")
        ) {
          Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Note at ${formatTime(currentSeconds)}", fontSize = 11.sp)
        }
      }
    }

    if (bookmarks.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp).fillMaxWidth()
          ) {
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text("No notes added yet", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Tap the Bookmark icon while watching to add notes at that exact second.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    } else {
      items(bookmarks) { b ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth().clickable { onJumpToBookmark(b.timestampSeconds) }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = formatTime(b.timestampSeconds),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = b.noteText,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onDeleteBookmark(b.id) }, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CourseOutlineContent(
  course: com.example.model.Course,
  currentLessonId: String,
  progressList: List<LessonProgressEntity>,
  onSelectLesson: (String) -> Unit,
  onStartQuiz: () -> Unit
) {
  LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    course.modules.forEach { module ->
      item {
        Text(
          text = module.title,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.primary
        )
      }

      items(module.lessons) { l ->
        val isCurrent = l.id == currentLessonId
        val progress = progressList.find { it.lessonId == l.id }
        val isCompleted = progress?.isCompleted == true

        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectLesson(l.id) }
            .testTag("outline_lesson_${l.id}")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.surface)
            ) {
              if (isCompleted) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              } else {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = l.title,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${l.durationSeconds / 60}m ${l.durationSeconds % 60}s • ${l.checkpoints.size} Checkpoints",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(8.dp))
      Button(
        onClick = onStartQuiz,
        modifier = Modifier.fillMaxWidth().testTag("outline_take_quiz_button")
      ) {
        Icon(Icons.Filled.Quiz, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Take Course Final Assessment")
      }
    }
  }
}
