package pictrue.com.reiniot.live.lgglsurfaceview;

import android.opengl.GLES20;
import android.util.Log;

import pictrue.com.reiniot.live.gl.LgGlSurfaceView;

/**
 * 2019/8/6.
 */
public class MyRender implements LgGlSurfaceView.LGRender {
    private static final String TAG = "MyRender";

    public MyRender() {

    }

    @Override
    public void onSurfaceCreated() {
        Log.e(TAG, "onSurfaceCreated: " );

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Log.e(TAG, "onSurfaceChanged: " );
    }

    @Override
    public void onDrawFrame() {
        Log.e(TAG, "onDrawFrame: " );
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0,1,1,1);
    }
}
