package com.catalinjurjiu.animcubeandroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import static com.catalinjurjiu.animcubeandroid.CubeConstants.AnimationMode;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.adjacentFaces;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.areaDirs;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.blockMode;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.border;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.botBlockFaceDim;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.cornerCoords;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.cubeBlocks;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.cycleCenters;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.cycleFactors;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.cycleLayerSides;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.cycleOffsets;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.cycleOrder;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.dragBlocks;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.drawOrder;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.eyeOrder;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.faceCorners;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.faceNormals;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.faceTwistDirs;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.factors;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.midBlockFaceDim;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.midBlockTable;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.modeChar;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.moveCodes;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.moveModes;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.oppositeCorners;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.rotCos;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.rotSign;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.rotSin;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.rotVec;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.topBlockFaceDim;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.topBlockTable;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.ComputationLogic.twistDirs;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.CubeAlign;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.CubeColors;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.CubeState;
import static com.catalinjurjiu.animcubeandroid.CubeConstants.DEFAULT_INITIAL_CUBE_ROTATION;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.darkerColor;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vAdd;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vCopy;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vMul;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vNorm;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vProd;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vScale;
import static com.catalinjurjiu.animcubeandroid.CubeUtils.vSub;

/**
 * <p>
 * View capable of displaying a 3D Rubik's Cube, with support for interaction through touch gestures and for animating a sequence of moves.
 * </p>
 * <p>
 * To animate a sequence of moves, the following methods are available: {@link #animateMoveSequence()}, {@link #animateMoveSequenceReversed()}, {@link #animateMove()}
 * and {@link #animateMoveReversed()}. Applying moves without animation is also possible, through one of: {@link #applyMoveSequence()}, {@link #applyMoveSequenceReversed()},
 * {@link #applyMove()} and {@link #applyMoveReversed()}.
 * </p>
 * <p>
 * User interaction through touch gestures is enabled by default but can be customized though {@link #setEditable(boolean)}.
 * </p>
 * <p>
 * Additionally, this object is able to notify interested parties when the cube's data model is changed, or when a certain animation has finished.
 * </p>
 * <p>Changes to the cube's model can occur in two cases:
 * <ul>
 * <li>when a sequence of moves is animated, the cube model is changed with every move;</li>
 * <li>when the cube is editable and the user rotates a face manually.</li>
 * </ul>
 * To be notified by such changes, an {@link AnimCube.OnCubeModelUpdatedListener} can be set.
 * </p>
 * <p>
 * In order to be notified when an animation is finished or when a certain move has been applied instantly, use an {@link AnimCube.OnCubeAnimationFinishedListener}.
 * </p>
 * <h2>
 * Important:
 * </h2>
 * <p>
 * This view is a subclass of {@link SurfaceView} and performs the animations on a dedicated thread. In order to ensure that the resources held by this object
 * are released gracefully, always call {@link #cleanUpResources()} when this view's parent is destroyed.
 * <br>
 * Good places to call this are {@link Activity#onDestroy()} & {@link Fragment#onDestroyView()}.
 * </p>
 */
@SuppressWarnings("unused")
public class AnimCubeDebug extends SurfaceView implements View.OnTouchListener {
    public static final String TAG = "AnimCube";
    private static final int NOTIFY_LISTENER_ANIMATION_FINISHED = 4242;
    private static final int NOTIFY_LISTENER_MODEL_UPDATED = 2424;
    // cube facelets
    private final int[][] cube = new int[6][9];
    private final int[][] initialCube = new int[6][9];
    // initial observer co-ordinate axes (view)
    private final double[] eye = {0.0, 0.0, -1.0};
    private final double[] eyeX = {1.0, 0.0, 0.0}; // (sideways)
    private final double[] eyeY = new double[3]; // (vertical)
    // sub cube dimensions
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
    // temporary eye vectors for second twisted sub-cube rotation (anti slice)
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
    private final int[] cubeColors = new int[6];
    private final int[] dragLayers = new int[18]; // which layers belongs to dragCorners
    private final int[] dragModes = new int[18]; // which layer modes dragCorners
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // background colors
    private int backgroundColor;
    private int backgroundColor2;
    private int faceletsContourColor;
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
    private boolean editable; // edit of the cube with a mouse
    private boolean twisting; // a user twists a cube layer
    private boolean spinning; // an animation twists a cube layer
    private boolean animating; // animation run
    private int perspective; // perspective deformation
    private double scale; // cube scale
    private int align; // cube alignment (top, center, bottom)
    private boolean showBackFaces;
    private double faceShift;
    // move sequence data
    private int[] move;
    private int movePos;
    private int moveDir;
    private boolean moveOne;
    private boolean moveAnimated;
    // double buffered animation
    // cube window size
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
    // current drag directions
    private double dragX;
    private double dragY;
    private Thread animThread; // thread to perform the animation
    private boolean animThreadInactive;
    private boolean mActionDownReceived;
    private float touchSensitivityCoefficient;
    private int animationMode = AnimationMode.STOPPED;
    private int backFacesDistance;
    private AnimCube.OnCubeModelUpdatedListener cubeModelUpdatedListener;
    private AnimCube.OnCubeAnimationFinishedListener cubeAnimationFinishedListener;
    private boolean isDebuggable;
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOTIFY_LISTENER_ANIMATION_FINISHED:
                    notifyListenerAnimationFinishedOnMainThread();
                    break;
                case NOTIFY_LISTENER_MODEL_UPDATED:
                    notifyListenerCubeUpdatedOnMainThread();
                    break;
                default:
                    LogUtil.w(TAG, "Unknown message in main thread handler", isDebuggable);
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
            synchronized (animThreadLock) {
                if (animThreadInactive || interrupted) {
                    LogUtil.d(TAG, "AnimThread was either inactive or interrupted, recreate", isDebuggable);
                    animThread.interrupt();
                    animThread = new Thread(animRunnable);
                    animThread.start();
                }
                repaint();
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
            stopAnimationAndDrawing();
            LogUtil.d(TAG, "surfaceDestroyed: end", isDebuggable);
        }
    };

    public AnimCubeDebug(Context context) {
        super(context);
        init(context, null);
    }

    public AnimCubeDebug(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AnimCubeDebug(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * <p>
     * Returns an {@code int[6][9]} representing the current cube model. Each integer in the array is a {@link CubeColors} and represents the color for a particular
     * facelet.
     * </p>
     * <p>
     * If custom colors have been defined, then a mapping between {@link CubeColors} and the custom color scheme needs to be performed, as the integers in the
     * array will still be values from {@link CubeColors}.
     * </p>
     *
     * @return an {code int[6][9] containing the cube colors for each facelet}
     */
    public int[][] getCubeModel() {
        synchronized (animThreadLock) {
            return cube;
        }
    }

    /**
     * <p>
     * Sets the cube in the specified state. This method expects an {@code int[6][9]} array(i.e. 6 faces, 9 facelets on each face).
     * </p>
     * <p>
     * The array needs to be populated with integers specified in {@link CubeColors}. Each integer specifies the color of one cube facelet. Additionally, the
     * order in which faces are specified is not relevant, since {@link AnimCubeDebug} doesn't care about the cube model that much. The specified model doesn't even have to be a
     * valid Rubik's cube.
     * </p>
     * <p>
     * <b>Note:</b> after this is set {@link #resetToInitialState()} will reset the cube to the state set here, not to the cube state previous to calling {@link #setCubeModel(String)}.
     * </p>
     *
     * @param colorValues an {@code int[6][9]} array with color values from {@link CubeColors}
     */
    public void setCubeModel(int[][] colorValues) {
        CubeUtils.deepCopy2DArray(colorValues, cube);
        CubeUtils.deepCopy2DArray(colorValues, initialCube);
        notifyHandlerAnimationFinished();
        repaint();
    }

    /**
     * <p>
     * Sets the cube in the specified state. This method expects a {@link String} with exactly 54 characters (i.e. 9 facelets on each cube face * 6 cube faces). If the string
     * is of different length, nothing will happen.
     * </p>
     * <p>
     * The string needs to be a sequence of integers specified in {@link CubeColors}. Each integer specifies the color of one cube facelet. Additionally, the
     * order in which faces are specified is not relevant, since {@link AnimCubeDebug} doesn't care about the cube model that much. The specified model doesn't even have to be a
     * valid Rubik's cube.
     * </p>
     * <p>
     * For example:<br>
     * <pre>    "000000000111111111222222222333333333444444444555555555"</pre><br>
     * Represents a solved cube.
     * </p>
     * <p>
     * <b>Note:</b> after this is set {@link #resetToInitialState()} will reset the cube to the state set here, not to the cube state previous to calling {@link #setCubeModel(String)}.
     * </p>
     *
     * @param colorValues a {@link String} of integers in the format described above.
     */
    public void setCubeModel(String colorValues) {
        boolean wasValid = setStringCubeModelInternal(colorValues);
        if (wasValid) {
            notifyHandlerCubeModelUpdated();
        }
        repaint();
    }

    /**
     * Reads whether individual face rotation through user touch events are allowed.
     *
     * @return {@code true} if editable mode is enabled, {@code false} otherwise
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Enables or disables individual face rotation through user touch event.
     *
     * @param isEditable {@code true} if the user should be able to edit the cube, {@code false} otherwise
     */
    public void setEditable(boolean isEditable) {
        this.editable = isEditable;
    }

    /**
     * <p>
     * Sets back faces distance from the cube. Typically, a value smaller than 2 means they won't be visible.
     * </p>
     * <p><b>Note:</b> In principle this works fine if called while the cube is animating, however the effect is rather sudden and noticeable.</p>
     *
     * @param backFaceDistance integer (typically between 2-10, but not necessarily), representing the distance of the back faces from the cube.
     * @see <a href="http://software.rubikscube.info/AnimCube/#hint">Complete documentation of back faces distance (originally called <i>hint</i>)</a>
     */
    public void setBackFacesDistance(int backFaceDistance) {
        setBackFacesDistanceInternal(backFaceDistance);
        repaint();
    }

    /**
     * Checks whether the cube is currently animating a move, or not.
     *
     * @return {@code true} if the cube is currently animating a move, {@code false} otherwise
     */
    public boolean isAnimating() {
        synchronized (animThreadLock) {
            return animating;
        }
    }

    /**
     * <p>
     * Sets the rotation speed of a single rotation. This parameter allows to customize the speed of quarter turn separately from face turns.
     * </p>
     * <p>
     * The value should consist only of decimal digits.
     * </p>
     * <p>
     * The higher value the slower is the animation. The default value is 10, which corresponds to approximately 1 second for face turn and approximately 2/3
     * seconds for quarter turn if not specified differently.
     * </p>
     * <p>
     * The face turn speed can be adjusted separately by {@link #setDoubleRotationSpeed(int)}.
     * </p>
     * <p>
     * If this is called while the cube is animating a move, its effects will only be applied starting with the next move.
     * </p>
     *
     * @param singleRotationSpeed the desired rotation speed.
     */
    public void setSingleRotationSpeed(int singleRotationSpeed) {
        this.speed = singleRotationSpeed;
    }

    /**
     * <p>
     * Sets the rotation speed of a double rotation. This parameter allows to customize the speed of face turns separately from quarter turns.
     * </p>
     * <p>
     * The value should consist only of decimal digits.
     * </p>
     * <p>The higher value the slower is the animation. The default value is 10, which corresponds to approximately 1 second
     * for the face turn.
     * <p>
     * The default is set to the 150% of the value of speed.
     * </p>
     * <p>
     * The quarter turn speed can be adjusted by {@link #setSingleRotationSpeed(int)}.
     * </p>
     * <p>
     * If this is called while the cube is animating a move, its effects will only be applied starting with the next move.
     * </p>
     *
     * @param doubleRotationSpeed the desired rotation speed.
     */
    public void setDoubleRotationSpeed(int doubleRotationSpeed) {
        this.doubleSpeed = doubleRotationSpeed;
    }

    /**
     * <p>
     * Enables or disables debug mode.
     * </p>
     * <p>
     * This is disabled by default.
     * </p>
     *
     * @param isDebuggable {@code true} to enable debug mode, {@code false} to disable it.
     */
    public void setDebuggable(boolean isDebuggable) {
        this.isDebuggable = isDebuggable;
    }

    /**
     * <p>
     * Sets the sequence of moves that need to be performed by {@link AnimCubeDebug} (and optionally, animated). Some of the moves affect centers and they can be moved to another layer from the user's point of
     * view. Such movements <b>do not affect</b> the notation from the user's point of view. The characters are not fixed to particular centers.
     * </p>
     * <p>For example, if an "M" is performed and then an "F" is needed, it should affect the front layer seen in the front position and not the bottom layer, where the center that was in the front position
     * is now placed. The chosen way is very familiar to the "corner-starters" (solving the cube starting from the corners).
     * </p>
     * <p>
     * The sequence is defined in extended Singmaster's notation. The basis for the turns are six letters of the following meaning.
     * <ul>
     * <li>U - Up (rotate top layer)</li>
     * <li>D - Down (rotate bottom layer)</li>
     * <li>F - Front (rotate front layer)</li>
     * <li>B - Back (rotate back layer)</li>
     * <li>L - Left (rotate left layer)</li>
     * <li>R - Right (rotate right layer)</li>
     * </ul>
     * </p>
     * <p>
     * The letter case is important here, because the same - but lowercase - letters are used for different moves. Modifiers can be appended to the move character.
     * <ul>
     * <li>Separate characters mean turning the corresponding layer 90 degrees clock-wise.</li>
     * <li>Appending apostrophe "'" or digit "3" means turning 90 degrees counter clock-wise.</li>
     * <li>Appending digit "2" means 180 degrees rotation of the corresponding layer (clock-wise).</li>
     * <li>You can use combination "2'" for double counter clock-wise turn. This combination is useful if you want to show the most efficient directions when using finger shortcuts.
     * </li>
     * </ul>
     * </p>
     * <p>
     * There are also some advanced modifiers that are written immediately after the move letter and right before the basic modifiers already defined. The possible modifiers are:
     * <ul>
     * <li>m - middle layer turn between the specified layer and the opposite one</li>
     * <li>c - whole-cube turn in the direction of the specified layer</li>
     * <li>s - slice turn; two opposite layers are turned in the same directions ("Rs" is equal to "R L'" or "L' R")</li>
     * <li>a - anti-slice turn; two opposite layers are turned in the opposite directions ("Ra" is equal to "R L" or "L R")</li>
     * <li>t - thick turn; two adjacent layers (the specified one and the adjacent one) are turned simultaneously</li>
     * </ul>
     * </p>
     * <p>
     * The library supports some additional characters to represent specific moves. The center layers can be rotated using the following characters in combination with previous modifiers.
     * <ul>
     * <li>E - equator (between U and D layers in the U'/D direction)</li>
     * <li>S - standing (between F and B layers in the F/B' direction)</li>
     * <li>M - middle (between L and R layers in the L/R' direction)</li>
     * </ul>
     * </p>
     * <p>
     * The library also supports turns of the entire cube. This feature can be used to rotate the cube in order to show the cube in the best position for the current situation to watch the move sequence. The available symbols to rotate the cube are shown in the following table (they can be also combined with the modifiers).
     * <ul>
     * <li> X - rotate around x-axis (in the same direction as "R" or "L'" is performed)</li>
     * <li>Y - rotate around y-axis (in the same direction as "F" or "B'" is performed)</li>
     * <li>Z - rotate around z-axis (in the same direction as "U" or "D'" is performed)</li>
     * </ul>
     * </p>
     * <p>
     * There is also a possibility to rotate two adjacent layers simultaneously. The notation and meaning is similar to the face-layer rotations, but the letters are in lowercase.
     * <ul>
     * <li>u - up (rotate two top layers)</li>
     * <li>d - down (rotate two bottom layers)</li>
     * <li>f - front (rotate two front layers)</li>
     * <li>b - back (rotate two back layers)</li>
     * <li>l - left (rotate two left layers)</li>
     * <li>r - right (rotate two right layers)</li>
     * </ul>
     * </p>
     * <p>
     * There is yet another character to be used in the parameter value - the dot '.' character. When a dot is found in the sequence during playing the animation, it is delayed for a half of the time the quarter turn is performed.
     * </p>
     * <p><b>Important:</b> In Josef Jelink's original AnimCube applet there could be several move sequences specified in the same string. The sequences were separated by the semicolon character ';'. This feature however is disabled in this version.<br>
     * If the move sequence string passed to this method has more than one move sequences defined, only the first will be taken into consideration, and the next will be ignored.</p>
     * <p><b>Note:</b> For additional details and a few left out alternatives to certain notations, see Josef's complete documentation for the move sequence <a href="http://software.rubikscube.info/AnimCube/#move">here.</a></p>
     *
     * @param moveSequence a {@link String} containing the desired move sequence, using the format described above.
     * @see <a href="http://software.rubikscube.info/AnimCube/#move">Josef's Jelinek complete documentation for the move sequence.</a>
     */
    public void setMoveSequence(String moveSequence) {
        move = getMove(moveSequence);
    }

    /**
     * <p>
     * Resets the cube to its initial state. This includes:
     * <ul>
     * <li>stopping any running animation</li>
     * <li>resetting the facelets colors to their initial state</li>
     * <li>resetting the move counter to the start of the currently defined move sequence.</li>
     * </ul>
     * </p>
     *
     * @see #setMoveSequence(String)
     * @see #setCubeModel(String)
     */
    public void resetToInitialState() {
        synchronized (animThreadLock) {
            boolean wasAnimating = animating;
            if (animating) {
                stopAnimation();
            }
            movePos = 0;
            resetCubeColors();
            if (!wasAnimating) {
                //notify listeners is also called when interrupting a current animation..this is just s.t. it won't be called twice.
                notifyHandlerCubeModelUpdated();
            }
        }
        repaint();
    }

    /**
     * <p>
     * Animates all the moves in the currently set move sequence one move at a time. When a move has completed, the next one is automatically started.
     * </p>
     * <p>The animation stops when the last move in the move sequence is reached and animated.</p>
     */
    public void animateMoveSequence() {
        startAnimation(AnimationMode.AUTO_PLAY_FORWARD);
    }

    /**
     * <p>
     * Animates all the moves in the currently set move sequence one move at a time, <i>in reverse</i> (i.e. from end to start with opposite twisting direction). When a move has
     * completed, the next one is automatically started.
     * </p>
     * <p>The animation stops when the first move in the move sequence is reached and animated.</p>
     */
    public void animateMoveSequenceReversed() {
        startAnimation(AnimationMode.AUTO_PLAY_BACKWARD);
    }

    /**
     * <p>
     * Animates <i>only</i> the next move from the move sequence. When it has completed, the next one is <b>not</b> automatically started.
     * </p>
     */
    public void animateMove() {
        startAnimation(AnimationMode.STEP_FORWARD);
    }

    /**
     * <p>
     * Animates in reverse (i.e. with opposite twisting direction) <i>only</i> the previous move from the move sequence. When it has completed, the next one is <b>not</b> automatically started.
     * </p>
     */
    public void animateMoveReversed() {
        startAnimation(AnimationMode.STEP_BACKWARD);
    }

    /**
     * <p>Instantly applies the whole move sequence on the cube, without animation.</p>
     */
    public void applyMoveSequence() {
        startAnimation(AnimationMode.AUTO_FAST_FORWARD);
    }

    /**
     * <p>Instantly applies the whole move sequence in reverse, on the cube, without animation.</p>
     */
    public void applyMoveSequenceReversed() {
        startAnimation(AnimationMode.AUTO_FAST_BACKWARD);
    }

    /**
     * <p>Instantly applies the next move on the cube, without animation.</p>
     */
    public void applyMove() {
        startAnimation(AnimationMode.STEP_FAST_FORWARD);
    }

    /**
     * <p>Instantly applies the previous move on reverse, on the cube, without animation.</p>
     */
    public void applyMoveReversed() {
        startAnimation(AnimationMode.STEP_FAST_BACKWARD);
    }

    /**
     * Stops an in-progress animation. No-op if an animation is not in progress.
     */
    public void stopAnimation() {
        LogUtil.e(TAG, "stopAnimation.", isDebuggable);
        synchronized (animThreadLock) {
            animationMode = AnimationMode.STOPPED;
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

    /**
     * <p>
     * Register a lister to be notified when the cube model is updated.
     * </p>
     * <p>
     * This normally happens after a spin animation, or after the user manually rotates a cube side.
     * </p>
     *
     * @param onCubeModelUpdatedListener the listener interested in cube model updates
     */
    public void setOnCubeModelUpdatedListener(AnimCube.OnCubeModelUpdatedListener onCubeModelUpdatedListener) {
        synchronized (animThreadLock) {
            if (onCubeModelUpdatedListener == null) {
                //listener removed, shutdown handler
                this.mainThreadHandler.removeMessages(NOTIFY_LISTENER_MODEL_UPDATED);
            }
            this.cubeModelUpdatedListener = onCubeModelUpdatedListener;
        }
    }

    /**
     * <p>
     * Register a lister to be notified when the cube has finished animating a move sequence.
     * </p>
     *
     * @param onCubeAnimationFinishedListener the listener interested being notified when the cube animation completes.
     */
    public void setOnAnimationFinishedListener(AnimCube.OnCubeAnimationFinishedListener onCubeAnimationFinishedListener) {
        synchronized (animThreadLock) {
            if (onCubeAnimationFinishedListener == null) {
                //listener removed, shutdown handler
                this.mainThreadHandler.removeMessages(NOTIFY_LISTENER_ANIMATION_FINISHED);
            }
            this.cubeAnimationFinishedListener = onCubeAnimationFinishedListener;
        }
    }

    /**
     * <p>
     * Saves the cube current state to a bundle, in order for it to be recovered after a configuration change or after the app is resumed from background.
     * </p>
     * <p>
     * If needed, the saved state can be read from the bundle by reading the values stored at keys defined in {@link CubeState}.
     * </p>
     *
     * @return a {@link Bundle} containing the cube's current state
     * @see #restoreState(Bundle)
     */
    public Bundle saveState() {
        Bundle b = new Bundle();
        int[][] cubeDeepCopy = new int[6][9];
        synchronized (animThreadLock) {
            CubeUtils.deepCopy2DArray(cube, cubeDeepCopy);
            for (int i = 0; i < cubeDeepCopy.length; i++) {
                b.putIntArray(CubeState.KEY_CUBE + i, cubeDeepCopy[i]);
            }
            for (int i = 0; i < initialCube.length; i++) {
                b.putIntArray(CubeState.KEY_INITIAL_CUBE + i, initialCube[i]);
            }
            b.putIntArray(CubeState.KEY_MOVE, move);
            b.putBoolean(CubeState.KEY_IS_ANIMATING, animating);
            b.putInt(CubeState.KEY_ANIMATION_MODE, animationMode);
            b.putDoubleArray(CubeState.KEY_EYE, eye);
            b.putDoubleArray(CubeState.KEY_EYE_X, eyeX);
            b.putDoubleArray(CubeState.KEY_EYE_Y, eyeY);
            b.putDouble(CubeState.KEY_ORIGINAL_ANGLE, originalAngle);
            if (moveDir == -1) {
                b.putInt(CubeState.KEY_MOVE_POS, movePos == move.length ? move.length : movePos + 1);
            } else {
                b.putInt(CubeState.KEY_MOVE_POS, movePos);
            }
            b.putBoolean(CubeState.KEY_EDITABLE, editable);
            b.putInt(CubeState.KEY_BACKFACES_DISTANCE, backFacesDistance);
            b.putInt(CubeState.KEY_SINGLE_ROTATION_SPEED, speed);
            b.putInt(CubeState.KEY_DOUBLE_ROTATION_SPEED, doubleSpeed);
            b.putBoolean(CubeState.KEY_IS_DEBUGGABLE, isDebuggable);
        }
        return b;
    }

    /**
     * <p>
     * Restores a previously saved state.
     * </p>
     * <p>
     * If the cube was animating when its state was saved, then this method will also resume the animation and repeat the step
     * that was interrupted by the configuration change.
     * </p>
     *
     * @param state a {@link Bundle} containing a previously saved state of the cube.
     * @see #saveState()
     */
    public void restoreState(Bundle state) {
        synchronized (animThreadLock) {
            for (int i = 0; i < cube.length; i++) {
                cube[i] = state.getIntArray(CubeState.KEY_CUBE + i);
            }
            for (int i = 0; i < initialCube.length; i++) {
                initialCube[i] = state.getIntArray(CubeState.KEY_INITIAL_CUBE + i);
            }

            move = state.getIntArray(CubeState.KEY_MOVE);
            movePos = state.getInt(CubeState.KEY_MOVE_POS);
            originalAngle = state.getDouble(CubeState.KEY_ORIGINAL_ANGLE);

            double[] buffer = state.getDoubleArray(CubeState.KEY_EYE);
            System.arraycopy(buffer, 0, eye, 0, eye.length);
            buffer = state.getDoubleArray(CubeState.KEY_EYE_X);
            System.arraycopy(buffer, 0, eyeX, 0, eyeX.length);
            buffer = state.getDoubleArray(CubeState.KEY_EYE_Y);
            System.arraycopy(buffer, 0, eyeY, 0, eyeY.length);

            editable = state.getBoolean(CubeState.KEY_EDITABLE);
            backFacesDistance = state.getInt(CubeState.KEY_BACKFACES_DISTANCE);
            setBackFacesDistanceInternal(backFacesDistance);
            speed = state.getInt(CubeState.KEY_SINGLE_ROTATION_SPEED);
            doubleSpeed = state.getInt(CubeState.KEY_DOUBLE_ROTATION_SPEED);
            isDebuggable = state.getBoolean(CubeState.KEY_IS_DEBUGGABLE);

            repaint();
            boolean animating = state.getBoolean(CubeState.KEY_IS_ANIMATING);
            if (animating) {
                int animationMode = state.getInt(CubeState.KEY_ANIMATION_MODE);
                if (animationMode != AnimationMode.STOPPED) {
                    startAnimation(animationMode);
                }
            }
        }
    }

    /**
     * <p>
     * Stops drawing and cleans up the held by this instance. Make sure to call when this instance is discarded, to prevent memory leaks.
     * </p>
     */
    public void cleanUpResources() {
        LogUtil.d(TAG, "cleanUpResources", isDebuggable);
        stopAnimationAndDrawing();
        getHolder().removeCallback(surfaceCallback);
        surfaceCallback = null;
        animThread.interrupt();
        animRunnable = null;
        mainThreadHandler = null;
        setOnTouchListener(null);
        LogUtil.d(TAG, "cleanUpResources: finish", isDebuggable);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        synchronized (animThreadLock) {
            if (animating) {
                return false;
            }
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mActionDownReceived = true;
                handlePointerDownEvent(event);
                return true;
            case MotionEvent.ACTION_UP:
                if (mActionDownReceived) {
                    handlePointerUpEvent();
                    mActionDownReceived = false;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                handlePointerDragEvent(event);
                return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            performDraw(canvas);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.AnimCube);

        initBackgroundColor(attributes);
        initCubeColors(attributes);
        initFaceletsContourColor(attributes);
        initCubeInitialState(attributes);
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

        if (!isInEditMode()) {
            // get the surface holder of he current surface view, add this view as a
            // callback
            getHolder().addCallback(surfaceCallback);
            animThread = new Thread(animRunnable, "AnimThread");
            // start animation thread
            animThread.start();

            // register to receive touch events
            setOnTouchListener(this);
        }
    }

    private void initDebuggable(TypedArray attributes) {
        this.isDebuggable = attributes.getBoolean(R.styleable.AnimCube_debuggable, false);
    }

    private void initBackgroundColor(TypedArray attributes) {
        this.backgroundColor = attributes.getColor(R.styleable.AnimCube_backgroundColor, Color.WHITE);
        // setup colors (contrast)
        this.backgroundColor2 = Color.rgb(Color.red(backgroundColor) / 2, Color.green(backgroundColor) / 2, Color.blue(backgroundColor) / 2);
    }

    private void initCubeColors(TypedArray attributes) {
        int colorsResourceId = attributes.getResourceId(R.styleable.AnimCube_cubeColors, R.array.cube_default_colors);
        if (isInEditMode()) {
            String[] s = getResources().getStringArray(colorsResourceId);
            for (int j = 0; j < s.length; j++) {
                cubeColors[j] = Color.parseColor(s[j]);
            }
        } else {
            TypedArray typedArray = getResources().obtainTypedArray(colorsResourceId);
            for (int i = 0; i < 6; i++) {
                cubeColors[i] = typedArray.getColor(i, 0);
            }
            typedArray.recycle();
        }
    }

    private void initFaceletsContourColor(TypedArray attributes) {
        this.faceletsContourColor = attributes.getColor(R.styleable.AnimCube_faceletsContourColor, Color.BLACK);
    }

    private void initCubeInitialState(TypedArray attributes) {
        String cubeColorsString = attributes.getString(R.styleable.AnimCube_initialState);
        if (cubeColorsString == null || cubeColorsString.length() != 54) {
            setCubeInDefaultState();
        } else {
            setStringCubeModelInternal(cubeColorsString);
        }
    }

    private void initMoves(TypedArray attributes) {
        String moves = attributes.getString(R.styleable.AnimCube_moves);
        if (moves != null) {
            setMoveSequence(moves);
        } else {
            move = new int[0];
        }
        movePos = 0;
    }

    private void initEditable(TypedArray attributes) {
        this.editable = attributes.getBoolean(R.styleable.AnimCube_editable, false);
    }

    private void initInitialRotation(TypedArray attributes) {
        int styleableIndex = R.styleable.AnimCube_initialRotation;
        String initialRotation = DEFAULT_INITIAL_CUBE_ROTATION;
        if (attributes.hasValue(styleableIndex)) {
            initialRotation = attributes.getString(styleableIndex);
        }
        setupInitialViewAngle(initialRotation);
    }

    private void initBackFacesDistance(TypedArray attributes) {
        int backFaceDistance = attributes.getInt(R.styleable.AnimCube_backFacesDistance, 0);
        setBackFacesDistanceInternal(backFaceDistance);
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
                if (CubeAlign.TOP.equals(alignParam)) {
                    // top
                    align = 0;
                } else if (CubeAlign.CENTER.equals(alignParam)) {
                    // center
                    align = 1;
                } else if (CubeAlign.BOTTOM.equals(alignParam)) {
                    // bottom
                    align = 2;
                }
            }
        } else {
            align = 1;
        }
    }

    private void initSingleRotationSpeed(TypedArray attributes) {
        this.speed = attributes.getInt(R.styleable.AnimCube_singleRotationSpeed, 5);
    }

    private void initDoubleRotationSpeed(TypedArray attributes) {
        this.doubleSpeed = attributes.getInt(R.styleable.AnimCube_doubleRotationSpeed, this.speed * 3 / 2);
    }

    /**
     * <p>
     * Begins performing the moves specified through {@link #setMoveSequence(String)}.
     * </p>
     * <p>
     * Supports several <i>animation modes</i>, specified through values from {@link AnimationMode}.
     * </p>
     *
     * @param mode a values from {@link AnimationMode} indicating the desires animation mode.
     * @see AnimationMode
     */
    private void startAnimation(int mode) {
        LogUtil.e(TAG, "startAnimation. mode:" + mode, isDebuggable);
        synchronized (animThreadLock) {
            LogUtil.e(TAG, "startAnimation. acquired lock", isDebuggable);
            stopAnimation();
            if (move.length == 0) {
                return;
            }
            switch (mode) {
                case AnimationMode.AUTO_PLAY_FORWARD: // play forward
                    moveDir = 1;
                    moveOne = false;
                    moveAnimated = true;
                    break;
                case AnimationMode.AUTO_PLAY_BACKWARD: // play backward
                    moveDir = -1;
                    moveOne = false;
                    moveAnimated = true;
                    break;
                case AnimationMode.STEP_FORWARD: // step forward
                    moveDir = 1;
                    moveOne = true;
                    moveAnimated = true;
                    break;
                case AnimationMode.STEP_BACKWARD: // step backward
                    moveDir = -1;
                    moveOne = true;
                    moveAnimated = true;
                    break;
                case AnimationMode.AUTO_FAST_FORWARD: // fast forward
                    moveDir = 1;
                    moveOne = false;
                    moveAnimated = false;
                    break;
                case AnimationMode.AUTO_FAST_BACKWARD: // fast forward
                    moveDir = -1;
                    moveOne = false;
                    moveAnimated = false;
                    break;
                case AnimationMode.STEP_FAST_FORWARD: // step one fast forward
                    moveDir = 1;
                    moveOne = true;
                    moveAnimated = false;
                    break;
                case AnimationMode.STEP_FAST_BACKWARD: // step one fast backward
                    moveDir = -1;
                    moveOne = true;
                    moveAnimated = false;
                    break;
                default:
                    LogUtil.w(TAG, "Unknown animation mode:" + mode + ". Nothing performed.", isDebuggable);
                    return;
            }
            animationMode = mode;
            LogUtil.e(TAG, "startAnimation: notify", isDebuggable);
            animThreadLock.notify();
            LogUtil.e(TAG, "startAnimation: end of sync block", isDebuggable);
        }
    }

    private void performDraw(Canvas canvas) {
        LogUtil.d(TAG, "performDraw: canvas", isDebuggable);
        synchronized (animThreadLock) {
            LogUtil.d(TAG, "performDraw: START Sync block", isDebuggable);
            paint.setColor(backgroundColor);
            if (isInEditMode()) {
                //Canvas.drawPaint is not supported in editMode...
                canvas.drawRect(0, 0, 100000, 100000, paint);
            } else {
                canvas.drawPaint(paint);
            }
            int height = getHeight();
            int width = getWidth();
            // create offscreen buffer for double buffering
            if (width != this.width || height != this.height) {
                this.width = width;
                this.height = height;
            }

            dragAreas = 0;
            if (natural) { // compact cube
                LogUtil.d(TAG, "performDraw: compact cube", isDebuggable);
                fixBlock(canvas, eye, eyeX, eyeY, cubeBlocks, 3); // draw cube and fill drag areas
            } else { // in twisted state
                LogUtil.d(TAG, "performDraw: in twisted state", isDebuggable);
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
                if (topProd < 0 && botProd > 0) {
                    // top facing away
                    orderMode = 0;
                } else if (topProd > 0 && botProd < 0) {
                    // bottom facing away: draw it first
                    orderMode = 1;
                } else {
                    // both top and bottom layer facing away: draw them first
                    orderMode = 2;
                }
                fixBlock(canvas,
                        eyeArray[eyeOrder[twistedMode][drawOrder[orderMode][0]]],
                        eyeArrayX[eyeOrder[twistedMode][drawOrder[orderMode][0]]],
                        eyeArrayY[eyeOrder[twistedMode][drawOrder[orderMode][0]]],
                        blockArray[drawOrder[orderMode][0]],
                        blockMode[twistedMode][drawOrder[orderMode][0]]);
                fixBlock(canvas,
                        eyeArray[eyeOrder[twistedMode][drawOrder[orderMode][1]]],
                        eyeArrayX[eyeOrder[twistedMode][drawOrder[orderMode][1]]],
                        eyeArrayY[eyeOrder[twistedMode][drawOrder[orderMode][1]]],
                        blockArray[drawOrder[orderMode][1]],
                        blockMode[twistedMode][drawOrder[orderMode][1]]);
                fixBlock(canvas,
                        eyeArray[eyeOrder[twistedMode][drawOrder[orderMode][2]]],
                        eyeArrayX[eyeOrder[twistedMode][drawOrder[orderMode][2]]],
                        eyeArrayY[eyeOrder[twistedMode][drawOrder[orderMode][2]]],
                        blockArray[drawOrder[orderMode][2]],
                        blockMode[twistedMode][drawOrder[orderMode][2]]);
            }
            LogUtil.d(TAG, "performDraw: done end of sync block", isDebuggable);
        }
        LogUtil.d(TAG, "performDraw: done", isDebuggable);
    }

    private void repaint() {
        LogUtil.e(TAG, "#rePaint()", isDebuggable);
        synchronized (animThreadLock) {
            Canvas c = getHolder().lockCanvas();
            if (c != null) {
                performDraw(c);
                getHolder().unlockCanvasAndPost(c);
            }
        }
    }

    private int[] getMove(String sequence) {
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
            move[num++] = getMovePart(sequence.substring(lastPos, pos));
            lastPos = pos + 1;
            pos = sequence.indexOf(';', lastPos);
        }
        move[num] = getMovePart(sequence.substring(lastPos));
        return move[0];
    }

    private int[] getMovePart(String sequence) {
        int length = 0;
        int[] move = new int[sequence.length()];
        // over dimmensioned
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '.') {
                move[length] = -1;
                length++;
            } else if (sequence.charAt(i) == '{') {
                i++;
                while (i < sequence.length()) {
                    if (sequence.charAt(i) == '}') {
                        break;
                    }
                    i++;
                }
            } else {
                for (int j = 0; j < 21; j++) {
                    if (sequence.charAt(i) == "UDFBLRESMXYZxyzudfblr".charAt(j)) {
                        i++;
                        int mode = moveModes[j];
                        move[length] = moveCodes[j] * 24;
                        if (i < sequence.length()) {
                            if (moveModes[j] == 0) { // modifiers
                                // for basic
                                // characters
                                // UDFBLR
                                for (int k = 0; k < modeChar.length; k++) {
                                    if (sequence.charAt(i) == modeChar[k]) {
                                        mode = k + 1;
                                        i++;
                                        break;
                                    }
                                }
                            }
                        }
                        move[length] += mode * 4;
                        if (i < sequence.length()) {
                            if (sequence.charAt(i) == '1') {
                                i++;
                            } else if (sequence.charAt(i) == '\''
                                    || sequence.charAt(i) == '3') {
                                move[length] += 2;
                                i++;
                            } else if (sequence.charAt(i) == '2') {
                                i++;
                                if (i < sequence.length()
                                        && sequence.charAt(i) == '\'') {
                                    move[length] += 3;
                                    i++;
                                } else {
                                    move[length] += 1;
                                }
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
        System.arraycopy(move, 0, returnMove, 0, length);
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
    }

    private void setCubeInDefaultState() {
        // clean the cube
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                cube[i][j] = i;
            }
        }
        CubeUtils.deepCopy2DArray(cube, initialCube);
    }

    private boolean setStringCubeModelInternal(String colorValues) {
        // setup color facelets
        if (colorValues.length() != 54) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = CubeColors.WHITE; k < CubeColors.GREEN + 1; k++) {
                    if (Integer.parseInt(colorValues.charAt(i * 9 + j) + "") == k) {
                        cube[i][j] = k;
                        break;
                    }
                }
            }
        }
        CubeUtils.deepCopy2DArray(cube, initialCube);
        return true;
    }

    private void setBackFacesDistanceInternal(int backFaceDistance) {
        this.backFacesDistance = backFaceDistance;
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

    private void resetCubeColors() {
        CubeUtils.deepCopy2DArray(initialCube, cube);
    }

    private void animateCube() {
        LogUtil.e(TAG, "animateCube.", isDebuggable);
        synchronized (animThreadLock) {
            LogUtil.e(TAG, "animateCube acquired lock - sync block", isDebuggable);
            interrupted = false;
            animThreadInactive = false;
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
                    interrupted = true;
                    LogUtil.e(TAG, "animateCube: after wait, interrupted exception:" + e.getMessage(), isDebuggable);
                    break;
                }
                if (restarted) {
                    continue;
                }
                boolean restart = false;
                animating = true;
                int[] mv = move;
                if (moveDir > 0) {
                    if (movePos >= mv.length) {
                        movePos = 0;
                    }
                } else {
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
                        repaint();
                        if (!moveOne) {
                            LogUtil.d(TAG, "animateCube: before sleep 33 * speed", isDebuggable);
                            sleep(33 * speed);
                            LogUtil.d(TAG, "animateCube: after sleep 33 * speed", isDebuggable);
                            if (interrupted || restarted) {
                                LogUtil.e(TAG, "animateCube: interrupted", isDebuggable);
                                break;
                            }
                        }
                    } else if (mv[movePos] < 1000) {
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
                        LogUtil.d(TAG, "animateCube: spin", isDebuggable);
                        spin(mv[movePos] / 24, num, mode, clockwise, moveAnimated);
                        LogUtil.d(TAG, "animateCube: after spin", isDebuggable);
                        if (moveOne) {
                            restart = true;
                        }
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
                    if (interrupted || restarted || restart) {
                        LogUtil.e(TAG, "animateCube: thread interrupted", isDebuggable);
                        break;
                    }
                }
                animating = false;
                animationMode = AnimationMode.STOPPED;
                LogUtil.d(TAG, "animateCube: repaint at end of loop", isDebuggable);
                repaint();
                notifyHandlerAnimationFinished();
            } while (!interrupted);
            animThreadInactive = true;
        }
        LogUtil.e(TAG, "Animate cube: Interrupted, all is fine, ended!", isDebuggable);
    } // run()

    private void stopAnimationAndDrawing() {
        LogUtil.d(TAG, "#stopAnimationAndDrawing: before animThreadLock", isDebuggable);
        synchronized (animThreadLock) {
            LogUtil.d(TAG, "stopAnimationAndDrawing: in animThreadLock sync block", isDebuggable);
            interrupted = true;
            LogUtil.d(TAG, "stopAnimationAndDrawing: end of animThreadLock sync block", isDebuggable);
        }

        if (animThread.isAlive()) {
            LogUtil.w(TAG, "stopAnimationAndDrawing calling JOIN on AnimThread.", isDebuggable);
            animThread.interrupt();
            try {
                animThread.join();
            } catch (InterruptedException e) {
                LogUtil.d(TAG, "interrupted while waiting for AnimThread to finish", isDebuggable);
            }
        }

        LogUtil.w(TAG, "stopAnimationAndDrawing DONE.", isDebuggable);
    }

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

    private void spin(int layer, int num, int mode, boolean clockwise, boolean animated) {
        twisting = false;
        natural = true;
        spinning = true;
        originalAngle = 0;
        if (faceTwistDirs[layer] > 0) {
            clockwise = !clockwise;
        }
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
                LogUtil.d(TAG, "spin: repaint in spin angle loop", isDebuggable);
                repaint();
                sleep(1);
                if (interrupted || restarted) {
                    LogUtil.d(TAG, "spin: interrupted or restarted in spin, break;", isDebuggable);
                    break;
                }
                lTime = System.currentTimeMillis();
            }
        }
        currentAngle = 0;
        twisting = false;
        natural = true;
        twistLayers(cube, layer, num, mode);
        notifyHandlerCubeModelUpdated();
        LogUtil.e(TAG, "Updated Cube Model -> twistLayers in spin", isDebuggable);
        spinning = false;
        if (animated) {
            LogUtil.e(TAG, "Updated Cube Model -> is animated, request repaint. isInterrupted:" + interrupted + " isRestarted:" + restarted, isDebuggable);
            repaint();
        }
    }

    private void notifyHandlerCubeModelUpdated() {
        mainThreadHandler.sendEmptyMessage(NOTIFY_LISTENER_MODEL_UPDATED);
    }

    private void notifyListenerCubeUpdatedOnMainThread() {
        int[][] cubeCopy = new int[6][9];
        synchronized (animThreadLock) {
            CubeUtils.deepCopy2DArray(cube, cubeCopy);
        }
        if (cubeModelUpdatedListener != null) {
            cubeModelUpdatedListener.onCubeModelUpdate(cubeCopy);
        }
    }

    private void notifyHandlerAnimationFinished() {
        if (mainThreadHandler != null) {
            mainThreadHandler.sendEmptyMessage(NOTIFY_LISTENER_ANIMATION_FINISHED);
        }
    }

    private void notifyListenerAnimationFinishedOnMainThread() {
        if (cubeAnimationFinishedListener != null) {
            cubeAnimationFinishedListener.onAnimationFinished();
        }
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
        LogUtil.e(TAG, "twistLayers", isDebuggable);

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
            for (int i = 0; i < 8; i++) {// to buffer
                twistBuffer[(i + num * 2) % 8] = cube[layer][cycleOrder[i]];
            }
            for (int i = 0; i < 8; i++) {// to cube
                cube[layer][cycleOrder[i]] = twistBuffer[i];
            }
            LogUtil.e(TAG, "Updated Cube in twistLayer", isDebuggable);
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
                LogUtil.e(TAG, "Updated Cube in twistLayer#2. j:" + j, isDebuggable);
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
            if (align == 0) {
                coordsY[i] = height / 2.0 * scale - y;
            } else if (align == 2) {
                coordsY[i] = height - (height / 2.0 * scale) - y;
            } else {
                coordsY[i] = height / 2.0 - y;
            }
        }
        // setup corner co-ordinates for all faces
        for (int i = 0; i < 6; i++) { // all faces
            for (int j = 0; j < 4; j++) { // all face corners
                cooX[i][j] = coordsX[faceCorners[i][j]];
                cooY[i][j] = coordsY[faceCorners[i][j]];
            }
        }
        if (showBackFaces) { // draw showBackFaces hidden facelets
            for (int i = 0; i < 6; i++) { // all faces
                vSub(vScale(vCopy(perspEye, eye), 5.0 + perspective), faceNormals[i]); // perspective correction
                if (vProd(perspEye, faceNormals[i]) < 0) { // draw only hidden faces
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
                                    getCorners(i, j, fillX, fillY, q + border[j][0], p + border[j][1]);
                                    fillX[j] += x;
                                    fillY[j] -= y;
                                }
                                paint.setColor(cubeColors[cube[i][p * 3 + q]]);
                                paint.setStyle(Paint.Style.FILL);

                                path.reset();
                                path.moveTo(fillX[0], fillY[0]);
                                path.lineTo(fillX[1], fillY[1]);
                                path.lineTo(fillX[2], fillY[2]);
                                path.lineTo(fillX[3], fillY[3]);
                                path.close();

                                canvas.drawPath(path, paint);
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setColor(darkerColor(cubeColors[cube[i][p * 3 + q]]));
                                canvas.drawPath(path, paint);
                            }
                        }
                    }
                }
            }
        }
        // draw antialias
        for (int i = 0; i < 6; i++) { // all faces
            int sideW = blocks[i][0][1] - blocks[i][0][0];
            int sideH = blocks[i][1][1] - blocks[i][1][0];
            if (sideW > 0 && sideH > 0) {
                for (int j = 0; j < 4; j++) { // corner co-ordinates
                    getCorners(i, j, fillX, fillY, blocks[i][0][factors[j][0]], blocks[i][1][factors[j][1]]);
                }
                if (sideW == 3 && sideH == 3) {
                    paint.setColor(backgroundColor2);
                } else {
                    paint.setColor(faceletsContourColor);
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
        // find and draw inner faces
        for (int i = 0; i < 6; i++) { // all faces
            int sideW = blocks[i][0][1] - blocks[i][0][0];
            int sideH = blocks[i][1][1] - blocks[i][1][0];
            if (sideW <= 0 || sideH <= 0) { // this face is inner and only black
                for (int j = 0; j < 4; j++) { // for all corners
                    int k = oppositeCorners[i][j];
                    fillX[j] = (int) (cooX[i][j] + (cooX[i ^ 1][k] - cooX[i][j]) * 2.0 / 3.0);
                    fillY[j] = (int) (cooY[i][j] + (cooY[i ^ 1][k] - cooY[i][j]) * 2.0 / 3.0);
                }
                paint.setColor(faceletsContourColor);

                path.reset();
                path.moveTo(fillX[0], fillY[0]);
                path.lineTo(fillX[1], fillY[1]);
                path.lineTo(fillX[2], fillY[2]);
                path.lineTo(fillX[3], fillY[3]);
                path.close();
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);

            } else {
                // draw face background (do not care about normals and visibility!)
                for (int j = 0; j < 4; j++) {
                    // corner co-ordinates
                    getCorners(i, j, fillX, fillY, blocks[i][0][factors[j][0]], blocks[i][1][factors[j][1]]);
                }

                paint.setColor(faceletsContourColor);

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
                            for (int j = 0; j < 4; j++) {
                                getCorners(i, j, fillX, fillY, q + border[j][0], p + border[j][1]);
                            }

                            paint.setColor(darkerColor(cubeColors[cube[i][p * 3 + q]]));

                            path.reset();
                            path.moveTo(fillX[0], fillY[0]);
                            path.lineTo(fillX[1], fillY[1]);
                            path.lineTo(fillX[2], fillY[2]);
                            path.lineTo(fillX[3], fillY[3]);
                            path.close();
                            canvas.drawPath(path, paint);

                            paint.setColor(cubeColors[cube[i][p * 3 + q]]);
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
                if (!editable || animating) {
                    // no need of twisting while animating
                    continue;
                }
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
                        for (int k = 0; k < 4; k++) {
                            // 4 points per area
                            getCorners(i, k, dragCornersX[dragAreas], dragCornersY[dragAreas],
                                    dragBlocks[j][k][0], dragBlocks[j][k][1]);
                        }
                        dragDirsX[dragAreas] = (dxh * areaDirs[j][0] + dxv * areaDirs[j][1]) * twistDirs[i][j];
                        dragDirsY[dragAreas] = (dyh * areaDirs[j][0] + dyv * areaDirs[j][1]) * twistDirs[i][j];
                        dragLayers[dragAreas] = adjacentFaces[i][j % 4];
                        if (j >= 4) {
                            dragLayers[dragAreas] &= ~1;
                        }
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
                        for (int k = 0; k < 4; k++) {
                            getCorners(i, k, dragCornersX[dragAreas], dragCornersY[dragAreas],
                                    dragBlocks[j][k][0], dragBlocks[j][k][1]);
                        }
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
                        for (int k = 0; k < 4; k++) {
                            getCorners(i, k, dragCornersX[dragAreas], dragCornersY[dragAreas],
                                    dragBlocks[j][k][0], dragBlocks[j][k][1]);
                        }
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

    private void getCorners(int face, int corner, int[] cornersX, int[] cornersY, double factor1, double factor2) {
        factor1 /= 3.0;
        factor2 /= 3.0;
        double x1 = cooX[face][0] + (cooX[face][1] - cooX[face][0]) * factor1;
        double y1 = cooY[face][0] + (cooY[face][1] - cooY[face][0]) * factor1;
        double x2 = cooX[face][3] + (cooX[face][2] - cooX[face][3]) * factor1;
        double y2 = cooY[face][3] + (cooY[face][2] - cooY[face][3]) * factor1;
        cornersX[corner] = (int) (0.5 + x1 + (x2 - x1) * factor2);
        cornersY[corner] = (int) (0.5 + y1 + (y2 - y1) * factor2);
    }

    private void handlePointerDownEvent(MotionEvent e) {
        lastDragX = lastX = Math.round(e.getX());
        lastDragY = lastY = Math.round(e.getY());
        toTwist = editable && !animating;
    }

    private void handlePointerUpEvent() {
        if (twisting && !spinning) {
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
                if (faceTwistDirs[twistedLayer] > 0) {
                    num = (4 - num) % 4;
                }
                originalAngle = 0;
                natural = true; // the cube in the natural state
                twistLayers(cube, twistedLayer, num, twistedMode); // rotate the facelets
                //handlePointerUpEvent is always called from the main thread, so we can notify the listener directly, instead of going through the handler
                notifyListenerCubeUpdatedOnMainThread();
                LogUtil.e(TAG, "Updated Cube Model -> twistLayers in handlePointerUpEvent", isDebuggable);
            }
            repaint();
        }
    }

    private void handlePointerDragEvent(MotionEvent e) {
        int x = Math.round(e.getX());
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
                    if (dx * dx + dy * dy < 144) {
                        // delay the decision about twisting
                        return;
                    }
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
            if (natural) {
                splitCube(twistedLayer);
            }
            currentAngle = 0.03 * (dragX * dx + dragY * dy) / Math.sqrt(dragX * dragX + dragY * dragY); // dv * cos a
        }
        repaint();
    }
}