package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.data.local.entity.LessonProgressEntity
import com.example.data.local.entity.UserStatsEntity
import com.example.data.remote.supabase.SupabaseSyncStatus
import com.example.model.Course
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AccountSettingsState
import com.example.ui.viewmodel.AdminPanelUiState

@Composable
fun MyAccountScreen(
  currentUserEmail: String?,
  userStats: UserStatsEntity,
  enrollments: List<CourseEnrollmentEntity>,
  lessonProgressList: List<LessonProgressEntity>,
  allCourses: List<Course>,
  accountSettings: AccountSettingsState,
  supabaseSyncStatus: SupabaseSyncStatus,
  supportFeedbackMessage: String?,
  adminUiState: AdminPanelUiState,
  onOpenAuth: () -> Unit,
  onResumeLesson: (courseId: String, lessonId: String) -> Unit,
  onExploreCatalog: () -> Unit,
  onUpdateAccountSettings: (autoPlay: Boolean, speed: Float, reminders: Boolean, wifiOnly: Boolean) -> Unit,
  onUpdateAboutMe: (displayName: String, bio: String) -> Unit,
  onSubmitSupportTicket: (subject: String, message: String) -> Unit,
  onClearSupportFeedback: () -> Unit,
  onSyncNow: () -> Unit,
  onOpenAdminLogin: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showAboutMeEditor by remember { mutableStateOf(false) }
  var editNameInput by remember(accountSettings.displayName) { mutableStateOf(accountSettings.displayName) }
  var editBioInput by remember(accountSettings.aboutMeBio) { mutableStateOf(accountSettings.aboutMeBio) }

  var supportSubject by remember { mutableStateOf("") }
  var supportMessage by remember { mutableStateOf("") }
  var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

  val completedLessonsTotal = lessonProgressList.count { it.isCompleted }
  val initials = remember(currentUserEmail, accountSettings.displayName) {
    val source = currentUserEmail?.substringBefore("@") ?: accountSettings.displayName
    source.replace(" ", "").take(2).uppercase().ifBlank { "SS" }
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Account Profile Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
            ) {
              Text(
                text = initials,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = accountSettings.displayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = currentUserEmail ?: "Guest Mode • Sign in to sync progress",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            FilledTonalButton(
              onClick = onOpenAuth,
              modifier = Modifier.height(40.dp).testTag("account_auth_button")
            ) {
              Text(
                text = if (currentUserEmail != null) "Account" else "Sign In",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            ProfileMetricPill(label = "Total XP", value = "${userStats.totalXp}")
            ProfileMetricPill(label = "Courses", value = "${enrollments.size}")
            ProfileMetricPill(label = "Completed", value = "$completedLessonsTotal")
            ProfileMetricPill(label = "Streak", value = "${userStats.streakDays}d")
          }
        }
      }
    }

    // 2. Course Details Section
    item {
      SectionHeader(
        icon = Icons.Filled.School,
        title = "Course Details",
        subtitle = "Your enrolled masterclasses & progress"
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (enrollments.isEmpty()) {
            Text(
              text = "You haven't enrolled in a course yet.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(onClick = onExploreCatalog) {
              Text("Browse Course Catalog")
            }
          } else {
            enrollments.forEachIndexed { index, enrollment ->
              val course = allCourses.find { it.id == enrollment.courseId }
              if (course != null) {
                val totalLessons = course.modules.sumOf { it.lessons.size }.coerceAtLeast(1)
                val completedInCourse = lessonProgressList.count { it.courseId == course.id && it.isCompleted }
                val progress = (completedInCourse.toFloat() / totalLessons.toFloat()).coerceIn(0f, 1f)
                val targetLessonId = enrollment.lastPlayedLessonId?.takeIf { it.isNotBlank() }
                  ?: course.modules.firstOrNull()?.lessons?.firstOrNull()?.id
                  ?: ""

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = course.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "${course.category} • ${course.level} • $completedInCourse/$totalLessons lessons",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    FilledTonalButton(
                      onClick = { onResumeLesson(course.id, targetLessonId) },
                      modifier = Modifier.height(36.dp)
                    ) {
                      Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                  }

                  LinearProgressIndicator(
                    progress = { progress },
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp))
                  )
                }

                if (index < enrollments.lastIndex) {
                  HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
              }
            }
          }
        }
      }
    }

    // 3. Account Settings Section
    item {
      SectionHeader(
        icon = Icons.Filled.Settings,
        title = "Account Settings",
        subtitle = "Playback, notifications & cloud sync"
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          SettingSwitchRow(
            title = "Auto-Play Next Lesson",
            subtitle = "Continue automatically when a video finishes",
            checked = accountSettings.autoPlayNextLesson,
            onCheckedChange = {
              onUpdateAccountSettings(
                it,
                accountSettings.defaultPlaybackSpeed,
                accountSettings.studyRemindersEnabled,
                accountSettings.wifiOnlyDownloads
              )
            }
          )

          HorizontalDivider()

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Default Playback Speed", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf(1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                FilterChip(
                  selected = accountSettings.defaultPlaybackSpeed == speed,
                  onClick = {
                    onUpdateAccountSettings(
                      accountSettings.autoPlayNextLesson,
                      speed,
                      accountSettings.studyRemindersEnabled,
                      accountSettings.wifiOnlyDownloads
                    )
                  },
                  label = { Text("${speed}x", fontSize = 12.sp) }
                )
              }
            }
          }

          HorizontalDivider()

          SettingSwitchRow(
            title = "Daily Study Reminders",
            subtitle = "Keep your learning streak active",
            checked = accountSettings.studyRemindersEnabled,
            onCheckedChange = {
              onUpdateAccountSettings(
                accountSettings.autoPlayNextLesson,
                accountSettings.defaultPlaybackSpeed,
                it,
                accountSettings.wifiOnlyDownloads
              )
            }
          )

          HorizontalDivider()

          SettingSwitchRow(
            title = "Wi-Fi Only Media Streaming",
            subtitle = "Reduce mobile data usage during lessons",
            checked = accountSettings.wifiOnlyDownloads,
            onCheckedChange = {
              onUpdateAccountSettings(
                accountSettings.autoPlayNextLesson,
                accountSettings.defaultPlaybackSpeed,
                accountSettings.studyRemindersEnabled,
                it
              )
            }
          )

          HorizontalDivider()

          // Cloud Sync Status Row
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            val (syncLabel, syncColor, syncIcon) = when (supabaseSyncStatus) {
              is SupabaseSyncStatus.Connected -> Triple("Cloud Sync Active", SuccessGreen, Icons.Filled.CloudDone)
              is SupabaseSyncStatus.Syncing -> Triple("Syncing to Cloud...", MaterialTheme.colorScheme.primary, Icons.Filled.CloudSync)
              is SupabaseSyncStatus.NotConfigured -> Triple("Offline Mode (Add Secret Key)", WarningAmber, Icons.Filled.CloudOff)
              is SupabaseSyncStatus.Error -> Triple("Sync Issue", ErrorRed, Icons.Filled.CloudOff)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(syncIcon, contentDescription = null, tint = syncColor, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Supabase Cloud Backup", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(syncLabel, fontSize = 11.sp, color = syncColor)
              }
            }

            TextButton(onClick = onSyncNow) {
              Text("Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 4. Customer Support Section
    item {
      SectionHeader(
        icon = Icons.Filled.SupportAgent,
        title = "Customer Support",
        subtitle = "FAQs & direct help desk"
      )
    }

    item {
      val faqs = remember {
        listOf(
          "How are certificates awarded?" to "Pass the course's Final Quiz with 70% or higher to unlock and download your verified Eduverse Certificate.",
          "How does AI Quiz Generation work?" to "Open Studio from the header, choose a topic and difficulty, and Gemini AI builds custom multiple-choice questions and checkpoints.",
          "How do I reset my password?" to "Tap Account above, choose Email & Password, and tap 'Forgot password?' to receive a reset link in your inbox."
        )
      }

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          faqs.forEachIndexed { idx, (question, answer) ->
            val expanded = expandedFaqIndex == idx
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { expandedFaqIndex = if (expanded) null else idx }
                .padding(vertical = 6.dp, horizontal = 4.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Icon(
                    Icons.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(question, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                  imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                  contentDescription = if (expanded) "Collapse" else "Expand",
                  modifier = Modifier.size(18.dp)
                )
              }
              AnimatedVisibility(visible = expanded) {
                Text(
                  text = answer,
                  fontSize = 12.sp,
                  lineHeight = 17.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(start = 24.dp, top = 6.dp)
                )
              }
            }
            if (idx < faqs.lastIndex) {
              HorizontalDivider()
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

          Text("Contact Support", fontSize = 13.sp, fontWeight = FontWeight.Bold)

          if (!supportFeedbackMessage.isNullOrBlank()) {
            Surface(
              color = SuccessGreen.copy(alpha = 0.12f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(10.dp)
              ) {
                Text(
                  text = supportFeedbackMessage,
                  fontSize = 12.sp,
                  color = SuccessGreen,
                  modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClearSupportFeedback) {
                  Text("OK", fontSize = 11.sp)
                }
              }
            }
          }

          OutlinedTextField(
            value = supportSubject,
            onValueChange = { supportSubject = it },
            label = { Text("Subject") },
            placeholder = { Text("e.g., Video playback or billing question") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("support_subject_input")
          )

          OutlinedTextField(
            value = supportMessage,
            onValueChange = { supportMessage = it },
            label = { Text("How can we help?") },
            placeholder = { Text("Describe your question or issue...") },
            minLines = 3,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("support_message_input")
          )

          Button(
            onClick = {
              onSubmitSupportTicket(supportSubject, supportMessage)
              if (supportSubject.isNotBlank() && supportMessage.isNotBlank()) {
                supportSubject = ""
                supportMessage = ""
              }
            },
            enabled = supportSubject.isNotBlank() && supportMessage.isNotBlank(),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("support_submit_button")
          ) {
            Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Send Support Request", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 5. About Me Section
    item {
      SectionHeader(
        icon = Icons.Filled.Info,
        title = "About Me",
        subtitle = "Personal bio & Eduverse platform details"
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(accountSettings.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(
              onClick = { showAboutMeEditor = !showAboutMeEditor },
              modifier = Modifier.testTag("edit_about_me_button")
            ) {
              Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (showAboutMeEditor) "Cancel" else "Edit", fontSize = 12.sp)
            }
          }

          if (!showAboutMeEditor) {
            Text(
              text = accountSettings.aboutMeBio,
              fontSize = 13.sp,
              lineHeight = 19.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            OutlinedTextField(
              value = editNameInput,
              onValueChange = { editNameInput = it },
              label = { Text("Display Name") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("about_me_name_input")
            )
            OutlinedTextField(
              value = editBioInput,
              onValueChange = { editBioInput = it },
              label = { Text("About Me Bio") },
              minLines = 3,
              modifier = Modifier.fillMaxWidth().testTag("about_me_bio_input")
            )
            FilledTonalButton(
              onClick = {
                onUpdateAboutMe(editNameInput, editBioInput)
                showAboutMeEditor = false
              },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_about_me_button")
            ) {
              Text("Save Profile", fontWeight = FontWeight.Bold)
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Image(
                painter = painterResource(id = R.drawable.eduverse_logo),
                contentDescription = "Eduverse Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(28.dp)
                  .clip(RoundedCornerShape(8.dp))
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("App Version", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Eduverse v1.0", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Platforms", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Android • Web • iOS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }

    // 6. Footer Section: Admin Login (One-Time Single-Admin Access)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(4.dp))

        val isAdminUnlocked = adminUiState is AdminPanelUiState.Unlocked
        Surface(
          color = if (isAdminUnlocked) SuccessGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(
              1.dp,
              if (isAdminUnlocked) SuccessGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
              RoundedCornerShape(14.dp)
            )
            .clickable { onOpenAdminLogin() }
            .testTag("account_footer_admin_login_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(if (isAdminUnlocked) SuccessGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.primaryContainer)
              ) {
                Icon(
                  imageVector = if (isAdminUnlocked) Icons.Filled.VerifiedUser else Icons.Filled.AdminPanelSettings,
                  contentDescription = "Admin Login",
                  tint = if (isAdminUnlocked) SuccessGreen else MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = if (isAdminUnlocked) "Admin Analytics Dashboard" else "Admin Login",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (isAdminUnlocked) {
                    "Single-admin verified • View watchtime, traffic & clicks"
                  } else {
                    "Restricted • One-time login accepted for admin only"
                  },
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (isAdminUnlocked) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SectionHeader(
  icon: ImageVector,
  title: String,
  subtitle: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
      Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun ProfileMetricPill(label: String, value: String) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
    Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun SettingSwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}
