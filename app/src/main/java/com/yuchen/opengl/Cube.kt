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

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class Cube(private val context: Context) {

    var width: Int = 0
    var height: Int = 0

    private val vaoIds = IntArray(1)
    private val vboIds = IntArray(1)
    private val textureIds = IntArray(2)

    private val vertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,
            0.5f, -0.5f, -0.5f,  1.0f, 0.0f,
            0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, 0.0f,

            -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
            0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,  0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,

            -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
            -0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

            0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
            0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,
            0.5f, -0.5f, -0.5f,  1.0f, 1.0f,
            0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
            0.5f, -0.5f,  0.5f,  1.0f, 0.0f,
            -0.5f, -0.5f,  0.5f,  0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,  0.0f, 1.0f,

            -0.5f,  0.5f, -0.5f,  0.0f, 1.0f,
            0.5f,  0.5f, -0.5f,  1.0f, 1.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
            0.5f,  0.5f,  0.5f,  1.0f, 0.0f,
            -0.5f,  0.5f,  0.5f,  0.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,  0.0f, 1.0f
    )

    private val cubePositions: List<Float> = listOf(
         0.0f,  0.0f,  0.0f,
         2.0f,  5.0f, -15.0f,
        -1.5f, -2.2f, -2.5f,
        -3.8f, -2.0f, -12.3f,
         2.4f, -0.4f, -3.5f,
        -1.7f,  3.0f, -7.5f,
         1.3f, -2.0f, -2.5f,
         1.5f,  2.0f, -2.5f,
         1.5f,  0.2f, -1.5f,
        -1.3f,  1.0f, -1.5f
    )

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
                    "layout (location = 2) in vec2 aTexCoord;\n" +
                    "out vec2 TexCoord;\n"+
                    "uniform mat4 model;\n"+
                    "uniform mat4 view;\n"+
                    "uniform mat4 projection;\n"+
                    "void main() {\n" +
                    "     gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
                    "     TexCoord = aTexCoord;\n" +
                    "}"
    private val fragmentShaderCode =
            "#version 300 es\n" +
                    "precision mediump float;\n" +
                    "out vec4 FragColor;\n"+
                    "in vec2 TexCoord;\n" +
                    "uniform sampler2D texture1;\n" +
                    "uniform sampler2D texture2;\n" +
                    "void main() {\n" +
                    "     FragColor = mix(texture(texture1, TexCoord), texture(texture2, TexCoord), 0.2);\n" +
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

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 5 * Float.SIZE_BYTES, 0)
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 5 * Float.SIZE_BYTES, 3 * Float.SIZE_BYTES)
        GLES30.glEnableVertexAttribArray(2)

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

        val viewMatrix4f = Matrix4f()
        android.opengl.Matrix.translateM(viewMatrix4f.array,0,0.0f,0.0f,-3.0f)

        val projectionMatrix4f = Matrix4f()
        if (height != 0 && width != 0) {
            android.opengl.Matrix.perspectiveM(projectionMatrix4f.array,0,45.0f, width.toFloat()/height.toFloat(), 0.1f, 100.0f)
        }

        val view = GLES30.glGetUniformLocation(mProgram, "view")
        GLES30.glUniformMatrix4fv(view, 1, false, viewMatrix4f.array, 0)

        val projection = GLES30.glGetUniformLocation(mProgram, "projection")
        GLES30.glUniformMatrix4fv(projection, 1, false, projectionMatrix4f.array, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[1])

        GLES30.glBindVertexArray(vaoIds[0])

        for(i in 0..9)
        {
            val modelMatrix4f = Matrix4f()
            android.opengl.Matrix.translateM(modelMatrix4f.array,0,cubePositions[3*i+0],cubePositions[3*i+1],cubePositions[3*i+2])
            android.opengl.Matrix.rotateM(modelMatrix4f.array,0,20.0f * i, 1.0f, 0.3f, 0.5f)

            val model = GLES30.glGetUniformLocation(mProgram, "model")
            GLES30.glUniformMatrix4fv(model, 1, false, modelMatrix4f.array, 0)

            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
        }

        GLES30.glBindVertexArray(0)
    }
}