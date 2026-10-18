package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ClientApprovalDialog
import com.example.ui.screens.ConnectedDevicesScreen
import com.example.ui.screens.FileShareScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NetworkSetupScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.RemoteControlScreen
import com.example.ui.screens.ScreenShareScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.InAirBackground
import com.example.ui.theme.InAirPrimary
import com.example.ui.theme.InAirSecondary
import com.example.ui.theme.InAirSurface
import com.example.ui.theme.InAirTheme
import com.example.viewmodel.InAirViewModel
import java.util.Locale

enum class AppDestination {
    SPLASH,
    ONBOARDING,
    MAIN_APP,
    NETWORK_SETUP,
    SETTINGS,
    PRIVACY_POLICY
}

enum class MainTab(val labelRes: Int, val icon: ImageVector, val tag: String) {
    HOME(R.string.nav_home, Icons.Default.Home, "tab_home"),
    SCREEN(R.string.nav_screen, Icons.Default.CastConnected, "tab_screen"),
    REMOTE(R.string.remote_control_title, Icons.Default.TouchApp, "tab_remote"),
    FILES(R.string.nav_files, Icons.Default.FolderShared, "tab_files"),
    DEVICES(R.string.nav_devices, Icons.Default.Devices, "tab_devices")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val inAirViewModel: InAirViewModel = viewModel()
            val currentLang by inAirViewModel.language.collectAsState()

            // RTL & Arabic Locale support
            val isArabic = currentLang == "ar"
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            val context = LocalContext.current
            val locale = Locale(currentLang)
            val config = Configuration(LocalConfiguration.current).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            val localizedContext = remember(currentLang) {
                context.createConfigurationContext(config)
            }

            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
                LocalConfiguration provides config,
                LocalContext provides localizedContext,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides this@MainActivity,
                androidx.activity.compose.LocalOnBackPressedDispatcherOwner provides this@MainActivity
            ) {
                InAirTheme {
                    InAirAppRoot(inAirViewModel)
                }
            }
        }
    }
}

@Composable
fun InAirAppRoot(viewModel: InAirViewModel) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    val pendingApproval by viewModel.pendingApproval.collectAsState()

    val mediaProjectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val projection = mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
            if (projection != null) {
                viewModel.startBroadcastWithProjection(projection)
            } else {
                viewModel.stopBroadcast()
            }
        } else {
            viewModel.stopBroadcast()
        }
    }

    fun requestMediaProjection() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        projectionLauncher.launch(captureIntent)
    }

    // Back handling
    BackHandler(enabled = currentDestination != AppDestination.MAIN_APP || selectedTab != MainTab.HOME) {
        when {
            currentDestination == AppDestination.SETTINGS ||
                    currentDestination == AppDestination.NETWORK_SETUP ||
                    currentDestination == AppDestination.PRIVACY_POLICY -> {
                currentDestination = AppDestination.MAIN_APP
            }
            selectedTab != MainTab.HOME -> {
                selectedTab = MainTab.HOME
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InAirBackground)
    ) {
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { destination ->
            when (destination) {
                AppDestination.SPLASH -> {
                    SplashScreen(
                        onSplashComplete = {
                            currentDestination = AppDestination.ONBOARDING
                        }
                    )
                }

                AppDestination.ONBOARDING -> {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            currentDestination = AppDestination.MAIN_APP
                        }
                    )
                }

                AppDestination.NETWORK_SETUP -> {
                    NetworkSetupScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentDestination = AppDestination.MAIN_APP }
                    )
                }

                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentDestination = AppDestination.MAIN_APP },
                        onNavigateToPrivacyPolicy = { currentDestination = AppDestination.PRIVACY_POLICY }
                    )
                }

                AppDestination.PRIVACY_POLICY -> {
                    PrivacyPolicyScreen(
                        onNavigateBack = { currentDestination = AppDestination.SETTINGS }
                    )
                }

                AppDestination.MAIN_APP -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = InAirBackground,
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .border(1.dp, Color(0x33FF2D3F))
                                    .navigationBarsPadding()
                                    .height(72.dp),
                                containerColor = InAirSurface,
                                tonalElevation = 8.dp
                            ) {
                                MainTab.values().forEach { tab ->
                                    val isSelected = selectedTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { selectedTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = stringResource(tab.labelRes),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(tab.labelRes),
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = InAirSecondary,
                                            indicatorColor = InAirPrimary,
                                            unselectedIconColor = Color(0xFFD4A5AB),
                                            unselectedTextColor = Color(0xFFD4A5AB)
                                        ),
                                        modifier = Modifier.testTag(tab.tag)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedTab) {
                                MainTab.HOME -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onRequestMediaProjection = { requestMediaProjection() },
                                        onNavigateToNetworkSetup = { currentDestination = AppDestination.NETWORK_SETUP },
                                        onNavigateToDevices = { selectedTab = MainTab.DEVICES },
                                        onNavigateToSettings = { currentDestination = AppDestination.SETTINGS }
                                    )
                                }

                                MainTab.SCREEN -> {
                                    ScreenShareScreen(
                                        viewModel = viewModel,
                                        onRequestMediaProjection = { requestMediaProjection() }
                                    )
                                }

                                MainTab.REMOTE -> {
                                    RemoteControlScreen()
                                }

                                MainTab.FILES -> {
                                    FileShareScreen(viewModel = viewModel)
                                }

                                MainTab.DEVICES -> {
                                    ConnectedDevicesScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Global Client Connection Approval Modal
        pendingApproval?.let { session ->
            ClientApprovalDialog(
                session = session,
                onAllow = { viewModel.approveClient(session.ipAddress) },
                onReject = { viewModel.rejectAndBlockClient(session.ipAddress) }
            )
        }
    }
}
