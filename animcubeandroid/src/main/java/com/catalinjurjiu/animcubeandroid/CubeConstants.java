package com.catalinjurjiu.animcubeandroid;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

/**
 * Contains various constants used by the cube when rendering, and when setting certain properties.
 */
public class CubeConstants {

    /**
     * <p>
     * String representing the initial cube rotation.
     * </p>
     */
    public static final String DEFAULT_INITIAL_CUBE_ROTATION = "lluu";

    /**
     * <p>
     * Contains the 3 possible vertical alignment values: TOP,CENTER,BOTTOM.
     * </p>
     * <p>These are only available to set through XML, and only make sense if the cube scale is also modified s.t. it won't fill the whole possible space.</p>
     */
    @StringDef
    public @interface CubeAlign {
        String TOP = "top";
        String CENTER = "center";
        String BOTTOM = "bottom";
    }

    /**
     * <p>Contains the animation modes available to the cube. To be used with {@link AnimCube#startAnimation(int)}</p>
     *
     * @see AnimCube#startAnimation(int)
     */
    @IntDef
    public @interface AnimationMode {
        /**
         * <p>
         * The cube is not animating. It's only used internally by the cube and has no effect when passed to {@link AnimCube#startAnimation(int)}.
         * </p>
         */
        int STOPPED = -1,
        /**
         * <p>
         * Animates the currently set move sequence one move at a time. When a move has completed, the next one is automatically started.
         * </p>
         * <p>The animation stops when the last move in the move sequence is reached and animated.</p>
         */
        AUTO_PLAY_FORWARD = 0,
        /**
         * <p>
         * Animates the currently set move sequence one move at a time, <i>in reverse</i> (i.e. from end to start with opposite twisting direction). When a move has completed, the next one is automatically started.
         * </p>
         * <p>The animation stops when the first move in the move sequence is reached and animated.</p>
         */
        AUTO_PLAY_BACKWARD = 1, // play backward
        /**
         * <p>
         * Animates <i>only</i> the next move from the move sequence. When it has completed, the next one is <b>not</b> automatically started.
         * </p>
         */
        STEP_FORWARD = 2, // step forward
        /**
         * <p>
         * Animates in reverse (i.e. with opposite twisting direction) <i>only</i> the previous move from the move sequence. When it has completed, the next one is <b>not</b> automatically started.
         * </p>
         */
        STEP_BACKWARD = 3, // step backward
        /**
         * <p>Instantly applies the whole move sequence on the cube, without animation.</p>
         */
        AUTO_FAST_FORWARD = 4, // fast forward to end
        /**
         * <p>Instantly applies the whole move sequence in reverse, on the cube, without animation.</p>
         */
        AUTO_FAST_BACKWARD = 5,  // fast backward to start
        /**
         * <p>Instantly applies the next move on the cube, without animation.</p>
         */
        STEP_FAST_FORWARD = 6, // fast forward one move
        /**
         * <p>Instantly applies the previous move on reverse, on the cube, without animation.</p>
         */
        STEP_FAST_BACKWARD = 7;  // fast backward one move
    }

    /**
     * <p>
     * Defines constants that match the default cube colors.
     * </p>
     */
    @IntDef
    public @interface CubeColors {
        int WHITE = 0,
                YELLOW = 1,
                ORANGE = 2,
                RED = 3,
                BLUE = 4,
                GREEN = 5;
    }

    /**
     * <p>
     * Contains the keys used to store various state information when {@link AnimCube#saveState()} is called.
     * </p>
     */
    @StringDef
    public @interface CubeState {
        String KEY_CUBE = "AnimCubeState::cube",
                KEY_INITIAL_CUBE = "AnimCubeState::initialCube",
                KEY_MOVE = "AnimCubeState::move",
                KEY_MOVE_POS = "AnimCubeState::movePos",
                KEY_EYE = "AnimCubeState::eye",
                KEY_EYE_X = "AnimCubeState::eyeX",
                KEY_EYE_Y = "AnimCubeState::eyeY",
                KEY_IS_ANIMATING = "AnimCubeState::isAnimating",
                KEY_ANIMATION_MODE = "AnimCubeState::animationMode",
                KEY_ORIGINAL_ANGLE = "AnimCubeState::originalAngle",
                KEY_EDITABLE = "AnimCubeState::editable",
                KEY_BACKFACES_DISTANCE = "AnimCubeState::backfacesDistance",
                KEY_SINGLE_ROTATION_SPEED = "AnimCubeState::singleRotationSpeed",
                KEY_DOUBLE_ROTATION_SPEED = "AnimCubeState::doubleRotationSpeed",
                KEY_IS_DEBUGGABLE = "AnimCubeState::isDebuggable";
    }

    static class ComputationLogic {
        // normal vectors
        static final double[][] faceNormals = {
                {0.0, -1.0, 0.0}, // U
                {0.0, 1.0, 0.0}, // D
                {0.0, 0.0, -1.0}, // F
                {0.0, 0.0, 1.0}, // B
                {-1.0, 0.0, 0.0}, // L
                {1.0, 0.0, 0.0}  // R
        };
        // vertex co-ordinates
        static final double[][] cornerCoords = {
                {-1.0, -1.0, -1.0}, // UFL
                {1.0, -1.0, -1.0}, // UFR
                {1.0, -1.0, 1.0}, // UBR
                {-1.0, -1.0, 1.0}, // UBL
                {-1.0, 1.0, -1.0}, // DFL
                {1.0, 1.0, -1.0}, // DFR
                {1.0, 1.0, 1.0}, // DBR
                {-1.0, 1.0, 1.0}  // DBL
        };
        // vertices of each face
        static final int[][] faceCorners = {
                {0, 1, 2, 3}, // U: UFL UFR UBR UBL
                {4, 7, 6, 5}, // D: DFL DBL DBR DFR
                {0, 4, 5, 1}, // F: UFL DFL DFR UFR
                {2, 6, 7, 3}, // B: UBR DBR DBL UBL
                {0, 3, 7, 4}, // L: UFL UBL DBL DFL
                {1, 5, 6, 2}  // R: UFR DFR DBR UBR
        };
        // corresponding corners on the opposite face
        static final int[][] oppositeCorners = {
                {0, 3, 2, 1}, // U->D
                {0, 3, 2, 1}, // D->U
                {3, 2, 1, 0}, // F->B
                {3, 2, 1, 0}, // B->F
                {0, 3, 2, 1}, // L->R
                {0, 3, 2, 1}, // R->L
        };
        // faces adjacent to each face
        static final int[][] adjacentFaces = {
                {2, 5, 3, 4}, // U: F R B L
                {4, 3, 5, 2}, // D: L B R F
                {4, 1, 5, 0}, // F: L D R U
                {5, 1, 4, 0}, // B: R D L U
                {0, 3, 1, 2}, // L: U B D F
                {2, 1, 3, 0}  // R: F D B U
        };
        // directions of facelet cycling for all faces
        static final int[] faceTwistDirs = {1, 1, -1, -1, -1, -1};
        static final int[] moveModes = {
                0, 0, 0, 0, 0, 0, // UDFBLR
                1, 1, 1,          // ESM
                3, 3, 3, 3, 3, 3, // XYZxyz
                2, 2, 2, 2, 2, 2  // udfblr
        };
        static final int[] moveCodes = {
                0, 1, 2, 3, 4, 5, // UDFBLR
                1, 2, 4,          // ESM
                5, 2, 0, 5, 2, 0, // XYZxyz
                0, 1, 2, 3, 4, 5  // udfblr
        };
        static final char[] modeChar = {'m', 't', 'c', 's', 'a'};
        // cube dimensions in number of facelets (mincol, maxcol, minrow, maxrow) for compact cube
        static final int[][][] cubeBlocks = {
                {{0, 3}, {0, 3}}, // U
                {{0, 3}, {0, 3}}, // D
                {{0, 3}, {0, 3}}, // F
                {{0, 3}, {0, 3}}, // B
                {{0, 3}, {0, 3}}, // L
                {{0, 3}, {0, 3}}  // R
        };
        // all possible subcube dimensions for top and bottom layers
        static final int[][][] topBlockTable = {
                {{0, 0}, {0, 0}},
                {{0, 3}, {0, 3}},
                {{0, 3}, {0, 1}},
                {{0, 1}, {0, 3}},
                {{0, 3}, {2, 3}},
                {{2, 3}, {0, 3}}
        };
        // subcube dimmensions for middle layers
        static final int[][][] midBlockTable = {
                {{0, 0}, {0, 0}},
                {{0, 3}, {1, 2}},
                {{1, 2}, {0, 3}}
        };
        // indices to topBlockTable[] and botBlockTable[] for each twistedLayer value
        static final int[][] topBlockFaceDim = {
                // U  D  F  B  L  R
                {1, 0, 3, 3, 2, 3}, // U
                {0, 1, 5, 5, 4, 5}, // D
                {2, 3, 1, 0, 3, 2}, // F
                {4, 5, 0, 1, 5, 4}, // B
                {3, 2, 2, 4, 1, 0}, // L
                {5, 4, 4, 2, 0, 1}  // R
        };
        static final int[][] midBlockFaceDim = {
                // U  D  F  B  L  R
                {0, 0, 2, 2, 1, 2}, // U
                {0, 0, 2, 2, 1, 2}, // D
                {1, 2, 0, 0, 2, 1}, // F
                {1, 2, 0, 0, 2, 1}, // B
                {2, 1, 1, 1, 0, 0}, // L
                {2, 1, 1, 1, 0, 0}  // R
        };
        static final int[][] botBlockFaceDim = {
                // U  D  F  B  L  R
                {0, 1, 5, 5, 4, 5}, // U
                {1, 0, 3, 3, 2, 3}, // D
                {4, 5, 0, 1, 5, 4}, // F
                {2, 3, 1, 0, 3, 2}, // B
                {5, 4, 4, 2, 0, 1}, // L
                {3, 2, 2, 4, 1, 0}  // R
        };
        // top facelet cycle
        static final int[] cycleOrder = {0, 1, 2, 5, 8, 7, 6, 3};
        // side facelet cycle offsets
        static final int[] cycleFactors = {1, 3, -1, -3, 1, 3, -1, -3};
        static final int[] cycleOffsets = {0, 2, 8, 6, 3, 1, 5, 7};
        // indices for faces of layers
        static final int[][] cycleLayerSides = {
                {3, 3, 3, 0}, // U: F=6-3k R=6-3k B=6-3k L=k
                {2, 1, 1, 1}, // D: L=8-k  B=2+3k R=2+3k F=2+3k
                {3, 3, 0, 0}, // F: L=6-3k D=6-3k R=k    U=k
                {2, 1, 1, 2}, // B: R=8-k  D=2+3k L=2+3k U=8-k
                {3, 2, 0, 0}, // L: U=6-3k B=8-k  D=k    F=k
                {2, 2, 0, 1}  // R: F=8-k  D=8-k  B=k    U=2+3k
        };
        // indices for sides of center layers
        static final int[][] cycleCenters = {
                {7, 7, 7, 4}, // E'(U): F=7-3k R=7-3k B=7-3k L=3+k
                {6, 5, 5, 5}, // E (D): L=5-k  B=1+3k R=1+3k F=1+3k
                {7, 7, 4, 4}, // S (F): L=7-3k D=7-3k R=3+k  U=3+k
                {6, 5, 5, 6}, // S'(B): R=5-k  D=1+3k L=1+3k U=5-k
                {7, 6, 4, 4}, // M (L): U=7-3k B=8-k  D=3+k  F=3+k
                {6, 6, 4, 5}  // M'(R): F=5-k  D=5-k  B=3+k  U=1+3k
        };
        static final int[][][] dragBlocks = {
                {{0, 0}, {3, 0}, {3, 1}, {0, 1}},
                {{3, 0}, {3, 3}, {2, 3}, {2, 0}},
                {{3, 3}, {0, 3}, {0, 2}, {3, 2}},
                {{0, 3}, {0, 0}, {1, 0}, {1, 3}},
                // center slices
                {{0, 1}, {3, 1}, {3, 2}, {0, 2}},
                {{2, 0}, {2, 3}, {1, 3}, {1, 0}}
        };
        static final int[][] areaDirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 0}, {0, 1}};
        static final int[][] twistDirs = {
                {1, 1, 1, 1, 1, -1}, // U
                {1, 1, 1, 1, 1, -1}, // D
                {1, -1, 1, -1, 1, 1}, // F
                {1, -1, 1, -1, -1, 1}, // B
                {-1, 1, -1, 1, -1, -1}, // L
                {1, -1, 1, -1, 1, 1}  // R
        };
        // various sign tables for computation of directions of rotations
        static final int[][][] rotCos = {
                {{1, 0, 0}, {0, 0, 0}, {0, 0, 1}}, // U-D
                {{1, 0, 0}, {0, 1, 0}, {0, 0, 0}}, // F-B
                {{0, 0, 0}, {0, 1, 0}, {0, 0, 1}}  // L-R
        };
        static final int[][][] rotSin = {
                {{0, 0, 1}, {0, 0, 0}, {-1, 0, 0}}, // U-D
                {{0, 1, 0}, {-1, 0, 0}, {0, 0, 0}}, // F-B
                {{0, 0, 0}, {0, 0, 1}, {0, -1, 0}}  // L-R
        };
        static final int[][][] rotVec = {
                {{0, 0, 0}, {0, 1, 0}, {0, 0, 0}}, // U-D
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 1}}, // F-B
                {{1, 0, 0}, {0, 0, 0}, {0, 0, 0}}  // L-R
        };
        static final int[] rotSign = {1, -1, 1, -1, 1, -1}; // U, D, F, B, L, R
        static final double[][] border = {{0.10, 0.10}, {0.90, 0.10}, {0.90, 0.90}, {0.10, 0.90}};
        static final int[][] factors = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};
        static final int[][] eyeOrder = {{1, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}, {1, 0, 2}};
        static final int[][] blockMode = {{0, 2, 2}, {2, 1, 2}, {2, 2, 2}, {2, 2, 2}, {2, 2, 2}, {2, 2, 2}};
        static final int[][] drawOrder = {{0, 1, 2}, {2, 1, 0}, {0, 2, 1}};
    }
}
