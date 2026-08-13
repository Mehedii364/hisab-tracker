package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun BiometricLockOverlayScreen(
    isBangla: Boolean = false,
    onTriggerBiometric: () -> Unit,
    onUnlockWithPin: (pin: String) -> Boolean,
    onUnlockSuccess: () -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Pulsing animation for the security badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Trigger biometric prompt on initial render
    LaunchedEffect(Unit) {
        onTriggerBiometric()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonBlackBg)
            .testTag("biometric_lock_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Security Icon Shield
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(NeonSurfaceCard)
                    .border(2.dp, NeonWhiteSolid, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Fingerprint Security",
                    tint = NeonTextPrimary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = if (isBangla) "হিসাব ট্র্যাকার সিকিউরিটি" else "Hisab Tracker Locked",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isBangla)
                    "আপনার আর্থিক ডাটা সুরক্ষিত রাখতে বায়োমেট্রিক (ফিংগারপ্রিন্ট/ফেস আইডি) বা পিন যাচাই করুন।"
                else
                    "Biometric protection active. Verify your fingerprint, face ID, or device PIN to access your accounts.",
                fontSize = 14.sp,
                color = NeonTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Primary Unlock with Biometric Button
            Button(
                onClick = onTriggerBiometric,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("unlock_biometric_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonWhiteSolid,
                    contentColor = NeonBlackBg
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isBangla) "বায়োমেট্রিক দিয়ে আনলক করুন" else "Unlock with Biometrics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Alternative PIN unlock option
            OutlinedButton(
                onClick = {
                    pinInput = ""
                    pinError = false
                    showPinDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("unlock_pin_btn"),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonTextPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Pin,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isBangla) "পিন কোড ব্যবহার করুন" else "Enter Security PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // PIN Entry Keypad Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Text(
                    text = if (isBangla) "সিকিউরিটি পিন দিন" else "Enter 4-Digit Security PIN",
                    fontWeight = FontWeight.Bold,
                    color = NeonTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBangla) "ডিফল্ট পিন: 1234" else "Default PIN: 1234",
                        fontSize = 12.sp,
                        color = NeonTextMuted
                    )

                    // PIN Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < pinInput.length) {
                                            if (pinError) Red500 else NeonWhiteSolid
                                        } else NeonSurfaceHover
                                    )
                                    .border(
                                        1.dp,
                                        if (pinError) Red500 else NeonBorder,
                                        CircleShape
                                    )
                            )
                        }
                    }

                    if (pinError) {
                        Text(
                            text = if (isBangla) "ভুল পিন দেয়া হয়েছে!" else "Incorrect PIN code. Try again.",
                            color = Red500,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Keypad grid 1-9, 0, Backspace
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        val padNumbers = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("", "0", "DEL")
                        )

                        for (row in padNumbers) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (key in row) {
                                    if (key.isEmpty()) {
                                        Spacer(modifier = Modifier.size(56.dp))
                                    } else {
                                        Surface(
                                            onClick = {
                                                if (key == "DEL") {
                                                    if (pinInput.isNotEmpty()) {
                                                        pinInput = pinInput.dropLast(1)
                                                        pinError = false
                                                    }
                                                } else if (pinInput.length < 4) {
                                                    pinInput += key
                                                    pinError = false
                                                    if (pinInput.length == 4) {
                                                        val valid = onUnlockWithPin(pinInput)
                                                        if (valid) {
                                                            showPinDialog = false
                                                            onUnlockSuccess()
                                                        } else {
                                                            pinError = true
                                                        }
                                                    }
                                                }
                                            },
                                            shape = CircleShape,
                                            color = NeonSurfaceHover,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder),
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (key == "DEL") {
                                                    Icon(
                                                        imageVector = Icons.Default.Backspace,
                                                        contentDescription = "Delete",
                                                        tint = NeonTextPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = key,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = NeonTextPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel", color = NeonTextMuted)
                }
            },
            containerColor = NeonSurfaceCard
        )
    }
}
