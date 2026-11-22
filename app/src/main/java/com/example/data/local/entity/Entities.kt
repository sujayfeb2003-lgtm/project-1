package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_enrollments")
data class CourseEnrollmentEntity(
  @PrimaryKey val courseId: String,
  val enrolledAt: Long = System.currentTimeMillis(),
  val lastPlayedLessonId: String? = null,
  val isCompleted: Boolean = false,
  val progressPercent: Float = 0f,
  val totalSecondsWatched: Int = 0,
  val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
  @PrimaryKey val lessonId: String,
  val courseId: String,
  val watchedSeconds: Int = 0,
  val totalSeconds: Int = 0,
  val isCompleted: Boolean = false,
  val checkpointsAnswered: String = "", // Comma-separated checkpoint seconds
  val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val quizId: String,
  val courseId: String,
  val score: Int,
  val totalQuestions: Int,
  val percentage: Int,
  val passed: Boolean,
  val timeSpentSeconds: Int,
  val attemptDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
  @PrimaryKey val id: Int = 1,
  val totalXp: Int = 240,
  val streakDays: Int = 3,
  val lastActiveDate: String = "",
  val totalMinutesStudied: Int = 45,
  val completedLessonsCount: Int = 2,
  val passedQuizzesCount: Int = 1,
  val unlockedBadges: String = "badge_first_lesson,badge_fast_learner"
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val courseId: String,
  val lessonId: String,
  val timestampSeconds: Int,
  val noteText: String,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stored_courses")
data class StoredCourseEntity(
  @PrimaryKey val id: String,
  val title: String,
  val tagline: String,
  val description: String,
  val category: String,
  val level: String,
  val instructor: String,
  val instructorRole: String,
  val rating: Float,
  val totalLessonsCount: Int,
  val totalDurationMinutes: Int,
  val accentColorHex: Long,
  val tagsJson: String,
  val modulesJson: String,
  val finalQuizJson: String,
  val isUserCreated: Boolean = true,
  val isModified: Boolean = false,
  val updatedAt: Long = System.currentTimeMillis()
)
