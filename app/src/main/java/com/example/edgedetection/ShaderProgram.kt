package com.example.edgedetection

import android.content.Context
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ShaderProgram(
    context: Context?,
    vertexShaderResId: Int,
    fragmentShaderResId: Int
) {
    private val programId: Int

    // Attribute locations
    private var positionHandle = 0
    private var texCoordHandle = 0

    init {
        val vertexShader = context?.let {
            loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderResId,
                it
            )
        }
        val fragmentShader =
            context?.let { loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderResId, it) }

        programId = GLES20.glCreateProgram().also {
            if (vertexShader != null) {
                GLES20.glAttachShader(it, vertexShader)
            }
            if (fragmentShader != null) {
                GLES20.glAttachShader(it, fragmentShader)
            }
            GLES20.glLinkProgram(it)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(it)
                throw RuntimeException("Could not link program: ${GLES20.glGetProgramInfoLog(it)}")
            }
        }

        // Get attribute locations
        positionHandle = GLES20.glGetAttribLocation(programId, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord")
    }

    fun useProgram() {
        GLES20.glUseProgram(programId)
    }

    fun setInt(name: String, value: Int) {
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, name), value)
    }

    fun draw(vertices: FloatArray, indices: ShortArray) {
        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }

        val indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indices)
                position(0)
            }

        // Position attribute
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false,
            5 * 4, vertexBuffer
        )

        // Texture coordinate attribute
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(
            texCoordHandle, 2, GLES20.GL_FLOAT, false,
            5 * 4, vertexBuffer
        )

        // Draw
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        // Disable
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadShader(type: Int, resId: Int, context: Context): Int {
        return try {
            // Read shader code from raw resource
            val inputStream = context.resources.openRawResource(resId)
            val shaderCode = inputStream.bufferedReader().use { it.readText() }

            GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)

                val compileStatus = IntArray(1)
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
                if (compileStatus[0] == 0) {
                    val infoLog = GLES20.glGetShaderInfoLog(shader)
                    GLES20.glDeleteShader(shader)
                    throw RuntimeException("Shader compilation error: $infoLog")
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Error loading shader resource: $resId", e)
        }
    }
}