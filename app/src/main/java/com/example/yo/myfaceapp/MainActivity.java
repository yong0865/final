package com.example.yo.myfaceapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.*;
import android.view.*;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final static int PERMISSIONS_REQUEST_CODE = 100;
    private final static int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                int hasWriteExternalStoragePermission =
                        ContextCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (hasCameraPermission == PackageManager.PERMISSION_GRANTED && hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                }
            } else {
            }
        } else {
            Toast.makeText(MainActivity.this, "Camera not supported", Toast.LENGTH_LONG).show();
        }

    }

    public void btnstart(View view) {
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }


}
