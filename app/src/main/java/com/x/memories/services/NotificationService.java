package com.x.memories.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.x.memories.GrantedRequests;
import com.x.memories.R;
import com.x.memories.Requests;
import com.x.memories.models.Request;
import com.x.memories.reusables.Utilities;

import java.util.ArrayList;

/**
 * Created by AKINDE-PETERS on 9/4/2016.
 */
public class NotificationService extends Service {

    Context context;
    FirebaseDatabase database;
    DatabaseReference reference;
    ArrayList<Request> requests = new ArrayList<>();
    ArrayList<Request> accepted_requests = new ArrayList<>();
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String uid = sharedPref.getString("LOGGEDIN_UID", "");

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("notifications").child(uid);
        initializeThread();
        return Service.START_NOT_STICKY;
    }

    private void initializeThread() {
        getNotifications();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getNotifications() {
//        reference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Toast.makeText(context, dataSnapshot.getValue().toString(),Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                requests.clear();
                        for(DataSnapshot datasnapshot: dataSnapshot.getChildren()){
                            try{
                                Request request = datasnapshot.getValue(Request.class);
                                if(request.getStatus().equals("sent")){
                                    requests.add(request);
                                }else{
                                    accepted_requests.add(request);
                                }
                            }catch (Exception  e){
                                Log.d("CastError","could cast objects");
                            };
                        }
                if(!requests.isEmpty()) {
                    sendNotification(requests);
                }

                if(!accepted_requests.isEmpty()) {
                    sendAcceptedNotifications(accepted_requests);
                }

                Log.d("FirebaseNotify",dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final ArrayList<Request> requests) {
        Intent intent = new Intent(this, Requests.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
        final Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if(requests.size() > 1){
            notificationBuilder
                    .setSmallIcon(R.drawable.ic_notify)
                    .setContentTitle("Permission Requests")
                    .setContentText("You have " + requests.size() +" pending permission requests")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("You have " + requests.size() +" pending private requests"))
                    .setAutoCancel(true)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
        }else{
            Intent acceptIntent = new Intent(this, Requests.class);
            acceptIntent.putExtra("action","accept");
            PendingIntent actionPendingIntent = PendingIntent.getActivity(this, 1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent declineIntent = new Intent(this, Requests.class);
            declineIntent.putExtra("action","decline");
            PendingIntent declinePendingIntent = PendingIntent.getActivity(this, 2, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Create the action
            final NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(R.drawable.ic_accept, "Accept", actionPendingIntent).build();
            final NotificationCompat.Action declineAction = new NotificationCompat.Action.Builder(R.drawable.ic_close_white, "Decline", declinePendingIntent).build();

            ///

//            new AsyncTask<String, Void, Bitmap>() {
//                @Override
//                protected Bitmap doInBackground(String... strings) {
//                    InputStream in;
//                    try {
//                        URL url = new URL(requests.get(0).getPost_id());
//                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//                        connection.setDoInput(true);
//                        connection.connect();
//                        in = connection.getInputStream();
//                        return BitmapFactory.decodeStream(in);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                }
//                @Override
//                protected void onPostExecute(Bitmap result) {
//
//                    super.onPostExecute(result);
//                    try {
//
//
//                        Log.d("WEARABLE","Gobe no dey");
//
//                    } catch (Exception e) {
//                        Log.d("WEARABLE","Gobe dey");
//                        e.printStackTrace();
//                    }
//                }
//            }.execute();

            ///

//            Bitmap result = Glide.with(this)
//                    .load(requests.get(0).getPost_id())
//                    .asBitmap()
//                    .into(400, 400)
//                    .get();

            Notification secondPageNotification = new NotificationCompat.Builder(context)
                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(Utilities.StringToBitMap(requests.get(0).getBitmapString())))
                    .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                    .build();

            notificationBuilder
                    .setSmallIcon(R.drawable.ic_notify)
                    .setContentTitle("Permission Request")
                    .setContentText(requests.get(0).getName()+" requests to see your private moment")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(requests.get(0).getName()+" requests to see your private moment"))
                    .setAutoCancel(true)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .extend(new NotificationCompat.WearableExtender()
                            .addAction(acceptAction)
                            .addAction(declineAction)
                            .setBackground(Utilities.StringToBitMap(requests.get(0).getBitmapString()))
                            .addPage(secondPageNotification));
        }

        if (Build.VERSION.SDK_INT >= 21) notificationBuilder.setVibrate(new long[100]);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        requests.clear();

    }

    private void sendAcceptedNotifications(ArrayList<Request> requests) {
        Intent intent = new Intent(this, GrantedRequests.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("requests",requests);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notifBuilder
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle("Granted Requests")
                .setContentText("You have " + requests.size() +" granted requests")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("You have " + requests.size() +" granted requests"))
                .setAutoCancel(true)
                .setColor(getResources().getColor(R.color.colorPrimaryDark))
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);



        if (Build.VERSION.SDK_INT >= 21) notifBuilder.setVibrate(new long[100]);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1 /* ID of notification */, notifBuilder.build());

        requests.clear();
    }

}
