package com.example.cosmin.kdocscanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SignActivity extends AppCompatActivity {

    private SignaturePad signaturePad;
    private Button clearBtn;
    private Button submitButton;
    private Button saveButton;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE ={Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private final static boolean SIGNED = true;
    private final static boolean YES = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.karrows);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        clearBtn = (Button) findViewById(R.id.clear_button);
        submitButton = (Button) findViewById(R.id.submit_button);
        saveButton = (Button) findViewById(R.id.save_button);

        toggleButtons();
        clearPad();
        saveSignature();
        submitButton();
    }

    public void toggleButtons(){
        signaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }
            @Override
            public void onSigned() {

                clearBtn.setEnabled(true);
                submitButton.setEnabled(true);
                saveButton.setEnabled(true);
            }
            @Override
            public void onClear() {
                clearBtn.setEnabled(false);
                submitButton.setEnabled(false);
                saveButton.setEnabled(false);
            }
        });
    }

    public void clearPad(){
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signaturePad.clear();
            }
        });
    }

    public void BitmapToJPG(Bitmap bitmap, File photo)throws IOException{
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream outputStream = new FileOutputStream(photo);
        tempBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        outputStream.close();
    }

    public boolean addJPGSignToGallery(Bitmap signature){
        boolean result = false;
        File location = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Signatures",
                              String.format("Signature_%d.jpg", System.currentTimeMillis()));
        try {
            BitmapToJPG(signature, location);
            scanMediaFile(location);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        SignActivity.this.sendBroadcast(mediaScanIntent);
    }

    public void saveSignature(){
        verifyStoragePermissions(this);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap signature = signaturePad.getTransparentSignatureBitmap();
                if(addJPGSignToGallery(signature) == true){
                    Toast.makeText(getApplicationContext(), "Signature saved!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Error while saving signature!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void submitButton(){
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = getIntent().getStringExtra("name");
                String surname = getIntent().getStringExtra("surname");
                String id = getIntent().getStringExtra("id");
                String address = getIntent().getStringExtra("address");
                String nationality = getIntent().getStringExtra("nationality");
                String birthdate = getIntent().getStringExtra("birthdate");
                String issuingdate = getIntent().getStringExtra("issuingdate");
                String issuedby = getIntent().getStringExtra("issuedby");
                String cnp = getIntent().getStringExtra("cnp");

                String userEmail = getIntent().getStringExtra("email");
                String userPassword = getIntent().getStringExtra("password");

                String FLAG = getIntent().getStringExtra("FLAG");

                Bitmap signatureBitmap = signaturePad.getTransparentSignatureBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                signatureBitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);

                if(FLAG.equals("document")) {
                    Intent finalReviewIntent = new Intent(SignActivity.this, DocumentActivity.class);
                    finalReviewIntent.putExtra("name", name);
                    finalReviewIntent.putExtra("surname", surname);
                    finalReviewIntent.putExtra("id", id + "");
                    finalReviewIntent.putExtra("address", address);
                    finalReviewIntent.putExtra("nationality", nationality);
                    finalReviewIntent.putExtra("birthdate", birthdate);
                    finalReviewIntent.putExtra("issuingdate", issuingdate);
                    finalReviewIntent.putExtra("issuedby", issuedby);
                    finalReviewIntent.putExtra("cnp", cnp);
                    finalReviewIntent.putExtra("byteArray", byteArrayOutputStream.toByteArray());
                    finalReviewIntent.putExtra("offSign", SIGNED);
                    startActivity(finalReviewIntent);
                }
                if(FLAG.equals("user")){
                    Intent completeRegisterIntent = new Intent(SignActivity.this, RegisterActivity.class);
                    completeRegisterIntent.putExtra("email", userEmail);
                    completeRegisterIntent.putExtra("password", userPassword);
                    completeRegisterIntent.putExtra("byteArray", byteArrayOutputStream.toByteArray());
                    completeRegisterIntent.putExtra("checker", YES);
                    startActivity(completeRegisterIntent);

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_EXTERNAL_STORAGE: {
                if(grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "Cannot write to external storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static void verifyStoragePermissions(Activity activity){
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }
}
