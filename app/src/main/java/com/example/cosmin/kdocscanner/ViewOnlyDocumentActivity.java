package com.example.cosmin.kdocscanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewOnlyDocumentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_only_document);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.karrows);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        createViewOfDocument();
        returnToHistory();
    }

    public void createViewOfDocument(){
        String name = getIntent().getStringExtra("Name");
        String surname = getIntent().getStringExtra("Surname");
        String id = getIntent().getStringExtra("ID");
        String address = getIntent().getStringExtra("Address");
        boolean sign = getIntent().getBooleanExtra("Sign", false);
        TextView nameTextView = (TextView) findViewById(R.id.textView26);
        TextView surnameTextView = (TextView) findViewById(R.id.textView99);
        TextView idTextView = (TextView) findViewById(R.id.textView28);
        TextView addressTextView = (TextView) findViewById(R.id.textView29);
        TextView signedTextView = (TextView) findViewById(R.id.textView30);
        nameTextView.setText(name);
        surnameTextView.setText(surname);
        idTextView.setText(String.valueOf(id));
        addressTextView.setText(address);
        if(sign) {
            signedTextView.setText("YES");
        }else{
            signedTextView.setText("NO");
        }
    }

    public void returnToHistory(){
        Button okBtn = (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent historyIntent = new Intent(ViewOnlyDocumentActivity.this, DocHistoryActivity.class);
                startActivity(historyIntent);
                finish();
            }
        });
    }
}
