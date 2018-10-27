package com.example.misaka.opengl_ex;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;

public class ResizeUtils {

    public void ResizeView(View view, float imageHeight, float imageWidth, Context context){
        float dpiImageHeight = convertPixelsToDp(imageHeight, context);
        float dpiImageWidth = convertPixelsToDp(imageWidth, context);

        float coeff = dpiImageHeight / dpiImageWidth;

        /*while()

        if(dpiImageHeight > view.getHeight() || dpiImageWidth > view.getWidth()) {
            float coeff = dpiImageHeight / dpiImageWidth;
            dpiImageWidth = view.getWidth();

        }*/
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
