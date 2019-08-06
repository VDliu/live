package pictrue.com.reiniot.live.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import pictrue.com.reiniot.live.R;
import pictrue.com.reiniot.live.gl.LgGlSurfaceView;
import pictrue.com.reiniot.live.gl.LgShaderUtil;

/**
 * 2019/8/6.
 */
public class TextureRender implements LgGlSurfaceView.LGRender {
    private Context mContext;

    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer fragmentBuffer;

    private float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f

//            0f, 0.5f,
//            0.5f, 0.5f,
//            0f, 0f,
//            0.5f, 0f
    };

    private int program;
    private int vPosition;
    private int fPosition;
    private int textureid;
    private int sampler;


    public TextureRender(Context context) {
        mContext = context;
        //转化为本地 防止被gc
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragmentData);
        fragmentBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated() {
        String vertexSource = LgShaderUtil.getRawResource(mContext, R.raw.vertex_shader);
        String fragmentSource = LgShaderUtil.getRawResource(mContext, R.raw.fragment_shader);

        program = LgShaderUtil.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        sampler = GLES20.glGetUniformLocation(program, "sTexture");

        int[] textureId = new int[1];
        //在显卡上开辟了一张纹理
        GLES20.glGenTextures(1, textureId, 0);
        textureid = textureId[0];
        //选择textureid作为当前纹理，后续对纹理的操作都将作用在此纹理上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid); //

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0); //当前纹理和GL_TEXTURE0 关联，当前纹理是textureid
        GLES20.glUniform1i(sampler, 0); //sampler 与 GLES20.GL_TEXTURE0 关联 ，因此textureid 和 sampler关联起来

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.test);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0); //都是在绑定的textureId上的操作

        bitmap.recycle();
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0, 1, 1, 1);

        GLES20.glUseProgram(program);
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);
        //使能顶点坐标
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);

        //使能纹理坐标
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }
}
