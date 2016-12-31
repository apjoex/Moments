package com.x.memories.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.x.memories.CreateEvent;
import com.x.memories.DialogActivity;
import com.x.memories.PreviewActivity;
import com.x.memories.R;
import com.x.memories.adapters.EventsPhotoAdapter;
import com.x.memories.models.Event;
import com.x.memories.models.EventPost;
import com.x.memories.reusables.Utilities;
import com.x.memories.services.UploadService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by AKINDE-PETERS on 11/29/2016.
 */

public class EventFragment extends Fragment {

    public final String APP_TAG = "Moments";
    Context context;
    AppCompatButton join_btn, create_btn;
    View join_event_view;
    SharedPreferences preferences;
    RelativeLayout event_showbox, event_empty, event_pics;
    EventsPhotoAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView photo_list;
    TextView event_name, tag;
    Event event;
    String time, photoFileName;
    ImageView add_event_photo, exit_event, share_event;
    FirebaseDatabase database;
    Query query;
    ProgressBar loading;
    ArrayList<EventPost> posts = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        database = FirebaseDatabase.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event, container, false);
        join_event_view = inflater.inflate(R.layout.join_event,null);
        join_btn = (AppCompatButton)v.findViewById(R.id.join_btn);
        create_btn = (AppCompatButton)v.findViewById(R.id.create_btn);
        event_showbox = (RelativeLayout)v.findViewById(R.id.event_showbox);
        event_empty = (RelativeLayout)v.findViewById(R.id.event_empty);
        event_pics = (RelativeLayout)v.findViewById(R.id.event_pics);
        event_name = (TextView)v.findViewById(R.id.event_name);
        tag = (TextView)v.findViewById(R.id.tag);
        add_event_photo = (ImageView)v.findViewById(R.id.add_event_photo);
        share_event = (ImageView)v.findViewById(R.id.share_event);
        exit_event = (ImageView)v.findViewById(R.id.exit_event);
        photo_list = (RecyclerView)v.findViewById(R.id.photo_list);
        loading = (ProgressBar)v.findViewById(R.id.loading);

        ColorStateList stateList =  ColorStateList.valueOf(Color.WHITE);
        join_btn.setSupportBackgroundTintList(stateList);
        create_btn.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        create_btn.setTextColor(getResources().getColor(R.color.colorPrimary));

        event_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 8);
                }else{
                    showJoinDialog();
                }

            }
        });

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, CreateEvent.class));
            }
        });

        return  v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(preferences.getBoolean("Event_active",false)){
            event_showbox.setVisibility(View.VISIBLE);
        }else{
            event_showbox.setVisibility(View.INVISIBLE);
        }

        //Get current event
        event = Utilities.getCurrentEvent(preferences);
        if(event!=null){
            event_name.setText(event.getName());
            showPhotos();
        }

        add_event_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence[] choices = {"Take photo","Add from gallery"};
                AlertDialog addDialog = new AlertDialog.Builder(context)
                        .setItems(choices, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){
                                    case 0:
                                        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(getActivity(),
                                                    new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    9);
                                        }else{
                                            capturePicture();
                                        }
                                        break;
                                    case 1:
                                        Intent intent = new Intent();
                                        intent.setType("image/*");
                                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 11);
                                        break;
                                }
                            }
                        }).create();
                addDialog.show();
            }
        });

        share_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                if(event.getProtect()){
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Join this event on Moments with passcode "+event.getCode()+"! Tap on the link below \nhttp://moments.app/?event="+event.getId());
                }else{
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Join this event on Moments! Tap on the link below \nhttp://moments.app/?event="+event.getId());
                }
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share event link"));
            }
        });

        exit_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(context).setTitle("Exit event?").setMessage("Are you sure you want to exit this event?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("Event_details","");
                                editor.putBoolean("Event_active",false);
                                editor.apply();
                                dialogInterface.dismiss();
                                onResume();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create();
                dialog.show();
            }
        });

    }

    private void showPhotos() {
        query = database.getReference("events").child(event.getId()).child("photos").orderByChild("time");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                posts.clear();
                for(DataSnapshot datasnapshot: dataSnapshot.getChildren()){
                    EventPost post = datasnapshot.getValue(EventPost.class);
                    posts.add(post);
                }

                Collections.reverse(posts);

                if(posts.size() > 0){
                    event_pics.setVisibility(View.VISIBLE);
                    //
                    layoutManager = new GridLayoutManager(context,3);
                    photo_list.setLayoutManager(layoutManager);
                    adapter = new EventsPhotoAdapter(context,posts);
//                    adapter.setHasStableIds(true);
                    photo_list.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    adapter.refresh();
                }else{
                    event_pics.setVisibility(View.INVISIBLE);
                    loading.setVisibility(View.INVISIBLE);
                    tag.setText("No event photo found");
                }

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
//                if(photo_placeholder.isShown()){
//                    photo_placeholder.setVisibility(View.INVISIBLE);
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void capturePicture() {
        time = String.valueOf(System.currentTimeMillis());
        photoFileName = "moments_"+time+".jpg";
        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Uri uri  = Uri.parse(Environment.getExternalStorageDirectory().getPath()+"memories_"+time+".jpg");
        intent1.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));
        startActivityForResult(intent1,10);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case 11:
                ClipData clipdata = data.getClipData();
                if(clipdata != null){
                    for (int i=0; i<clipdata.getItemCount();i++) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), clipdata.getItemAt(i).getUri());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("bmp_"+i, Utilities.BitMapToString(bitmap));
                            editor.apply();
                            //DO something
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    Intent myIntent = new Intent(context, UploadService.class);
                    if(event != null){
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event",event);
                        bundle.putBoolean("multiple",true);
                        bundle.putInt("bitmap_count",clipdata.getItemCount());
                        myIntent.putExtras(bundle);
                    }
                    getActivity().startService(myIntent);
                }else {
                    InputStream ist = null;
                    try {
                        ist = context.getContentResolver().openInputStream(data.getData());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap bitmap = BitmapFactory.decodeStream(ist);
                    Log.d("DATA",bitmap.toString());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("bmp_" + 0, Utilities.BitMapToString(bitmap));
                    editor.apply();
                    Intent myIntent = new Intent(context, UploadService.class);
                    if (event != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("event", event);
                        bundle.putBoolean("multiple", true);
                        bundle.putInt("bitmap_count", 1);
                        myIntent.putExtras(bundle);
                    }
                    getActivity().startService(myIntent);
                }
                break;
            case 10:
//                File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "memories_" + time + ".jpg");
                Uri imageUri = getPhotoFileUri(photoFileName);
                Intent intent = new Intent(context, PreviewActivity.class);
                intent.putExtra("image_uri", imageUri.toString());
                intent.putExtra("time",time);
                Bundle bundle = new Bundle();
                bundle.putSerializable("event",event);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
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
            case 8:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showJoinDialog();
                } else {
                    Toast.makeText(getActivity(),"Permissions not granted",Toast.LENGTH_SHORT).show();

                }
                break;
            case 9:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    capturePicture();
                } else {
                    Toast.makeText(getActivity(),"Please grant permissions to take photos",Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

    private void showJoinDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(getActivity(), DialogActivity.class);
            ActivityOptions options = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), join_btn, "transition");
            }
            assert options != null;
            startActivityForResult(intent, 5, options.toBundle());
        }else{
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(join_event_view)
                    .create();
            dialog.show();
        }
    }
}
