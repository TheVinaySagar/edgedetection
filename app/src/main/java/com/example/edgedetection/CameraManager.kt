package com.example.edgedetection

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val surfaceTexture: SurfaceTexture,
    private val cameraSelector: CameraSelector
) {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Create preview use case
            val preview = Preview.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()
                .apply {
                    // Instead of setting to null, use a custom surface provider
                    setSurfaceProvider { request ->
                        val texture = surfaceTexture
                        texture.setDefaultBufferSize(request.resolution.width, request.resolution.height)
                        request.provideSurface(android.view.Surface(texture),
                            ContextCompat.getMainExecutor(context)) { }
                    }
                }

            // Image analysis pipeline
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(640, 480))
                .build()
                .apply {
                    setAnalyzer(cameraExecutor, FrameAnalyzer())
                }

            try {
                // Unbind all use cases before binding new ones
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                cameraProvider?.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    inner class FrameAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            try {
                // Convert your image to byte array as needed
                val buffer = image.planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)

                // Safe call to the native code
                val result = NativeBridge.detectEdges(data, image.width, image.height)
                // Process the result

            } catch (e: Exception) {
                android.util.Log.e("CameraManager", "Error analyzing frame", e)
            } finally {
                image.close()
            }
        }
    }
}