package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Quiz
import com.example.model.QuizQuestion
import com.example.ui.theme.CodeBgDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.QuizState

@Composable
fun QuizQuestionCard(
  question: QuizQuestion,
  questionNumber: Int,
  totalQuestions: Int,
  selectedOptionIndex: Int?,
  isSubmitted: Boolean,
  onSelectOption: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    modifier = modifier.fillMaxWidth().testTag("quiz_question_card_$questionNumber")
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      // Category tag and Question number
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
            text = question.category,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        Text(
          text = "Question $questionNumber of $totalQuestions",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = question.prompt,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 23.sp
      )

      // Code snippet (if any)
      if (question.codeSnippet != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CodeBgDark)
            .padding(14.dp)
        ) {
          Text(
            text = question.codeSnippet,
            color = Color(0xFFD0BCFF),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Multiple Choice Options
      question.options.forEachIndexed { optIndex, optionText ->
        val isSelected = selectedOptionIndex == optIndex
        val isCorrect = optIndex == question.correctAnswerIndex

        val containerColor = when {
          !isSubmitted && isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
          isSubmitted && isCorrect -> SuccessGreen.copy(alpha = 0.2f)
          isSubmitted && isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.2f)
          else -> MaterialTheme.colorScheme.surface
        }

        val borderColor = when {
          !isSubmitted && isSelected -> MaterialTheme.colorScheme.primary
          isSubmitted && isCorrect -> SuccessGreen
          isSubmitted && isSelected && !isCorrect -> ErrorRed
          else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = !isSubmitted) { onSelectOption(optIndex) }
            .padding(14.dp)
            .testTag("quiz_option_${questionNumber}_$optIndex")
        ) {
          // Option Badge (A, B, C, D)
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(if (isSelected || (isSubmitted && isCorrect)) borderColor else MaterialTheme.colorScheme.primaryContainer)
          ) {
            if (isSubmitted && isCorrect) {
              Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else if (isSubmitted && isSelected && !isCorrect) {
              Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
              Text(
                text = ('A' + optIndex).toString(),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Text(
            text = optionText,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Detailed Explanation if submitted
      if (isSubmitted) {
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
          color = MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(14.dp)) {
            Icon(
              imageVector = Icons.Filled.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Explanation",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = question.explanation,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun QuizResultSummary(
  quizState: QuizState,
  onRetakeQuiz: () -> Unit,
  onClaimCertificate: () -> Unit,
  onBackToCourses: () -> Unit,
  modifier: Modifier = Modifier
) {
  val quiz = quizState.quiz ?: return
  val percentage = if (quiz.questions.isNotEmpty()) ((quizState.score.toFloat() / quiz.questions.size.toFloat()) * 100).toInt() else 0
  val isPassed = quizState.isPassed

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    modifier = modifier.fillMaxWidth().testTag("quiz_result_summary_card")
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(24.dp)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(
            if (isPassed) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
          )
      ) {
        Icon(
          imageVector = if (isPassed) Icons.Filled.EmojiEvents else Icons.Filled.Replay,
          contentDescription = null,
          tint = if (isPassed) SuccessGreen else WarningAmber,
          modifier = Modifier.size(40.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = if (isPassed) "Quiz Passed Successfully!" else "Keep Practicing!",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )

      Text(
        text = if (isPassed) "Mastery achieved! You scored $percentage%" else "You scored $percentage% (Passing requirement: ${quiz.passingPercentage}%)",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Score Stats Box
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(MaterialTheme.colorScheme.surface)
          .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            text = "${quizState.score}/${quiz.questions.size}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "XP Earned", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            text = "+${quizState.earnedXp} XP",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "Status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            text = if (isPassed) "PASSED" else "NEEDS WORK",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isPassed) SuccessGreen else WarningAmber
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      if (isPassed) {
        Button(
          onClick = onClaimCertificate,
          shape = RoundedCornerShape(50.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("claim_certificate_button")
        ) {
          Icon(Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("View Official Certificate", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedButton(
          onClick = onRetakeQuiz,
          shape = RoundedCornerShape(50.dp),
          modifier = Modifier.weight(1f).height(46.dp).testTag("retake_quiz_button")
        ) {
          Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Retake", fontWeight = FontWeight.SemiBold)
        }

        FilledTonalButton(
          onClick = onBackToCourses,
          shape = RoundedCornerShape(50.dp),
          modifier = Modifier.weight(1f).height(46.dp).testTag("back_to_courses_button")
        ) {
          Text("Return Home", fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
