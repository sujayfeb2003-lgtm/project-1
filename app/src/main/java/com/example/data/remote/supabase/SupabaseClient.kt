package com.example.data.remote.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Singleton Supabase HTTP client, analytics counter store, and session manager.
 * Reads SUPABASE_URL and SUPABASE_PUBLISHABLE_KEY from BuildConfig (Secrets Gradle Plugin).
 */
class SupabaseClient(context: Context) {

  companion object {
    private const val PREFS_NAME = "skillstream_supabase_session"
    private const val KEY_ACCESS_TOKEN = "supabase_access_token"
    private const val KEY_REFRESH_TOKEN = "supabase_refresh_token"
    private const val KEY_USER_ID = "supabase_user_id"
    private const val KEY_USER_EMAIL = "supabase_user_email"
    private const val KEY_AUTH_PROVIDER = "supabase_auth_provider"
    private const val KEY_DEVICE_INSTALL_ID = "supabase_device_install_id"

    // Analytics counters persisted per install
    private const val KEY_TOTAL_WATCH_SECONDS = "analytics_watch_time_seconds"
    private const val KEY_TOTAL_LINK_CLICKS = "analytics_link_clicks"
    private const val KEY_TOTAL_TRAFFIC_VISITS = "analytics_traffic_visits"

    // One-time Admin lock state (persisted after single admin claim)
    private const val KEY_ADMIN_CLAIMED = "admin_claimed_once"
    private const val KEY_ADMIN_ACCESS_TOKEN = "admin_access_token"
    private const val KEY_ADMIN_EMAIL = "admin_email"
    private const val KEY_ADMIN_USER_ID = "admin_user_id"

    private const val PLACEHOLDER_KEY = "MY_SUPABASE_PUBLISHABLE_KEY"
  }

  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  val supabaseUrl: String
    get() = try {
      BuildConfig.SUPABASE_URL.trim().trimEnd('/') + "/"
    } catch (e: Throwable) {
      "https://uihcxccaglixqwlwmbzw.supabase.co/"
    }

  val projectId: String
    get() = try {
      BuildConfig.SUPABASE_PROJECT_ID
    } catch (e: Throwable) {
      "uihcxccaglixqwlwmbzw"
    }

  fun getPublishableKey(): String {
    return try {
      BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()
    } catch (e: Throwable) {
      ""
    }
  }

  fun isConfigured(): Boolean {
    val key = getPublishableKey()
    return key.isNotBlank() && key != PLACEHOLDER_KEY
  }

  fun getOrCreateDeviceInstallId(): String {
    val existing = prefs.getString(KEY_DEVICE_INSTALL_ID, null)
    if (!existing.isNullOrBlank()) return existing
    val created = "device:${UUID.randomUUID()}"
    prefs.edit().putString(KEY_DEVICE_INSTALL_ID, created).apply()
    return created
  }

  // --- Analytics Local Accumulators ---

  fun addWatchSeconds(deltaSeconds: Int): Long {
    if (deltaSeconds <= 0) return getWatchSeconds()
    val updated = getWatchSeconds() + deltaSeconds
    prefs.edit().putLong(KEY_TOTAL_WATCH_SECONDS, updated).apply()
    return updated
  }

  fun getWatchSeconds(): Long = prefs.getLong(KEY_TOTAL_WATCH_SECONDS, 0L)

  fun incrementLinkClicks(delta: Int = 1): Int {
    val updated = getLinkClicks() + delta
    prefs.edit().putInt(KEY_TOTAL_LINK_CLICKS, updated).apply()
    return updated
  }

  fun getLinkClicks(): Int = prefs.getInt(KEY_TOTAL_LINK_CLICKS, 0)

  fun incrementTrafficVisits(delta: Int = 1): Int {
    val updated = getTrafficVisits() + delta
    prefs.edit().putInt(KEY_TOTAL_TRAFFIC_VISITS, updated).apply()
    return updated
  }

  fun getTrafficVisits(): Int = prefs.getInt(KEY_TOTAL_TRAFFIC_VISITS, 0)

  // --- User Session Persistence ---

  fun saveSession(
    accessToken: String?,
    refreshToken: String?,
    userId: String?,
    email: String?,
    provider: String
  ) {
    prefs.edit()
      .putString(KEY_ACCESS_TOKEN, accessToken)
      .putString(KEY_REFRESH_TOKEN, refreshToken)
      .putString(KEY_USER_ID, userId)
      .putString(KEY_USER_EMAIL, email)
      .putString(KEY_AUTH_PROVIDER, provider)
      .apply()
  }

  fun clearSession() {
    prefs.edit()
      .remove(KEY_ACCESS_TOKEN)
      .remove(KEY_REFRESH_TOKEN)
      .remove(KEY_USER_ID)
      .remove(KEY_USER_EMAIL)
      .remove(KEY_AUTH_PROVIDER)
      .apply()
  }

  fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
  fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
  fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
  fun getAuthProvider(): String = prefs.getString(KEY_AUTH_PROVIDER, "guest") ?: "guest"

  // --- Single-Admin One-Time Lock Session ---

  fun saveAdminClaimedSession(accessToken: String, userId: String, email: String) {
    prefs.edit()
      .putBoolean(KEY_ADMIN_CLAIMED, true)
      .putString(KEY_ADMIN_ACCESS_TOKEN, accessToken)
      .putString(KEY_ADMIN_USER_ID, userId)
      .putString(KEY_ADMIN_EMAIL, email)
      .apply()
  }

  fun isAdminClaimedLocally(): Boolean = prefs.getBoolean(KEY_ADMIN_CLAIMED, false)
  fun getAdminAccessToken(): String? = prefs.getString(KEY_ADMIN_ACCESS_TOKEN, null)
  fun getAdminEmail(): String? = prefs.getString(KEY_ADMIN_EMAIL, null)
  fun getAdminUserId(): String? = prefs.getString(KEY_ADMIN_USER_ID, null)

  private val moshi: Moshi by lazy {
    Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
  }

  private val authInterceptor = Interceptor { chain ->
    val original = chain.request()
    val publishableKey = getPublishableKey()
    val builder = original.newBuilder()
      .header("Accept", "application/json")
      .header("Content-Type", "application/json")

    if (publishableKey.isNotBlank()) {
      builder.header("apikey", publishableKey)
    }

    if (original.header("Authorization") == null) {
      val token = getAccessToken()
      if (!token.isNullOrBlank()) {
        builder.header("Authorization", "Bearer $token")
      } else if (publishableKey.isNotBlank() && !publishableKey.startsWith("sb_publishable_")) {
        builder.header("Authorization", "Bearer $publishableKey")
      }
    }

    chain.proceed(builder.build())
  }

  private val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .addInterceptor(authInterceptor)
      .build()
  }

  val api: SupabaseApiService by lazy {
    Retrofit.Builder()
      .baseUrl(supabaseUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(SupabaseApiService::class.java)
  }

  fun parseErrorMessage(rawJson: String?, fallback: String): String {
    if (rawJson.isNullOrBlank()) return fallback
    return try {
      val adapter = moshi.adapter(SupabaseErrorBody::class.java)
      val parsed = adapter.fromJson(rawJson)
      parsed?.errorDescription
        ?: parsed?.msg
        ?: parsed?.message
        ?: parsed?.error
        ?: fallback
    } catch (e: Exception) {
      fallback
    }
  }
}
