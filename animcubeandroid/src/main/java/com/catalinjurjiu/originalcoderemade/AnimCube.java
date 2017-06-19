package com.catalinjurjiu.originalcoderemade;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.catalinjurjiu.animcubeandroid.Color;
import com.catalinjurjiu.animcubeandroid.CubeConstants;
import com.catalinjurjiu.animcubeandroid.CubeUtils;
import com.catalinjurjiu.animcubeandroid.LogUtil;
import com.catalinjurjiu.animcubeandroid.R;

import static com.catalinjurjiu.animcubeandroid.CubeConstants.adjacentFaces;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.areaDirs;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.border;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.botBlockFaceDim;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.cornerCoords;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.cubeBlocks;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.cycleCenters;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.cycleFactors;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.cycleLayerSides;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.cycleOffsets;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.cycleOrder;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.dragBlocks;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.faceCorners;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.faceNormals;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.faceTwistDirs;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.factors;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.midBlockFaceDim;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.midBlockTable;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.oppositeCorners;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.rotCos;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.rotSign;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.rotSin;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.rotVec;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.topBlockFaceDim;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.topBlockTable;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.twistDirs;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.arrayMovePos;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.colors;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.realMoveLength;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vAdd;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vCopy;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vMul;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vNorm;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vProd;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vScale;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vSub;

/**
 * @author Josef Jelinek
 * @version 3.5b
 */

public final class AnimCube extends SurfaceView implements View.OnTouchListener {
    public static final String TAG = "AnimCube";
    private static final int NEXT_MOVE = 0;
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
    private final double[] eyeD = new double[3];
    private final Path path = new Path();
    private final Object animThreadLock = new Object(); // lock object for the animation thread
    private final Object renderThreadLock = new Object();
    // background colors
    private Color backgroundColor;
    private Color backgroundColor2;
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
    private boolean toTwist; // layer can be twisted
    private boolean interrupted; // thread was interrupted
    private boolean restarted; // animation was stopped
    private boolean mirrored; // mirroring of the cube view
    private boolean editable; // editation of the cube with a mouse
    private boolean twisting; // a user twists a cube layer
    private boolean spinning; // an animation twists a cube layer
    private boolean animating; // animation run
    private boolean dragging; // progress bar is controlled
    private int perspective; // perspective deformation
    private double scale; // cube scale
    private int align; // cube alignment (top, center, bottom)
    private boolean showBackFaces;
    private double faceShift;
    // move sequence data
    private int[][] move;
    private int movePos;
    private int moveDir;
    private boolean moveOne;
    private boolean moveAnimated;
    // double buffered animation
    // cube window size (applet window is resizable)
    private int width;
    private int height;
    // last position of mouse (for dragging the cube)
    private int lastX;
    private int lastY;
    // last position of mouse (when waiting for clear decision)
    private int lastDragX;
    private int lastDragY;
    // drag areas
    private int dragAreas;
    private int[] dragLayers = new int[18]; // which layers belongs to dragCorners
    private int[] dragModes = new int[18]; // which layer modes dragCorners
    // current drag directions
    private double dragX;
    private double dragY;
    private HandlerThread renderThread;
    private Thread animThread; // thread to perform the animation
    private Handler renderHandler;
    /**
     * CATA ADDED FIELDS
     **/
    private SurfaceHolder surfaceHolder;
    private boolean surfaceCreated;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float touchSensitivityCoefficient;
    private boolean mActionDownReceived;
    private boolean isDebuggable;
    private Runnable paintRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG, "paintRunnable", isDebuggable);
            synchronized (renderThreadLock) {
                LogUtil.d(TAG, "paintRunnable: inside sync block", isDebuggable);
                if (surfaceCreated) {
                    LogUtil.d(TAG, "paintRunnable: surface IS created", isDebuggable);
                    paint();
                } else {
                    LogUtil.d(TAG, "paintRunnable: surface IS NOT created", isDebuggable);
                }
                LogUtil.d(TAG, "paintRunnable: paint has finished, end of sync block", isDebuggable);
            }
        }
    };
    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            animateCube();
        }
    };
    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            LogUtil.d(TAG, "Surface Created", isDebuggable);
            synchronized (renderThreadLock) {
                surfaceCreated = true;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            LogUtil.d(TAG, "Surface surfaceChanged", isDebuggable);
            repaint();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            LogUtil.d(TAG, "Surface surfaceDestroyed", isDebuggable);
            synchronized (renderThreadLock) {
                surfaceCreated = false;
                renderHandler.removeCallbacks(paintRunnable);
            }
        }
    };

    public AnimCube(Context context) {
        super(context);
        init(context, null, -1, -1);
    }

    public AnimCube(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }

    public AnimCube(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    @SuppressWarnings("NewApi")
    public AnimCube(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * @param colorValues
     */
    public void setCubeColors(String colorValues) {
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

    /**
     * @param moveSequence
     */
    public void setMoveSequence(String moveSequence) {
        move = getMove(moveSequence, false);
    }

    /**
     *
     */
    public void resetToInitialState() {
        stopAnimation();
        movePos = 0;
        resetCubeColors();
        repaint();
    }

    /**
     * @param mode
     */
    public void startAnimation(int mode) {
        LogUtil.e(TAG, "startAnimation. mode:" + mode, isDebuggable);
        synchronized (animThreadLock) {
            LogUtil.e(TAG, "startAnimation. acquired lock", isDebuggable);
            stopAnimation();
            if (move.length == 0 || move[NEXT_MOVE].length == 0) {
                return;
            }
            switch (mode) {
                case 0: // play forward
                    moveDir = 1;
                    moveOne = false;
                    moveAnimated = true;
                    break;
                case 1: // play backward
                    moveDir = -1;
                    moveOne = false;
                    moveAnimated = true;
                    break;
                case 2: // step forward
                    moveDir = 1;
                    moveOne = true;
                    moveAnimated = true;
                    break;
                case 3: // step backward
                    moveDir = -1;
                    moveOne = true;
                    moveAnimated = true;
                    break;
                case 4: // fast forward
                    moveDir = 1;
                    moveOne = false;
                    moveAnimated = false;
                    break;
            }
            LogUtil.e(TAG, "startAnimation: notify", isDebuggable);
            animThreadLock.notify();
            LogUtil.e(TAG, "startAnimation: end of sync block", isDebuggable);
        }
    }

    /**
     *
     */
    public void stopAnimation() {
        LogUtil.e(TAG, "stopAnimation.", isDebuggable);
        synchronized (animThreadLock) {
            LogUtil.e(TAG, "stopAnimation acquired lock.", isDebuggable);
            restarted = true;
            LogUtil.e(TAG, "stopAnimation: notify", isDebuggable);
            animThreadLock.notify();
            try {
                LogUtil.e(TAG, "stopAnimation: wait", isDebuggable);
                animThreadLock.wait();
                LogUtil.e(TAG, "stopAnimation: after wait - ok", isDebuggable);
            } catch (InterruptedException e) {
                interrupted = true;
                LogUtil.e(TAG, "stopAnimation: after wait - interrupted exception", isDebuggable);
            }
            restarted = false;
            LogUtil.e(TAG, "stopAnimation: end of sync block", isDebuggable);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (animating) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mActionDownReceived = true;
                handlePointerDownEvent(event);
                break;
            case MotionEvent.ACTION_UP:
                if (mActionDownReceived) {
                    handlePointerUpEvent();
                    mActionDownReceived = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                handlePointerDragEvent(event);
                break;
        }
        return true;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.AnimCube);

        initBackgroundColor(attributes);
        initCubeColors(attributes);
        initMoves(attributes);
        initEditable(attributes);
        initInitialRotation(attributes);
        initBackFacesDistance(attributes);
        initGestureSensitivity(attributes);
        initScale(attributes);
        initPerspective(attributes);
        initVerticalAlign(attributes);
        initSingleRotationSpeed(attributes);
        initDoubleRotationSpeed(attributes);
        initDebuggable(attributes);
        //done, recycle typed array
        attributes.recycle();

        // get the surface holder of he current surface view, add this view as a
        // callback
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(surfaceCallback);
        renderThread = new HandlerThread("RenderThread", Process.THREAD_PRIORITY_DISPLAY);
        renderThread.start();
        renderHandler = new Handler(renderThread.getLooper());
        animThread = new Thread(animRunnable, "AnimThread");
        // start animation thread
        animThread.start();

        // register to receive touch events
        setOnTouchListener(this);
    }

    private void initDebuggable(TypedArray attributes) {
        this.isDebuggable = attributes.getBoolean(R.styleable.AnimCube_debuggable, false);
    }

    private void initBackgroundColor(TypedArray attributes) {
        this.backgroundColor = new Color(attributes.getColor(R.styleable.AnimCube_backgroundColor,
                android.graphics.Color.WHITE));
        // setup colors (contrast)
        this.backgroundColor2 = new Color(backgroundColor.red / 2, backgroundColor.green / 2, backgroundColor.blue / 2);
    }

    private void initCubeColors(TypedArray attributes) {
        String cubeColorsString = attributes.getString(R.styleable.AnimCube_cubeColors);
        if (cubeColorsString == null || cubeColorsString.length() != 54) {
            setCubeDefaultColors();
        } else {
            setCubeColors(cubeColorsString);
        }
        // setup initial values
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                initialCube[i][j] = cube[i][j];
            }
        }
    }

    private void initMoves(TypedArray attributes) {
        String moves = attributes.getString(R.styleable.AnimCube_moves);
        if (moves != null) {
            setMoveSequence(moves);
        } else {
            move = new int[0][0];
        }
        movePos = 0;
    }

    private void initEditable(TypedArray attributes) {
        this.editable = attributes.getBoolean(R.styleable.AnimCube_editable, false);
    }

    private void initInitialRotation(TypedArray attributes) {
        int styleableIndex = R.styleable.AnimCube_initialRotation;
        String initialRotation = CubeConstants.DEFAULT_INITIAL_CUBE_ROTATION;
        if (attributes.hasValue(styleableIndex)) {
            initialRotation = attributes.getString(styleableIndex);
        }
        setupInitialViewAngle(initialRotation);
    }

    private void initBackFacesDistance(TypedArray attributes) {
        int backFaceDistance = attributes.getInt(R.styleable.AnimCube_backFacesDistance, 0);
        if (backFaceDistance >= 2 && backFaceDistance <= 10) {
            this.showBackFaces = true;
            this.faceShift = backFaceDistance;
            if (this.faceShift < 1.0) {
                this.showBackFaces = false;
            } else {
                this.faceShift /= 10.0;
            }
        } else {
            this.showBackFaces = false;
            this.faceShift = 0;
        }
    }

    private void initGestureSensitivity(TypedArray attributes) {
        float sensitivityCoefficient = attributes.getFloat(R.styleable.AnimCube_touchSensitivity, 1.0f);

        if (sensitivityCoefficient < 0.1f) {
            sensitivityCoefficient = 0.1f;
        } else if (sensitivityCoefficient > 2) {
            sensitivityCoefficient = 2f;
        }

        this.touchSensitivityCoefficient = 5.0f * 1.0f / sensitivityCoefficient;
    }

    private void initScale(TypedArray attributes) {
        int scaleParam = attributes.getInt(R.styleable.AnimCube_scale, 0);
        this.scale = 1.0 / (1.0 + scaleParam / 10.0);
    }

    private void initPerspective(TypedArray attributes) {
        this.perspective = attributes.getInt(R.styleable.AnimCube_perspective, 2);
    }

    private void initVerticalAlign(TypedArray attributes) {
        int styleableIndex = R.styleable.AnimCube_verticalAlign;
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
    }

    private void initSingleRotationSpeed(TypedArray attributes) {
        this.speed = attributes.getInt(R.styleable.AnimCube_single_rotation_speed, 5);
    }

    private void initDoubleRotationSpeed(TypedArray attributes) {
        this.doubleSpeed = attributes.getInt(R.styleable.AnimCube_double_rotation_speed, this.speed * 3 / 2);
    }

    private void paint() {
        LogUtil.d(TAG, "repaint: paint canvas", isDebuggable);
        synchronized (animThreadLock) {
            LogUtil.d(TAG, "repaint: paint canvas synchronized", isDebuggable);
            Canvas canvas = surfaceHolder.lockCanvas();
            drawTheCanvas(canvas);
            surfaceHolder.unlockCanvasAndPost(canvas);
            LogUtil.d(TAG, "repaint: paint has finished, end of sync block", isDebuggable);
        }

    }

    private void drawTheCanvas(Canvas canvas) {
        paint.setColor(backgroundColor.colorCode);
        canvas.drawPaint(paint);
        int height = getHeight();
        int width = getWidth();
        // create offscreen buffer for double buffering
        if (width != this.width || height != this.height) {
            this.width = width;
            this.height = height;
        }

        dragAreas = 0;
        if (natural) // compact cube
            fixBlock(canvas, eye, eyeX, eyeY, cubeBlocks, 3); // draw cube and fill drag areas
        else { // in twisted state
            // compute top observer
            double cosA = Math.cos(originalAngle + currentAngle);
            double sinA = Math.sin(originalAngle + currentAngle) * rotSign[twistedLayer];
            for (int i = 0; i < 3; i++) {
                tempEye[i] = 0;
                tempEyeX[i] = 0;
                for (int j = 0; j < 3; j++) {
                    int axis = twistedLayer / 2;
                    tempEye[i] += eye[j] * (rotVec[axis][i][j] + rotCos[axis][i][j] * cosA + rotSin[axis][i][j] * sinA);
                    tempEyeX[i] += eyeX[j] * (rotVec[axis][i][j] + rotCos[axis][i][j] * cosA + rotSin[axis][i][j] * sinA);
                }
            }
            vMul(tempEyeY, tempEye, tempEyeX);
            // compute bottom anti-observer
            double cosB = Math.cos(originalAngle - currentAngle);
            double sinB = Math.sin(originalAngle - currentAngle) * rotSign[twistedLayer];
            for (int i = 0; i < 3; i++) {
                tempEye2[i] = 0;
                tempEyeX2[i] = 0;
                for (int j = 0; j < 3; j++) {
                    int axis = twistedLayer / 2;
                    tempEye2[i] += eye[j] * (rotVec[axis][i][j] + rotCos[axis][i][j] * cosB + rotSin[axis][i][j] * sinB);
                    tempEyeX2[i] += eyeX[j] * (rotVec[axis][i][j] + rotCos[axis][i][j] * cosB + rotSin[axis][i][j] * sinB);
                }
            }
            vMul(tempEyeY2, tempEye2, tempEyeX2);
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
            vSub(vScale(vCopy(perspEye, eye), 5.0 + perspective), vScale(vCopy(perspNormal, faceNormals[twistedLayer]), 1.0 / 3.0));
            vSub(vScale(vCopy(perspEyeI, eye), 5.0 + perspective), vScale(vCopy(perspNormal, faceNormals[twistedLayer ^ 1]), 1.0 / 3.0));
            double topProd = vProd(perspEye, faceNormals[twistedLayer]);
            double botProd = vProd(perspEyeI, faceNormals[twistedLayer ^ 1]);
            int orderMode;
            if (topProd < 0 && botProd > 0) // top facing away
                orderMode = 0;
            else if (topProd > 0 && botProd < 0) // bottom facing away: draw it first
                orderMode = 1;
            else // both top and bottom layer facing away: draw them first
                orderMode = 2;
            fixBlock(canvas,
                    eyeArray[CubeUtils.eyeOrder[twistedMode][CubeUtils.drawOrder[orderMode][0]]],
                    eyeArrayX[CubeUtils.eyeOrder[twistedMode][CubeUtils.drawOrder[orderMode][0]]],
                    eyeArrayY[CubeUtils.eyeOrder[twistedMode][CubeUtils.drawOrder[orderMode][0]]],
                    blockArray[CubeUtils.drawOrder[orderMode][0]],
                    CubeUtils.blockMode[twistedMode][CubeUtils.drawOrder[orderMode][0]]);
            fixBlock(canvas,
                    eyeArray[CubeUtils.eyeOrder[twistedMode][CubeUtils.drawOrder[orderMode][1]]],
                    eyeArrayX[CubeUtils.eyeOrder[twistedMode][CubeUtils.drawOrder[orderMode][1]]],
                    eyeArrayY[CubeUtils.eyeOrder[twistedMode][CubeUtils.drawOrder[orderMode][1]]],
                    blockArray[CubeUtils.drawOrder[orderMode][1]],
                    CubeUtils.blockMode[twistedMode][CubeUtils.drawOrder[orderMode][1]]);
            fixBlock(canvas,
                    eyeArray[CubeUtils.eyeOrder[twistedMode][CubeUtils.drawOrder[orderMode][2]]],
                    eyeArrayX[CubeUtils.eyeOrder[twistedMode][CubeUtils.drawOrder[orderMode][2]]],
                    eyeArrayY[CubeUtils.eyeOrder[twistedMode][CubeUtils.drawOrder[orderMode][2]]],
                    blockArray[CubeUtils.drawOrder[orderMode][2]],
                    CubeUtils.blockMode[twistedMode][CubeUtils.drawOrder[orderMode][2]]);
        }
    }

    private void repaint() {
        renderHandler.removeCallbacks(paintRunnable);
        renderHandler.post(paintRunnable);
    }

    private int[][] getMove(String sequence, boolean info) {
        if (info) {
            int pos = sequence.indexOf('{');
            while (pos != -1) {
                pos = sequence.indexOf('{', pos + 1);
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
        int[] move = new int[sequence.length()];
        // overdimmensioned
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
        for (int i = 0; i < 3; i++) {
            initialEye[i] = eye[i];
            initialEyeX[i] = eyeX[i];
            initialEyeY[i] = eyeY[i];
        }
    }

    private void setCubeDefaultColors() {
        // clean the cube
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                cube[i][j] = i + 10;
            }
        }
    }

    private void resetCubeColors() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                cube[i][j] = initialCube[i][j];
            }
        }
    }

    private void doMove(int[][] cube, int[] move, int start, int length, boolean reversed) {
        int position = reversed ? start + length : start;
        while (true) {
            if (reversed) {
                if (position <= start)
                    break;
                position--;
            }
            if (move[position] < 1000 && move[position] >= 0) {
                int modifier = move[position] % 4 + 1;
                int mode = move[position] / 4 % 6;
                if (modifier == 4) // reversed double turn
                    modifier = 2;
                if (reversed)
                    modifier = 4 - modifier;
                twistLayers(cube, move[position] / 24, modifier, mode);
            }
            if (!reversed) {
                position++;
                if (position >= start + length)
                    break;
            }
        }
    }

    private void animateCube() {
        LogUtil.e(TAG, "animateCube.", isDebuggable);
        synchronized (animThreadLock) {
            LogUtil.e(TAG, "animateCube acquired lock - sync block", isDebuggable);
            interrupted = false;
            do {
                if (restarted) {
                    LogUtil.e(TAG, "animateCube notify at restarted check", isDebuggable);
                    animThreadLock.notify();
                    LogUtil.e(TAG, "animateCube after notify", isDebuggable);
                }
                try {
                    LogUtil.e(TAG, "animateCube: before wait", isDebuggable);
                    animThreadLock.wait();
                    LogUtil.e(TAG, "animateCube: after wait - got lock - ok", isDebuggable);
                } catch (InterruptedException e) {
                    LogUtil.e(TAG, "animateCube: after wait, interrupted exception:" + e.getMessage(), e, isDebuggable);
                    break;
                }
                if (restarted)
                    continue;
                boolean restart = false;
                animating = true;
                int[] mv = move[NEXT_MOVE];
                if (moveDir > 0) {
                    if (movePos >= mv.length) {
                        movePos = 0;
                    }
                } else {
                    if (movePos == 0)
                        movePos = mv.length;
                }
                while (true) {
                    if (moveDir < 0) {
                        if (movePos == 0)
                            break;
                        movePos--;
                    }
                    if (mv[movePos] == -1) {
                        repaint();
                        if (!moveOne)
                            sleep(33 * speed);
                    } else if (mv[movePos] < 1000) {
                        int num = mv[movePos] % 4 + 1;
                        int mode = mv[movePos] / 4 % 6;
                        boolean clockwise = num < 3;
                        if (num == 4)
                            num = 2;
                        if (moveDir < 0) {
                            clockwise = !clockwise;
                            num = 4 - num;
                        }
                        spin(mv[movePos] / 24, num, mode, clockwise, moveAnimated);
                        if (moveOne)
                            restart = true;
                    }
                    if (moveDir > 0) {
                        movePos++;
                        if (movePos < mv.length && mv[movePos] >= 1000) {
                            movePos++;
                        }
                        if (movePos == mv.length) {
                            break;
                        }
                    }
                    if (interrupted || restarted || restart)
                        break;
                }
                animating = false;
                repaint();
            } while (!interrupted);
        }
        LogUtil.e(TAG, "Animate cube: Interrupted, all is fine, ended!", isDebuggable);
    } // run()

    private void sleep(int time) {
        LogUtil.e(TAG, "sleep, time: " + time, isDebuggable);
        synchronized (animThreadLock) {
            LogUtil.e(TAG, "sleep, acquired lock. sync block", isDebuggable);
            try {
                LogUtil.e(TAG, "sleep, before timed wait", isDebuggable);
                animThreadLock.wait(time);
                LogUtil.e(TAG, "sleep, after timed wait -- all is good", isDebuggable);
            } catch (InterruptedException e) {
                interrupted = true;
                LogUtil.e(TAG, "sleep, after timed wait -- got interrupted exception", isDebuggable);
            }
        }
    }

    private void clear() {
        synchronized (animThreadLock) {
            movePos = 0;
            natural = true;
            mirrored = false;
            for (int i = 0; i < 6; i++)
                for (int j = 0; j < 9; j++)
                    cube[i][j] = initialCube[i][j];
            for (int i = 0; i < 3; i++) {
                eye[i] = initialEye[i];
                eyeX[i] = initialEyeX[i];
                eyeY[i] = initialEyeY[i];
            }
        }
    }

    private void spin(int layer, int num, int mode, boolean clockwise, boolean animated) {
        twisting = false;
        natural = true;
        spinning = true;
        originalAngle = 0;
        if (faceTwistDirs[layer] > 0)
            clockwise = !clockwise;
        if (animated) {
            double phit = Math.PI / 2; // target for currentAngle (default pi/2)
            double phis = clockwise ? 1.0 : -1.0; // sign
            int turnTime = 67 * speed; // milliseconds to be used for one turn
            if (num == 2) {
                phit = Math.PI;
                turnTime = 67 * doubleSpeed; // double turn is usually faster than two quarter turns
            }
            twisting = true;
            twistedLayer = layer;
            twistedMode = mode;
            splitCube(layer); // start twisting
            long sTime = System.currentTimeMillis();
            long lTime = sTime;
            double d = phis * phit / turnTime;
            for (currentAngle = 0; currentAngle * phis < phit; currentAngle = d * (lTime - sTime)) {
                repaint();
                sleep(25);
                if (interrupted || restarted)
                    break;
                lTime = System.currentTimeMillis();
            }
        }
        currentAngle = 0;
        twisting = false;
        natural = true;
        twistLayers(cube, layer, num, mode);
        spinning = false;
        if (animated)
            repaint();
    }

    private void splitCube(int layer) {
        for (int i = 0; i < 6; i++) { // for all faces
            topBlocks[i] = topBlockTable[topBlockFaceDim[layer][i]];
            botBlocks[i] = topBlockTable[botBlockFaceDim[layer][i]];
            midBlocks[i] = midBlockTable[midBlockFaceDim[layer][i]];
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
            for (int i = 0; i < 8; i++) // to buffer
                twistBuffer[(i + num * 2) % 8] = cube[layer][cycleOrder[i]];
            for (int i = 0; i < 8; i++) // to cube
                cube[layer][cycleOrder[i]] = twistBuffer[i];
        }
        // rotate side facelets
        int k = num * 3;
        for (int i = 0; i < 4; i++) { // to buffer
            int n = adjacentFaces[layer][i];
            int c = middle ? cycleCenters[layer][i] : cycleLayerSides[layer][i];
            int factor = cycleFactors[c];
            int offset = cycleOffsets[c];
            for (int j = 0; j < 3; j++) {
                twistBuffer[k % 12] = cube[n][j * factor + offset];
                k++;
            }
        }
        k = 0; // MS VM JIT bug if placed into the loop init
        for (int i = 0; i < 4; i++) { // to cube
            int n = adjacentFaces[layer][i];
            int c = middle ? cycleCenters[layer][i] : cycleLayerSides[layer][i];
            int factor = cycleFactors[c];
            int offset = cycleOffsets[c];
            int j = 0; // MS VM JIT bug if for is used
            while (j < 3) {
                cube[n][j * factor + offset] = twistBuffer[k];
                j++;
                k++;
            }
        }
    }

    private void fixBlock(Canvas canvas, double[] eye, double[] eyeX, double[] eyeY, int[][][] blocks, int mode) {
        // project 3D co-ordinates into 2D screen ones
        for (int i = 0; i < 8; i++) {
            double min = width < height ? width : height;
            double x = min / 3.7 * vProd(cornerCoords[i], eyeX) * scale;
            double y = min / 3.7 * vProd(cornerCoords[i], eyeY) * scale;
            double z = min / (5.0 + perspective) * vProd(cornerCoords[i], eye) * scale;
            x = x / (1 - z / min); // perspective transformation
            y = y / (1 - z / min); // perspective transformation
            coordsX[i] = width / 2.0 + x;
            if (align == 0)
                coordsY[i] = height / 2.0 * scale - y;
            else if (align == 2)
                coordsY[i] = height - (height / 2.0 * scale) - y;
            else
                coordsY[i] = height / 2.0 - y;
        }
        // setup corner co-ordinates for all faces
        for (int i = 0; i < 6; i++) { // all faces
            for (int j = 0; j < 4; j++) { // all face corners
                cooX[i][j] = coordsX[faceCorners[i][j]];
                cooY[i][j] = coordsY[faceCorners[i][j]];
            }
        }
        if (showBackFaces) { // draw showBackFaces hiden facelets
            for (int i = 0; i < 6; i++) { // all faces
                vSub(vScale(vCopy(perspEye, eye), 5.0 + perspective), faceNormals[i]); // perspective correction
                if (vProd(perspEye, faceNormals[i]) < 0) { // draw only hiden faces
                    vScale(vCopy(tempNormal, faceNormals[i]), faceShift);
                    double min = width < height ? width : height;
                    double x = min / 3.7 * vProd(tempNormal, eyeX);
                    double y = min / 3.7 * vProd(tempNormal, eyeY);
                    double z = min / (5.0 + perspective) * vProd(tempNormal, eye);
                    x = x / (1 - z / min); // perspective transformation
                    y = y / (1 - z / min); // perspective transformation
                    int sideW = blocks[i][0][1] - blocks[i][0][0];
                    int sideH = blocks[i][1][1] - blocks[i][1][0];
                    if (sideW > 0 && sideH > 0) { // this side is not only black
                        // draw colored facelets
                        for (int n = 0, p = blocks[i][1][0]; n < sideH; n++, p++) {
                            for (int o = 0, q = blocks[i][0][0]; o < sideW; o++, q++) {
                                for (int j = 0; j < 4; j++) {
                                    getCorners(i, j, fillX, fillY, q + border[j][0], p + border[j][1], mirrored);
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
                for (int j = 0; j < 4; j++) // corner co-ordinates
                    getCorners(i, j, fillX, fillY, blocks[i][0][factors[j][0]], blocks[i][1][factors[j][1]], mirrored);
                if (sideW == 3 && sideH == 3)
                    paint.setColor(backgroundColor2.colorCode);
                else
                    paint.setColor(android.graphics.Color.BLACK);
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
                    int k = oppositeCorners[i][j];
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

            } else {
                // draw black face background (do not care about normals and visibility!)
                for (int j = 0; j < 4; j++) // corner co-ordinates
                    getCorners(i, j, fillX, fillY, blocks[i][0][factors[j][0]], blocks[i][1][factors[j][1]], mirrored);

                paint.setColor(android.graphics.Color.BLACK);

                path.reset();
                path.moveTo(fillX[0], fillY[0]);
                path.lineTo(fillX[1], fillY[1]);
                path.lineTo(fillX[2], fillY[2]);
                path.lineTo(fillX[3], fillY[3]);
                path.close();
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);
            }
        }
        // draw all visible faces and get dragging regions
        for (int i = 0; i < 6; i++) { // all faces
            vSub(vScale(vCopy(perspEye, eye), 5.0 + perspective), faceNormals[i]); // perspective correction
            if (vProd(perspEye, faceNormals[i]) > 0) { // draw only faces towards us
                int sideW = blocks[i][0][1] - blocks[i][0][0];
                int sideH = blocks[i][1][1] - blocks[i][1][0];
                if (sideW > 0 && sideH > 0) { // this side is not only black
                    // draw colored facelets
                    for (int n = 0, p = blocks[i][1][0]; n < sideH; n++, p++) {
                        for (int o = 0, q = blocks[i][0][0]; o < sideW; o++, q++) {
                            for (int j = 0; j < 4; j++)
                                getCorners(i, j, fillX, fillY, q + border[j][0], p + border[j][1], mirrored);

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
                        }
                    }
                }
                if (!editable || animating) // no need of twisting while animating
                    continue;
                // horizontal and vertical directions of face - interpolated
                double dxh = (cooX[i][1] - cooX[i][0] + cooX[i][2] - cooX[i][3]) / 6.0;
                double dyh = (cooX[i][3] - cooX[i][0] + cooX[i][2] - cooX[i][1]) / 6.0;
                double dxv = (cooY[i][1] - cooY[i][0] + cooY[i][2] - cooY[i][3]) / 6.0;
                double dyv = (cooY[i][3] - cooY[i][0] + cooY[i][2] - cooY[i][1]) / 6.0;
                if (mode == 3) { // just the normal cube
                    if (dragAreas >= 18) {
                        break;
                    }
                    for (int j = 0; j < 6; j++) { // 4 areas 3x1 per face + 2 center slices
                        for (int k = 0; k < 4; k++) // 4 points per area
                            getCorners(i, k, dragCornersX[dragAreas], dragCornersY[dragAreas],
                                    dragBlocks[j][k][0], dragBlocks[j][k][1], false);
                        dragDirsX[dragAreas] = (dxh * areaDirs[j][0] + dxv * areaDirs[j][1]) * twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh * areaDirs[j][0] + dyv * areaDirs[j][1]) * twistDirs[i][j];
                        dragLayers[dragAreas] = adjacentFaces[i][j % 4];
                        if (j >= 4)
                            dragLayers[dragAreas] &= ~1;
                        dragModes[dragAreas] = j / 4;
                        dragAreas++;
                        if (dragAreas >= 18) {
                            break;
                        }
                    }
                } else if (mode == 0) { // twistable top layer
                    if (dragAreas >= 18) {
                        break;
                    }
                    if (i != twistedLayer && sideW > 0 && sideH > 0) { // only 3x1 faces
                        int j = sideW == 3 ? (blocks[i][1][0] == 0 ? 0 : 2) : (blocks[i][0][0] == 0 ? 3 : 1);
                        for (int k = 0; k < 4; k++)
                            getCorners(i, k, dragCornersX[dragAreas], dragCornersY[dragAreas],
                                    dragBlocks[j][k][0], dragBlocks[j][k][1], false);
                        dragDirsX[dragAreas] = (dxh * areaDirs[j][0] + dxv * areaDirs[j][1]) * twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh * areaDirs[j][0] + dyv * areaDirs[j][1]) * twistDirs[i][j];
                        dragLayers[dragAreas] = twistedLayer;
                        dragModes[dragAreas] = 0;
                        dragAreas++;
                    }
                } else if (mode == 1) { // twistable center layer
                    if (dragAreas >= 18) {
                        break;
                    }
                    if (i != twistedLayer && sideW > 0 && sideH > 0) { // only 3x1 faces
                        int j = sideW == 3 ? 4 : 5;
                        for (int k = 0; k < 4; k++)
                            getCorners(i, k, dragCornersX[dragAreas], dragCornersY[dragAreas],
                                    dragBlocks[j][k][0], dragBlocks[j][k][1], false);
                        dragDirsX[dragAreas] = (dxh * areaDirs[j][0] + dxv * areaDirs[j][1]) * twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh * areaDirs[j][0] + dyv * areaDirs[j][1]) * twistDirs[i][j];
                        dragLayers[dragAreas] = twistedLayer;
                        dragModes[dragAreas] = 1;
                        dragAreas++;
                    }
                }
            }
        }
    }

    private void getCorners(int face, int corner, int[] cornersX, int[] cornersY, double factor1, double factor2, boolean mirror) {
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

    private void handlePointerDownEvent(MotionEvent e) {
        lastDragX = lastX = Math.round(e.getX());
        lastDragY = lastY = Math.round(e.getY());
        toTwist = false;
        if (mirrored)
            lastDragX = lastX = width - lastX;
        if (editable && !animating)
            toTwist = true;
    }

    private void handlePointerUpEvent() {
        dragging = false;
        if (twisting && !spinning) {
            twisting = false;
            originalAngle += currentAngle;
            currentAngle = 0.0;
            double angle = originalAngle;
            while (angle < 0.0)
                angle += 32.0 * Math.PI;
            int num = (int) (angle * 8.0 / Math.PI) % 16; // 2pi ~ 16
            if (num % 4 == 0 || num % 4 == 3) { // close enough to a corner
                num = (num + 1) / 4; // 2pi ~ 4
                if (faceTwistDirs[twistedLayer] > 0)
                    num = (4 - num) % 4;
                originalAngle = 0;
                natural = true; // the cube in the natural state
                twistLayers(cube, twistedLayer, num, twistedMode); // rotate the facelets
            }
            repaint();
        }
    }

    private void handlePointerDragEvent(MotionEvent e) {
        if (dragging) {
            stopAnimation();
            int len = realMoveLength(move[NEXT_MOVE]);
            int pos = ((Math.round(e.getX()) - 1) * len * 2 / (width - 2) + 1) / 2;
            pos = Math.max(0, Math.min(len, pos));
            if (pos > 0)
                pos = arrayMovePos(move[NEXT_MOVE], pos);
            if (pos > movePos)
                doMove(cube, move[NEXT_MOVE], movePos, pos - movePos, false);
            if (pos < movePos)
                doMove(cube, move[NEXT_MOVE], pos, movePos - pos, true);
            movePos = pos;
            repaint();
            return;
        }
        int x = mirrored ? width - Math.round(e.getX()) : Math.round(e.getX());
        int y = Math.round(e.getY());
        int dx = Math.round((x - lastX) / touchSensitivityCoefficient);
        int dy = Math.round((y - lastY) / touchSensitivityCoefficient);
        if (editable && toTwist && !twisting && !animating) { // we do not twist but we can
            lastDragX = x;
            lastDragY = y;
            for (int i = 0; i < dragAreas; i++) { // check if inside a drag area
                double d1 = dragCornersX[i][0];
                double x1 = dragCornersX[i][1] - d1;
                double y1 = dragCornersX[i][3] - d1;
                double d2 = dragCornersY[i][0];
                double x2 = dragCornersY[i][1] - d2;
                double y2 = dragCornersY[i][3] - d2;
                double a = (y2 * (lastX - d1) - y1 * (lastY - d2)) / (x1 * y2 - y1 * x2);
                double b = (-x2 * (lastX - d1) + x1 * (lastY - d2)) / (x1 * y2 - y1 * x2);
                if (a > 0 && a < 1 && b > 0 && b < 1) { // we are in
                    if (dx * dx + dy * dy < 144) // delay the decision about twisting
                        return;
                    dragX = dragDirsX[i];
                    dragY = dragDirsY[i];
                    double d = Math.abs(dragX * dx + dragY * dy) / Math.sqrt((dragX * dragX + dragY * dragY) * (dx * dx + dy * dy));
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
            vNorm(vAdd(eye, vScale(vCopy(eyeD, eyeX), dx * -0.016)));
            vNorm(vMul(eyeX, eyeY, eye));
            vNorm(vAdd(eye, vScale(vCopy(eyeD, eyeY), dy * 0.016)));
            vNorm(vMul(eyeY, eye, eyeX));
            lastX = x;
            lastY = y;
        } else {
            if (natural)
                splitCube(twistedLayer);
            currentAngle = 0.03 * (dragX * dx + dragY * dy) / Math.sqrt(dragX * dragX + dragY * dragY); // dv * cos a
        }
        repaint();
    }
}