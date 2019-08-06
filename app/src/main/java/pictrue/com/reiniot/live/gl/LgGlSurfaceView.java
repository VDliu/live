package pictrue.com.reiniot.live.gl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

/**
 * 2019/8/6.
 * 1.继承surfaceview,并实现callback回调
 * 2.自定义GLThread ,主要用于opengl绘制
 * 3.添加设置suface和eglcontext方法
 * 4.提供和系统GLSurfaceview相同的调用
 */
public abstract class LgGlSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private Surface surface;
    private EGLContext eglContext;
    private LgGlThread lgGlThread;
    private LGRender render;

    //手动刷新
    public final static int RENDERMODE_WHEN_DIRTY = 0;

    //自动刷新
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int renderMode = RENDERMODE_CONTINUOUSLY;


    public LgGlSurfaceView(Context context) {
        this(context, null);
    }

    public LgGlSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LgGlSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public void setEglContext(EGLContext eglContext) {
        this.eglContext = eglContext;
    }

    public void setRenderMode(int renderMode) {
        if (render == null) {
            throw new RuntimeException("render must not be null");
        }
        this.renderMode = renderMode;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (surface == null) {
            surface = holder.getSurface();
        }

        lgGlThread = new LgGlThread(new WeakReference<LgGlSurfaceView>(this));
        lgGlThread.setSurfaceCreate(true);
        lgGlThread.start();


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        lgGlThread.setSurfaceChange(true);
        lgGlThread.setSurfaceWidthAndHeight(width, height);


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        lgGlThread.onDestory();
        lgGlThread = null;
        eglContext = null;
    }

    public void setRender(LGRender render) {
        this.render = render;
    }

    public EGLContext getEglContext() {
        if (lgGlThread != null) {
            return lgGlThread.getEglContext();
        }
        return eglContext;
    }

    public void requestRender() {
        if (lgGlThread != null) {
            lgGlThread.requestRender();
        }
    }

    public interface LGRender {
        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onDrawFrame();

    }

    //绘制线程
    public static class LgGlThread extends Thread {
        private WeakReference<LgGlSurfaceView> glSurfaceViewWeakReference;
        private LgEglHelper eglHelper;
        private boolean isExit = false;
        private int surfaceWidth;
        private int surfaceHeight;
        private boolean isSurfaceCreate = false;
        private boolean isSurfaceChange = false;
        private Object lock;
        private boolean isStart = false;

        public void setSurfaceWidthAndHeight(int w, int h) {
            this.surfaceWidth = w;
            this.surfaceHeight = h;
        }

        public void setSurfaceCreate(boolean surfaceCreate) {
            isSurfaceCreate = surfaceCreate;
        }

        public void setSurfaceChange(boolean surfaceChange) {
            isSurfaceChange = surfaceChange;
        }

        public LgGlThread(WeakReference<LgGlSurfaceView> glSurfaceViewWeakReference) {
            this.glSurfaceViewWeakReference = glSurfaceViewWeakReference;
        }


        @Override
        public void run() {
            super.run();
            lock = new Object();
            eglHelper = new LgEglHelper();
            eglHelper.initEgl(glSurfaceViewWeakReference.get().surface, glSurfaceViewWeakReference.get().eglContext);

            while (true) {
                if (isExit) {
                    release();
                    break;
                }

                if (isStart) {
                    if (glSurfaceViewWeakReference.get().renderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (lock) {
                            try {
                                lock.wait(); //等待手动刷新
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (glSurfaceViewWeakReference.get().renderMode == RENDERMODE_CONTINUOUSLY) {
                        try {
                            Thread.sleep(1000 / 16); //美秒60帧率
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("render mode is wrong value");
                    }
                }

                onCreate();

                onSufaceChange();

                onDraw();
                isStart = true;

            }

        }

        private void onCreate() {
            if (isSurfaceCreate && glSurfaceViewWeakReference.get().render != null) {
                glSurfaceViewWeakReference.get().render.onSurfaceCreated();
                isSurfaceCreate = false;
            }
        }

        private void onSufaceChange() {
            if (isSurfaceChange && glSurfaceViewWeakReference.get().render != null) {
                glSurfaceViewWeakReference.get().render.onSurfaceChanged(surfaceWidth, surfaceHeight);
                isSurfaceChange = false;
            }
        }

        private void onDraw() {
            if (glSurfaceViewWeakReference.get().render != null && eglHelper != null) {
                glSurfaceViewWeakReference.get().render.onDrawFrame();

                if (!isStart) { //开始调用两次 解决第一次执行的不会绘制问题
                    glSurfaceViewWeakReference.get().render.onDrawFrame();
                }
                eglHelper.swapBuffers();
            }
        }

        private void requestRender() {
            if (lock == null) return;
            synchronized (lock) {
                lock.notify();
            }
        }

        public void onDestory() {
            isExit = true;
            requestRender();
        }

        private void release() {
            if (eglHelper != null) {
                eglHelper.destory();
                eglHelper = null;
                lock = null;
                glSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEglContext() {
            if (eglHelper != null) {
                return eglHelper.getmEglContext();
            }
            return null;
        }
    }
}
