package com.example.cosmin.kdocscanner;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import Classes.Document;
import EmailAPI.SendMail;
import Utils.DatabaseHelper;
import Utils.ExpandableListAdapter;

public class DocHistoryActivity extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    DatabaseHelper dbHelper = new DatabaseHelper(this);

    ArrayList<String> signed = new ArrayList<String>();
    ArrayList<String> unsigned = new ArrayList<String>();

    private static int prev = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.karrows);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        expListView = (ExpandableListView) findViewById(R.id.expandableLv);
        listData();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);

        childExpandedListener();
        groupExpandedListener();
        groupCollapsedListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dochistory_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.gmailBtn:
                sendEmail(signed, unsigned);
                break;
            case R.id.otherBtn:
                Toast.makeText(getApplicationContext(), "OTHER", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void listData(){
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        //Headers
        listDataHeader.add("Signed Documents");
        listDataHeader.add("Unsigned Documents");

        //Items

        List<Document> documents = dbHelper.getAllDocuments();

        for(Document doc : documents){
            if(doc.getIsSigned()){
                signed.add("ID: "+doc.getID()+ "-Name: "+doc.getName() + " " + doc.getSurname());
                listDataChild.put(listDataHeader.get(0), signed);
            }else{
                unsigned.add("ID: "+doc.getID()+ "-Name: "+doc.getName() + " " + doc.getSurname());
                listDataChild.put(listDataHeader.get(1), unsigned);
            }
        }
        listDataChild.put(listDataHeader.get(0), signed);
        listDataChild.put(listDataHeader.get(1), unsigned);

    }

    public void sendEmail(ArrayList<String> signedDocs, ArrayList<String> unsignedDocs){

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

        String emailAddress = "vilcan.cosmin@gmail.com";
        String subject = "Document History " + date;

        StringBuilder messageString = new StringBuilder("Document History");
        messageString.append("\n");
        messageString.append("\n");

        messageString.append("Signed Documents: ");
        messageString.append("\n");

        for(String signedDoc : signedDocs){
            messageString.append(signedDoc);
            messageString.append("\n");
        }

        messageString.append("\n");
        messageString.append("Unsigned Documents: ");
        messageString.append("\n");

        for(String unsignedDoc : unsignedDocs){
            messageString.append(unsignedDoc);
            messageString.append("\n");
        }

        SendMail sendMail = new SendMail(this, emailAddress, subject, messageString.toString());
        sendMail.execute();
    }

    public void childExpandedListener(){
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String childContent = (String) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
                String[] splitedChildContent = childContent.split("-");
                String idPart = splitedChildContent[0];
                String idValue = idPart.substring(4);
                Document document = dbHelper.getParticularDocument(idValue);
                String retrievedName = document.getName();
                String retrievedSurname = document.getSurname();
                String retrievedID = document.getID();
                String retrievedAddress = document.getAddress();
                boolean retrievedSigned = document.getIsSigned();

                Intent docViewIntent = new Intent(DocHistoryActivity.this, ViewOnlyDocumentActivity.class);
                docViewIntent.putExtra("Name", retrievedName);
                docViewIntent.putExtra("Surname", retrievedSurname);
                docViewIntent.putExtra("ID", retrievedID);
                docViewIntent.putExtra("Address", retrievedAddress);
                docViewIntent.putExtra("Sign", retrievedSigned);
                startActivity(docViewIntent);
                return false;
            }
        });
    }

    public void groupExpandedListener(){
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if(prev != -1)
                {
                    expListView.collapseGroup(prev);
                }
                prev = groupPosition;
            }
        });
    }

    public void groupCollapsedListener(){
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });
    }
}
