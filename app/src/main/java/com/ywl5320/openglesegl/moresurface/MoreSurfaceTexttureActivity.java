package com.ywl5320.openglesegl.moresurface;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.ywl5320.openglesegl.R;

/**
 * 同一个纹理 渲染到不同的surface上面
 */

public class MoreSurfaceTexttureActivity extends AppCompatActivity {
    LinearLayout content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_surface);
        content = findViewById(R.id.ll_content);
        final MoreSurfaceGLTextureView textureView = findViewById(R.id.moresurface);
        textureView.getRender().setOnRenderTextureListener(new MoreSurfaceTextureRender.OnRenderTextureListener() {
            @Override
            public void onCreate(final int texture_id) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (content.getChildCount() > 0) {
                            content.removeAllViews();
                        }

                        for (int i = 0; i < 3; i++) {
                            MultiSurfaceView multiSurfaceView = new MultiSurfaceView(MoreSurfaceTexttureActivity.this);
                            //null表示用自己的surface,使用共享的eglContext,
                            multiSurfaceView.setSurfaceAndEglContext(null, textureView.getEglContext());
                            multiSurfaceView.setTextureid(texture_id, i);

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            lp.width = 200;
                            lp.height = 300;
                            multiSurfaceView.setLayoutParams(lp);
                            content.addView(multiSurfaceView);
                        }
                    }
                });
            }
        });
    }
}
