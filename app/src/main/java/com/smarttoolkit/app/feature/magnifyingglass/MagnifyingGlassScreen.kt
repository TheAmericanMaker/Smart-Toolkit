package com.smarttoolkit.app.feature.magnifyingglass

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smarttoolkit.app.ui.components.PermissionHandler
import com.smarttoolkit.app.ui.components.UtilityTopBar

@Composable
fun MagnifyingGlassScreen(
    onBack: () -> Unit,
    viewModel: MagnifyingGlassViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            UtilityTopBar(
                title = "Magnifier",
                onBack = onBack,
                actions = {
                    IconButton(onClick = viewModel::toggleTorch) {
                        Icon(
                            if (state.isTorchOn) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
                            contentDescription = "Toggle torch"
                        )
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
                rationaleText = "Camera permission is needed to use the magnifier."
            ) {
                CameraPreviewWithZoom(
                    zoomRatio = state.zoomRatio,
                    minZoom = state.minZoom,
                    maxZoom = state.maxZoom,
                    isTorchOn = state.isTorchOn,
                    onZoomChanged = viewModel::onZoomChanged,
                    onZoomRangeDetected = viewModel::onZoomRangeDetected,
                    modifier = Modifier.weight(1f)
                )
                ZoomControls(
                    zoomRatio = state.zoomRatio,
                    minZoom = state.minZoom,
                    maxZoom = state.maxZoom,
                    onZoomChanged = viewModel::onZoomChanged
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewWithZoom(
    zoomRatio: Float,
    minZoom: Float,
    maxZoom: Float,
    isTorchOn: Boolean,
    onZoomChanged: (Float) -> Unit,
    onZoomRangeDetected: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    LaunchedEffect(Unit) {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).await()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview
            )
            camera?.cameraInfo?.zoomState?.value?.let { zoomState ->
                onZoomRangeDetected(zoomState.minZoomRatio, zoomState.maxZoomRatio)
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(zoomRatio) {
        camera?.cameraControl?.setZoomRatio(zoomRatio)
    }

    LaunchedEffect(isTorchOn) {
        camera?.cameraControl?.enableTorch(isTorchOn)
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ZoomControls(
    zoomRatio: Float,
    minZoom: Float,
    maxZoom: Float,
    onZoomChanged: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onZoomChanged((zoomRatio - 0.5f).coerceAtLeast(minZoom)) }) {
            Icon(Icons.Filled.Remove, contentDescription = "Zoom out")
        }
        Slider(
            value = zoomRatio,
            onValueChange = onZoomChanged,
            valueRange = minZoom..maxZoom.coerceAtLeast(minZoom + 0.1f),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { onZoomChanged((zoomRatio + 0.5f).coerceAtMost(maxZoom)) }) {
            Icon(Icons.Filled.Add, contentDescription = "Zoom in")
        }
        Text(
            "%.1fx".format(zoomRatio),
            style = MaterialTheme.typography.labelLarge
        )
    }
}
