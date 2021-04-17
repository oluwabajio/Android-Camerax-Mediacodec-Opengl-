package com.example.openglcameraandvideotutorial.cameraX;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.UseCase;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Toast;

import com.example.openglcameraandvideotutorial.databinding.ActivityCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    ActivityCameraBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Bitmap bitmap;
    private static final String TAG = "CameraActivity";
    CameraRenderer cameraRenderer;
    ImageAnalysis.Analyzer imageAnalyzer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //This is where we set our cameraX properties
        //Basically we create an image Analyzer that fetches us the c
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraRenderer = new CameraRenderer(this, binding);
        imageAnalyzer = cameraRenderer;




        initListners();
        initGLSurfaceVew();
        startCamera();


    }

    private void initGLSurfaceVew() {
        // Request an OpenGL ES 2.0 compatible context.
        binding.glSurfaceView.setEGLContextClientVersion(2);

        // Set the renderer to our demo renderer, defined below.
        binding.glSurfaceView.setRenderer(cameraRenderer);
        binding.glSurfaceView.setPreserveEGLContextOnPause(true);
        binding.glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    private void startCamera() {

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // Select back camera as a default
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                    // Used to bind the lifecycle of cameras to the lifecycle owner
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(((LifecycleOwner) CameraActivity.this), cameraSelector, getImageAnalysis());


                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, ContextCompat.getMainExecutor(this));


    }

    private UseCase getImageAnalysis() {
        ImageAnalysis imageAnalysis = null;

        imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(640, 640)).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageAnalyzer);
        return imageAnalysis;

    }


    private Bitmap getBitmapp(ImageProxy mImage) {


        ByteArrayOutputStream outputbytes = new ByteArrayOutputStream();

        ByteBuffer bufferY = mImage.getPlanes()[0].getBuffer();
        byte[] data0 = new byte[bufferY.remaining()];
        bufferY.get(data0);

        ByteBuffer bufferU = mImage.getPlanes()[1].getBuffer();
        byte[] data1 = new byte[bufferU.remaining()];
        bufferU.get(data1);

        ByteBuffer bufferV = mImage.getPlanes()[2].getBuffer();
        byte[] data2 = new byte[bufferV.remaining()];
        bufferV.get(data2);

        try {
            outputbytes.write(data0);
            outputbytes.write(data2);
            outputbytes.write(data1);


            final YuvImage yuvImage = new YuvImage(outputbytes.toByteArray(), ImageFormat.NV21, mImage.getWidth(), mImage.getHeight(), null);
            ByteArrayOutputStream outBitmap = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 50, outBitmap);
            byte[] imageBytes = outBitmap.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            mImage.close();
        }
        return null;
    }


    private Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int vuSize = uBuffer.remaining();

        byte[] nv21 = new byte[ySize + vuSize];

        yBuffer.get(nv21, 0, ySize);
        uBuffer.get(nv21, ySize, vuSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 50, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private Bitmap getBitmapFromImage(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }


    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        binding.glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        binding.glSurfaceView.onPause();
    }

    private void initListners() {
    }


}