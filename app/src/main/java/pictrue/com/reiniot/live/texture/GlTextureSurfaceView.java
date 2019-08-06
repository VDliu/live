package pictrue.com.reiniot.live.texture;

import android.content.Context;
import android.util.AttributeSet;

import pictrue.com.reiniot.live.gl.LgGlSurfaceView;

/**
 * 2019/8/6.
 */
public class GlTextureSurfaceView extends LgGlSurfaceView {
    public GlTextureSurfaceView(Context context) {
        this(context, null);
    }

    public GlTextureSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlTextureSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new TextureRender(context));
    }
}
