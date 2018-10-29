package com.example.misaka.opengl_ex;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLSurfaceView.Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class GLRenderer implements Renderer {

    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT) * 4;

    private Context context;
    private FloatBuffer vertexData;

    private int aPositionLocation;
    private int aTextureLocation;
    private int uTextureUnitLocation;

    private int programId;

    private int[] texture = new int[2];
    private Bitmap bitmap;
    private int mImageWidth;
    private int mImageHeight;

    private int width;
    private int height;

    private List<Filter> filters = new ArrayList<>();

    private EffectFactory effectFactory;
    private List<Effect> mEffect = new ArrayList<>();

    void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    GLRenderer(Context context) {
        this.context = context;
    }

    void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        createAndUseProgram(R.raw.texture_vertex_shader, R.raw.texture_fragment_shader);
        getLocations();
        prepareData();
        bindData();

        EffectContext mEffectContext = EffectContext.createWithCurrentGlContext();
        effectFactory = mEffectContext.getFactory();

        bitmap.recycle();
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        this.width = width;
        this.height = height;
    }

    private void prepareData() {
        float[] vertices = {
                -1,  1, 1,   0, 0,
                -1, -1, 1,   0, 1,
                1,  1, 1,   1, 0,
                1, -1, 1,   1, 1,
        };

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);

        texture = GLTextureHelper.loadTexture(context, bitmap);
        mImageHeight = bitmap.getHeight();
        mImageWidth  = bitmap.getWidth();
        bitmap.recycle();
    }

    private void createAndUseProgram(int vertex_sh, int fragment_sh) {
        int vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, vertex_sh);
        int fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, fragment_sh);
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        glUseProgram(programId);
    }

    private void getLocations() {
        aPositionLocation = glGetAttribLocation(programId, "a_Position");
        aTextureLocation = glGetAttribLocation(programId, "a_Texture");
        uTextureUnitLocation = glGetUniformLocation(programId, "u_TextureUnit");
    }

    private void bindData() {
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        vertexData.position(POSITION_COUNT);
        glVertexAttribPointer(aTextureLocation, TEXTURE_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aTextureLocation);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture[0]);
        glUniform1i(uTextureUnitLocation, 0);
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        glClearColor(0f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);

        initEffect();
        applyEffect();

        glViewport(0, 0, width, height);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture[1]);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    private void initEffect() {
        for (int i = 0; i < filters.size(); i++) {
           /* if (mEffect == null) {
                mEffect.get(i).release();
            }*/
            switch (filters.get(i).getName()) {
                case "af":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_AUTOFIX));
                    mEffect.get(i).setParameter("scale",0.5f);
                    break;
                case "bw":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_BLACKWHITE));
                    mEffect.get(i).setParameter("black", filters.get(i).getParams().get(0));
                    mEffect.get(i).setParameter("white", filters.get(i).getParams().get(1));
                    break;
                case "br":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_BRIGHTNESS));
                    mEffect.get(i).setParameter("brightness", filters.get(i).getParams().get(0));
                    break;
                case "ng":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_NEGATIVE));
                    break;
                case "rt":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_ROTATE));
                    mEffect.get(i).setParameter("angle",filters.get(i).getParams().get(0));
                    break;
                case "vg":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_VIGNETTE));
                    mEffect.get(i).setParameter("scale",filters.get(i).getParams().get(0));
                    break;
                case "ct":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_CONTRAST));
                    mEffect.get(i).setParameter("contrast",filters.get(i).getParams().get(0));
                    break;
                case "cp":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_CROSSPROCESS));
                    break;
                case "dt":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_DOCUMENTARY));
                    break;
                case "fl":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_FILLLIGHT));
                    mEffect.get(i).setParameter("strength",filters.get(i).getParams().get(0));
                    break;
                case "fe":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_FISHEYE));
                    mEffect.get(i).setParameter("scale",filters.get(i).getParams().get(0));
                    break;
                case "gs":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_GRAYSCALE));
                    break;
                case "pr":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_POSTERIZE));
                    break;
                case "sa":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_SEPIA));
                    break;
                case "tr":
                    mEffect.add(effectFactory.createEffect(
                            EffectFactory.EFFECT_TEMPERATURE));
                    mEffect.get(i).setParameter("scale", filters.get(i).getParams().get(0));
                    break;
                default:
                    break;
            }
        }

        // Rotate image to normal position
        mEffect.add(effectFactory.createEffect(
                EffectFactory.EFFECT_FLIP));
        mEffect.get(mEffect.size() - 1).setParameter("vertical", true);
    }
                /*// region
            case R.id.duotone:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_DUOTONE);
                mEffect.setParameter("first_color", Color.YELLOW);
                mEffect.setParameter("second_color", Color.DKGRAY);
                break;
            case R.id.flipvert:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FLIP);
                mEffect.setParameter("vertical", true);
                break;
            case R.id.fliphor:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FLIP);
                mEffect.setParameter("horizontal", true);
                break;
            case R.id.grain:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_GRAIN);
                mEffect.setParameter("strength", 1.0f);
                break;

            case R.id.lomoish:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_LOMOISH);
                break;

            case R.id.rotate:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_ROTATE);
                mEffect.setParameter("angle", 180);
                break;
            case R.id.saturate:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SATURATE);
                mEffect.setParameter("scale", .5f);
                break;
            case R.id.sharpen:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SHARPEN);
                break;
            case R.id.tint:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_TINT);
                mEffect.setParameter("tint", Color.MAGENTA);
                break;
            case R.id.vignette:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_VIGNETTE);
                mEffect.setParameter("scale", .5f);
                break;
           // endregion*/

    private void applyEffect() {
        int mEffectCount = mEffect.size();
        if (mEffectCount > 0) {
            mEffect.get(0).apply(texture[0], mImageWidth, mImageHeight, texture[1]);
            for (int i = 1; i < mEffectCount; i++) {
                int sourceTexture = texture[1];
                int destinationTexture = texture[2];
                mEffect.get(i).apply(sourceTexture, mImageWidth, mImageHeight, destinationTexture);
                texture[1] = destinationTexture;
                texture[2] = sourceTexture;
            }
        }
        mEffect.clear();
    }
}
