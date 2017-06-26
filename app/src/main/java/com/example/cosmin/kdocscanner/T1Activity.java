package com.example.cosmin.kdocscanner;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import Classes.Document;
import EmailAPI.SendMail;
import Requests.T1Request;
import Utils.DatabaseHelper;

public class T1Activity extends AppCompatActivity {

    DatabaseHelper dbHelper = new DatabaseHelper(this);
    private String nameExtra;
    private String surnameExtra;
    private String idExtra;
    private String addressExtra;

    TextView nameTextView;
    TextView surnameTextView;
    TextView idTextView;
    TextView addressTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.karrows);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if(extras != null) {
            autocompleteBasedOnOCR();
        }
        submitAction();
    }

    public void autocompleteBasedOnOCR(){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        boolean flag = extras.getBoolean("AutoCompleteFLAG", false);

        if(extras != null){
            if(flag){
                nameExtra = extras.getString("name");
                surnameExtra = extras.getString("surname");
                idExtra = extras.getString("id");
                addressExtra = extras.getString("address");

                nameTextView = (TextView) findViewById(R.id.nameTxt);
                surnameTextView = (TextView) findViewById(R.id.surnameTxt);
                idTextView = (TextView) findViewById(R.id.idTxt);
                addressTextView = (TextView) findViewById(R.id.addressTxt);

                nameTextView.setText(nameExtra);
                surnameTextView.setText(surnameExtra);
                idTextView.setText(idExtra);
                addressTextView.setText(addressExtra);
            }
        }

    }

    public void submitAction(){

        if(haveNetworkConnection()){
            //saveMetaMySQL();
            //T1WithoutSQL();
            createDocumentSQLite();

        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(T1Activity.this);
            builder.setMessage("Internet unavailable")
                    .setNegativeButton("OK", null)
                    .create()
                    .show();
        }
    }

    public void createDocumentSQLite(){
        Button submitBtn = (Button) findViewById(R.id.submitBtn);
        final EditText nameTxt = (EditText) findViewById(R.id.nameTxt);
        final EditText surnameTxt = (EditText) findViewById(R.id.surnameTxt);
        final EditText idTxt = (EditText) findViewById(R.id.idTxt);
        final EditText addressTxt = (EditText) findViewById(R.id.addressTxt);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    final String name = nameTxt.getText().toString();
                    final String surname = surnameTxt.getText().toString();
                    final String id = idTxt.getText().toString();
                    final String address = addressTxt.getText().toString();

                    if (!nameTxt.equals("") && !surnameTxt.equals("") && !idTxt.equals("") && !addressTxt.equals("")) {

                        Document document = new Document(name, surname, id, address, false);
                        dbHelper.createDocument(document);
                        Toast.makeText(getApplicationContext(), "Document created succesfully", Toast.LENGTH_SHORT).show();
                        Intent docReviewIntent = new Intent(T1Activity.this, DocumentActivity.class);
                        docReviewIntent.putExtra("name", name);
                        docReviewIntent.putExtra("surname", surname);
                        docReviewIntent.putExtra("id", id);
                        docReviewIntent.putExtra("address", address);

                        startActivity(docReviewIntent);
                        sendEmail(name, surname, id, address);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Please complete all fields in order to submit the document",
                                Toast.LENGTH_LONG)
                                .show();
                    }
            }
        });
    }

    public void T1WithoutSQL() {
        Button submitBtn = (Button) findViewById(R.id.submitBtn);
        final EditText nameTxt = (EditText) findViewById(R.id.nameTxt);
        final EditText surnameTxt = (EditText) findViewById(R.id.surnameTxt);
        final EditText idTxt = (EditText) findViewById(R.id.idTxt);
        final EditText addressTxt = (EditText) findViewById(R.id.addressTxt);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final String name = nameTxt.getText().toString();
                    final String surname = surnameTxt.getText().toString();
                    final String id = idTxt.getText().toString();
                    final String address = addressTxt.getText().toString();

                    if (!nameTxt.equals("") && !surnameTxt.equals("") && !idTxt.equals("") && !addressTxt.equals("")) {
                        Intent docReviewIntent = new Intent(T1Activity.this, DocumentActivity.class);
                        docReviewIntent.putExtra("name", name);
                        docReviewIntent.putExtra("surname", surname);
                        docReviewIntent.putExtra("id", id + "");
                        docReviewIntent.putExtra("address", address);

                        startActivity(docReviewIntent);
                        sendEmail(name, surname, id, address);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Please complete all fields in order to submit the document",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }catch(NumberFormatException nfEx){
                    Toast.makeText(getApplicationContext(),
                            "Please complete all fields in order to submit the document",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    public void saveMetaMySQL(){
        Button submitBtn = (Button) findViewById(R.id.submitBtn);
        final EditText nameTxt = (EditText) findViewById(R.id.nameTxt);
        final EditText surnameTxt = (EditText) findViewById(R.id.surnameTxt);
        final EditText idTxt = (EditText) findViewById(R.id.idTxt);
        final EditText addressTxt = (EditText) findViewById(R.id.addressTxt);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    final String name = nameTxt.getText().toString();
                    final String surname = surnameTxt.getText().toString();
                    final String id = idTxt.getText().toString();
                    final String address = addressTxt.getText().toString();

                    if (!nameTxt.equals("") && !surnameTxt.equals("") && !idTxt.equals("") && !addressTxt.equals("")) {
                        Response.Listener<String> listener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    boolean success = jsonObject.getBoolean("success");
                                    if (success) {
                                        Intent docReviewIntent = new Intent(T1Activity.this, DocumentActivity.class);
                                        docReviewIntent.putExtra("name", name);
                                        docReviewIntent.putExtra("surname", surname);
                                        docReviewIntent.putExtra("id", id + "");
                                        docReviewIntent.putExtra("address", address);

                                        startActivity(docReviewIntent);
                                        sendEmail(name, surname, id, address);
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(T1Activity.this);
                                        builder.setMessage("Adding document failed")
                                                .setNegativeButton("Retry", null)
                                                .create()
                                                .show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        T1Request t1Request = new T1Request(name, surname, id, address, listener);
                        RequestQueue queue = Volley.newRequestQueue(T1Activity.this);
                        queue.add(t1Request);


                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Please complete all fields in order to submit the document",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }catch(NumberFormatException nfEx){
                    Toast.makeText(getApplicationContext(),
                            "Please complete all fields in order to submit the document",
                            Toast.LENGTH_LONG)
                            .show();
                }

            }
        });
    }

    public void sendEmail(String name, String surname, String id, String address){

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        String email = "vilcan.cosmin@gmail.com";
        String subject = "New Document: " +name+ " was created!";
        String message = "La data de: "+date+
                " a fost creat documentul: "+name+
                "\n\n Surname: "+surname+
                "\n ID: "+ id+
                "\n Adresa: "+ address;

        SendMail sendMail = new SendMail(this, email, subject, message);
        sendMail.execute();
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
