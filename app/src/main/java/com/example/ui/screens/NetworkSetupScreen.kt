package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.network.HotspotManager
import com.example.ui.components.AnimatedGradientProgressBar
import com.example.ui.components.InAirCard
import com.example.ui.components.InAirGradientButton
import com.example.ui.theme.InAirBackground
import com.example.ui.theme.InAirPrimary
import com.example.ui.theme.InAirSecondary
import com.example.ui.theme.InAirSurfaceVariant
import com.example.viewmodel.InAirViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSetupScreen(
    viewModel: InAirViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.networkProfile.collectAsState()
    val isHotspot by viewModel.isHotspotActive.collectAsState()

    var customSsid by remember { mutableStateOf(profile.ssid) }
    var customPass by remember { mutableStateOf(profile.passphrase) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Password strength logic
    val passLength = customPass.length
    val hasUpper = customPass.any { it.isUpperCase() }
    val hasLower = customPass.any { it.isLowerCase() }
    val hasDigit = customPass.any { it.isDigit() }
    val hasSpecial = customPass.any { !it.isLetterOrDigit() }

    val strengthFraction: Float
    val strengthTextRes: Int
    val strengthColor: Color

    when {
        passLength < 8 -> {
            strengthFraction = (passLength / 8f) * 0.33f
            strengthTextRes = R.string.password_strength_weak
            strengthColor = Color(0xFFFF2D3F)
        }
        passLength >= 10 && ((hasUpper && hasLower) || hasSpecial) && hasDigit -> {
            strengthFraction = 1f
            strengthTextRes = R.string.password_strength_strong
            strengthColor = Color(0xFF4CAF50)
        }
        else -> {
            strengthFraction = 0.66f
            strengthTextRes = R.string.password_strength_medium
            strengthColor = Color(0xFFFF9800)
        }
    }

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
            // Top Bar
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
                        text = stringResource(R.string.network_setup_title),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.network_setup_subtitle),
                        color = InAirSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Automatic Local-Only Hotspot Card
            InAirCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FF2D3F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiTethering,
                            contentDescription = null,
                            tint = InAirPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.hotspot_auto_managed),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.hotspot_auto_managed_desc),
                    color = Color(0xFFD4A5AB),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                InAirGradientButton(
                    text = if (isHotspot) "Hotspot Running" else "Start Local Hotspot",
                    onClick = {
                        viewModel.startLocalHotspot()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Manual Credentials Setup Card
            InAirCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FF2D3F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = InAirPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Manual Hotspot / Wi-Fi",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom SSID
                OutlinedTextField(
                    value = customSsid,
                    onValueChange = { customSsid = it },
                    label = { Text(stringResource(R.string.manual_ssid_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InAirPrimary,
                        unfocusedBorderColor = Color(0x33FF2D3F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = InAirSecondary,
                        unfocusedLabelColor = Color(0xFFD4A5AB),
                        cursorColor = InAirPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Password
                OutlinedTextField(
                    value = customPass,
                    onValueChange = { customPass = it },
                    label = { Text(stringResource(R.string.manual_password_label)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Password Visibility",
                                tint = InAirSecondary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InAirPrimary,
                        unfocusedBorderColor = Color(0x33FF2D3F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = InAirSecondary,
                        unfocusedLabelColor = Color(0xFFD4A5AB),
                        cursorColor = InAirPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Strength Meter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.password_strength_label)}: ",
                        color = Color(0xFFD4A5AB),
                        fontSize = 12.sp
                    )
                    Text(
                        text = stringResource(strengthTextRes),
                        color = strengthColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                AnimatedGradientProgressBar(progressFraction = strengthFraction)

                Spacer(modifier = Modifier.height(18.dp))

                InAirGradientButton(
                    text = "Apply Credentials",
                    enabled = customPass.length >= 8,
                    onClick = {
                        viewModel.setManualHotspot(customSsid, customPass)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Open System Hotspot Settings Card
            InAirCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = InAirSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.open_system_hotspot_settings),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                InAirGradientButton(
                    text = stringResource(R.string.open_system_hotspot_settings),
                    onClick = {
                        HotspotManager.openSystemHotspotSettings(context)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
