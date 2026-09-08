package com.example.scamshield.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scamshield.model.RiskAnalysisResult
import com.example.scamshield.ui.theme.DarkBorder
import com.example.scamshield.ui.theme.DarkSurface
import com.example.scamshield.ui.theme.DarkSurfaceVariant
import com.example.scamshield.ui.theme.PrimaryShield
import com.example.scamshield.ui.theme.TextMuted
import com.example.scamshield.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    onBack: () -> Unit,
    onResultAvailable: (RiskAnalysisResult) -> Unit,
    viewModel: QrScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var manualText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.analysisResult) {
        val result = uiState.analysisResult
        if (result != null) {
            onResultAvailable(result)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleManualInputDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Manual Payload Input",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (hasCameraPermission) {
                        IconButton(onClick = { viewModel.toggleTorch() }) {
                            Icon(
                                imageVector = if (uiState.isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Torch",
                                tint = if (uiState.isTorchEnabled) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasCameraPermission) {
                // Camera Preview View
                CameraPreviewView(
                    isTorchEnabled = uiState.isTorchEnabled,
                    isScanningActive = uiState.isScanningActive,
                    onQrDetected = { payload ->
                        viewModel.onQrCodeScanned(payload)
                    }
                )

                // Viewfinder Overlay Graphics
                ScannerOverlayViewfinder()

                // Instructions Banner at Bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(DarkSurface.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = PrimaryShield,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Position QR code within frame to scan",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { viewModel.toggleManualInputDialog(true) }) {
                        Text(
                            text = "Or paste QR payload manually",
                            fontSize = 13.sp,
                            color = PrimaryShield,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // Permission Request Container
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                            .border(1.dp, DarkBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Permission Required",
                            tint = PrimaryShield,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Camera Access Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ScamShield X uses your camera to scan QR codes and analyze risk signals locally before you pay or proceed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryShield,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Grant Camera Permission",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { viewModel.toggleManualInputDialog(true) }) {
                        Text(
                            text = "Or paste QR payload manually",
                            fontSize = 14.sp,
                            color = PrimaryShield
                        )
                    }
                }
            }

            // Processing Overlay State
            if (uiState.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(DarkSurface, RoundedCornerShape(16.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                            .padding(28.dp)
                    ) {
                        CircularProgressIndicator(color = PrimaryShield)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analyzing QR Payload...",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Evaluating signals with local Risk Engine",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Manual Input Dialog
            if (uiState.showManualInputDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.toggleManualInputDialog(false) },
                    title = {
                        Text(
                            text = "Paste QR Payload",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Enter or paste URL or UPI QR raw payload string:",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = manualText,
                                onValueChange = { manualText = it },
                                placeholder = { Text("upi://pay?pa=... or https://...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryShield,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.toggleManualInputDialog(false)
                                if (manualText.isNotBlank()) {
                                    viewModel.onQrCodeScanned(manualText)
                                    manualText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryShield,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Analyze", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.toggleManualInputDialog(false) }) {
                            Text("Cancel", color = TextMuted)
                        }
                    },
                    containerColor = DarkSurface
                )
            }
        }
    }
}

@Composable
private fun ScannerOverlayViewfinder() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val sizePx = 260.dp.toPx()
        val left = (size.width - sizePx) / 2
        val top = (size.height - sizePx) / 2
        val cornerLength = 32.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val color = Color(0xFF00E5FF)

        // Top-Left Corner
        drawLine(color, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
        drawLine(color, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

        // Top-Right Corner
        drawLine(color, Offset(left + sizePx, top), Offset(left + sizePx - cornerLength, top), strokeWidth)
        drawLine(color, Offset(left + sizePx, top), Offset(left + sizePx, top + cornerLength), strokeWidth)

        // Bottom-Left Corner
        drawLine(color, Offset(left, top + sizePx), Offset(left + cornerLength, top + sizePx), strokeWidth)
        drawLine(color, Offset(left, top + sizePx), Offset(left, top + sizePx - cornerLength), strokeWidth)

        // Bottom-Right Corner
        drawLine(color, Offset(left + sizePx, top + sizePx), Offset(left + sizePx - cornerLength, top + sizePx), strokeWidth)
        drawLine(color, Offset(left + sizePx, top + sizePx), Offset(left + sizePx, top + sizePx - cornerLength), strokeWidth)
    }
}