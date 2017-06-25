package com.catalinjurjiu.originalcoderemade;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.catalinjurjiu.animcubeandroid.LogUtil;

/**
 * Created by catalin on 22.06.2017.
 */

public class RenderThread extends Thread {

    private static final String TAG = "RenderThread";
    public final Object renderThreadMonitor = new Object();
    AnimCube animCube;
    SurfaceHolder surfaceHolder;
    boolean isDebuggable;
    private boolean hasPaintRequest = false;
    private boolean interrupted = false;
    private boolean interruptRequestConsumed = false;
    private boolean initComplete = false;
    private boolean paintRequestWhileInitNotComplete;


    public RenderThread() {
        super(TAG);
        LogUtil.d(TAG, "RenderThread: created", isDebuggable);
    }

    @Override
    public void run() {
        LogUtil.d(TAG, "RenderThread#run", isDebuggable);
        synchronized (renderThreadMonitor) {
            LogUtil.d(TAG, "RenderThread#run sync area", isDebuggable);
            while (true) {
                initComplete = true;
                if (!paintRequestWhileInitNotComplete) {
                    do {
                        LogUtil.d(TAG, "RenderThread#run before notify", isDebuggable);
                        renderThreadMonitor.notify();
                        LogUtil.d(TAG, "RenderThread#run after notify", isDebuggable);
                        try {
                            LogUtil.d(TAG, "RenderThread#run before wait", isDebuggable);

                            renderThreadMonitor.wait();
                            LogUtil.d(TAG, "RenderThread#run after wait - ok", isDebuggable);
                        } catch (InterruptedException e) {
                            LogUtil.d(TAG, "RenderThread#run after wait - interrupted", isDebuggable);
                            interrupted = true;
                            break;
                        }
                    } while (!hasPaintRequest);
                    LogUtil.d(TAG, "RenderThread#run after while", isDebuggable);
                } else {
                    paintRequestWhileInitNotComplete = false;
                    hasPaintRequest = true;
                }

                if (interrupted) {
                    LogUtil.d(TAG, "RenderThread#run interrupted, so we break of infinite while", isDebuggable);
                    break;
                }

                if (hasPaintRequest) {
                    LogUtil.d(TAG, "RenderThread#run have paint request, before lock canvas", isDebuggable);
                    if (surfaceHolder.getSurface().isValid()) {
                        LogUtil.w(TAG, "RenderThread#run CANVAS IS VALID", isDebuggable);
                        LogUtil.e(TAG, "RenderThread#run BEFORE some lock", isDebuggable);
                        LogUtil.e(TAG, "RenderThread#run some lock SYNC AREA", isDebuggable);
                        Canvas canvas = surfaceHolder.lockCanvas();
                        LogUtil.d(TAG, "RenderThread#run after lock canvas", isDebuggable);
                        if (canvas != null) {
                            LogUtil.d(TAG, "RenderThread#run CANVAS NOT NULL", isDebuggable);
                            animCube.performDraw(canvas);
                            LogUtil.d(TAG, "RenderThread#run after paint canvas", isDebuggable);
                            surfaceHolder.unlockCanvasAndPost(canvas);
                            LogUtil.d(TAG, "RenderThread#run after unlock and post canvas. FINISH", isDebuggable);
                        } else {
                            LogUtil.d(TAG, "RenderThread#run CANVAS IS NULL", isDebuggable);
                        }
                        LogUtil.e(TAG, "RenderThread#run END of some lock SYNC AREA", isDebuggable);
                        LogUtil.e(TAG, "RenderThread#run AFTER some lock", isDebuggable);
                    } else {
                        LogUtil.w(TAG, "RenderThread#run CANVAS NOT VALID", isDebuggable);
                    }
                    hasPaintRequest = false;
                    LogUtil.d(TAG, "RenderThread#run end - hasPaintRequest = false", isDebuggable);
                }
            }
            LogUtil.d(TAG, "RenderThread#run interrupt request consumed, notifying monitor", isDebuggable);
            interruptRequestConsumed = true;
            renderThreadMonitor.notify();
        }
    }

    public void requestPaint() {
        LogUtil.d(TAG, "RenderThread#request paint", isDebuggable);
        synchronized (renderThreadMonitor) {
            LogUtil.d(TAG, "RenderThread#request paint sync area", isDebuggable);
            if (initComplete) {
                if (!interrupted) {
                    LogUtil.d(TAG, "RenderThread#request paint sync area -> not interrupted, has paint request= true", isDebuggable);
                    hasPaintRequest = true;
                    LogUtil.d(TAG, "RenderThread#request paint sync area -> not interrupted, notifying monitor", isDebuggable);
                    renderThreadMonitor.notify();
                    LogUtil.d(TAG, "RenderThread#request paint end", isDebuggable);
                } else {
                    LogUtil.d(TAG, "RenderThread#request paint INTERRUPTED, DO NOTHING", isDebuggable);
                }
            } else {
                paintRequestWhileInitNotComplete = true;
            }
        }
    }

    public void requestShutdown() {
        LogUtil.d(TAG, "RenderThread#requestShutdown", isDebuggable);
        synchronized (renderThreadMonitor) {
            LogUtil.d(TAG, "RenderThread#requestShutdown sync area", isDebuggable);
            if (interruptRequestConsumed) {
                LogUtil.d(TAG, "RenderThread#requestShutdown Already interrupted", isDebuggable);
                //already interrupted
                return;
            }
            interrupted = true;
            LogUtil.d(TAG, "RenderThread#requestShutdown set interrupted to True. notifying", isDebuggable);
            renderThreadMonitor.notify();
            try {
                LogUtil.d(TAG, "RenderThread#requestShutdown before wait", isDebuggable);
                renderThreadMonitor.wait();
                LogUtil.d(TAG, "RenderThread#requestShutdown after wait - ok", isDebuggable);
            } catch (InterruptedException e) {
                LogUtil.d(TAG, "RenderThread#requestShutdown after wait - interrupted", isDebuggable);
                e.printStackTrace();
            } finally {
                LogUtil.d(TAG, "RenderThread#requestShutdown finally -> release references", isDebuggable);
                surfaceHolder = null;
                animCube = null;
                LogUtil.d(TAG, "RenderThread#requestShutdown finally -> done", isDebuggable);
            }
        }
    }

    public boolean hasFinished() {
        synchronized (renderThreadMonitor) {
            return interruptRequestConsumed;
        }
    }
}
