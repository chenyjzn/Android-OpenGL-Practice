package com.yuchen.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MyGLSurfaceView : GLSurfaceView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private val renderer: MyGLRenderer

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)
        renderer = MyGLRenderer(context)
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
    }
}