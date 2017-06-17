package com.catalinjurjiu.animcubeandroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * @author Josef Jelinek @ http://software.rubikscube.info/AnimCube/
 *         gloom@email.cz
 *         <p>
 *         Adapted for Android by Catalin George Jurjiu
 *         catalin.jurjiu@gmail.com
 * @version 3.5b
 */
public final class AnimCube extends SurfaceView implements Runnable,
        View.OnTouchListener, SurfaceHolder.Callback {

    // to perform the animation
    private final Color[] colors = new Color[24];
    // cube facelets
    private final int[][] cube = new int[6][9];
    private final int[][] initialCube = new int[6][9];
    // initial observer co-ordinate axes (view)
    private final double[] eye = {0.0, 0.0, -1.0};
    private final double[] eyeX = {1.0, 0.0, 0.0}; // (sideways)
    private final double[] eyeY = new double[3]; // (vertical)
    private final double[] initialEye = new double[3];
    private final double[] initialEyeX = new double[3];
    private final double[] initialEyeY = new double[3];
    // subcube dimensions
    private final int[][][] topBlocks = new int[6][][];
    private final int[][][] midBlocks = new int[6][][];
    private final int[][][] botBlocks = new int[6][][];
    private final int[] twistBuffer = new int[12];
    private final int[][] dragCornersX = new int[18][4];
    private final int[][] dragCornersY = new int[18][4];
    private final double[] dragDirsX = new double[18];
    private final double[] dragDirsY = new double[18];
    // temporary eye vectors for twisted sub-cube rotation
    private final double[] tempEye = new double[3];
    private final double[] tempEyeX = new double[3];
    private final double[] tempEyeY = new double[3];
    // temporary eye vectors for second twisted sub-cube rotation (antislice)
    private final double[] tempEye2 = new double[3];
    private final double[] tempEyeX2 = new double[3];
    private final double[] tempEyeY2 = new double[3];
    // temporary vectors to compute visibility in perspective projection
    private final double[] perspEye = new double[3];
    private final double[] perspEyeI = new double[3];
    private final double[] perspNormal = new double[3];
    // eye arrays to store various eyes for various modes
    private final double[][] eyeArray = new double[3][];
    private final double[][] eyeArrayX = new double[3][];
    private final double[][] eyeArrayY = new double[3][];
    private final int[][][][] blockArray = new int[3][][][];
    // polygon co-ordinates to fill (cube faces or facelets)
    private final int[] fillX = new int[4];
    private final int[] fillY = new int[4];
    // projected vertex co-ordinates (to screen)
    private final double[] coordsX = new double[8];
    private final double[] coordsY = new double[8];
    private final double[][] cooX = new double[6][4];
    private final double[][] cooY = new double[6][4];
    private final double[] tempNormal = new double[3];
    private final Path path = new Path();
    private final double[] eyeD = new double[3];
    private final Object animThreadLock; // lock to synchronize the animThread
    private Thread animThread; // thread
    // background colors
    private Color bgColor;
    private Color bgColor2;
    private int[] dragLayers = new int[18]; // which layers belongs to
    // dragCorners
    private int[] dragModes = new int[18]; // which layer modes dragCorners
    // current twisted layer
    private int twistedLayer;
    private int twistedMode;
    // angle of rotation of the twistedLayer
    private double currentAngle; // edited angle of twisted layer
    private double originalAngle; // angle of twisted layer
    // animation speed
    private int speed;
    private int doubleSpeed;
    // current state of the program
    private boolean natural = true; // cube is compact, no layer is twisted
    private boolean interrupted; // thread was interrupted
    private boolean restarted; // animation was stopped
    private boolean mirrored; // mirroring of the cube view
    private boolean editable; // editation of the cube with a mouse
    private boolean animating; // animation run
    private boolean demo; // demo mode
    private int persp; // perspective deformation
    private double scale; // cube scale
    private int align; // cube alignment (top, center, bottom)
    private boolean hint = false;
    private double faceShift;
    // move sequence data
    private int[][] move;
    private int[][] demoMove;
    private int movePos;
    private int moveDir;
    private boolean moveOne;
    private boolean moveAnimated;
    private String[] infoText;
    private int curInfoText;
    // state of buttons
    private int progressHeight = 6;
    // double buffered animation
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // cube window size (applet window is resizable)
    private int width;
    private int height;
    // last position of mouse (for dragging the cube)
    private int lastX;
    private int lastY;
    // last position of mouse (when waiting for clear decission)
    private int lastDragX;
    private int lastDragY;
    // drag areas
    private int dragAreas;
    // current drag directions
    private double dragX;
    private double dragY;
    private boolean twisting;
    private boolean spinning;
    private SurfaceHolder surfaceHolder;
    private boolean mActionDownReceived;
    private boolean toTwist;
    private boolean dragging;
    private int curMove;
    private boolean pushed;
    private float touchSensitivityCoefficient;
    private String initialColorValues;

    public AnimCube(Context context) {
        this(context, null);
    }

    public AnimCube(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimCube(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        animThreadLock = new Object();
        animThread = new Thread(this, "Cube Animator");
        initViewsAndStates(context, attrs, defStyleAttr);
    }

    public void mousePressed(MotionEvent e) {
        lastDragX = lastX = Math.round(e.getX());
        lastDragY = lastY = Math.round(e.getY());
        toTwist = false;
        if (progressHeight > 0 && move.length > 0 && move[curMove].length > 0
                && lastY >= height - progressHeight && lastY < height) {
            stopAnimation();
            int len = CubeUtils.realMoveLength(move[curMove]);
            int pos = ((lastX - 1) * len * 2 / (width - 2) + 1) / 2;
            pos = Math.max(0, Math.min(len, pos));
            if (pos > 0) {
                pos = CubeUtils.arrayMovePos(move[curMove], pos);
            }
            if (pos > movePos) {
                doMove(cube, move[curMove], movePos, pos - movePos, false);
            }
            if (pos < movePos) {
                doMove(cube, move[curMove], pos, movePos - pos, true);
            }
            movePos = pos;
            dragging = true;
            repaint();
        } else {
            if (mirrored) {
                lastDragX = lastX = width - lastX;
            }
            if (editable && !animating) {// && (e.getPointerCount() > 1)) {
                toTwist = true;
            }
        }
    }

    public void mouseDragged(MotionEvent e) {

        if (pushed) {
            return;
        }
        if (dragging) {
            stopAnimation();
            int len = CubeUtils.realMoveLength(move[curMove]);
            int pos = (((Math.round(e.getX())) - 1) * len * 2 / (width - 2) + 1) / 2;
            pos = Math.max(0, Math.min(len, pos));
            if (pos > 0) {
                pos = CubeUtils.arrayMovePos(move[curMove], pos);
            }
            if (pos > movePos) {
                doMove(cube, move[curMove], movePos, pos - movePos, false);
            }
            if (pos < movePos) {
                doMove(cube, move[curMove], pos, movePos - pos, true);
            }
            movePos = pos;
            repaint();
            return;
        }
        int x = mirrored ? width - Math.round(e.getX()) : Math.round(e.getX());
        int y = Math.round(e.getY());
        int dx = Math.round((x - lastX) / touchSensitivityCoefficient);
        int dy = Math.round((y - lastY) / touchSensitivityCoefficient);
        if (editable && toTwist && !twisting && !animating) { // we do not twist
            // but we can
            lastDragX = x;
            lastDragY = y;
            for (int i = 0; i < dragAreas; i++) { // check if inside a drag area
                double d1 = dragCornersX[i][0];
                double x1 = dragCornersX[i][1] - d1;
                double y1 = dragCornersX[i][3] - d1;
                double d2 = dragCornersY[i][0];
                double x2 = dragCornersY[i][1] - d2;
                double y2 = dragCornersY[i][3] - d2;
                double a = (y2 * (lastX - d1) - y1 * (lastY - d2))
                        / (x1 * y2 - y1 * x2);
                double b = (-x2 * (lastX - d1) + x1 * (lastY - d2))
                        / (x1 * y2 - y1 * x2);
                if (a > 0 && a < 1 && b > 0 && b < 1) { // we are in
                    if (dx * dx + dy * dy < 144) { // delay the decision about
                        // twisting
                        return;
                    }
                    dragX = dragDirsX[i];
                    dragY = dragDirsY[i];
                    double d = Math.abs(dragX * dx + dragY * dy)
                            / Math.sqrt((dragX * dragX + dragY * dragY)
                            * (dx * dx + dy * dy));
                    if (d > 0.75) {
                        twisting = true;
                        twistedLayer = dragLayers[i];
                        twistedMode = dragModes[i];
                        break;
                    }
                }
            }
            toTwist = false;
            lastX = lastDragX;
            lastY = lastDragY;
        }

        dx = Math.round((x - lastX) / touchSensitivityCoefficient);
        dy = Math.round((y - lastY) / touchSensitivityCoefficient);
        if (!twisting || animating) { // whole cube rotation
            CubeUtils.vNorm(CubeUtils.vAdd(eye, CubeUtils.vScale(CubeUtils.vCopy(eyeD, eyeX), dx * -0.016)));
            CubeUtils.vNorm(CubeUtils.vMul(eyeX, eyeY, eye));
            CubeUtils.vNorm(CubeUtils.vAdd(eye, CubeUtils.vScale(CubeUtils.vCopy(eyeD, eyeY), dy * 0.016)));
            CubeUtils.vNorm(CubeUtils.vMul(eyeY, eye, eyeX));
            lastX = x;
            lastY = y;
        } else {
            if (natural) {
                splitCube(twistedLayer);
            }
            currentAngle = 0.03 * (dragX * dx + dragY * dy)
                    / Math.sqrt(dragX * dragX + dragY * dragY); // dv * cos a
        }
        repaint();
    }

    public void mouseReleased(MotionEvent e) {
        dragging = false;
        if (pushed) {
            pushed = false;
            repaint();
        } else if (twisting && !spinning) {
            twisting = false;
            originalAngle += currentAngle;
            currentAngle = 0.0;
            double angle = originalAngle;
            while (angle < 0.0) {
                angle += 32.0 * Math.PI;
            }
            int num = (int) (angle * 8.0 / Math.PI) % 16; // 2pi ~ 16
            if (num % 4 == 0 || num % 4 == 3) { // close enough to a corner
                num = (num + 1) / 4; // 2pi ~ 4
                if (CubeConstants.faceTwistDirs[twistedLayer] > 0) {
                    num = (4 - num) % 4;
                }
                originalAngle = 0;
                natural = true; // the cube in the natural state
                twistLayers(cube, twistedLayer, num, twistedMode); // rotate the
                // facelets
            }
            repaint();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (animating) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mActionDownReceived = true;
                mousePressed(event);
                break;
            case MotionEvent.ACTION_UP:
                if (mActionDownReceived) {
                    mouseReleased(event);
                    mActionDownReceived = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mouseDragged(event);
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("RubikSolver", "Surface Created");
        repaint();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d("RubikSolver", "Surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("RubikSolver", "Surface destroyed");
        this.surfaceHolder = null;
    }

    public void paint(Canvas canvas) {
        Log.d("RubikSolver", "repaint: paint canvas");
        paint.setColor(bgColor.colorCode);
        canvas.drawPaint(paint);
        int height = getHeight();
        int width = getWidth();
        // create offscreen buffer for double buffering
        if (width != this.width || height != this.height) {
            this.width = width;
            this.height = height;
        }

        synchronized (animThreadLock) {
            dragAreas = 0;
            if (natural) { // compact cube
                fixBlock(canvas, eye, eyeX, eyeY, CubeConstants.cubeBlocks, 3); // draw
                // cube and fill drag areas
            } else { // in twisted state
                // compute top observer
                double cosA = Math.cos(originalAngle + currentAngle);
                double sinA = Math.sin(originalAngle + currentAngle)
                        * CubeConstants.rotSign[twistedLayer];
                for (int i = 0; i < 3; i++) {
                    tempEye[i] = 0;
                    tempEyeX[i] = 0;
                    for (int j = 0; j < 3; j++) {
                        int axis = twistedLayer / 2;
                        tempEye[i] += eye[j]
                                * (CubeConstants.rotVec[axis][i][j]
                                + CubeConstants.rotCos[axis][i][j]
                                * cosA + CubeConstants.rotSin[axis][i][j]
                                * sinA);
                        tempEyeX[i] += eyeX[j]
                                * (CubeConstants.rotVec[axis][i][j]
                                + CubeConstants.rotCos[axis][i][j]
                                * cosA + CubeConstants.rotSin[axis][i][j]
                                * sinA);
                    }
                }
                CubeUtils.vMul(tempEyeY, tempEye, tempEyeX);
                // compute bottom anti-observer
                double cosB = Math.cos(originalAngle - currentAngle);
                double sinB = Math.sin(originalAngle - currentAngle)
                        * CubeConstants.rotSign[twistedLayer];
                for (int i = 0; i < 3; i++) {
                    tempEye2[i] = 0;
                    tempEyeX2[i] = 0;
                    for (int j = 0; j < 3; j++) {
                        int axis = twistedLayer / 2;
                        tempEye2[i] += eye[j] * (CubeConstants.rotVec[axis][i][j] + CubeConstants.rotCos[axis][i][j] * cosB + CubeConstants.rotSin[axis][i][j] * sinB);
                        tempEyeX2[i] += eyeX[j] * (CubeConstants.rotVec[axis][i][j] + CubeConstants.rotCos[axis][i][j] * cosB + CubeConstants.rotSin[axis][i][j] * sinB);
                    }
                }
                CubeUtils.vMul(tempEyeY2, tempEye2, tempEyeX2);
                eyeArray[0] = eye;
                eyeArrayX[0] = eyeX;
                eyeArrayY[0] = eyeY;
                eyeArray[1] = tempEye;
                eyeArrayX[1] = tempEyeX;
                eyeArrayY[1] = tempEyeY;
                eyeArray[2] = tempEye2;
                eyeArrayX[2] = tempEyeX2;
                eyeArrayY[2] = tempEyeY2;
                blockArray[0] = topBlocks;
                blockArray[1] = midBlocks;
                blockArray[2] = botBlocks;
                // perspective corrections
                CubeUtils.vSub(CubeUtils.vScale(CubeUtils.vCopy(perspEye, eye), 5.0 + persp),
                        CubeUtils.vScale(CubeUtils.vCopy(perspNormal,
                                CubeConstants.faceNormals[twistedLayer]),
                                1.0 / 3.0));
                CubeUtils.vSub(CubeUtils.vScale(CubeUtils.vCopy(perspEyeI, eye), 5.0 + persp),
                        CubeUtils.vScale(CubeUtils.vCopy(perspNormal,
                                CubeConstants.faceNormals[twistedLayer ^ 1]),
                                1.0 / 3.0));
                double topProd = CubeUtils.vProd(perspEye,
                        CubeConstants.faceNormals[twistedLayer]);
                double botProd = CubeUtils.vProd(perspEyeI,
                        CubeConstants.faceNormals[twistedLayer ^ 1]);
                int orderMode;
                if (topProd < 0 && botProd > 0) // top facing away
                    orderMode = 0;
                else if (topProd > 0 && botProd < 0) // bottom facing away: draw
                    // it first
                    orderMode = 1;
                else
                    // both top and bottom layer facing away: draw them first
                    orderMode = 2;
                fixBlock(
                        canvas,
                        eyeArray[CubeConstants.eyeOrder[twistedMode][CubeConstants.drawOrder[orderMode][0]]],
                        eyeArrayX[CubeConstants.eyeOrder[twistedMode][CubeConstants.drawOrder[orderMode][0]]],
                        eyeArrayY[CubeConstants.eyeOrder[twistedMode][CubeConstants.drawOrder[orderMode][0]]],
                        blockArray[CubeConstants.drawOrder[orderMode][0]],
                        CubeConstants.blockMode[twistedMode][CubeConstants.drawOrder[orderMode][0]]);
                fixBlock(
                        canvas,
                        eyeArray[CubeConstants.eyeOrder[twistedMode][CubeConstants.drawOrder[orderMode][1]]],
                        eyeArrayX[CubeConstants.eyeOrder[twistedMode][CubeConstants.drawOrder[orderMode][1]]],
                        eyeArrayY[CubeConstants.eyeOrder[twistedMode][CubeConstants.drawOrder[orderMode][1]]],
                        blockArray[CubeConstants.drawOrder[orderMode][1]],
                        CubeConstants.blockMode[twistedMode][CubeConstants.drawOrder[orderMode][1]]);
                fixBlock(
                        canvas,
                        eyeArray[CubeConstants.eyeOrder[twistedMode][CubeConstants.drawOrder[orderMode][2]]],
                        eyeArrayX[CubeConstants.eyeOrder[twistedMode][CubeConstants.drawOrder[orderMode][2]]],
                        eyeArrayY[CubeConstants.eyeOrder[twistedMode][CubeConstants.drawOrder[orderMode][2]]],
                        blockArray[CubeConstants.drawOrder[orderMode][2]],
                        CubeConstants.blockMode[twistedMode][CubeConstants.drawOrder[orderMode][2]]);
            }
        }
        Log.d("RubikSolver", "repaint: paint finished");
    } // paint()

    public void startAnimation(int mode) {
        Log.d("RubikSolver", "startAnimation, before sync lock");
        synchronized (animThreadLock) {
            Log.d("RubikSolver",
                    "startAnimation, after sync lock, before stopAnimation()");
            stopAnimation();
            Log.d("RubikSolver",
                    "startAnimation, after sync lock, after stopAnimation()");
            if (!demo && (move.length == 0 || move[curMove].length == 0)) {
                Log.d("RubikSolver", "startAnimation, first return");
                return;
            }
            if (demo && (demoMove.length == 0 || demoMove[0].length == 0)) {
                Log.d("RubikSolver", "startAnimation, second return");
                return;
            }
            moveDir = 1;
            moveOne = false;
            moveAnimated = true;
            switch (mode) {
                case CubeConstants.ANIMATION_PLAY_FORWARD: // play forward
                    break;
                case CubeConstants.ANIMATION_PLAY_BACKWARD: // play backward
                    moveDir = -1;
                    break;
                case CubeConstants.ANIMATION_STEP_FORWARD: // step forward
                    moveOne = true;
                    break;
                case CubeConstants.ANIMATION_STEP_BACKWARD: // step backward
                    moveDir = -1;
                    moveOne = true;
                    break;
                case CubeConstants.ANIMATION_FAST_FORWARD: // fast forward
                    moveAnimated = false;
                    break;
                case CubeConstants.ANIMATION_FAST_BACKWARD: // fast forward
                    moveAnimated = false;
                    moveDir = -1;
                    break;
            }
            Log.d("RubikSolver", "startAnimation: notify");
            animThreadLock.notify();
        }
    }

    public void stopAnimation() {
        Log.d("RubikSolver", "stopAnimation: before sync lock");
        synchronized (animThreadLock) {
            restarted = true;
            Log.d("RubikSolver", "stopAnimation: notify");
            animThreadLock.notify();
            try {
                Log.d("RubikSolver", "stopAnimation: wait");
                animThreadLock.wait();
                Log.d("RubikSolver", "stopAnimation: run");
            } catch (InterruptedException e) {
                Log.d("RubikSolver", "stopAnimation: interrupted exception");
                interrupted = true;
            }
            Log.d("RubikSolver", "stopAnimation: after try-catch");
            restarted = false;
        }
    }

    @Override
    public void run() {
        Log.d("RubikSolver", "run, before lock");
        synchronized (animThreadLock) {
            Log.d("RubikSolver", "run, after  lock");
            interrupted = false;
            do {
                if (surfaceHolder == null) {
                    Log.d("RubikSolver", "surface holder==null, sleep");
                    sleep(33 * speed);
                    Log.d("RubikSolver",
                            "surface holder==null, after sleep, continue sleep");
                    continue;
                }
                if (restarted) {
                    Log.d("RubikSolver", "run-restart: notify");
                    animThreadLock.notify();
                }
                try {
                    Log.d("RubikSolver", "run-after-restart: wait");
                    animThreadLock.wait();
                    Log.d("RubikSolver", "run-after-restart-wait: run");
                } catch (InterruptedException e) {
                    Log.d("RubikSolver",
                            "run-after-restart-wait: interrupted exception - break");
                    break;
                }
                if (restarted) {
                    Log.d("RubikSolver",
                            "run-after-restart-wait: interrupted exception - break");
                    continue;
                }
                boolean restart = false;
                animating = true;
                int[] mv = demo ? demoMove[0] : move[curMove];
                if (moveDir > 0) {
                    if (movePos >= mv.length) {
                        movePos = 0;
                    }
                } else {
                    curInfoText = -1;
                    if (movePos == 0) {
                        movePos = mv.length;
                    }
                }
                while (true) {
                    if (moveDir < 0) {
                        if (movePos == 0) {
                            break;
                        }
                        movePos--;
                    }
                    if (mv[movePos] == -1) {
                        // painting the newCanvas
                        Log.d("RubikSolver", "run: 989 before paint");
                        repaint();
                        Log.d("RubikSolver", "run: 989 after paint");
                        if (!moveOne) {
                            sleep(33 * speed);
                        }
                    } else if (mv[movePos] >= 1000) {
                        curInfoText = moveDir > 0 ? mv[movePos] - 1000 : -1;
                    } else {
                        int num = mv[movePos] % 4 + 1;
                        int mode = mv[movePos] / 4 % 6;
                        boolean clockwise = num < 3;
                        if (num == 4) {
                            num = 2;
                        }
                        if (moveDir < 0) {
                            clockwise = !clockwise;
                            num = 4 - num;
                        }
                        spin(mv[movePos] / 24, num, mode, clockwise,
                                moveAnimated);
                        if (moveOne) {
                            Log.d("RubikSolver", "run: 996 restart = true");
                            restart = true;
                        }
                    }
                    if (moveDir > 0) {
                        movePos++;
                        if (movePos < mv.length && mv[movePos] >= 1000) {
                            curInfoText = mv[movePos] - 1000;
                            movePos++;
                        }
                        if (movePos == mv.length) {
                            if (demo) {
                                Log.d("RubikSolver", "run: 1008 !demo->break");
                                break;
                            }
                            movePos = 0;

                            // stopAnimation();
                            // animating = false;
                            // for (int i = 0; i < 6; i++) {
                            // for (int j = 0; j < 9; j++) {
                            // cube[i][j] = initialCube[i][j];
                            // }
                            // }
                        }
                    } else {
                        curInfoText = -1;
                    }
                    if (interrupted || restarted || restart) {
                        Log.d("RubikSolver", "run: interrupted: " + interrupted
                                + " restarted: " + restarted + " restart: "
                                + restart + ". break,1022");
                        break;
                    }
                }
                animating = false;
                Log.d("RubikSolver", "run: after while(true),1027");
                Log.d("RubikSolver", "run: 1042 before paint");
                repaint();
                Log.d("RubikSolver", "run: 1044 after paint");
                if (demo) {
                    clear();
                    demo = false;
                }
            } while (!interrupted);
        }
        Log.d("RubikSolver", "Interrupted!1051");
    } // run()

    public void setCubeColors(String colorValues) {
        this.initialColorValues = colorValues;
        // setup color facelets
        if (colorValues.length() != 54) {
            return;
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                cube[i][j] = 23;
                for (int k = 0; k < 23; k++) {
                    if (Character.toLowerCase(colorValues.charAt(i * 9 + j)) == "0123456789wyorgbldmcpnk"
                            .charAt(k)) {
                        cube[i][j] = k;
                        break;
                    }
                }
            }
        }
    }

    private void setupColors() {
        // setup colors
        colors[0] = new Color(255, 128, 64); // 0 - light orange
        colors[1] = new Color(255, 0, 0); // 1 - pure red
        colors[2] = new Color(0, 255, 0); // 2 - pure green
        colors[3] = new Color(0, 0, 255); // 3 - pure blue
        colors[4] = new Color(153, 153, 153); // 4 - white grey
        colors[5] = new Color(170, 170, 68); // 5 - yellow grey
        colors[6] = new Color(187, 119, 68); // 6 - orange grey
        colors[7] = new Color(153, 68, 68); // 7 - red grey
        colors[8] = new Color(68, 119, 68); // 8 - green grey
        colors[9] = new Color(0, 68, 119); // 9 - blue grey
        colors[10] = new Color(255, 255, 255); // W - white
        colors[11] = new Color(255, 255, 0); // Y - yellow
        colors[12] = new Color(255, 96, 32); // O - orange
        colors[13] = new Color(208, 0, 0); // R - red
        colors[14] = new Color(0, 144, 0); // G - green
        colors[15] = new Color(32, 64, 208); // B - blue
        colors[16] = new Color(176, 176, 176); // L - light gray
        colors[17] = new Color(80, 80, 80); // D - dark gray
        colors[18] = new Color(255, 0, 255); // M - magenta
        colors[19] = new Color(0, 255, 255); // C - cyan
        colors[20] = new Color(255, 160, 192); // P - pink
        colors[21] = new Color(32, 255, 16); // N - light green
        colors[22] = new Color(0, 0, 0); // K - black
        colors[23] = new Color(128, 128, 128); // . - gray
    }

    public void setMoveSequence(String moveSequence) {
        move = getMove(moveSequence, false);
    }

    public void resetToInitialState() {
        stopAnimation();
        movePos = 0;
        setCubeColors(initialColorValues);
        repaint();
    }

    public void setShowBackFace(boolean showBackFace) {
        if (showBackFace) {
            hint = true;
            faceShift = 5;
            if (faceShift < 1.0) {
                hint = false;
            } else {
                faceShift /= 10.0;
            }
        } else {
            hint = false;
            faceShift = 0;
        }
    }

    public void setTouchSensitivity(String touchSensitivityString) {
        float sensitivityCoeff = 0;
        if (touchSensitivityString.equals("Normal")) {
            sensitivityCoeff = 1.0f;
        }
        if (touchSensitivityString.equals("High")) {
            sensitivityCoeff = 2.0f;
        }
        if (touchSensitivityString.equals("Low")) {
            sensitivityCoeff = 0.5f;
        }
        touchSensitivityCoefficient = 5.0f * 1.0f / sensitivityCoeff;
    }

    public void setAnimationSpeed(String animationSpeedString) {
        int animationSpeed = 0;
        if (animationSpeedString.equals("Normal")) {
            animationSpeed = 5;
        } else if (animationSpeedString.equals("Fast")) {
            animationSpeed = 2;
        } else if (animationSpeedString.equals("Slow")) {
            animationSpeed = 9;
        }
        speed = (int) animationSpeed;
        doubleSpeed = speed * 3 / 2;
    }

    public void onDestroy() {
        if (animThread != null) {
            animThread.interrupt();
        }
        animThread = null;
    }

    private void initViewsAndStates(Context context, AttributeSet attrs,
                                    int defStyleAttr) {
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.AnimCube);

        int styleableIndex = R.styleable.AnimCube_backgroundColor;
        if (attributes.hasValue(styleableIndex)) {
            bgColor = new Color(attributes.getColor(styleableIndex,
                    android.graphics.Color.WHITE));
        } else {
            bgColor = new Color(android.graphics.Color.WHITE);
        }

        styleableIndex = R.styleable.AnimCube_editable;
        if (attributes.hasValue(styleableIndex)) {
            editable = attributes.getBoolean(styleableIndex, false);
        } else {
            editable = false;
        }

        String initialPosition = CubeConstants.DEFAULT_INITIAL_CUBE_ROTATION;
        styleableIndex = R.styleable.AnimCube_initialRotation;
        if (attributes.hasValue(styleableIndex)) {
            initialPosition = attributes.getString(styleableIndex);

        }
        setupInitialViewAngle(initialPosition);

        styleableIndex = R.styleable.AnimCube_backFacesDistance;
        if (attributes.hasValue(styleableIndex)) {
            int backFaceDistance = attributes.getInt(styleableIndex, 0);
            if (backFaceDistance >= 2 && backFaceDistance <= 10) {
                hint = true;
                faceShift = backFaceDistance;
                if (faceShift < 1.0) {
                    hint = false;
                } else {
                    faceShift /= 10.0;
                }
            } else {
                hint = false;
                faceShift = 0;
            }
        } else {
            hint = false;
            faceShift = 0;
        }

        float sensitivityCoeff;
        styleableIndex = R.styleable.AnimCube_touchSensitivity;
        if (attributes.hasValue(styleableIndex)) {
            sensitivityCoeff = attributes.getFloat(styleableIndex, 1.0f);
        } else {
            sensitivityCoeff = 1.0f;
        }

        if (sensitivityCoeff < 0.1) {
            sensitivityCoeff = 0.1f;
        } else if (sensitivityCoeff > 2) {
            sensitivityCoeff = 2f;
        }

        touchSensitivityCoefficient = 5.0f * 1.0f / sensitivityCoeff;
        int intscale;
        styleableIndex = R.styleable.AnimCube_scale;
        if (attributes.hasValue(styleableIndex)) {
            intscale = attributes.getInt(styleableIndex, 0);
        } else {
            intscale = 0;
        }
        scale = 1.0 / (1.0 + intscale / 10.0);

        styleableIndex = R.styleable.AnimCube_perspective;
        if (attributes.hasValue(styleableIndex)) {
            persp = attributes.getInt(styleableIndex, 2);
        } else {
            persp = 2;
        }

        styleableIndex = R.styleable.AnimCube_verticalAlign;
        if (attributes.hasValue(styleableIndex)) {
            String alignParam = attributes.getString(styleableIndex);
            if (alignParam == null) {
                align = 1;
            } else {
                if (CubeConstants.TOP_ALIGN.equals(alignParam)) // top
                    align = 0;
                else if (CubeConstants.CENTER_ALIGN.equals(alignParam)) // center
                    align = 1;
                else if (CubeConstants.BOTTOM_ALIGN.equals(alignParam)) // bottom
                    align = 2;
            }
        } else {
            align = 1;
        }

        styleableIndex = R.styleable.AnimCube_single_rotation_speed;
        if (attributes.hasValue(styleableIndex)) {
            speed = attributes.getInt(styleableIndex, 5);
        } else {
            speed = 5;
        }

        styleableIndex = R.styleable.AnimCube_double_rotation_speed;
        if (attributes.hasValue(styleableIndex)) {
            doubleSpeed = attributes.getInt(styleableIndex, speed * 3 / 2);
        } else {
            doubleSpeed = speed * 3 / 2;
        }

        attributes.recycle();

        // get the surface holder of he current surface view, add this view as a
        // callback
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        // register to receive all mouse events
        setOnTouchListener(this);
        setupColors();

        // clean the cube
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                cube[i][j] = i + 10;
            }
        }

        // setup move sequence (and info texts)
        move = new int[0][0];
        movePos = 0;
        curInfoText = -1;

        // hint displaying
        progressHeight = move.length == 0 ? 0 : 6;

        // setup initial values
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                initialCube[i][j] = cube[i][j];
            }
        }
        for (int i = 0; i < 3; i++) {
            initialEye[i] = eye[i];
            initialEyeX[i] = eyeX[i];
            initialEyeY[i] = eyeY[i];
        }
        // setup colors (contrast)
        int red = bgColor.red;
        int green = bgColor.green;
        int blue = bgColor.blue;

        bgColor2 = new Color(red / 2, green / 2, blue / 2);
        curInfoText = -1;

        // paint first frame
        repaint();
        // start animation thread
        animThread.start();
    } // initViewsAndStates()

    private void setupInitialViewAngle(String initialPosition) {
        CubeUtils.vNorm(CubeUtils.vMul(eyeY, eye, eyeX));
        double pi12 = Math.PI / 12;
        for (int i = 0; i < initialPosition.length(); i++) {
            double angle = pi12;
            switch (Character.toLowerCase(initialPosition.charAt(i))) {
                case 'd':
                    angle = -angle;
                case 'u':
                    CubeUtils.vRotY(eye, angle);
                    CubeUtils.vRotY(eyeX, angle);
                    break;
                case 'f':
                    angle = -angle;
                case 'b':
                    CubeUtils.vRotZ(eye, angle);
                    CubeUtils.vRotZ(eyeX, angle);
                    break;
                case 'l':
                    angle = -angle;
                case 'r':
                    CubeUtils.vRotX(eye, angle);
                    CubeUtils.vRotX(eyeX, angle);
                    break;
            }
        }
        CubeUtils.vNorm(CubeUtils.vMul(eyeY, eye, eyeX)); // fix eyeY
    }

    // Various useful vector functions
    private void sleep(int time) {
        Log.d("RubikSolver", "sleep: before sync block time - " + time);
        synchronized (animThreadLock) {
            Log.d("RubikSolver", "sleep: in sync block time - " + time);
            try {
                Log.d("RubikSolver", "sleep: in sync block in try before wait");
                animThreadLock.wait(time);
                Log.d("RubikSolver", "sleep: in sync block in try after wait");
            } catch (InterruptedException e) {
                Log.d("RubikSolver", "sleep: interrupted exception");
                interrupted = true;
            }
        }
        Log.d("RubikSolver", "sleep: after sync block");
    }

    private void clear() {
        Log.d("RubikSolver", "clear: before sync block");
        synchronized (animThreadLock) {
            Log.d("RubikSolver", "clear: in sync block");
            movePos = 0;
            natural = true;
            mirrored = false;
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 9; j++) {
                    cube[i][j] = initialCube[i][j];
                }
            }
            for (int i = 0; i < 3; i++) {
                eye[i] = initialEye[i];
                eyeX[i] = initialEyeX[i];
                eyeY[i] = initialEyeY[i];
            }
        }
        Log.d("RubikSolver", "clear: after sync block");
    }

    private int[][] getMove(String sequence, boolean info) {
        if (info) {
            int inum = 0;
            int pos = sequence.indexOf('{');
            while (pos != -1) {
                inum++;
                pos = sequence.indexOf('{', pos + 1);
            }
            if (infoText == null) {
                curInfoText = 0;
                infoText = new String[inum];
            } else {
                String[] infoText2 = new String[infoText.length + inum];
                for (int i = 0; i < infoText.length; i++)
                    infoText2[i] = infoText[i];
                curInfoText = infoText.length;
                infoText = infoText2;
            }
        }
        int num = 1;
        int pos = sequence.indexOf(';');
        while (pos != -1) {
            num++;
            pos = sequence.indexOf(';', pos + 1);
        }
        int[][] move = new int[num][];
        int lastPos = 0;
        pos = sequence.indexOf(';');
        num = 0;
        while (pos != -1) {
            move[num++] = getMovePart(sequence.substring(lastPos, pos), info);
            lastPos = pos + 1;
            pos = sequence.indexOf(';', lastPos);
        }
        move[num] = getMovePart(sequence.substring(lastPos), info);
        return move;
    }

    private int[] getMovePart(String sequence, boolean info) {
        int length = 0;
        int[] move = new int[sequence.length()]; // overdimmensioned
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '.') {
                move[length] = -1;
                length++;
            } else if (sequence.charAt(i) == '{') {
                i++;
                String s = "";
                while (i < sequence.length()) {
                    if (sequence.charAt(i) == '}')
                        break;
                    if (info)
                        s += sequence.charAt(i);
                    i++;
                }
                if (info) {
                    infoText[curInfoText] = s;
                    move[length] = 1000 + curInfoText;
                    curInfoText++;
                    length++;
                }
            } else {
                for (int j = 0; j < 21; j++) {
                    if (sequence.charAt(i) == "UDFBLRESMXYZxyzudfblr".charAt(j)) {
                        i++;
                        int mode = CubeConstants.moveModes[j];
                        move[length] = CubeConstants.moveCodes[j] * 24;
                        if (i < sequence.length()) {
                            if (CubeConstants.moveModes[j] == 0) { // modifiers
                                // for basic
                                // characters
                                // UDFBLR
                                for (int k = 0; k < CubeConstants.modeChar.length; k++) {
                                    if (sequence.charAt(i) == CubeConstants.modeChar[k]) {
                                        mode = k + 1;
                                        i++;
                                        break;
                                    }
                                }
                            }
                        }
                        move[length] += mode * 4;
                        if (i < sequence.length()) {
                            if (sequence.charAt(i) == '1')
                                i++;
                            else if (sequence.charAt(i) == '\''
                                    || sequence.charAt(i) == '3') {
                                move[length] += 2;
                                i++;
                            } else if (sequence.charAt(i) == '2') {
                                i++;
                                if (i < sequence.length()
                                        && sequence.charAt(i) == '\'') {
                                    move[length] += 3;
                                    i++;
                                } else
                                    move[length] += 1;
                            }
                        }
                        length++;
                        i--;
                        break;
                    }
                }
            }
        }
        int[] returnMove = new int[length];
        for (int i = 0; i < length; i++)
            returnMove[i] = move[i];
        return returnMove;
    }

    private void doMove(int[][] cube, int[] move, int start, int length,
                        boolean reversed) {
        int position = reversed ? start + length : start;
        while (true) {
            if (reversed) {
                if (position <= start) {
                    break;
                }
                position--;
            }
            if (move[position] >= 1000) {
                curInfoText = reversed ? -1 : move[position] - 1000;
            } else if (move[position] >= 0) {
                int modifier = move[position] % 4 + 1;
                int mode = move[position] / 4 % 6;
                if (modifier == 4) { // reversed double turn
                    modifier = 2;
                }
                if (reversed) {
                    modifier = 4 - modifier;
                }
                twistLayers(cube, move[position] / 24, modifier, mode);
            }
            if (!reversed) {
                position++;
                if (position >= start + length) {
                    break;
                }
            }
        }
    }

    private void spin(int layer, int num, int mode, boolean clockwise,
                      boolean animated) {
        twisting = false;
        natural = true;
        spinning = true;
        originalAngle = 0;
        if (CubeConstants.faceTwistDirs[layer] > 0) {
            clockwise = !clockwise;
        }
        if (animated) {
            double phit = Math.PI / 2; // target for currentAngle (default pi/2)
            double phis = clockwise ? 1.0 : -1.0; // sign
            int turnTime = 67 * speed; // milliseconds to be used for one turn
            if (num == 2) {
                phit = Math.PI;
                turnTime = 67 * doubleSpeed; // double turn is usually faster
                // than two quarter turns
            }
            twisting = true;
            twistedLayer = layer;
            twistedMode = mode;
            splitCube(layer); // start twisting
            long sTime = System.currentTimeMillis();
            long lTime = sTime;
            double d = phis * phit / turnTime;
            for (currentAngle = 0; currentAngle * phis < phit; currentAngle = d
                    * (lTime - sTime)) {
                Log.d("RubikSolver", "spin: 1273 after paint");
                repaint();
                Log.d("RubikSolver", "spin: 1274 after paint, sleep next of 25");
                sleep(25);
                if (interrupted || restarted) {
                    break;
                }
                lTime = System.currentTimeMillis();
            }
        }
        currentAngle = 0;
        twisting = false;
        natural = true;
        twistLayers(cube, layer, num, mode);
        spinning = false;
        if (animated) {
            Log.d("RubikSolver", "spin: 1289 before paint, is animated");
            repaint();
            Log.d("RubikSolver", "spin: 1291 after paint, is animated");
        }
    }

    private void repaint() {
        Log.d("RubikSolver", "repaint: beforeCondition");
        if (this.surfaceHolder != null
                && this.surfaceHolder.getSurface().isValid()) {
            Log.d("RubikSolver", "repaint: have holder, is valid");
            Canvas canvas = surfaceHolder.lockCanvas();
            paint(canvas);
            getHolder().unlockCanvasAndPost(canvas);
            Log.d("RubikSolver", "repaint: canvas unlocked");
        }
    }

    private void splitCube(int layer) {
        for (int i = 0; i < 6; i++) { // for all faces
            topBlocks[i] = CubeConstants.topBlockTable[CubeConstants.topBlockFaceDim[layer][i]];
            botBlocks[i] = CubeConstants.topBlockTable[CubeConstants.botBlockFaceDim[layer][i]];
            midBlocks[i] = CubeConstants.midBlockTable[CubeConstants.midBlockFaceDim[layer][i]];
        }
        natural = false;
    }

    private void twistLayers(int[][] cube, int layer, int num, int mode) {
        switch (mode) {
            case 3:
                twistLayer(cube, layer ^ 1, num, false);
            case 2:
                twistLayer(cube, layer, 4 - num, false);
            case 1:
                twistLayer(cube, layer, 4 - num, true);
                break;
            case 5:
                twistLayer(cube, layer ^ 1, 4 - num, false);
                twistLayer(cube, layer, 4 - num, false);
                break;
            case 4:
                twistLayer(cube, layer ^ 1, num, false);
            default:
                twistLayer(cube, layer, 4 - num, false);
        }
    }

    private void twistLayer(int[][] cube, int layer, int num, boolean middle) {
        if (!middle) {
            // rotate top facelets
            for (int i = 0; i < 8; i++) { // to buffer
                twistBuffer[(i + num * 2) % 8] = cube[layer][CubeConstants.cycleOrder[i]];
            }
            for (int i = 0; i < 8; i++) {// to cube
                cube[layer][CubeConstants.cycleOrder[i]] = twistBuffer[i];
            }
        }
        // rotate side facelets
        int k = num * 3;
        for (int i = 0; i < 4; i++) { // to buffer
            int n = CubeConstants.adjacentFaces[layer][i];
            int c = middle ? CubeConstants.cycleCenters[layer][i]
                    : CubeConstants.cycleLayerSides[layer][i];
            int factor = CubeConstants.cycleFactors[c];
            int offset = CubeConstants.cycleOffsets[c];
            for (int j = 0; j < 3; j++) {
                twistBuffer[k % 12] = cube[n][j * factor + offset];
                k++;
            }
        }
        k = 0; // MS VM JIT bug if placed into the loop initViewsAndStates
        for (int i = 0; i < 4; i++) { // to cube
            int n = CubeConstants.adjacentFaces[layer][i];
            int c = middle ? CubeConstants.cycleCenters[layer][i]
                    : CubeConstants.cycleLayerSides[layer][i];
            int factor = CubeConstants.cycleFactors[c];
            int offset = CubeConstants.cycleOffsets[c];
            int j = 0; // MS VM JIT bug if for is used
            while (j < 3) {
                cube[n][j * factor + offset] = twistBuffer[k];
                j++;
                k++;
            }
        }
    }

    private void fixBlock(Canvas canvas, double[] eye, double[] eyeX,
                          double[] eyeY, int[][][] blocks, int mode) {
        // project 3D co-ordinates into 2D screen ones
        for (int i = 0; i < 8; i++) {
            double min = width < height ? width : height - progressHeight;
            double x = min / 3.7 * CubeUtils.vProd(CubeConstants.cornerCoords[i], eyeX)
                    * scale;
            double y = min / 3.7 * CubeUtils.vProd(CubeConstants.cornerCoords[i], eyeY)
                    * scale;
            double z = min / (5.0 + persp)
                    * CubeUtils.vProd(CubeConstants.cornerCoords[i], eye) * scale;
            x = x / (1 - z / min); // perspective transformation
            y = y / (1 - z / min); // perspective transformation
            coordsX[i] = width / 2.0 + x;
            if (align == 0)
                coordsY[i] = (height - progressHeight) / 2.0 * scale - y;
            else if (align == 2)
                coordsY[i] = height - progressHeight
                        - (height - progressHeight) / 2.0 * scale - y;
            else
                coordsY[i] = (height - progressHeight) / 2.0 - y;
        }
        // setup corner co-ordinates for all faces
        for (int i = 0; i < 6; i++) { // all faces
            for (int j = 0; j < 4; j++) { // all face corners
                cooX[i][j] = coordsX[CubeConstants.faceCorners[i][j]];
                cooY[i][j] = coordsY[CubeConstants.faceCorners[i][j]];
            }
        }
        if (hint) { // draw hint hiden facelets
            for (int i = 0; i < 6; i++) { // all faces
                CubeUtils.vSub(CubeUtils.vScale(CubeUtils.vCopy(perspEye, eye), 5.0 + persp),
                        CubeConstants.faceNormals[i]); // perspective correction
                if (CubeUtils.vProd(perspEye, CubeConstants.faceNormals[i]) < 0) { // draw
                    // only
                    // hiden
                    // faces
                    CubeUtils.vScale(CubeUtils.vCopy(tempNormal, CubeConstants.faceNormals[i]),
                            faceShift);
                    double min = width < height ? width : height
                            - progressHeight;
                    double x = min / 3.7 * CubeUtils.vProd(tempNormal, eyeX);
                    double y = min / 3.7 * CubeUtils.vProd(tempNormal, eyeY);
                    double z = min / (5.0 + persp) * CubeUtils.vProd(tempNormal, eye);
                    x = x / (1 - z / min); // perspective transformation
                    y = y / (1 - z / min); // perspective transformation
                    int sideW = blocks[i][0][1] - blocks[i][0][0];
                    int sideH = blocks[i][1][1] - blocks[i][1][0];
                    if (sideW > 0 && sideH > 0) { // this side is not only black
                        // draw colored facelets
                        for (int n = 0, p = blocks[i][1][0]; n < sideH; n++, p++) {
                            for (int o = 0, q = blocks[i][0][0]; o < sideW; o++, q++) {
                                for (int j = 0; j < 4; j++) {
                                    getCorners(i, j, fillX, fillY, q
                                                    + CubeConstants.border[j][0], p
                                                    + CubeConstants.border[j][1],
                                            mirrored);
                                    fillX[j] += mirrored ? -x : x;
                                    fillY[j] -= y;
                                }
                                paint.setColor(colors[cube[i][p * 3 + q]].colorCode);
                                paint.setStyle(Paint.Style.FILL);

                                path.reset();
                                path.moveTo(fillX[0], fillY[0]);
                                path.lineTo(fillX[1], fillY[1]);
                                path.lineTo(fillX[2], fillY[2]);
                                path.lineTo(fillX[3], fillY[3]);
                                path.close();

                                canvas.drawPath(path, paint);
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setColor(colors[cube[i][p * 3 + q]].darker());
                                canvas.drawPath(path, paint);
                            }
                        }
                    }
                }
            }
        }
        // draw black antialias
        for (int i = 0; i < 6; i++) { // all faces
            int sideW = blocks[i][0][1] - blocks[i][0][0];
            int sideH = blocks[i][1][1] - blocks[i][1][0];
            if (sideW > 0 && sideH > 0) {
                for (int j = 0; j < 4; j++)
                    // corner co-ordinates
                    getCorners(i, j, fillX, fillY,
                            blocks[i][0][CubeConstants.factors[j][0]],
                            blocks[i][1][CubeConstants.factors[j][1]], mirrored);
                if (sideW == 3 && sideH == 3) {
                    paint.setColor(bgColor2.colorCode);
                } else {
                    paint.setColor(android.graphics.Color.BLACK);
                }
                path.reset();
                path.moveTo(fillX[0], fillY[0]);
                path.lineTo(fillX[1], fillY[1]);
                path.lineTo(fillX[2], fillY[2]);
                path.lineTo(fillX[3], fillY[3]);
                path.close();
                canvas.drawPath(path, paint);
            }
        }
        // find and draw black inner faces
        for (int i = 0; i < 6; i++) { // all faces
            int sideW = blocks[i][0][1] - blocks[i][0][0];
            int sideH = blocks[i][1][1] - blocks[i][1][0];
            if (sideW <= 0 || sideH <= 0) { // this face is inner and only black
                for (int j = 0; j < 4; j++) { // for all corners
                    int k = CubeConstants.oppositeCorners[i][j];
                    fillX[j] = (int) (cooX[i][j] + (cooX[i ^ 1][k] - cooX[i][j]) * 2.0 / 3.0);
                    fillY[j] = (int) (cooY[i][j] + (cooY[i ^ 1][k] - cooY[i][j]) * 2.0 / 3.0);
                    if (mirrored)
                        fillX[j] = width - fillX[j];
                }
                paint.setColor(android.graphics.Color.BLACK);

                path.reset();
                path.moveTo(fillX[0], fillY[0]);
                path.lineTo(fillX[1], fillY[1]);
                path.lineTo(fillX[2], fillY[2]);
                path.lineTo(fillX[3], fillY[3]);
                path.close();
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);
                paint.setStyle(Paint.Style.STROKE);

            } else {
                // draw black face background (do not care about normals and
                // visibility!)
                for (int j = 0; j < 4; j++)
                    // corner co-ordinates
                    getCorners(i, j, fillX, fillY,
                            blocks[i][0][CubeConstants.factors[j][0]],
                            blocks[i][1][CubeConstants.factors[j][1]], mirrored);
                paint.setColor(android.graphics.Color.BLACK);

                path.reset();
                path.moveTo(fillX[0], fillY[0]);
                path.lineTo(fillX[1], fillY[1]);
                path.lineTo(fillX[2], fillY[2]);
                path.lineTo(fillX[3], fillY[3]);
                path.close();
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);
                paint.setStyle(Paint.Style.STROKE);

            }
        }
        // draw all visible faces and get dragging regions
        for (int i = 0; i < 6; i++) { // all faces
            CubeUtils.vSub(CubeUtils.vScale(CubeUtils.vCopy(perspEye, eye), 5.0 + persp),
                    CubeConstants.faceNormals[i]); // perspective correction
            if (CubeUtils.vProd(perspEye, CubeConstants.faceNormals[i]) > 0) { // draw
                // only
                // faces
                // towards
                // us
                int sideW = blocks[i][0][1] - blocks[i][0][0];
                int sideH = blocks[i][1][1] - blocks[i][1][0];
                if (sideW > 0 && sideH > 0) { // this side is not only black
                    // draw colored facelets
                    for (int n = 0, p = blocks[i][1][0]; n < sideH; n++, p++) {
                        for (int o = 0, q = blocks[i][0][0]; o < sideW; o++, q++) {
                            for (int j = 0; j < 4; j++)
                                getCorners(i, j, fillX, fillY, q
                                        + CubeConstants.border[j][0], p
                                        + CubeConstants.border[j][1], mirrored);
                            paint.setColor(colors[cube[i][p * 3 + q]].darker());

                            path.reset();
                            path.moveTo(fillX[0], fillY[0]);
                            path.lineTo(fillX[1], fillY[1]);
                            path.lineTo(fillX[2], fillY[2]);
                            path.lineTo(fillX[3], fillY[3]);
                            path.close();

                            canvas.drawPath(path, paint);
                            paint.setColor(colors[cube[i][p * 3 + q]].colorCode);

                            path.reset();
                            path.moveTo(fillX[0], fillY[0]);
                            path.lineTo(fillX[1], fillY[1]);
                            path.lineTo(fillX[2], fillY[2]);
                            path.lineTo(fillX[3], fillY[3]);
                            path.close();

                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawPath(path, paint);
                            paint.setStyle(Paint.Style.STROKE);

                        }
                    }
                }
                if (!editable || animating) // no need of twisting while
                    // animating
                    continue;
                // horizontal and vertical directions of face - interpolated
                double dxh = (cooX[i][1] - cooX[i][0] + cooX[i][2] - cooX[i][3]) / 6.0;
                double dyh = (cooX[i][3] - cooX[i][0] + cooX[i][2] - cooX[i][1]) / 6.0;
                double dxv = (cooY[i][1] - cooY[i][0] + cooY[i][2] - cooY[i][3]) / 6.0;
                double dyv = (cooY[i][3] - cooY[i][0] + cooY[i][2] - cooY[i][1]) / 6.0;
                if (mode == 3) { // just the normal cube
                    for (int j = 0; j < 6; j++) { // 4 areas 3x1 per face + 2
                        // center slices
                        for (int k = 0; k < 4; k++)
                            // 4 points per area
                            getCorners(i, k, dragCornersX[dragAreas],
                                    dragCornersY[dragAreas],
                                    CubeConstants.dragBlocks[j][k][0],
                                    CubeConstants.dragBlocks[j][k][1], false);
                        dragDirsX[dragAreas] = (dxh
                                * CubeConstants.areaDirs[j][0] + dxv
                                * CubeConstants.areaDirs[j][1])
                                * CubeConstants.twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh
                                * CubeConstants.areaDirs[j][0] + dyv
                                * CubeConstants.areaDirs[j][1])
                                * CubeConstants.twistDirs[i][j];
                        dragLayers[dragAreas] = CubeConstants.adjacentFaces[i][j % 4];
                        if (j >= 4)
                            dragLayers[dragAreas] &= ~1;
                        dragModes[dragAreas] = j / 4;
                        dragAreas++;
                        if (dragAreas == 18)
                            break;
                    }
                } else if (mode == 0) { // twistable top layer
                    if (i != twistedLayer && sideW > 0 && sideH > 0) { // only
                        // 3x1
                        // faces
                        int j = sideW == 3 ? (blocks[i][1][0] == 0 ? 0 : 2)
                                : (blocks[i][0][0] == 0 ? 3 : 1);
                        for (int k = 0; k < 4; k++)
                            getCorners(i, k, dragCornersX[dragAreas],
                                    dragCornersY[dragAreas],
                                    CubeConstants.dragBlocks[j][k][0],
                                    CubeConstants.dragBlocks[j][k][1], false);
                        dragDirsX[dragAreas] = (dxh
                                * CubeConstants.areaDirs[j][0] + dxv
                                * CubeConstants.areaDirs[j][1])
                                * CubeConstants.twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh
                                * CubeConstants.areaDirs[j][0] + dyv
                                * CubeConstants.areaDirs[j][1])
                                * CubeConstants.twistDirs[i][j];
                        dragLayers[dragAreas] = twistedLayer;
                        dragModes[dragAreas] = 0;
                        dragAreas++;
                    }
                } else if (mode == 1) { // twistable center layer
                    if (i != twistedLayer && sideW > 0 && sideH > 0) { // only
                        // 3x1
                        // faces
                        int j = sideW == 3 ? 4 : 5;
                        for (int k = 0; k < 4; k++)
                            getCorners(i, k, dragCornersX[dragAreas],
                                    dragCornersY[dragAreas],
                                    CubeConstants.dragBlocks[j][k][0],
                                    CubeConstants.dragBlocks[j][k][1], false);
                        dragDirsX[dragAreas] = (dxh
                                * CubeConstants.areaDirs[j][0] + dxv
                                * CubeConstants.areaDirs[j][1])
                                * CubeConstants.twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh
                                * CubeConstants.areaDirs[j][0] + dyv
                                * CubeConstants.areaDirs[j][1])
                                * CubeConstants.twistDirs[i][j];
                        dragLayers[dragAreas] = twistedLayer;
                        dragModes[dragAreas] = 1;
                        dragAreas++;
                    }
                }
            }
        }
    }

    private void getCorners(int face, int corner, int[] cornersX,
                            int[] cornersY, double factor1, double factor2, boolean mirror) {
        factor1 /= 3.0;
        factor2 /= 3.0;
        double x1 = cooX[face][0] + (cooX[face][1] - cooX[face][0]) * factor1;
        double y1 = cooY[face][0] + (cooY[face][1] - cooY[face][0]) * factor1;
        double x2 = cooX[face][3] + (cooX[face][2] - cooX[face][3]) * factor1;
        double y2 = cooY[face][3] + (cooY[face][2] - cooY[face][3]) * factor1;
        cornersX[corner] = (int) (0.5 + x1 + (x2 - x1) * factor2);
        cornersY[corner] = (int) (0.5 + y1 + (y2 - y1) * factor2);
        if (mirror)
            cornersX[corner] = width - cornersX[corner];
    }
}
