package com.example.data.remote.supabase

import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Repository coordinating Supabase Authentication, user analytics tracking,
 * and Single-Admin one-time login verification + dashboard metrics.
 */
class SupabaseRepository(private val client: SupabaseClient) {

  companion object {
    private const val TAG = "SupabaseRepository"
  }

  private val _syncStatus = MutableStateFlow<SupabaseSyncStatus>(
    if (client.isConfigured()) SupabaseSyncStatus.Syncing else SupabaseSyncStatus.NotConfigured
  )
  val syncStatus: StateFlow<SupabaseSyncStatus> = _syncStatus.asStateFlow()

  fun isConfigured(): Boolean = client.isConfigured()
  fun currentSessionEmail(): String? = client.getUserEmail()
  fun currentSessionUserId(): String? = client.getUserId()

  fun isAdminUnlockedOnThisDevice(): Boolean =
    client.isAdminClaimedLocally() && !client.getAdminAccessToken().isNullOrBlank()

  fun getClaimedAdminEmail(): String? = client.getAdminEmail()

  fun recordLocalWatchSeconds(deltaSeconds: Int): Long = client.addWatchSeconds(deltaSeconds)
  fun recordLocalLinkClick(): Int = client.incrementLinkClicks(1)
  fun recordLocalTrafficVisit(): Int = client.incrementTrafficVisits(1)

  fun getLocalWatchSeconds(): Long = client.getWatchSeconds()
  fun getLocalLinkClicks(): Int = client.getLinkClicks()
  fun getLocalTrafficVisits(): Int = client.getTrafficVisits()

  suspend fun signInWithEmailAndPassword(email: String, password: String): Result<SupabaseUserDto> {
    if (!client.isConfigured()) {
      return Result.failure(IllegalStateException("Supabase Publishable Key is not configured in AI Studio Secrets."))
    }
    return try {
      val response = client.api.signInWithPassword(
        request = SupabaseEmailPasswordRequest(email = email.trim(), password = password)
      )
      if (response.isSuccessful) {
        val body = response.body()
        val user = body?.user
        val userId = user?.id ?: body?.id ?: ""
        val userEmail = user?.email ?: body?.email ?: email.trim()
        if (body?.accessToken != null && userId.isNotBlank()) {
          client.saveSession(
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            userId = userId,
            email = userEmail,
            provider = "supabase_password"
          )
          Result.success(user ?: SupabaseUserDto(id = userId, email = userEmail))
        } else {
          Result.failure(IllegalStateException("Invalid auth response from Supabase."))
        }
      } else {
        val msg = client.parseErrorMessage(
          response.errorBody()?.string(),
          "Sign-in failed (HTTP ${response.code()})."
        )
        Result.failure(IllegalStateException(msg))
      }
    } catch (e: Exception) {
      Log.e(TAG, "Supabase signIn failed: ${e.localizedMessage}")
      Result.failure(e)
    }
  }

  suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<SupabaseUserDto> {
    if (!client.isConfigured()) {
      return Result.failure(IllegalStateException("Supabase Publishable Key is not configured in AI Studio Secrets."))
    }
    return try {
      val response = client.api.signUp(
        request = SupabaseEmailPasswordRequest(email = email.trim(), password = password)
      )
      if (response.isSuccessful) {
        val body = response.body()
        val user = body?.user
        val userId = user?.id ?: body?.id ?: ""
        val userEmail = user?.email ?: body?.email ?: email.trim()

        if (body?.accessToken != null && userId.isNotBlank()) {
          client.saveSession(
            accessToken = body.accessToken,
            refreshToken = body.refreshToken,
            userId = userId,
            email = userEmail,
            provider = "supabase_password"
          )
          Result.success(user ?: SupabaseUserDto(id = userId, email = userEmail))
        } else if (userId.isNotBlank()) {
          Result.failure(
            IllegalStateException(
              "Account created for $userEmail. Please confirm your email via the link sent by Supabase before signing in."
            )
          )
        } else {
          Result.failure(IllegalStateException("Unexpected response while creating Supabase account."))
        }
      } else {
        val msg = client.parseErrorMessage(
          response.errorBody()?.string(),
          "Account creation failed (HTTP ${response.code()})."
        )
        Result.failure(IllegalStateException(msg))
      }
    } catch (e: Exception) {
      Log.e(TAG, "Supabase signUp failed: ${e.localizedMessage}")
      Result.failure(e)
    }
  }

  suspend fun sendMagicLink(email: String, redirectUrl: String? = null): Result<String> {
    if (!client.isConfigured()) {
      return Result.failure(IllegalStateException("Supabase Publishable Key is not configured in AI Studio Secrets."))
    }
    return try {
      val response = client.api.sendMagicLinkOtp(
        redirectTo = redirectUrl,
        request = SupabaseOtpRequest(email = email.trim(), createUser = true)
      )
      if (response.isSuccessful) {
        Result.success("Magic sign-in link sent to ${email.trim()}. Check your inbox.")
      } else {
        val msg = client.parseErrorMessage(
          response.errorBody()?.string(),
          "Failed to send magic link (HTTP ${response.code()})."
        )
        Result.failure(IllegalStateException(msg))
      }
    } catch (e: Exception) {
      Log.e(TAG, "Supabase magic link failed: ${e.localizedMessage}")
      Result.failure(e)
    }
  }

  suspend fun sendPasswordReset(email: String): Result<String> {
    if (!client.isConfigured()) {
      return Result.failure(IllegalStateException("Supabase Publishable Key is not configured in AI Studio Secrets."))
    }
    return try {
      val response = client.api.sendPasswordReset(
        request = SupabaseRecoverRequest(email = email.trim())
      )
      if (response.isSuccessful) {
        Result.success("Password reset instructions sent to ${email.trim()}.")
      } else {
        val msg = client.parseErrorMessage(
          response.errorBody()?.string(),
          "Failed to send password reset email (HTTP ${response.code()})."
        )
        Result.failure(IllegalStateException(msg))
      }
    } catch (e: Exception) {
      Log.e(TAG, "Supabase password reset failed: ${e.localizedMessage}")
      Result.failure(e)
    }
  }

  suspend fun handleMagicLinkCallback(urlString: String): Result<SupabaseUserDto> {
    if (!client.isConfigured()) {
      return Result.failure(IllegalStateException("Supabase Publishable Key is not configured."))
    }
    return try {
      val normalized = urlString.replace("#", "?")
      val uri = Uri.parse(normalized)
      val accessToken = uri.getQueryParameter("access_token")
      val refreshToken = uri.getQueryParameter("refresh_token")

      if (accessToken.isNullOrBlank()) {
        return Result.failure(IllegalArgumentException("No access token found in magic link URL."))
      }

      val userResponse = client.api.getCurrentUser("Bearer $accessToken")
      if (userResponse.isSuccessful && userResponse.body() != null) {
        val user = userResponse.body()!!
        client.saveSession(
          accessToken = accessToken,
          refreshToken = refreshToken,
          userId = user.id,
          email = user.email,
          provider = "supabase_magic_link"
        )
        Result.success(user)
      } else {
        val msg = client.parseErrorMessage(
          userResponse.errorBody()?.string(),
          "Failed to verify Supabase magic link."
        )
        Result.failure(IllegalStateException(msg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signOut() {
    val token = client.getAccessToken()
    if (client.isConfigured() && !token.isNullOrBlank()) {
      try {
        client.api.signOut("Bearer $token")
      } catch (e: Exception) {
        Log.w(TAG, "Supabase signOut request error: ${e.localizedMessage}")
      }
    }
    client.clearSession()
  }

  // ===========================================================================
  // Single-Admin One-Time Login & Live Analytics RPCs
  // ===========================================================================

  /**
   * Performs a one-time Admin login (or first-time Admin account claim) via Supabase Auth
   * and permanently locks admin_config to this single account & device via PostgreSQL RPC.
   */
  suspend fun claimOrLoginSingleAdmin(
    email: String,
    password: String,
    createAccountFirst: Boolean
  ): Result<AdminClaimVerifyResponse> {
    if (!client.isConfigured()) {
      return Result.failure(IllegalStateException("Set SUPABASE_PUBLISHABLE_KEY in AI Studio Secrets first."))
    }
    if (client.isAdminClaimedLocally()) {
      return Result.failure(
        IllegalStateException("Admin login was already claimed once on this device (${client.getAdminEmail()}).")
      )
    }

    val trimmedEmail = email.trim()
    if (trimmedEmail.isBlank() || password.length < 6) {
      return Result.failure(IllegalArgumentException("Enter a valid admin email and password (min 6 characters)."))
    }

    return try {
      val authResponse = if (createAccountFirst) {
        client.api.signUp(SupabaseEmailPasswordRequest(trimmedEmail, password))
      } else {
        client.api.signInWithPassword(request = SupabaseEmailPasswordRequest(trimmedEmail, password))
      }

      if (!authResponse.isSuccessful) {
        val errMsg = client.parseErrorMessage(
          authResponse.errorBody()?.string(),
          "Admin authentication failed (HTTP ${authResponse.code()})."
        )
        return Result.failure(IllegalStateException(errMsg))
      }

      val authBody = authResponse.body()
      val accessToken = authBody?.accessToken
      val userId = authBody?.user?.id ?: authBody?.id
      val resolvedEmail = authBody?.user?.email ?: authBody?.email ?: trimmedEmail

      if (accessToken.isNullOrBlank() || userId.isNullOrBlank()) {
        return Result.failure(
          IllegalStateException("Admin sign-in requires an active session token. Confirm your email in Supabase if confirmation is enabled.")
        )
      }

      // Verify / Claim one-time admin lock in PostgreSQL
      val deviceId = client.getOrCreateDeviceInstallId()
      val rpcResponse = client.api.adminClaimAndVerify(
        bearerToken = "Bearer $accessToken",
        body = AdminDeviceRpcRequest(deviceId = deviceId)
      )

      if (!rpcResponse.isSuccessful || rpcResponse.body() == null) {
        val errMsg = client.parseErrorMessage(
          rpcResponse.errorBody()?.string(),
          "Admin verification failed (HTTP ${rpcResponse.code()}). Ensure updated supabase_schema.sql has been run."
        )
        return Result.failure(IllegalStateException(errMsg))
      }

      val claimResult = rpcResponse.body()!!
      if (!claimResult.allowed) {
        return Result.failure(
          IllegalStateException(claimResult.message ?: "Only one admin login is accepted and it has already been claimed.")
        )
      }

      // Persist one-time admin session + active user session
      client.saveAdminClaimedSession(accessToken, userId, claimResult.adminEmail ?: resolvedEmail)
      client.saveSession(accessToken, authBody.refreshToken, userId, resolvedEmail, "supabase_admin")
      Result.success(claimResult)
    } catch (e: Exception) {
      Log.e(TAG, "Admin claim/login error: ${e.localizedMessage}", e)
      Result.failure(e)
    }
  }

  /**
   * Fetches aggregated watchtime by user, total visitors, total link clicks, and total traffic
   * from Supabase RPC get_admin_dashboard_metrics.
   */
  suspend fun fetchAdminDashboardMetrics(): Result<AdminDashboardMetricsResponse> {
    if (!client.isConfigured()) {
      return Result.failure(IllegalStateException("Supabase Publishable Key is not configured."))
    }
    val adminToken = client.getAdminAccessToken()
      ?: return Result.failure(IllegalStateException("Admin one-time login is required."))

    return try {
      val deviceId = client.getOrCreateDeviceInstallId()
      val response = client.api.getAdminDashboardMetrics(
        bearerToken = "Bearer $adminToken",
        body = AdminDeviceRpcRequest(deviceId = deviceId)
      )
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        val msg = client.parseErrorMessage(
          response.errorBody()?.string(),
          "Failed to load admin metrics (HTTP ${response.code()})."
        )
        Result.failure(IllegalStateException(msg))
      }
    } catch (e: Exception) {
      Log.e(TAG, "fetchAdminDashboardMetrics failed: ${e.localizedMessage}", e)
      Result.failure(e)
    }
  }

  // ===========================================================================
  // User Usage, Watchtime, Link Click & Traffic Sync
  // ===========================================================================

  suspend fun syncUserUsage(
    stats: UserStatsEntity,
    enrolledCount: Int,
    lessonsCompletedCount: Int,
    quizzesPassedCount: Int,
    eventType: String,
    eventDetail: String? = null,
    watchSecondsDelta: Int = 0,
    overrideEmail: String? = null,
    overrideProvider: String? = null
  ): Result<SupabaseAppUserRecord?> {
    if (!client.isConfigured()) {
      _syncStatus.value = SupabaseSyncStatus.NotConfigured
      return Result.failure(IllegalStateException("Set SUPABASE_PUBLISHABLE_KEY in AI Studio Secrets."))
    }

    _syncStatus.value = SupabaseSyncStatus.Syncing

    val supabaseUserId = client.getUserId()
    val hasSupabaseSession = !client.getAccessToken().isNullOrBlank() && !supabaseUserId.isNullOrBlank()

    val userKey = if (hasSupabaseSession) supabaseUserId!! else client.getOrCreateDeviceInstallId()
    val resolvedAuthUserId = if (hasSupabaseSession) supabaseUserId else null
    val resolvedEmail = if (hasSupabaseSession) (overrideEmail ?: client.getUserEmail()) else null
    val resolvedProvider = if (hasSupabaseSession) {
      overrideProvider ?: client.getAuthProvider()
    } else {
      "guest_device"
    }
    val displayName = resolvedEmail?.substringBefore("@") ?: "Guest (${Build.MODEL})"
    val nowIso = currentIsoTimestamp()

    val payload = SupabaseAppUserUpsert(
      userKey = userKey,
      authUserId = resolvedAuthUserId,
      email = resolvedEmail,
      displayName = displayName,
      authProvider = resolvedProvider,
      appPlatform = "android",
      deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
      xpPoints = stats.totalXp,
      coursesEnrolled = enrolledCount,
      lessonsCompleted = lessonsCompletedCount,
      quizzesPassed = quizzesPassedCount,
      watchTimeSeconds = client.getWatchSeconds(),
      linkClicks = client.getLinkClicks(),
      trafficVisits = client.getTrafficVisits().coerceAtLeast(1),
      lastActiveAt = nowIso
    )

    return try {
      val upsertResponse = client.api.upsertAppUser(body = payload)
      if (!upsertResponse.isSuccessful) {
        val errorMsg = client.parseErrorMessage(
          upsertResponse.errorBody()?.string(),
          "Supabase sync failed (HTTP ${upsertResponse.code()}). Ensure supabase_schema.sql has been run."
        )
        _syncStatus.value = SupabaseSyncStatus.Error(errorMsg)
        return Result.failure(IllegalStateException(errorMsg))
      }

      try {
        client.api.insertActivityLog(
          body = SupabaseActivityLogInsert(
            userKey = userKey,
            authUserId = resolvedAuthUserId,
            email = resolvedEmail,
            eventType = eventType,
            eventDetail = eventDetail,
            watchSecondsDelta = watchSecondsDelta,
            platform = "android"
          )
        )
      } catch (logErr: Exception) {
        Log.w(TAG, "Activity log insert skipped: ${logErr.localizedMessage}")
      }

      val savedRecord = upsertResponse.body()?.firstOrNull()
      _syncStatus.value = SupabaseSyncStatus.Connected(
        lastSyncedIso = nowIso,
        userKey = userKey,
        email = resolvedEmail
      )
      Result.success(savedRecord)
    } catch (e: Exception) {
      val msg = e.localizedMessage ?: "Network error connecting to Supabase."
      Log.e(TAG, "Supabase sync error: $msg", e)
      _syncStatus.value = SupabaseSyncStatus.Error(msg)
      Result.failure(e)
    }
  }

  private fun currentIsoTimestamp(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
  }
}
