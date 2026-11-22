package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyOff
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.remote.PasswordlessAuthState
import com.example.data.remote.supabase.SupabaseSyncStatus
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

enum class AuthMode {
  PASSWORD,
  MAGIC_LINK
}

@Composable
fun PasswordlessAuthDialog(
  authState: PasswordlessAuthState,
  currentUserEmail: String?,
  supabaseSyncStatus: SupabaseSyncStatus = SupabaseSyncStatus.NotConfigured,
  supabaseProjectId: String = "uihcxccaglixqwlwmbzw",
  onSyncToSupabase: () -> Unit = {},
  onSendLink: (String) -> Unit,
  onVerifyLink: (emailLink: String, fallbackEmail: String?) -> Unit,
  onSignInWithPassword: (email: String, password: String) -> Unit = { _, _ -> },
  onCreateAccountWithPassword: (email: String, password: String) -> Unit = { _, _ -> },
  onSendPasswordReset: (email: String) -> Unit = {},
  onSignOut: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedMode by remember { mutableStateOf(AuthMode.PASSWORD) }
  var isSignUpMode by remember { mutableStateOf(false) }

  var emailInput by remember { mutableStateOf(currentUserEmail ?: "") }
  var passwordInput by remember { mutableStateOf("") }
  var confirmPasswordInput by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }
  var localValidationError by remember { mutableStateOf<String?>(null) }

  var manualLinkInput by remember { mutableStateOf("") }
  var showManualLinkInput by remember { mutableStateOf(false) }

  val isBusy = authState is PasswordlessAuthState.SendingLink ||
    authState is PasswordlessAuthState.SigningIn ||
    authState is PasswordlessAuthState.VerifyingLink

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Image(
            painter = painterResource(id = R.drawable.eduverse_logo),
            contentDescription = "Eduverse Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (selectedMode == AuthMode.PASSWORD) {
                if (isSignUpMode) "Create Eduverse Account" else "Eduverse Sign-In"
              } else {
                "Eduverse Magic Link"
              },
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = if (selectedMode == AuthMode.PASSWORD) {
                "Email ID & Password Authentication"
              } else {
                "Firebase Magic Email Link"
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Filled.Close, contentDescription = "Close authentication dialog")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        when (authState) {
          is PasswordlessAuthState.Authenticated -> {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  Icons.Filled.CheckCircle,
                  contentDescription = "Authenticated",
                  tint = SuccessGreen,
                  modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Authenticated Successfully", fontWeight = FontWeight.Bold, color = SuccessGreen)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = authState.email,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = authState.methodLabel,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          is PasswordlessAuthState.LinkSent -> {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Filled.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Magic Link Sent!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "We sent a sign-in link to ${authState.email}.",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = "Click the link in your email on this device to sign in automatically, or paste the link below.",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          }

          is PasswordlessAuthState.PasswordResetSent -> {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Filled.LockReset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Reset Email Sent", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = authState.message,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
          }

          is PasswordlessAuthState.Error -> {
            AuthErrorBanner(message = authState.message)
          }

          else -> {}
        }

        if (localValidationError != null) {
          AuthErrorBanner(message = localValidationError!!)
        }

        if (authState !is PasswordlessAuthState.Authenticated) {
          // Segmented Method Selector: Email & Password vs Magic Link
          SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
          ) {
            SegmentedButton(
              selected = selectedMode == AuthMode.PASSWORD,
              onClick = {
                selectedMode = AuthMode.PASSWORD
                localValidationError = null
              },
              shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
              modifier = Modifier.testTag("auth_mode_password_tab")
            ) {
              Text("Email & Password", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            SegmentedButton(
              selected = selectedMode == AuthMode.MAGIC_LINK,
              onClick = {
                selectedMode = AuthMode.MAGIC_LINK
                localValidationError = null
              },
              shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
              modifier = Modifier.testTag("auth_mode_link_tab")
            ) {
              Text("Magic Link", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
          }

          // Shared Email ID Field
          OutlinedTextField(
            value = emailInput,
            onValueChange = {
              emailInput = it
              localValidationError = null
            },
            label = { Text("Email ID") },
            placeholder = { Text("name@example.com") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Email,
              imeAction = if (selectedMode == AuthMode.PASSWORD) ImeAction.Next else ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
              onSend = {
                if (selectedMode == AuthMode.MAGIC_LINK && emailInput.isNotBlank()) {
                  onSendLink(emailInput)
                }
              }
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_email_input")
          )

          if (selectedMode == AuthMode.PASSWORD) {
            // Password Input
            OutlinedTextField(
              value = passwordInput,
              onValueChange = {
                passwordInput = it
                localValidationError = null
              },
              label = { Text("Password") },
              placeholder = { Text(if (isSignUpMode) "Minimum 6 characters" else "Enter your password") },
              leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
              trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                  Icon(
                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                  )
                }
              },
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              singleLine = true,
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isSignUpMode) ImeAction.Next else ImeAction.Done
              ),
              keyboardActions = KeyboardActions(
                onDone = {
                  if (!isSignUpMode && emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                    onSignInWithPassword(emailInput, passwordInput)
                  }
                }
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_password_input")
            )

            // Confirm Password Input when Creating Account
            AnimatedVisibility(visible = isSignUpMode) {
              OutlinedTextField(
                value = confirmPasswordInput,
                onValueChange = {
                  confirmPasswordInput = it
                  localValidationError = null
                },
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                  IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                      imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                      contentDescription = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password"
                    )
                  }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Password,
                  imeAction = ImeAction.Done
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("auth_confirm_password_input")
              )
            }

            // Primary Submit Button (Sign In or Create Account)
            Button(
              onClick = {
                localValidationError = null
                if (isSignUpMode) {
                  when {
                    passwordInput.length < 6 -> {
                      localValidationError = "Password must be at least 6 characters."
                    }
                    passwordInput != confirmPasswordInput -> {
                      localValidationError = "Passwords do not match."
                    }
                    else -> {
                      onCreateAccountWithPassword(emailInput, passwordInput)
                    }
                  }
                } else {
                  onSignInWithPassword(emailInput, passwordInput)
                }
              },
              enabled = emailInput.isNotBlank() &&
                passwordInput.isNotBlank() &&
                (!isSignUpMode || confirmPasswordInput.isNotBlank()) &&
                !isBusy,
              shape = RoundedCornerShape(50.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("auth_password_submit_button")
            ) {
              if (authState is PasswordlessAuthState.SigningIn) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSignUpMode) "Creating Account..." else "Signing In...")
              } else {
                Icon(
                  imageVector = if (isSignUpMode) Icons.Filled.PersonAdd else Icons.Filled.LockOpen,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isSignUpMode) "Create Account" else "Sign In with Email",
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Secondary Actions: Toggle Sign Up / Sign In + Forgot Password
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              TextButton(
                onClick = {
                  isSignUpMode = !isSignUpMode
                  localValidationError = null
                },
                modifier = Modifier.testTag("auth_toggle_signup_button")
              ) {
                Text(
                  text = if (isSignUpMode) "Already have an account?" else "Create new account",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }

              if (!isSignUpMode) {
                TextButton(
                  onClick = {
                    localValidationError = null
                    onSendPasswordReset(emailInput)
                  },
                  enabled = !isBusy,
                  modifier = Modifier.testTag("auth_forgot_password_button")
                ) {
                  Text("Forgot password?", fontSize = 12.sp)
                }
              }
            }
          } else {
            // Passwordless Magic Link UI
            Button(
              onClick = { onSendLink(emailInput) },
              enabled = emailInput.isNotBlank() && !isBusy,
              shape = RoundedCornerShape(50.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("send_magic_link_button")
            ) {
              if (authState is PasswordlessAuthState.SendingLink) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sending Magic Link...")
              } else {
                Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send Passwordless Magic Link", fontWeight = FontWeight.Bold)
              }
            }

            // Toggle Paste Link Option for testing/manual verification
            TextButton(
              onClick = { showManualLinkInput = !showManualLinkInput },
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (showManualLinkInput) "Hide manual link entry" else "Have a magic link? Paste here",
                fontSize = 12.sp
              )
            }

            AnimatedVisibility(visible = showManualLinkInput) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = manualLinkInput,
                  onValueChange = { manualLinkInput = it },
                  label = { Text("Paste Magic Link") },
                  placeholder = { Text("https://skillstream-auth.web.app/emailSignIn?apiKey=...") },
                  minLines = 2,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_link_input")
                )

                FilledTonalButton(
                  onClick = { onVerifyLink(manualLinkInput, emailInput.ifBlank { null }) },
                  enabled = manualLinkInput.isNotBlank() && !isBusy,
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("verify_magic_link_button")
                ) {
                  if (authState is PasswordlessAuthState.VerifyingLink) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Verifying Link...", fontSize = 12.sp)
                  } else {
                    Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify & Complete Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }

        // Supabase Backend Connection & User Sync Status Card
        SupabaseStatusBanner(
          status = supabaseSyncStatus,
          projectId = supabaseProjectId,
          onSyncNow = onSyncToSupabase
        )
      }
    },
    confirmButton = {
      if (authState is PasswordlessAuthState.Authenticated) {
        Button(
          onClick = onSignOut,
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
          shape = RoundedCornerShape(50.dp),
          modifier = Modifier.testTag("auth_sign_out_button")
        ) {
          Text("Sign Out")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Done")
      }
    },
    modifier = modifier.testTag("passwordless_auth_dialog")
  )
}

@Composable
private fun SupabaseStatusBanner(
  status: SupabaseSyncStatus,
  projectId: String,
  onSyncNow: () -> Unit
) {
  val (bgColor, tintColor, statusTitle, statusDetail, icon) = when (status) {
    is SupabaseSyncStatus.Connected -> StatusUi(
      bg = SuccessGreen.copy(alpha = 0.10f),
      tint = SuccessGreen,
      title = "Supabase Connected",
      detail = "Project $projectId • Synced to app_users (${status.email ?: status.userKey.take(16)})",
      icon = Icons.Filled.CloudDone
    )
    is SupabaseSyncStatus.Syncing -> StatusUi(
      bg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
      tint = MaterialTheme.colorScheme.primary,
      title = "Syncing with Supabase...",
      detail = "Project $projectId • Updating app_users table",
      icon = Icons.Filled.CloudSync
    )
    is SupabaseSyncStatus.NotConfigured -> StatusUi(
      bg = WarningAmber.copy(alpha = 0.12f),
      tint = WarningAmber,
      title = "Supabase Key Required",
      detail = "Add SUPABASE_PUBLISHABLE_KEY in AI Studio Secrets for project $projectId.",
      icon = Icons.Filled.CloudOff
    )
    is SupabaseSyncStatus.Error -> StatusUi(
      bg = ErrorRed.copy(alpha = 0.10f),
      tint = ErrorRed,
      title = "Supabase Sync Issue",
      detail = status.message,
      icon = Icons.Filled.CloudOff
    )
  }

  Surface(
    color = bgColor,
    shape = RoundedCornerShape(10.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("supabase_status_banner")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = statusTitle,
          tint = tintColor,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = statusTitle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = tintColor
          )
          Text(
            text = statusDetail,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      if (status !is SupabaseSyncStatus.Syncing && status !is SupabaseSyncStatus.NotConfigured) {
        TextButton(
          onClick = onSyncNow,
          modifier = Modifier.testTag("supabase_sync_now_button")
        ) {
          Text("Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

private data class StatusUi(
  val bg: androidx.compose.ui.graphics.Color,
  val tint: androidx.compose.ui.graphics.Color,
  val title: String,
  val detail: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun AuthErrorBanner(message: String) {
  Surface(
    color = ErrorRed.copy(alpha = 0.12f),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(10.dp)
    ) {
      Icon(Icons.Outlined.Info, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = message,
        fontSize = 12.sp,
        color = ErrorRed,
        lineHeight = 16.sp,
        modifier = Modifier.weight(1f)
      )
    }
  }
}
