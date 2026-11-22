package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SkillStreamDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.CourseEnrollmentEntity
import com.example.data.local.entity.LessonProgressEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.StoredCourseEntity
import com.example.data.local.entity.UserStatsEntity

@Database(
  entities = [
    CourseEnrollmentEntity::class,
    LessonProgressEntity::class,
    QuizAttemptEntity::class,
    UserStatsEntity::class,
    BookmarkEntity::class,
    StoredCourseEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun skillStreamDao(): SkillStreamDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "skillstream_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
