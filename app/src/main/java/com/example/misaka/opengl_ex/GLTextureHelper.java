package com.example.misaka.opengl_ex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.widget.Toast;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;

public class GLTextureHelper {
    private static final String TAG = "TextureHelper";

    // Load texture from resourceId
    public static int loadTexture(Context context, int resourceId) {

        // Create texture object
        final int[] textureIds = new int[1];
        glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.w(TAG, "Could not generate a new OpenGL texture object.");
            Toast.makeText(context, "Could not generate a new OpenGL texture object.", Toast.LENGTH_SHORT).show();
            return 0;
        }
        // Get bitmap
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        if (bitmap == null) {
            Log.w(TAG, "Resource ID " + resourceId + " could not be decoded.");
            Toast.makeText(context, "Resource ID " + resourceId + " could not be decoded.", Toast.LENGTH_SHORT).show();
            glDeleteTextures(1, textureIds, 0);
            return 0;
        }

        // Setting texture object
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();

        glBindTexture(GL_TEXTURE_2D, 0);
        return textureIds[0];
    }

    // Load texture from bitmap
    public static int loadTexture(Context context,  Bitmap bitmap) {

        // Create texture object
        final int[] textureIds = new int[1];
        glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.w(TAG, "Could not generate a new OpenGL texture object.");
            Toast.makeText(context, "Could not generate a new OpenGL texture object.", Toast.LENGTH_SHORT).show();
            return 0;
        }

        // Setting texture object
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();

        glBindTexture(GL_TEXTURE_2D, 0);
        return textureIds[0];
    }
}
