package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.data.local.entity.LessonProgressEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.UserStatsEntity
import com.example.data.remote.gemini.GeminiAiService
import com.example.data.repository.CourseRepository
import com.example.model.Course
import com.example.model.CourseModule
import com.example.model.Lesson
import com.example.model.LessonVisualType
import com.example.model.Quiz
import com.example.model.QuizQuestion
import com.example.model.UserBadge
import com.example.model.VideoCheckpoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppNavDestination {
  HOME,
  CATALOG,
  VIDEO_PLAYER,
  QUIZ_VIEW,
  PROGRESS_ANALYTICS,
  CREATOR_STUDIO,
  MY_ACCOUNT,
  ADMIN_PANEL
}

data class AccountSettingsState(
  val displayName: String = "Eduverse Learner",
  val aboutMeBio: String = "Passionate Android & AI developer mastering Jetpack Compose, Kotlin Coroutines, and cloud backends on Eduverse.",
  val autoPlayNextLesson: Boolean = true,
  val defaultPlaybackSpeed: Float = 1.0f,
  val studyRemindersEnabled: Boolean = true,
  val wifiOnlyDownloads: Boolean = false
)

sealed class AdminPanelUiState {
  data class Locked(val errorMessage: String? = null) : AdminPanelUiState()
  object Authenticating : AdminPanelUiState()
  data class Unlocked(
    val adminEmail: String,
    val metrics: com.example.data.remote.supabase.AdminDashboardMetricsResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
  ) : AdminPanelUiState()
}

enum class LessonTab {
  CHECKPOINTS,
  TRANSCRIPT,
  CODE,
  NOTES,
  OUTLINE
}

data class QuizState(
  val quiz: Quiz? = null,
  val currentQuestionIndex: Int = 0,
  val selectedAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex
  val isSubmitted: Boolean = false,
  val score: Int = 0,
  val timeRemainingSeconds: Int = 0,
  val totalQuestions: Int = 0,
  val isPassed: Boolean = false,
  val earnedXp: Int = 0
)

data class VideoPlayerState(
  val currentCourse: Course? = null,
  val currentLesson: Lesson? = null,
  val currentTimestampSeconds: Int = 0,
  val isPlaying: Boolean = false,
  val playbackSpeed: Float = 1.0f,
  val activeCheckpointPrompt: VideoCheckpoint? = null,
  val checkpointAnswerSelected: Int? = null,
  val checkpointFeedback: String? = null,
  val isCheckpointCorrect: Boolean? = null,
  val isLessonFinished: Boolean = false,
  val activeLessonTab: LessonTab = LessonTab.CHECKPOINTS,
  val isFullscreen: Boolean = false
)

class SkillStreamViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val repository = CourseRepository(database.skillStreamDao())

  // Dynamic list of all courses (combining preloaded and Room-stored custom courses)
  val allCourses: StateFlow<List<Course>> = repository.allCoursesFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, repository.defaultCourses)

  // Navigation State
  private val _currentNavDestination = MutableStateFlow(AppNavDestination.HOME)
  val currentNavDestination: StateFlow<AppNavDestination> = _currentNavDestination.asStateFlow()

  // Selected Course for detail bottom sheet / modal
  private val _selectedCourseDetail = MutableStateFlow<Course?>(null)
  val selectedCourseDetail: StateFlow<Course?> = _selectedCourseDetail.asStateFlow()

  // Video Player State
  private val _videoPlayerState = MutableStateFlow(VideoPlayerState())
  val videoPlayerState: StateFlow<VideoPlayerState> = _videoPlayerState.asStateFlow()

  // Quiz State
  private val _quizState = MutableStateFlow(QuizState())
  val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

  // Certificate Modal State
  private val _showCertificateForCourse = MutableStateFlow<Course?>(null)
  val showCertificateForCourse: StateFlow<Course?> = _showCertificateForCourse.asStateFlow()

  // Search & Filter
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedCategoryFilter = MutableStateFlow("All")
  val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

  private val _selectedLevelFilter = MutableStateFlow("All")
  val selectedLevelFilter: StateFlow<String> = _selectedLevelFilter.asStateFlow()

  // Creator Studio & Editing Course State
  private val _editingCourse = MutableStateFlow<Course?>(null)
  val editingCourse: StateFlow<Course?> = _editingCourse.asStateFlow()

  // AI Generation & Assistant States
  private val _isAiGenerating = MutableStateFlow(false)
  val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

  private val _aiStatusMessage = MutableStateFlow<String?>(null)
  val aiStatusMessage: StateFlow<String?> = _aiStatusMessage.asStateFlow()

  private val _aiHintDialogText = MutableStateFlow<String?>(null)
  val aiHintDialogText: StateFlow<String?> = _aiHintDialogText.asStateFlow()

  private val _aiExplanationDialogText = MutableStateFlow<String?>(null)
  val aiExplanationDialogText: StateFlow<String?> = _aiExplanationDialogText.asStateFlow()

  // DB Flows
  val enrollments: StateFlow<List<CourseEnrollmentEntity>> = repository.enrollments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val lessonProgressList: StateFlow<List<LessonProgressEntity>> = repository.allLessonProgress
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val quizAttempts: StateFlow<List<QuizAttemptEntity>> = repository.allQuizAttempts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val userStats: StateFlow<UserStatsEntity> = repository.userStats
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStatsEntity())

  // Bookmarks for current active lesson
  private val _activeBookmarks = MutableStateFlow<List<BookmarkEntity>>(emptyList())
  val activeBookmarks: StateFlow<List<BookmarkEntity>> = _activeBookmarks.asStateFlow()

  // My Account Settings & About Me (Persisted in SharedPreferences)
  private val accountPrefs = application.getSharedPreferences("skillstream_account_prefs", android.content.Context.MODE_PRIVATE)
  private val _accountSettings = MutableStateFlow(
    AccountSettingsState(
      displayName = accountPrefs.getString("display_name", "Eduverse Learner") ?: "Eduverse Learner",
      aboutMeBio = accountPrefs.getString(
        "about_me_bio",
        "Passionate Android & AI developer mastering Jetpack Compose, Kotlin Coroutines, and cloud backends on Eduverse."
      ) ?: "",
      autoPlayNextLesson = accountPrefs.getBoolean("auto_play_next", true),
      defaultPlaybackSpeed = accountPrefs.getFloat("default_speed", 1.0f),
      studyRemindersEnabled = accountPrefs.getBoolean("study_reminders", true),
      wifiOnlyDownloads = accountPrefs.getBoolean("wifi_only", false)
    )
  )
  val accountSettings: StateFlow<AccountSettingsState> = _accountSettings.asStateFlow()

  private val _supportFeedbackMessage = MutableStateFlow<String?>(null)
  val supportFeedbackMessage: StateFlow<String?> = _supportFeedbackMessage.asStateFlow()

  private var playbackTimerJob: Job? = null
  private var quizTimerJob: Job? = null

  init {
    viewModelScope.launch {
      allCourses.collect { courses ->
        repository.currentCoursesSnapshot = courses
      }
    }

    // Seed default enrollment if fresh and sync app usage / traffic visit to Supabase
    viewModelScope.launch {
      repository.enrollInCourse("course_android_compose", "lesson_state_hoisting")
      supabaseRepository.recordLocalTrafficVisit()
      syncUsageToSupabase("app_open", "App launched")
      if (supabaseRepository.isAdminUnlockedOnThisDevice()) {
        val claimedEmail = supabaseRepository.getClaimedAdminEmail() ?: "Admin"
        _adminUiState.value = AdminPanelUiState.Unlocked(adminEmail = claimedEmail, isLoading = true)
        refreshAdminMetrics()
      }
    }
  }

  // --- Navigation Controls ---
  fun navigateTo(destination: AppNavDestination) {
    if (destination != AppNavDestination.VIDEO_PLAYER && _videoPlayerState.value.isPlaying) {
      pauseVideo()
    }
    _currentNavDestination.value = destination
    if (destination == AppNavDestination.ADMIN_PANEL && supabaseRepository.isAdminUnlockedOnThisDevice()) {
      refreshAdminMetrics()
    }
  }

  fun showCourseDetail(course: Course?) {
    _selectedCourseDetail.value = course
    if (course != null) {
      trackLinkClick("Course: ${course.title}")
    }
  }

  fun updateAboutMe(displayName: String, bio: String) {
    val cleanName = displayName.trim().ifBlank { "Eduverse Learner" }
    val cleanBio = bio.trim()
    accountPrefs.edit()
      .putString("display_name", cleanName)
      .putString("about_me_bio", cleanBio)
      .apply()
    _accountSettings.value = _accountSettings.value.copy(
      displayName = cleanName,
      aboutMeBio = cleanBio
    )
    syncUsageToSupabase("profile_updated", "Updated About Me profile")
  }

  fun updateAccountSettings(
    autoPlayNextLesson: Boolean = _accountSettings.value.autoPlayNextLesson,
    defaultPlaybackSpeed: Float = _accountSettings.value.defaultPlaybackSpeed,
    studyRemindersEnabled: Boolean = _accountSettings.value.studyRemindersEnabled,
    wifiOnlyDownloads: Boolean = _accountSettings.value.wifiOnlyDownloads
  ) {
    accountPrefs.edit()
      .putBoolean("auto_play_next", autoPlayNextLesson)
      .putFloat("default_speed", defaultPlaybackSpeed)
      .putBoolean("study_reminders", studyRemindersEnabled)
      .putBoolean("wifi_only", wifiOnlyDownloads)
      .apply()
    _accountSettings.value = _accountSettings.value.copy(
      autoPlayNextLesson = autoPlayNextLesson,
      defaultPlaybackSpeed = defaultPlaybackSpeed,
      studyRemindersEnabled = studyRemindersEnabled,
      wifiOnlyDownloads = wifiOnlyDownloads
    )
  }

  fun submitSupportTicket(subject: String, message: String) {
    val cleanSubject = subject.trim()
    val cleanMessage = message.trim()
    if (cleanSubject.isBlank() || cleanMessage.isBlank()) {
      _supportFeedbackMessage.value = "Please enter both a subject and message."
      return
    }
    syncUsageToSupabase(
      eventType = "support_ticket",
      eventDetail = "${cleanSubject.take(60)}: ${cleanMessage.take(140)}"
    )
    _supportFeedbackMessage.value = "Support request submitted! Our team will respond to your account email shortly."
  }

  fun clearSupportFeedback() {
    _supportFeedbackMessage.value = null
  }

  fun showCertificate(course: Course?) {
    _showCertificateForCourse.value = course
  }

  fun dismissCertificate() {
    _showCertificateForCourse.value = null
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun updateCategoryFilter(category: String) {
    _selectedCategoryFilter.value = category
  }

  fun updateLevelFilter(level: String) {
    _selectedLevelFilter.value = level
  }

  // --- Creator Studio Controls ---
  fun openCreatorStudio(courseToEdit: Course? = null) {
    _editingCourse.value = courseToEdit
    navigateTo(AppNavDestination.CREATOR_STUDIO)
  }

  fun saveCourse(course: Course) {
    viewModelScope.launch {
      repository.saveCourse(course)
      _editingCourse.value = null
      _aiStatusMessage.value = "Masterclass '${course.title}' saved successfully!"
    }
  }

  fun deleteCourse(courseId: String) {
    viewModelScope.launch {
      repository.deleteCourse(courseId)
      if (_editingCourse.value?.id == courseId) {
        _editingCourse.value = null
      }
      _aiStatusMessage.value = "Course removed."
    }
  }

  fun resetCourseToDefault(courseId: String) {
    viewModelScope.launch {
      repository.resetCourseToDefault(courseId)
      _aiStatusMessage.value = "Course restored to default masterclass curriculum."
    }
  }

  // --- GenAI for Quizzes & Checkpoints ---
  fun generateQuizWithAi(
    topic: String,
    level: String = "Intermediate",
    questionCount: Int = 4,
    courseId: String? = null,
    courseTitle: String? = null,
    onComplete: ((Quiz) -> Unit)? = null
  ) {
    viewModelScope.launch {
      _isAiGenerating.value = true
      _aiStatusMessage.value = "Generating high-yield questions with Gemini AI for '$topic'..."
      val result = GeminiAiService.generateQuiz(
        topic = topic,
        level = level,
        questionCount = questionCount,
        courseId = courseId ?: "custom_ai_${UUID.randomUUID().toString().take(6)}",
        courseTitle = courseTitle ?: topic
      )
      _isAiGenerating.value = false
      result.onSuccess { generatedQuiz ->
        _aiStatusMessage.value = "AI Quiz created successfully! (${generatedQuiz.questions.size} questions)"
        onComplete?.invoke(generatedQuiz)
      }.onFailure {
        _aiStatusMessage.value = "Failed to generate AI quiz. Please try again."
      }
    }
  }

  fun generateCheckpointsWithAi(
    lessonTitle: String,
    lessonDescription: String,
    durationSeconds: Int,
    codeSnippet: String? = null,
    onResult: (List<VideoCheckpoint>) -> Unit
  ) {
    viewModelScope.launch {
      _isAiGenerating.value = true
      _aiStatusMessage.value = "Generating interactive video checkpoints with Gemini AI..."
      val result = GeminiAiService.generateCheckpoints(
        lessonTitle = lessonTitle,
        lessonDescription = lessonDescription,
        durationSeconds = durationSeconds,
        codeSnippet = codeSnippet
      )
      _isAiGenerating.value = false
      result.onSuccess { checkpoints ->
        _aiStatusMessage.value = "Generated ${checkpoints.size} interactive checkpoints."
        onResult(checkpoints)
      }.onFailure {
        _aiStatusMessage.value = "Checkpoint generation failed."
      }
    }
  }

  fun requestAiHintForCurrentQuizQuestion() {
    val quiz = _quizState.value.quiz ?: return
    val currentIdx = _quizState.value.currentQuestionIndex
    val question = quiz.questions.getOrNull(currentIdx) ?: return

    viewModelScope.launch {
      _isAiGenerating.value = true
      val result = GeminiAiService.getQuizHint(question.prompt, question.options)
      _isAiGenerating.value = false
      result.onSuccess { hint ->
        _aiHintDialogText.value = hint
      }
    }
  }

  fun requestAiExplanationForCurrentQuizQuestion() {
    val quiz = _quizState.value.quiz ?: return
    val currentIdx = _quizState.value.currentQuestionIndex
    val question = quiz.questions.getOrNull(currentIdx) ?: return
    val selectedIndex = _quizState.value.selectedAnswers[currentIdx] ?: 0

    viewModelScope.launch {
      _isAiGenerating.value = true
      val result = GeminiAiService.explainQuizQuestion(
        prompt = question.prompt,
        codeSnippet = question.codeSnippet,
        options = question.options,
        selectedOptionIndex = selectedIndex,
        correctOptionIndex = question.correctAnswerIndex
      )
      _isAiGenerating.value = false
      result.onSuccess { explanation ->
        _aiExplanationDialogText.value = explanation
      }
    }
  }

  fun dismissAiDialogs() {
    _aiHintDialogText.value = null
    _aiExplanationDialogText.value = null
    _aiStatusMessage.value = null
  }

  // --- Video Player Controls ---
  fun openLesson(courseId: String, lessonId: String) {
    val currentList = allCourses.value
    val pair = repository.getLessonById(courseId, lessonId, currentList) ?: return
    val course = pair.first
    val lesson = pair.second

    viewModelScope.launch {
      trackLinkClick("Lesson: ${lesson.title}")
      repository.enrollInCourse(courseId, lessonId)
      val progress = database.skillStreamDao().getLessonProgressSync(lessonId)
      val startSeconds = progress?.watchedSeconds ?: 0

      _videoPlayerState.value = VideoPlayerState(
        currentCourse = course,
        currentLesson = lesson,
        currentTimestampSeconds = startSeconds,
        isPlaying = true,
        playbackSpeed = _accountSettings.value.defaultPlaybackSpeed,
        activeCheckpointPrompt = null,
        checkpointAnswerSelected = null,
        checkpointFeedback = null,
        isCheckpointCorrect = null,
        isLessonFinished = progress?.isCompleted == true,
        activeLessonTab = LessonTab.CHECKPOINTS
      )

      _currentNavDestination.value = AppNavDestination.VIDEO_PLAYER
      startPlaybackTimer()

      // Load bookmarks for this lesson
      database.skillStreamDao().getBookmarksForLesson(lessonId).collect { bookmarks ->
        _activeBookmarks.value = bookmarks
      }
    }
  }

  fun togglePlayPause() {
    val current = _videoPlayerState.value
    if (current.isPlaying) {
      pauseVideo()
    } else {
      resumeVideo()
    }
  }

  fun pauseVideo() {
    playbackTimerJob?.cancel()
    _videoPlayerState.value = _videoPlayerState.value.copy(isPlaying = false)
    flushPendingWatchSeconds()
  }

  fun resumeVideo() {
    _videoPlayerState.value = _videoPlayerState.value.copy(isPlaying = true)
    startPlaybackTimer()
  }

  fun seekTo(seconds: Int) {
    val lesson = _videoPlayerState.value.currentLesson ?: return
    val targetSeconds = seconds.coerceIn(0, lesson.durationSeconds)
    _videoPlayerState.value = _videoPlayerState.value.copy(
      currentTimestampSeconds = targetSeconds,
      activeCheckpointPrompt = null,
      checkpointAnswerSelected = null,
      checkpointFeedback = null
    )
    saveProgress(targetSeconds)
  }

  fun seekRelative(deltaSeconds: Int) {
    val current = _videoPlayerState.value.currentTimestampSeconds
    seekTo(current + deltaSeconds)
  }

  fun setPlaybackSpeed(speed: Float) {
    _videoPlayerState.value = _videoPlayerState.value.copy(playbackSpeed = speed)
    if (_videoPlayerState.value.isPlaying) {
      startPlaybackTimer()
    }
  }

  fun setLessonTab(tab: LessonTab) {
    _videoPlayerState.value = _videoPlayerState.value.copy(activeLessonTab = tab)
  }

  fun toggleFullscreen() {
    _videoPlayerState.value = _videoPlayerState.value.copy(isFullscreen = !_videoPlayerState.value.isFullscreen)
  }

  private var pendingWatchSeconds = 0

  private fun flushPendingWatchSeconds() {
    val delta = pendingWatchSeconds
    if (delta <= 0) return
    pendingWatchSeconds = 0
    val lessonTitle = _videoPlayerState.value.currentLesson?.title ?: "Video Lesson"
    syncUsageToSupabase(
      eventType = "video_watch",
      eventDetail = "$lessonTitle (+${delta}s)",
      watchSecondsDelta = delta
    )
  }

  private fun startPlaybackTimer() {
    playbackTimerJob?.cancel()
    playbackTimerJob = viewModelScope.launch {
      while (_videoPlayerState.value.isPlaying) {
        val speed = _videoPlayerState.value.playbackSpeed
        val delayMillis = (1000L / speed).toLong()
        delay(delayMillis)

        val state = _videoPlayerState.value
        val lesson = state.currentLesson ?: break
        val currentSec = state.currentTimestampSeconds
        val nextSec = currentSec + 1

        // Record 1 second of user watchtime
        supabaseRepository.recordLocalWatchSeconds(1)
        pendingWatchSeconds += 1
        if (pendingWatchSeconds >= 15) {
          flushPendingWatchSeconds()
        }

        // Check if there's an interactive checkpoint at this exact second
        val checkpoint = lesson.checkpoints.find { it.timestampSeconds == nextSec }
        val answeredCheckpoints = getAnsweredCheckpointsForLesson(lesson.id)

        if (checkpoint != null && !answeredCheckpoints.contains(checkpoint.timestampSeconds.toString())) {
          _videoPlayerState.value = state.copy(
            currentTimestampSeconds = nextSec,
            isPlaying = false,
            activeCheckpointPrompt = checkpoint,
            checkpointAnswerSelected = null,
            checkpointFeedback = null,
            isCheckpointCorrect = null
          )
          saveProgress(nextSec)
          flushPendingWatchSeconds()
          break
        }

        if (nextSec >= lesson.durationSeconds) {
          _videoPlayerState.value = state.copy(
            currentTimestampSeconds = lesson.durationSeconds,
            isPlaying = false,
            isLessonFinished = true
          )
          saveProgress(lesson.durationSeconds, isCompleted = true)
          flushPendingWatchSeconds()
          break
        } else {
          _videoPlayerState.value = state.copy(currentTimestampSeconds = nextSec)
          if (nextSec % 5 == 0) {
            saveProgress(nextSec)
          }
        }
      }
    }
  }

  private fun getAnsweredCheckpointsForLesson(lessonId: String): Set<String> {
    val progress = lessonProgressList.value.find { it.lessonId == lessonId }
    return progress?.checkpointsAnswered?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
  }

  fun answerCheckpoint(selectedOptionIndex: Int) {
    val state = _videoPlayerState.value
    val checkpoint = state.activeCheckpointPrompt ?: return
    val isCorrect = selectedOptionIndex == checkpoint.correctOptionIndex
    _videoPlayerState.value = state.copy(
      checkpointAnswerSelected = selectedOptionIndex,
      isCheckpointCorrect = isCorrect,
      checkpointFeedback = if (isCorrect) "Correct! " + checkpoint.explanation else "Incorrect. " + checkpoint.explanation
    )

    if (isCorrect) {
      val course = state.currentCourse ?: return
      val lesson = state.currentLesson ?: return
      viewModelScope.launch {
        repository.updateLessonProgress(
          courseId = course.id,
          lessonId = lesson.id,
          watchedSeconds = state.currentTimestampSeconds,
          totalSeconds = lesson.durationSeconds,
          isCompleted = false,
          answeredCheckpointTimestamp = checkpoint.timestampSeconds
        )
      }
    }
  }

  fun dismissCheckpointAndResume() {
    _videoPlayerState.value = _videoPlayerState.value.copy(
      activeCheckpointPrompt = null,
      checkpointAnswerSelected = null,
      checkpointFeedback = null,
      isCheckpointCorrect = null,
      isPlaying = true
    )
    startPlaybackTimer()
  }

  fun jumpToNextLesson() {
    val currentCourse = _videoPlayerState.value.currentCourse ?: return
    val currentLesson = _videoPlayerState.value.currentLesson ?: return
    val allLessons = currentCourse.modules.flatMap { it.lessons }
    val currentIndex = allLessons.indexOfFirst { it.id == currentLesson.id }
    if (currentIndex != -1 && currentIndex < allLessons.size - 1) {
      val nextLesson = allLessons[currentIndex + 1]
      openLesson(currentCourse.id, nextLesson.id)
    } else {
      startQuiz(currentCourse.finalQuiz)
    }
  }

  private fun saveProgress(seconds: Int, isCompleted: Boolean = false) {
    val course = _videoPlayerState.value.currentCourse ?: return
    val lesson = _videoPlayerState.value.currentLesson ?: return
    viewModelScope.launch {
      repository.updateLessonProgress(
        courseId = course.id,
        lessonId = lesson.id,
        watchedSeconds = seconds,
        totalSeconds = lesson.durationSeconds,
        isCompleted = isCompleted
      )
      if (isCompleted) {
        syncUsageToSupabase("lesson_completed", "${course.title}: ${lesson.title}")
      }
    }
  }

  fun addBookmark(noteText: String) {
    val course = _videoPlayerState.value.currentCourse ?: return
    val lesson = _videoPlayerState.value.currentLesson ?: return
    val currentSec = _videoPlayerState.value.currentTimestampSeconds
    viewModelScope.launch {
      repository.addBookmark(
        courseId = course.id,
        lessonId = lesson.id,
        timestampSeconds = currentSec,
        noteText = noteText.ifBlank { "Note at ${formatTime(currentSec)}" }
      )
    }
  }

  fun deleteBookmark(bookmarkId: Int) {
    viewModelScope.launch {
      repository.removeBookmark(bookmarkId)
    }
  }

  // --- Quiz Handling ---
  fun startQuiz(quiz: Quiz) {
    quizTimerJob?.cancel()
    _quizState.value = QuizState(
      quiz = quiz,
      currentQuestionIndex = 0,
      selectedAnswers = emptyMap(),
      isSubmitted = false,
      score = 0,
      timeRemainingSeconds = quiz.timeLimitMinutes * 60,
      totalQuestions = quiz.questions.size,
      isPassed = false,
      earnedXp = 0
    )
    _currentNavDestination.value = AppNavDestination.QUIZ_VIEW

    // Start quiz countdown timer
    quizTimerJob = viewModelScope.launch {
      while (_quizState.value.timeRemainingSeconds > 0 && !_quizState.value.isSubmitted) {
        delay(1000L)
        val remaining = _quizState.value.timeRemainingSeconds - 1
        _quizState.value = _quizState.value.copy(timeRemainingSeconds = remaining)
        if (remaining <= 0) {
          submitQuiz()
          break
        }
      }
    }
  }

  fun selectQuizOption(questionIndex: Int, optionIndex: Int) {
    if (_quizState.value.isSubmitted) return
    val updatedAnswers = _quizState.value.selectedAnswers.toMutableMap()
    updatedAnswers[questionIndex] = optionIndex
    _quizState.value = _quizState.value.copy(selectedAnswers = updatedAnswers)
  }

  fun nextQuizQuestion() {
    val current = _quizState.value
    val quiz = current.quiz ?: return
    if (current.currentQuestionIndex < quiz.questions.size - 1) {
      _quizState.value = current.copy(currentQuestionIndex = current.currentQuestionIndex + 1)
    }
  }

  fun prevQuizQuestion() {
    val current = _quizState.value
    if (current.currentQuestionIndex > 0) {
      _quizState.value = current.copy(currentQuestionIndex = current.currentQuestionIndex - 1)
    }
  }

  fun submitQuiz() {
    quizTimerJob?.cancel()
    val state = _quizState.value
    val quiz = state.quiz ?: return
    var score = 0
    quiz.questions.forEachIndexed { index, question ->
      val selected = state.selectedAnswers[index]
      if (selected == question.correctAnswerIndex) {
        score++
      }
    }
    val percentage = if (quiz.questions.isNotEmpty()) ((score.toFloat() / quiz.questions.size.toFloat()) * 100).toInt() else 0
    val isPassed = percentage >= quiz.passingPercentage
    val earnedXp = if (isPassed) quiz.xpReward else 30
    val timeSpent = (quiz.timeLimitMinutes * 60) - state.timeRemainingSeconds

    _quizState.value = state.copy(
      isSubmitted = true,
      score = score,
      isPassed = isPassed,
      earnedXp = earnedXp
    )

    viewModelScope.launch {
      repository.recordQuizAttempt(
        quizId = quiz.id,
        courseId = quiz.courseId,
        score = score,
        totalQuestions = quiz.questions.size,
        timeSpentSeconds = timeSpent
      )
      syncUsageToSupabase("quiz_completed", "${quiz.title} ($score/${quiz.questions.size})")
    }
  }

  fun retakeQuiz() {
    val currentQuiz = _quizState.value.quiz ?: return
    startQuiz(currentQuiz)
  }

  val getBadges: () -> List<UserBadge> = {
    val stats = userStats.value
    val unlocked = stats.unlockedBadges.split(",").filter { it.isNotBlank() }.toSet()
    repository.getBadges(unlocked)
  }

  // --- Supabase Backend & Auth Manager ---
  private val supabaseClient = com.example.data.remote.supabase.SupabaseClient(application)
  val supabaseRepository = com.example.data.remote.supabase.SupabaseRepository(supabaseClient)
  val supabaseSyncStatus = supabaseRepository.syncStatus
  val supabaseProjectId: String = supabaseClient.projectId

  val authManager = com.example.data.remote.FirebasePasswordlessAuthManager(application, supabaseRepository)
  val passwordlessAuthState = authManager.authState
  val currentUserEmail = authManager.currentUserEmail

  private val _showAuthDialog = MutableStateFlow(false)
  val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

  fun openAuthDialog() {
    _showAuthDialog.value = true
  }

  fun dismissAuthDialog() {
    _showAuthDialog.value = false
    authManager.resetState()
  }

  private val _adminUiState = MutableStateFlow<AdminPanelUiState>(AdminPanelUiState.Locked())
  val adminUiState: StateFlow<AdminPanelUiState> = _adminUiState.asStateFlow()

  fun submitSingleAdminLogin(email: String, password: String, isFirstTimeSetup: Boolean) {
    if (supabaseRepository.isAdminUnlockedOnThisDevice()) {
      refreshAdminMetrics()
      return
    }
    viewModelScope.launch {
      _adminUiState.value = AdminPanelUiState.Authenticating
      val result = supabaseRepository.claimOrLoginSingleAdmin(email, password, isFirstTimeSetup)
      result.fold(
        onSuccess = { claim ->
          val adminEmail = claim.adminEmail ?: email.trim()
          _adminUiState.value = AdminPanelUiState.Unlocked(adminEmail = adminEmail, isLoading = true)
          syncUsageToSupabase("admin_claimed", "Single admin login claimed")
          refreshAdminMetrics()
        },
        onFailure = { err ->
          _adminUiState.value = AdminPanelUiState.Locked(
            errorMessage = err.localizedMessage ?: "Admin authentication failed."
          )
        }
      )
    }
  }

  fun refreshAdminMetrics() {
    if (!supabaseRepository.isAdminUnlockedOnThisDevice()) return
    val currentEmail = supabaseRepository.getClaimedAdminEmail() ?: "Admin"
    val existingMetrics = (_adminUiState.value as? AdminPanelUiState.Unlocked)?.metrics
    _adminUiState.value = AdminPanelUiState.Unlocked(
      adminEmail = currentEmail,
      metrics = existingMetrics,
      isLoading = true,
      errorMessage = null
    )
    viewModelScope.launch {
      val result = supabaseRepository.fetchAdminDashboardMetrics()
      result.fold(
        onSuccess = { metrics ->
          _adminUiState.value = AdminPanelUiState.Unlocked(
            adminEmail = currentEmail,
            metrics = metrics,
            isLoading = false,
            errorMessage = null
          )
        },
        onFailure = { err ->
          _adminUiState.value = AdminPanelUiState.Unlocked(
            adminEmail = currentEmail,
            metrics = existingMetrics,
            isLoading = false,
            errorMessage = err.localizedMessage ?: "Failed to load Supabase analytics."
          )
        }
      )
    }
  }

  fun trackLinkClick(linkLabel: String) {
    supabaseRepository.recordLocalLinkClick()
    syncUsageToSupabase("link_click", linkLabel)
  }

  fun syncUsageToSupabase(
    eventType: String = "manual_sync",
    eventDetail: String? = null,
    watchSecondsDelta: Int = 0
  ) {
    viewModelScope.launch {
      val stats = userStats.value
      val enrolledCount = enrollments.value.size
      val completedCount = lessonProgressList.value.count { it.isCompleted }
      val passedQuizzes = quizAttempts.value.count { it.totalQuestions > 0 && (it.score * 100 / it.totalQuestions) >= 70 }
      supabaseRepository.syncUserUsage(
        stats = stats,
        enrolledCount = enrolledCount,
        lessonsCompletedCount = completedCount,
        quizzesPassedCount = passedQuizzes,
        eventType = eventType,
        eventDetail = eventDetail,
        watchSecondsDelta = watchSecondsDelta
      )
    }
  }

  fun sendPasswordlessLink(email: String) {
    viewModelScope.launch {
      authManager.sendSignInLinkToEmail(email)
    }
  }

  fun verifyPasswordlessLink(emailLink: String, fallbackEmail: String? = null) {
    viewModelScope.launch {
      val res = authManager.completeSignInWithEmailLink(emailLink, fallbackEmail)
      if (res.isSuccess) {
        syncUsageToSupabase("sign_in_magic_link", "Signed in via magic link")
      }
    }
  }

  fun signInWithEmailAndPassword(email: String, password: String) {
    viewModelScope.launch {
      val res = authManager.signInWithEmailAndPassword(email, password)
      if (res.isSuccess) {
        syncUsageToSupabase("sign_in", "Signed in with Email & Password")
      }
    }
  }

  fun createUserWithEmailAndPassword(email: String, password: String) {
    viewModelScope.launch {
      val res = authManager.createUserWithEmailAndPassword(email, password)
      if (res.isSuccess) {
        syncUsageToSupabase("sign_up", "Created account with Email & Password")
      }
    }
  }

  fun sendPasswordResetEmail(email: String) {
    viewModelScope.launch {
      authManager.sendPasswordResetEmail(email)
    }
  }

  fun signOutPasswordless() {
    viewModelScope.launch {
      authManager.signOut()
      syncUsageToSupabase("sign_out", "User signed out")
    }
  }

  fun handleIncomingDeepLink(link: String?) {
    if (link != null) {
      trackLinkClick("Deep link: ${link.take(60)}")
    }
    if (link != null && authManager.isSignInWithEmailLink(link)) {
      _showAuthDialog.value = true
      viewModelScope.launch {
        val res = authManager.completeSignInWithEmailLink(link)
        if (res.isSuccess) {
          syncUsageToSupabase("sign_in_magic_link", "Deep link verification")
        }
      }
    }
  }

  fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
  }
}

