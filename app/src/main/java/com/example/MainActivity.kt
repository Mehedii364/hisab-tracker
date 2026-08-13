package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.security.BiometricAvailability
import com.example.security.BiometricSecurityHelper
import com.example.ui.HisabViewModel
import com.example.ui.LocaleStrings
import com.example.ui.screens.*
import com.example.ui.theme.*
import java.io.File

class MainActivity : FragmentActivity() {

    private val viewModel: HisabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HisabTrackerTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: HisabViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Home, 1: History, 2: Accounts, 3: AI Chat, 4: Reports, 5: Profile

    val notification by viewModel.uiNotification.collectAsState()
    val pendingDeleteState by viewModel.pendingDeleteDialogState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val isBangla = userProfile.appLanguage == "BN"

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricHelper = remember(context) { BiometricSecurityHelper(context) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let { notif ->
            val result = snackbarHostState.showSnackbar(
                message = notif.message,
                actionLabel = if (notif.showUndo) "UNDO" else null,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastAction()
            }
            viewModel.dismissNotification()
        }
    }

    if (isAppLocked) {
        BiometricLockOverlayScreen(
            isBangla = isBangla,
            onTriggerBiometric = {
                if (activity != null) {
                    val status = biometricHelper.checkBiometricSupport()
                    if (status is BiometricAvailability.Available) {
                        biometricHelper.promptBiometricAuthentication(
                            activity = activity,
                            title = if (isBangla) "হিসাব ট্র্যাকার সিকিউরিটি" else "Unlock Hisab Tracker",
                            subtitle = if (isBangla) "বায়োমেট্রিক স্ক্যান করুন" else "Verify Fingerprint or Facial Recognition",
                            description = if (isBangla) "আপনার ব্যক্তিগত অ্যাকাউন্ট ও লেনদেন সুরক্ষিত রাখতে বায়োমেট্রিক স্ক্যান করুন।" else "Scan biometrics to unlock your personal financial data.",
                            onSuccess = { viewModel.unlockApp() },
                            onError = { _, _ -> },
                            onFailed = {}
                        )
                    }
                }
            },
            onUnlockWithPin = { pin ->
                viewModel.verifyPin(pin)
            },
            onUnlockSuccess = {
                viewModel.unlockApp()
            }
        )
        return
    }

    Scaffold(
        containerColor = NeonBlackBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(28.dp),
                color = NeonSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.height(68.dp)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("nav_home"),
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text(LocaleStrings.get("nav_home", isBangla), fontWeight = if (selectedTab == 0) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonBlackBg,
                            selectedTextColor = NeonTextPrimary,
                            indicatorColor = NeonWhiteSolid,
                            unselectedIconColor = NeonTextMuted,
                            unselectedTextColor = NeonTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("nav_transactions"),
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transactions") },
                        label = { Text(LocaleStrings.get("nav_transactions", isBangla), fontWeight = if (selectedTab == 1) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonBlackBg,
                            selectedTextColor = NeonTextPrimary,
                            indicatorColor = NeonWhiteSolid,
                            unselectedIconColor = NeonTextMuted,
                            unselectedTextColor = NeonTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.testTag("nav_accounts"),
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Accounts") },
                        label = { Text(LocaleStrings.get("nav_accounts", isBangla), fontWeight = if (selectedTab == 2) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonBlackBg,
                            selectedTextColor = NeonTextPrimary,
                            indicatorColor = NeonWhiteSolid,
                            unselectedIconColor = NeonTextMuted,
                            unselectedTextColor = NeonTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        modifier = Modifier.testTag("nav_ai_chat"),
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant") },
                        label = { Text(LocaleStrings.get("nav_ai_chat", isBangla), fontWeight = if (selectedTab == 3) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonBlackBg,
                            selectedTextColor = NeonTextPrimary,
                            indicatorColor = NeonWhiteSolid,
                            unselectedIconColor = NeonTextMuted,
                            unselectedTextColor = NeonTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        modifier = Modifier.testTag("nav_analytics"),
                        icon = { Icon(Icons.Default.PieChart, contentDescription = "Reports") },
                        label = { Text(LocaleStrings.get("nav_reports", isBangla), fontWeight = if (selectedTab == 4) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonBlackBg,
                            selectedTextColor = NeonTextPrimary,
                            indicatorColor = NeonWhiteSolid,
                            unselectedIconColor = NeonTextMuted,
                            unselectedTextColor = NeonTextMuted
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        modifier = Modifier.testTag("nav_profile"),
                        icon = {
                            val context = LocalContext.current
                            val photoFile = File(userProfile.profilePicturePath)
                            val hasPhoto = userProfile.profilePicturePath.isNotBlank() && photoFile.exists()
                            if (hasPhoto) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(photoFile)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = "Profile")
                            }
                        },
                        label = { Text(LocaleStrings.get("nav_profile", isBangla), fontWeight = if (selectedTab == 5) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonBlackBg,
                            selectedTextColor = NeonTextPrimary,
                            indicatorColor = NeonWhiteSolid,
                            unselectedIconColor = NeonTextMuted,
                            unselectedTextColor = NeonTextMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { selectedTab = 3 },
                    onNavigateToTransactions = { selectedTab = 1 },
                    onNavigateToBudgets = { selectedTab = 4 }
                )
                1 -> TransactionsScreen(viewModel = viewModel)
                2 -> AccountsScreen(viewModel = viewModel)
                3 -> AiChatScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { selectedTab = 6 }
                )
                4 -> AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { selectedTab = 3 }
                )
                5 -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { selectedTab = 6 }
                )
                6 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Pending Action / Delete Confirmation Dialog
    if (pendingDeleteState.isVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Confirm Deletion", fontWeight = FontWeight.Bold, color = NeonTextPrimary) },
            text = {
                Text("Are you sure you want to delete this transaction (${pendingDeleteState.description} - ৳${pendingDeleteState.amount.toInt()})?", color = NeonTextPrimary)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Red500),
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancel", color = NeonTextPrimary)
                }
            },
            containerColor = NeonSurfaceCard
        )
    }
}
