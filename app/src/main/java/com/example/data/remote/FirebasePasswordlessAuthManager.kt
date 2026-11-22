package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.remote.supabase.SupabaseRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * State representing authentication flow across Supabase Auth & Firebase Auth.
 */
sealed class PasswordlessAuthState {
  object Idle : PasswordlessAuthState()
  object SendingLink : PasswordlessAuthState()
  object SigningIn : PasswordlessAuthState()
  data class LinkSent(val email: String, val message: String) : PasswordlessAuthState()
  data class PasswordResetSent(val email: String, val message: String) : PasswordlessAuthState()
  object VerifyingLink : PasswordlessAuthState()
  data class Authenticated(
    val user: FirebaseUser?,
    val email: String,
    val methodLabel: String = "Supabase Backend"
  ) : PasswordlessAuthState()
  data class Error(val message: String, val canRetry: Boolean = true) : PasswordlessAuthState()
}

/**
 * Unified Authentication Manager supporting:
 * 1. Supabase Auth (Email ID & Password, Account Creation, Password Reset, Magic Link)
 * 2. Firebase Auth fallback (when Firebase is configured via google-services.json)
 */
class FirebasePasswordlessAuthManager(
  private val context: Context,
  private val supabaseRepository: SupabaseRepository? = null
) {

  companion object {
    private const val TAG = "AuthManager"
    private const val PREFS_NAME = "skillstream_auth_prefs"
    private const val KEY_EMAIL_FOR_SIGN_IN = "email_for_sign_in"

    const val DEFAULT_CONTINUE_URL = "https://skillstream-auth.web.app/emailSignIn"
    const val SUPABASE_REDIRECT_URL = "skillstream://emailSignIn"
    const val ANDROID_PACKAGE_NAME = "com.aistudio.skillstream.kxmpzq"
    const val IOS_BUNDLE_ID = "com.aistudio.skillstream"
    const val MINIMUM_VERSION = "1"
  }

  private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _authState = MutableStateFlow<PasswordlessAuthState>(PasswordlessAuthState.Idle)
  val authState: StateFlow<PasswordlessAuthState> = _authState.asStateFlow()

  private val _currentUserEmail = MutableStateFlow<String?>(null)
  val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

  private var authInstance: FirebaseAuth? = null

  init {
    // Restore active Supabase session first if present
    val supabaseEmail = supabaseRepository?.currentSessionEmail()
    if (!supabaseEmail.isNullOrBlank()) {
      _currentUserEmail.value = supabaseEmail
      _authState.value = PasswordlessAuthState.Authenticated(
        user = null,
        email = supabaseEmail,
        methodLabel = "Signed in with Supabase"
      )
    } else {
      try {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
          authInstance = FirebaseAuth.getInstance()
          val current = authInstance?.currentUser
          if (current != null) {
            _currentUserEmail.value = current.email
            _authState.value = PasswordlessAuthState.Authenticated(
              user = current,
              email = current.email ?: "User",
              methodLabel = "Signed in with Firebase Auth"
            )
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Firebase Auth not yet configured: ${e.localizedMessage}")
      }
    }
  }

  /**
   * Signs in an existing user with their Email ID and Password.
   * Uses Supabase Backend when configured, falling back to Firebase Auth if available.
   */
  suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?> {
    val trimmedEmail = email.trim()
    if (!isValidEmail(trimmedEmail)) {
      val errorMsg = "Please enter a valid email address."
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      return Result.failure(IllegalArgumentException(errorMsg))
    }
    if (password.isBlank()) {
      val errorMsg = "Please enter your password."
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      return Result.failure(IllegalArgumentException(errorMsg))
    }

    _authState.value = PasswordlessAuthState.SigningIn

    if (supabaseRepository != null && supabaseRepository.isConfigured()) {
      val supabaseResult = supabaseRepository.signInWithEmailAndPassword(trimmedEmail, password)
      return supabaseResult.fold(
        onSuccess = { sbUser ->
          val resolvedEmail = sbUser.email ?: trimmedEmail
          _currentUserEmail.value = resolvedEmail
          _authState.value = PasswordlessAuthState.Authenticated(
            user = null,
            email = resolvedEmail,
            methodLabel = "Signed in via Supabase Backend"
          )
          Result.success(null)
        },
        onFailure = { err ->
          val errorMsg = err.localizedMessage ?: "Supabase sign-in failed."
          _authState.value = PasswordlessAuthState.Error(errorMsg)
          Result.failure(err)
        }
      )
    }

    return try {
      val auth = getOrCreateFirebaseAuth()
      val authResult = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
      val user = authResult.user
      val signedInEmail = user?.email ?: trimmedEmail

      _currentUserEmail.value = signedInEmail
      _authState.value = PasswordlessAuthState.Authenticated(
        user = user,
        email = signedInEmail,
        methodLabel = "Signed in with Email & Password"
      )
      Result.success(user)
    } catch (e: Exception) {
      val errorMsg = if (supabaseRepository != null && !supabaseRepository.isConfigured()) {
        "Configure SUPABASE_PUBLISHABLE_KEY in AI Studio Secrets to sign in."
      } else {
        e.localizedMessage ?: "Sign-in failed. Please check your email and password."
      }
      Log.e(TAG, "Email/password sign-in failed: $errorMsg", e)
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      Result.failure(e)
    }
  }

  /**
   * Registers a new user account with Email ID and Password.
   */
  suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?> {
    val trimmedEmail = email.trim()
    if (!isValidEmail(trimmedEmail)) {
      val errorMsg = "Please enter a valid email address."
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      return Result.failure(IllegalArgumentException(errorMsg))
    }
    if (password.length < 6) {
      val errorMsg = "Password must be at least 6 characters long."
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      return Result.failure(IllegalArgumentException(errorMsg))
    }

    _authState.value = PasswordlessAuthState.SigningIn

    if (supabaseRepository != null && supabaseRepository.isConfigured()) {
      val supabaseResult = supabaseRepository.signUpWithEmailAndPassword(trimmedEmail, password)
      return supabaseResult.fold(
        onSuccess = { sbUser ->
          val resolvedEmail = sbUser.email ?: trimmedEmail
          _currentUserEmail.value = resolvedEmail
          _authState.value = PasswordlessAuthState.Authenticated(
            user = null,
            email = resolvedEmail,
            methodLabel = "Account created via Supabase Backend"
          )
          Result.success(null)
        },
        onFailure = { err ->
          val errorMsg = err.localizedMessage ?: "Account creation failed."
          _authState.value = PasswordlessAuthState.Error(errorMsg)
          Result.failure(err)
        }
      )
    }

    return try {
      val auth = getOrCreateFirebaseAuth()
      val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
      val user = authResult.user
      val createdEmail = user?.email ?: trimmedEmail

      _currentUserEmail.value = createdEmail
      _authState.value = PasswordlessAuthState.Authenticated(
        user = user,
        email = createdEmail,
        methodLabel = "Account created with Email & Password"
      )
      Result.success(user)
    } catch (e: Exception) {
      val errorMsg = if (supabaseRepository != null && !supabaseRepository.isConfigured()) {
        "Configure SUPABASE_PUBLISHABLE_KEY in AI Studio Secrets to create an account."
      } else {
        e.localizedMessage ?: "Account creation failed."
      }
      Log.e(TAG, "Email/password sign-up failed: $errorMsg", e)
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      Result.failure(e)
    }
  }

  /**
   * Sends a password reset email to the given Email ID.
   */
  suspend fun sendPasswordResetEmail(email: String): Result<String> {
    val trimmedEmail = email.trim()
    if (!isValidEmail(trimmedEmail)) {
      val errorMsg = "Please enter a valid email address to reset your password."
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      return Result.failure(IllegalArgumentException(errorMsg))
    }

    _authState.value = PasswordlessAuthState.SigningIn

    if (supabaseRepository != null && supabaseRepository.isConfigured()) {
      val supabaseResult = supabaseRepository.sendPasswordReset(trimmedEmail)
      return supabaseResult.fold(
        onSuccess = { msg ->
          _authState.value = PasswordlessAuthState.PasswordResetSent(trimmedEmail, msg)
          Result.success(msg)
        },
        onFailure = { err ->
          val errorMsg = err.localizedMessage ?: "Failed to send password reset email."
          _authState.value = PasswordlessAuthState.Error(errorMsg)
          Result.failure(err)
        }
      )
    }

    return try {
      val auth = getOrCreateFirebaseAuth()
      auth.sendPasswordResetEmail(trimmedEmail).await()
      val msg = "Password reset instructions sent to $trimmedEmail. Check your inbox."
      _authState.value = PasswordlessAuthState.PasswordResetSent(trimmedEmail, msg)
      Result.success(msg)
    } catch (e: Exception) {
      val errorMsg = e.localizedMessage ?: "Failed to send password reset email."
      Log.e(TAG, "Password reset failed: $errorMsg", e)
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      Result.failure(e)
    }
  }

  /**
   * Builds the cross-platform ActionCodeSettings required by Firebase Passwordless Authentication.
   */
  fun buildActionCodeSettings(
    url: String = DEFAULT_CONTINUE_URL,
    androidPackage: String = ANDROID_PACKAGE_NAME,
    iosBundle: String = IOS_BUNDLE_ID
  ): ActionCodeSettings {
    return ActionCodeSettings.newBuilder()
      .setUrl(url)
      .setHandleCodeInApp(true)
      .setAndroidPackageName(
        androidPackage,
        true,
        MINIMUM_VERSION
      )
      .setIOSBundleId(iosBundle)
      .build()
  }

  /**
   * Sends the passwordless magic link to the provided email address.
   */
  suspend fun sendSignInLinkToEmail(
    email: String,
    actionCodeSettings: ActionCodeSettings = buildActionCodeSettings()
  ): Result<String> {
    val trimmedEmail = email.trim()
    if (!isValidEmail(trimmedEmail)) {
      val errorMsg = "Please enter a valid email address."
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      return Result.failure(IllegalArgumentException(errorMsg))
    }

    _authState.value = PasswordlessAuthState.SendingLink

    if (supabaseRepository != null && supabaseRepository.isConfigured()) {
      val supabaseResult = supabaseRepository.sendMagicLink(trimmedEmail, SUPABASE_REDIRECT_URL)
      return supabaseResult.fold(
        onSuccess = { msg ->
          saveEmailForSignIn(trimmedEmail)
          _authState.value = PasswordlessAuthState.LinkSent(trimmedEmail, msg)
          Result.success(msg)
        },
        onFailure = { err ->
          val errorMsg = err.localizedMessage ?: "Failed to send Supabase magic link."
          _authState.value = PasswordlessAuthState.Error(errorMsg)
          Result.failure(err)
        }
      )
    }

    return try {
      val auth = getOrCreateFirebaseAuth()
      auth.sendSignInLinkToEmail(trimmedEmail, actionCodeSettings).await()

      saveEmailForSignIn(trimmedEmail)

      val successMsg = "Magic sign-in link sent to $trimmedEmail. Please check your inbox or spam folder."
      _authState.value = PasswordlessAuthState.LinkSent(trimmedEmail, successMsg)
      Result.success(successMsg)
    } catch (e: Exception) {
      val errorMsg = e.localizedMessage ?: "Failed to send sign-in link."
      Log.e(TAG, "Error sending sign in link: $errorMsg", e)
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      Result.failure(e)
    }
  }

  /**
   * Checks if an incoming deep link or URL string is a valid Supabase or Firebase Email Link.
   */
  fun isSignInWithEmailLink(link: String?): Boolean {
    if (link.isNullOrBlank()) return false
    if (link.contains("access_token=") && link.contains("refresh_token=")) return true
    return try {
      val auth = getOrCreateFirebaseAuth()
      auth.isSignInWithEmailLink(link)
    } catch (e: Exception) {
      link.contains("apiKey=") && (link.contains("oobCode=") || link.contains("mode=signIn"))
    }
  }

  /**
   * Completes the sign-in using the incoming deep link (Supabase or Firebase).
   */
  suspend fun completeSignInWithEmailLink(
    emailLink: String,
    fallbackEmail: String? = null
  ): Result<FirebaseUser?> {
    _authState.value = PasswordlessAuthState.VerifyingLink

    if (emailLink.contains("access_token=") && supabaseRepository != null) {
      val sbResult = supabaseRepository.handleMagicLinkCallback(emailLink)
      return sbResult.fold(
        onSuccess = { user ->
          clearSavedEmailForSignIn()
          val resolvedEmail = user.email ?: fallbackEmail ?: "User"
          _currentUserEmail.value = resolvedEmail
          _authState.value = PasswordlessAuthState.Authenticated(
            user = null,
            email = resolvedEmail,
            methodLabel = "Signed in via Supabase Magic Link"
          )
          Result.success(null)
        },
        onFailure = { err ->
          _authState.value = PasswordlessAuthState.Error(err.localizedMessage ?: "Magic link verification failed.")
          Result.failure(err)
        }
      )
    }

    val targetEmail = fallbackEmail?.trim() ?: getSavedEmailForSignIn()

    if (targetEmail.isNullOrBlank()) {
      val errorMsg = "Email address required to complete sign-in from this link."
      _authState.value = PasswordlessAuthState.Error(errorMsg, canRetry = true)
      return Result.failure(IllegalStateException(errorMsg))
    }

    return try {
      val auth = getOrCreateFirebaseAuth()
      val authResult = auth.signInWithEmailLink(targetEmail, emailLink).await()
      val user = authResult.user

      clearSavedEmailForSignIn()

      _currentUserEmail.value = user?.email ?: targetEmail
      _authState.value = PasswordlessAuthState.Authenticated(
        user = user,
        email = user?.email ?: targetEmail,
        methodLabel = "Signed in with passwordless magic link"
      )
      Result.success(user)
    } catch (e: Exception) {
      val errorMsg = e.localizedMessage ?: "Failed to authenticate with the provided email link."
      Log.e(TAG, "Sign in with email link failed: $errorMsg", e)
      _authState.value = PasswordlessAuthState.Error(errorMsg)
      Result.failure(e)
    }
  }

  /**
   * Signs out the current user from Supabase & Firebase.
   */
  suspend fun signOut() {
    try {
      supabaseRepository?.signOut()
    } catch (e: Exception) {
      Log.w(TAG, "Supabase sign out error: ${e.localizedMessage}")
    }
    try {
      authInstance?.signOut()
    } catch (e: Exception) {
      Log.w(TAG, "Firebase sign out error: ${e.localizedMessage}")
    }
    clearSavedEmailForSignIn()
    _currentUserEmail.value = null
    _authState.value = PasswordlessAuthState.Idle
  }

  /**
   * Clears any active error state or returns to Idle.
   */
  fun resetState() {
    _authState.value = if (_currentUserEmail.value != null) {
      PasswordlessAuthState.Authenticated(
        user = authInstance?.currentUser,
        email = _currentUserEmail.value!!,
        methodLabel = if (supabaseRepository?.isConfigured() == true) "Signed in via Supabase" else "Signed in with Firebase Auth"
      )
    } else {
      PasswordlessAuthState.Idle
    }
  }

  fun saveEmailForSignIn(email: String) {
    prefs.edit().putString(KEY_EMAIL_FOR_SIGN_IN, email).apply()
  }

  fun getSavedEmailForSignIn(): String? {
    return prefs.getString(KEY_EMAIL_FOR_SIGN_IN, null)
  }

  fun clearSavedEmailForSignIn() {
    prefs.edit().remove(KEY_EMAIL_FOR_SIGN_IN).apply()
  }

  private fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
  }

  private fun getOrCreateFirebaseAuth(): FirebaseAuth {
    if (authInstance == null) {
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
      }
      authInstance = FirebaseAuth.getInstance()
    }
    return authInstance ?: FirebaseAuth.getInstance()
  }
}
