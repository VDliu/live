package com.ywl5320.openglesegl;

import android.content.Context;
import android.util.AttributeSet;

public class WlGLTextureView extends WLEGLSurfaceView{

    public WlGLTextureView(Context context) {
        this(context, null);
    }

    public WlGLTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WlGLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new WlTextureRender(context));
    }
}
