package com.ywl5320.openglesegl.orthom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.ywl5320.openglesegl.FboRender;
import com.ywl5320.openglesegl.R;
import com.ywl5320.openglesegl.WLEGLSurfaceView;
import com.ywl5320.openglesegl.WlShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 投影矩阵
 */
public class OrTextureRender implements WLEGLSurfaceView.WlGLRender {
    private static final String TAG = "OrTextureRender";

    private Context context;


    /**
     *
     * 顶点坐标
     *              (0,1)
     *              |
     * (-1,0)---------------------(1,0)
     *              |
     *              (-1,0)
     *
     */
    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };
    private FloatBuffer vertexBuffer;

    //左上角为
    /**
     * android 纹理坐标
     *
     * (0,0)----------------->(1,0)
     *  |
     *  |
     *  |
     *  (0,1)----------------->(1,1)
     *
     *
     */
    private float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };



    /**
     * 离屏渲染为标准的纹理坐标 左下角为原点
     * ，此处是渲染离屏渲染中的framebufer,使用标准的opengl纹理坐标来绘制
     *
     * (0,1)----------------->(1,1)
     *  |
     *  |
     *  |
     *  (0,0)----------------->(1,0)
     */

    private float[] fboFragmentData = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f
    };

    private int w;
    private int h;
    private FloatBuffer fboFragmentBuffer;
    private FloatBuffer fragmentBuffer;

    private int program;
    private int vPosition;
    private int fPosition;
    private int textureid;
    private int sampler;

    private int vboId;
    private int fboId;

    private int imgTextureId;


    private int uMatrixLocation;

    private FboRender fboRender;

    private float[] orMatrix = new float[16];


    public OrTextureRender(Context context) {
        this.context = context;
        fboRender = new FboRender(context);
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

        fboFragmentBuffer = ByteBuffer.allocateDirect(fboFragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fboFragmentData);
        fboFragmentBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated() {

        fboRender.onCreate();
        String vertexSource = WlShaderUtil.getRawResource(context, R.raw.or_vertex_shader);
        String fragmentSource = WlShaderUtil.getRawResource(context, R.raw.fragment_shader);

        program = WlShaderUtil.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        sampler = GLES20.glGetUniformLocation(program, "sTexture");
        uMatrixLocation = GLES20.glGetUniformLocation(program, "u_Matrix");


        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4 + fboFragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, fboFragmentData.length * 4, fboFragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);


        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        textureid = textureIds[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sampler, 0);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1080, 1760, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureid, 0);
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("ywl5320", "fbo wrong");
        } else {
            Log.e("ywl5320", "fbo success");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //读取需要绘制在自定义的framebuffer中的纹理
        imgTextureId = loadTexrute(R.drawable.test);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        w= width;
        h= height;
        Log.e(TAG, "onSurfaceChanged: wi" + width + "h=" + height);
        // fboRender.onChange(width, height);
        float ratio = width > height ? (float) width / height : (float) height / width;

        if (width > height) { //横屏 ,下 上分别为-1 1 ，左右
            //图片166x220 ,高度固定 此时为屏幕高度
            // ratio = height / 220 为高度缩放比例
            //realW = ratio * 167 为宽度缩放后的长度
            //width / realW -->
            Matrix.orthoM(orMatrix, 0, -width / ((height / 220f) * 167f), width / ((height / 220f) * 167f), -1, 1, -1f, 1f);
        } else {//竖屏
            Matrix.orthoM(orMatrix, 0, -1, 1, -height / ((width / 167f) * 220f), height / ((width / 167f) * 220f), 0f, 5f);
        }
    }

    @Override
    public void onDrawFrame() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, orMatrix, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //之前都是离屏渲染，渲染在绑定的framebuffer上，
        //没有离屏渲染的时候 是渲染在默认的framebuffer中，就是平时看见的view就是在默认的framebuffer中
        //将自定义的framebuffer中的内容绘制在textureid纹理上，该纹理在默认的framebuffer中
         //fboRender.onDraw(textureid);
         onDraw(textureid);
    }

    public void onDraw(int textureId) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        GLES20.glUseProgram(program);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4 + fragmentData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

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
