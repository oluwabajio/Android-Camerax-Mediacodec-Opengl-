package com.example.openglcameraandvideotutorial.cameraX;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.openglcameraandvideotutorial.utils.AppUtils;
import com.example.openglcameraandvideotutorial.databinding.ActivityCameraBinding;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRenderer implements GLSurfaceView.Renderer, ImageAnalysis.Analyzer {
    Activity activity;
    ActivityCameraBinding binding;
    Bitmap image;
    ImageFilter imageFilter;
    private int[] textures = new int[2];
    EffectContext effectContext;
    private Effect effect;

    public CameraRenderer(Activity activity, ActivityCameraBinding binding) {
        this.activity = activity;
        this.binding = binding;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0f, 0f, 0f, 1f); // Set the background frame color
        if (imageFilter == null) {
            imageFilter = new ImageFilter();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        if (imageFilter == null) {
            imageFilter = new ImageFilter();
        }

        generateTexture();

        if (effectContext == null) {
            effectContext = EffectContext.createWithCurrentGlContext();
        }

        if (effect !=null) {
            effect.release();
        }

        Bitmap image = this.image;
        EffectContext effectContext = this.effectContext;
        if (image != null && effectContext != null) {
            EffectFactory factory = effectContext.getFactory();
            effect = factory.createEffect(EffectFactory.EFFECT_DOCUMENTARY);
            effect.apply(textures[0], image.getWidth(), image.getHeight(), textures[1]);

        }

        imageFilter.draw(textures[0]);
//        image.recycle();
    //    imageFilter.draw(textures[1]); //Texture with the effect

    }


    private void generateTexture() {

        try {


            // Generate textures
            GLES20.glGenTextures(2, textures, 0);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]); // Upload to texture
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);

            // Set texture parameters
            setTextureParameters();
        } catch (Exception ex) {
            Log.e("TAG", "generateTexture: error = "+ ex.getMessage() );
        }

    }

    private void setTextureParameters() {
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
        );
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
        );

    }


    @Override
    public void analyze(@NonNull ImageProxy image) {
        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        AppUtils utils = new AppUtils();
        Bitmap bi2 = utils.toBitmap(image);
        Bitmap bi = Bitmap.createBitmap(bi2, 0, 0, bi2.getWidth(), bi2.getHeight(), matrix, true);

        // Bitmap bi = toBitmap(image.getImage());
        // Bitmap bi = getBitmapp(image);
        // Bitmap bi = getBitmapFromImage(image.getImage());
        this.image = bi;
        image.close();


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.imageView.setImageBitmap(bi);
                Log.e("TAG", "run: got here");


            }
        });
        binding.glSurfaceView.requestRender();
    }
}
