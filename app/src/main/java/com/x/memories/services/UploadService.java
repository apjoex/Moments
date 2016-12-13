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
import com.x.memories.models.Post;
import com.x.memories.reusables.Utilities;

/**
 * Created by AKINDE-PETERS on 11/28/2016.
 */

public class UploadService extends Service {
    Context context;
    Bitmap bitmap;
    String time, uid, caption;
    SharedPreferences preferences;
    Boolean privacy;
    NotificationManager notificationManager;
    DatabaseReference ref;
    GeoFire geoFire;
    double latitude, longitude;

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
        latitude = intent.getDoubleExtra("lat",0.0);
        longitude = intent.getDoubleExtra("long",0.0);
        bitmap = Utilities.StringToBitMap(preferences.getString("cropped_bmp", ""));
        time = intent.getStringExtra("time");
        initializeThread();
        return Service.START_NOT_STICKY;
    }

    private void initializeThread() {
        upload(bitmap);
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
                        postToFirebase(photoUrl);
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

    private void postToFirebase(String photoUrl) {

        String localtime = String.valueOf(System.currentTimeMillis());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        geoFire.setLocation(uid + "_" + localtime, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.d("MOMENTS_SERVICE","GeoFire done!");
            }
        });
        Post post = new Post(photoUrl,privacy,uid,localtime,"Posted by "+preferences.getString("LOGGEDIN_NAME","Someone")+"\n"+caption);
        myRef.child("photos").child(uid+"_"+localtime).setValue(post, new DatabaseReference.CompletionListener() {
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
