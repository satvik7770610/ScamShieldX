package com.example.scamshield.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.scamshield.camerashield.CameraMessageShieldScreen
import com.example.scamshield.scanner.QrScannerScreen
import com.example.scamshield.shieldscan.ShieldScanActivity
import com.example.scamshield.ui.components.ScamShieldBottomBar
import com.example.scamshield.ui.dev.MlTestLabScreen
import com.example.scamshield.ui.history.HistoryDetailScreen
import com.example.scamshield.ui.history.HistoryScreen
import com.example.scamshield.ui.home.HomeScreen
import com.example.scamshield.ui.result.RiskResultScreen
import com.example.scamshield.ui.screenshot.ScreenshotShieldScreen
import com.example.scamshield.ui.settings.SettingsScreen
import com.example.scamshield.ui.text.TextAnalyzerScreen
import java.net.URLDecoder
import java.net.URLEncoder

private fun encodePayload(payload: String): String {
    return try {
        URLEncoder.encode(payload, "UTF-8")
    } catch (_: Exception) {
        payload
    }
}

private fun decodePayload(encoded: String): String {
    return try {
        URLDecoder.decode(encoded, "UTF-8")
    } catch (_: Exception) {
        encoded
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    initialSharedContent: String? = null,
    onSharedContentHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        ScreenRoute.Home.route,
        ScreenRoute.CameraShield.route,
        ScreenRoute.History.route,
        ScreenRoute.Settings.route
    )

    LaunchedEffect(initialSharedContent) {
        if (!initialSharedContent.isNullOrBlank()) {
            val encoded = encodePayload(initialSharedContent)
            navController.navigate("${ScreenRoute.RiskResult.route}?payload=$encoded&isShared=true")
            onSharedContentHandled()
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ScamShieldBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(ScreenRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenRoute.Home.route) {
                HomeScreen(
                    onNavigateToQrScanner = {
                        navController.navigate(ScreenRoute.QrScanner.route)
                    },
                    onNavigateToCameraShield = {
                        navController.navigate(ScreenRoute.CameraShield.route)
                    },
                    onNavigateToTextAnalyzer = {
                        navController.navigate(ScreenRoute.TextAnalyzer.route)
                    },
                    onNavigateToScreenshotShield = {
                        navController.navigate(ScreenRoute.ScreenshotShield.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(ScreenRoute.History.route)
                    },
                    onNavigateToHistoryDetail = { historyId ->
                        navController.navigate("${ScreenRoute.HistoryDetail.route}?historyId=$historyId")
                    },
                    onNavigateToSettings = {
                        navController.navigate(ScreenRoute.Settings.route)
                    },
                    onNavigateToMlTestLab = {
                        navController.navigate(ScreenRoute.MlTestLab.route)
                    },
                    onNavigateToRiskResult = { payload ->
                        val encoded = encodePayload(payload)
                        navController.navigate("${ScreenRoute.RiskResult.route}?payload=$encoded")
                    },
                    onNavigateToShieldScan = {
                        ShieldScanActivity.start(context)
                    }
                )
            }

            composable(ScreenRoute.QrScanner.route) {
                QrScannerScreen(
                    onBack = { navController.popBackStack() },
                    onResultAvailable = { result ->
                        val encoded = encodePayload(result.rawPayload)
                        navController.navigate("${ScreenRoute.RiskResult.route}?payload=$encoded") {
                            popUpTo(ScreenRoute.QrScanner.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(ScreenRoute.CameraShield.route) {
                CameraMessageShieldScreen(
                    onBack = { navController.popBackStack() },
                    onResultAvailable = { result ->
                        val encoded = encodePayload(result.rawPayload)
                        navController.navigate("${ScreenRoute.RiskResult.route}?payload=$encoded") {
                            popUpTo(ScreenRoute.CameraShield.route) { inclusive = true }
                        }
                    }
                )
            }

            composable("${ScreenRoute.RiskResult.route}?payload={payload}&isShared={isShared}") { backStackEntry ->
                val rawParam = backStackEntry.arguments?.getString("payload") ?: "upi://pay?pa=demo-scam@upi&pn=Prize%20Claim&am=4999"
                val isSharedParam = backStackEntry.arguments?.getString("isShared") == "true"
                val payload = decodePayload(rawParam)
                RiskResultScreen(
                    rawPayload = payload,
                    isSharedContent = isSharedParam,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ScreenRoute.TextAnalyzer.route) {
                TextAnalyzerScreen(
                    onBack = { navController.popBackStack() },
                    onResultAvailable = { result ->
                        val encoded = encodePayload(result.rawPayload)
                        navController.navigate("${ScreenRoute.RiskResult.route}?payload=$encoded")
                    }
                )
            }

            composable(ScreenRoute.ScreenshotShield.route) {
                ScreenshotShieldScreen(
                    onBack = { navController.popBackStack() },
                    onResultAvailable = { result ->
                        val encoded = encodePayload(result.rawPayload)
                        navController.navigate("${ScreenRoute.RiskResult.route}?payload=$encoded")
                    }
                )
            }

            composable(ScreenRoute.MlTestLab.route) {
                MlTestLabScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ScreenRoute.History.route) {
                HistoryScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHistoryDetail = { historyId ->
                        navController.navigate("${ScreenRoute.HistoryDetail.route}?historyId=$historyId")
                    }
                )
            }

            composable("${ScreenRoute.HistoryDetail.route}?historyId={historyId}") { backStackEntry ->
                val historyIdStr = backStackEntry.arguments?.getString("historyId") ?: "0"
                val historyId = historyIdStr.toLongOrNull() ?: 0L
                HistoryDetailScreen(
                    historyId = historyId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(ScreenRoute.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHistory = { navController.navigate(ScreenRoute.History.route) },
                    onNavigateToMlTestLab = { navController.navigate(ScreenRoute.MlTestLab.route) }
                )
            }
        }
    }
}