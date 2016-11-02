package com.x.memories;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class VideoPlay extends AppCompatActivity {

    VideoView vidView;
    Context context;
    String url;
    Uri vidUri;
    ProgressBar loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_play);
        context = this;

        assert getSupportActionBar() != null;
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vidView = (VideoView)findViewById(R.id.myVideo);
        loading = (ProgressBar)findViewById(R.id.loadingbar);

        url = getIntent().getStringExtra("video_url");
        vidUri = Uri.parse(url);
        vidView.setVideoURI(vidUri);
        vidView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                loading.setVisibility(View.GONE);
                mediaPlayer.setLooping(true);
                vidView.start();
            }
        });


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
