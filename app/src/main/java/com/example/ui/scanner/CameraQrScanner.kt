package com.example.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.NasiOrange
import com.example.ui.theme.NasiOrangeDark
import com.example.ui.theme.NasiPurple
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@Composable
fun CameraQrScanner(
    onQrDecoded: (String) -> Unit,
    modifier: Modifier = Modifier,
    isScanningActive: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
            .testTag("camera_scanner_container"),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission && isScanningActive) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            var lastScannedTime = 0L
                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val now = System.currentTimeMillis()
                                if (now - lastScannedTime > 1500L) { // Throttle scan detections
                                    val decoded = decodeQrFromImage(imageProxy)
                                    if (!decoded.isNullOrBlank()) {
                                        lastScannedTime = now
                                        previewView.post {
                                            onQrDecoded(decoded)
                                        }
                                    }
                                }
                                imageProxy.close()
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Scanning Overlay: Viewfinder box and animated laser line
            ViewfinderOverlay(
                modifier = Modifier.fillMaxSize()
            )

            // Flashlight button on top right
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                IconButton(
                    onClick = {
                        camera?.let { cam ->
                            if (cam.cameraInfo.hasFlashUnit()) {
                                isFlashOn = !isFlashOn
                                cam.cameraControl.enableTorch(isFlashOn)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight",
                        tint = if (isFlashOn) NasiOrange else Color.White
                    )
                }
            }
        } else {
            // Permission or fallback prompt
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideocamOff,
                    contentDescription = null,
                    tint = NasiOrange,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Akses Kamera Diperlukan untuk Scan",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Izinkan akses kamera agar kasir dapat memindai QR code pelanggan.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = NasiPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Beri Izin Kamera")
                }
            }
        }
    }
}

@Composable
private fun ViewfinderOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserProgress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent mask with central transparent square
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val boxSize = (canvasW * 0.68f).coerceAtMost(canvasH * 0.68f)
            val boxLeft = (canvasW - boxSize) / 2f
            val boxTop = (canvasH - boxSize) / 2f

            // Viewfinder Corner Brackets
            val cornerLength = boxSize * 0.16f
            val strokeW = 4.dp.toPx()
            val bracketColor = Color(0xFFFFB830) // NasiYellow

            // Top-Left
            drawLine(bracketColor, Offset(boxLeft, boxTop), Offset(boxLeft + cornerLength, boxTop), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(boxLeft, boxTop), Offset(boxLeft, boxTop + cornerLength), strokeW, StrokeCap.Round)

            // Top-Right
            drawLine(bracketColor, Offset(boxLeft + boxSize, boxTop), Offset(boxLeft + boxSize - cornerLength, boxTop), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(boxLeft + boxSize, boxTop), Offset(boxLeft + boxSize, boxTop + cornerLength), strokeW, StrokeCap.Round)

            // Bottom-Left
            drawLine(bracketColor, Offset(boxLeft, boxTop + boxSize), Offset(boxLeft + cornerLength, boxTop + boxSize), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(boxLeft, boxTop + boxSize), Offset(boxLeft, boxTop + boxSize - cornerLength), strokeW, StrokeCap.Round)

            // Bottom-Right
            drawLine(bracketColor, Offset(boxLeft + boxSize, boxTop + boxSize), Offset(boxLeft + boxSize - cornerLength, boxTop + boxSize), strokeW, StrokeCap.Round)
            drawLine(bracketColor, Offset(boxLeft + boxSize, boxTop + boxSize), Offset(boxLeft + boxSize, boxTop + boxSize - cornerLength), strokeW, StrokeCap.Round)

            // Animated Laser Line
            val laserY = boxTop + (boxSize * laserProgress)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFFF9800),
                        Color(0xFFFF5722),
                        Color(0xFFFF9800),
                        Color.Transparent
                    ),
                    startX = boxLeft,
                    endX = boxLeft + boxSize
                ),
                start = Offset(boxLeft, laserY),
                end = Offset(boxLeft + boxSize, laserY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Subtitle hint
        Text(
            text = "Arahkan kamera ke Barcode / QR Pelanggan",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun decodeQrFromImage(image: ImageProxy): String? {
    return try {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val width = image.width
        val height = image.height

        val source = PlanarYUVLuminanceSource(
            bytes,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        val result = reader.decode(binaryBitmap)
        result.text
    } catch (e: NotFoundException) {
        null
    } catch (e: Exception) {
        null
    }
}
