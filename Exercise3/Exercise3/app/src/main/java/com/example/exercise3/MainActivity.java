package com.example.exercise3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.exercise3.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button camera;
    Button gallery;
    TextView resultText;
    int imageSize = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.cameraButton);
        gallery = findViewById(R.id.galleryButton);
        resultText = findViewById(R.id.resultText);
        imageView = findViewById(R.id.imageView);


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){ //for camera
            if (requestCode == 3){ // for the retrieve result
                // Extract Image from Intent
                Bitmap image = (Bitmap) data.getExtras().get("data");
                // Get Dimensions
                int dimension = Math.min(image.getWidth(), image.getHeight());
                // Resize
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);


            } else {
                Uri imageUrl = data.getData();
                Bitmap image = null;

                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUrl);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                imageView.setImageBitmap(image);
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void classifyImage (Bitmap image){
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int [imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];

                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f /1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f /1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f /1));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);



            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;

            for (int i = 0; i < confidences.length; i++){
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }

            }

            String[] classes = {"Apple", "Banana", "Orange"};
            resultText.setText(classes[maxPos]);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

}