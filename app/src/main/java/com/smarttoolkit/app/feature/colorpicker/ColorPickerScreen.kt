package com.smarttoolkit.app.feature.colorpicker

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.PermissionHandler
import com.smarttoolkit.app.ui.components.UtilityTopBar
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@Composable
fun ColorPickerScreen(
    onBack: () -> Unit,
    viewModel: ColorPickerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette by viewModel.palette.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(stream)
                stream?.close()
                if (bitmap != null) {
                    val cx = bitmap.width / 2
                    val cy = bitmap.height / 2
                    val pixel = bitmap.getPixel(cx, cy)
                    val r = android.graphics.Color.red(pixel)
                    val g = android.graphics.Color.green(pixel)
                    val b = android.graphics.Color.blue(pixel)
                    viewModel.onColorSampled(r, g, b)
                    bitmap.recycle()
                }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            UtilityTopBar(
                title = "Color Picker",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = "Pick from gallery")
                    }
                    if (palette.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearPalette) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear palette")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PermissionHandler(
                permission = Manifest.permission.CAMERA,
                rationaleText = "Camera permission is needed to pick colors."
            ) {
                CameraWithColorSampling(
                    onColorSampled = viewModel::onColorSampled,
                    modifier = Modifier.weight(1f)
                )

                // Palette row
                if (palette.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saved", style = MaterialTheme.typography.labelSmall)
                        palette.take(20).forEach { entry ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(entry.value))
                            } catch (_: Exception) { Color.Gray }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("color", entry.value))
                                    }
                            )
                        }
                    }
                }

                ColorInfoPanel(state = state, context = context, onSave = viewModel::saveColor)
            }
        }
    }
}

@Composable
private fun CameraWithColorSampling(
    onColorSampled: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).await()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            var lastSampleTime = 0L
            analysis.setAnalyzer(executor) { imageProxy ->
                val now = System.currentTimeMillis()
                if (now - lastSampleTime >= 150) {
                    lastSampleTime = now
                    try {
                        val buffer: ByteBuffer = imageProxy.planes[0].buffer
                        val rowStride = imageProxy.planes[0].rowStride
                        val pixelStride = imageProxy.planes[0].pixelStride
                        val centerX = imageProxy.width / 2
                        val centerY = imageProxy.height / 2
                        val offset = centerY * rowStride + centerX * pixelStride
                        if (offset + 2 < buffer.capacity()) {
                            val r = buffer.get(offset).toInt() and 0xFF
                            val g = buffer.get(offset + 1).toInt() and 0xFF
                            val b = buffer.get(offset + 2).toInt() and 0xFF
                            onColorSampled(r, g, b)
                        }
                    } catch (_: Exception) {}
                }
                imageProxy.close()
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
            )
        } catch (_: Exception) {}
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val crosshairRadius = 24f
            drawCircle(Color.White, crosshairRadius, Offset(cx, cy), style = Stroke(3f))
            drawCircle(Color.Black, crosshairRadius, Offset(cx, cy), style = Stroke(1.5f))
            drawLine(Color.White, Offset(cx - crosshairRadius - 8, cy), Offset(cx - crosshairRadius + 4, cy), strokeWidth = 2f)
            drawLine(Color.White, Offset(cx + crosshairRadius - 4, cy), Offset(cx + crosshairRadius + 8, cy), strokeWidth = 2f)
            drawLine(Color.White, Offset(cx, cy - crosshairRadius - 8), Offset(cx, cy - crosshairRadius + 4), strokeWidth = 2f)
            drawLine(Color.White, Offset(cx, cy + crosshairRadius - 4), Offset(cx, cy + crosshairRadius + 8), strokeWidth = 2f)
        }
    }
}

@Composable
private fun ColorInfoPanel(state: ColorPickerUiState, context: Context, onSave: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(state.red, state.green, state.blue))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(state.colorHex, style = MaterialTheme.typography.titleMedium)
                if (state.colorName.isNotEmpty()) {
                    Text(
                        state.colorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "RGB(%d, %d, %d)".format(state.red, state.green, state.blue),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "HSL(%d\u00B0, %d%%, %d%%)".format(
                        state.hue.toInt(),
                        (state.saturation * 100).toInt(),
                        (state.lightness * 100).toInt()
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onSave) {
                Icon(Icons.Filled.AddCircle, contentDescription = "Save color")
            }
            IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("color", state.colorHex))
            }) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy color")
            }
        }
    }
}
