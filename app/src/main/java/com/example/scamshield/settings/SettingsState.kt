package com.example.scamshield.settings

data class SettingsState(
    val hapticWarningEnabled: Boolean = true,
    val localAnalysisOnly: Boolean = true,
    val showTechnicalDetails: Boolean = true,
    val protectionActive: Boolean = true
)