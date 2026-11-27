package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.chhotuai.SystemHealthSnapshot
import com.example.data.model.ChhotuChatMessageEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.LotusCoral
import com.example.ui.theme.SaffronFlame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChhotuAiAssistantScreen(
    user: UserProfileEntity?,
    settings: PlatformSettingsEntity,
    messages: List<ChhotuChatMessageEntity>,
    healthSnapshot: SystemHealthSnapshot?,
    isBusy: Boolean,
    defaultLanguage: String,
    onBack: () -> Unit,
    onOpenAdminControlCenter: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    onClearHistory: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedLang by remember(defaultLanguage) {
        mutableStateOf(if (defaultLanguage == "hi") "hi" else "en")
    }
    var inputText by remember { mutableStateOf("") }
    var autoSpeakReplies by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var ttsReady by remember { mutableStateOf(false) }
    var lastSpokenId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    val tts = remember {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context.applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
        instance
    }

    val speakText: (String, String) -> Unit = { text, lang ->
        if (ttsReady) {
            val locale = Locale.forLanguageTag(if (lang == "hi") "hi-IN" else "en-IN")
            tts.language = locale
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "chhotu_${System.currentTimeMillis()}")
        }
    }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { tts.stop(); tts.shutdown() }
            runCatching { speechRecognizer?.destroy() }
        }
    }

    val startVoiceInput: () -> Unit = {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            onShowSnackbar("Speech recognition service is not available on this device.")
        } else {
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() { isListening = true }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) {
                    isListening = false
                    onShowSnackbar("Voice input did not detect speech. Please try again or type.")
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!spoken.isNullOrBlank()) {
                        inputText = spoken
                        onSendMessage(spoken, selectedLang)
                        inputText = ""
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                val speechLocale = if (selectedLang == "hi") "hi-IN" else "en-IN"
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocale)
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (selectedLang == "hi") "Chhotu AI से बोलकर पूछें..." else "Speak to Chhotu AI...")
            }
            runCatching {
                isListening = true
                recognizer.startListening(intent)
            }.onFailure {
                isListening = false
                onShowSnackbar("Could not start voice recognition: ${it.message}")
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceInput()
        } else {
            onShowSnackbar("Microphone permission denied. Voice input requires RECORD_AUDIO permission.")
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
            val newest = messages.last()
            if (autoSpeakReplies && newest.role == "CHHOTU_AI" && newest.id != lastSpokenId) {
                lastSpokenId = newest.id
                speakText(newest.text, newest.languageCode)
            }
        }
    }

    val backendOnline = healthSnapshot?.backendState?.isOnline == true
    val backendConfigured = healthSnapshot?.backendState?.isConfigured == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SaffronFlame.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.SmartToy, contentDescription = "Chhotu AI", tint = SaffronFlame, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Chhotu AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = when {
                                    !settings.chhotuAiEnabled -> "Paused by Admin"
                                    backendOnline -> "Existing AI Connected"
                                    backendConfigured -> "Backend Error"
                                    else -> "Backend Not Connected • App Guides Ready"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    !settings.chhotuAiEnabled -> LotusCoral
                                    backendOnline -> EmeraldVerified
                                    else -> AmberGold
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            autoSpeakReplies = !autoSpeakReplies
                            if (!autoSpeakReplies) runCatching { tts.stop() }
                        },
                        modifier = Modifier.testTag("chhotu_tts_toggle")
                    ) {
                        Icon(
                            imageVector = if (autoSpeakReplies) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Toggle Auto Voice Reply",
                            tint = if (autoSpeakReplies) SaffronFlame else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (user?.isAdmin == true) {
                        IconButton(
                            onClick = onOpenAdminControlCenter,
                            modifier = Modifier.testTag("chhotu_open_admin_center")
                        ) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = "Chhotu AI Control Center", tint = EmeraldVerified)
                        }
                    }
                    IconButton(onClick = onClearHistory) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear My Chat History")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Language + Security Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedLang == "hi",
                    onClick = { selectedLang = "hi" },
                    label = { Text("हिन्दी (HI)") },
                    modifier = Modifier.testTag("chhotu_lang_hi")
                )
                FilterChip(
                    selected = selectedLang == "en",
                    onClick = { selectedLang = "en" },
                    label = { Text("English (EN)") },
                    modifier = Modifier.testTag("chhotu_lang_en")
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldVerified.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Private History", style = MaterialTheme.typography.labelSmall, color = EmeraldVerified)
                    }
                }
            }

            // Quick Support Guide Chips
            val quickTopics = if (selectedLang == "hi") {
                listOf(
                    "Account कैसे बनाएँ?",
                    "Channel और Logo कैसे लगाएँ?",
                    "Video Upload कैसे करें?",
                    "Shorts कैसे बनाएँ?",
                    "कमाई और Payout नियम",
                    "Creator Studio कैसे देखें?",
                    "App System Status"
                )
            } else {
                listOf(
                    "How to create Account?",
                    "How to create Channel?",
                    "How to upload Video?",
                    "How to make Shorts?",
                    "Earnings & 50/50 Rules",
                    "Using Creator Studio",
                    "System Health Status"
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickTopics) { prompt ->
                    AssistChip(
                        onClick = { onSendMessage(prompt, selectedLang) },
                        label = { Text(prompt, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            // Chat Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = if (selectedLang == "hi") "नमस्ते ${user?.username ?: ""}! मैं Chhotu AI हूँ"
                                    else "Namaste ${user?.username ?: ""}! I am Chhotu AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selectedLang == "hi") {
                                        "BharatTube का Official AI Assistant. आपकी चैट हिस्ट्री केवल आपके खाते (${user?.handle ?: ""}) में सुरक्षित रहती है। नीचे माइक दबाकर बोलें या कोई भी सवाल पूछें।"
                                    } else {
                                        "BharatTube's Official AI Assistant. Your chat history is isolated to your profile (${user?.handle ?: ""}). Tap a guide above, type, or use the mic button."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!backendOnline) {
                                    Surface(
                                        color = AmberGold.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "Existing Chhotu AI Server: ${healthSnapshot?.backendState?.statusLabel ?: "Not Connected"}. Configure CHHOTU_AI_BASE_URL in AI Studio Secrets to forward chats to your existing Chhotu AI model.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AmberGold,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                                if (user?.isAdmin == true) {
                                    OutlinedButton(onClick = onOpenAdminControlCenter) {
                                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Open Chhotu AI Control Center (17-Point Check)")
                                    }
                                }
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.role == "USER"
                    val timeLabel = remember(msg.createdAt) {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.createdAt))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = when {
                                msg.sourceMode == "SECURITY_BLOCK" -> LotusCoral.copy(alpha = 0.16f)
                                isUser -> SaffronFlame
                                else -> MaterialTheme.colorScheme.surface
                            },
                            tonalElevation = if (isUser) 0.dp else 2.dp,
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!isUser) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = when (msg.sourceMode) {
                                                "EXISTING_BACKEND" -> "Chhotu AI • Server"
                                                "SECURITY_BLOCK" -> "Chhotu AI • Security Policy"
                                                else -> "Chhotu AI • App Guide"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (msg.sourceMode == "SECURITY_BLOCK") LotusCoral else SaffronFlame
                                        )
                                        IconButton(
                                            onClick = { speakText(msg.text, msg.languageCode) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Listen to Chhotu AI response",
                                                modifier = Modifier.size(16.dp),
                                                tint = SaffronFlame
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = msg.text,
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = timeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isUser) Color.White.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (isBusy) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = SaffronFlame)
                            Text("Chhotu AI is checking...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (isListening) {
                Surface(
                    color = SaffronFlame.copy(alpha = 0.14f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = SaffronFlame)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (selectedLang == "hi") "सुन रहा हूँ... हिन्दी या अंग्रेज़ी में बोलें" else "Listening... Speak in Hindi or English",
                            style = MaterialTheme.typography.labelMedium,
                            color = SaffronFlame
                        )
                    }
                }
            }

            // Input Row with Voice Mic + Text + Send
            Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledIconButton(
                        onClick = {
                            if (isListening) {
                                runCatching { speechRecognizer?.stopListening() }
                                isListening = false
                            } else {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    startVoiceInput()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isListening) LotusCoral else SaffronFlame.copy(alpha = 0.16f),
                            contentColor = if (isListening) Color.White else SaffronFlame
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chhotu_voice_input_button")
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Voice Input to Chhotu AI")
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                if (selectedLang == "hi") "Chhotu AI से पूछें..." else "Ask Chhotu AI...",
                                maxLines = 1
                            )
                        },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chhotu_chat_input")
                    )

                    FilledIconButton(
                        onClick = {
                            val msg = inputText.trim()
                            if (msg.isNotEmpty()) {
                                inputText = ""
                                onSendMessage(msg, selectedLang)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isBusy && settings.chhotuAiEnabled,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SaffronFlame),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chhotu_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send to Chhotu AI")
                    }
                }
            }
        }
    }
}
