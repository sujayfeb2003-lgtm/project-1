package com.example.data.remote.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class SupabaseSyncStatus {
  object NotConfigured : SupabaseSyncStatus()
  object Syncing : SupabaseSyncStatus()
  data class Connected(
    val lastSyncedIso: String,
    val userKey: String,
    val email: String?
  ) : SupabaseSyncStatus()
  data class Error(val message: String) : SupabaseSyncStatus()
}

// =============================================================================
// Supabase GoTrue Auth DTOs
// =============================================================================

@JsonClass(generateAdapter = true)
data class SupabaseEmailPasswordRequest(
  val email: String,
  val password: String
)

@JsonClass(generateAdapter = true)
data class SupabaseOtpRequest(
  val email: String,
  @Json(name = "create_user") val createUser: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SupabaseRecoverRequest(
  val email: String
)

@JsonClass(generateAdapter = true)
data class SupabaseUserDto(
  val id: String = "",
  val email: String? = null,
  @Json(name = "created_at") val createdAt: String? = null,
  @Json(name = "confirmed_at") val confirmedAt: String? = null,
  @Json(name = "last_sign_in_at") val lastSignInAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
  @Json(name = "access_token") val accessToken: String? = null,
  @Json(name = "token_type") val tokenType: String? = null,
  @Json(name = "expires_in") val expiresIn: Long? = null,
  @Json(name = "refresh_token") val refreshToken: String? = null,
  val user: SupabaseUserDto? = null,
  val id: String? = null,
  val email: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseErrorBody(
  val code: Int? = null,
  @Json(name = "error_code") val errorCode: String? = null,
  val msg: String? = null,
  val message: String? = null,
  @Json(name = "error_description") val errorDescription: String? = null,
  val error: String? = null,
  val hint: String? = null
)

// =============================================================================
// Supabase PostgREST Table DTOs
// =============================================================================

@JsonClass(generateAdapter = true)
data class SupabaseAppUserUpsert(
  @Json(name = "user_key") val userKey: String,
  @Json(name = "auth_user_id") val authUserId: String? = null,
  val email: String? = null,
  @Json(name = "display_name") val displayName: String? = null,
  @Json(name = "auth_provider") val authProvider: String,
  @Json(name = "app_platform") val appPlatform: String = "android",
  @Json(name = "device_model") val deviceModel: String? = null,
  @Json(name = "xp_points") val xpPoints: Int = 0,
  @Json(name = "courses_enrolled") val coursesEnrolled: Int = 0,
  @Json(name = "lessons_completed") val lessonsCompleted: Int = 0,
  @Json(name = "quizzes_passed") val quizzesPassed: Int = 0,
  @Json(name = "watch_time_seconds") val watchTimeSeconds: Long = 0L,
  @Json(name = "link_clicks") val linkClicks: Int = 0,
  @Json(name = "traffic_visits") val trafficVisits: Int = 1,
  @Json(name = "last_active_at") val lastActiveAt: String
)

@JsonClass(generateAdapter = true)
data class SupabaseAppUserRecord(
  val id: String = "",
  @Json(name = "user_key") val userKey: String = "",
  @Json(name = "auth_user_id") val authUserId: String? = null,
  val email: String? = null,
  @Json(name = "display_name") val displayName: String? = null,
  @Json(name = "auth_provider") val authProvider: String = "guest",
  @Json(name = "app_platform") val appPlatform: String = "android",
  @Json(name = "device_model") val deviceModel: String? = null,
  @Json(name = "xp_points") val xpPoints: Int = 0,
  @Json(name = "courses_enrolled") val coursesEnrolled: Int = 0,
  @Json(name = "lessons_completed") val lessonsCompleted: Int = 0,
  @Json(name = "quizzes_passed") val quizzesPassed: Int = 0,
  @Json(name = "watch_time_seconds") val watchTimeSeconds: Long = 0L,
  @Json(name = "link_clicks") val linkClicks: Int = 0,
  @Json(name = "traffic_visits") val trafficVisits: Int = 1,
  @Json(name = "last_active_at") val lastActiveAt: String? = null,
  @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseActivityLogInsert(
  @Json(name = "user_key") val userKey: String,
  @Json(name = "auth_user_id") val authUserId: String? = null,
  val email: String? = null,
  @Json(name = "event_type") val eventType: String,
  @Json(name = "event_detail") val eventDetail: String? = null,
  @Json(name = "watch_seconds_delta") val watchSecondsDelta: Int = 0,
  val platform: String = "android"
)

// =============================================================================
// Single-Admin RPC DTOs
// =============================================================================

@JsonClass(generateAdapter = true)
data class AdminDeviceRpcRequest(
  @Json(name = "p_device_id") val deviceId: String
)

@JsonClass(generateAdapter = true)
data class AdminClaimVerifyResponse(
  val allowed: Boolean = false,
  val status: String? = null,
  @Json(name = "admin_email") val adminEmail: String? = null,
  val message: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminActivityEventDto(
  val id: String = "",
  @Json(name = "user_key") val userKey: String = "",
  val email: String? = null,
  @Json(name = "event_type") val eventType: String = "",
  @Json(name = "event_detail") val eventDetail: String? = null,
  @Json(name = "watch_seconds_delta") val watchSecondsDelta: Int = 0,
  val platform: String = "android",
  @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminDashboardMetricsResponse(
  @Json(name = "total_visitors") val totalVisitors: Long = 0L,
  @Json(name = "authenticated_users") val authenticatedUsers: Long = 0L,
  @Json(name = "total_watch_time_seconds") val totalWatchTimeSeconds: Long = 0L,
  @Json(name = "total_link_clicks") val totalLinkClicks: Long = 0L,
  @Json(name = "total_traffic") val totalTraffic: Long = 0L,
  val users: List<SupabaseAppUserRecord> = emptyList(),
  @Json(name = "recent_events") val recentEvents: List<AdminActivityEventDto> = emptyList()
)
