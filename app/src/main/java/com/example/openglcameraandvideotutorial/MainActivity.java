package com.example.openglcameraandvideotutorial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.example.openglcameraandvideotutorial.cameraX.CameraActivity;
import com.example.openglcameraandvideotutorial.databinding.ActivityMainBinding;
import com.example.openglcameraandvideotutorial.decode_mp4.DecodeMp4Activity;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initListeners();
    }

    private void initListeners() {
        binding.btnCamera.setOnClickListener(v -> {
         goToCameraPage();
        });

        binding.btnDecodeMp4.setOnClickListener(v -> {
            goToMp4DecodePage();
        });
    }

    private void goToMp4DecodePage() {
        startActivity(new Intent(this, DecodeMp4Activity.class));
    }

    private void goToCameraPage() {
        if (hasCameraPermission()) {
            startActivity(new Intent(this, CameraActivity.class));
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(  this,    CAMERA_PERMISSION,  CAMERA_REQUEST_CODE );
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(  this, Manifest.permission.CAMERA ) == PackageManager.PERMISSION_GRANTED;
    }

}