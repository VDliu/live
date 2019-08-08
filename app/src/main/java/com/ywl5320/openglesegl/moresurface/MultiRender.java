package com.ywl5320.openglesegl.moresurface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import com.ywl5320.openglesegl.R;
import com.ywl5320.openglesegl.WLEGLSurfaceView;
import com.ywl5320.openglesegl.WlShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class MultiRender implements WLEGLSurfaceView.WlGLRender {
    private static final String TAG = "MultiRender";

    private Context context;

    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            //三角形
            -0.25f, -0.25f,
            0.25f, -0.25f,
            0f, 0.15f
    };
    private FloatBuffer vertexBuffer;

    private float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    private FloatBuffer fragmentBuffer;

    private int program;
    private int vPosition;
    private int fPosition;
    private int sampler;

    private int vboId;
    private int textureId;
    private int imgTextureId;

    private float[] orMatrix = new float[16];
    private int uMatrixLocation;
    private int textureid;
    private int fragment_index;

    public MultiRender(Context context) {
        this.context = context;

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

    public void setTextureId(int textureId, int fragment_index) {
        this.textureId = textureId;
        this.fragment_index = fragment_index;

    }

    @Override
    public void onSurfaceCreated() {
        String vertexSource = WlShaderUtil.getRawResource(context, R.raw.vertex_shader);

        String fragmentSource = "";
        if (fragment_index == 0) {
            fragmentSource = WlShaderUtil.getRawResource(context, R.raw.fragment_shader1);
        } else if (fragment_index == 1) {
            fragmentSource = WlShaderUtil.getRawResource(context, R.raw.fragment_shader2);
        } else if (fragment_index == 2) {
            fragmentSource = WlShaderUtil.getRawResource(context, R.raw.fragment_shader3);
        }

        program = WlShaderUtil.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        sampler = GLES20.glGetUniformLocation(program, "sTexture");
        uMatrixLocation = GLES20.glGetUniformLocation(program, "u_Matrix");

        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        textureid = textureIds[0];


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glUniform1i(sampler, 1);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        imgTextureId = loadTexrute(R.drawable.girl);


        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }


    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.e(TAG, "onSurfaceChanged: w =" + width + ",h =" + height);
        GLES20.glViewport(0, 0, width, height);
//        if (width > height) { //横屏 ,下 上分别为-1 1 ，左右
//            //图片166x220 ,高度固定 此时为屏幕高度
//            // ratio = height / 220 为高度缩放比例
//            //realW = ratio * 167 为宽度缩放后的长度
//            //width / realW -->
//            Matrix.orthoM(orMatrix, 0, -width / ((height / 702f) * 526f), width / ((height / 702f) * 526f), -1, 1, -1f, 1f);
//        } else {//竖屏
//            Matrix.orthoM(orMatrix, 0, -1, 1, -height / ((width / 526f) * 702f), height / ((width / 526f) * 702f), 0f, 5f);
//        }
//        //绕着x周旋转180度  1 0 0 表示绕着x轴旋转 ，旋转的是顶点坐标
//        Matrix.rotateM(orMatrix, 0, 180, 1, 0, 0);

    }

    @Override
    public void onDrawFrame() {
        // Log.e(TAG, "onDrawFrame: --" );
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f, 1f, 0f, 1f);

        GLES20.glUseProgram(program);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1); //将glsl中的纹理对象 绑定在gpu的纹理1单元
        GLES20.glUniform1i(sampler, 1);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(vPosition);

        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        //绘制gril
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1); //将glsl中的纹理对象 绑定在gpu的纹理1单元
        GLES20.glUniform1i(sampler, 1);

        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                32);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(vPosition);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
        //end


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private int loadTexrute(int src) {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), src);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureIds[0];

    }
}
