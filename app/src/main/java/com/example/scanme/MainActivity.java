package com.example.scanme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    CardView cvinput, cvfromfile, cvfromcamera;
    private AlertDialog.Builder dialogbuilder;
    private AlertDialog dialog;
    private int STORAGE_PERMISSION_CODE = 1;
    private static final int GalleryPick = 1;

    MyDatabaseHelper myDB;
    RecyclerView recyclerView;
    ArrayList<String> id, input, result;
    ResultAdapter resultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recylerview);

        myDB = new MyDatabaseHelper(MainActivity.this);
        id = new ArrayList<>();
        input = new ArrayList<>();
        result = new ArrayList<>();

        storeData();

        resultAdapter = new ResultAdapter(MainActivity.this,this, id, input, result);
        recyclerView.setAdapter(resultAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        cvinput = findViewById(R.id.cvadd);
        cvinput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialogInput();
            }
        });

    }
    public void createDialogInput(){
        dialogbuilder = new AlertDialog.Builder(this);
        final View dialoginput = getLayoutInflater().inflate(R.layout.pop_up_input, null);
        cvfromcamera = dialoginput.findViewById(R.id.cvfromcamera);
        cvfromfile = dialoginput.findViewById(R.id.cvfromfile);

        cvfromcamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, InputFromCameraActivity.class);
                startActivity(intent);
                finish();
            }
        });

        cvfromfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, InputFromFileActivity.class);
                startActivity(intent);
                finish();
                /*if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, GalleryPick);
                } else {
                    permissionAccessFile();
                }*/
            }
        });

        dialogbuilder.setView(dialoginput);
        dialog = dialogbuilder.create();
        dialog.show();

    }

    public void permissionAccessFile(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("These permissions are required")
                    .setMessage("These permissions are required to take pictures")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cvfromfile.callOnClick();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }
    private void storeData(){
        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0){
        }else{
            while (cursor.moveToNext()){
                id.add(cursor.getString(0));
                input.add(cursor.getString(1));
                result.add(cursor.getString(2));
            }
        }
    }
}