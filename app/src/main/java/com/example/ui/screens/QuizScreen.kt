package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Quiz
import com.example.ui.components.QuizQuestionCard
import com.example.ui.components.QuizResultSummary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.QuizState

@Composable
fun QuizScreen(
  quizState: QuizState,
  onSelectOption: (questionIndex: Int, optionIndex: Int) -> Unit,
  onNextQuestion: () -> Unit,
  onPrevQuestion: () -> Unit,
  onSubmitQuiz: () -> Unit,
  onRetakeQuiz: () -> Unit,
  onClaimCertificate: () -> Unit,
  onBackToCourses: () -> Unit,
  onRequestAiHint: () -> Unit = {},
  onRequestAiExplanation: () -> Unit = {},
  aiHintText: String? = null,
  aiExplanationText: String? = null,
  isAiGenerating: Boolean = false,
  onDismissAiDialog: () -> Unit = {},
  formatTime: (Int) -> String,
  modifier: Modifier = Modifier
) {
  val quiz = quizState.quiz

  if (quiz == null) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No quiz currently active", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBackToCourses) {
          Text("Browse Courses")
        }
      }
    }
    return
  }

  // AI Hint Dialog
  if (aiHintText != null) {
    AlertDialog(
      onDismissRequest = onDismissAiDialog,
      icon = { Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = WarningAmber) },
      title = { Text("Gemini AI Study Hint", fontWeight = FontWeight.Bold) },
      text = { Text(aiHintText, fontSize = 14.sp, lineHeight = 20.sp) },
      confirmButton = {
        Button(onClick = onDismissAiDialog) {
          Text("Got it!")
        }
      }
    )
  }

  // AI Explanation Dialog
  if (aiExplanationText != null) {
    AlertDialog(
      onDismissRequest = onDismissAiDialog,
      icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
      title = { Text("Gemini AI Concept Deep Dive", fontWeight = FontWeight.Bold) },
      text = { Text(aiExplanationText, fontSize = 14.sp, lineHeight = 20.sp) },
      confirmButton = {
        Button(onClick = onDismissAiDialog) {
          Text("Close")
        }
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("quiz_screen")
  ) {
    // 1. Top Bar with Title, Timer, and Back
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
      IconButton(onClick = onBackToCourses, modifier = Modifier.testTag("quiz_back_button")) {
        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(
          text = quiz.title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1
        )
        Text(
          text = "Pass mark: ${quiz.passingPercentage}% • +${quiz.xpReward} XP",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Live Timer Countdown Badge
      Surface(
        color = if (quizState.timeRemainingSeconds < 60) Color(0x33EF4444) else MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(50.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Timer,
            contentDescription = null,
            tint = if (quizState.timeRemainingSeconds < 60) Color(0xFFEF4444) else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = formatTime(quizState.timeRemainingSeconds),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (quizState.timeRemainingSeconds < 60) Color(0xFFEF4444) else MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }

    // 2. Progress Indicator
    val progress = (quizState.currentQuestionIndex + 1).toFloat() / quiz.questions.size.toFloat()
    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier.fillMaxWidth().height(4.dp),
      color = MaterialTheme.colorScheme.primary,
      trackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    // 3. Question Card or Result Summary
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (quizState.isSubmitted) {
        item {
          QuizResultSummary(
            quizState = quizState,
            onRetakeQuiz = onRetakeQuiz,
            onClaimCertificate = onClaimCertificate,
            onBackToCourses = onBackToCourses
          )
        }

        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Answer Review & Explanations",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onBackground
            )
            FilledTonalButton(
              onClick = onRequestAiExplanation,
              shape = RoundedCornerShape(50.dp),
              modifier = Modifier.testTag("ai_explain_button")
            ) {
              if (isAiGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Analyzing...")
              } else {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("GenAI Deep Dive")
              }
            }
          }
        }

        // Show all reviewed questions
        items(quiz.questions.size) { idx ->
          val q = quiz.questions[idx]
          QuizQuestionCard(
            question = q,
            questionNumber = idx + 1,
            totalQuestions = quiz.questions.size,
            selectedOptionIndex = quizState.selectedAnswers[idx],
            isSubmitted = true,
            onSelectOption = {}
          )
        }
      } else {
        val currentQ = quiz.questions.getOrNull(quizState.currentQuestionIndex)
        if (currentQ != null) {
          item {
            // GenAI Hint Action Bar
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
              Text(
                text = "Question ${quizState.currentQuestionIndex + 1} of ${quiz.questions.size}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )

              FilledTonalButton(
                onClick = onRequestAiHint,
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.height(34.dp).testTag("ai_hint_button")
              ) {
                if (isAiGenerating) {
                  CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Thinking...", fontSize = 11.sp)
                } else {
                  Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("GenAI Hint", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            QuizQuestionCard(
              question = currentQ,
              questionNumber = quizState.currentQuestionIndex + 1,
              totalQuestions = quiz.questions.size,
              selectedOptionIndex = quizState.selectedAnswers[quizState.currentQuestionIndex],
              isSubmitted = false,
              onSelectOption = { optIdx ->
                onSelectOption(quizState.currentQuestionIndex, optIdx)
              }
            )
          }
        }
      }
    }

    // 4. Bottom Controls (Prev, Next, Submit)
    if (!quizState.isSubmitted) {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          OutlinedButton(
            onClick = onPrevQuestion,
            enabled = quizState.currentQuestionIndex > 0,
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.height(46.dp).testTag("quiz_prev_button")
          ) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("Previous", fontWeight = FontWeight.SemiBold)
          }

          val isLast = quizState.currentQuestionIndex == quiz.questions.size - 1

          if (isLast) {
            Button(
              onClick = onSubmitQuiz,
              shape = RoundedCornerShape(50.dp),
              modifier = Modifier.height(46.dp).testTag("quiz_submit_button")
            ) {
              Text("Submit Quiz (${quizState.selectedAnswers.size}/${quiz.questions.size})", fontWeight = FontWeight.Bold)
            }
          } else {
            Button(
              onClick = onNextQuestion,
              shape = RoundedCornerShape(50.dp),
              modifier = Modifier.height(46.dp).testTag("quiz_next_button")
            ) {
              Text("Next", fontWeight = FontWeight.Bold)
              Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}
