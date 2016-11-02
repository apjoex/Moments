package com.x.memories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.x.memories.reusables.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GrantedView extends AppCompatActivity {

    Context context;
    String type, url;
    RelativeLayout video_place, photo_place;
    ImageView granted_image;
    ProgressBar loadingBar;
    FloatingActionButton saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_granted_view);
        context = this;
        type = getIntent().getExtras().getString("type");
        url = getIntent().getExtras().getString("url");


        video_place = (RelativeLayout)findViewById(R.id.video_place);
        photo_place = (RelativeLayout)findViewById(R.id.photo_place);
        granted_image = (ImageView)findViewById(R.id.granted_image);
        loadingBar = (ProgressBar)findViewById(R.id.loadingbar);
        saveBtn = (FloatingActionButton)findViewById(R.id.fav_btn);

        assert getSupportActionBar() != null;
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(type.equals("photo")){
            video_place.setVisibility(View.GONE);
            showPic(url);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveToInternalStorage(granted_image, Utilities.getTime());
                }
            });
        }else{
            photo_place.setVisibility(View.GONE);
            playVideo(url);
        }

    }

    private void playVideo(String url) {
        final VideoView vidView = (VideoView) findViewById(R.id.myVideo);
        Uri vidUri = Uri.parse(url);
        vidView.setVideoURI(vidUri);
        vidView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                loadingBar.setVisibility(View.GONE);
                mediaPlayer.setLooping(true);
                vidView.start();
            }
        });
    }

    private void showPic(String url) {
        Glide.with(context)
                .load(url)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                              @Override
                              public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                  loadingBar.setVisibility(View.GONE);
                                  return false;
                              }
                          }
                )
                .into(granted_image);
    }

    private void saveToInternalStorage(ImageView imageViewPreview, String time){
        GlideBitmapDrawable drawable = (GlideBitmapDrawable) imageViewPreview.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
//            ContextWrapper cw = new ContextWrapper(getActivity());
//            // path to /data/data/yourapp/app_data/imageDir
//            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//            // Create imageDir
//            File mypath=new File(directory,"moments_"+time+".jpg");
        File mypath = new File(Environment.getExternalStorageDirectory().getPath()+"/Moments");

        if(mypath.exists() && mypath.isDirectory()){
            File savepath = new File(mypath, "moments_" + time + ".jpg");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(savepath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Toast.makeText(context, "Moment saved successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }else{
            mypath.mkdir();
            File savepath = new File(mypath, "moments_" + time + ".jpg");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(savepath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Toast.makeText(context, "Moment saved successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
