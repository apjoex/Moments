package com.x.memories.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.x.memories.R;
import com.x.memories.models.EventPost;
import com.x.memories.models.Post;
import com.x.memories.models.Request;
import com.x.memories.reusables.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by AKINDE-PETERS on 10/7/2016.
 */

public class SlideShowDialogFragment extends DialogFragment {

    ArrayList<Post> posts;
    ArrayList<EventPost> eventPosts;
    ViewPager viewPager;
    private int selectedPosition = 0;
    private MyViewPagerAdapter myViewPagerAdapter;
    SharedPreferences preferences;

    public static SlideShowDialogFragment newInstance(){
        return new SlideShowDialogFragment();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_photo_viewer, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.pager);
        posts = (ArrayList<Post>) getArguments().getSerializable("posts");
        eventPosts = (ArrayList<EventPost>) getArguments().getSerializable("event_posts");
        selectedPosition = getArguments().getInt("position");
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);

        final RelativeLayout tut_holder = (RelativeLayout)v.findViewById(R.id.tut_holder);
        final ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(getActivity(), new MyPinchListener());
        tut_holder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(preferences.getBoolean("tutorial",false)){
            tut_holder.setVisibility(View.GONE);
        }

        AppCompatButton got_btn = (AppCompatButton)v.findViewById(R.id.got_btn);
        ColorStateList stateList =  ColorStateList.valueOf(Color.rgb(0,151,214));
        got_btn.setSupportBackgroundTintList(stateList);
        got_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tut_holder.setVisibility(View.GONE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("tutorial",true);
                editor.apply();
            }
        });


        setCurrentItem(selectedPosition);

        return v;
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    public void removeFragment(Fragment fragment){
        android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myViewPagerAdapter.proceed();
                } else {
                    Toast.makeText(getActivity(),"Please grant permissions to save moments",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class MyPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d("TAG", "PINCH! OUCH!");
            Log.d("SCATE FACTOR", ""+detector.getScaleFactor());
//            if(detector.getScaleFactor() < 0.90){
//                removeFragment(SlideShowDialogFragment.this);
//            }
            return true;
        }
    }


    //  adapter
    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.fragment_photo_view, container, false);

            final ImageView imageViewPreview = (ImageView) view.findViewById(R.id.image);
            TextView captionText = (TextView)view.findViewById(R.id.caption_text);
            Button request_btn = (Button)view.findViewById(R.id.request_btn);
            final ProgressBar loadingBar = (ProgressBar)view.findViewById(R.id.loadingbar);
            final FloatingActionButton favBtn = (FloatingActionButton)view.findViewById(R.id.fav_btn);
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            favBtn.hide();


            if(posts != null){
                final Post post = posts.get(position);

                request_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), null, "Requesting...", false, false);
                        new AsyncTask<String, Void, Bitmap>() {
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            @Override
                            protected Bitmap doInBackground(String... strings) {
                                try {
                                    return Glide.with(getActivity())
                                            .load(posts.get(position).getUrl())
                                            .asBitmap()
                                            .centerCrop()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(400, 400) // Width and height
                                            .get();
                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Bitmap result) {
                                super.onPostExecute(result);
                                try {
                                    Log.d("WEARABLE","Gobe no dey");
                                    Request request = new Request(preferences.getString("LOGGEDIN_NAME","Someone"),preferences.getString("LOGGEDIN_UID",""), Utilities.getTime(),"photo","sent",posts.get(position).getUrl(),posts.get(position).getCaption(),Utilities.BitMapToString(result));
                                    FirebaseDatabase.getInstance().getReference().child("notifications").child(posts.get(position).getUid()).child(request.getUid()+"_"+request.getTime()).setValue(request, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            Toast.makeText(getActivity(),"Request sent. You'll get notified when you are granted permission to view the moment",Toast.LENGTH_SHORT).show();
                                            if(progressDialog.isShowing()){ progressDialog.dismiss(); }
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.d("WEARABLE","Gobe dey");
                                    e.printStackTrace();
                                }
                            }
                        }.execute();


                        Request request = new Request(preferences.getString("LOGGEDIN_NAME","Someone"),preferences.getString("LOGGEDIN_UID",""), Utilities.getTime(),"photo","sent",posts.get(position).getUrl(),posts.get(position).getCaption(),Utilities.BitMapToString(((BitmapDrawable)imageViewPreview.getDrawable()).getBitmap()));
                        FirebaseDatabase.getInstance().getReference().child("notifications").child(posts.get(position).getUid()).child(request.getUid()+"_"+request.getTime()).setValue(request, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(getActivity(),"Request sent. You'll get notified when you are granted permission to view the moment",Toast.LENGTH_SHORT).show();
                                if(progressDialog.isShowing()){ progressDialog.dismiss(); }
                            }
                        });
                    }
                });

                favBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    10);
                        }else{
                            saveToInternalStorage(imageViewPreview,post.getTime());
                        }
                    }
                });

                if(post.getPrivacy() && !posts.get(position).getUid().equals(preferences.getString("LOGGEDIN_UID",""))){
                    request_btn.setVisibility(View.VISIBLE);
                    captionText.setVisibility(View.INVISIBLE);
                    Glide.with(getActivity())
                            .load(post.getUrl())
                            .crossFade()
                            .bitmapTransform(new BlurTransformation(getActivity(),50))
                            .listener(new RequestListener<String, GlideDrawable>() {
                                          @Override
                                          public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                              return false;
                                          }

                                          @Override
                                          public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                              favBtn.hide();
                                              loadingBar.setVisibility(View.GONE);
                                              return false;
                                          }
                                      }
                            )
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageViewPreview);
                }else{
                    request_btn.setVisibility(View.INVISIBLE);
                    captionText.setVisibility(View.VISIBLE);
                    captionText.setText(post.getCaption());
                    captionText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Dosis.ttf"), Typeface.ITALIC);

                    Glide.with(getActivity())
                            .load(post.getUrl())
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(new RequestListener<String, GlideDrawable>() {
                                          @Override
                                          public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                              return false;
                                          }

                                          @Override
                                          public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                              favBtn.show();
                                              loadingBar.setVisibility(View.GONE);
                                              return false;
                                          }
                                      }
                            )
                            .into(imageViewPreview);
                }
            }

            if(eventPosts != null){

                final EventPost post = eventPosts.get(position);

                favBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    10);
                        }else{
                            saveToInternalStorage(imageViewPreview,post.getTime());
                        }
                    }
                });


                request_btn.setVisibility(View.INVISIBLE);
                captionText.setVisibility(View.VISIBLE);
                captionText.setText("");
                captionText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Dosis.ttf"), Typeface.ITALIC);

                Glide.with(getActivity())
                        .load(post.getUrl())
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<String, GlideDrawable>() {
                                      @Override
                                      public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                          return false;
                                      }

                                      @Override
                                      public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                          favBtn.show();
                                          loadingBar.setVisibility(View.GONE);
                                          return false;
                                      }
                                  }
                        )
                        .into(imageViewPreview);
            }


            final ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(getActivity(), new MyPinchListener());
            imageViewPreview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mScaleDetector.onTouchEvent(event);
                    return true;
                }
            });

            container.addView(view);

            return view;
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
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                Bitmap result = Bitmap.createBitmap(w, h, bitmap.getConfig());
                Canvas canvas = new Canvas(result);
                canvas.drawBitmap(bitmap, 0, 0, null);
                Paint paint = new Paint();
                paint.setColor(Color.argb(120,255,255,255));
                paint.setTextSize(100);
                paint.setAntiAlias(true);
                paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Al_Fresco.otf"));
                int xPos = (canvas.getWidth() - 250);
                int yPos = (canvas.getHeight() - 20);
                canvas.drawText("moments",xPos, yPos, paint);
                File savepath = new File(mypath, "moments_" + time + ".jpg");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(savepath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    result.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    Toast.makeText(getActivity(), "Moment saved successfully", Toast.LENGTH_SHORT).show();
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
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                Bitmap result = Bitmap.createBitmap(w, h, bitmap.getConfig());
                Canvas canvas = new Canvas(result);
                canvas.drawBitmap(bitmap, 0, 0, null);
                Paint paint = new Paint();
                paint.setColor(Color.argb(120,255,255,255));
                paint.setTextSize(100);
                paint.setAntiAlias(true);
                paint.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Al_Fresco.otf"));
                int xPos = (canvas.getWidth() - 250);
                int yPos = (canvas.getHeight() - 20);
                canvas.drawText("moments",xPos, yPos, paint);
                File savepath = new File(mypath, "moments_" + time + ".jpg");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(savepath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    Toast.makeText(getActivity(), "Moment saved successfully", Toast.LENGTH_SHORT).show();
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
        public int getCount() {
            if(posts != null){
                return posts.size();
            }else if(eventPosts != null){
                return eventPosts.size();
            }else{
                return 0;
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        void proceed() {
            Toast.makeText(getActivity(),"Great! Permission granted. Let's try that once more...",Toast.LENGTH_SHORT).show();
        }

        private class MyPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                Log.d("TAG", "PINCH! OUCH!");
                Log.d("SCATE FACTOR", ""+detector.getScaleFactor());
                if(detector.getScaleFactor() < 0.90){
                    removeFragment(SlideShowDialogFragment.this);
                }
                return true;
            }
        }
    }
}
