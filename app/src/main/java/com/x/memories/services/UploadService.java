package com.x.memories.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.x.memories.R;
import com.x.memories.models.Event;
import com.x.memories.models.EventPost;
import com.x.memories.models.Post;
import com.x.memories.reusables.Utilities;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by AKINDE-PETERS on 11/28/2016.
 */

public class UploadService extends Service {
    Context context;
    Bitmap bitmap;
    String time, uid, caption;
    SharedPreferences preferences;
    Boolean privacy, multiple;
    NotificationManager notificationManager;
    DatabaseReference ref;
    DatabaseReference myRef;
    GeoFire geoFire;
    double latitude, longitude;
    Event event;
    int count;
    ArrayList<Bitmap> bitmaps = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ref = FirebaseDatabase.getInstance().getReference("geofire/photos");
        geoFire = new GeoFire(ref);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        uid = preferences.getString("LOGGEDIN_UID", "");
        caption = intent.getStringExtra("caption");
        privacy = intent.getBooleanExtra("privacy",false);
        multiple = intent.getBooleanExtra("multiple",false);
        latitude = intent.getDoubleExtra("lat",0.0);
        longitude = intent.getDoubleExtra("long",0.0);
        event = (Event)intent.getExtras().getSerializable("event");
        bitmap = Utilities.StringToBitMap(preferences.getString("cropped_bmp", ""));
        count = intent.getExtras().getInt("bitmap_count");
        time = intent.getStringExtra("time");
        initializeThread();
        return Service.START_NOT_STICKY;
    }

    private void initializeThread() {
        if(multiple){
            bitmaps.clear();
            for (int i = 0; i < count; i++){
                Bitmap bitmap = Utilities.StringToBitMap(preferences.getString("bmp_"+i, ""));
                bitmaps.add(bitmap);
                if(i == count - 1){
                    Log.d("BITMAP",""+bitmaps.size());
                    uploadAll(bitmaps);
                }
            }
        }else{
            upload(bitmap);
        }
    }

    private void uploadAll(ArrayList<Bitmap> bitmaps) {
        for(int i = 0; i < bitmaps.size(); i++){
            //Set notification information:
            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
            notificationBuilder.setOngoing(true).setSmallIcon(R.drawable.ic_notify).setColor(getResources().getColor(R.color.colorPrimaryDark)).setContentTitle("Posting moments...").setProgress(count, 0, false);

            //Send the notification:
            final Notification[] notification = {notificationBuilder.build()};
            notificationManager.notify(13, notification[0]);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmaps.get(i), null, null);
            Uri compressedUri =  Uri.parse(path);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imagesRef = storage.getReferenceFromUrl("gs://memories-ec966.appspot.com/").child("images");
            String localtime = String.valueOf(System.currentTimeMillis());
            final int finalI = i;
            imagesRef.child(localtime).putFile(compressedUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String photoUrl = taskSnapshot.getDownloadUrl().toString();
                            postToFirebase(photoUrl,multiple);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            if(taskSnapshot.getBytesTransferred() == taskSnapshot.getTotalByteCount()){
                                if(count == 1){
                                    notificationManager.cancel(13);
                                }else{
                                    notificationBuilder.setProgress(count, finalI, false).setContentText(""+(finalI + 1)+" of "+count);
                                    notification[0] = notificationBuilder.build();
                                    notificationManager.notify(13, notification[0]);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d("MOMENTS_SERVICE","Storage error");
                        }
                    });
            if(finalI == bitmaps.size() - 1){
                Log.d("SERVICE","Reach here");
            }
        }
    }


    private void upload(Bitmap bmp) {

        //Set notification information:
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_notify)
                .setColor(getResources().getColor(R.color.colorPrimaryDark))
                .setContentTitle("Posting your moment...")
                .setProgress(100, 0, false);

        //Send the notification:
        final Notification[] notification = {notificationBuilder.build()};
        notificationManager.notify(11, notification[0]);

        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bmp, null, null);
        Uri compressedUri =  Uri.parse(path);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imagesRef = storage.getReferenceFromUrl("gs://memories-ec966.appspot.com/").child("images");
        imagesRef.child(time).putFile(compressedUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String photoUrl = taskSnapshot.getDownloadUrl().toString();
                        postToFirebase(photoUrl, false);
//                        Toast.makeText(context, "Upload finish!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) (100 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                        Log.d("MOMENTS_PROGRESS",""+taskSnapshot.getBytesTransferred());
                        Log.d("MOMENTS_SIZE",""+taskSnapshot.getTotalByteCount());
                        notificationBuilder.setProgress(100, progress - 20, false)
                                .setContentText(""+progress+"%");


                        //Send the notification:
                        notification[0] = notificationBuilder.build();
                        notificationManager.notify(11, notification[0]);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d("MOMENTS_SERVICE","Storage error");
//                        Toast.makeText(context, "Something went wrong somewhere. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void postToFirebase(String photoUrl, final Boolean multiple) {

        String localtime = String.valueOf(System.currentTimeMillis());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(event != null){
            myRef = database.getReference().child("events").child(event.getId()).child("photos");
            EventPost post = new EventPost(photoUrl,event.getId(),uid,localtime,"Posted by "+preferences.getString("LOGGEDIN_NAME","Someone")+"\n"+caption);
            myRef.child(uid+"_"+localtime).setValue(post, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("MOMENTS_SERVICE","Error sele "+databaseError.getMessage());
                    } else {
                        Log.d("MOMENTS_SERVICE","Successful upload");
                        notificationManager.cancel(11);
                        notificationManager.cancel(13);
                    }
                }
            });
        }else{
            myRef = database.getReference().child("photos");
            geoFire.setLocation(uid + "_" + localtime, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    Log.d("MOMENTS_SERVICE","GeoFire done!");
                }
            });
            Post post = new Post(photoUrl,privacy,uid,localtime,"Posted by "+preferences.getString("LOGGEDIN_NAME","Someone")+"\n"+caption);
            myRef.child(uid+"_"+localtime).setValue(post, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("MOMENTS_SERVICE","Error sele "+databaseError.getMessage());
                    } else {
                        Log.d("MOMENTS_SERVICE","Successful upload");
                        notificationManager.cancel(11);
                    }
                }
            });
        }


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
