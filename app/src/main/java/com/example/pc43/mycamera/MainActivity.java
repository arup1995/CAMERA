package com.example.pc43.mycamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.os.Environment.DIRECTORY_DCIM;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1;
    private static final int PICK_FROM_GALLERY = 2;
    int REQUEST_CODE = 1;
    Button mCapture;
    Button mGallery;
    ImageView mimageView;
    private int RESULT_LOAD_IMAGE = 2;
    private static final int REQUEST_FOR_CAMERA = 1003;
    private static final int REQUEST_FOR_EXTERNAL_STORAGE = 1001;
    private String mCurrentPhotoPath;
    private Uri file;
    private Bitmap bitmapImage;
    private int REQUEST_CAMERA = 0;


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

                requestPermissionForExternalStorage();
            }
        });
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int gallaryPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (gallaryPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
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
        switch (requestCode) {
            case REQUEST_FOR_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
//                    Toast.makeText(getApplicationContext(), "SMS Permission granted", Toast.LENGTH_LONG).show();
                    requestPermissionForCamera();
                } else {
//                    Toast.makeText(getApplicationContext(), "",Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case REQUEST_FOR_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
//                    Toast.makeText(getApplicationContext(), "SMS Permission granted", Toast.LENGTH_LONG).show();
                    cameraIntent();
                } else {
//                    Toast.makeText(getApplicationContext(), "",Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case PICK_FROM_GALLERY: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        callGallaryRequest();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }


        }
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
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            onCaptureImageResult(data);

        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            mimageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    public boolean requestPermissionForExternalStorage() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
// explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//Toast.makeText(getApplicationContext(), "External storage permission is mandatory",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_FOR_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_FOR_EXTERNAL_STORAGE);
            }
            return true;
        } else {
            requestPermissionForCamera();
            return false;
        }
    }

    public boolean requestPermissionForCamera() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
// explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {
//Toast.makeText(getApplicationContext(), "External storage permission is mandatory",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        REQUEST_FOR_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        REQUEST_FOR_CAMERA);
            }
            return true;
        } else {
            cameraIntent();
            return false;
        }
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //for nougat
        if (Build.VERSION.SDK_INT >= 24) {
            File nFile = getFile();
            file = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider", nFile);
        } else {
            //getting uri of the file
            file = Uri.fromFile(getFile());
        }
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, file);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "There is no Camera Application found", Toast.LENGTH_SHORT).show();
        }
    }

    private void onCaptureImageResult(Intent data) {
        try {
            bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), file);
            if (bitmapImage == null) {
                bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), file);
                //uploadImage(file);
            }
            mimageView.setImageBitmap(bitmapImage);
            uploadImage(bitmapImage, mCurrentPhotoPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getFile() {

        File folder = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM);// the file path

        //if it doesn't exist the folder will be created
        if (!folder.exists()) {
            folder.mkdir();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image_file = null;

        try {
            image_file = File.createTempFile(imageFileName, ".jpeg", folder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCurrentPhotoPath = image_file.getAbsolutePath();
        return image_file;
    }

    public void uploadImage(final Bitmap bitmapImage, String currentPhotoPath) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
        Bitmap compressedBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
       // Bitmap resizedBitmap = getResizedBitmap(compressedBitmap, compressedBitmap.getWidth() / 8, compressedBitmap.getHeight() / 8);
        byte imgBytes[] = out.toByteArray();
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("image/jpeg"), imgBytes);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", currentPhotoPath, requestFile);
       // mimageView.setImageBitmap(resizedBitmap);
    }

//    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
//        int width = bm.getWidth();
//        int height = bm.getHeight();
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        // CREATE A MATRIX FOR THE MANIPULATION
//        Matrix matrix = new Matrix();
//        // RESIZE THE BIT MAP
//        matrix.postScale(scaleWidth, scaleHeight);
//
//        // "RECREATE" THE NEW BITMAP
//        Bitmap resizedBitmap = Bitmap.createBitmap(
//                bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
//        return resizedBitmap;
//    }
}

