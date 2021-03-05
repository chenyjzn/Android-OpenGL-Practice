package com.yuchen.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.opengl.GLES30
import android.opengl.GLUtils
import android.os.Build
import android.renderscript.Matrix4f
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class Triangle(private val context: Context) {

    private val vaoIds = IntArray(1)
    private val vboIds = IntArray(1)
    private val eboIds = IntArray(1)
    private val textureIds = IntArray(2)

    private val vertices = floatArrayOf(
            // positions          // colors           // texture coords
            0.5f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f,   1.0f, 1.0f,   // top right
            0.5f, -0.5f, 0.0f,   0.0f, 1.0f, 0.0f,   1.0f, 0.0f,   // bottom right
            -0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f,   0.0f, 0.0f,   // bottom left
            -0.5f,  0.5f, 0.0f,   1.0f, 1.0f, 0.0f,   0.0f, 1.0f    // top left
    )

    private val indices = intArrayOf(
            0, 1, 3,   // first triangle
            1, 2, 3    // second triangle
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
                "layout (location = 2) in vec2 aTexCoord;\n" +
                "out vec3 ourColor;\n"+
                "out vec2 TexCoord;\n"+
                "uniform mat4 transform;\n"+
                "void main() {\n" +
                "     gl_Position = transform * vec4(aPos, 1.0);\n" +
                "     ourColor = aColor;\n" +
                "     TexCoord = aTexCoord;\n" +
                "}"
    private val fragmentShaderCode =
        "#version 300 es\n" +
                "precision mediump float;\n" +
                "out vec4 FragColor;\n"+
                "in vec3 ourColor;\n" +
                "in vec2 TexCoord;\n" +
                "uniform sampler2D texture1;\n" +
                "uniform sampler2D texture2;\n" +
                "void main() {\n" +
                "     FragColor = mix(texture(texture1, TexCoord), texture(texture2, TexCoord), 0.2) * vec4(ourColor, 1.0);\n" +
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

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 8 * Float.SIZE_BYTES, 0)
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 8 * Float.SIZE_BYTES, 3 * Float.SIZE_BYTES)
        GLES30.glEnableVertexAttribArray(1)

        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 8 * Float.SIZE_BYTES, 6 * Float.SIZE_BYTES)
        GLES30.glEnableVertexAttribArray(2)

        //EBO
        GLES30.glGenBuffers(1, eboIds, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, eboIds[0])
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.size * Int.SIZE_BYTES, indicesBuffer, GLES30.GL_STATIC_DRAW)

        //Texture 0
        val bitmap0 = BitmapFactory.decodeResource(context.resources,R.drawable.container)

        GLES30.glGenTextures(2, textureIds,0)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap0, 0)

        bitmap0.recycle()

        //Texture 1
        val bitmap1 = BitmapFactory.decodeResource(context.resources,R.drawable.awesomeface)
        val matrix = Matrix()
        matrix.postScale(1.0f, -1.0f, bitmap1.width/2.0f, bitmap1.height/2.0f)
        val bitmap1Flip = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.width, bitmap1.height, matrix, true)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[1])

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap1Flip, 0)

        bitmap1.recycle()
        bitmap1Flip.recycle()
        // tell opengl for each sampler to which texture unit it belongs to (only has to be done once)
        GLES30.glUseProgram(mProgram)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(mProgram, "texture1"), 0)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(mProgram, "texture2"), 1)

        GLES30.glBindVertexArray(0)
    }

    fun draw() {
        GLES30.glUseProgram(mProgram)

        val matrix4f = Matrix4f()
        matrix4f.scale(0.5f, 0.5f, 0.5f)
        matrix4f.translate(0.5f, -0.5f, 0.0f)
        matrix4f.rotate(((System.currentTimeMillis()/10)%360).toFloat(), 0.0f, 0.0f, 1.0f)

        val transformLoc = GLES30.glGetUniformLocation(mProgram, "transform")
        GLES30.glUniformMatrix4fv(transformLoc, 1, false, matrix4f.array, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[1])

        GLES30.glBindVertexArray(vaoIds[0])

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0)

        GLES30.glBindVertexArray(0)
    }
}