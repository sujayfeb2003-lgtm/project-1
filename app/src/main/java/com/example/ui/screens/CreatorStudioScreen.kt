package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.model.Course
import com.example.model.CourseModule
import com.example.model.Lesson
import com.example.model.LessonVisualType
import com.example.model.Quiz
import com.example.model.QuizQuestion
import com.example.model.VideoCheckpoint
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import java.util.UUID

enum class StudioTab {
  COURSES_LIST,
  COURSE_EDITOR,
  AI_QUIZ_GENERATOR
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatorStudioScreen(
  courses: List<Course>,
  initialEditingCourse: Course?,
  isAiGenerating: Boolean,
  aiStatusMessage: String?,
  onSaveCourse: (Course) -> Unit,
  onDeleteCourse: (String) -> Unit,
  onResetCourseToDefault: (String) -> Unit,
  onGenerateQuizWithAi: (topic: String, level: String, questionCount: Int, courseId: String?, courseTitle: String?, onComplete: (Quiz) -> Unit) -> Unit,
  onGenerateCheckpointsWithAi: (lessonTitle: String, lessonDescription: String, durationSeconds: Int, codeSnippet: String?, onResult: (List<VideoCheckpoint>) -> Unit) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember {
    mutableStateOf(if (initialEditingCourse != null) StudioTab.COURSE_EDITOR else StudioTab.COURSES_LIST)
  }

  // Active course being created or edited
  var editingCourseId by remember { mutableStateOf(initialEditingCourse?.id ?: "course_custom_${UUID.randomUUID().toString().take(6)}") }
  var courseTitle by remember { mutableStateOf(initialEditingCourse?.title ?: "") }
  var courseTagline by remember { mutableStateOf(initialEditingCourse?.tagline ?: "") }
  var courseDescription by remember { mutableStateOf(initialEditingCourse?.description ?: "") }
  var courseCategory by remember { mutableStateOf(initialEditingCourse?.category ?: "Mobile Dev") }
  var courseLevel by remember { mutableStateOf(initialEditingCourse?.level ?: "Intermediate") }
  var courseInstructor by remember { mutableStateOf(initialEditingCourse?.instructor ?: "Internal Staff") }
  var courseInstructorRole by remember { mutableStateOf(initialEditingCourse?.instructorRole ?: "Lead Instructor") }
  var courseAccentColor by remember { mutableStateOf(initialEditingCourse?.accentColorHex ?: 0xFF6366F1) }
  var courseTags by remember { mutableStateOf(initialEditingCourse?.tags?.joinToString(", ") ?: "Compose, Kotlin, Android") }

  // Modules & Lessons
  val modulesState = remember {
    mutableStateListOf<CourseModule>().apply {
      if (initialEditingCourse != null) {
        addAll(initialEditingCourse.modules)
      } else {
        add(
          CourseModule(
            id = "mod_${UUID.randomUUID().toString().take(6)}",
            courseId = editingCourseId,
            title = "Module 1: Getting Started",
            description = "Fundamental principles and hands-on video session",
            order = 1,
            lessons = listOf(
              Lesson(
                id = "lesson_${UUID.randomUUID().toString().take(6)}",
                courseId = editingCourseId,
                moduleId = "mod_1",
                title = "Lesson 1: Introduction & Environment Setup",
                description = "Learn the core concepts and project setup.",
                durationSeconds = 180,
                visualType = LessonVisualType.CODE_EDITOR,
                checkpoints = listOf(
                  VideoCheckpoint(
                    timestampSeconds = 45,
                    title = "Live Checkpoint: Setup Check",
                    question = "What is the primary advantage of modern declarative UI?",
                    options = listOf(
                      "Automatic state synchronization and reactive re-renders",
                      "Increased boilerplate code",
                      "Synchronous blocking renders",
                      "Manual view tree invalidation"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Declarative UIs automatically reflect state changes without tedious DOM/view hierarchy tracking."
                  )
                )
              )
            )
          )
        )
      }
    }
  }

  // Final Quiz State for the course
  var finalQuizState by remember {
    mutableStateOf(
      initialEditingCourse?.finalQuiz ?: Quiz(
        id = "quiz_${editingCourseId}",
        courseId = editingCourseId,
        title = if (courseTitle.isNotBlank()) "$courseTitle Final Exam" else "Course Assessment",
        description = "Mastery verification quiz",
        passingPercentage = 75,
        xpReward = 150,
        timeLimitMinutes = 5,
        questions = listOf(
          QuizQuestion(
            id = "q_${UUID.randomUUID().toString().take(6)}",
            prompt = "What is the primary benefit of declarative state management?",
            options = listOf(
              "Unidirectional data flow and predictable state transitions",
              "Direct mutable manipulation of UI elements",
              "Ignoring lifecycle callbacks",
              "Disabling compiler optimizations"
            ),
            correctAnswerIndex = 0,
            explanation = "Unidirectional data flow ensures clear predictability and high testability."
          )
        )
      )
    )
  }

  // Active Lesson Editor Modal / Bottom Dialog state
  var editingLessonTarget by remember { mutableStateOf<Pair<Int, Lesson?>?>(null) } // moduleIndex to Lesson (null = new)

  // AI Quiz Generator dedicated tool state
  var aiQuizTopic by remember { mutableStateOf(if (courseTitle.isNotBlank()) courseTitle else "Modern Android Jetpack Compose & State Hoisting") }
  var aiQuizLevel by remember { mutableStateOf("Intermediate") }
  var aiQuizCount by remember { mutableStateOf(4) }
  var generatedAiQuizPreview by remember { mutableStateOf<Quiz?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Internal Access Studio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Text("Create, modify, upload videos & GenAI quizzes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("studio_back_button")) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (selectedTab == StudioTab.COURSE_EDITOR) {
            Button(
              onClick = {
                val totalLessons = modulesState.sumOf { it.lessons.size }
                val totalDuration = modulesState.sumOf { it.lessons.sumOf { l -> l.durationSeconds } } / 60
                val tagsList = courseTags.split(",").map { it.trim() }.filter { it.isNotBlank() }

                val course = Course(
                  id = editingCourseId,
                  title = courseTitle.ifBlank { "Untitled Masterclass" },
                  tagline = courseTagline.ifBlank { "Interactive Video Masterclass" },
                  description = courseDescription.ifBlank { "Comprehensive course designed for skill mastery." },
                  category = courseCategory,
                  level = courseLevel,
                  instructor = courseInstructor.ifBlank { "Internal Staff" },
                  instructorRole = courseInstructorRole.ifBlank { "Instructor" },
                  rating = 5.0f,
                  totalLessonsCount = totalLessons.coerceAtLeast(1),
                  totalDurationMinutes = totalDuration.coerceAtLeast(5),
                  accentColorHex = courseAccentColor,
                  modules = modulesState.toList(),
                  finalQuiz = finalQuizState.copy(courseId = editingCourseId),
                  tags = if (tagsList.isNotEmpty()) tagsList else listOf(courseCategory),
                  isUserCreated = true,
                  isModified = true
                )
                onSaveCourse(course)
                selectedTab = StudioTab.COURSES_LIST
              },
              shape = RoundedCornerShape(50.dp),
              modifier = Modifier.testTag("save_course_button")
            ) {
              Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save Course", fontWeight = FontWeight.Bold)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize().testTag("creator_studio_screen")
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // AI Status message banner
      if (aiStatusMessage != null) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(aiStatusMessage, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
          }
        }
      }

      // Studio Tabs
      TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        Tab(
          selected = selectedTab == StudioTab.COURSES_LIST,
          onClick = { selectedTab = StudioTab.COURSES_LIST },
          text = { Text("Courses (${courses.size})", fontWeight = FontWeight.SemiBold) },
          icon = { Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        Tab(
          selected = selectedTab == StudioTab.COURSE_EDITOR,
          onClick = { selectedTab = StudioTab.COURSE_EDITOR },
          text = { Text("Course & Video Editor", fontWeight = FontWeight.SemiBold) },
          icon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        Tab(
          selected = selectedTab == StudioTab.AI_QUIZ_GENERATOR,
          onClick = { selectedTab = StudioTab.AI_QUIZ_GENERATOR },
          text = { Text("GenAI Quiz Studio", fontWeight = FontWeight.SemiBold) },
          icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) }
        )
      }

      when (selectedTab) {
        StudioTab.COURSES_LIST -> {
          CoursesListTab(
            courses = courses,
            onAddNewCourse = {
              editingCourseId = "course_custom_${UUID.randomUUID().toString().take(6)}"
              courseTitle = ""
              courseTagline = ""
              courseDescription = ""
              courseCategory = "Mobile Dev"
              courseLevel = "Intermediate"
              courseInstructor = "Internal Staff"
              courseInstructorRole = "Lead Instructor"
              courseAccentColor = 0xFF6366F1
              courseTags = "Compose, Kotlin, Android"
              modulesState.clear()
              modulesState.add(
                CourseModule(
                  id = "mod_${UUID.randomUUID().toString().take(6)}",
                  courseId = editingCourseId,
                  title = "Module 1: Overview",
                  description = "Introductory video lessons and interactive checkpoints",
                  order = 1,
                  lessons = emptyList()
                )
              )
              finalQuizState = Quiz(
                id = "quiz_${editingCourseId}",
                courseId = editingCourseId,
                title = "Masterclass Assessment",
                description = "Mastery quiz",
                questions = emptyList()
              )
              selectedTab = StudioTab.COURSE_EDITOR
            },
            onEditCourse = { course ->
              editingCourseId = course.id
              courseTitle = course.title
              courseTagline = course.tagline
              courseDescription = course.description
              courseCategory = course.category
              courseLevel = course.level
              courseInstructor = course.instructor
              courseInstructorRole = course.instructorRole
              courseAccentColor = course.accentColorHex
              courseTags = course.tags.joinToString(", ")
              modulesState.clear()
              modulesState.addAll(course.modules)
              finalQuizState = course.finalQuiz
              selectedTab = StudioTab.COURSE_EDITOR
            },
            onDeleteCourse = onDeleteCourse,
            onResetCourse = onResetCourseToDefault
          )
        }

        StudioTab.COURSE_EDITOR -> {
          CourseEditorTab(
            courseId = editingCourseId,
            title = courseTitle,
            onTitleChange = { courseTitle = it },
            tagline = courseTagline,
            onTaglineChange = { courseTagline = it },
            description = courseDescription,
            onDescriptionChange = { courseDescription = it },
            category = courseCategory,
            onCategoryChange = { courseCategory = it },
            level = courseLevel,
            onLevelChange = { courseLevel = it },
            instructor = courseInstructor,
            onInstructorChange = { courseInstructor = it },
            tags = courseTags,
            onTagsChange = { courseTags = it },
            modules = modulesState,
            finalQuiz = finalQuizState,
            isAiGenerating = isAiGenerating,
            onAddModule = {
              val newModOrder = modulesState.size + 1
              modulesState.add(
                CourseModule(
                  id = "mod_${UUID.randomUUID().toString().take(6)}",
                  courseId = editingCourseId,
                  title = "Module $newModOrder: Advanced Concepts",
                  description = "Core video lecture and interactive demonstrations",
                  order = newModOrder,
                  lessons = emptyList()
                )
              )
            },
            onDeleteModule = { modIndex ->
              if (modIndex in modulesState.indices) {
                modulesState.removeAt(modIndex)
              }
            },
            onAddLessonClicked = { modIndex ->
              editingLessonTarget = Pair(modIndex, null)
            },
            onEditLessonClicked = { modIndex, lesson ->
              editingLessonTarget = Pair(modIndex, lesson)
            },
            onDeleteLesson = { modIndex, lessonId ->
              if (modIndex in modulesState.indices) {
                val mod = modulesState[modIndex]
                val updatedLessons = mod.lessons.filter { it.id != lessonId }
                modulesState[modIndex] = mod.copy(lessons = updatedLessons)
              }
            },
            onGenerateQuizForCourse = {
              onGenerateQuizWithAi(
                courseTitle.ifBlank { "Modern Software Engineering" },
                courseLevel,
                4,
                editingCourseId,
                courseTitle
              ) { generatedQuiz ->
                finalQuizState = generatedQuiz
              }
            }
          )
        }

        StudioTab.AI_QUIZ_GENERATOR -> {
          AiQuizStudioTab(
            topic = aiQuizTopic,
            onTopicChange = { aiQuizTopic = it },
            level = aiQuizLevel,
            onLevelChange = { aiQuizLevel = it },
            count = aiQuizCount,
            onCountChange = { aiQuizCount = it },
            isGenerating = isAiGenerating,
            generatedQuiz = generatedAiQuizPreview,
            onGenerate = {
              onGenerateQuizWithAi(aiQuizTopic, aiQuizLevel, aiQuizCount, editingCourseId, courseTitle.ifBlank { aiQuizTopic }) { q ->
                generatedAiQuizPreview = q
              }
            },
            onApplyQuizToCourse = { quiz ->
              finalQuizState = quiz
              selectedTab = StudioTab.COURSE_EDITOR
            }
          )
        }
      }
    }
  }

  // Lesson & Video Upload Dialog
  editingLessonTarget?.let { (modIndex, lessonToEdit) ->
    LessonVideoEditorDialog(
      courseId = editingCourseId,
      moduleId = modulesState.getOrNull(modIndex)?.id ?: "mod_1",
      initialLesson = lessonToEdit,
      isAiGenerating = isAiGenerating,
      onGenerateCheckpoints = { title, desc, duration, snippet, onRes ->
        onGenerateCheckpointsWithAi(title, desc, duration, snippet, onRes)
      },
      onDismiss = { editingLessonTarget = null },
      onSaveLesson = { savedLesson ->
        if (modIndex in modulesState.indices) {
          val currentMod = modulesState[modIndex]
          val updatedLessons = currentMod.lessons.toMutableList()
          val existingIdx = updatedLessons.indexOfFirst { it.id == savedLesson.id }
          if (existingIdx != -1) {
            updatedLessons[existingIdx] = savedLesson
          } else {
            updatedLessons.add(savedLesson)
          }
          modulesState[modIndex] = currentMod.copy(lessons = updatedLessons)
        }
        editingLessonTarget = null
      }
    )
  }
}

@Composable
private fun CoursesListTab(
  courses: List<Course>,
  onAddNewCourse: () -> Unit,
  onEditCourse: (Course) -> Unit,
  onDeleteCourse: (String) -> Unit,
  onResetCourse: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Text("Course & Video Inventory", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text("Manage internal masterclasses and video assets", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
          onClick = onAddNewCourse,
          shape = RoundedCornerShape(50.dp),
          modifier = Modifier.testTag("create_new_course_button")
        ) {
          Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Course")
        }
      }
    }

    items(courses, key = { it.id }) { course ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().testTag("studio_course_card_${course.id}")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(Color(course.accentColorHex))
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = course.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            if (course.isUserCreated) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "Custom",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(course.tagline, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

          Spacer(modifier = Modifier.height(8.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("📚 ${course.modules.size} Modules", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            Text("🎬 ${course.totalLessonsCount} Lessons", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            Text("⏱ ${course.totalDurationMinutes} mins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("🏷 ${course.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Actions
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
          ) {
            if (!course.isUserCreated) {
              TextButton(
                onClick = { onResetCourse(course.id) },
                modifier = Modifier.testTag("reset_course_${course.id}")
              ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", fontSize = 12.sp)
              }
            } else {
              TextButton(
                onClick = { onDeleteCourse(course.id) },
                colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed),
                modifier = Modifier.testTag("delete_course_${course.id}")
              ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", fontSize = 12.sp)
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
              onClick = { onEditCourse(course) },
              shape = RoundedCornerShape(50.dp),
              modifier = Modifier.testTag("edit_course_${course.id}")
            ) {
              Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Modify & Upload Videos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseEditorTab(
  courseId: String,
  title: String,
  onTitleChange: (String) -> Unit,
  tagline: String,
  onTaglineChange: (String) -> Unit,
  description: String,
  onDescriptionChange: (String) -> Unit,
  category: String,
  onCategoryChange: (String) -> Unit,
  level: String,
  onLevelChange: (String) -> Unit,
  instructor: String,
  onInstructorChange: (String) -> Unit,
  tags: String,
  onTagsChange: (String) -> Unit,
  modules: List<CourseModule>,
  finalQuiz: Quiz,
  isAiGenerating: Boolean,
  onAddModule: () -> Unit,
  onDeleteModule: (Int) -> Unit,
  onAddLessonClicked: (Int) -> Unit,
  onEditLessonClicked: (Int, Lesson) -> Unit,
  onDeleteLesson: (Int, String) -> Unit,
  onGenerateQuizForCourse: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Text("Course Metadata & Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text("Course Title") },
        placeholder = { Text("e.g. Masterclass: Advanced Kotlin Coroutines") },
        modifier = Modifier.fillMaxWidth().testTag("course_title_input")
      )

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = tagline,
        onValueChange = onTaglineChange,
        label = { Text("Tagline / Short Hook") },
        placeholder = { Text("e.g. Deep dive into flow architectures & reactive state") },
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Full Description") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      Text("Category & Difficulty Level", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      Spacer(modifier = Modifier.height(6.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Mobile Dev", "AI & Data", "System Design", "Cloud & DevOps", "Design").forEach { cat ->
          FilterChip(
            selected = category == cat,
            onClick = { onCategoryChange(cat) },
            label = { Text(cat) }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Beginner", "Intermediate", "Advanced").forEach { lvl ->
          FilterChip(
            selected = level == lvl,
            onClick = { onLevelChange(lvl) },
            label = { Text(lvl) }
          )
        }
      }
    }

    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = instructor,
          onValueChange = onInstructorChange,
          label = { Text("Instructor Name") },
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = tags,
          onValueChange = onTagsChange,
          label = { Text("Tags (comma-separated)") },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Video Modules & Lessons Section
    item {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Text("Modules & Video Lessons", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Text("Manage curriculum, upload video assets & interactive checkpoints", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FilledTonalButton(
          onClick = onAddModule,
          shape = RoundedCornerShape(50.dp),
          modifier = Modifier.testTag("add_module_button")
        ) {
          Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Module")
        }
      }
    }

    items(modules.size) { modIdx ->
      val module = modules[modIdx]
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(module.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(module.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onDeleteModule(modIdx) }) {
              Icon(Icons.Filled.Delete, contentDescription = "Delete Module", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // List lessons inside module
          if (module.lessons.isEmpty()) {
            Surface(
              color = MaterialTheme.colorScheme.surface,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
              Text(
                text = "No video lessons in this module yet.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
              )
            }
          } else {
            module.lessons.forEach { lesson ->
              Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp)
                  .clickable { onEditLessonClicked(modIdx, lesson) }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.padding(10.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                      imageVector = if (lesson.isCustomUploaded) Icons.Filled.Movie else Icons.Outlined.Videocam,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                      Text(lesson.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${lesson.durationSeconds}s (${lesson.durationSeconds / 60}m)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• ${lesson.checkpoints.size} Checkpoints", fontSize = 11.sp, color = WarningAmber)
                        if (lesson.isCustomUploaded) {
                          Text("• 📁 Uploaded Video", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                      }
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onEditLessonClicked(modIdx, lesson) }) {
                      Icon(Icons.Filled.Edit, contentDescription = "Edit Lesson", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onDeleteLesson(modIdx, lesson.id) }) {
                      Icon(Icons.Filled.Delete, contentDescription = "Delete Lesson", tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Add / Upload Lesson Button for this Module
          OutlinedButton(
            onClick = { onAddLessonClicked(modIdx) },
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.fillMaxWidth().testTag("upload_video_lesson_button_$modIdx")
          ) {
            Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add / Upload Video Lesson to Module")
          }
        }
      }
    }

    // Final Quiz Configuration Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Course Assessment Quiz", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            FilledTonalButton(
              onClick = onGenerateQuizForCourse,
              shape = RoundedCornerShape(50.dp),
              modifier = Modifier.testTag("ai_generate_quiz_for_course_button")
            ) {
              if (isAiGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generating...")
              } else {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Auto-Generate with AI")
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(finalQuiz.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text("${finalQuiz.questions.size} Questions • Pass mark: ${finalQuiz.passingPercentage}% • +${finalQuiz.xpReward} XP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

          if (finalQuiz.questions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            finalQuiz.questions.take(3).forEachIndexed { idx, q ->
              Text("${idx + 1}. ${q.prompt}", fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonVideoEditorDialog(
  courseId: String,
  moduleId: String,
  initialLesson: Lesson?,
  isAiGenerating: Boolean,
  onGenerateCheckpoints: (title: String, desc: String, duration: Int, codeSnippet: String?, onResult: (List<VideoCheckpoint>) -> Unit) -> Unit,
  onDismiss: () -> Unit,
  onSaveLesson: (Lesson) -> Unit
) {
  val isNew = initialLesson == null
  var lessonTitle by remember { mutableStateOf(initialLesson?.title ?: "") }
  var lessonDesc by remember { mutableStateOf(initialLesson?.description ?: "") }
  var durationSeconds by remember { mutableStateOf(initialLesson?.durationSeconds ?: 180) }
  var visualType by remember { mutableStateOf(initialLesson?.visualType ?: LessonVisualType.LOCAL_VIDEO_PLAYER) }
  var videoUri by remember { mutableStateOf(initialLesson?.videoUri) }
  var videoFileName by remember { mutableStateOf(initialLesson?.videoFileName) }
  var isCustomUploaded by remember { mutableStateOf(initialLesson?.isCustomUploaded ?: false) }
  var codeSnippet by remember { mutableStateOf(initialLesson?.codePreview ?: "") }

  // Checkpoints
  val checkpointsState = remember {
    mutableStateListOf<VideoCheckpoint>().apply {
      if (initialLesson != null) {
        addAll(initialLesson.checkpoints)
      }
    }
  }

  // Android File Picker for Video Upload
  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      videoUri = uri.toString()
      videoFileName = uri.lastPathSegment ?: "Uploaded_Video_${System.currentTimeMillis()}.mp4"
      isCustomUploaded = true
      visualType = LessonVisualType.LOCAL_VIDEO_PLAYER
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (isNew) "Add / Upload Video Lesson" else "Edit Video Lesson", fontWeight = FontWeight.Bold)
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().height(420.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          OutlinedTextField(
            value = lessonTitle,
            onValueChange = { lessonTitle = it },
            label = { Text("Lesson Title") },
            placeholder = { Text("e.g. State Hoisting in Practice") },
            modifier = Modifier.fillMaxWidth().testTag("lesson_title_input")
          )
        }

        item {
          OutlinedTextField(
            value = lessonDesc,
            onValueChange = { lessonDesc = it },
            label = { Text("Lesson Description") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = durationSeconds.toString(),
              onValueChange = { durationSeconds = it.toIntOrNull() ?: 60 },
              label = { Text("Duration (seconds)") },
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Video Upload Section
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text("Internal Video Asset", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Spacer(modifier = Modifier.height(6.dp))

              if (videoFileName != null) {
                Surface(
                  color = SuccessGreen.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                  ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(videoFileName!!, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                      if (videoUri != null) {
                        Text(videoUri!!, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                      }
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Button(
                onClick = { videoPickerLauncher.launch("video/*") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("pick_device_video_button")
              ) {
                Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (videoFileName != null) "Replace Video File from Device" else "Select & Upload Video File (*.mp4)")
              }
            }
          }
        }

        // Interactive Checkpoints
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Interactive Checkpoints (${checkpointsState.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            FilledTonalButton(
              onClick = {
                onGenerateCheckpoints(
                  lessonTitle.ifBlank { "Core Concepts" },
                  lessonDesc.ifBlank { "Overview" },
                  durationSeconds,
                  codeSnippet
                ) { aiCheckpoints ->
                  checkpointsState.clear()
                  checkpointsState.addAll(aiCheckpoints)
                }
              },
              shape = RoundedCornerShape(50.dp),
              modifier = Modifier.testTag("ai_generate_checkpoints_button")
            ) {
              if (isAiGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI...", fontSize = 11.sp)
              } else {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("GenAI Auto-Create", fontSize = 11.sp)
              }
            }
          }
        }

        items(checkpointsState.size) { cpIdx ->
          val cp = checkpointsState[cpIdx]
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.padding(8.dp)
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("⏱ ${cp.timestampSeconds}s: ${cp.title}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WarningAmber)
                Text(cp.question, fontSize = 11.sp, maxLines = 1)
              }
              IconButton(onClick = { checkpointsState.removeAt(cpIdx) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Checkpoint", tint = Color.Gray, modifier = Modifier.size(16.dp))
              }
            }
          }
        }

        item {
          OutlinedButton(
            onClick = {
              val newSec = ((checkpointsState.size + 1) * 30).coerceAtMost(durationSeconds - 5)
              checkpointsState.add(
                VideoCheckpoint(
                  timestampSeconds = newSec,
                  title = "Live Checkpoint at ${newSec}s",
                  question = "Confirm your comprehension of the previous section:",
                  options = listOf("Key practice is verified", "Alternative solution", "Incorrect approach", "Suboptimal implementation"),
                  correctOptionIndex = 0,
                  explanation = "Reinforces lesson material."
                )
              )
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Checkpoint Manually", fontSize = 12.sp)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val lesson = Lesson(
            id = initialLesson?.id ?: "lesson_${UUID.randomUUID().toString().take(8)}",
            courseId = courseId,
            moduleId = moduleId,
            title = lessonTitle.ifBlank { "Untitled Lesson" },
            description = lessonDesc.ifBlank { "Lesson description" },
            durationSeconds = durationSeconds.coerceAtLeast(10),
            visualType = if (isCustomUploaded) LessonVisualType.LOCAL_VIDEO_PLAYER else visualType,
            checkpoints = checkpointsState.toList(),
            codePreview = codeSnippet.ifBlank { null },
            videoUri = videoUri,
            videoFileName = videoFileName,
            isCustomUploaded = isCustomUploaded
          )
          onSaveLesson(lesson)
        },
        modifier = Modifier.testTag("save_lesson_modal_button")
      ) {
        Text("Save Lesson")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun AiQuizStudioTab(
  topic: String,
  onTopicChange: (String) -> Unit,
  level: String,
  onLevelChange: (String) -> Unit,
  count: Int,
  onCountChange: (Int) -> Unit,
  isGenerating: Boolean,
  generatedQuiz: Quiz?,
  onGenerate: () -> Unit,
  onApplyQuizToCourse: (Quiz) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gemini GenAI Quiz Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Generate high-yield, structured technical assessment questions complete with code snippets, 4-option multiple choice, answer keys, and deep explanations.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = topic,
            onValueChange = onTopicChange,
            label = { Text("Topic or Concept") },
            placeholder = { Text("e.g. Jetpack Compose SideEffects & Coroutine Scopes") },
            modifier = Modifier.fillMaxWidth().testTag("ai_quiz_topic_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text("Difficulty Level", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            listOf("Beginner", "Intermediate", "Advanced").forEach { lvl ->
              FilterChip(
                selected = level == lvl,
                onClick = { onLevelChange(lvl) },
                label = { Text(lvl) }
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text("Question Count", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            listOf(2, 4, 6, 8).forEach { c ->
              FilterChip(
                selected = count == c,
                onClick = { onCountChange(c) },
                label = { Text("$c Questions") }
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = onGenerate,
            enabled = !isGenerating && topic.isNotBlank(),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_ai_quiz_gen_button")
          ) {
            if (isGenerating) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Gemini AI is crafting questions...")
            } else {
              Icon(Icons.Filled.AutoAwesome, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Generate Quiz with Gemini AI", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    if (generatedQuiz != null) {
      item {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text("Generated AI Assessment Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${generatedQuiz.questions.size} Questions • +${generatedQuiz.xpReward} XP", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
          }
          Button(
            onClick = { onApplyQuizToCourse(generatedQuiz) },
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.testTag("apply_quiz_to_course_button")
          ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Apply to Masterclass")
          }
        }
      }

      items(generatedQuiz.questions.size) { idx ->
        val q = generatedQuiz.questions[idx]
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Q${idx + 1}: ${q.prompt}", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            if (q.codeSnippet != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = q.codeSnippet,
                  color = Color(0xFF38BDF8),
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(10.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            q.options.forEachIndexed { optIdx, opt ->
              val isCorrect = optIdx == q.correctAnswerIndex
              Surface(
                color = if (isCorrect) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(6.dp),
                border = if (isCorrect) androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen) else null,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(8.dp)
                ) {
                  Text(
                    text = "${('A' + optIdx)}. $opt",
                    fontSize = 12.sp,
                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCorrect) SuccessGreen else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("💡 Explanation: ${q.explanation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}
