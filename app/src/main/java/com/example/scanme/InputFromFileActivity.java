package com.example.scanme;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

public class InputFromFileActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 1234;
    Uri imageuri;
    TextView resultrecognized, inputrecognized;
    ImageView ivfile;
    ImageButton backbtn;
    CardView btnchoosefile;


    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_from_file);

        backbtn = findViewById(R.id.btnback);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InputFromFileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        inputrecognized = findViewById(R.id.inputrec);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        resultrecognized = findViewById(R.id.resultrec);

        ivfile = findViewById(R.id.ivfile);

        openGallery();

        btnchoosefile = findViewById(R.id.cvchoosefile);
        btnchoosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
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
                                MyDatabaseHelper myDB = new MyDatabaseHelper(InputFromFileActivity.this);
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

                            Toast.makeText(InputFromFileActivity.this,"" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        } catch (Exception e){
            Toast.makeText(InputFromFileActivity.this,"Gagal membaca gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permission, STORAGE_PERMISSION_CODE);
            } else {
                openGallery2();
            }
        } else {
            openGallery2();
        }
    }

    private void openGallery2() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        activityResultLauncher.launch(galleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
            } else {
                Toast.makeText(this, "Perizinan Di Tolak", Toast.LENGTH_SHORT);
            }
        }
    }

   private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        imageuri = result.getData().getData();
                        ivfile.setImageURI(imageuri);
                        recognizedImage();
                    }
                    else {
                        Toast.makeText(InputFromFileActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private static String formatNumberCurrency(String numberr) {
        DecimalFormat formatter = new DecimalFormat("###");
        return formatter.format(Double.parseDouble(numberr));
    }
}