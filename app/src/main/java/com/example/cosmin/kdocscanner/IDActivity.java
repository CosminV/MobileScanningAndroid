package com.example.cosmin.kdocscanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.microblink.activity.BaseScanActivity;
import com.microblink.directApi.DirectApiErrorListener;
import com.microblink.directApi.Recognizer;
import com.microblink.hardware.orientation.Orientation;
import com.microblink.recognition.FeatureNotSupportedException;
import com.microblink.recognition.InvalidLicenceKeyException;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.blinkid.mrtd.MRTDRecognizerSettings;
import com.microblink.recognizers.blinkid.romania.front.RomanianIDFrontSideRecognizerSettings;
import com.microblink.recognizers.blinkocr.BlinkOCRRecognizerSettings;
import com.microblink.recognizers.blinkocr.engine.BlinkOCREngineOptions;
import com.microblink.recognizers.blinkocr.parser.generic.RawParserSettings;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;
import com.microblink.view.recognition.ScanResultListener;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import Utils.ConvertUtils;

import static android.R.attr.bitmap;
import static android.R.attr.type;

public class IDActivity extends Activity implements AppCompatCallback {

    private static final int REQUEST_CODE = 99;

    private ImageButton selectButton;
    private ImageButton cameraButton;
    private ImageView cameraImageView;
    private ImageButton scanButton;

    private Recognizer recognizer = null;
    private RecognitionSettings recognitionSettings;
    private static final String licenseKey = "L35JCAC6-7PER3PZU-NM3DRECK-5FCXBDPC-ZEFMHRMB-6FX53RQ3-6FX52ZR2-HOZHMODM";

    ProgressDialog pd;

    private Bitmap bmp = null;
    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_idnew);

        //setContentView(R.layout.activity_idnew);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setIcon(R.drawable.karrows);

        initButtons();
        setupRomanianIDSettings();
        scanAction();

        /*String bitmapStringFromResultActivity = getIntent().getStringExtra("BitmapStringFromResultActivity");
        boolean retryFlag = getIntent().getBooleanExtra("retryFlag", false);

        if(retryFlag == true){
            cameraImageView = (ImageView) findViewById(R.id.imageThumbnail);
            byte[] decodedByte = Base64.decode(bitmapStringFromResultActivity, 0);
            Bitmap retryBMP = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            cameraImageView.setImageBitmap(retryBMP);
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            recognizer = Recognizer.getSingletonInstance();
        } catch (FeatureNotSupportedException e) {
            Toast.makeText(getApplicationContext(), "Feature not supported! Reason: " + e.getReason().getDescription(), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            recognizer.setLicenseKey(this, licenseKey);
        } catch (InvalidLicenceKeyException e) {
            Toast.makeText(this, "Checking the license key failed! Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recognizer.initialize(this, recognitionSettings, new DirectApiErrorListener() {
            @Override
            public void onRecognizerError(Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Failed to initialize recognizer. Reason: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.id_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.exit:
                exitCompletely();
                break;
            case R.id.contactUs:
                Toast.makeText(getApplicationContext(), "ContactUs", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bugReport:
                Toast.makeText(getApplicationContext(), "Bug Report Activity!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.halfRotateLeftBtn:
                cameraImageView = (ImageView) findViewById(R.id.imageThumbnail);
                cameraImageView.setRotation(90);
                break;
            case R.id.halfRotateRightBtn:
                cameraImageView = (ImageView) findViewById(R.id.imageThumbnail);
                cameraImageView.setRotation(-90);
                break;
            case R.id.resetBtn:
                cameraImageView = (ImageView) findViewById(R.id.imageThumbnail);
                cameraImageView.setRotation(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initButtons() {
        selectButton = (ImageButton)findViewById(R.id.openBtn);
        selectButton.setOnClickListener(new ScanButtonClick(ScanConstants.OPEN_MEDIA));

        cameraButton = (ImageButton)findViewById(R.id.takeBtn);
        cameraButton.setOnClickListener(new ScanButtonClick(ScanConstants.OPEN_CAMERA));
    }

    private class ScanButtonClick implements View.OnClickListener {

        private int preference;
        public ScanButtonClick(int preference){this.preference = preference;}
        public ScanButtonClick(){}
        @Override
        public void onClick(View v) {
            startScan(preference);
        }
    }

    private void startScan(int preference) {
        Intent selectIntent = new Intent(getApplicationContext(), ScanActivity.class);
        selectIntent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(selectIntent, REQUEST_CODE);
    }

    public void scanAction()
    {
        scanButton = (ImageButton) findViewById(R.id.scanBtn);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cameraImageView = (ImageView) findViewById(R.id.imageThumbnail);
                bmp = ((BitmapDrawable)cameraImageView.getDrawable()).getBitmap();

                if(bmp != null){
                    scanButton.setEnabled(false);

                    pd = new ProgressDialog(IDActivity.this);
                    pd.setIndeterminate(true);
                    pd.setMessage("Performing recognition");
                    pd.setCancelable(true);
                    pd.show();

                    recognizer.recognizeBitmap(bmp, Orientation.ORIENTATION_LANDSCAPE_RIGHT, new ScanResultListener() {
                        @Override
                        public void onScanningDone(@Nullable final RecognitionResults recognitionResults) {
                            BaseRecognitionResult[] baseRecognitionResults = recognitionResults.getRecognitionResults();
                            boolean haveSomething = false;
                            if(baseRecognitionResults != null){
                                for(BaseRecognitionResult baseRecognitionResult : baseRecognitionResults){
                                    if(!baseRecognitionResult.isEmpty() && baseRecognitionResult.isValid()){
                                        haveSomething = true;
                                        break;
                                    }
                                }
                            }
                            if(haveSomething){
                                cameraImageView = (ImageView) findViewById(R.id.imageThumbnail);
                                bmp = ((BitmapDrawable)cameraImageView.getDrawable()).getBitmap();

                                ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
                                bmp.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOS);
                                String bitmapString =  Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);

                                Intent intent = new Intent(IDActivity.this, OCRResultActivity.class);
                                intent.putExtra(BaseScanActivity.EXTRAS_RECOGNITION_RESULTS, recognitionResults);
                                intent.putExtra("BitmapString", bitmapString);
                                startActivity(intent);
                                //finish();

                            }else{
                                Toast.makeText(getApplicationContext(), "Nothing scanned!", Toast.LENGTH_SHORT).show();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        scanButton.setEnabled(true);
                                        pd.dismiss();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(recognizer != null){
            recognizer.terminate();
        }
    }

    private void setupRomanianIDSettings(){
        recognitionSettings = new RecognitionSettings();
        recognitionSettings.setRecognizerSettingsArray(new RecognizerSettings[]{
                new RomanianIDFrontSideRecognizerSettings()});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView cameraImageView = null;

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                cameraImageView = (ImageView) findViewById(R.id.imageThumbnail);
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
                cameraImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void exitCompletely() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }
}
