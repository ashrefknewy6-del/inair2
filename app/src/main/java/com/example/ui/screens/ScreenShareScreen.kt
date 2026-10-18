package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BroadcastStatus
import com.example.model.StreamFps
import com.example.model.StreamResolution
import com.example.ui.components.InAirCard
import com.example.ui.components.InAirGradientButton
import com.example.ui.components.LiveBandwidthGraph
import com.example.ui.components.StatusChip
import com.example.ui.theme.InAirBackground
import com.example.ui.theme.InAirCardBorder
import com.example.ui.theme.InAirPrimary
import com.example.ui.theme.InAirSecondary
import com.example.ui.theme.InAirSignatureGradient
import com.example.ui.theme.InAirSurfaceVariant
import com.example.viewmodel.InAirViewModel

@Composable
fun ScreenShareScreen(
    viewModel: InAirViewModel,
    onRequestMediaProjection: () -> Unit
) {
    val status by viewModel.broadcastStatus.collectAsState()
    val config by viewModel.streamConfig.collectAsState()
    val previewBitmap by viewModel.latestPreviewBitmap.collectAsState()
    val bandwidthKbps by viewModel.bandwidthKbps.collectAsState()
    val bandwidthHistory by viewModel.bandwidthHistory.collectAsState()
    val isBroadcasting = status == BroadcastStatus.BROADCASTING

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
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.screen_share_title),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.screen_share_subtitle),
                        color = InAirSecondary,
                        fontSize = 12.sp
                    )
                }
                StatusChip(status = status)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Live Preview Thumbnail Card
            InAirCard {
                Text(
                    text = stringResource(R.string.screen_preview_title),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(1.dp, InAirCardBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null && isBroadcasting) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Screen Stream Live Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Cast,
                                contentDescription = null,
                                tint = Color(0x66FF2D3F),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(if (isBroadcasting) R.string.screen_share_active else R.string.screen_share_inactive),
                                color = Color(0xFFD4A5AB),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle stream button
                InAirGradientButton(
                    text = if (isBroadcasting) stringResource(R.string.btn_stop_broadcast) else stringResource(R.string.btn_start_broadcast),
                    onClick = {
                        viewModel.toggleBroadcast(onRequestMediaProjection)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resolution Selector Card
            InAirCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = InAirPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.screen_stream_resolution),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StreamResolution.values().forEach { res ->
                        val isSelected = config.resolution == res
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) InAirSignatureGradient else androidx.compose.ui.graphics.SolidColor(InAirSurfaceVariant))
                                .border(1.dp, if (isSelected) Color(0x66FF2D3F) else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { viewModel.setStreamResolution(res) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = res.label,
                                color = if (isSelected) Color.White else Color(0xFFD4A5AB),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FPS Selector Card
            InAirCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = InAirPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.screen_stream_fps),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StreamFps.values().forEach { fps ->
                        val isSelected = config.fps == fps
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) InAirSignatureGradient else androidx.compose.ui.graphics.SolidColor(InAirSurfaceVariant))
                                .border(1.dp, if (isSelected) Color(0x66FF2D3F) else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { viewModel.setStreamFps(fps) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${fps.fps} FPS",
                                color = if (isSelected) Color.White else Color(0xFFD4A5AB),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bandwidth Usage and Animated Graph
            InAirCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transfer Rate",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.bandwidth_usage, bandwidthKbps),
                        color = InAirSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LiveBandwidthGraph(history = bandwidthHistory)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
