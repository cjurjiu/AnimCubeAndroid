package com.catalinjurjiu.animcubeandroid;

/**
 * Contains the constants form the cube solver, 3D solution display and from the RubikCube, RubikFace classes
 */
public class CubeConstants {
    // normal vectors
    public static final double[][] faceNormals = {
            {0.0, -1.0, 0.0}, // U
            {0.0, 1.0, 0.0}, // D
            {0.0, 0.0, -1.0}, // F
            {0.0, 0.0, 1.0}, // B
            {-1.0, 0.0, 0.0}, // L
            {1.0, 0.0, 0.0}  // R
    };
    // vertex co-ordinates
    public static final double[][] cornerCoords = {
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
    public static final int[][] faceCorners = {
            {0, 1, 2, 3}, // U: UFL UFR UBR UBL
            {4, 7, 6, 5}, // D: DFL DBL DBR DFR
            {0, 4, 5, 1}, // F: UFL DFL DFR UFR
            {2, 6, 7, 3}, // B: UBR DBR DBL UBL
            {0, 3, 7, 4}, // L: UFL UBL DBL DFL
            {1, 5, 6, 2}  // R: UFR DFR DBR UBR
    };
    // corresponding corners on the opposite face
    public static final int[][] oppositeCorners = {
            {0, 3, 2, 1}, // U->D
            {0, 3, 2, 1}, // D->U
            {3, 2, 1, 0}, // F->B
            {3, 2, 1, 0}, // B->F
            {0, 3, 2, 1}, // L->R
            {0, 3, 2, 1}, // R->L
    };
    // faces adjacent to each face
    public static final int[][] adjacentFaces = {
            {2, 5, 3, 4}, // U: F R B L
            {4, 3, 5, 2}, // D: L B R F
            {4, 1, 5, 0}, // F: L D R U
            {5, 1, 4, 0}, // B: R D L U
            {0, 3, 1, 2}, // L: U B D F
            {2, 1, 3, 0}  // R: F D B U
    };
    // directions of facelet cycling for all faces
    public static final int[] faceTwistDirs = {1, 1, -1, -1, -1, -1};
    // transformation tables for compatibility with Lars's applet
    public static final int[] posFaceTransform = {3, 2, 0, 5, 1, 4};
    public static final int[][] posFaceletTransform = {
            {6, 3, 0, 7, 4, 1, 8, 5, 2}, // B +27
            {2, 5, 8, 1, 4, 7, 0, 3, 6}, // F +18
            {0, 1, 2, 3, 4, 5, 6, 7, 8}, // U +0
            {0, 1, 2, 3, 4, 5, 6, 7, 8}, // R +45
            {6, 3, 0, 7, 4, 1, 8, 5, 2}, // D +9
            {0, 1, 2, 3, 4, 5, 6, 7, 8}  // L +36
    };
    public static final int[] moveModes = {
            0, 0, 0, 0, 0, 0, // UDFBLR
            1, 1, 1,          // ESM
            3, 3, 3, 3, 3, 3, // XYZxyz
            2, 2, 2, 2, 2, 2  // udfblr
    };
    public static final int[] moveCodes = {
            0, 1, 2, 3, 4, 5, // UDFBLR
            1, 2, 4,          // ESM
            5, 2, 0, 5, 2, 0, // XYZxyz
            0, 1, 2, 3, 4, 5  // udfblr
    };
    public static final char[] modeChar = {'m', 't', 'c', 's', 'a'};
    // cube dimensions in number of facelets (mincol, maxcol, minrow, maxrow) for compact cube
    public static final int[][][] cubeBlocks = {
            {{0, 3}, {0, 3}}, // U
            {{0, 3}, {0, 3}}, // D
            {{0, 3}, {0, 3}}, // F
            {{0, 3}, {0, 3}}, // B
            {{0, 3}, {0, 3}}, // L
            {{0, 3}, {0, 3}}  // R
    };
    // all possible subcube dimensions for top and bottom layers
    public static final int[][][] topBlockTable = {
            {{0, 0}, {0, 0}},
            {{0, 3}, {0, 3}},
            {{0, 3}, {0, 1}},
            {{0, 1}, {0, 3}},
            {{0, 3}, {2, 3}},
            {{2, 3}, {0, 3}}
    };
    // subcube dimmensions for middle layers
    public static final int[][][] midBlockTable = {
            {{0, 0}, {0, 0}},
            {{0, 3}, {1, 2}},
            {{1, 2}, {0, 3}}
    };
    // indices to topBlockTable[] and botBlockTable[] for each twistedLayer value
    public static final int[][] topBlockFaceDim = {
            // U  D  F  B  L  R
            {1, 0, 3, 3, 2, 3}, // U
            {0, 1, 5, 5, 4, 5}, // D
            {2, 3, 1, 0, 3, 2}, // F
            {4, 5, 0, 1, 5, 4}, // B
            {3, 2, 2, 4, 1, 0}, // L
            {5, 4, 4, 2, 0, 1}  // R
    };
    public static final int[][] midBlockFaceDim = {
            // U  D  F  B  L  R
            {0, 0, 2, 2, 1, 2}, // U
            {0, 0, 2, 2, 1, 2}, // D
            {1, 2, 0, 0, 2, 1}, // F
            {1, 2, 0, 0, 2, 1}, // B
            {2, 1, 1, 1, 0, 0}, // L
            {2, 1, 1, 1, 0, 0}  // R
    };
    public static final int[][] botBlockFaceDim = {
            // U  D  F  B  L  R
            {0, 1, 5, 5, 4, 5}, // U
            {1, 0, 3, 3, 2, 3}, // D
            {4, 5, 0, 1, 5, 4}, // F
            {2, 3, 1, 0, 3, 2}, // B
            {5, 4, 4, 2, 0, 1}, // L
            {3, 2, 2, 4, 1, 0}  // R
    };
    // top facelet cycle
    public static final int[] cycleOrder = {0, 1, 2, 5, 8, 7, 6, 3};
    // side facelet cycle offsets
    public static final int[] cycleFactors = {1, 3, -1, -3, 1, 3, -1, -3};
    public static final int[] cycleOffsets = {0, 2, 8, 6, 3, 1, 5, 7};
    // indices for faces of layers
    public static final int[][] cycleLayerSides = {
            {3, 3, 3, 0}, // U: F=6-3k R=6-3k B=6-3k L=k
            {2, 1, 1, 1}, // D: L=8-k  B=2+3k R=2+3k F=2+3k
            {3, 3, 0, 0}, // F: L=6-3k D=6-3k R=k    U=k
            {2, 1, 1, 2}, // B: R=8-k  D=2+3k L=2+3k U=8-k
            {3, 2, 0, 0}, // L: U=6-3k B=8-k  D=k    F=k
            {2, 2, 0, 1}  // R: F=8-k  D=8-k  B=k    U=2+3k
    };
    // indices for sides of center layers
    public static final int[][] cycleCenters = {
            {7, 7, 7, 4}, // E'(U): F=7-3k R=7-3k B=7-3k L=3+k
            {6, 5, 5, 5}, // E (D): L=5-k  B=1+3k R=1+3k F=1+3k
            {7, 7, 4, 4}, // S (F): L=7-3k D=7-3k R=3+k  U=3+k
            {6, 5, 5, 6}, // S'(B): R=5-k  D=1+3k L=1+3k U=5-k
            {7, 6, 4, 4}, // M (L): U=7-3k B=8-k  D=3+k  F=3+k
            {6, 6, 4, 5}  // M'(R): F=5-k  D=5-k  B=3+k  U=1+3k
    };
    public static final int[][][] dragBlocks = {
            {{0, 0}, {3, 0}, {3, 1}, {0, 1}},
            {{3, 0}, {3, 3}, {2, 3}, {2, 0}},
            {{3, 3}, {0, 3}, {0, 2}, {3, 2}},
            {{0, 3}, {0, 0}, {1, 0}, {1, 3}},
            // center slices
            {{0, 1}, {3, 1}, {3, 2}, {0, 2}},
            {{2, 0}, {2, 3}, {1, 3}, {1, 0}}
    };
    public static final int[][] areaDirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 0}, {0, 1}};
    public static final int[][] twistDirs = {
            {1, 1, 1, 1, 1, -1}, // U
            {1, 1, 1, 1, 1, -1}, // D
            {1, -1, 1, -1, 1, 1}, // F
            {1, -1, 1, -1, -1, 1}, // B
            {-1, 1, -1, 1, -1, -1}, // L
            {1, -1, 1, -1, 1, 1}  // R
    };
    // various sign tables for computation of directions of rotations
    public static final int[][][] rotCos = {
            {{1, 0, 0}, {0, 0, 0}, {0, 0, 1}}, // U-D
            {{1, 0, 0}, {0, 1, 0}, {0, 0, 0}}, // F-B
            {{0, 0, 0}, {0, 1, 0}, {0, 0, 1}}  // L-R
    };
    public static final int[][][] rotSin = {
            {{0, 0, 1}, {0, 0, 0}, {-1, 0, 0}}, // U-D
            {{0, 1, 0}, {-1, 0, 0}, {0, 0, 0}}, // F-B
            {{0, 0, 0}, {0, 0, 1}, {0, -1, 0}}  // L-R
    };
    public static final int[][][] rotVec = {
            {{0, 0, 0}, {0, 1, 0}, {0, 0, 0}}, // U-D
            {{0, 0, 0}, {0, 0, 0}, {0, 0, 1}}, // F-B
            {{1, 0, 0}, {0, 0, 0}, {0, 0, 0}}  // L-R
    };
    public static final int[] rotSign = {1, -1, 1, -1, 1, -1}; // U, D, F, B, L, R
    public static final double[][] border = {{0.10, 0.10}, {0.90, 0.10}, {0.90, 0.90}, {0.10, 0.90}};
    public static final int[][] factors = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};

    public static final int[][] eyeOrder = {{1, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}, {1, 0, 2}};
    public static final int[][] blockMode = {{0, 2, 2}, {2, 1, 2}, {2, 2, 2}, {2, 2, 2}, {2, 2, 2}, {2, 2, 2}};
    public static final int[][] drawOrder = {{0, 1, 2}, {2, 1, 0}, {0, 2, 1}};

    public static final int ANIMATION_PLAY_FORWARD = 0; // play forward
    public static final int ANIMATION_PLAY_BACKWARD = 1; // play backward
    public static final int ANIMATION_STEP_FORWARD = 2; // step forward
    public static final int ANIMATION_STEP_BACKWARD = 3; // step backward
    public static final int ANIMATION_FAST_FORWARD = 4; // fast forward
    public static final int ANIMATION_FAST_BACKWARD = 5;  // fast backward

    public static final String DEFAULT_INITIAL_CUBE_ROTATION = "lluu";
    public static final String TOP_ALIGN = "top-align";
    public static final String CENTER_ALIGN = "center-align";
    public static final String BOTTOM_ALIGN = "bottom-align";

    /**
     * MAYBE WILL KEEP MAYBE NOT
     */
    public static final String[][][] turnSymbol = {
            { // "standard" notation
                    {"U", "D", "F", "B", "L", "R"},
                    {"Um", "Dm", "Fm", "Bm", "Lm", "Rm"},
                    {"Ut", "Dt", "Ft", "Bt", "Lt", "Rt"},
                    {"Uc", "Dc", "Fc", "Bc", "Lc", "Rc"},
                    {"Us", "Ds", "Fs", "Bs", "Ls", "Rs"},
                    {"Ua", "Da", "Fa", "Ba", "La", "Ra"}
            },
            { // "reduced" notation
                    {"U", "D", "F", "B", "L", "R"},
                    {"~E", "E", "S", "~S", "M", "~M"},
                    {"u", "d", "f", "b", "l", "r"},
                    {"Z", "~Z", "Y", "~Y", "~X", "X"},
                    {"Us", "Ds", "Fs", "Bs", "Ls", "Rs"},
                    {"Ua", "Da", "Fa", "Ba", "La", "Ra"}
            },
            { // "reduced" notation - swapped Y and Z
                    {"U", "D", "F", "B", "L", "R"},
                    {"~E", "E", "S", "~S", "M", "~M"},
                    {"u", "d", "f", "b", "l", "r"},
                    {"Y", "~Y", "Z", "~Z", "~X", "X"},
                    {"Us", "Ds", "Fs", "Bs", "Ls", "Rs"},
                    {"Ua", "Da", "Fa", "Ba", "La", "Ra"}
            },
            { // another reduced notation
                    {"U", "D", "F", "B", "L", "R"},
                    {"u", "d", "f", "b", "l", "r"},
                    {"Uu", "Dd", "Ff", "Bb", "Ll", "Rr"},
                    {"QU", "QD", "QF", "QB", "QL", "QR"},
                    {"UD'", "DU'", "FB'", "BF'", "LR'", "RL'"},
                    {"UD", "DU", "FB", "BF", "LR", "RL"}
            }
    };
    public static final String[] modifierStrings = {"", "2", "'", "2'"};
    private static final String[] metricChar = {"", "q", "f", "s"};
}
