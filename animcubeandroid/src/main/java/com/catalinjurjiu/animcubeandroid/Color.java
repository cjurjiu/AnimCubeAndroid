package com.catalinjurjiu.animcubeandroid;

import android.support.annotation.ColorInt;

/**
 * Created by Catalin on 4/25/2015.
 */
public class Color {
    public int colorCode;
    public int red;
    public int green;
    public int blue;

    public Color(int colorCode) {
        this.colorCode = colorCode;
        this.red = android.graphics.Color.red(colorCode);
        this.green = android.graphics.Color.green(colorCode);
        this.blue = android.graphics.Color.blue(colorCode);
    }

    public Color(int red, int green, int blue) {
        this.colorCode = android.graphics.Color.rgb(red, green, blue);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @ColorInt
    public int darker() {
        return android.graphics.Color.rgb((int) (red * 0.6), (int) (green * 0.6), (int) (blue * 0.6));
    }
}
