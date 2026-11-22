package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.data.local.entity.LessonProgressEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillStreamDao {
  // Course Enrollments
  @Query("SELECT * FROM course_enrollments ORDER BY lastActiveTimestamp DESC")
  fun getAllEnrollments(): Flow<List<CourseEnrollmentEntity>>

  @Query("SELECT * FROM course_enrollments WHERE courseId = :courseId LIMIT 1")
  fun getEnrollment(courseId: String): Flow<CourseEnrollmentEntity?>

  @Query("SELECT * FROM course_enrollments WHERE courseId = :courseId LIMIT 1")
  suspend fun getEnrollmentSync(courseId: String): CourseEnrollmentEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEnrollment(enrollment: CourseEnrollmentEntity)

  // Lesson Progress
  @Query("SELECT * FROM lesson_progress WHERE courseId = :courseId")
  fun getLessonProgressForCourse(courseId: String): Flow<List<LessonProgressEntity>>

  @Query("SELECT * FROM lesson_progress")
  fun getAllLessonProgress(): Flow<List<LessonProgressEntity>>

  @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
  fun getLessonProgress(lessonId: String): Flow<LessonProgressEntity?>

  @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
  suspend fun getLessonProgressSync(lessonId: String): LessonProgressEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLessonProgress(progress: LessonProgressEntity)

  // Quiz Attempts
  @Query("SELECT * FROM quiz_attempts ORDER BY attemptDate DESC")
  fun getAllQuizAttempts(): Flow<List<QuizAttemptEntity>>

  @Query("SELECT * FROM quiz_attempts WHERE courseId = :courseId ORDER BY attemptDate DESC")
  fun getQuizAttemptsForCourse(courseId: String): Flow<List<QuizAttemptEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQuizAttempt(attempt: QuizAttemptEntity)

  // User Stats
  @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
  fun getUserStats(): Flow<UserStatsEntity?>

  @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
  suspend fun getUserStatsSync(): UserStatsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUserStats(stats: UserStatsEntity)

  // Bookmarks
  @Query("SELECT * FROM bookmarks WHERE lessonId = :lessonId ORDER BY timestampSeconds ASC")
  fun getBookmarksForLesson(lessonId: String): Flow<List<BookmarkEntity>>

  @Query("SELECT * FROM bookmarks WHERE courseId = :courseId ORDER BY createdAt DESC")
  fun getBookmarksForCourse(courseId: String): Flow<List<BookmarkEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookmark(bookmark: BookmarkEntity)

  @Query("DELETE FROM bookmarks WHERE id = :id")
  suspend fun deleteBookmark(id: Int)

  // Stored / Custom Courses & Video Lessons
  @Query("SELECT * FROM stored_courses ORDER BY updatedAt DESC")
  fun getAllStoredCourses(): Flow<List<com.example.data.local.entity.StoredCourseEntity>>

  @Query("SELECT * FROM stored_courses WHERE id = :id LIMIT 1")
  suspend fun getStoredCourseById(id: String): com.example.data.local.entity.StoredCourseEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStoredCourse(course: com.example.data.local.entity.StoredCourseEntity)

  @Query("DELETE FROM stored_courses WHERE id = :id")
  suspend fun deleteStoredCourse(id: String)
}
