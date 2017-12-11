package com.example.cosmin.kdocscanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.microblink.activity.BaseScanActivity;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.blinkid.mrtd.MRTDRecognitionResult;
import com.microblink.recognizers.blinkid.mrtd.MRTDRecognizerSettings;
import com.microblink.recognizers.blinkid.romania.front.RomanianIDFrontSideRecognizerSettings;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;

public class OCRTriggerActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 1337;
    private static final String TAG = "TEST-OCR";

    private static final int PERMISSION_REQUEST_CODE = 0x123;

    private RecognitionSettings recognitionSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrtrigger);

        setupRecognitionSettings();
        goToOCRActivity();
    }

    private void setupRecognitionSettings(){
        recognitionSettings = new RecognitionSettings();
        recognitionSettings.setRecognizerSettingsArray(new RecognizerSettings[]{
            new RomanianIDFrontSideRecognizerSettings()});
        }

    private void goToOCRActivity(){
        Button scanBtn = (Button) findViewById(R.id.scanImageBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OCRTriggerActivity.this, OCRActivity.class);
                intent.putExtra(BaseScanActivity.EXTRAS_LICENSE_KEY, "L35JCAC6-7PER3PZU-NM3DRECK-5FCXBDPC-ZEFMHRMB-6FX53RQ3-6FX52ZR2-HOZHMODM");
                intent.putExtra(BaseScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);
                startActivityForResult(intent, MY_REQUEST_CODE);
            }
        });
    }

    public void showResult(RecognitionResults results){
        BaseRecognitionResult[] resultArray = results.getRecognitionResults();
        if(resultArray != null && resultArray.length > 0){
            if(resultArray[0] instanceof MRTDRecognitionResult){
                MRTDRecognitionResult result = (MRTDRecognitionResult) resultArray[0];
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append('\n');
                stringBuilder.append("Name: ");
                stringBuilder.append(result.getSecondaryId());
                stringBuilder.append('\n');
                stringBuilder.append("Surname: ");
                stringBuilder.append(result.getPrimaryId());
                stringBuilder.append('\n');
                stringBuilder.append("ID: ");
                stringBuilder.append(result.getDocumentNumber());
                stringBuilder.append('\n');
                stringBuilder.append("Nationality: ");
                stringBuilder.append(result.getNationality());

                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("OCR Result")
                        .setMessage(stringBuilder.toString())
                        .setCancelable(false)
                        .setPositiveButton("OK", null)
                        .create();
                alertDialog.show();
            }
        }else{
            Toast.makeText(OCRTriggerActivity.this, "Nothing scanned", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MY_REQUEST_CODE && resultCode == BaseScanActivity.RESULT_OK){
            RecognitionResults results = data.getParcelableExtra(BaseScanActivity.EXTRAS_RECOGNITION_RESULTS);
            showResult(results);
            Toast.makeText(this, "Scan complete!", Toast.LENGTH_SHORT).show();
        }
    }
}
