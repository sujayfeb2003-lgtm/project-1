package com.example.model

enum class LessonVisualType {
  CODE_EDITOR,
  ARCHITECTURE_CANVAS,
  TERMINAL_EXECUTION,
  DESIGN_SYSTEM_PREVIEW,
  DATA_FLOW_VIZ,
  LOCAL_VIDEO_PLAYER
}

data class VideoCheckpoint(
  val timestampSeconds: Int,
  val title: String,
  val question: String,
  val options: List<String>,
  val correctOptionIndex: Int,
  val explanation: String
)

data class TranscriptSnippet(
  val startSecond: Int,
  val endSecond: Int,
  val speaker: String,
  val text: String,
  val codeSnippet: String? = null
)

data class Lesson(
  val id: String,
  val courseId: String,
  val moduleId: String,
  val title: String,
  val description: String,
  val durationSeconds: Int,
  val visualType: LessonVisualType,
  val checkpoints: List<VideoCheckpoint> = emptyList(),
  val transcripts: List<TranscriptSnippet> = emptyList(),
  val takeaways: List<String> = emptyList(),
  val codePreview: String? = null,
  val videoUri: String? = null,
  val videoFileName: String? = null,
  val isCustomUploaded: Boolean = false
)

data class CourseModule(
  val id: String,
  val courseId: String,
  val title: String,
  val description: String,
  val order: Int,
  val lessons: List<Lesson>
)

data class QuizQuestion(
  val id: String,
  val prompt: String,
  val codeSnippet: String? = null,
  val options: List<String>,
  val correctAnswerIndex: Int,
  val explanation: String,
  val category: String = "Core Concept"
)

data class Quiz(
  val id: String,
  val courseId: String,
  val title: String,
  val description: String,
  val passingPercentage: Int = 70,
  val xpReward: Int = 150,
  val timeLimitMinutes: Int = 5,
  val questions: List<QuizQuestion>
)

data class Course(
  val id: String,
  val title: String,
  val tagline: String,
  val description: String,
  val category: String,
  val level: String, // "Beginner", "Intermediate", "Advanced"
  val instructor: String,
  val instructorRole: String,
  val rating: Float,
  val totalLessonsCount: Int,
  val totalDurationMinutes: Int,
  val accentColorHex: Long,
  val modules: List<CourseModule>,
  val finalQuiz: Quiz,
  val tags: List<String>,
  val isUserCreated: Boolean = false,
  val isModified: Boolean = false
)

data class UserBadge(
  val id: String,
  val title: String,
  val description: String,
  val iconName: String,
  val unlockedAt: Long? = null,
  val isUnlocked: Boolean = false
)
