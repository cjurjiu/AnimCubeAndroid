package com.catalinjurjiu.animcubeandroid;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * Various utils used primarily when rendering the cube (with few exceptions).
 * Created by catalin on 05.06.2017.
 */
class CubeUtils {

    private CubeUtils() {
        //private c-tor to prevent instantiation
    }

    static int darkerColor(@ColorInt int colorInt) {
        return Color.rgb((int) (Color.red(colorInt) * 0.6), (int) (Color.green(colorInt) * 0.6), (int) (Color.blue(colorInt) * 0.6));
    }

    static double[] vCopy(double[] vector, double[] srcVec) {
        vector[0] = srcVec[0];
        vector[1] = srcVec[1];
        vector[2] = srcVec[2];
        return vector;
    }

    static double[] vNorm(double[] vector) {
        double length = Math.sqrt(vProd(vector, vector));
        vector[0] /= length;
        vector[1] /= length;
        vector[2] /= length;
        return vector;
    }

    static double[] vScale(double[] vector, double value) {
        vector[0] *= value;
        vector[1] *= value;
        vector[2] *= value;
        return vector;
    }

    static double vProd(double[] vec1, double[] vec2) {
        return vec1[0] * vec2[0] + vec1[1] * vec2[1] + vec1[2] * vec2[2];
    }

    static double[] vAdd(double[] vector, double[] srcVec) {
        vector[0] += srcVec[0];
        vector[1] += srcVec[1];
        vector[2] += srcVec[2];
        return vector;
    }

    static double[] vSub(double[] vector, double[] srcVec) {
        vector[0] -= srcVec[0];
        vector[1] -= srcVec[1];
        vector[2] -= srcVec[2];
        return vector;
    }

    static double[] vMul(double[] vector, double[] vec1, double[] vec2) {
        vector[0] = vec1[1] * vec2[2] - vec1[2] * vec2[1];
        vector[1] = vec1[2] * vec2[0] - vec1[0] * vec2[2];
        vector[2] = vec1[0] * vec2[1] - vec1[1] * vec2[0];
        return vector;
    }

    static double[] vRotX(double[] vector, double angle) {
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        double y = vector[1] * cosA - vector[2] * sinA;
        double z = vector[1] * sinA + vector[2] * cosA;
        vector[1] = y;
        vector[2] = z;
        return vector;
    }

    static double[] vRotY(double[] vector, double angle) {
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        double x = vector[0] * cosA - vector[2] * sinA;
        double z = vector[0] * sinA + vector[2] * cosA;
        vector[0] = x;
        vector[2] = z;
        return vector;
    }

    static double[] vRotZ(double[] vector, double angle) {
        double sinA = Math.sin(angle);
        double cosA = Math.cos(angle);
        double x = vector[0] * cosA - vector[1] * sinA;
        double y = vector[0] * sinA + vector[1] * cosA;
        vector[0] = x;
        vector[1] = y;
        return vector;
    }

    static void deepCopy2DArray(int[][] src, int[][] dest) {
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
    }
}
