package com.ywl5320.openglesegl.orthom;

import android.content.Context;
import android.util.AttributeSet;

import com.ywl5320.openglesegl.WLEGLSurfaceView;

public class OrGLTextureView extends WLEGLSurfaceView{

    public OrGLTextureView(Context context) {
        this(context, null);
    }

    public OrGLTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OrGLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new OrTextureRender(context));
    }
}
