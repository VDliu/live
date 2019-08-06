package pictrue.com.reiniot.live.lgglsurfaceview;

import android.content.Context;
import android.util.AttributeSet;

import pictrue.com.reiniot.live.gl.LgGlSurfaceView;

/**
 * 2019/8/6.
 */
public class MySurfaceView extends LgGlSurfaceView {
    public MySurfaceView(Context context) {
        this(context,null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new MyRender());
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
