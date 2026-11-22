package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Course
import com.example.ui.theme.WarningAmber

@Composable
fun CertificateDialog(
  course: Course,
  onDismiss: () -> Unit,
  studentName: String = "Alex Rivera",
  completionDate: String = "August 18, 2026",
  certificateId: String = "SKILL-8849-CERT",
  modifier: Modifier = Modifier
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      modifier = modifier
        .fillMaxWidth()
        .shadow(20.dp, RoundedCornerShape(24.dp))
        .testTag("certificate_dialog")
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(20.dp)
      ) {
        // Top Dismiss Row
        Row(
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.fillMaxWidth()
        ) {
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
          }
        }

        // Certificate Frame Box with Golden / Cyan Borders
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
              )
            )
            .border(2.dp, Brush.linearGradient(listOf(WarningAmber, Color(0xFF38BDF8))), RoundedCornerShape(12.dp))
            .padding(18.dp)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            // Gold Seal
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(WarningAmber.copy(alpha = 0.2f))
                .border(1.5.dp, WarningAmber, CircleShape)
            ) {
              Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(28.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "CERTIFICATE OF ACHIEVEMENT",
              color = WarningAmber,
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.5.sp,
              textAlign = TextAlign.Center
            )

            Text(
              text = "This is proudly presented to",
              color = Color(0xFF94A3B8),
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = studentName,
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "for successfully completing all interactive video lessons and mastering the comprehensive examination in",
              color = Color(0xFF94A3B8),
              fontSize = 10.sp,
              textAlign = TextAlign.Center,
              lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = course.title,
              color = Color(0xFF38BDF8),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Signature & Verification Row
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(horizontalAlignment = Alignment.Start) {
                Text(
                  text = course.instructor,
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "Lead Instructor",
                  color = Color(0xFF94A3B8),
                  fontSize = 9.sp
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = certificateId,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                }
                Text(
                  text = completionDate,
                  color = Color(0xFF94A3B8),
                  fontSize = 9.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(50.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("close_certificate_btn")
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
