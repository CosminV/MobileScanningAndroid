package com.example.cosmin.kdocscanner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.RunnableFuture;

import Classes.UserAccount;
import EmailAPI.SendMail;
import Requests.RegisterRequest;
import Utils.ConvertUtils;
import Utils.DatabaseHelper;
import Utils.ProgressDialogManager;

public class RegisterActivity extends AppCompatActivity {

    DatabaseHelper dbHelper = new DatabaseHelper(this);
    ConvertUtils convertUtils = new ConvertUtils();

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.karrows);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerAction();
        userSignature();

        EditText etEmail = (EditText) findViewById(R.id.emailTxt);
        String emailExtra = getIntent().getStringExtra("emailExtra");
        etEmail.setText(emailExtra);

        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");

        byte[] bitmapArray = getIntent().getByteArrayExtra("byteArray");
        boolean fromUserSign = getIntent().getBooleanExtra("checker", false);

        if(fromUserSign == true){
            autoFill(email, password, bitmapArray);
        }
    }

    //functie pentru completarea automata a campurilor dupa introducerea semnaturii
    public void autoFill(String email, String password, byte[] bitmapArray){
        EditText emailET = (EditText) findViewById(R.id.emailTxt);
        EditText passwordET = (EditText) findViewById(R.id.passwordTxt);
        ImageView signImageView = (ImageView) findViewById(R.id.userSignatureImageView);

        emailET.setText(email);
        passwordET.setText(password);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        signImageView.setImageBitmap(bitmap);
    }

    //intrarea in evenimentul de captura a semnaturii si procesarea acesteia
    public void userSignature(){

        final ImageView signImageView = (ImageView) findViewById(R.id.userSignatureImageView);
        signImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                signImageView.setImageResource(0);
                EditText emailET = (EditText) findViewById(R.id.emailTxt);
                EditText passwordET = (EditText) findViewById(R.id.passwordTxt);

                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();


                Intent signIntent = new Intent(RegisterActivity.this, SignActivity.class);
                signIntent.putExtra("email", email);
                signIntent.putExtra("password", password);

                signIntent.putExtra("FLAG", "user");
                startActivity(signIntent);
                return true;
            }
        });
    }

    public void registerAction() {

        ImageButton registerBtn = (ImageButton) findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // se verifica daca exista conexiune la internet.
                if(haveNetworkConnection()){

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                //initializarea controalelor folosite pentru actiunea de register.
                                final EditText etUsername = (EditText) findViewById(R.id.emailTxt);
                                final EditText etPassword = (EditText) findViewById(R.id.passwordTxt);

                                final String userName = etUsername.getText().toString();
                                final String userPassword = etPassword.getText().toString();

                                // stabilirea URL-ului
                                URL url = new URL("http://192.168.175.44:83/api/User");

                                //initializarea unui progress dialog pe durata evenimentului de register
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd = ProgressDialogManager.initiateProgressDialog("Registering ...", RegisterActivity.this);
                                    }
                                });

                                //deschiderea conexiunii
                                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setConnectTimeout(8000);

                                JSONObject jsonBodyUserRegister = new JSONObject();
                                jsonBodyUserRegister.put("userName", userName);
                                jsonBodyUserRegister.put("userPassword", userPassword);

                                final String requestBodyRegister = jsonBodyUserRegister.toString();
                                Log.i("JSON REGISTER", requestBodyRegister);
                                connection.setDoOutput(true);
                                connection.getOutputStream().write(requestBodyRegister.getBytes());

                                HttpResponseCache responseCache = HttpResponseCache.install(getCacheDir(), 100000L);
                                int responseCodeRegister = connection.getResponseCode();
                                Log.i("ResponseCode: ", responseCodeRegister + "");

                                connection.disconnect();

                                if(responseCodeRegister == 201){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialogManager.destroyProgressDialog(pd);
                                            Toast.makeText(getApplicationContext(), "Registration complete", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(loginIntent);
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialogManager.destroyProgressDialog(pd);
                                            Toast.makeText(getApplicationContext(), "Registration failed. Please try again later.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                //Log.i("ResponseCode", connection.getResponseCode() + "");

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (ProtocolException e) {
                                e.printStackTrace();
                            } catch(SocketTimeoutException e){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ProgressDialogManager.destroyProgressDialog(pd);
                                        Toast.makeText(getApplicationContext(), "Couldn't establish a connection to the server. Try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage("Internet unavailable")
                            .setNegativeButton("OK", null)
                            .create()
                            .show();
                }
            }
        });
    }

    /*public void SQLiteRegister(){
        final EditText etEmail = (EditText) findViewById(R.id.emailTxt);
        final EditText etPassword = (EditText) findViewById(R.id.passwordTxt);
        final EditText etName = (EditText) findViewById(R.id.nameTxt);

        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();
        String name = etName.getText().toString();

        UserAccount userAccount = new UserAccount(email, password, name, age, location);
        ImageView signedImage = (ImageView) findViewById(R.id.userSignatureImageView);
        Bitmap signedBitmap = ((BitmapDrawable)signedImage.getDrawable()).getBitmap();
        byte[] signatureArray = convertUtils.bitmapToByteArray(signedBitmap);
        dbHelper.registerUser(userAccount, signatureArray);
        sendConfirmationEmail(email, password);

        Toast.makeText(getApplicationContext(), "Registered succesfully", Toast.LENGTH_SHORT).show();
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        loginIntent.putExtra("emailExtra", email);
        loginIntent.putExtra("passwordExtra", password);
        startActivity(loginIntent);
    }*/

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //noinspection deprecation
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void sendConfirmationEmail(String emailAddress, String password){
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

        String subject = "Account created at " + date;

        StringBuilder stringBuilder = new StringBuilder("You have succesfully created your account. Thank you!");
        stringBuilder.append("\n\n");
        stringBuilder.append("Your login informations are: ");
        stringBuilder.append("\n");
        stringBuilder.append("Email: " + emailAddress);
        stringBuilder.append("\n");
        stringBuilder.append("Password: "+ password);

        SendMail sendMail = new SendMail(this, emailAddress, subject, stringBuilder.toString());
        sendMail.execute();
    }
}