package Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import Classes.Document;
import Classes.UserAccount;

public class DatabaseHelper extends SQLiteOpenHelper {

    SQLiteDatabase db;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "scannerDB.db";

    private static final String USER_TABLE = "useraccounts";
    private static final String DOC_TABLE = "documents";

    private static final String CREATE_USERACCOUNTS = "create table "+USER_TABLE+"(email text unique not null, password text not null, name text not null, age id not null, location text not null, signature blob);";
    private static final String CREATE_DOC = "create table "+DOC_TABLE+"(name text not null, surname text not null, id id not null, address text not null, flag id not null, signature blob);";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void registerUser(UserAccount userAccount, byte[] signArray){
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", userAccount.getEmail());
        contentValues.put("password", userAccount.getPassword());
        contentValues.put("name", userAccount.getName());
        contentValues.put("age", userAccount.getAge());
        contentValues.put("location", userAccount.getLocation());
        contentValues.put("signature", signArray);

        db.insert(USER_TABLE, null, contentValues);
        db.close();

    }

    public void createDocument(Document document){
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", document.getName());
        contentValues.put("surname", document.getSurname());
        contentValues.put("id", document.getID());
        contentValues.put("address", document.getAddress());
        if(document.getIsSigned() == true){
            contentValues.put("flag", 1);
        }else{
            contentValues.put("flag", 0);
        }

        db.insert(DOC_TABLE, null, contentValues);
        db.close();
    }

    public int updateDocument(String id, byte[] signArray){
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("flag", 1);
        contentValues.put("signature", signArray);
        return db.update(DOC_TABLE, contentValues, "id = ?",
                         new String[]{String.valueOf(id)});
    }

    public String loginCheck(String email){
        db = this.getReadableDatabase();
        String selectQuery = "select email, password from " + USER_TABLE;

        Cursor cursor = db.rawQuery(selectQuery, null);
        String mail;
        String password = "";
        if(cursor.moveToFirst()){
            do{
                mail = cursor.getString(0);
                if(mail.equals(email)){
                    password = cursor.getString(1);
                    break;
                }
            }while(cursor.moveToNext());
        }
        return password;
    }

    public List<Document> getAllDocuments(){
        List<Document> documentList = new ArrayList<Document>();
        String selectQuery = "SELECT * FROM " + DOC_TABLE;

        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                Document document = new Document();
                document.setName(cursor.getString(0));
                document.setSurname(cursor.getString(1));
                document.setID(cursor.getString(2));
                document.setAddress(cursor.getString(3));
                if(cursor.getInt(4) == 0){
                    document.setIsSigned(false);
                }else{
                    document.setIsSigned(true);
                }
                documentList.add(document);
            }while(cursor.moveToNext());
        }
        return documentList;
    }

    public Document getParticularDocument(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Document document;
        Cursor cursor = db.query(DOC_TABLE, new String[]{"name", "surname", "id", "address", "flag", "signature"}, "id=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if(cursor != null)
            cursor.moveToFirst();
        if(cursor.getInt(4) == 0){
            document = new Document(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), false);
        }else{
            document = new Document(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), true);
        }


        return document;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERACCOUNTS);
        db.execSQL(CREATE_DOC);
        this.db = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropUA = "DROP TABLE IF EXISTS " + USER_TABLE;
        String dropDOC = "DROP TABLE IF EXISTS " + DOC_TABLE;
        db.execSQL(dropUA);
        db.execSQL(dropDOC);
        this.onCreate(db);
    }
}
