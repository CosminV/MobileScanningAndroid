package com.example.cosmin.kdocscanner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
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

import Requests.LoginRequest;
import Utils.DatabaseHelper;
import Utils.ProgressDialogManager;

public class LoginActivity extends AppCompatActivity {

    DatabaseHelper dbHelper = new DatabaseHelper(this);

    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loginnew);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.karrows);

        EditText emailTxt = (EditText)findViewById(R.id.emailTxt);
        EditText passwordTxt = (EditText)findViewById(R.id.passwordTxt);
        String emailExtra = getIntent().getStringExtra("emailExtra");
        emailTxt.setText(emailExtra);
        String passwordExtra = getIntent().getStringExtra("passwordExtra");
        passwordTxt.setText(passwordExtra);

        checkBoxChangeState();
        logInAction();
        registerAction();
        exitApp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.contactUs:
                break;
            case R.id.exitApp:
                exitCompletely();
                break;
            case R.id.bugReport:
                Intent ocrIntent = new Intent(LoginActivity.this, OCRTriggerActivity.class);
                startActivity(ocrIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void LoginWithoutSQL(){
        EditText emailTxt = (EditText) findViewById(R.id.emailTxt);
        String email = emailTxt.getText().toString();
        EditText passwordTxt = (EditText) findViewById(R.id.passwordTxt);
        String password = passwordTxt.getText().toString();

        Intent loginConfirmationIntent = new Intent(LoginActivity.this, LoginConfirmationActivity.class);
        loginConfirmationIntent.putExtra("email", email);
        startActivity(loginConfirmationIntent);
    }

    public void registerAction() {
        ImageButton registerBtn = (ImageButton) findViewById(R.id.registerBtn);
        final CheckBox demoCB = (CheckBox) findViewById(R.id.demoCheckBox);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!demoCB.isChecked()) {
                    Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                    EditText etEmail = (EditText) findViewById(R.id.emailTxt);
                    String emailExtra = etEmail.getText().toString();
                    registerIntent.putExtra("emailExtra", emailExtra);
                    startActivity(registerIntent);
                }else{
                    Toast.makeText(getApplicationContext(), "You do not have to register for a demo version. \n Use demo credential to login", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void checkBoxChangeState(){
        final EditText etUsername = (EditText) findViewById(R.id.emailTxt);
        final EditText etPassword = (EditText) findViewById(R.id.passwordTxt);
        final CheckBox demoCB = (CheckBox) findViewById(R.id.demoCheckBox);
        demoCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(demoCB.isChecked()) {
                    etUsername.setText("demo");
                    etPassword.setText("demo123");
                }else{
                    etUsername.setText("");
                    etPassword.setText("");
                }
            }
        });
    }

    public void logInAction() {
        ImageButton loginBtn = (ImageButton) findViewById(R.id.logInBtn);
        final CheckBox demoCB = (CheckBox) findViewById(R.id.demoCheckBox);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // se verifica daca exista conexiune la internet.
                if(haveNetworkConnection()){
                    if(demoCB.isChecked()){
                        demoLogin();
                    }else{
                        DBLogin();
                    }
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Internet unavailable")
                            .setNegativeButton("OK", null)
                            .create()
                            .show();
                }
            }
        });
    }

    public void demoLogin(){

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final EditText etUsername = (EditText) findViewById(R.id.emailTxt);
                final EditText etPassword = (EditText) findViewById(R.id.passwordTxt);
                final String userName = etUsername.getText().toString();
                final String userPassword = etPassword.getText().toString();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd = ProgressDialogManager.initiateProgressDialog("Entering demo verison ...", LoginActivity.this);
                    }
                });

                if(!userName.equals("") && !userPassword.equals("")){
                    if(userName.equals("demo") && userPassword.equals("demo123")){
                        SharedPreferences sharedPreferences = getSharedPreferences("mobileScanningPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("appVersion", "demo");

                        Intent mainIntent = new Intent(LoginActivity.this, MainDrawer.class);
                        startActivity(mainIntent);
                        finish();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogManager.destroyProgressDialog(pd);
                                Toast.makeText(getApplicationContext(), "Welcome to demo version", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogManager.destroyProgressDialog(pd);
                                Toast.makeText(getApplicationContext(), "Please login with demo credentials", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressDialogManager.destroyProgressDialog(pd);
                            Toast.makeText(getApplicationContext(), "Please fill username and password fields", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    public void DBLogin(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    //initializare controale folosite la login
                    final EditText etUsername = (EditText) findViewById(R.id.emailTxt);
                    final EditText etPassword = (EditText) findViewById(R.id.passwordTxt);
                    final String userName = etUsername.getText().toString();
                    final String userPassword = etPassword.getText().toString();

                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    String returnedMessage = "";

                    URL url = new URL("http://192.168.175.44:83/api/User");

                    //initierea unui progress dialog pe durata conexiunii cu serverul.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pd = ProgressDialogManager.initiateProgressDialog("Logging in ...", LoginActivity.this);
                        }
                    });

                    // apelarea WS pentru login
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setConnectTimeout(8000);

                    JSONObject jsonBodyUserRegister = new JSONObject();
                    jsonBodyUserRegister.put("userName", userName);
                    jsonBodyUserRegister.put("userPassword", userPassword);
                    jsonBodyUserRegister.put("loginDateTime", date);

                    final String requestBodyRegister = jsonBodyUserRegister.toString();
                    Log.i("JSON REGISTER", requestBodyRegister);
                    connection.setDoOutput(true);
                    connection.getOutputStream().write(requestBodyRegister.getBytes());

                    HttpResponseCache responseCache = HttpResponseCache.install(getCacheDir(), 100000L);
                    int responseCodeRegister = connection.getResponseCode();
                    Log.i("ResponseCode: ", responseCodeRegister + "");

                    // daca operatia a avut succes, calculez headerul ce returneaza rezultatul
                    if(responseCodeRegister == 201){

                        int value = 0;
                        do{
                            Log.i("Responsemessage" + value, connection.getHeaderField(value) + " " + connection.getHeaderFieldKey(value));
                            if(connection.getHeaderFieldKey(value).equals("IsUser")){
                                returnedMessage = connection.getHeaderField(value);
                                Log.i("FINALIDFROMRESPONSE", returnedMessage);
                                value = -2;
                            }
                            value++;
                        }while(connection.getHeaderFieldKey(value) != null && connection.getHeaderField(value) != null && value > 0);

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogManager.destroyProgressDialog(pd);
                                Toast.makeText(getApplicationContext(), "Unable to login. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        System.out.println("Unable to POST");
                    }
                    connection.disconnect();

                    //daca se returneaza 1, autentificarea a avut succes. daca se returneaza 0, credentialele au fost introduse gresit.
                    if(returnedMessage.equals("1")){
                        Intent mainIntent = new Intent(getApplicationContext(), MainDrawer.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogManager.destroyProgressDialog(pd);
                                Toast.makeText(getApplicationContext(), "Welcome, " + userName, Toast.LENGTH_SHORT).show();
                            }
                        });
                        startActivity(mainIntent);
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogManager.destroyProgressDialog(pd);
                                Toast.makeText(getApplicationContext(), "Login failed! Wrong credential, try again!", Toast.LENGTH_SHORT).show();
                                etPassword.setText("");
                            }
                        });
                        System.out.println("Login failed! Wrong credential, try again!");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (SocketTimeoutException e){
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
    }

    public void SQLiteLogin(){
        EditText emailTxt = (EditText)findViewById(R.id.emailTxt);
        String email = emailTxt.getText().toString();
        EditText passwordTxt = (EditText)findViewById(R.id.passwordTxt);
        String password = passwordTxt.getText().toString();

        if(email.equals("") | password.equals("")){
            if(email.equals("")){
                emailTxt.setBackgroundColor(Color.RED);
            }
            if(passwordTxt.equals("")){
                passwordTxt.setBackgroundColor(Color.RED);
            }
        }else{
            String foundPassword = dbHelper.loginCheck(email);
            if(password.equals(foundPassword)){
                Toast.makeText(getApplication(), "Welcome, " + email + " !", Toast.LENGTH_SHORT).show();
                Intent mainIntent = new Intent(getApplicationContext(), MainDrawer.class);
                startActivity(mainIntent);
            }else{
                Toast failToast = Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT);
                failToast.show();
            }
        }

        passwordTxt.setText("");
    }

    public void exitApp() {
        ImageButton exitBtn = (ImageButton) findViewById(R.id.exitBtn);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplication(), "Goodbye", Toast.LENGTH_SHORT).show();
                exitCompletely();
            }
        });
    }

    public void exitCompletely() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
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
}
