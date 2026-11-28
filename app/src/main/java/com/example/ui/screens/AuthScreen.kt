package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfileEntity
import com.example.ui.components.BharatTubeLogoMark
import com.example.ui.components.ChannelLogoAvatar
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    allUsers: List<UserProfileEntity>,
    googleLoginEnabled: Boolean = true,
    onBack: () -> Unit,
    onGoogleSignIn: (name: String, email: String) -> Unit,
    onRequestOtp: (identifier: String, onOtpReady: (String) -> Unit) -> Unit,
    onVerifyOtpLogin: (phone: String, otp: String, displayName: String) -> Unit,
    onEmailLogin: (email: String, password: String) -> Unit,
    onEmailRegister: (name: String, email: String, password: String) -> Unit,
    onResetPassword: (identifier: String, otp: String, newPassword: String) -> Unit,
    onSwitchExistingUser: (String) -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Google, 1=Mobile OTP, 2=Email, 3=Forgot Password

    // Google form state
    var googleName by remember { mutableStateOf("") }
    var googleEmail by remember { mutableStateOf("") }

    // Mobile OTP state
    var phoneNumber by remember { mutableStateOf("") }
    var otpDisplayName by remember { mutableStateOf("") }
    var phoneOtpInput by remember { mutableStateOf("") }
    var generatedPhoneOtp by remember { mutableStateOf<String?>(null) }

    // Email Login / Sign Up state
    var isEmailSignUp by remember { mutableStateOf(false) }
    var emailName by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Forgot Password state
    var resetIdentifier by remember { mutableStateOf("") }
    var resetOtpInput by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }
    var generatedResetOtp by remember { mutableStateOf<String?>(null) }

    val authTabs = listOf("Google", "Mobile OTP", "Email", "Forgot Password")

    Scaffold(
        containerColor = Color(0xFF0E1016),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BharatTubeLogoMark(modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("BharatTube Account", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("auth_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1016))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF131620),
                contentColor = Color.White,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = SaffronFlame,
                            height = 2.5.dp
                        )
                    }
                }
            ) {
                authTabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == idx) SaffronFlame else Color(0xFFA0A5B8),
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("auth_tab_$idx")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF151822),
                        border = BorderStroke(1.dp, Color(0xFF252938)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SaffronFlame.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                BharatTubeLogoMark(modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sign in to BharatTube",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Upload Videos & Shorts, Go Live, Comment, Like & Earn in Creator Studio.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA0A5B8)
                                )
                            }
                        }
                    }
                }

                when (selectedTab) {
                    // 0. GOOGLE LOGIN
                    0 -> {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF151822),
                                border = BorderStroke(1.dp, Color(0xFF252938)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "Continue with Google",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Enter your Google account details to create or link your BharatTube profile.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFA0A5B8)
                                    )
                                    OutlinedTextField(
                                        value = googleName,
                                        onValueChange = { googleName = it },
                                        label = { Text("Full Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("auth_google_name")
                                    )
                                    OutlinedTextField(
                                        value = googleEmail,
                                        onValueChange = { googleEmail = it },
                                        label = { Text("Google Email (e.g. name@gmail.com)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("auth_google_email")
                                    )
                                    Button(
                                        onClick = { onGoogleSignIn(googleName, googleEmail) },
                                        enabled = googleLoginEnabled && googleEmail.contains("@"),
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("auth_google_submit")
                                    ) {
                                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 1. MOBILE NUMBER + OTP LOGIN
                    1 -> {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF151822),
                                border = BorderStroke(1.dp, Color(0xFF252938)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = SaffronFlame)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Mobile Number + OTP Login", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it.filter { c -> c.isDigit() || c == '+' }.take(13) },
                                        label = { Text("Mobile Number (10 digits)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("auth_phone_input")
                                    )
                                    OutlinedTextField(
                                        value = otpDisplayName,
                                        onValueChange = { otpDisplayName = it },
                                        label = { Text("Your Name (for new account)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("auth_phone_name")
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            onRequestOtp(phoneNumber) { otp ->
                                                generatedPhoneOtp = otp
                                            }
                                        },
                                        enabled = phoneNumber.filter { it.isDigit() }.length >= 10,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_send_phone_otp")
                                    ) {
                                        Text("Send 6-Digit OTP", color = SaffronFlame, fontWeight = FontWeight.Bold)
                                    }
                                    generatedPhoneOtp?.let { code ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = EmeraldVerified.copy(alpha = 0.14f),
                                            border = BorderStroke(1.dp, EmeraldVerified.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Verification Code Sent: $code (Valid 5 mins)",
                                                color = EmeraldVerified,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                    OutlinedTextField(
                                        value = phoneOtpInput,
                                        onValueChange = { phoneOtpInput = it.filter { c -> c.isDigit() }.take(6) },
                                        label = { Text("Enter 6-Digit OTP") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("auth_phone_otp_input")
                                    )
                                    Button(
                                        onClick = { onVerifyOtpLogin(phoneNumber, phoneOtpInput, otpDisplayName) },
                                        enabled = phoneOtpInput.length == 6,
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("auth_verify_phone_otp")
                                    ) {
                                        Text("Verify OTP & Sign In", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 2. EMAIL SIGN UP / EMAIL LOGIN
                    2 -> {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF151822),
                                border = BorderStroke(1.dp, Color(0xFF252938)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Email, contentDescription = null, tint = SaffronFlame)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            if (isEmailSignUp) "Create Email Account" else "Email Login",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = !isEmailSignUp,
                                            onClick = { isEmailSignUp = false },
                                            label = { Text("Sign In") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = SaffronFlame,
                                                selectedLabelColor = Color.Black
                                            ),
                                            modifier = Modifier.testTag("auth_email_mode_login")
                                        )
                                        FilterChip(
                                            selected = isEmailSignUp,
                                            onClick = { isEmailSignUp = true },
                                            label = { Text("Create Account") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = SaffronFlame,
                                                selectedLabelColor = Color.Black
                                            ),
                                            modifier = Modifier.testTag("auth_email_mode_signup")
                                        )
                                    }
                                    if (isEmailSignUp) {
                                        OutlinedTextField(
                                            value = emailName,
                                            onValueChange = { emailName = it },
                                            label = { Text("Full Name *") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("auth_email_name")
                                        )
                                    }
                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        label = { Text("Email Address *") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                                    )
                                    OutlinedTextField(
                                        value = passwordInput,
                                        onValueChange = { passwordInput = it },
                                        label = { Text("Password (min 6 characters) *") },
                                        singleLine = true,
                                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { showPassword = !showPassword }) {
                                                Icon(
                                                    if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                                    contentDescription = "Toggle password"
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("auth_email_password")
                                    )
                                    if (!isEmailSignUp) {
                                        TextButton(
                                            onClick = {
                                                resetIdentifier = emailInput
                                                selectedTab = 3
                                            },
                                            modifier = Modifier.align(Alignment.End).testTag("auth_forgot_password_link")
                                        ) {
                                            Text("Forgot Password?", color = SaffronFlame)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            if (isEmailSignUp) {
                                                onEmailRegister(emailName, emailInput, passwordInput)
                                            } else {
                                                onEmailLogin(emailInput, passwordInput)
                                            }
                                        },
                                        enabled = emailInput.contains("@") && passwordInput.length >= 6 && (!isEmailSignUp || emailName.isNotBlank()),
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("auth_email_submit")
                                    ) {
                                        Text(
                                            text = if (isEmailSignUp) "Sign Up & Create Channel" else "Sign In with Email",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. FORGOT PASSWORD (OTP RESET)
                    else -> {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF151822),
                                border = BorderStroke(1.dp, Color(0xFF252938)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.LockReset, contentDescription = null, tint = SaffronFlame)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Reset Password via OTP", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    OutlinedTextField(
                                        value = resetIdentifier,
                                        onValueChange = { resetIdentifier = it },
                                        label = { Text("Registered Email or Mobile Number") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("auth_reset_identifier")
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            onRequestOtp(resetIdentifier) { code ->
                                                generatedResetOtp = code
                                            }
                                        },
                                        enabled = resetIdentifier.isNotBlank(),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_send_reset_otp")
                                    ) {
                                        Text("Send Reset OTP", color = SaffronFlame, fontWeight = FontWeight.Bold)
                                    }
                                    generatedResetOtp?.let { code ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = EmeraldVerified.copy(alpha = 0.14f),
                                            border = BorderStroke(1.dp, EmeraldVerified.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Reset OTP: $code",
                                                color = EmeraldVerified,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                    OutlinedTextField(
                                        value = resetOtpInput,
                                        onValueChange = { resetOtpInput = it.filter { c -> c.isDigit() }.take(6) },
                                        label = { Text("6-Digit OTP") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("auth_reset_otp_input")
                                    )
                                    OutlinedTextField(
                                        value = resetNewPassword,
                                        onValueChange = { resetNewPassword = it },
                                        label = { Text("New Password (min 6 characters)") },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_reset_new_password")
                                    )
                                    Button(
                                        onClick = { onResetPassword(resetIdentifier, resetOtpInput, resetNewPassword) },
                                        enabled = resetIdentifier.isNotBlank() && resetOtpInput.length == 6 && resetNewPassword.length >= 6,
                                        colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("auth_reset_submit")
                                    ) {
                                        Text("Update Password & Sign In", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Switch from Saved Device Profiles
                if (allUsers.isNotEmpty()) {
                    item {
                        HorizontalDivider(color = Color(0xFF252938))
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Saved Profiles on This Device",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    items(allUsers, key = { it.uid }) { profile ->
                        Surface(
                            onClick = { onSwitchExistingUser(profile.uid) },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF151822),
                            border = BorderStroke(1.dp, Color(0xFF252938)),
                            modifier = Modifier.fillMaxWidth().testTag("auth_quick_user_${profile.uid}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChannelLogoAvatar(
                                    avatarUri = profile.avatarUri,
                                    contentDescription = profile.username,
                                    size = 42.dp,
                                    borderWidth = 1.5.dp,
                                    borderColor = SaffronFlame
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.username,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${profile.handle} • ${profile.email.ifBlank { profile.phone }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFA0A5B8)
                                    )
                                }
                                Text("Continue", color = SaffronFlame, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Terms & Privacy links
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Terms of Service",
                            color = SaffronFlame,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.clickable(onClick = onOpenTerms).padding(8.dp)
                        )
                        Text("•", color = Color(0xFF6B7185), textAlign = TextAlign.Center)
                        Text(
                            text = "Privacy Policy",
                            color = SaffronFlame,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.clickable(onClick = onOpenPrivacy).padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
