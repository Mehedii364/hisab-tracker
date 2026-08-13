package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.preferences.AiSettingsManager
import com.example.ui.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HisabViewModel
) {
    val aiSettings by viewModel.aiSettings.collectAsState()

    var openRouterKey by remember(aiSettings) { mutableStateOf(aiSettings.openRouterApiKey) }
    var geminiKey by remember(aiSettings) { mutableStateOf(aiSettings.geminiApiKey) }
    var primaryProvider by remember(aiSettings) { mutableStateOf(aiSettings.primaryProvider) }
    var fallbackProvider by remember(aiSettings) { mutableStateOf(aiSettings.fallbackProvider) }
    var openRouterModel by remember(aiSettings) { mutableStateOf(aiSettings.openRouterModel) }
    var geminiModel by remember(aiSettings) { mutableStateOf(aiSettings.geminiModel) }
    var temperature by remember(aiSettings) { mutableStateOf(aiSettings.temperature) }
    var maxTokensText by remember(aiSettings) { mutableStateOf(aiSettings.maxTokens.toString()) }
    var autoFailover by remember(aiSettings) { mutableStateOf(aiSettings.autoFailover) }
    var aiEnabled by remember(aiSettings) { mutableStateOf(aiSettings.aiEnabled) }
    var systemPrompt by remember(aiSettings) { mutableStateOf(aiSettings.systemPrompt) }

    var showKeyVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & AI Configuration", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General AI Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable AI Assistant", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Process natural language transactions in Bangla/English", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = aiEnabled,
                            onCheckedChange = { aiEnabled = it },
                            modifier = Modifier.testTag("enable_ai_switch")
                        )
                    }
                }
            }

            // OpenRouter API Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OpenRouter Configuration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        OutlinedTextField(
                            value = openRouterKey,
                            onValueChange = { openRouterKey = it },
                            label = { Text("OpenRouter API Key") },
                            placeholder = { Text("sk-or-v1-...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("openrouter_key_input"),
                            visualTransformation = if (showKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showKeyVisible = !showKeyVisible }) {
                                    Icon(
                                        imageVector = if (showKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Key Visibility"
                                    )
                                }
                            },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = openRouterModel,
                            onValueChange = { openRouterModel = it },
                            label = { Text("OpenRouter Model") },
                            placeholder = { Text("google/gemini-2.5-flash") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Gemini Native API Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini Native Configuration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        OutlinedTextField(
                            value = geminiKey,
                            onValueChange = { geminiKey = it },
                            label = { Text("Gemini API Key") },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gemini_key_input"),
                            visualTransformation = if (showKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = geminiModel,
                            onValueChange = { geminiModel = it },
                            label = { Text("Gemini Model") },
                            placeholder = { Text("gemini-3.5-flash") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Failover & Strategy
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Provider Strategy & Auto Failover", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = primaryProvider == "OpenRouter",
                                onClick = {
                                    primaryProvider = "OpenRouter"
                                    fallbackProvider = "Gemini"
                                },
                                label = { Text("Primary: OpenRouter") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = primaryProvider == "Gemini",
                                onClick = {
                                    primaryProvider = "Gemini"
                                    fallbackProvider = "OpenRouter"
                                },
                                label = { Text("Primary: Gemini") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Auto Failover", fontWeight = FontWeight.SemiBold)
                                Text("Automatically switch to fallback provider on error", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = autoFailover,
                                onCheckedChange = { autoFailover = it },
                                modifier = Modifier.testTag("auto_failover_switch")
                            )
                        }
                    }
                }
            }

            // Hyperparameters & System Prompt
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("LLM Parameters & Custom Prompt", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        Text("Temperature: ${String.format("%.2f", temperature)}")
                        Slider(
                            value = temperature,
                            onValueChange = { temperature = it },
                            valueRange = 0.0f..1.0f
                        )

                        OutlinedTextField(
                            value = maxTokensText,
                            onValueChange = { maxTokensText = it },
                            label = { Text("Max Tokens") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = systemPrompt,
                            onValueChange = { systemPrompt = it },
                            label = { Text("System Prompt Instructions") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 5
                        )

                        TextButton(onClick = { systemPrompt = AiSettingsManager.DEFAULT_SYSTEM_PROMPT }) {
                            Text("Reset System Prompt to Default")
                        }
                    }
                }
            }

            // Save Settings Button
            item {
                Button(
                    onClick = {
                        val tokens = maxTokensText.toIntOrNull() ?: 2048
                        viewModel.updateAiSettings(
                            openRouterKey = openRouterKey,
                            geminiKey = geminiKey,
                            primary = primaryProvider,
                            fallback = fallbackProvider,
                            openRouterModel = openRouterModel,
                            geminiModel = geminiModel,
                            temperature = temperature,
                            maxTokens = tokens,
                            autoFailover = autoFailover,
                            aiEnabled = aiEnabled,
                            systemPrompt = systemPrompt
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_settings_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save All Settings", fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
