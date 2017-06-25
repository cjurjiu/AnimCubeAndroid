package com.catalinjurjiu.animcubeandroid;

/**
 * Created by catalin on 05.06.2017.
 */

public class CubeUtils {

    // cube colors
    public static final Color[] colors = {
            new Color(255, 128, 64),   // 0 - light orange
            new Color(255, 0, 0),      // 1 - pure red
            new Color(0, 255, 0),      // 2 - pure green
            new Color(0, 0, 255),      // 3 - pure blue
            new Color(153, 153, 153),  // 4 - white grey
            new Color(170, 170, 68),   // 5 - yellow grey
            new Color(187, 119, 68),   // 6 - orange grey
            new Color(153, 68, 68),    // 7 - red grey
            new Color(68, 119, 68),    // 8 - green grey
            new Color(0, 68, 119),     // 9 - blue grey
            new Color(255, 255, 255), // W - white
            new Color(255, 255, 0),  // Y - yellow
            new Color(255, 96, 32),   // O - orange
            new Color(208, 0, 0),     // R - red
            new Color(0, 144, 0),     // G - green
            new Color(32, 64, 208),   // B - blue
            new Color(176, 176, 176), // L - light gray
            new Color(80, 80, 80),    // D - dark gray
            new Color(255, 0, 255),   // M - magenta
            new Color(0, 255, 255),   // C - cyan
            new Color(255, 160, 192), // P - pink
            new Color(32, 255, 16),   // N - light green
            new Color(0, 0, 0),       // K - black
            new Color(128, 128, 128) // . - gray
    };
    public static final int[][] eyeOrder = {{1, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}, {1, 0, 2}};
    public static final int[][] blockMode = {{0, 2, 2}, {2, 1, 2}, {2, 2, 2}, {2, 2, 2}, {2, 2, 2}, {2, 2, 2}};
    public static final int[][] drawOrder = {{0, 1, 2}, {2, 1, 0}, {0, 2, 1}};

    public static int realMoveLength(int[] move) {
        int length = 0;
        for (int i = 0; i < move.length; i++) {
            if (move[i] < 1000) {
                length++;
            }
        }
        return length;
    }

    public static int arrayMovePos(int[] move, int realPos) {
        int pos = 0;
        int rpos = 0;
        while (true) {
            while (pos < move.length && move[pos] >= 1000)
                pos++;
            if (rpos == realPos)
                break;
            if (pos < move.length) {
                rpos++;
                pos++;
            }
        }
        return pos;
    }

    public static double[] vCopy(double[] vector, double[] srcVec) {
        vector[0] = srcVec[0];
        vector[1] = srcVec[1];
        vector[2] = srcVec[2];
        return vector;
    }

    public static double[] vNorm(double[] vector) {
        double length = Math.sqrt(vProd(vector, vector));
        vector[0] /= length;
        vector[1] /= length;
        vector[2] /= length;
        return vector;
    }

    public static double[] vScale(double[] vector, double value) {
        vector[0] *= value;
        vector[1] *= value;
        vector[2] *= value;
        return vector;
    }

    public static double vProd(double[] vec1, double[] vec2) {
        return vec1[0] * vec2[0] + vec1[1] * vec2[1] + vec1[2] * vec2[2];
    }

    public static double[] vAdd(double[] vector, double[] srcVec) {
        vector[0] += srcVec[0];
        vector[1] += srcVec[1];
        vector[2] += srcVec[2];
        return vector;
    }

    public static double[] vSub(double[] vector, double[] srcVec) {
        vector[0] -= srcVec[0];
        vector[1] -= srcVec[1];
        vector[2] -= srcVec[2];
        return vector;
    }

    public static double[] vMul(double[] vector, double[] vec1, double[] vec2) {
        vector[0] = vec1[1] * vec2[2] - vec1[2] * vec2[1];
        vector[1] = vec1[2] * vec2[0] - vec1[0] * vec2[2];
        vector[2] = vec1[0] * vec2[1] - vec1[1] * vec2[0];
        return vector;
    }

    public static double[] vRotX(double[] vector, double angle) {
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        double y = vector[1] * cosA - vector[2] * sinA;
        double z = vector[1] * sinA + vector[2] * cosA;
        vector[1] = y;
        vector[2] = z;
        return vector;
    }

    public static double[] vRotY(double[] vector, double angle) {
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        double x = vector[0] * cosA - vector[2] * sinA;
        double z = vector[0] * sinA + vector[2] * cosA;
        vector[0] = x;
        vector[2] = z;
        return vector;
    }

    public static double[] vRotZ(double[] vector, double angle) {
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        double x = vector[0] * cosA - vector[1] * sinA;
        double y = vector[0] * sinA + vector[1] * cosA;
        vector[0] = x;
        vector[1] = y;
        return vector;
    }
}
