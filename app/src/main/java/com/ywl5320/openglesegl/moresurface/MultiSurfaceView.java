package com.ywl5320.openglesegl.moresurface;

import android.content.Context;
import android.util.AttributeSet;

import com.ywl5320.openglesegl.WLEGLSurfaceView;

/**
 * 2019/8/8.
 * 渲染
 */
public class MultiSurfaceView extends WLEGLSurfaceView {
    private MultiRender render;

    public MultiSurfaceView(Context context) {
        this(context, null);
    }

    public MultiSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        render = new MultiRender(context);
        setRender(render);
    }

    public void setTextureid(int textureid,int fragment_index) {
        if (render != null) {
            render.setTextureId(textureid,fragment_index);
        }
    }
}
