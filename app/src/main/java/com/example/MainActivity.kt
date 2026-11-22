package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CertificateDialog
import com.example.ui.components.CourseDetailBottomSheet
import com.example.ui.components.PasswordlessAuthDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CreatorStudioScreen
import com.example.ui.screens.LearnHomeScreen
import com.example.ui.screens.MyAccountScreen
import com.example.ui.screens.ProgressAnalyticsScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.VideoLessonScreen
import com.example.ui.theme.SkillStreamTheme
import com.example.ui.theme.SleekNavBgLight
import com.example.ui.theme.SleekUserBadgeLight
import com.example.ui.theme.SleekUserBadgeText
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.SkillStreamViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: SkillStreamViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Handle incoming passwordless email link on app start
    handleAuthIntent(intent)

    setContent {
      SkillStreamTheme {
        MainAppContent(viewModel = viewModel)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleAuthIntent(intent)
  }

  private fun handleAuthIntent(incomingIntent: Intent?) {
    val dataString = incomingIntent?.dataString
    if (dataString != null) {
      viewModel.handleIncomingDeepLink(dataString)
    }
  }
}

@Composable
fun MainAppContent(viewModel: SkillStreamViewModel) {
  val currentDest by viewModel.currentNavDestination.collectAsStateWithLifecycle()
  val playerState by viewModel.videoPlayerState.collectAsStateWithLifecycle()
  val quizState by viewModel.quizState.collectAsStateWithLifecycle()
  val userStats by viewModel.userStats.collectAsStateWithLifecycle()
  val enrollments by viewModel.enrollments.collectAsStateWithLifecycle()
  val lessonProgressList by viewModel.lessonProgressList.collectAsStateWithLifecycle()
  val quizAttempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
  val bookmarks by viewModel.activeBookmarks.collectAsStateWithLifecycle()
  val selectedDetailCourse by viewModel.selectedCourseDetail.collectAsStateWithLifecycle()
  val certificateCourse by viewModel.showCertificateForCourse.collectAsStateWithLifecycle()
  val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()

  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val categoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
  val levelFilter by viewModel.selectedLevelFilter.collectAsStateWithLifecycle()

  val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
  val aiStatusMessage by viewModel.aiStatusMessage.collectAsStateWithLifecycle()
  val aiHintText by viewModel.aiHintDialogText.collectAsStateWithLifecycle()
  val aiExplanationText by viewModel.aiExplanationDialogText.collectAsStateWithLifecycle()
  val editingCourse by viewModel.editingCourse.collectAsStateWithLifecycle()

  val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
  val passwordlessAuthState by viewModel.passwordlessAuthState.collectAsStateWithLifecycle()
  val currentUserEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
  val supabaseSyncStatus by viewModel.supabaseSyncStatus.collectAsStateWithLifecycle()
  val adminUiState by viewModel.adminUiState.collectAsStateWithLifecycle()
  val accountSettings by viewModel.accountSettings.collectAsStateWithLifecycle()
  val supportFeedbackMessage by viewModel.supportFeedbackMessage.collectAsStateWithLifecycle()

  val badges = viewModel.getBadges()

  // Calculate initials from signed in email or default
  val displayInitials = remember(currentUserEmail) {
    if (!currentUserEmail.isNullOrBlank()) {
      val namePart = currentUserEmail!!.substringBefore("@").replace(".", "").replace("_", "")
      if (namePart.length >= 2) namePart.substring(0, 2).uppercase() else "ME"
    } else {
      "JD"
    }
  }

  // Hide bottom bar in video player when in fullscreen or when in Creator Studio
  val showBottomBar = !playerState.isFullscreen && currentDest != AppNavDestination.CREATOR_STUDIO

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    topBar = {
      if (currentDest != AppNavDestination.VIDEO_PLAYER && currentDest != AppNavDestination.CREATOR_STUDIO) {
        SleekHeader(
          activeDestination = currentDest,
          userInitials = displayInitials,
          isAuthenticated = currentUserEmail != null,
          userEmail = currentUserEmail,
          onOpenStudio = { viewModel.openCreatorStudio() },
          onOpenAuth = { viewModel.openAuthDialog() }
        )
      }
    },
    bottomBar = {
      if (showBottomBar) {
        SkillStreamBottomBar(
          currentDestination = currentDest,
          onNavigate = { dest -> viewModel.navigateTo(dest) },
          hasActiveVideo = playerState.currentLesson != null
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Crossfade(targetState = currentDest, label = "main_nav_crossfade") { destination ->
        when (destination) {
          AppNavDestination.HOME -> {
            LearnHomeScreen(
              stats = userStats,
              enrollments = enrollments,
              allCourses = allCourses,
              onResumeLesson = { courseId, lessonId ->
                viewModel.openLesson(courseId, lessonId)
              },
              onExploreCatalog = {
                viewModel.navigateTo(AppNavDestination.CATALOG)
              },
              onStartQuiz = { course ->
                viewModel.startQuiz(course.finalQuiz)
              },
              onViewCertificate = { course ->
                viewModel.showCertificate(course)
              }
            )
          }

          AppNavDestination.CATALOG -> {
            CatalogScreen(
              courses = allCourses,
              enrollments = enrollments,
              searchQuery = searchQuery,
              selectedCategory = categoryFilter,
              selectedLevel = levelFilter,
              onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
              onCategorySelected = { viewModel.updateCategoryFilter(it) },
              onLevelSelected = { viewModel.updateLevelFilter(it) },
              onCourseSelected = { course ->
                viewModel.showCourseDetail(course)
              },
              onStartCourse = { course ->
                val firstLessonId = course.modules.firstOrNull()?.lessons?.firstOrNull()?.id ?: ""
                viewModel.openLesson(course.id, firstLessonId)
              }
            )
          }

          AppNavDestination.VIDEO_PLAYER -> {
            VideoLessonScreen(
              playerState = playerState,
              lessonProgressList = lessonProgressList,
              bookmarks = bookmarks,
              onTogglePlay = { viewModel.togglePlayPause() },
              onSeek = { viewModel.seekTo(it) },
              onSeekRelative = { viewModel.seekRelative(it) },
              onSetSpeed = { viewModel.setPlaybackSpeed(it) },
              onToggleFullscreen = { viewModel.toggleFullscreen() },
              onAnswerCheckpoint = { viewModel.answerCheckpoint(it) },
              onResumeFromCheckpoint = { viewModel.dismissCheckpointAndResume() },
              onNextLesson = { viewModel.jumpToNextLesson() },
              onSelectLessonTab = { viewModel.setLessonTab(it) },
              onSelectAnotherLesson = { lessonId ->
                playerState.currentCourse?.let { course ->
                  viewModel.openLesson(course.id, lessonId)
                }
              },
              onStartCourseQuiz = {
                playerState.currentCourse?.let { course ->
                  viewModel.startQuiz(course.finalQuiz)
                }
              },
              onAddBookmark = { note ->
                viewModel.addBookmark(note)
              },
              onDeleteBookmark = { id ->
                viewModel.deleteBookmark(id)
              },
              onBack = {
                viewModel.navigateTo(AppNavDestination.HOME)
              },
              formatTime = { viewModel.formatTime(it) }
            )
          }

          AppNavDestination.QUIZ_VIEW -> {
            QuizScreen(
              quizState = quizState,
              onSelectOption = { qIdx, optIdx ->
                viewModel.selectQuizOption(qIdx, optIdx)
              },
              onNextQuestion = { viewModel.nextQuizQuestion() },
              onPrevQuestion = { viewModel.prevQuizQuestion() },
              onSubmitQuiz = { viewModel.submitQuiz() },
              onRetakeQuiz = { viewModel.retakeQuiz() },
              onClaimCertificate = {
                val quiz = quizState.quiz
                val course = allCourses.find { it.id == quiz?.courseId }
                if (course != null) {
                  viewModel.showCertificate(course)
                }
              },
              onBackToCourses = {
                viewModel.navigateTo(AppNavDestination.HOME)
              },
              onRequestAiHint = { viewModel.requestAiHintForCurrentQuizQuestion() },
              onRequestAiExplanation = { viewModel.requestAiExplanationForCurrentQuizQuestion() },
              aiHintText = aiHintText,
              aiExplanationText = aiExplanationText,
              isAiGenerating = isAiGenerating,
              onDismissAiDialog = { viewModel.dismissAiDialogs() },
              formatTime = { viewModel.formatTime(it) }
            )
          }

          AppNavDestination.PROGRESS_ANALYTICS -> {
            ProgressAnalyticsScreen(
              stats = userStats,
              enrollments = enrollments,
              quizAttempts = quizAttempts,
              badges = badges,
              courses = allCourses,
              onViewCertificate = { course ->
                viewModel.showCertificate(course)
              },
              onStartQuiz = { course ->
                viewModel.startQuiz(course.finalQuiz)
              }
            )
          }

          AppNavDestination.CREATOR_STUDIO -> {
            CreatorStudioScreen(
              courses = allCourses,
              initialEditingCourse = editingCourse,
              isAiGenerating = isAiGenerating,
              aiStatusMessage = aiStatusMessage,
              onSaveCourse = { viewModel.saveCourse(it) },
              onDeleteCourse = { viewModel.deleteCourse(it) },
              onResetCourseToDefault = { viewModel.resetCourseToDefault(it) },
              onGenerateQuizWithAi = { topic, lvl, count, cid, ctitle, onComp ->
                viewModel.generateQuizWithAi(topic, lvl, count, cid, ctitle, onComp)
              },
              onGenerateCheckpointsWithAi = { title, desc, dur, snip, onRes ->
                viewModel.generateCheckpointsWithAi(title, desc, dur, snip, onRes)
              },
              onBack = { viewModel.navigateTo(AppNavDestination.HOME) }
            )
          }

          AppNavDestination.MY_ACCOUNT -> {
            MyAccountScreen(
              currentUserEmail = currentUserEmail,
              userStats = userStats,
              enrollments = enrollments,
              lessonProgressList = lessonProgressList,
              allCourses = allCourses,
              accountSettings = accountSettings,
              supabaseSyncStatus = supabaseSyncStatus,
              supportFeedbackMessage = supportFeedbackMessage,
              adminUiState = adminUiState,
              onOpenAuth = { viewModel.openAuthDialog() },
              onResumeLesson = { courseId, lessonId ->
                viewModel.openLesson(courseId, lessonId)
              },
              onExploreCatalog = {
                viewModel.navigateTo(AppNavDestination.CATALOG)
              },
              onUpdateAccountSettings = { autoPlay, speed, reminders, wifiOnly ->
                viewModel.updateAccountSettings(autoPlay, speed, reminders, wifiOnly)
              },
              onUpdateAboutMe = { displayName, bio ->
                viewModel.updateAboutMe(displayName, bio)
              },
              onSubmitSupportTicket = { subject, message ->
                viewModel.submitSupportTicket(subject, message)
              },
              onClearSupportFeedback = {
                viewModel.clearSupportFeedback()
              },
              onSyncNow = {
                viewModel.syncUsageToSupabase("manual_sync", "User triggered sync from My Account")
              },
              onOpenAdminLogin = {
                viewModel.navigateTo(AppNavDestination.ADMIN_PANEL)
              }
            )
          }

          AppNavDestination.ADMIN_PANEL -> {
            AdminDashboardScreen(
              adminState = adminUiState,
              onSubmitAdminLogin = { email, password, isFirstTime ->
                viewModel.submitSingleAdminLogin(email, password, isFirstTime)
              },
              onRefreshMetrics = { viewModel.refreshAdminMetrics() },
              onBackToAccount = { viewModel.navigateTo(AppNavDestination.MY_ACCOUNT) }
            )
          }
        }
      }

      // Course Detail Modal Bottom Sheet
      if (selectedDetailCourse != null) {
        val detailCourse = selectedDetailCourse!!
        val isEnrolled = enrollments.any { it.courseId == detailCourse.id }
        CourseDetailBottomSheet(
          course = detailCourse,
          isEnrolled = isEnrolled,
          onDismiss = { viewModel.showCourseDetail(null) },
          onStartCourse = {
            viewModel.showCourseDetail(null)
            val firstLessonId = detailCourse.modules.firstOrNull()?.lessons?.firstOrNull()?.id ?: ""
            viewModel.openLesson(detailCourse.id, firstLessonId)
          },
          onStartQuiz = {
            viewModel.showCourseDetail(null)
            viewModel.startQuiz(detailCourse.finalQuiz)
          }
        )
      }

      // Official Certificate Modal Dialog
      if (certificateCourse != null) {
        CertificateDialog(
          course = certificateCourse!!,
          onDismiss = { viewModel.dismissCertificate() }
        )
      }

      // Auth Dialog (Supabase + Firebase Email/Password & Magic Link)
      if (showAuthDialog) {
        PasswordlessAuthDialog(
          authState = passwordlessAuthState,
          currentUserEmail = currentUserEmail,
          supabaseSyncStatus = supabaseSyncStatus,
          supabaseProjectId = viewModel.supabaseProjectId,
          onSyncToSupabase = { viewModel.syncUsageToSupabase("manual_sync", "User triggered sync") },
          onSendLink = { email -> viewModel.sendPasswordlessLink(email) },
          onVerifyLink = { link, fallbackEmail -> viewModel.verifyPasswordlessLink(link, fallbackEmail) },
          onSignInWithPassword = { email, password -> viewModel.signInWithEmailAndPassword(email, password) },
          onCreateAccountWithPassword = { email, password -> viewModel.createUserWithEmailAndPassword(email, password) },
          onSendPasswordReset = { email -> viewModel.sendPasswordResetEmail(email) },
          onSignOut = { viewModel.signOutPasswordless() },
          onDismiss = { viewModel.dismissAuthDialog() }
        )
      }
    }
  }
}

@Composable
fun SleekHeader(
  activeDestination: AppNavDestination,
  userInitials: String = "JD",
  isAuthenticated: Boolean = false,
  userEmail: String? = null,
  onOpenStudio: () -> Unit = {},
  onOpenAuth: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val title = when (activeDestination) {
    AppNavDestination.HOME -> "Eduverse"
    AppNavDestination.CATALOG -> "Explore Masterclasses"
    AppNavDestination.VIDEO_PLAYER -> "Interactive Lesson"
    AppNavDestination.QUIZ_VIEW -> "Knowledge Assessment"
    AppNavDestination.PROGRESS_ANALYTICS -> "Learning Analytics"
    AppNavDestination.CREATOR_STUDIO -> "Creator Studio"
    AppNavDestination.MY_ACCOUNT -> "My Account"
    AppNavDestination.ADMIN_PANEL -> "Admin Traffic Panel"
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Image(
        painter = painterResource(id = R.drawable.eduverse_logo),
        contentDescription = "Eduverse Logo",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .testTag("header_eduverse_logo")
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.3).sp,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = if (isAuthenticated && userEmail != null) userEmail else "Eduverse Masterclass",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      // Internal Access Creator Studio Pill Button
      FilledTonalButton(
        onClick = onOpenStudio,
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier.height(36.dp).testTag("header_open_studio_button")
      ) {
        Icon(Icons.Filled.VideoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Sleek User Avatar Pill with Passwordless Auth Trigger
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(if (isAuthenticated) SuccessGreen.copy(alpha = 0.2f) else SleekUserBadgeLight)
          .clickable { onOpenAuth() }
          .testTag("user_profile_avatar")
      ) {
        Text(
          text = userInitials,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = if (isAuthenticated) SuccessGreen else SleekUserBadgeText
        )
      }
    }
  }
}

@Composable
fun SkillStreamBottomBar(
  currentDestination: AppNavDestination,
  onNavigate: (AppNavDestination) -> Unit,
  hasActiveVideo: Boolean,
  modifier: Modifier = Modifier
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    tonalElevation = 4.dp,
    modifier = modifier
      .fillMaxWidth()
      .border(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
  ) {
    NavigationBar(
      modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
      containerColor = Color.Transparent,
      tonalElevation = 0.dp
    ) {
      NavigationBarItem(
        selected = currentDestination == AppNavDestination.HOME,
        onClick = { onNavigate(AppNavDestination.HOME) },
        icon = {
          Icon(
            imageVector = if (currentDestination == AppNavDestination.HOME) Icons.Filled.School else Icons.Outlined.School,
            contentDescription = "My Learning"
          )
        },
        label = { Text("Learn", fontSize = 10.sp, maxLines = 1, fontWeight = if (currentDestination == AppNavDestination.HOME) FontWeight.Bold else FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
          selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag("nav_item_learn")
      )

      NavigationBarItem(
        selected = currentDestination == AppNavDestination.CATALOG,
        onClick = { onNavigate(AppNavDestination.CATALOG) },
        icon = {
          Icon(
            imageVector = if (currentDestination == AppNavDestination.CATALOG) Icons.Filled.Explore else Icons.Outlined.Explore,
            contentDescription = "Explore Catalog"
          )
        },
        label = { Text("Explore", fontSize = 10.sp, maxLines = 1, fontWeight = if (currentDestination == AppNavDestination.CATALOG) FontWeight.Bold else FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
          selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag("nav_item_catalog")
      )

      NavigationBarItem(
        selected = currentDestination == AppNavDestination.VIDEO_PLAYER,
        onClick = { onNavigate(AppNavDestination.VIDEO_PLAYER) },
        icon = {
          BadgedBox(badge = {
            if (hasActiveVideo) {
              Badge(containerColor = MaterialTheme.colorScheme.primary)
            }
          }) {
            Icon(
              imageVector = if (currentDestination == AppNavDestination.VIDEO_PLAYER) Icons.Filled.PlayCircleFilled else Icons.Outlined.PlayCircleOutline,
              contentDescription = "Video Player"
            )
          }
        },
        label = { Text("Player", fontSize = 10.sp, maxLines = 1, fontWeight = if (currentDestination == AppNavDestination.VIDEO_PLAYER) FontWeight.Bold else FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
          selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag("nav_item_player")
      )

      NavigationBarItem(
        selected = currentDestination == AppNavDestination.QUIZ_VIEW,
        onClick = { onNavigate(AppNavDestination.QUIZ_VIEW) },
        icon = {
          Icon(
            imageVector = if (currentDestination == AppNavDestination.QUIZ_VIEW) Icons.Filled.Quiz else Icons.Outlined.Quiz,
            contentDescription = "Quizzes"
          )
        },
        label = { Text("Quiz", fontSize = 10.sp, maxLines = 1, fontWeight = if (currentDestination == AppNavDestination.QUIZ_VIEW) FontWeight.Bold else FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
          selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag("nav_item_quiz")
      )

      NavigationBarItem(
        selected = currentDestination == AppNavDestination.PROGRESS_ANALYTICS,
        onClick = { onNavigate(AppNavDestination.PROGRESS_ANALYTICS) },
        icon = {
          Icon(
            imageVector = if (currentDestination == AppNavDestination.PROGRESS_ANALYTICS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
            contentDescription = "Analytics"
          )
        },
        label = { Text("Stats", fontSize = 10.sp, maxLines = 1, fontWeight = if (currentDestination == AppNavDestination.PROGRESS_ANALYTICS) FontWeight.Bold else FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
          selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag("nav_item_analytics")
      )

      val isAccountSelected = currentDestination == AppNavDestination.MY_ACCOUNT ||
        currentDestination == AppNavDestination.ADMIN_PANEL
      NavigationBarItem(
        selected = isAccountSelected,
        onClick = { onNavigate(AppNavDestination.MY_ACCOUNT) },
        icon = {
          Icon(
            imageVector = if (isAccountSelected) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
            contentDescription = "My Account"
          )
        },
        label = { Text("Account", fontSize = 10.sp, maxLines = 1, fontWeight = if (isAccountSelected) FontWeight.Bold else FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
          selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag("nav_item_account")
      )
    }
  }
}
