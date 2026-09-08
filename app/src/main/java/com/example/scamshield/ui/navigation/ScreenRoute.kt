package com.example.scamshield.ui.navigation

sealed class ScreenRoute(val route: String, val title: String) {
    object Home : ScreenRoute("home", "ScamShield X")
    object QrScanner : ScreenRoute("qr_scanner", "QR Scanner")
    object CameraShield : ScreenRoute("camera_shield", "Camera Shield")
    object RiskResult : ScreenRoute("risk_result", "Risk Analysis")
    object TextAnalyzer : ScreenRoute("text_analyzer", "Text & Link Analyzer")
    object ScreenshotShield : ScreenRoute("screenshot_shield", "Screenshot Shield")
    object History : ScreenRoute("history", "Threat History")
    object HistoryDetail : ScreenRoute("history_detail", "Stored History Detail")
    object Settings : ScreenRoute("settings", "Protection Settings")
    object MlTestLab : ScreenRoute("ml_test_lab", "ML Test Lab")
    object ShieldScan : ScreenRoute("shield_scan", "Shield Scan")
}