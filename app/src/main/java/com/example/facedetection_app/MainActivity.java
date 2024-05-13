package com.example.facedetection_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageView chosenImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        chosenImage = findViewById(R.id.chosenImage);
        MaterialButton choseImageBtn = findViewById(R.id.choseImageBtn);
        choseImageBtn.setOnClickListener(view ->{

            openGallery();
        });

    }


    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                chosenImage.setImageBitmap(bitmap);
                //detectFace(bitmap);
                detectObjects(bitmap);
                // Now you have the bitmap, you can use it for face detection or display it in an ImageView
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            openGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "You Need to give Storage permission to Open Gallery!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void detectObjects(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionObjectDetectorOptions options =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                        .enableClassification()  // Optional: Enable object classification
                        .build();

        FirebaseVisionObjectDetector detector = FirebaseVision.getInstance()
                .getOnDeviceObjectDetector(options);

        detector.processImage(image)
                .addOnSuccessListener(objects -> {
                    // Task completed successfully
                    // Process the detected objects
                    for (FirebaseVisionObject object : objects) {
                        Rect bounds = object.getBoundingBox();  // Get the bounding box of the object
                        int id = object.getTrackingId();         // Get the tracking ID of the object (if available)
                        int category = object.getClassificationCategory(); // Get the category of the detected object (if classification is enabled)

                        Log.d("MyApp","image bound"+bounds);
                        // Process the detected object
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("MyApp","Error "+e.getMessage());
                    // Task failed with an exception
                    // Handle the error
                });
    }

    private void detectFace(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .build();
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        detector.detectInImage(image)
                .addOnSuccessListener(faces -> {
                    for (FirebaseVisionFace face : faces){
                        Log.d("MyApp","faces "+face.toString());
                    }

                }).addOnFailureListener(e -> {
                    Log.e("MyApp","Error "+e.getMessage());
                });

    }
}