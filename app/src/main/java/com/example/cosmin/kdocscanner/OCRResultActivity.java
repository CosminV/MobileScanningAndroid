package com.example.cosmin.kdocscanner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.microblink.activity.BaseScanActivity;
import com.microblink.recognizers.BaseRecognitionResult;
import com.microblink.recognizers.RecognitionResults;
import com.microblink.recognizers.blinkid.romania.front.RomanianIDFrontSideRecognitionResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import Utils.ConvertUtils;
import Utils.ProgressDialogManager;

public class OCRResultActivity extends AppCompatActivity {

    RecognitionResults results;
    ImageButton againButton;
    ImageButton createButton;
    ImageButton newClientButton;

    TextView nameTextView;
    TextView surnameTextView;
    TextView idTextView;
    TextView addressTextView;
    TextView sexTextView;
    TextView birthdayTextView;
    TextView expiryDateTextView;
    TextView nationalityTextView;
    TextView issuingDateTextView;
    TextView issuedByTextView;
    TextView birthPlaceTextView;
    TextView CNPTextView;

    ProgressDialog pd;

    String templateFileLocation = "/storage/emulated/0/documenteScan/docTemplate.txt";
    String ip = "192.168.175.44:83";
    String ip2 = "192.168.184.55:52600";
    String ip3 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrresult);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.karrows);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nameTextView = (TextView) findViewById(R.id.txtName);
        surnameTextView = (TextView) findViewById(R.id.txtSurname);
        idTextView = (TextView) findViewById(R.id.txtID);
        addressTextView = (TextView) findViewById(R.id.txtAddress);
        sexTextView = (TextView) findViewById(R.id.txtSex);
        birthdayTextView = (TextView) findViewById(R.id.txtBirthday);
        expiryDateTextView = (TextView) findViewById(R.id.txtExpiryDate);
        nationalityTextView = (TextView) findViewById(R.id.txtNationality);
        issuingDateTextView = (TextView) findViewById(R.id.txtIssuingDate);
        issuedByTextView = (TextView) findViewById(R.id.txtIssuedBy);
        birthPlaceTextView = (TextView) findViewById(R.id.txtBirthPlace);
        CNPTextView = (TextView) findViewById(R.id.txtCNP);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            results = intent.getParcelableExtra(BaseScanActivity.EXTRAS_RECOGNITION_RESULTS);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("mobileScanningPrefs", Context.MODE_PRIVATE);
        String typeFlag = sharedPreferences.getString("typeFlag", "n/a");
        createButton = (ImageButton) findViewById(R.id.createBtn);
        newClientButton = (ImageButton) findViewById(R.id.newClientBtn);

        if(typeFlag.equals("contract")){
            createButton.setEnabled(true);
            newClientButton.setEnabled(false);
        }else if(typeFlag.equals("client")){
            newClientButton.setEnabled(true);
            createButton.setEnabled(false);
        }
        showResult(results);
        scanAgain();
        sendDataToDB();
        createDocumentIndirect();
    }

    //extragere rezultate OCR
    public void showResult(RecognitionResults results) {
        BaseRecognitionResult[] resultArray = results.getRecognitionResults();

        //verificare daca exista rezultate transmise de operatie
        if (resultArray != null && resultArray.length > 0) {
            for(BaseRecognitionResult baseRecognitionResult : resultArray) {
                if (baseRecognitionResult instanceof RomanianIDFrontSideRecognitionResult) {

                    //extragerea propriu-zisa a rezultatelor OCR-izarii unei carti de identitate din Romania
                    RomanianIDFrontSideRecognitionResult result = (RomanianIDFrontSideRecognitionResult) baseRecognitionResult;
                    String name = result.getFirstName();
                    String surname = result.getLastName();
                    String idSeries = result.getIdentityCardSeries();
                    String idNumber = result.getIdentityCardNumber();
                    String id = idSeries + idNumber;
                    String nationality = result.getNationality();
                    String sex = result.getSex();
                    String address = result.getAddress();
                    String issuedBy = result.getIssuedBy();
                    String birthPlace = result.getPlaceOfBirth();
                    String cnp = result.getCNP();
                    Date issuingDate = result.getValidFrom();
                    Date birthDate = result.getDateOfBirth();
                    Date expiryDate = result.getValidUntil();
                    Log.d("OUTPUT RO ID ", name + surname + id + nationality + sex + address + issuedBy + birthPlace + issuingDate + birthDate + expiryDate);

                    Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String birthDateString = formatter.format(birthDate);
                    String issuingDateString = formatter.format(issuingDate);
                    String expiryDateString = formatter.format(expiryDate);

                    //setarea rezultatelor anterior extrase in campurile respective din fereastra de preview.
                    nameTextView.setText(name);
                    surnameTextView.setText(surname);
                    idTextView.setText(id);
                    addressTextView.setText(address);
                    sexTextView.setText(sex);
                    birthdayTextView.setText(birthDateString);
                    expiryDateTextView.setText(expiryDateString);
                    nationalityTextView.setText(nationality);
                    issuingDateTextView.setText(issuingDateString);
                    issuedByTextView.setText(issuedBy);
                    birthPlaceTextView.setText(birthPlace);
                    CNPTextView.setText(cnp);
                }
            }
        }
    }

    // reintoarcere la activitatea de preluarea a pozei documentului.
    public void scanAgain(){
        againButton = (ImageButton) findViewById(R.id.againBtn);
        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bitmapString = getIntent().getStringExtra("BitmapString");
                Intent scanAgainIntent = new Intent(OCRResultActivity.this, IDActivity.class);
                scanAgainIntent.putExtra("BitmapStringFromResultActivity", bitmapString);
                scanAgainIntent.putExtra("retryFlag", true);
                startActivity(scanAgainIntent);
            }
        });
    }

    // metoda pentru transmiterea datelor unui client anterior OCR-izate catre baza de date.
    public void sendDataToDB() {
        newClientButton = (ImageButton) findViewById(R.id.newClientBtn);
        newClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL("http://" + ip + "/api/Person");
                                final String name = nameTextView.getText().toString();
                                final String surname = surnameTextView.getText().toString();
                                String id = idTextView.getText().toString();
                                final String address = addressTextView.getText().toString();
                                final String sex = sexTextView.getText().toString();
                                final String birthDate = birthdayTextView.getText().toString();
                                final String expiryDate = expiryDateTextView.getText().toString();
                                final String nationality = nationalityTextView.getText().toString();
                                final String issuingDate = issuingDateTextView.getText().toString();
                                final String issuedBy = issuedByTextView.getText().toString();
                                final String birthPlace = birthPlaceTextView.getText().toString();
                                final String cnp = CNPTextView.getText().toString();
                                int responseCodeAddressCity = 0;
                                int responseCodePersonBirth = 0;
                                int responseCodePersonID = 0;
                                int responseCodePersonNationality = 0;
                                int responseCodePersonPersonalIdentificationNumber = 0;
                                int responseCodePersonSex = 0;
                                int responseCodePersonCard = 0;
                                String[] addressArray = address.split("\\n");
                                String finalIDResponse = "";
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd = ProgressDialogManager.initiateProgressDialog("Sending client data...", OCRResultActivity.this);
                                    }
                                });
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setConnectTimeout(8000);

                                JSONObject jsonBodyName = new JSONObject();
                                try {
                                    jsonBodyName.put("IDType", 1);
                                    jsonBodyName.put("FirstName", name);
                                    jsonBodyName.put("MiddleName", null);
                                    jsonBodyName.put("LastName", surname);
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                                final String requestBodyName = jsonBodyName.toString();
                                Log.i("ADDRESS", address);
                                Log.i("JSON", requestBodyName);
                                connection.setDoOutput(true);
                                connection.getOutputStream().write(requestBodyName.getBytes());

                                // GET RETURNED ID

                                if(connection.getResponseCode() == 201){

                                    int value = 0;
                                    do{
                                        Log.i("Responsemessage" + value, connection.getHeaderField(value) + " " + connection.getHeaderFieldKey(value));
                                        if(connection.getHeaderFieldKey(value).equals("PID")){
                                            finalIDResponse = connection.getHeaderField(value);
                                            Log.i("FINALIDFROMRESPONSE", finalIDResponse);
                                            value = -2;
                                        }
                                        value++;
                                    }while(connection.getHeaderFieldKey(value) != null && connection.getHeaderField(value) != null && value > 0);

                                } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ProgressDialogManager.destroyProgressDialog(pd);
                                                Toast.makeText(getApplicationContext(), "Unable to send client data", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        System.out.println("Unable to POST");
                                }

                                connection.disconnect();

                                // POST PersonAddressStreet

                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setConnectTimeout(8000);

                                setHttpConnection(url);

                                JSONObject jsonBodyAddressStreet = postPersonAddressStreet(finalIDResponse, addressArray);

                                final String requestBodyAddressStreet = jsonBodyAddressStreet.toString();
                                Log.i("JSON", requestBodyAddressStreet);
                                connection.setDoOutput(true);
                                connection.getOutputStream().write(requestBodyAddressStreet.getBytes());

                                int responseCodeAddressStreet = connection.getResponseCode();
                                connection.disconnect();

                                // POST PersonAddressCity
                                if(responseCodeAddressStreet == 201){
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("POST");
                                    connection.setRequestProperty("Content-Type", "application/json");
                                    connection.setConnectTimeout(8000);

                                    JSONObject jsonBodyAddressCity = postPersonAddressCity(finalIDResponse, addressArray);

                                    final String requestBodyAddressCity = jsonBodyAddressCity.toString();
                                    Log.i("JSON", requestBodyAddressCity);
                                    connection.setDoOutput(true);
                                    connection.getOutputStream().write(requestBodyAddressCity.getBytes());
                                    responseCodeAddressCity = connection.getResponseCode();
                                    connection.disconnect();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialogManager.destroyProgressDialog(pd);
                                            Toast.makeText(getApplicationContext(), "Unable to send client data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    System.out.println("Eroare aparuta la ev. de POST pentru AddressStreet");
                                }
                                // POST PersonBirth
                                if(responseCodeAddressCity == 201){
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("POST");
                                    connection.setRequestProperty("Content-Type", "application/json");
                                    connection.setConnectTimeout(8000);

                                    JSONObject jsonBodyPersonBirth = postPersonBirth(finalIDResponse, birthPlace, birthDate);

                                    final String requestBodyPersonBirth = jsonBodyPersonBirth.toString();
                                    Log.i("JSON", requestBodyPersonBirth);
                                    connection.setDoOutput(true);
                                    connection.getOutputStream().write(requestBodyPersonBirth.getBytes());
                                    responseCodePersonBirth = connection.getResponseCode();
                                    connection.disconnect();
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialogManager.destroyProgressDialog(pd);
                                            Toast.makeText(getApplicationContext(), "Unable to send client data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    System.out.println("Eroare aparuta la ev. de POST pentru AddressCity");
                                }

                                // POST PersonID
                                if(responseCodePersonBirth == 201){
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("POST");
                                    connection.setRequestProperty("Content-Type", "application/json");
                                    connection.setConnectTimeout(8000);

                                    JSONObject jsonBodyPersonID = postPersonID(finalIDResponse, id, issuingDate, expiryDate);

                                    final String requestBodyPersonID = jsonBodyPersonID.toString();
                                    Log.i("JSON", requestBodyPersonID);
                                    connection.setDoOutput(true);
                                    connection.getOutputStream().write(requestBodyPersonID.getBytes());
                                    responseCodePersonID = connection.getResponseCode();
                                    connection.disconnect();
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialogManager.destroyProgressDialog(pd);
                                            Toast.makeText(getApplicationContext(), "Unable to send client data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    System.out.println("Eroare aparuta la ev. de POST pentru PersonBirth");
                                }


                                // POST PersonNationality
                                if(responseCodePersonID == 201){
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("POST");
                                    connection.setRequestProperty("Content-Type", "application/json");
                                    connection.setConnectTimeout(8000);

                                    JSONObject jsonBodyPersonNationality = postPersonNationality(finalIDResponse, nationality);

                                    final String requestBodyPersonNationality = jsonBodyPersonNationality.toString();
                                    Log.i("JSON", requestBodyPersonNationality);
                                    connection.setDoOutput(true);
                                    connection.getOutputStream().write(requestBodyPersonNationality.getBytes());
                                    responseCodePersonNationality = connection.getResponseCode();
                                    connection.disconnect();
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialogManager.destroyProgressDialog(pd);
                                            Toast.makeText(getApplicationContext(), "Unable to send client data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    System.out.println("Eroare aparuta la ev. de POST pentru PersonID");
                                }


                                // POST PersonPersonalIdentificationNumber
                                if(responseCodePersonNationality == 201){
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("POST");
                                    connection.setRequestProperty("Content-Type", "application/json");
                                    connection.setConnectTimeout(8000);

                                    JSONObject jsonBodyPersonPersonalIdentificationNumber = postPersonPersonalIdentificationNumber(finalIDResponse, cnp);

                                    final String requestBodyPersonalIdentificationNumber = jsonBodyPersonPersonalIdentificationNumber.toString();
                                    Log.i("JSON", requestBodyPersonalIdentificationNumber);
                                    connection.setDoOutput(true);
                                    connection.getOutputStream().write(requestBodyPersonalIdentificationNumber.getBytes());
                                    responseCodePersonPersonalIdentificationNumber = connection.getResponseCode();
                                    connection.disconnect();
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialogManager.destroyProgressDialog(pd);
                                            Toast.makeText(getApplicationContext(), "Unable to send client data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    System.out.println("Eroare aparuta la ev. de POST pentru PersonNationality");
                                }

                                // POST PersonSex
                                if(responseCodePersonPersonalIdentificationNumber == 201){
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("POST");
                                    connection.setRequestProperty("Content-Type", "application/json");
                                    connection.setConnectTimeout(8000);

                                    JSONObject jsonBodyPersonSex = postPersonSex(finalIDResponse, sex);

                                    final String requestBodyPersonSex = jsonBodyPersonSex.toString();
                                    Log.i("JSON", requestBodyPersonSex);
                                    connection.setDoOutput(true);
                                    connection.getOutputStream().write(requestBodyPersonSex.getBytes());
                                    responseCodePersonSex = connection.getResponseCode();
                                    connection.disconnect();
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialogManager.destroyProgressDialog(pd);
                                            Toast.makeText(getApplicationContext(), "Unable to send client data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    System.out.println("Eroare aparuta la ev. de POST pentru PersonPersonalIdentificationNumber");
                                }

                                if(responseCodePersonSex == 201){
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("POST");
                                    connection.setRequestProperty("Content-Type", "application/json");
                                    connection.setConnectTimeout(8000);

                                    String bitmapString = getIntent().getStringExtra("BitmapString");

                                    JSONObject jsonBodyPersonCard = postPersonCard(finalIDResponse, bitmapString);

                                    final String requestBodyPersonCard = jsonBodyPersonCard.toString();
                                    Log.i("JSON", requestBodyPersonCard);
                                    connection.setDoOutput(true);
                                    connection.getOutputStream().write(requestBodyPersonCard.getBytes());
                                    responseCodePersonCard = connection.getResponseCode();
                                    connection.disconnect();

                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialogManager.destroyProgressDialog(pd);
                                            Toast.makeText(getApplicationContext(), "Unable to send client data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    System.out.println("Eroare aparuta la ev. de POST pentru PersonSex");
                                }
                                HttpResponseCache responseCache = HttpResponseCache.install(getCacheDir(), 100000L);
                                connection.disconnect();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ProgressDialogManager.destroyProgressDialog(pd);
                                        Toast.makeText(getApplicationContext(), "Data sent succesfully!", Toast.LENGTH_LONG).show();
                                    }
                                });
                                Log.i("ResponseCode", connection.getResponseCode() + "");

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (java.net.SocketTimeoutException e){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ProgressDialogManager.destroyProgressDialog(pd);
                                        Toast.makeText(getApplicationContext(), "Couldn't establish a connection to the server. Try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception ex) {
                        System.out.println(ex.toString());
                    }
            }
        });
    }

    public void createDocumentIndirect(){
        createButton = (ImageButton) findViewById(R.id.createBtn);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nameTextView.getText().toString();
                final String surname = surnameTextView.getText().toString();
                String id = idTextView.getText().toString();
                final String address = addressTextView.getText().toString();
                final String birthDate = birthdayTextView.getText().toString();
                final String nationality = nationalityTextView.getText().toString();
                final String issuingDate = issuingDateTextView.getText().toString();
                final String issuedBy = issuedByTextView.getText().toString();
                final String cnp = CNPTextView.getText().toString();

                Intent docActivityIntent = new Intent(OCRResultActivity.this, DocumentActivity.class);
                docActivityIntent.putExtra("name", name);
                docActivityIntent.putExtra("surname", surname);
                docActivityIntent.putExtra("id", id);
                docActivityIntent.putExtra("address", address);
                docActivityIntent.putExtra("nationality", nationality);
                docActivityIntent.putExtra("birthdate", birthDate);
                docActivityIntent.putExtra("issuingdate", issuingDate);
                docActivityIntent.putExtra("issuedby", issuedBy);
                docActivityIntent.putExtra("cnp", cnp);
                startActivity(docActivityIntent);
            }
        });
    }

    public void createDocumentDirect(){
        createButton = (ImageButton) findViewById(R.id.createBtn);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nameTextView.getText().toString();
                final String surname = surnameTextView.getText().toString();
                String id = idTextView.getText().toString();
                final String address = addressTextView.getText().toString();
                final String sex = sexTextView.getText().toString();
                final String birthDate = birthdayTextView.getText().toString();
                final String expiryDate = expiryDateTextView.getText().toString();
                final String nationality = nationalityTextView.getText().toString();
                final String issuingDate = issuingDateTextView.getText().toString();
                final String issuedBy = issuedByTextView.getText().toString();
                final String birthPlace = birthPlaceTextView.getText().toString();
                final String cnp = CNPTextView.getText().toString();

                File templateFile = new File(templateFileLocation);
                String namearg = "[name]";
                String surnamearg = "[surname]";
                String idarg = "[id]";
                String cnparg = "[cnp]";
                String nationalityarg = "[nationality]";
                String addressarg = "[address]";
                String birthdatearg = "[birthdate]";
                String issuingdatearg = "[issuingdate]";
                String issuedbyarg = "[issuedby]";
                String todaydatearg = "[todaydate]";
                String replacenamearg = name;
                String replacesurnamearg = surname;
                String replaceidarg = id;
                String replacecnparg = cnp;
                String replacenationalityarg = nationality;
                String replaceaddressarg = address;
                String replacebirthdatearg = birthDate;
                String replaceissuingdatearg = issuingDate;
                String replaceissuedbyarg = issuedBy;
                String replacetodayarg = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());

                try{
                    FileReader fileReader = new FileReader(templateFile);
                    String s;
                    String fullFileContent = "";
                    String replacedFullFileContent = "";

                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    while((s = bufferedReader.readLine()) != null){
                        fullFileContent += "\n" + s;
                    }

                    replacedFullFileContent = fullFileContent.replace(namearg, replacenamearg);
                    replacedFullFileContent = replacedFullFileContent.replace(surnamearg, replacesurnamearg);
                    replacedFullFileContent = replacedFullFileContent.replace(idarg, replaceidarg);
                    replacedFullFileContent = replacedFullFileContent.replace(cnparg, replacecnparg);
                    replacedFullFileContent = replacedFullFileContent.replace(nationalityarg, replacenationalityarg);
                    replacedFullFileContent = replacedFullFileContent.replace(addressarg, replaceaddressarg);
                    replacedFullFileContent = replacedFullFileContent.replace(birthdatearg, replacebirthdatearg);
                    replacedFullFileContent = replacedFullFileContent.replace(issuingdatearg, replaceissuingdatearg);
                    replacedFullFileContent = replacedFullFileContent.replace(issuedbyarg, replaceissuedbyarg);
                    replacedFullFileContent = replacedFullFileContent.replace(todaydatearg, replacetodayarg);

                    FileWriter fileWriter = new FileWriter("/storage/emulated/0/documenteScan/" + surname + " "+ replacetodayarg + ".txt");
                    fileWriter.write(replacedFullFileContent);
                    fileWriter.close();

                    Toast.makeText(getApplicationContext(), "Succesfully created document", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public static String getIDNumber(String id){
        String idNumber = "";

        /*if(id.length() == 8){
            idNumber = id.substring(2);
        }else{
            System.out.println("Camp invalid");
        }*/

        String idArray[] = id.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        idNumber = idArray[1];
        return idNumber;
    }

    public HttpURLConnection setHttpConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        return connection;
    }

    public JSONObject postPersonAddressStreet(String finalIDResponse, String[] addressArray){

        JSONObject jsonBodyAddressStreet = new JSONObject();
        try {
            jsonBodyAddressStreet.put("ID", finalIDResponse);
            jsonBodyAddressStreet.put("AddressStreet", addressArray[1]);
            jsonBodyAddressStreet.put("AddressStreetNo", null);
            jsonBodyAddressStreet.put("AddressBuilding", null);
            jsonBodyAddressStreet.put("AddressBuildingNo", null);
            jsonBodyAddressStreet.put("AddressFloor", null);
            jsonBodyAddressStreet.put("AddressAppartment", null);

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonBodyAddressStreet;
    }

    public JSONObject postPersonAddressCity(String finalIDResponse, String[] addressArray){

        JSONObject jsonBodyAddressCity = new JSONObject();
        try {
            jsonBodyAddressCity.put("ID", finalIDResponse);
            jsonBodyAddressCity.put("AddressCity", addressArray[0]);
            jsonBodyAddressCity.put("AddressCityCode", null);
            jsonBodyAddressCity.put("AddressCounty", null);
            jsonBodyAddressCity.put("AddressCountry", "Romania");

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonBodyAddressCity;
    }

    public JSONObject postPersonBirth(String finalIDResponse, String birthPlace, String birthDate){
        JSONObject jsonBodyPersonBirth = new JSONObject();
        try {
            jsonBodyPersonBirth.put("ID", finalIDResponse);
            jsonBodyPersonBirth.put("BirthPlace", birthPlace);
            jsonBodyPersonBirth.put("BirthDate", birthDate);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonBodyPersonBirth;
    }

    public JSONObject postPersonID(String finalIDResponse, String id, String issuingDate, String expiryDate){
        JSONObject jsonBodyPersonID = new JSONObject();
        try {
            jsonBodyPersonID.put("ID", finalIDResponse);
            jsonBodyPersonID.put("IDType", 1);
            jsonBodyPersonID.put("IDNumber", getIDNumber(id));
            jsonBodyPersonID.put("IDSeries", getIDSeries(id));
            jsonBodyPersonID.put("IDIssuingDate", issuingDate);
            jsonBodyPersonID.put("IDExpiryDate", expiryDate);

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonBodyPersonID;
    }

    public JSONObject postPersonNationality(String finalIDResponse, String nationality){
        JSONObject jsonBodyPersonNationality = new JSONObject();
        try {
            jsonBodyPersonNationality.put("ID", finalIDResponse);
            jsonBodyPersonNationality.put("Nationality", nationality);
            jsonBodyPersonNationality.put("Citizenship", nationality);

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonBodyPersonNationality;
    }

    public JSONObject postPersonPersonalIdentificationNumber(String finalIDResponse, String cnp){
        JSONObject jsonBodyPersonPersonalIdentificationNumber = new JSONObject();
        try {
            jsonBodyPersonPersonalIdentificationNumber.put("ID", finalIDResponse);
            jsonBodyPersonPersonalIdentificationNumber.put("PersonalIdentificationNumber", cnp);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonBodyPersonPersonalIdentificationNumber;
    }

    public JSONObject postPersonSex(String finalIDResponse, String sex){
        JSONObject jsonBodyPersonSex = new JSONObject();
        try {
            jsonBodyPersonSex.put("ID", finalIDResponse);
            jsonBodyPersonSex.put("Sex", sex);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonBodyPersonSex;
    }

    public JSONObject postPersonCard(String finalIDResponse, String bitmapString){
        JSONObject jsonBodyPersonIDCard = new JSONObject();
        try {
            jsonBodyPersonIDCard.put("ID", finalIDResponse);
            jsonBodyPersonIDCard.put("IDCard", bitmapString);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return jsonBodyPersonIDCard;
    }

    public static String getIDSeries(String id){
        String idSeries = "";

        String idArray[] = id.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        idSeries = idArray[0];

        return idSeries;
    }

    public static String getAddressStreet(String address){
        String addressStreet = "sss";

        if(address.length() > 0){
            int position = address.lastIndexOf(".");
            addressStreet = position + "";
            if(position > 0) {
                addressStreet = address.substring(position + 2);
            }
        }else{
            System.out.println("Camp invalid STREET");
        }

        return addressStreet;
    }

    public static String getAddressCity(String address){
        String addressCity = "xxx";

        if(address.length() > 0){
            int position = address.lastIndexOf(".");
            addressCity = position + "";
            if(position > 0) {
                addressCity = address.substring(0, position);
            }
        }else{
            System.out.println("Camp invalid CITY");
        }

        return addressCity;
    }

}