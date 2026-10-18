package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.InAirCard
import com.example.ui.theme.InAirBackground
import com.example.ui.theme.InAirCardBorder
import com.example.ui.theme.InAirPrimary
import com.example.ui.theme.InAirSecondary
import com.example.ui.theme.InAirSurfaceVariant
import com.example.viewmodel.InAirViewModel

@Composable
fun ConnectedDevicesScreen(
    viewModel: InAirViewModel
) {
    val context = LocalContext.current
    val connectedClients by viewModel.connectedClients.collectAsState()
    val blockedIps by viewModel.blockedIps.collectAsState()

    fun formatDuration(connectedAtMillis: Long): String {
        val diffSec = ((System.currentTimeMillis() - connectedAtMillis) / 1000).coerceAtLeast(0)
        return when {
            diffSec < 60 -> "${diffSec}s"
            diffSec < 3600 -> "${diffSec / 60}m"
            else -> "${diffSec / 3600}h"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InAirBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.devices_title),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.devices_subtitle),
                        color = InAirSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (connectedClients.isEmpty()) {
                item {
                    InAirCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Devices,
                                    contentDescription = null,
                                    tint = Color(0x66FF2D3F),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.no_devices_connected),
                                    color = Color(0xFFD4A5AB),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            } else {
                items(connectedClients, key = { it.id }) { client ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        InAirCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x33FF2D3F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Laptop,
                                            contentDescription = null,
                                            tint = InAirPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = client.ipAddress,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = stringResource(R.string.device_connected_time, formatDuration(client.connectedAtMillis)),
                                            color = Color(0xFFD4A5AB),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Kick Button
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.kickClient(client.ipAddress)
                                            Toast.makeText(context, R.string.device_kicked_toast, Toast.LENGTH_SHORT).show()
                                        },
                                        shape = CircleShape,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FF6B6B)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = InAirSecondary),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(stringResource(R.string.btn_kick_device), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Block Button
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.rejectAndBlockClient(client.ipAddress)
                                            Toast.makeText(context, R.string.device_blocked_toast, Toast.LENGTH_SHORT).show()
                                        },
                                        shape = CircleShape,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, InAirPrimary),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = InAirPrimary),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(stringResource(R.string.btn_block_device), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Blocked Clients Section
            if (blockedIps.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.blocked_devices_title),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(blockedIps.toList(), key = { it }) { ip ->
                    InAirCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = InAirPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(ip, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            TextButton(
                                onClick = { viewModel.unblockClient(ip) }
                            ) {
                                Text(stringResource(R.string.btn_unblock), color = InAirSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
