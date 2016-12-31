package com.x.memories.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.x.memories.PreviewActivity;
import com.x.memories.R;
import com.x.memories.adapters.FeedsAdapter;
import com.x.memories.models.Post;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by AKINDE-PETERS on 8/29/2016.
 */
public class PhotoFragment extends Fragment {

    public final String APP_TAG = "Moments";
    Context context;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FloatingActionButton add_btn;
    private static final int PICS_FROM_CAMERA = 0;
    String time, photoFileName;
    FirebaseDatabase database;
    SharedPreferences preferences;
    Query query;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView photo_list;
    ArrayList<Post> posts = new ArrayList<>();
    RelativeLayout photo_placeholder;
    DatabaseReference ref;
    GeoFire geoFire;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        database = FirebaseDatabase.getInstance();
        query = database.getReference("photos").orderByChild("time").limitToLast(100);
        ref = FirebaseDatabase.getInstance().getReference("geofire/photos");
        geoFire = new GeoFire(ref);
        mAuth = FirebaseAuth.getInstance();
        preferences  = PreferenceManager.getDefaultSharedPreferences(context);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Memories", "onAuthStateChanged:signed_in:" + user.getUid());
                    new CountDownTimer(500, 500) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            add_btn.show();
                        }
                    }.start();

                } else {
                    // User is signed out
                    Log.d("Memories", "onAuthStateChanged:signed_out");
                    add_btn.hide();
                    if(!photo_placeholder.isShown()){
                        photo_placeholder.setVisibility(View.VISIBLE);
                    }
                }
                // ...
            }

        };

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo, container, false);
        add_btn = (FloatingActionButton)v.findViewById(R.id.add_photo_btn);
        photo_placeholder = (RelativeLayout)v.findViewById(R.id.photo_placeholder);
        photo_list = (RecyclerView)v.findViewById(R.id.photo_list);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        photo_list.setItemAnimator(itemAnimator);

        photo_placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                10);
                }else{
                    capturePicture();
                }
            }
        });

        showPhotos();
        return v;
    }

    private void showPhotos() {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                posts.clear();
                for(DataSnapshot datasnapshot: dataSnapshot.getChildren()){
                    Post post = datasnapshot.getValue(Post.class);
                    posts.add(post);
                }

                Collections.reverse(posts);

                layoutManager = new GridLayoutManager(context,3);
                photo_list.setLayoutManager(layoutManager);
                adapter = new FeedsAdapter(context,posts,1);
                adapter.setHasStableIds(true);
                photo_list.setAdapter(adapter);
//                photo_list.addOnItemTouchListener(new FeedsAdapter.RecyclerTouchListener(context, photo_list, new FeedsAdapter.ClickListener() {
//                    @Override
//                    public void onClick(View view, final int position) {
//
//
//                    }
//
//                    @Override
//                    public void onLongClick(View view, int position) {
//
//                    }
//                }));


                //Hide loading screen
                if(photo_placeholder.isShown()){
                    photo_placeholder.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void capturePicture() {
        time = String.valueOf(System.currentTimeMillis());
        photoFileName = "moments_"+time+".jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Uri uri  = Uri.parse(Environment.getExternalStorageDirectory().getPath(), "memories_"+time+".jpg");
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent,PICS_FROM_CAMERA);
    }

    private Uri getPhotoFileUri(String fileName) {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    capturePicture();
                } else {
                    Toast.makeText(getActivity(),"Please grant permissions to take photos",Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case PICS_FROM_CAMERA:
//                File file = new File(Environment.getExternalStorageDirectory().getPath(), "memories_" + time + ".jpg");
                Uri imageUri = getPhotoFileUri(photoFileName);
                Intent intent = new Intent(context, PreviewActivity.class);
                intent.putExtra("image_uri", imageUri.toString());
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("time",time);
                startActivity(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
