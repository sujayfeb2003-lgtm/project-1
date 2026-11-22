package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lesson
import com.example.model.LessonVisualType
import com.example.model.VideoCheckpoint
import com.example.ui.theme.CodeBgDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.VideoPlayerState

@Composable
fun VideoPlayerComposable(
  state: VideoPlayerState,
  onTogglePlay: () -> Unit,
  onSeek: (Int) -> Unit,
  onSeekRelative: (Int) -> Unit,
  onSetSpeed: (Float) -> Unit,
  onToggleFullscreen: () -> Unit,
  onAnswerCheckpoint: (Int) -> Unit,
  onResumeFromCheckpoint: () -> Unit,
  onNextLesson: () -> Unit,
  onAddBookmarkClicked: () -> Unit,
  formatTime: (Int) -> String,
  modifier: Modifier = Modifier
) {
  val lesson = state.currentLesson ?: return
  var showSpeedMenu by remember { mutableStateOf(false) }

  // Active live transcript line
  val currentTranscript = lesson.transcripts.find {
    state.currentTimestampSeconds in it.startSecond..it.endSecond
  }

  // Animation pulse for video playback simulation
  val infiniteTransition = rememberInfiniteTransition(label = "video_anim")
  val wavePulse by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = CodeBgDark),
    modifier = modifier
      .fillMaxWidth()
      .then(if (state.isFullscreen) Modifier.fillMaxSize() else Modifier.height(260.dp))
      .shadow(12.dp, RoundedCornerShape(24.dp))
      .testTag("video_player_card")
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // 1. Interactive Animated Video Canvas Screen
      AnimatedVideoContent(
        lesson = lesson,
        isPlaying = state.isPlaying,
        currentSeconds = state.currentTimestampSeconds,
        wavePulse = wavePulse,
        modifier = Modifier.fillMaxSize()
      )

      // 2. Live Subtitle/Caption Overlay
      if (currentTranscript != null && state.activeCheckpointPrompt == null) {
        Surface(
          color = Color(0xCC000000),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 60.dp, start = 16.dp, end = 16.dp)
        ) {
          Text(
            text = currentTranscript.text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }

      // 3. Top Video Overlay Controls (Title, Speed, Bookmark)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xCC000000), Color.Transparent)
            )
          )
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .align(Alignment.TopCenter)
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = lesson.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          Text(
            text = state.currentCourse?.title ?: "Eduverse Video Lesson",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            maxLines = 1
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Speed button
          Box {
            FilledTonalButton(
              onClick = { showSpeedMenu = true },
              shape = RoundedCornerShape(8.dp),
              contentPadding = ButtonDefaults.TextButtonContentPadding,
              modifier = Modifier.height(32.dp).testTag("playback_speed_button")
            ) {
              Text(
                text = "${state.playbackSpeed}x",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
            DropdownMenu(
              expanded = showSpeedMenu,
              onDismissRequest = { showSpeedMenu = false }
            ) {
              listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                DropdownMenuItem(
                  text = { Text("${speed}x") },
                  onClick = {
                    onSetSpeed(speed)
                    showSpeedMenu = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Bookmark action
          IconButton(
            onClick = onAddBookmarkClicked,
            modifier = Modifier.size(36.dp).testTag("video_bookmark_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.BookmarkAdd,
              contentDescription = "Add Timestamped Note",
              tint = Color.White
            )
          }

          // Fullscreen toggle
          IconButton(
            onClick = onToggleFullscreen,
            modifier = Modifier.size(36.dp).testTag("video_fullscreen_button")
          ) {
            Icon(
              imageVector = if (state.isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
              contentDescription = "Toggle Fullscreen",
              tint = Color.White
            )
          }
        }
      }

      // 4. Center Play/Pause & Skip Buttons
      if (!state.isPlaying && state.activeCheckpointPrompt == null && !state.isLessonFinished) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.fillMaxSize()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
          ) {
            IconButton(
              onClick = { onSeekRelative(-10) },
              modifier = Modifier
                .size(44.dp)
                .background(Color(0x88000000), CircleShape)
            ) {
              Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = Color.White)
            }

            IconButton(
              onClick = onTogglePlay,
              modifier = Modifier
                .size(60.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .testTag("center_play_button")
            ) {
              Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play Lesson",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
              )
            }

            IconButton(
              onClick = { onSeekRelative(10) },
              modifier = Modifier
                .size(44.dp)
                .background(Color(0x88000000), CircleShape)
            ) {
              Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White)
            }
          }
        }
      }

      // 5. Bottom Scrub Timeline & Controls
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color(0xE6000000))
            )
          )
          .padding(horizontal = 12.dp, vertical = 6.dp)
          .align(Alignment.BottomCenter)
      ) {
        // Timeline Scrub Bar with Checkpoint Markers
        Box(modifier = Modifier.fillMaxWidth()) {
          Slider(
            value = state.currentTimestampSeconds.toFloat(),
            onValueChange = { onSeek(it.toInt()) },
            valueRange = 0f..lesson.durationSeconds.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
              thumbColor = MaterialTheme.colorScheme.primary,
              activeTrackColor = MaterialTheme.colorScheme.primary,
              inactiveTrackColor = Color(0x66FFFFFF)
            ),
            modifier = Modifier.fillMaxWidth().testTag("video_timeline_slider")
          )

          // Draw Checkpoint Flag Dots on timeline
          lesson.checkpoints.forEach { cp ->
            val ratio = cp.timestampSeconds.toFloat() / lesson.durationSeconds.toFloat()
            Box(
              modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = (ratio * 280).dp) // Approximate visual marker
                .size(8.dp)
                .clip(CircleShape)
                .background(WarningAmber)
                .border(1.dp, Color.White, CircleShape)
            )
          }
        }

        // Bottom status row (Play/Pause mini, timestamps, checkpoints badge)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onTogglePlay,
              modifier = Modifier.size(32.dp).testTag("bottom_play_pause_button")
            ) {
              Icon(
                imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                tint = Color.White
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${formatTime(state.currentTimestampSeconds)} / ${formatTime(lesson.durationSeconds)}",
              color = Color.White,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          if (lesson.checkpoints.isNotEmpty()) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x33F59E0B))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${lesson.checkpoints.size} Checkpoints",
                color = WarningAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // 6. IN-VIDEO INTERACTIVE CHECKPOINT POPUP OVERLAY
      androidx.compose.animation.AnimatedVisibility(
        visible = state.activeCheckpointPrompt != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
      ) {
        val checkpoint = state.activeCheckpointPrompt
        if (checkpoint != null) {
          InVideoCheckpointOverlay(
            checkpoint = checkpoint,
            selectedIndex = state.checkpointAnswerSelected,
            isCorrect = state.isCheckpointCorrect,
            feedback = state.checkpointFeedback,
            onSelectOption = onAnswerCheckpoint,
            onResume = onResumeFromCheckpoint
          )
        }
      }

      // 7. LESSON COMPLETED OVERLAY
      if (state.isLessonFinished && state.activeCheckpointPrompt == null) {
        LessonFinishedOverlay(
          onReplay = { onSeek(0); onTogglePlay() },
          onNextLesson = onNextLesson
        )
      }
    }
  }
}

@Composable
private fun AnimatedVideoContent(
  lesson: Lesson,
  isPlaying: Boolean,
  currentSeconds: Int,
  wavePulse: Float,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(
        Brush.radialGradient(
          colors = listOf(Color(0xFF1E293B), Color(0xFF090D16)),
          center = Offset(400f, 300f),
          radius = 600f
        )
      )
      .padding(16.dp)
  ) {
    when (lesson.visualType) {
      LessonVisualType.CODE_EDITOR -> {
        Column {
          // Simulated IDE tabs
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF0F172A), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Box(
              modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "Composable.kt",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isPlaying) "⚡ Compiling & Streaming Live..." else "⏸ Live Simulation Paused",
              color = if (isPlaying) SuccessGreen else Color.Gray,
              fontSize = 10.sp
            )
          }

          // Code Box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .background(Color(0xFF0B1120), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
              .padding(8.dp)
          ) {
            Text(
              text = lesson.codePreview ?: "// Live Interactive Code Demo\nfun executeDemo() {\n   println(\"Step: $currentSeconds\")\n}",
              color = Color(0xFF38BDF8),
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              lineHeight = 16.sp
            )
          }
        }
      }

      LessonVisualType.ARCHITECTURE_CANVAS,
      LessonVisualType.DATA_FLOW_VIZ,
      LessonVisualType.DESIGN_SYSTEM_PREVIEW,
      LessonVisualType.TERMINAL_EXECUTION -> {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxSize()
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size((80 * wavePulse).dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
              .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
          ) {
            Text(
              text = "${lesson.visualType.name.take(12)}",
              color = MaterialTheme.colorScheme.primary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = if (isPlaying) "▶ Live Interactive Lecture Stream" else "Paused - Tap timeline or checkpoints to jump",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      LessonVisualType.LOCAL_VIDEO_PLAYER -> {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size((72 * wavePulse).dp)
              .clip(CircleShape)
              .background(Color(0xFF6366F1).copy(alpha = 0.25f))
              .border(2.dp, Color(0xFF818CF8), CircleShape)
          ) {
            Icon(
              imageVector = Icons.Filled.PlayArrow,
              contentDescription = null,
              tint = Color(0xFF818CF8),
              modifier = Modifier.size(36.dp)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = lesson.videoFileName ?: "Custom Uploaded Video Lesson",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
          )
          Text(
            text = if (isPlaying) "Streaming: ${lesson.title}" else "Video paused at $currentSeconds s",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
          )
          if (lesson.videoUri != null) {
            Text(
              text = "URI: ${lesson.videoUri.take(32)}...",
              color = Color(0xFF64748B),
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

@Composable
fun InVideoCheckpointOverlay(
  checkpoint: VideoCheckpoint,
  selectedIndex: Int?,
  isCorrect: Boolean?,
  feedback: String?,
  onSelectOption: (Int) -> Unit,
  onResume: () -> Unit
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xF2090D16))
      .padding(16.dp)
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = null,
            tint = WarningAmber,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = checkpoint.title,
            color = WarningAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = checkpoint.question,
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Options
        checkpoint.options.forEachIndexed { index, option ->
          val isThisSelected = selectedIndex == index
          val buttonBg = when {
            selectedIndex == null -> Color(0xFF0F172A)
            isThisSelected && isCorrect == true -> SuccessGreen.copy(alpha = 0.2f)
            isThisSelected && isCorrect == false -> ErrorRed.copy(alpha = 0.2f)
            index == checkpoint.correctOptionIndex && selectedIndex != null -> SuccessGreen.copy(alpha = 0.2f)
            else -> Color(0xFF0F172A)
          }

          val borderColor = when {
            isThisSelected && isCorrect == true -> SuccessGreen
            isThisSelected && isCorrect == false -> ErrorRed
            index == checkpoint.correctOptionIndex && selectedIndex != null -> SuccessGreen
            else -> Color(0xFF334155)
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(buttonBg)
              .border(1.dp, borderColor, RoundedCornerShape(8.dp))
              .clickable(enabled = selectedIndex == null) { onSelectOption(index) }
              .padding(horizontal = 10.dp, vertical = 8.dp)
              .testTag("checkpoint_option_$index")
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(borderColor)
            ) {
              Text(
                text = ('A' + index).toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = option,
              color = Color.White,
              fontSize = 12.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Feedback & Resume Button
        if (feedback != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = feedback,
            color = if (isCorrect == true) SuccessGreen else Color(0xFFFCA5A5),
            fontSize = 11.sp,
            lineHeight = 15.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = onResume,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isCorrect == true) MaterialTheme.colorScheme.primary else Color(0xFF475569)
            ),
            modifier = Modifier.fillMaxWidth().testTag("resume_video_button")
          ) {
            Text(if (isCorrect == true) "Continue Lesson ▶" else "Understood, Resume ▶")
          }
        }
      }
    }
  }
}

@Composable
fun LessonFinishedOverlay(
  onReplay: () -> Unit,
  onNextLesson: () -> Unit
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xEE090D16))
      .padding(16.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = null,
        tint = SuccessGreen,
        modifier = Modifier.size(48.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Lesson Completed! 🎉",
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "+50 XP Earned • Real-Time Progress Updated",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(16.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(onClick = onReplay) {
          Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Replay")
        }
        Button(
          onClick = onNextLesson,
          modifier = Modifier.testTag("next_lesson_button")
        ) {
          Text("Next Lesson / Quiz ▶")
        }
      }
    }
  }
}
