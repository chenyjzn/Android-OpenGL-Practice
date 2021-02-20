package com.yuchen.opengl

import android.opengl.GLES30
import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class Triangle {

    private val vaoIds = IntArray(1)
    private val vboIds = IntArray(1)
    private val eboIds = IntArray(1)

    private val vertices = floatArrayOf(
            // positions         // colors
            0.5f, -0.5f, 0.0f,  1.0f, 0.0f, 0.0f,   // bottom right
            -0.5f, -0.5f, 0.0f,  0.0f, 1.0f, 0.0f,   // bottom left
            0.0f,  0.5f, 0.0f,  0.0f, 0.0f, 1.0f    // top
    )

    private val indices = intArrayOf(
            0, 1, 2,
    )

    private var indicesBuffer: IntBuffer =
            ByteBuffer.allocateDirect(indices.size * Int.SIZE_BYTES).run {
                order(ByteOrder.nativeOrder())
                asIntBuffer().apply {
                    put(indices)
                    position(0)
                }
            }

    private var vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

    private val vertexShaderCode =
        "#version 300 es\n" +
                "layout (location = 0) in vec3 aPos;\n" +
                "layout (location = 1) in vec3 aColor;\n" +
                "out vec3 ourColor;\n"+
                "void main() {\n" +
                "     gl_Position = vec4(aPos, 1.0);\n" +
                "     ourColor = aColor;\n" +
                "}"
    private val fragmentShaderCode =
        "#version 300 es\n" +
                "precision mediump float;\n" +
                "in vec3 ourColor;\n"+
                "out vec4 fragColor;\n" +
                "void main() {\n" +
                "     fragColor = vec4(ourColor,0.0);\n" +
                "}"

    private var mProgram: Int

    init {
        val vertexShader: Int = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER).also { shader ->
            GLES30.glShaderSource(shader, vertexShaderCode)
            GLES30.glCompileShader(shader)
        }
        val fragmentShader: Int = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also { shader ->
            GLES30.glShaderSource(shader, fragmentShaderCode)
            GLES30.glCompileShader(shader)
        }

        mProgram = GLES30.glCreateProgram()
        GLES30.glAttachShader(mProgram, vertexShader)
        GLES30.glAttachShader(mProgram, fragmentShader)
        GLES30.glLinkProgram(mProgram)

        //VAO
        GLES30.glGenVertexArrays(1, vaoIds, 0)
        GLES30.glBindVertexArray(vaoIds[0])

        //VBO
        GLES30.glGenBuffers(1, vboIds, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboIds[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * Float.SIZE_BYTES, vertexBuffer, GLES30.GL_STATIC_DRAW)

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0)
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 6 * Float.SIZE_BYTES, 3 * Float.SIZE_BYTES)
        GLES30.glEnableVertexAttribArray(1)

        //EBO
        GLES30.glGenBuffers(1, eboIds, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, eboIds[0])
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.size * Int.SIZE_BYTES, indicesBuffer, GLES30.GL_STATIC_DRAW)

        GLES30.glBindVertexArray(0)
    }

    fun draw() {
        GLES30.glUseProgram(mProgram)

        GLES30.glBindVertexArray(vaoIds[0])

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 3, GLES30.GL_UNSIGNED_INT, 0)

        GLES30.glBindVertexArray(0)
    }
}