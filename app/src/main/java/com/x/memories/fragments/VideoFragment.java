package com.x.memories.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.x.memories.R;
import com.x.memories.adapters.FeedsAdapter;
import com.x.memories.models.Post;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by AKINDE-PETERS on 8/29/2016.
 */
public class VideoFragment extends Fragment {

    Context context;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FloatingActionButton add_btn;
    private static final int VIDEO_CAPTURED = 1;
    String time;
    Uri uri;
    LayoutInflater layoutInflater;
    String uid, localtime;
    FirebaseDatabase database;
    Query query;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView video_list;
    ArrayList<Post> posts = new ArrayList<>();
    RelativeLayout video_placeholder;
    Boolean privacy = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        database = FirebaseDatabase.getInstance();
        query = database.getReference("videos").limitToLast(100);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Memories", "onAuthStateChanged:signed_in:" + user.getUid());
                    uid = user.getUid();
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
                }
                // ...
            }

        };

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layoutInflater = inflater;
        View v = inflater.inflate(R.layout.fragment_video, container, false);
        video_placeholder = (RelativeLayout)v.findViewById(R.id.video_placeholder);
        video_list = (RecyclerView)v.findViewById(R.id.video_list);
        add_btn = (FloatingActionButton)v.findViewById(R.id.add_video_btn);

        video_placeholder.setOnClickListener(new View.OnClickListener() {
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
                    captureVideo();
                }
            }
        });

        showVideos();
        return v;
    }

    private void showVideos() {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                posts.clear();
                for(DataSnapshot datasnapshot: dataSnapshot.getChildren()){
                    Post post = datasnapshot.getValue(Post.class);
                    posts.add(post);
                }

                Collections.reverse(posts);

                layoutManager = new LinearLayoutManager(context);
                video_list.setLayoutManager(layoutManager);
                adapter = new FeedsAdapter(context,posts,2);
                video_list.setAdapter(adapter);


                //Hide loading screen
                if(video_placeholder.isShown()){
                    video_placeholder.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void captureVideo() {
        Intent captureVideoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
        time = String.valueOf(System.currentTimeMillis());
        File mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/memories_"+time+".mp4");
        Uri videoUri = Uri.fromFile(mediaFile);
        captureVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        captureVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
        captureVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(captureVideoIntent, VIDEO_CAPTURED);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureVideo();
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
            case VIDEO_CAPTURED:
                File file = new File(Environment.getExternalStorageDirectory().getPath(), "memories_" + time + ".mp4");
                uri = Uri.fromFile(file);

                View captionView = layoutInflater.inflate(R.layout.caption_layout, null);
                final EditText captionText = (EditText)captionView.findViewById(R.id.caption_box);
                SwitchCompat priv_switch = (SwitchCompat)captionView.findViewById(R.id.switcher);
                priv_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        privacy = b;
                    }
                });
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setView(captionView)
                        .setCancelable(false)
                        .setPositiveButton("POST", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final ProgressDialog progressDialog = ProgressDialog.show(context,null,"Posting your moment...",false,false);
                                postVideo(uri, captionText.getText().toString(), progressDialog);
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                uri = null;
                                dialogInterface.dismiss();
                            }
                        }).create();
                dialog.show();

//                Intent intent = new Intent(context, PreviewActivity.class);
//                intent.putExtra("image_uri", imageUri.toString());
//                intent.putExtra("time",time);
//                startActivity(intent);
        }
    }

    private void postVideo(Uri uri, final String caption, final ProgressDialog progressDialog) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imagesRef = storage.getReferenceFromUrl("gs://memories-ec966.appspot.com/").child("videos");
        imagesRef.child(time).putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String videoUrl = taskSnapshot.getDownloadUrl().toString();
                        postToFirebase(videoUrl, caption, progressDialog);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        if(progressDialog.isShowing()) {progressDialog.dismiss();}
                        Toast.makeText(context, "Something went wrong somewhere. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void postToFirebase(String videoUrl, String caption, final ProgressDialog progressDialog) {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
        localtime = date.format(currentLocalTime);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Post post = new Post(videoUrl,privacy,uid,localtime,caption);
        myRef.child("videos").child(uid+"_"+localtime).setValue(post, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(progressDialog.isShowing()){ progressDialog.dismiss(); }
                if (databaseError != null) {
                    Toast.makeText(context, "Something went wrong somewhere. Please try again", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
