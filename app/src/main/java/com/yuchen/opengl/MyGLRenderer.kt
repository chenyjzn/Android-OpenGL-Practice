package com.yuchen.opengl

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Build
import androidx.annotation.RequiresApi
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(val context: Context) : GLSurfaceView.Renderer {

    lateinit var cube: Cube

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        cube = Cube(context)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        cube.draw()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        cube.width = width
        cube.height = height
    }
}