package com.example.edgedetection

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.edgedetection.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var renderer: EdgeDetectionRenderer
    private lateinit var cameraManager: CameraManager

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup OpenGL renderer
        binding.glSurfaceView.apply {
            setEGLContextClientVersion(2)
            // Create the renderer
            renderer = EdgeDetectionRenderer(applicationContext)
            // Set the renderer first before setting render mode
            setRenderer(renderer)
            // Now set render mode
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }

        // Check for camera permission before setting up camera
        if (hasCameraPermission()) {
            setupCamera()
        } else {
            requestCameraPermission()
        }

        // Toggle button handler
        binding.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            NativeBridge.setProcessingMode(isChecked)
        }
    }

    private fun setupCamera() {
        // Setup camera after OpenGL surface is ready
        renderer.setOnSurfaceReadyListener { surfaceTexture ->
            cameraManager = CameraManager(
                context = this,
                surfaceTexture = surfaceTexture,
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            )
            cameraManager.startCamera()
        }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, 
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.glSurfaceView.onPause()
        if (::cameraManager.isInitialized) {
            cameraManager.stopCamera()
        }
    }
}