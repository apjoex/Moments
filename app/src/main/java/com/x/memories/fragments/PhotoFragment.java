package com.x.memories.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.x.memories.models.Request;
import com.x.memories.reusables.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by AKINDE-PETERS on 8/29/2016.
 */
public class PhotoFragment extends Fragment {

    Context context;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FloatingActionButton add_btn;
    private static final int PICS_FROM_CAMERA = 0;
    String time;
    FirebaseDatabase database;
    SharedPreferences preferences;
    Query query;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView photo_list;
    ArrayList<Post> posts = new ArrayList<>();
    RelativeLayout photo_placeholder;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        database = FirebaseDatabase.getInstance();
        query = database.getReference("photos").orderByChild("time").limitToLast(100);
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

//    @Override
//    public void onResume() {
//        super.onResume();
//        showPhotos();
////        if(posts.size() > 0){
////            photo_placeholder.setVisibility(View.INVISIBLE);
////        }else{
////            photo_placeholder.setVisibility(View.VISIBLE);
////        }
//    }

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
                adapter = new FeedsAdapter(context,posts,1,photo_list,false);
                adapter.setHasStableIds(true);
                photo_list.setAdapter(adapter);

                photo_list.addOnItemTouchListener(new FeedsAdapter.RecyclerTouchListener(context, photo_list, new FeedsAdapter.ClickListener() {
                    @Override
                    public void onClick(View view, final int position) {

                        if(posts.get(position).getPrivacy() && !posts.get(position).getUid().equals(preferences.getString("LOGGEDIN_UID",""))){
                            AlertDialog dialog = new AlertDialog.Builder(context)
                                    .setTitle("Private moment")
                                    .setMessage("This is a private moment. You need permission to view this moment.")
                                    .setPositiveButton("REQUEST PERMISSION", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            final ProgressDialog progressDialog = ProgressDialog.show(context, null, "Requesting...", false, false);
                                            Request request = new Request(preferences.getString("LOGGEDIN_NAME","Someone"),preferences.getString("LOGGEDIN_UID",""), Utilities.getTime(),"photo","sent",posts.get(position).getUrl(),posts.get(position).getCaption());
                                            FirebaseDatabase.getInstance().getReference().child("notifications").child(posts.get(position).getUid()).child(request.getUid()+"_"+request.getTime()).setValue(request, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    Toast.makeText(context,"Request sent. You'll get notified when you are granted permission to view the moment",Toast.LENGTH_SHORT).show();
                                                    if(progressDialog.isShowing()){ progressDialog.dismiss(); }
                                                }
                                            });
                                        }
                                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).create();
                            dialog.show();
                        }else{
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("posts", posts);
                            bundle.putInt("position", position);

                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            SlideShowDialogFragment newFragment = SlideShowDialogFragment.newInstance();
                            newFragment.setArguments(bundle);
                            newFragment.show(ft, "slideshow");
                        }
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));


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
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        time = String.valueOf(System.currentTimeMillis());
        Uri uri  = Uri.parse("file:///sdcard/"+"memories_"+time+".jpg");
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent,PICS_FROM_CAMERA);
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
                File file = new File(Environment.getExternalStorageDirectory().getPath(), "memories_" + time + ".jpg");
                Uri imageUri = Uri.fromFile(file);
                Intent intent = new Intent(context, PreviewActivity.class);
                intent.putExtra("image_uri", imageUri.toString());
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
