package com.x.memories.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.x.memories.models.Post;
import com.x.memories.models.Request;
import com.x.memories.reusables.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by AKINDE-PETERS on 10/7/2016.
 */

public class SlideShowDialogFragment extends DialogFragment {

    ArrayList<Post> posts;
    ViewPager viewPager;
    private int selectedPosition = 0;
    private MyViewPagerAdapter myViewPagerAdapter;

    public static SlideShowDialogFragment newInstance(){
        return new SlideShowDialogFragment();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_photo_viewer, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.pager);


//        lblCount = (TextView) v.findViewById(R.id.lbl_count);
//        lblTitle = (TextView) v.findViewById(R.id.title);
//        lblDate = (TextView) v.findViewById(R.id.date);

        posts = (ArrayList<Post>) getArguments().getSerializable("posts");
        selectedPosition = getArguments().getInt("position");

//        Log.e(TAG, "position: " + selectedPosition);
//        Log.e(TAG, "images size: " + images.size());

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        return v;
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayMetaInfo(selectedPosition);
    }

    //  page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    private void displayMetaInfo(int position) {
//        lblCount.setText((position + 1) + " of " + images.size());

        Post post = posts.get(position);
//        caption_text.setText(post.getCaption());
//        lblDate.setText(image.getTimestamp());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
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


            final Post post = posts.get(position);
//
//            Glide.with(getActivity()).load(post.getUrl())
//                    .thumbnail(0.5f)
//                    .crossFade()
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(imageViewPreview);

            request_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), null, "Requesting...", false, false);
                    Request request = new Request(preferences.getString("LOGGEDIN_NAME","Someone"),preferences.getString("LOGGEDIN_UID",""), Utilities.getTime(),"photo","sent",posts.get(position).getUrl(),posts.get(position).getCaption());
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
                    saveToInternalStorage(imageViewPreview,post.getTime());
                }
            });

            if(post.getPrivacy()){
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

            }else{
                mypath.mkdir();
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
            return posts.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


    }
}
