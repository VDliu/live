package com.ywl5320.openglesegl.matrix_rotate;

import android.content.Context;
import android.util.AttributeSet;

import com.ywl5320.openglesegl.WLEGLSurfaceView;

public class MRotateGLTextureView extends WLEGLSurfaceView{

    public MRotateGLTextureView(Context context) {
        this(context, null);
    }

    public MRotateGLTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MRotateGLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new MRotateTextureRender(context));
    }
}
