package com.example.pc43.mycamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1;
    private static final int PICK_FROM_GALLERY = 2;
    int REQUEST_CODE = 1;
    Button mCapture;
    Button mGallery;
    ImageView mimageView;
    private int RESULT_LOAD_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCapture = (Button) findViewById(R.id.btn_camera);
        mGallery = (Button) findViewById(R.id.btn_gallery);
        mimageView = (ImageView) findViewById(R.id.imageView);
        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permission
                if (Build.VERSION.SDK_INT >= 23) {
                    int cameraPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                    if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                    } else {
                        callCameraRequest();
                    }
                } else {
                    callCameraRequest();
                }
            }
        });
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int gallaryPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (gallaryPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                    } else {
                        callGallaryRequest();
                    }
                } else {
                    callGallaryRequest();
                }
            }

        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callCameraRequest();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (requestCode == PICK_FROM_GALLERY) {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        callGallaryRequest();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void callCameraRequest() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, REQUEST_CODE);
    }

    private void callGallaryRequest() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            mimageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        } else if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            mimageView.setImageBitmap(bmp);

        }
    }
}

