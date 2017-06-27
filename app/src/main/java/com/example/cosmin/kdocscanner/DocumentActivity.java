package com.example.cosmin.kdocscanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import Utils.ConvertUtils;
import Utils.DatabaseHelper;
import Utils.ProgressDialogManager;

public class DocumentActivity extends AppCompatActivity {

    DatabaseHelper dbHelper = new DatabaseHelper(this);
    ConvertUtils convertUtils = new ConvertUtils();

    TextView nameText;
    TextView surnameText;
    TextView idText;
    TextView addressText;
    TextView birthdayText;
    TextView nationalityText;
    TextView issuingDateText;
    TextView issuedByText;
    TextView CNPText;

    String templateFileLocation = "/storage/emulated/0/documenteScan/docTemplate.txt";
    String PdfDestination = "/storage/emulated/0/PDFDocs";

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.karrows);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button doneDoc = (Button) findViewById(R.id.doneBtn);

        nameText = (TextView) findViewById(R.id.nameTextView);
        surnameText = (TextView) findViewById(R.id.surnameTextView);
        idText = (TextView) findViewById(R.id.idTextView);
        addressText = (TextView) findViewById(R.id.addressTextView);
        birthdayText = (TextView) findViewById(R.id.birthdateTextView);
        nationalityText = (TextView) findViewById(R.id.nationalityTextView);
        issuingDateText = (TextView) findViewById(R.id.issuingdateTextView);
        issuedByText = (TextView) findViewById(R.id.issuedbyTextView);
        CNPText = (TextView) findViewById(R.id.cnpTextView);

        createDocReview();
        signDocument();
        toggleButtons();
        returningToApp();
        createDocument();
    }

    public void createDocReview(){
        String name = getIntent().getStringExtra("name");
        String surname = getIntent().getStringExtra("surname");
        String id = getIntent().getStringExtra("id");
        String address = getIntent().getStringExtra("address");
        String nationality = getIntent().getStringExtra("nationality");
        String birthdate = getIntent().getStringExtra("birthdate");
        String issuingdate = getIntent().getStringExtra("issuingdate");
        String issuedby = getIntent().getStringExtra("issuedby");
        String cnp = getIntent().getStringExtra("cnp");
        byte[] bitmapArray = getIntent().getByteArrayExtra("byteArray");

        nameText.setText(name);
        surnameText.setText(surname);
        idText.setText(id);
        //addressText.setText(address.substring(0, 10) + "...");
        addressText.setText(address);
        nationalityText.setText(nationality);
        birthdayText.setText(birthdate);
        issuingDateText.setText(issuingdate);
        issuedByText.setText(issuedby);
        CNPText.setText(cnp);
        ImageView signImageView = (ImageView) findViewById(R.id.signImageView);
       try{
           Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
           signImageView.setImageBitmap(bitmap);
       }catch (Exception ex){
           ex.printStackTrace();
       }

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        TextView curentDateTextView = (TextView) findViewById(R.id.dateTextView);
        curentDateTextView.setText(date);
    }

    public void signDocument(){
        Button signDoc = (Button) findViewById(R.id.signButton);
        signDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nameText.getText().toString();
                final String surname = surnameText.getText().toString();
                String id = idText.getText().toString();
                final String address = getIntent().getStringExtra("address");
                final String birthDate = birthdayText.getText().toString();
                final String nationality = nationalityText.getText().toString();
                final String issuingDate = issuingDateText.getText().toString();
                final String issuedBy = issuedByText.getText().toString();
                final String cnp = CNPText.getText().toString();

                Intent signIntent = new Intent(DocumentActivity.this, SignActivity.class);
                signIntent.putExtra("name", name);
                signIntent.putExtra("surname", surname);
                signIntent.putExtra("id", id + "");
                signIntent.putExtra("address", address);
                signIntent.putExtra("nationality", nationality);
                signIntent.putExtra("birthdate", birthDate);
                signIntent.putExtra("issuingdate", issuingDate);
                signIntent.putExtra("issuedby", issuedBy);
                signIntent.putExtra("cnp", cnp);
                signIntent.putExtra("FLAG", "document");
                startActivity(signIntent);
            }
        });
    }

    public void createDocument(){
        Button doneDoc = (Button) findViewById(R.id.doneBtn);
        doneDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //createTextDocumentFromTemplate();
                pd = ProgressDialogManager.initiateProgressDialog("Creating PDF Document...", DocumentActivity.this);
                try {
                    createPDFDocument();
                    ProgressDialogManager.destroyProgressDialog(pd);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    ProgressDialogManager.destroyProgressDialog(pd);
                } catch (DocumentException e) {
                    e.printStackTrace();
                    ProgressDialogManager.destroyProgressDialog(pd);
                } catch (IOException e) {
                    e.printStackTrace();
                    ProgressDialogManager.destroyProgressDialog(pd);
                }
                ProgressDialogManager.destroyProgressDialog(pd);
            }
        });
    }

    public void createPDFDocument() throws IOException, DocumentException {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    final String name = nameText.getText().toString();
                    final String surname = surnameText.getText().toString();
                    String id = idText.getText().toString();
                    final String address = getIntent().getStringExtra("address");
                    final String birthDate = birthdayText.getText().toString();
                    final String nationality = nationalityText.getText().toString();
                    final String issuingDate = issuingDateText.getText().toString();
                    final String issuedBy = issuedByText.getText().toString();
                    final String cnp = CNPText.getText().toString();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pd = ProgressDialogManager.initiateProgressDialog("Creating PDF Document...", DocumentActivity.this);
                        }
                    });

                    //1. Setare locatie scriere PDF
                    File pdfFile = new File(PdfDestination + "/" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + ".pdf");
                    OutputStream outputStream = new FileOutputStream(pdfFile);

                    //2. Initializre document
                    Document document = new Document(PageSize.A4);
                    PdfWriter.getInstance(document, outputStream);

                    //3. Deschidere document
                    document.open();

                    //4. Construire continut document

                    //4.0 whitespaces

                    StringBuilder stringBuilderDefault = new StringBuilder("\n\n\n\n\n");
                    Paragraph p0 = new Paragraph(stringBuilderDefault.toString());

                    //4.1 Paragraf 1
                    StringBuilder stringBuilderP1 = new StringBuilder("Domnule Presedinte");
                    stringBuilderP1.append("\n");
                    stringBuilderP1.append("\n");

                    Paragraph p1 = new Paragraph(stringBuilderP1.toString());
                    p1.setFont(new Font(Font.FontFamily.TIMES_ROMAN, Font.BOLD));
                    p1.setAlignment(Element.ALIGN_LEFT);
                    p1.setIndentationLeft(100);

                    //4.2 Paragraf 2
                    StringBuilder stringBuilderP2 = new StringBuilder("Subsemnatul ");
                    stringBuilderP2.append(surname + " ");
                    stringBuilderP2.append(name + ", ");
                    stringBuilderP2.append("domiciliat in ");
                    stringBuilderP2.append(address.replace("\n", "") + ", ");
                    stringBuilderP2.append("posesor al CI seria ");
                    stringBuilderP2.append(id.substring(0, 2) + ", numarul ");
                    stringBuilderP2.append(id.substring(2, 8) + ", ");
                    stringBuilderP2.append("eliberat la data de ");
                    stringBuilderP2.append(issuingDate + " ");
                    stringBuilderP2.append("de catre ");
                    stringBuilderP2.append(issuedBy);
                    stringBuilderP2.append("\n");

                    Paragraph p2 = new Paragraph(stringBuilderP2.toString());
                    p2.setAlignment(Element.ALIGN_LEFT);
                    p2.setIndentationLeft(25);
                    p2.setFirstLineIndent(75);

                    //4.3 Paragraf 3
                    StringBuilder stringBuilderP3 = new StringBuilder("Va rog sa...");
                    stringBuilderP3.append("\n");

                    Paragraph p3 = new Paragraph(stringBuilderP3.toString());
                    p3.setAlignment(Element.ALIGN_LEFT);
                    p3.setIndentationLeft(25);
                    p3.setFirstLineIndent(75);

                    //4.4 Paragraf 4
                    StringBuilder stringBuilderP4 = new StringBuilder("Data");
                    stringBuilderP4.append("\n");

                    Paragraph p4 = new Paragraph(stringBuilderP4.toString());
                    p4.setAlignment(Element.ALIGN_LEFT);
                    p4.setIndentationLeft(25);
                    p4.setFirstLineIndent(75);

                    StringBuilder stringBuilderP41 = new StringBuilder(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                    stringBuilderP41.append("\n");

                    Paragraph p41 = new Paragraph(stringBuilderP41.toString());
                    p41.setAlignment(Element.ALIGN_LEFT);
                    p41.setIndentationLeft(25);
                    p41.setFirstLineIndent(75);

                    //4.5 Paragraf 5
                    StringBuilder stringBuilderP5 = new StringBuilder("Semnatura");
                    stringBuilderP5.append("\n");

                    Paragraph p5 = new Paragraph(stringBuilderP5.toString());
                    p5.setAlignment(Element.ALIGN_RIGHT);
                    p5.setIndentationRight(25);
                    p5.setFirstLineIndent(75);

                    //4.6 Paragraf 6
                    ImageView signImageView = (ImageView) findViewById(R.id.signImageView);
                    Bitmap signedBitmap = ((BitmapDrawable) signImageView.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    signedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Image signature = Image.getInstance(stream.toByteArray());
                    signature.setAlignment(Element.ALIGN_RIGHT);
                    signature.setIndentationRight(25);
                    signature.scalePercent(10);

                    //5. Adagare continut la document si finalizarea acestuia
                    document.add(p0);
                    document.add(p1);
                    document.add(new Paragraph("\n\n"));
                    document.add(p2);
                    document.add(new Paragraph("\n"));
                    document.add(p3);
                    document.add(new Paragraph("\n\n\n\n\n\n\n"));
                    document.add(p4);
                    document.add(p41);
                    document.add(new Paragraph("\n\n\n"));
                    document.add(p5);
                    document.add(signature);
                    document.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressDialogManager.destroyProgressDialog(pd);
                            Toast.makeText(getApplicationContext(), "PDF Document created!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent mainIntent = new Intent(DocumentActivity.this, MainDrawer.class);
                    startActivity(mainIntent);
                } catch (FileNotFoundException e) {
                    ProgressDialogManager.destroyProgressDialog(pd);
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    ProgressDialogManager.destroyProgressDialog(pd);
                    e.printStackTrace();
                } catch (IOException e) {
                    ProgressDialogManager.destroyProgressDialog(pd);
                    e.printStackTrace();
                } catch (BadElementException e) {
                    ProgressDialogManager.destroyProgressDialog(pd);
                    e.printStackTrace();
                } catch (DocumentException e) {
                    ProgressDialogManager.destroyProgressDialog(pd);
                    e.printStackTrace();
                }
            }
        });
    }
    public void createTextDocumentFromTemplate(){

                final String name = nameText.getText().toString();
                final String surname = surnameText.getText().toString();
                String id = idText.getText().toString();
                final String address = getIntent().getStringExtra("address");
                final String birthDate = birthdayText.getText().toString();
                final String nationality = nationalityText.getText().toString();
                final String issuingDate = issuingDateText.getText().toString();
                final String issuedBy = issuedByText.getText().toString();
                final String cnp = CNPText.getText().toString();

                File templateFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/docTemplate.txt");
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
                        if(!s.equals(",")) {
                            fullFileContent += "\n" + s;
                        }else{
                            fullFileContent += "\n" + s;
                        }
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

                    FileWriter fileWriter = new FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + surname + " "+ replacetodayarg + ".txt");
                    fileWriter.write(replacedFullFileContent);
                    fileWriter.close();

                    Toast.makeText(getApplicationContext(), "Succesfully created document", Toast.LENGTH_SHORT).show();

                    Intent mainIntent = new Intent(DocumentActivity.this, MainDrawer.class);
                    startActivity(mainIntent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

    public void toggleButtons(){
        boolean alreadySigned = getIntent().getBooleanExtra("offSign", false);
        Button signDoc = (Button) findViewById(R.id.signButton);
        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        if(alreadySigned == true){
            signDoc.setEnabled(false);
            doneBtn.setEnabled(true);
        }else{
            signDoc.setEnabled(true);
            doneBtn.setEnabled(false);
        }
    }

    public void returningToApp(){
        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainMenuIntent = new Intent(DocumentActivity.this, MainDrawer.class);
                startActivity(mainMenuIntent);
            }
        });
    }
}
