package pictrue.com.reiniot.live.activity;

import android.opengl.GLES20;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import pictrue.com.reiniot.live.R;
import pictrue.com.reiniot.live.gl.LgEglHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = findViewById(R.id.surface);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(final SurfaceHolder holder, int format, final int width, final int height) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        LgEglHelper helper = new LgEglHelper();
                        helper.initEgl(holder.getSurface(), null);

                        while (true) {

                            GLES20.glViewport(0, 0, width, height);

                            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                            GLES20.glClearColor(0,1,0,1);
                            helper.swapBuffers();

                            try {
                                Thread.sleep(16);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });


    }
}
