package com.example.cosmin.kdocscanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_confirmation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.karrows);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String email = getIntent().getStringExtra("email");
        TextView emailTxtView = (TextView) findViewById(R.id.textView12);
        emailTxtView.setText(email);

        continueAction();
    }

    public void continueAction(){
        Button continueBtn = (Button) findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(LoginConfirmationActivity.this, MainDrawer.class);
                startActivity(mainIntent);
            }
        });
    }
}
