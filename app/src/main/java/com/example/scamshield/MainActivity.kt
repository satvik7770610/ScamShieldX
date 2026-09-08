package com.example.scamshield

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.scamshield.ui.navigation.AppNavigation
import com.example.scamshield.ui.theme.ScamShieldTheme

class MainActivity : ComponentActivity() {

    private var sharedContentState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedContentState = extractSharedText(intent)

        setContent {
            ScamShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(
                        initialSharedContent = sharedContentState,
                        onSharedContentHandled = {
                            sharedContentState = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val newSharedText = extractSharedText(intent)
        if (!newSharedText.isNullOrBlank()) {
            sharedContentState = newSharedText
        }
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent == null || intent.action != Intent.ACTION_SEND) return null
        val mimeType = intent.type
        if (mimeType != null && mimeType.startsWith("text/")) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                return text.trim()
            }
        }
        return null
    }
}