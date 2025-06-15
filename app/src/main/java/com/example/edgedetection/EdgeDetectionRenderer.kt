package com.example.edgedetection

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class EdgeDetectionRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val textureIds = IntArray(1)
    private lateinit var surfaceTexture: SurfaceTexture
    private var shaderProgram: ShaderProgram? = null
    private var surfaceReadyListener: ((SurfaceTexture) -> Unit)? = null
    private var surfaceCreated = false

    private val vertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, // bottom left
        1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // bottom right
        -1.0f,  1.0f, 0.0f, 0.0f, 1.0f, // top left
        1.0f,  1.0f, 0.0f, 1.0f, 1.0f  // top right
    )

    private val indices = shortArrayOf(0, 1, 2, 2, 1, 3)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Generate texture
        GLES20.glGenTextures(1, textureIds, 0)
        surfaceTexture = SurfaceTexture(textureIds[0])

        try {
            // Create shader program
            shaderProgram = ShaderProgram(
                context = context,
                vertexShaderResId = R.raw.vertex_shader,
                fragmentShaderResId = R.raw.fragment_shader
            )
            
            // Notify that surface is ready
            surfaceCreated = true
            surfaceReadyListener?.invoke(surfaceTexture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Draw texture
        shaderProgram?.useProgram()
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        shaderProgram?.setInt("textureSampler", 0)
        shaderProgram?.draw(vertices, indices)
    }


    fun setOnSurfaceReadyListener(listener: (SurfaceTexture) -> Unit) {
        surfaceReadyListener = listener
        if (surfaceCreated) {
            listener(surfaceTexture)
        }
    }
}