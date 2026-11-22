package com.example.data.remote.supabase

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit service for Supabase GoTrue Auth and PostgREST endpoints.
 */
interface SupabaseApiService {

  // --- Supabase GoTrue Authentication ---

  @POST("auth/v1/signup")
  suspend fun signUp(
    @Body request: SupabaseEmailPasswordRequest
  ): Response<SupabaseAuthResponse>

  @POST("auth/v1/token")
  suspend fun signInWithPassword(
    @Query("grant_type") grantType: String = "password",
    @Body request: SupabaseEmailPasswordRequest
  ): Response<SupabaseAuthResponse>

  @POST("auth/v1/otp")
  suspend fun sendMagicLinkOtp(
    @Query("redirect_to") redirectTo: String? = null,
    @Body request: SupabaseOtpRequest
  ): Response<Unit>

  @POST("auth/v1/recover")
  suspend fun sendPasswordReset(
    @Body request: SupabaseRecoverRequest
  ): Response<Unit>

  @GET("auth/v1/user")
  suspend fun getCurrentUser(
    @Header("Authorization") bearerToken: String
  ): Response<SupabaseUserDto>

  @POST("auth/v1/logout")
  suspend fun signOut(
    @Header("Authorization") bearerToken: String
  ): Response<Unit>

  // --- Supabase PostgREST Table Operations ---

  @POST("rest/v1/app_users")
  suspend fun upsertAppUser(
    @Query("on_conflict") onConflict: String = "user_key",
    @Header("Prefer") prefer: String = "resolution=merge-duplicates,return=representation",
    @Body body: SupabaseAppUserUpsert
  ): Response<List<SupabaseAppUserRecord>>

  @GET("rest/v1/app_users")
  suspend fun getOwnAppUser(
    @Query("user_key") userKeyFilter: String,
    @Query("select") select: String = "*"
  ): Response<List<SupabaseAppUserRecord>>

  @POST("rest/v1/user_activity_logs")
  suspend fun insertActivityLog(
    @Header("Prefer") prefer: String = "return=minimal",
    @Body body: SupabaseActivityLogInsert
  ): Response<Unit>

  // --- Single-Admin RPCs ---

  @POST("rest/v1/rpc/admin_claim_and_verify")
  suspend fun adminClaimAndVerify(
    @Header("Authorization") bearerToken: String,
    @Body body: AdminDeviceRpcRequest
  ): Response<AdminClaimVerifyResponse>

  @POST("rest/v1/rpc/get_admin_dashboard_metrics")
  suspend fun getAdminDashboardMetrics(
    @Header("Authorization") bearerToken: String,
    @Body body: AdminDeviceRpcRequest
  ): Response<AdminDashboardMetricsResponse>
}
