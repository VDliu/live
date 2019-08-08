package com.ywl5320.openglesegl.moresurface;

import android.content.Context;
import android.util.AttributeSet;

import com.ywl5320.openglesegl.WLEGLSurfaceView;

public class MoreSurfaceGLTextureView extends WLEGLSurfaceView{
    MoreSurfaceTextureRender render;

    public MoreSurfaceGLTextureView(Context context) {
        this(context, null);
    }

    public MoreSurfaceGLTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoreSurfaceGLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        render = new  MoreSurfaceTextureRender(context);
        setRender(render);
    }

    public MoreSurfaceTextureRender getRender(){
        return render;
    }
}
