package com.example.data.local

import com.example.data.local.entity.StoredCourseEntity
import com.example.data.remote.gemini.GeminiClient
import com.example.model.Course
import com.example.model.CourseModule
import com.example.model.Lesson
import com.example.model.Quiz
import com.squareup.moshi.Types

object CourseStorageHelper {

  private val moshi = GeminiClient.moshi

  fun courseToEntity(course: Course): StoredCourseEntity {
    val listStringType = Types.newParameterizedType(List::class.java, String::class.java)
    val tagsAdapter = moshi.adapter<List<String>>(listStringType)
    val tagsJson = tagsAdapter.toJson(course.tags)

    val modulesAdapter = moshi.adapter<List<CourseModule>>(
      Types.newParameterizedType(List::class.java, CourseModule::class.java)
    )
    val modulesJson = modulesAdapter.toJson(course.modules)

    val quizAdapter = moshi.adapter(Quiz::class.java)
    val quizJson = quizAdapter.toJson(course.finalQuiz)

    return StoredCourseEntity(
      id = course.id,
      title = course.title,
      tagline = course.tagline,
      description = course.description,
      category = course.category,
      level = course.level,
      instructor = course.instructor,
      instructorRole = course.instructorRole,
      rating = course.rating,
      totalLessonsCount = course.totalLessonsCount,
      totalDurationMinutes = course.totalDurationMinutes,
      accentColorHex = course.accentColorHex,
      tagsJson = tagsJson,
      modulesJson = modulesJson,
      finalQuizJson = quizJson,
      isUserCreated = course.isUserCreated,
      isModified = course.isModified,
      updatedAt = System.currentTimeMillis()
    )
  }

  fun entityToCourse(entity: StoredCourseEntity): Course? {
    return try {
      val listStringType = Types.newParameterizedType(List::class.java, String::class.java)
      val tagsAdapter = moshi.adapter<List<String>>(listStringType)
      val tags = tagsAdapter.fromJson(entity.tagsJson) ?: emptyList()

      val modulesAdapter = moshi.adapter<List<CourseModule>>(
        Types.newParameterizedType(List::class.java, CourseModule::class.java)
      )
      val modules = modulesAdapter.fromJson(entity.modulesJson) ?: emptyList()

      val quizAdapter = moshi.adapter(Quiz::class.java)
      val quiz = quizAdapter.fromJson(entity.finalQuizJson) ?: Quiz(
        id = "quiz_${entity.id}",
        courseId = entity.id,
        title = "${entity.title} Quiz",
        description = "Assessment",
        questions = emptyList()
      )

      Course(
        id = entity.id,
        title = entity.title,
        tagline = entity.tagline,
        description = entity.description,
        category = entity.category,
        level = entity.level,
        instructor = entity.instructor,
        instructorRole = entity.instructorRole,
        rating = entity.rating,
        totalLessonsCount = entity.totalLessonsCount,
        totalDurationMinutes = entity.totalDurationMinutes,
        accentColorHex = entity.accentColorHex,
        modules = modules,
        finalQuiz = quiz,
        tags = tags,
        isUserCreated = entity.isUserCreated,
        isModified = entity.isModified
      )
    } catch (e: Exception) {
      null
    }
  }
}
