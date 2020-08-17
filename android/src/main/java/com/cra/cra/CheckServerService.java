package com.cra.cra;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class CheckServerService extends Service {
    DB_Local db_local;
    DatabaseReference firebaseReference;
    DatabaseReference firebaseReferencebook;
    String Book_name;
    String Book_Category;
    String Book_Voice;
    String Book_Description;
    int Post_Position;
    Boolean stop_thread;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        db_local = new DB_Local(this);
        firebaseReference = FirebaseDatabase.getInstance().getReference("Accounts");
        firebaseReferencebook = FirebaseDatabase.getInstance().getReference("Books");
        stop_thread = false;
        //start_using_server_firebase();
        //db_local.start_server();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("CRA", "CRA", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("CRA");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        Book_name = extras.getString("Book_Name");
        Book_Category = extras.getString("Book_Category");
        Book_Voice = extras.getString("Book_Voice");
        Book_Description = extras.getString("Book_Description");
        Post_Position = extras.getInt("Post_Position");
        new Thread(new Runnable(){
            public void run() {
                while(!stop_thread)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Query query = firebaseReferencebook.orderByChild("name").equalTo(Book_name);
                    query.addListenerForSingleValueEvent(valueEventListener);
                }

            }
        }).start();



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (stop_thread)//success
        {
            //db_local.end_server();
            //Stop_using_server_firebase();
            show_notification();
        }
    }

    private void show_notification()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"CRA")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Book Conversion")
                .setContentText("Your book '" + Book_name.substring(0,Book_name.length() - 7) + "' has been converted successfully!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Your book '" + Book_name.substring(0,Book_name.length() - 7) + "' has been converted successfully!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent notify_intent = new Intent(this, BookInfoAndDownloadActivity.class);
        notify_intent.putExtra("Book_Name",Book_name);
        notify_intent.putExtra("Book_Category",Book_Category);
        notify_intent.putExtra("Book_Voice",Book_Voice);
        notify_intent.putExtra("Book_Description",Book_Description);
        notify_intent.putExtra("Post_Position",Post_Position);
        notify_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notify_intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }
    private void start_using_server_firebase()
    {
        String id = db_local.get_curr_user_key();
        String FName = db_local.get_curr_user_fname();
        String LName = db_local.get_curr_user_lname();
        String Email = db_local.get_curr_user_email();
        String Pass = db_local.get_curr_user_pass();
        String Book_names = db_local.get_curr_user_books();
        Account a = new Account(FName,LName,Email,Pass,"TRUE",id,Book_names);
        firebaseReference.child(id).setValue(a);
    }

    private void Stop_using_server_firebase()
    {
        String id = db_local.get_curr_user_key();
        String FName = db_local.get_curr_user_fname();
        String LName = db_local.get_curr_user_lname();
        String Email = db_local.get_curr_user_email();
        String Pass = db_local.get_curr_user_pass();
        String Book_names = db_local.get_curr_user_books();
        Account a = new Account(FName,LName,Email,Pass,"FALSE",id,Book_names);
        firebaseReference.child(id).setValue(a);
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String up = snapshot.child("uploaded").getValue().toString();
                    if (up.equals("TRUE"))
                    {
                        stop_thread = true;
                        stopSelf();
                    }

                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
