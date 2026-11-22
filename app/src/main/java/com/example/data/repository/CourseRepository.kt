package com.example.data.repository

import com.example.data.SampleCoursesData
import com.example.data.local.CourseStorageHelper
import com.example.data.local.dao.SkillStreamDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.data.local.entity.LessonProgressEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.UserStatsEntity
import com.example.model.Course
import com.example.model.CourseModule
import com.example.model.Lesson
import com.example.model.Quiz
import com.example.model.UserBadge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CourseRepository(private val dao: SkillStreamDao) {

  val defaultCourses: List<Course> = SampleCoursesData.courses

  val storedCoursesFlow: Flow<List<Course>> = dao.getAllStoredCourses().map { list ->
    list.mapNotNull { CourseStorageHelper.entityToCourse(it) }
  }

  // Merges base courses with custom and modified stored courses
  val allCoursesFlow: Flow<List<Course>> = storedCoursesFlow.map { storedList ->
    val storedMap = storedList.associateBy { it.id }
    val merged = mutableListOf<Course>()

    // Add base courses (overridden if modified in DB)
    defaultCourses.forEach { defaultCourse ->
      if (storedMap.containsKey(defaultCourse.id)) {
        merged.add(storedMap[defaultCourse.id]!!)
      } else {
        merged.add(defaultCourse)
      }
    }

    // Add newly created user courses
    storedList.filter { !defaultCourses.any { def -> def.id == it.id } }.forEach { userCourse ->
      merged.add(0, userCourse)
    }

    merged
  }

  var currentCoursesSnapshot: List<Course> = SampleCoursesData.courses

  val enrollments: Flow<List<CourseEnrollmentEntity>> = dao.getAllEnrollments()

  val allLessonProgress: Flow<List<LessonProgressEntity>> = dao.getAllLessonProgress()

  val allQuizAttempts: Flow<List<QuizAttemptEntity>> = dao.getAllQuizAttempts()

  val userStats: Flow<UserStatsEntity> = dao.getUserStats().map { it ?: UserStatsEntity() }

  suspend fun saveCourse(course: Course) {
    val entity = CourseStorageHelper.courseToEntity(course)
    dao.insertStoredCourse(entity)
  }

  suspend fun deleteCourse(courseId: String) {
    dao.deleteStoredCourse(courseId)
  }

  suspend fun resetCourseToDefault(courseId: String) {
    dao.deleteStoredCourse(courseId)
  }

  fun getCourseById(courseId: String, currentList: List<Course> = currentCoursesSnapshot): Course? {
    return currentList.find { it.id == courseId } ?: defaultCourses.find { it.id == courseId }
  }

  fun getLessonById(courseId: String, lessonId: String, currentList: List<Course> = currentCoursesSnapshot): Pair<Course, Lesson>? {
    val course = getCourseById(courseId, currentList) ?: return null
    for (module in course.modules) {
      val lesson = module.lessons.find { it.id == lessonId }
      if (lesson != null) return Pair(course, lesson)
    }
    return null
  }

  fun getLessonProgress(lessonId: String): Flow<LessonProgressEntity?> {
    return dao.getLessonProgress(lessonId)
  }

  fun getBookmarksForLesson(lessonId: String): Flow<List<BookmarkEntity>> {
    return dao.getBookmarksForLesson(lessonId)
  }

  fun getBookmarksForCourse(courseId: String): Flow<List<BookmarkEntity>> {
    return dao.getBookmarksForCourse(courseId)
  }

  suspend fun enrollInCourse(courseId: String, initialLessonId: String? = null) {
    val existing = dao.getEnrollmentSync(courseId)
    if (existing == null) {
      val course = getCourseById(courseId)
      val firstLessonId = initialLessonId ?: course?.modules?.firstOrNull()?.lessons?.firstOrNull()?.id
      dao.insertEnrollment(
        CourseEnrollmentEntity(
          courseId = courseId,
          lastPlayedLessonId = firstLessonId,
          progressPercent = 0f,
          lastActiveTimestamp = System.currentTimeMillis()
        )
      )
    }
  }

  suspend fun updateLessonProgress(
    courseId: String,
    lessonId: String,
    watchedSeconds: Int,
    totalSeconds: Int,
    isCompleted: Boolean,
    answeredCheckpointTimestamp: Int? = null
  ) {
    val current = dao.getLessonProgressSync(lessonId)
    val existingCheckpoints = current?.checkpointsAnswered?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
    if (answeredCheckpointTimestamp != null) {
      existingCheckpoints.add(answeredCheckpointTimestamp.toString())
    }

    val updatedProgress = LessonProgressEntity(
      lessonId = lessonId,
      courseId = courseId,
      watchedSeconds = maxOf(watchedSeconds, current?.watchedSeconds ?: 0),
      totalSeconds = totalSeconds,
      isCompleted = (current?.isCompleted == true) || isCompleted,
      checkpointsAnswered = existingCheckpoints.joinToString(","),
      lastUpdated = System.currentTimeMillis()
    )
    dao.insertLessonProgress(updatedProgress)

    // Recalculate Course Overall Progress
    val course = getCourseById(courseId)
    if (course != null) {
      val totalLessons = course.totalLessonsCount
      var completedCount = 0
      var totalWatchedSecs = 0
      for (module in course.modules) {
        for (l in module.lessons) {
          val lp = dao.getLessonProgressSync(l.id)
          if (lp?.isCompleted == true) completedCount++
          totalWatchedSecs += (lp?.watchedSeconds ?: 0)
        }
      }
      val percent = if (totalLessons > 0) (completedCount.toFloat() / totalLessons.toFloat()) * 100f else 0f
      val isAllCompleted = completedCount >= totalLessons

      val enrollment = dao.getEnrollmentSync(courseId) ?: CourseEnrollmentEntity(courseId = courseId)
      dao.insertEnrollment(
        enrollment.copy(
          lastPlayedLessonId = lessonId,
          progressPercent = percent,
          totalSecondsWatched = totalWatchedSecs,
          isCompleted = isAllCompleted,
          lastActiveTimestamp = System.currentTimeMillis()
        )
      )

      // Update User Stats
      val stats = dao.getUserStatsSync() ?: UserStatsEntity()
      val newMinutes = (stats.totalMinutesStudied + (watchedSeconds / 60)).coerceAtLeast(stats.totalMinutesStudied)
      val newXp = stats.totalXp + if (isCompleted && current?.isCompleted != true) 50 else 0
      dao.insertUserStats(
        stats.copy(
          totalMinutesStudied = newMinutes,
          totalXp = newXp,
          completedLessonsCount = if (isCompleted && current?.isCompleted != true) stats.completedLessonsCount + 1 else stats.completedLessonsCount
        )
      )
    }
  }

  suspend fun recordQuizAttempt(
    quizId: String,
    courseId: String,
    score: Int,
    totalQuestions: Int,
    timeSpentSeconds: Int
  ): QuizAttemptEntity {
    val percentage = if (totalQuestions > 0) ((score.toFloat() / totalQuestions.toFloat()) * 100).toInt() else 0
    val passed = percentage >= 70
    val attempt = QuizAttemptEntity(
      quizId = quizId,
      courseId = courseId,
      score = score,
      totalQuestions = totalQuestions,
      percentage = percentage,
      passed = passed,
      timeSpentSeconds = timeSpentSeconds,
      attemptDate = System.currentTimeMillis()
    )
    dao.insertQuizAttempt(attempt)

    // Update XP and Stats
    val stats = dao.getUserStatsSync() ?: UserStatsEntity()
    val earnedXp = if (passed) 150 else 30
    var badges = stats.unlockedBadges.split(",").filter { it.isNotBlank() }.toMutableSet()
    if (percentage == 100) badges.add("badge_quiz_master")
    if (passed) badges.add("badge_graduate")

    dao.insertUserStats(
      stats.copy(
        totalXp = stats.totalXp + earnedXp,
        passedQuizzesCount = if (passed) stats.passedQuizzesCount + 1 else stats.passedQuizzesCount,
        unlockedBadges = badges.joinToString(",")
      )
    )
    return attempt
  }

  suspend fun addBookmark(courseId: String, lessonId: String, timestampSeconds: Int, noteText: String) {
    dao.insertBookmark(
      BookmarkEntity(
        courseId = courseId,
        lessonId = lessonId,
        timestampSeconds = timestampSeconds,
        noteText = noteText,
        createdAt = System.currentTimeMillis()
      )
    )
  }

  suspend fun removeBookmark(bookmarkId: Int) {
    dao.deleteBookmark(bookmarkId)
  }

  fun getBadges(unlockedIds: Set<String>): List<UserBadge> {
    return SampleCoursesData.badges.map { badge ->
      badge.copy(isUnlocked = unlockedIds.contains(badge.id))
    }
  }
}
