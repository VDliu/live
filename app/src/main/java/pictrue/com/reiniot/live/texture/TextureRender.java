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

    private int vboId;

    /**
     * 不使用VBO时，我们每次绘制（ glDrawArrays ）图形时都是从本地内存处获取顶点数据然后传输给OpenGL来绘制，这样就会频繁的操作CPU->GPU增大开销，从而降低效率。
     * 使用VBO，我们就能把顶点数据缓存到GPU开辟的一段内存中，然后使用时不必再从本地获取，而是直接从显存中获取，这样就能提升绘制的效率。
     */


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

        int[] vbos = new int[1];
        //在显卡上创建vbo
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];


        //绑定
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        //开辟vbo空间 传null表示先不放如数据
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);

        //设置顶点数据,先传入顶点坐标
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);

        //设置顶点数据,传入纹理坐标
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);

        //解绑
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

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
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //使能顶点坐标
        GLES20.glEnableVertexAttribArray(vPosition);
        //直接在cpu vertexBuffer中取出顶点坐标
        //GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);
        //在gpu vbo buffer中去取出顶点坐标
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0);

        //使能纹理坐标
        GLES20.glEnableVertexAttribArray(fPosition);
        //直接从cpu fragmentBuffer中获取到纹理坐标
        //GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer);
        //从gpu vbo中的buffer中获取到纹理坐标
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }
}
