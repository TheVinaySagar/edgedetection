package com.example.edgedetection

object NativeBridge {
    init {
        try {
            System.loadLibrary("edge-detector")
            println("Successfully loaded native libraries")
        } catch (e: UnsatisfiedLinkError) {
            println("Failed to load edge-detector library: ${e.message}")
            e.printStackTrace()
        }
    }

    // Native methods that will be implemented in C++
    external fun initializeDetector(): Boolean
    external fun detectEdges(imageData: ByteArray, width: Int, height: Int): IntArray
    external fun setProcessingMode(enabled: Boolean): Boolean
}