package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.supabase.AdminActivityEventDto
import com.example.data.remote.supabase.AdminDashboardMetricsResponse
import com.example.data.remote.supabase.SupabaseAppUserRecord
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AdminPanelUiState

@Composable
fun AdminDashboardScreen(
  adminState: AdminPanelUiState,
  onSubmitAdminLogin: (email: String, password: String, isFirstTimeSetup: Boolean) -> Unit,
  onRefreshMetrics: () -> Unit,
  onBackToAccount: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  BackHandler { onBackToAccount() }

  Column(modifier = modifier.fillMaxSize()) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      TextButton(
        onClick = onBackToAccount,
        modifier = Modifier.testTag("admin_back_to_account_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back to My Account",
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Back to My Account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    when (adminState) {
      is AdminPanelUiState.Locked -> {
        AdminOneTimeLoginGate(
          isAuthenticating = false,
          errorMessage = adminState.errorMessage,
          onSubmit = onSubmitAdminLogin,
          modifier = Modifier.weight(1f)
        )
      }

      is AdminPanelUiState.Authenticating -> {
        AdminOneTimeLoginGate(
          isAuthenticating = true,
          errorMessage = null,
          onSubmit = onSubmitAdminLogin,
          modifier = Modifier.weight(1f)
        )
      }

      is AdminPanelUiState.Unlocked -> {
        AdminAnalyticsDashboard(
          adminEmail = adminState.adminEmail,
          metrics = adminState.metrics,
          isLoading = adminState.isLoading,
          errorMessage = adminState.errorMessage,
          onRefresh = onRefreshMetrics,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun AdminOneTimeLoginGate(
  isAuthenticating: Boolean,
  errorMessage: String?,
  onSubmit: (email: String, password: String, isFirstTimeSetup: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var isFirstTimeSetup by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Header Shield Badge
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
      Icon(
        imageVector = Icons.Filled.AdminPanelSettings,
        contentDescription = "Admin Panel",
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.size(34.dp)
      )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "Admin Control Center",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Single-Admin Lock • One-Time Login Accepted",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "How One-Time Admin Lock Works",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Text(
          text = "Only one admin login is accepted across your Supabase backend. Once claimed, this device stays authorized for the Admin Panel and any second login from another account or device is rejected.",
          fontSize = 12.sp,
          lineHeight = 17.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          SegmentedButton(
            selected = !isFirstTimeSetup,
            onClick = { isFirstTimeSetup = false },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            modifier = Modifier.testTag("admin_mode_signin_tab")
          ) {
            Text("Admin Sign-In", fontSize = 12.sp)
          }
          SegmentedButton(
            selected = isFirstTimeSetup,
            onClick = { isFirstTimeSetup = true },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            modifier = Modifier.testTag("admin_mode_claim_tab")
          ) {
            Text("First-Time Claim", fontSize = 12.sp)
          }
        }

        if (!errorMessage.isNullOrBlank()) {
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
                text = errorMessage,
                fontSize = 12.sp,
                color = ErrorRed,
                lineHeight = 16.sp
              )
            }
          }
        }

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Admin Email ID") },
          placeholder = { Text("admin@example.com") },
          leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_email_input")
        )

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Admin Password") },
          placeholder = { Text("Minimum 6 characters") },
          leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
          trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
              Icon(
                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = if (showPassword) "Hide password" else "Show password"
              )
            }
          },
          visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_password_input")
        )

        Button(
          onClick = { onSubmit(email, password, isFirstTimeSetup) },
          enabled = email.isNotBlank() && password.length >= 6 && !isAuthenticating,
          shape = RoundedCornerShape(50.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("admin_login_submit_button")
        ) {
          if (isAuthenticating) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verifying Single-Admin Lock...")
          } else {
            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isFirstTimeSetup) "Create & Claim One-Time Admin" else "Verify & Unlock Admin (Once)",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AdminAnalyticsDashboard(
  adminEmail: String,
  metrics: AdminDashboardMetricsResponse?,
  isLoading: Boolean,
  errorMessage: String?,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Verified Single-Admin Banner
    item {
      Surface(
        color = SuccessGreen.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = Icons.Filled.VerifiedUser,
              contentDescription = "Single Admin Verified",
              tint = SuccessGreen,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Single-Admin Lock Active",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
              )
              Text(
                text = "Claimed once by $adminEmail",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          FilledTonalButton(
            onClick = onRefresh,
            enabled = !isLoading,
            modifier = Modifier
              .height(40.dp)
              .testTag("admin_refresh_button")
          ) {
            if (isLoading) {
              CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            } else {
              Icon(Icons.Filled.Refresh, contentDescription = "Refresh analytics", modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("Refresh", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }

    if (!errorMessage.isNullOrBlank()) {
      item {
        Surface(
          color = ErrorRed.copy(alpha = 0.12f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(errorMessage, fontSize = 12.sp, color = ErrorRed)
          }
        }
      }
    }

    // 2x2 KPI Summary Cards
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          AdminMetricCard(
            title = "Total Visitors",
            value = "${metrics?.totalVisitors ?: 0}",
            subtitle = "${metrics?.authenticatedUsers ?: 0} signed-in accounts",
            icon = Icons.Filled.Group,
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
          )
          AdminMetricCard(
            title = "Total Traffic",
            value = "${metrics?.totalTraffic ?: 0}",
            subtitle = "Total app visits",
            icon = Icons.Filled.TrendingUp,
            accentColor = SuccessGreen,
            modifier = Modifier.weight(1f)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          AdminMetricCard(
            title = "Total Watchtime",
            value = formatWatchTime(metrics?.totalWatchTimeSeconds ?: 0L),
            subtitle = "${metrics?.totalWatchTimeSeconds ?: 0}s total watched",
            icon = Icons.Filled.PlayCircleFilled,
            accentColor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
          )
          AdminMetricCard(
            title = "Total Link Clicks",
            value = "${metrics?.totalLinkClicks ?: 0}",
            subtitle = "Course, lesson & link taps",
            icon = Icons.Filled.Link,
            accentColor = WarningAmber,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Section: Watchtime & Traffic by User
    item {
      Text(
        text = "Watchtime & Traffic by User (${metrics?.users?.size ?: 0})",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    val users = metrics?.users.orEmpty()
    if (users.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = if (isLoading) "Loading user analytics from Supabase..." else "No user records synced yet. Tap Refresh or watch a lesson to sync data.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(users, key = { it.userKey }) { user ->
        AdminUserRowCard(user = user)
      }
    }

    // Section: Recent Traffic & Link Click Events
    item {
      Text(
        text = "Recent Traffic & Link Click Events (${metrics?.recentEvents?.size ?: 0})",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 6.dp)
      )
    }

    val events = metrics?.recentEvents.orEmpty()
    if (events.isNotEmpty()) {
      items(events, key = { it.id }) { event ->
        AdminEventRowCard(event = event)
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun AdminMetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = title,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f))
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun AdminUserRowCard(user: SupabaseAppUserRecord) {
  val label = user.email ?: user.displayName ?: user.userKey.take(18)
  val initials = label.take(2).uppercase()

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
          Text(
            text = initials,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${user.appPlatform.uppercase()} • ${user.authProvider} • ${user.deviceModel ?: "Device"}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        Surface(
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
          shape = RoundedCornerShape(50.dp)
        ) {
          Text(
            text = formatWatchTime(user.watchTimeSeconds),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        MiniStatPill(label = "Traffic", value = "${user.trafficVisits}")
        MiniStatPill(label = "Link Clicks", value = "${user.linkClicks}")
        MiniStatPill(label = "Lessons", value = "${user.lessonsCompleted}")
        MiniStatPill(label = "XP", value = "${user.xpPoints}")
      }
    }
  }
}

@Composable
private fun MiniStatPill(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun AdminEventRowCard(event: AdminActivityEventDto) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    shape = RoundedCornerShape(10.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = event.eventType.uppercase(),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = event.eventDetail ?: event.email ?: event.userKey.take(20),
          fontSize = 12.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      Text(
        text = event.createdAt?.take(19)?.replace("T", " ") ?: "",
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun formatWatchTime(totalSeconds: Long): String {
  if (totalSeconds <= 0L) return "0m 0s"
  val hours = totalSeconds / 3600
  val mins = (totalSeconds % 3600) / 60
  val secs = totalSeconds % 60
  return when {
    hours > 0 -> "${hours}h ${mins}m"
    mins > 0 -> "${mins}m ${secs}s"
    else -> "${secs}s"
  }
}
