package com.example.misaka.opengl_ex;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ResizeUtils {
//    public void resizeView(RelativeLayout relativeLayout ,float imageHeight, float imageWidth, Context context){
//        DisplayMetrics screenDpi = context.getResources().getDisplayMetrics();
//
//        float dpiImageHeight = convertPixelsToDp(imageHeight, context);
//        float dpiImageWidth = convertPixelsToDp(imageWidth, context);
//        float resizeCoeff = dpiImageHeight / dpiImageWidth;
//
//        float viewW = screenDpi.densityDpi;
//        float viewH = resizeCoeff * screenDpi.densityDpi;
//
//        relativeLayout.getLayoutParams().height = (int)convertDpToPixel(viewH, context);
//    }

    // Set height and width for OpenGL layout, depending on image parameters
    void resizeView(RelativeLayout relativeLayout, float imageHeight, float imageWidth, Context context){
        float displayH = context.getResources().getDisplayMetrics().heightPixels;
        float displayW = context.getResources().getDisplayMetrics().widthPixels;

        // OpenGL layout should not occupy more than 80% of the screen
        float maximumH = displayH * 0.8f;
        float coeff;

//        if(imageHeight > imageWidth) coeff = imageHeight / imageWidth;
//        else coeff = imageWidth / imageHeight;

        coeff = imageHeight / imageWidth;

        float viewW = displayW;
        float viewH = coeff * displayW;
        if(viewH > maximumH){
            viewH = maximumH;
            if(imageHeight == imageWidth) viewW = viewH;
            else viewW = viewH / coeff;
        }

        relativeLayout.getLayoutParams().width  = (int)viewW;
        relativeLayout.getLayoutParams().height = (int)viewH;
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}