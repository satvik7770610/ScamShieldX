package com.example.scamshield.ui.text

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.ui.components.ScamShieldSectionLabel
import com.example.scamshield.ui.components.SecurityDivider
import com.example.scamshield.ui.theme.DarkBorder
import com.example.scamshield.ui.theme.DarkSurface
import com.example.scamshield.ui.theme.DarkSurfaceVariant
import com.example.scamshield.ui.theme.PrimaryShield
import com.example.scamshield.ui.theme.RiskHigh
import com.example.scamshield.ui.theme.TextMuted
import com.example.scamshield.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextAnalyzerScreen(
    onBack: () -> Unit,
    onResultAvailable: (RiskAnalysisResult) -> Unit,
    viewModel: TextAnalyzerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.analysisResult) {
        val result = uiState.analysisResult
        if (result != null) {
            onResultAvailable(result)
            viewModel.clearResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ANALYZE TEXT & LINKS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ScamShieldSectionLabel(number = "01", title = "INPUT TEXT OR URL")
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Check SMS messages, URL links, recruitment offers & payment requests before replying or opening.",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = { viewModel.onInputTextChanged(it) },
                placeholder = {
                    Text(
                        "Paste SMS message, suspicious link, or recruitment text here...",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryShield,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(4.dp)
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = RiskHigh,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.analyzeInput() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RiskHigh,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "ANALYZE NOW",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            SecurityDivider(verticalPadding = 20.dp)

            ScamShieldSectionLabel(number = "02", title = "TEST CASES")
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SampleTextChip(
                    title = "PHISHING BANK LINK",
                    text = "https://paypa1-demo.example.com/login",
                    onClick = { viewModel.setDemoText("https://paypa1-demo.example.com/login") }
                )
                SampleTextChip(
                    title = "URGENT ACCOUNT BLOCK SMS",
                    text = "URGENT: Your bank account will be blocked in 24 hours. Verify immediately at https://sbi-netbanking-verify.top/update-kyc",
                    onClick = { viewModel.setDemoText("URGENT: Your bank account will be blocked in 24 hours. Verify immediately at https://sbi-netbanking-verify.top/update-kyc") }
                )
                SampleTextChip(
                    title = "PRIZE CLAIM REFUND SCAM",
                    text = "CONGRATULATIONS! You won ₹1,00,000 cash reward. Deposit ₹1,000 processing fee to receive your refund immediately.",
                    onClick = { viewModel.setDemoText("CONGRATULATIONS! You won ₹1,00,000 cash reward. Deposit ₹1,000 processing fee to receive your refund immediately.") }
                )
            }
        }
    }
}

@Composable
private fun SampleTextChip(
    title: String,
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = PrimaryShield
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 2
            )
        }
    }
}
