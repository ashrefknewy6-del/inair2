package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.BroadcastStatus
import com.example.ui.components.InAirCard
import com.example.ui.components.InAirGradientButton
import com.example.ui.components.PulsingPowerButton
import com.example.ui.components.StatusChip
import com.example.ui.theme.InAirBackground
import com.example.ui.theme.InAirCardBorder
import com.example.ui.theme.InAirPrimary
import com.example.ui.theme.InAirSecondary
import com.example.ui.theme.InAirSurfaceVariant
import com.example.util.QRCodeGenerator
import com.example.viewmodel.InAirViewModel

@Composable
fun HomeScreen(
    viewModel: InAirViewModel,
    onRequestMediaProjection: () -> Unit,
    onNavigateToNetworkSetup: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val status by viewModel.broadcastStatus.collectAsState()
    val profile by viewModel.networkProfile.collectAsState()
    val connectedClients by viewModel.connectedClients.collectAsState()
    val isHotspot by viewModel.isHotspotActive.collectAsState()

    var showQrDialog by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(profile.url) {
        qrBitmap = QRCodeGenerator.generateQRCode(profile.url)
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = stringResource(R.string.tagline),
                        color = InAirSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(InAirSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = InAirSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Chip
            StatusChip(status = status)

            Spacer(modifier = Modifier.height(24.dp))

            // Central Power Button
            PulsingPowerButton(
                status = status,
                onClick = {
                    viewModel.toggleBroadcast(onRequestMediaProjection)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Connection Address Card
            InAirCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FF2D3F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isHotspot) Icons.Default.WifiTethering else Icons.Default.Wifi,
                                contentDescription = null,
                                tint = InAirPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.network_card_title),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // QR Code icon button
                    IconButton(
                        onClick = { showQrDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FF2D3F))
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Show QR Code",
                            tint = InAirSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Web Address (IP:Port)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14080A))
                        .border(1.dp, InAirCardBorder, RoundedCornerShape(16.dp))
                        .clickable { copyToClipboard("InAir URL", profile.url) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.web_address),
                            color = Color(0xFFD4A5AB),
                            fontSize = 11.sp
                        )
                        Text(
                            text = profile.url,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy URL",
                        tint = InAirSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Hotspot Details if active
                if (profile.ssid.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF14080A))
                            .border(1.dp, InAirCardBorder, RoundedCornerShape(16.dp))
                            .clickable { copyToClipboard("Hotspot Password", profile.passphrase) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${stringResource(R.string.hotspot_name)}: ${profile.ssid}",
                                color = Color(0xFFD4A5AB),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${stringResource(R.string.hotspot_password)}: ${profile.passphrase.ifEmpty { "None" }}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (profile.passphrase.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Password",
                                tint = InAirSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connected Devices Counter Card
            InAirCard(
                modifier = Modifier.clickable { onNavigateToDevices() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FF2D3F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = InAirPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.devices_title),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (connectedClients.isNotEmpty()) {
                                    stringResource(R.string.connected_devices_count, connectedClients.size)
                                } else {
                                    stringResource(R.string.no_devices_connected)
                                },
                                color = InAirSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "›",
                        color = InAirSecondary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Network Setup button
            InAirGradientButton(
                text = stringResource(R.string.network_setup_title),
                onClick = onNavigateToNetworkSetup,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Full QR Code Dialog
        if (showQrDialog) {
            Dialog(onDismissRequest = { showQrDialog = false }) {
                InAirCard {
                    Text(
                        text = stringResource(R.string.web_address),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.qr_code_hint),
                        color = Color(0xFFD4A5AB),
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    qrBitmap?.let { bmp ->
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Connection QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    InAirGradientButton(
                        text = stringResource(R.string.btn_cancel),
                        onClick = { showQrDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
