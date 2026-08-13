package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.HisabViewModel
import com.example.ui.LocaleStrings
import com.example.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: HisabViewModel,
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val isBangla = profile.appLanguage == "BN"

    // Edit state toggle
    var isEditing by remember { mutableStateOf(false) }

    // Form states
    var nameText by remember(profile.userName) { mutableStateOf(profile.userName) }
    var nicknameText by remember(profile.nickname) { mutableStateOf(profile.nickname) }
    var mobileText by remember(profile.mobileNumber) { mutableStateOf(profile.mobileNumber) }
    var emailText by remember(profile.userEmail) { mutableStateOf(profile.userEmail) }
    var addressText by remember(profile.address) { mutableStateOf(profile.address) }
    var countryText by remember(profile.country) { mutableStateOf(profile.country) }
    var currencyText by remember(profile.currencySymbol) { mutableStateOf(profile.currencySymbol) }
    var timezoneText by remember(profile.timezone) { mutableStateOf(profile.timezone) }
    var bioText by remember(profile.bio) { mutableStateOf(profile.bio) }

    var incomeTargetText by remember(profile.monthlyIncomeTarget) { mutableStateOf(profile.monthlyIncomeTarget.toInt().toString()) }
    var expenseLimitText by remember(profile.monthlyExpenseLimit) { mutableStateOf(profile.monthlyExpenseLimit.toInt().toString()) }
    var savingsTargetText by remember(profile.monthlySavingsTarget) { mutableStateOf(profile.monthlySavingsTarget.toInt().toString()) }
    var savingsPercentText by remember(profile.savingsTargetPercent) { mutableStateOf(profile.savingsTargetPercent.toInt().toString()) }
    var budgetStartDayText by remember(profile.budgetStartDay) { mutableStateOf(profile.budgetStartDay.toString()) }

    var defaultAccountText by remember(profile.defaultAccount) { mutableStateOf(profile.defaultAccount) }
    var defaultPaymentMethodText by remember(profile.defaultPaymentMethod) { mutableStateOf(profile.defaultPaymentMethod) }

    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Activity Result Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.saveProfilePictureFromUri(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.saveProfilePictureFromBitmap(context, it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch()
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = NeonBlackBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = NeonTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                if (isBangla) "প্রোফাইল সেটিংস" else "Profile & Account",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonTextPrimary
                            )
                            Text(
                                if (isBangla) "ব্যক্তিগত ও আর্থিক নিরাপত্তা" else "Personal & Financial Security",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonTextMuted
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = null,
                            tint = NeonTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEditing) (if (isBangla) "সম্পন্ন" else "Done") else (if (isBangla) "সম্পাদনা" else "Edit"),
                            fontWeight = FontWeight.Bold,
                            color = NeonTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeonBlackBg
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 🪪 1. PROFILE HEADER CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .clickable { showPhotoOptionsDialog = true }
                            .testTag("profile_avatar_box")
                    ) {
                        val photoFile = File(profile.profilePicturePath)
                        val hasPhoto = profile.profilePicturePath.isNotBlank() && photoFile.exists()

                        Surface(
                            shape = CircleShape,
                            color = NeonSurfaceHover,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (hasPhoto) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(photoFile)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = profile.userName.take(1).uppercase(),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeonTextPrimary
                                    )
                                }
                            }
                        }

                        // Edit Badge
                        Surface(
                            shape = CircleShape,
                            color = NeonWhiteSolid,
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Profile Photo",
                                    tint = NeonBlackBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = profile.userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonTextPrimary
                    )
                    Text(
                        text = "Hisab Tracker User • ${profile.userEmail}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald500.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PRO MEMBER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald500,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = { isEditing = !isEditing },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = NeonTextPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEditing) "Close Edit" else "✎ Edit Profile",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonTextPrimary
                            )
                        }
                    }
                }
            }

            // 👤 2. BASIC INFORMATION
            ProfileSectionHeader(
                title = if (isBangla) "ব্যক্তিগত তথ্য (Basic Information)" else "Basic Information",
                icon = Icons.Default.Badge
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Full Name", color = NeonTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonTextPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                        )
                        OutlinedTextField(
                            value = nicknameText,
                            onValueChange = { nicknameText = it },
                            label = { Text("Nickname", color = NeonTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Face, contentDescription = null, tint = NeonTextPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                        )
                        OutlinedTextField(
                            value = mobileText,
                            onValueChange = { mobileText = it },
                            label = { Text("Mobile Number", color = NeonTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NeonTextPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                        )
                        OutlinedTextField(
                            value = emailText,
                            onValueChange = { emailText = it },
                            label = { Text("Email Address", color = NeonTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonTextPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                        )
                        OutlinedTextField(
                            value = addressText,
                            onValueChange = { addressText = it },
                            label = { Text("Address", color = NeonTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = NeonTextPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                        )
                    } else {
                        ProfileInfoItem(icon = Icons.Default.Person, label = "Full Name", value = profile.userName)
                        ProfileInfoItem(icon = Icons.Default.Face, label = "Nickname", value = profile.nickname)
                        ProfileInfoItem(icon = Icons.Default.Phone, label = "Mobile Number", value = profile.mobileNumber)
                        ProfileInfoItem(icon = Icons.Default.Email, label = "Email", value = profile.userEmail)
                        ProfileInfoItem(icon = Icons.Default.LocationOn, label = "Address", value = profile.address)
                        ProfileInfoItem(icon = Icons.Default.Public, label = "Country", value = profile.country)
                    }
                }
            }

            // 💰 3. FINANCIAL PREFERENCES
            ProfileSectionHeader(
                title = if (isBangla) "আর্থিক পছন্দসমূহ (Financial Preferences)" else "Financial Preferences",
                icon = Icons.Default.AccountBalance
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = incomeTargetText,
                            onValueChange = { incomeTargetText = it },
                            label = { Text("Monthly Income Target (${profile.currencySymbol})", color = NeonTextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                        )
                        OutlinedTextField(
                            value = expenseLimitText,
                            onValueChange = { expenseLimitText = it },
                            label = { Text("Expense Limit (${profile.currencySymbol})", color = NeonTextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeonTextPrimary, unfocusedTextColor = NeonTextPrimary)
                        )
                    } else {
                        ProfileInfoItem(icon = Icons.Default.AttachMoney, label = "Default Currency", value = "${profile.currencySymbol} BDT")
                        ProfileInfoItem(icon = Icons.Default.AccountBalanceWallet, label = "Default Account", value = profile.defaultAccount)
                        ProfileInfoItem(icon = Icons.Default.CreditCard, label = "Default Payment Method", value = profile.defaultPaymentMethod)
                        ProfileInfoItem(icon = Icons.Default.TrendingUp, label = "Monthly Income Target", value = "${profile.currencySymbol}${profile.monthlyIncomeTarget.toInt()}")
                        ProfileInfoItem(icon = Icons.Default.TrendingDown, label = "Monthly Expense Limit", value = "${profile.currencySymbol}${profile.monthlyExpenseLimit.toInt()}")
                    }
                }
            }

            // Save Changes Button
            if (isEditing) {
                Button(
                    onClick = {
                        viewModel.updateUserProfile(
                            name = nameText,
                            nickname = nicknameText,
                            mobileNumber = mobileText,
                            email = emailText,
                            address = addressText,
                            country = countryText,
                            timezone = timezoneText,
                            bio = bioText,
                            incomeTarget = incomeTargetText.toDoubleOrNull() ?: 50000.0,
                            expenseLimit = expenseLimitText.toDoubleOrNull() ?: 35000.0,
                            savingsTarget = savingsTargetText.toDoubleOrNull() ?: 15000.0,
                            savingsPercent = savingsPercentText.toDoubleOrNull() ?: 20.0,
                            currency = currencyText,
                            language = profile.appLanguage,
                            theme = profile.themeMode,
                            defaultAccount = defaultAccountText,
                            defaultPaymentMethod = defaultPaymentMethodText,
                            budgetStartDay = budgetStartDayText.toIntOrNull() ?: 1
                        )
                        isEditing = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonWhiteSolid, contentColor = NeonBlackBg)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "পরিবর্তনগুলো সংরক্ষণ করুন" else "Save Profile Changes", fontWeight = FontWeight.Bold)
                }
            }

            // ⚙️ 4. APP PREFERENCES
            ProfileSectionHeader(
                title = if (isBangla) "অ্যাপ পছন্দসমূহ (App Preferences)" else "App Preferences",
                icon = Icons.Default.Tune
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Language Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(LocaleStrings.get("language", isBangla), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NeonTextPrimary)
                            Text("বাংলা / English", fontSize = 12.sp, color = NeonTextMuted)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = profile.appLanguage == "BN",
                                onClick = { viewModel.setAppLanguage("BN") },
                                label = { Text("বাংলা", fontWeight = FontWeight.Bold, color = NeonTextPrimary) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = NeonSurfaceHover)
                            )
                            FilterChip(
                                selected = profile.appLanguage == "EN",
                                onClick = { viewModel.setAppLanguage("EN") },
                                label = { Text("English", fontWeight = FontWeight.Bold, color = NeonTextPrimary) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = NeonSurfaceHover)
                            )
                        }
                    }

                    HorizontalDivider(color = NeonBorder)

                    ProfileToggleItem(
                        icon = Icons.Default.Notifications,
                        label = if (isBangla) "নোটিফিকেশন (Notifications)" else "Notifications",
                        checked = profile.notificationEnabled,
                        onCheckedChange = { viewModel.toggleProfilePreference("notification", it) }
                    )
                }
            }

            // 🔐 5. SECURITY & ACCESS
            ProfileSectionHeader(
                title = if (isBangla) "নিরাপত্তা ও সিকিউরিটি (Security)" else "Security & Access",
                icon = Icons.Default.Security
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NeonSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileToggleItem(
                        icon = Icons.Default.Fingerprint,
                        label = if (isBangla) "বায়োমেট্রিক সিকিউরিটি (Fingerprint & Face ID)" else "Biometric Lock (Fingerprint & Face)",
                        checked = profile.biometricLockEnabled,
                        onCheckedChange = { viewModel.toggleProfilePreference("biometric_lock", it) }
                    )

                    ProfileToggleItem(
                        icon = Icons.Default.Pin,
                        label = if (isBangla) "পিন লক (PIN Lock)" else "PIN Lock Protection",
                        checked = profile.pinLockEnabled,
                        onCheckedChange = { viewModel.toggleProfilePreference("pin_lock", it) }
                    )

                    HorizontalDivider(color = NeonBorder)

                    ProfileClickableRow(
                        icon = Icons.Default.Lock,
                        label = if (isBangla) "এখনই অ্যাপ লক করুন" else "Lock App Now",
                        subtitle = if (isBangla) "তাৎক্ষণিক বায়োমেট্রিক স্ক্রিন লক" else "Immediate biometric security lock",
                        onClick = { viewModel.lockApp() }
                    )

                    ProfileClickableRow(
                        icon = Icons.Default.FileDownload,
                        label = if (isBangla) "ডাটা ব্যাকআপ ও এক্সপোর্ট" else "Data Backup & Export",
                        onClick = { viewModel.backupData() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Photo Dialog
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = { Text(LocaleStrings.get("select_photo_title", isBangla), fontWeight = FontWeight.Bold, color = NeonTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonSurfaceHover,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoOptionsDialog = false
                                val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) cameraLauncher.launch() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = NeonTextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(LocaleStrings.get("take_photo", isBangla), fontWeight = FontWeight.SemiBold, color = NeonTextPrimary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonSurfaceHover,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoOptionsDialog = false
                                galleryLauncher.launch("image/*")
                            }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonTextPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(LocaleStrings.get("choose_gallery", isBangla), fontWeight = FontWeight.SemiBold, color = NeonTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
                    Text(LocaleStrings.get("cancel", isBangla), color = NeonTextPrimary)
                }
            },
            containerColor = NeonSurfaceCard
        )
    }
}

@Composable
fun ProfileSectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonTextPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = NeonTextPrimary
        )
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonTextMuted, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = NeonTextMuted)
            Text(text = value.ifBlank { "Not specified" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = NeonTextPrimary)
        }
    }
}

@Composable
fun ProfileToggleItem(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = NeonTextPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = NeonTextPrimary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonBlackBg,
                checkedTrackColor = NeonWhiteSolid,
                uncheckedThumbColor = NeonTextMuted,
                uncheckedTrackColor = NeonSurfaceHover
            )
        )
    }
}

@Composable
fun ProfileClickableRow(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    tint: Color = NeonTextPrimary,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = tint)
                    if (subtitle != null) {
                        Text(text = subtitle, fontSize = 11.sp, color = NeonTextMuted)
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NeonTextMuted, modifier = Modifier.size(18.dp))
        }
    }
}
