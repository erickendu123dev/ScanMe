package com.example.scanme;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.DecimalFormat;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;

public class InputFromCameraActivity extends AppCompatActivity {

    CardView cvtakephoto;
    private static final int CAMERA_PERMISSION_CODE = 1234;
    Uri imageuri;
    TextView resultrecognized, inputrecognized;
    ImageView ivcapture;
    ImageButton backbtn;

    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_from_camera);

        openCamera();

        inputrecognized = findViewById(R.id.inputrec);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        resultrecognized = findViewById(R.id.resultrec);

        backbtn = findViewById(R.id.btnback);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InputFromCameraActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ivcapture = findViewById(R.id.ivcapture);
        cvtakephoto = findViewById(R.id.cvtakephoto);
        cvtakephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED){
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, CAMERA_PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
            }
        });
    }
    private void openCamera(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                    String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permission, CAMERA_PERMISSION_CODE);
                } else {
                    openCamera2();
                }
        } else {
                openCamera2();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }
                else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void openCamera2(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "new image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "from camera");
        imageuri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        activityResultLauncher.launch(intent);
    }

    private void recognizedImage() {
        try {
            InputImage inputImage = InputImage.fromFilePath(this, imageuri);

            Task<Text> textTask = textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            String recognizedResult = text.getText();
                            inputrecognized.setText("Input : " + recognizedResult);
                            Calculable calc = null;
                            try {
                                calc = new ExpressionBuilder(recognizedResult).build();
                                double result1 = calc.calculate();
                                String result2 = formatNumberCurrency(String.valueOf(result1));
                                resultrecognized.setText("Result : " + result2);
                                MyDatabaseHelper myDB = new MyDatabaseHelper(InputFromCameraActivity.this);
                                myDB.addResult(recognizedResult.trim(),
                                        result2.trim());
                            } catch (Exception e) {
                                e.printStackTrace();

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(InputFromCameraActivity.this,"" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        } catch (Exception e){
            Toast.makeText(InputFromCameraActivity.this,"Gagal membaca gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                     ivcapture.setImageURI(imageuri);
                     recognizedImage();
                    }
                    else {
                        Toast.makeText(InputFromCameraActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private static String formatNumberCurrency(String numberr) {
        DecimalFormat formatter = new DecimalFormat("###");
        return formatter.format(Double.parseDouble(numberr));
    }
}