package com.example.cosmin.kdocscanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.microblink.activity.BaseScanActivity;
import com.microblink.directApi.DirectApiErrorListener;
import com.microblink.directApi.Recognizer;
import com.microblink.hardware.orientation.Orientation;
import com.microblink.recognition.FeatureNotSupportedException;
import com.microblink.recognition.InvalidLicenceKeyException;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.view.recognition.ScanResultListener;

import java.io.IOException;
import java.io.InputStream;

public class OCRActivity extends Activity {

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final String BITMAP_NAME = "croID.jpg";

    public static final int CAMERA_REQUEST_CODE = 0x101;

    public static final String TAG = "TEST OCR";

    private Button scanButton;
    private Button takeButton;
    private ImageView bitmapImageView;

    private Recognizer recognizer = null;
    private RecognitionSettings recognitionSettings;
    private String licenseKey;

    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null){
            recognitionSettings = extras.getParcelable(BaseScanActivity.EXTRAS_RECOGNITION_SETTINGS);
            licenseKey = extras.getString(BaseScanActivity.EXTRAS_LICENSE_KEY);
        }

        AssetManager assetManager = getAssets();
        InputStream inputStream = null;

        try{
            inputStream = assetManager.open(BITMAP_NAME);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = BITMAP_CONFIG;
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);

        }catch(IOException ex){
            Toast.makeText(getApplicationContext(), "Failed to load image from assets!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }finally{
            try{
                inputStream.close();
            }catch(IOException ex){

            }
        }
        bitmapImageView = (ImageView) findViewById(R.id.imageViewForOCR);
        bitmapImageView.setImageBitmap(bitmap);

        OCRScan();
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
            recognizer.setLicenseKey(this, "L35JCAC6-7PER3PZU-NM3DRECK-5FCXBDPC-ZEFMHRMB-6FX53RQ3-6FX52ZR2-HOZHMODM");
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

    public void OCRScan(){
        scanButton = (Button) findViewById(R.id.scanBtn);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap != null){
                    scanButton.setEnabled(false);

                    final ProgressDialog pd = new ProgressDialog(OCRActivity.this);
                    pd.setIndeterminate(true);
                    pd.setMessage("Performing recognition");
                    pd.setCancelable(true);
                    pd.show();

                    recognizer.recognizeBitmap(bitmap, Orientation.ORIENTATION_LANDSCAPE_RIGHT, new ScanResultListener() {
                        @Override
                        public void onScanningDone(@Nullable RecognitionResults recognitionResults) {
                            BaseRecognitionResult[] baseRecognitionResults = recognitionResults.getRecognitionResults();
                            boolean haveSomething = true;
                            if(baseRecognitionResults != null){
                                for(BaseRecognitionResult baseRecognitionResult : baseRecognitionResults){
                                    if(!baseRecognitionResult.isEmpty() && baseRecognitionResult.isValid()){
                                        haveSomething = true;
                                        break;
                                    }
                                }
                            }

                            if(haveSomething){
                                Intent intent = new Intent();
                                intent.putExtra(BaseScanActivity.EXTRAS_RECOGNITION_RESULTS, recognitionResults);
                                setResult(BaseScanActivity.RESULT_OK, intent);
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(), "Nothing scanned!", Toast.LENGTH_SHORT).show();
                                pd.hide();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        scanButton.setEnabled(true);
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
}
