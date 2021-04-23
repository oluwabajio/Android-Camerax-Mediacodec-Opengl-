package com.example.openglcameraandvideotutorial.decodeMp4Grafika;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.widget.ImageView;

import com.example.openglcameraandvideotutorial.databinding.ActivityDecodeMp4Binding;
import com.example.openglcameraandvideotutorial.decode_mp4.IVCGLLib;
import com.example.openglcameraandvideotutorial.decode_mp4.MyRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Mp4MultiSurfaceRenderer   implements GLSurfaceView.Renderer {
    private static final String TAG = "MyRenderer";
    private int textureId;
    SurfaceTexture mSurfaceTexture;

    private String vertShader;
    private String fragShader_Pre;
    private int programHandle;
    private int mPositionHandle;
    private int mTextureCoordHandle;
    int screenWidth, screenHeight;
    Activity context;
    ActivityDecodeMp4Binding binding;


    FloatBuffer verticesBuffer, textureVerticesPreviewBuffer;
    private ShortBuffer drawListBuffer;

    // number of coordinates per vertex in this array
    private final int COORDS_PER_VERTEX = 2;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private final float squareVertices[] = { // in counterclockwise order:
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f
    };
    private final float textureVerticesPreview[] = { // in counterclockwise order:
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
    };
    private final short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices


    public Mp4MultiSurfaceRenderer(Activity context) {
        this.context = context;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        initTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.screenHeight = height;
        this.screenWidth = width;
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        if (true) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            mSurfaceTexture.updateTexImage();
            draw();
        }

    }


    public SurfaceTexture createSurfaceTexture() {
        textureId = createVideoTexture();
        mSurfaceTexture = new SurfaceTexture(textureId);
        return mSurfaceTexture;
    }

    public int getTextureId() {
        return textureId;
    }


    private int createVideoTexture() {
        int[] texture = new int[2];

        GLES20.glGenTextures(2, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    void initTexture() {
        verticesBuffer = IVCGLLib.glToFloatBuffer(squareVertices);
        textureVerticesPreviewBuffer = IVCGLLib
                .glToFloatBuffer(textureVerticesPreview);
        drawListBuffer = IVCGLLib.glToShortBuffer(drawOrder);

        vertShader = IVCGLLib.loadFromAssetsFile(
                "IVC_VShader_Preview.sh", context.getResources());
        fragShader_Pre = IVCGLLib.loadFromAssetsFile(
                "IVC_FShader_Preview_Deform.sh", context.getResources());

        programHandle = IVCGLLib.glCreateProgram(vertShader, fragShader_Pre);

        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "position");
        mTextureCoordHandle = GLES20.glGetAttribLocation(programHandle, "inputTextureCoordinate");


    }

    void draw() {


        GLES20.glUseProgram(programHandle);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, verticesBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesPreviewBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programHandle, "sampler2d"), 0);

        IVCGLLib.glUseFBO(0, 0, screenWidth, screenHeight, false, 0, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        IVCGLLib.glCheckGlError("glDrawElements");

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);


    }


    public Bitmap createBitmapFromGLSurface(int x, int y, int w, int h) {
        w = screenWidth;
        h = screenHeight;


        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        try {


            GLES20.glReadPixels(x, y, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            e.printStackTrace();
            return null;
        }
        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }


    public Bitmap createBitmapFromGLSurface_(int x, int y, int w, int h) {
        w = screenWidth;
        h = screenHeight;

        ByteBuffer mPixelBuf;
        mPixelBuf = ByteBuffer.allocateDirect(w * h * 4);
        mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);


        mPixelBuf.rewind();
        GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                mPixelBuf);


        try {
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mPixelBuf.rewind();
            bmp.copyPixelsFromBuffer(mPixelBuf);
            return bmp;
        } catch (Exception ex) {
            Log.e(TAG, "createBitmapFromGLSurface_: error: " + ex.getMessage());
            return null;
        }


    }

    void release() {
        Log.d(TAG, "deleting program " + programHandle);
        GLES20.glDeleteProgram(programHandle);
        programHandle = -1;

        Log.d(TAG, "releasing SurfaceTexture");
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }


}
