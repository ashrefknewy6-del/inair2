package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.components.InAirCard
import com.example.ui.components.InAirGradientButton
import com.example.ui.theme.InAirBackground
import com.example.ui.theme.InAirPrimary
import com.example.ui.theme.InAirSecondary
import com.example.ui.theme.InAirSurfaceVariant
import com.example.viewmodel.InAirViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: InAirViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
) {
    val prefs by viewModel.securityPreferences.collectAsState()
    val currentLang by viewModel.language.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var tempPin by remember { mutableStateOf(prefs.pinCode) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InAirBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(InAirSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_title),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.settings_subtitle),
                        color = InAirSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security Section
            Text(
                text = stringResource(R.string.settings_security_section),
                color = InAirSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            InAirCard {
                // Require Device Approval
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.setting_require_approval),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.setting_require_approval_desc),
                            color = Color(0xFFD4A5AB),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = prefs.requireDeviceApproval,
                        onCheckedChange = { checked ->
                            viewModel.updateSecurityPreferences(prefs.copy(requireDeviceApproval = checked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = InAirPrimary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = InAirSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PIN Protection Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.setting_pin_protection),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.setting_pin_protection_desc),
                            color = Color(0xFFD4A5AB),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = prefs.isPinProtectionEnabled,
                        onCheckedChange = { checked ->
                            viewModel.updateSecurityPreferences(prefs.copy(isPinProtectionEnabled = checked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = InAirPrimary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = InAirSurfaceVariant
                        )
                    )
                }

                if (prefs.isPinProtectionEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.current_pin_label, prefs.pinCode),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = {
                            tempPin = prefs.pinCode
                            showPinDialog = true
                        }) {
                            Text(stringResource(R.string.setting_change_pin), color = InAirSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Auto-Stop Timer Section
            Text(
                text = stringResource(R.string.setting_auto_stop),
                color = InAirSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            InAirCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = InAirPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.setting_auto_stop),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.setting_auto_stop_desc),
                            color = Color(0xFFD4A5AB),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val options = listOf(
                    0 to stringResource(R.string.timer_never),
                    15 to stringResource(R.string.timer_15m),
                    30 to stringResource(R.string.timer_30m),
                    60 to stringResource(R.string.timer_1h)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { (mins, label) ->
                        val isSelected = prefs.autoStopMinutes == mins
                        TextButton(
                            onClick = {
                                viewModel.updateSecurityPreferences(prefs.copy(autoStopMinutes = mins))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) InAirPrimary else InAirSurfaceVariant)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFFD4A5AB),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Language Section
            Text(
                text = stringResource(R.string.settings_appearance_section),
                color = InAirSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            InAirCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = InAirPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.setting_language),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { viewModel.setLanguage("en") },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (currentLang == "en") InAirPrimary else InAirSurfaceVariant)
                        ) {
                            Text(
                                text = "English",
                                color = if (currentLang == "en") Color.White else Color(0xFFD4A5AB),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(
                            onClick = { viewModel.setLanguage("ar") },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (currentLang == "ar") InAirPrimary else InAirSurfaceVariant)
                        ) {
                            Text(
                                text = "العربية",
                                color = if (currentLang == "ar") Color.White else Color(0xFFD4A5AB),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // About Section
            Text(
                text = stringResource(R.string.settings_about_section),
                color = InAirSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            InAirCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = InAirPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.about_version),
                            color = InAirSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.about_desc),
                    color = Color(0xFFD4A5AB),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                InAirGradientButton(
                    text = stringResource(R.string.btn_privacy_policy),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = onNavigateToPrivacyPolicy,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Change PIN Modal
        if (showPinDialog) {
            Dialog(onDismissRequest = { showPinDialog = false }) {
                InAirCard {
                    Text(
                        text = stringResource(R.string.setting_change_pin),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = tempPin,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) tempPin = it },
                        label = { Text("4-digit PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InAirPrimary,
                            unfocusedBorderColor = Color(0x33FF2D3F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = InAirSecondary,
                            unfocusedLabelColor = Color(0xFFD4A5AB),
                            cursorColor = InAirPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPinDialog = false }) {
                            Text(stringResource(R.string.btn_cancel), color = Color(0xFFD4A5AB))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        InAirGradientButton(
                            text = "Save",
                            enabled = tempPin.length >= 4,
                            onClick = {
                                viewModel.updateSecurityPreferences(prefs.copy(pinCode = tempPin))
                                showPinDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}
