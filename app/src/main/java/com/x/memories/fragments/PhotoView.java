package com.x.memories.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.x.memories.R;
import com.x.memories.models.Post;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by AKINDE-PETERS on 9/4/2016.
 */
public class PhotoView extends Fragment {

    Context context;
    ImageView image;
    Button request_btn;
    TextView caption_text;
    Post post;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_view, container, false);
        post = (Post) getArguments().getSerializable("photo");
        image = (ImageView) rootView.findViewById(R.id.image);
        request_btn = (Button) rootView.findViewById(R.id.request_btn);
        caption_text = (TextView)rootView.findViewById(R.id.caption_text);

        if(post.getPrivacy()){

            request_btn.setVisibility(View.VISIBLE);
            caption_text.setVisibility(View.INVISIBLE);

            Glide.with(getActivity().getApplicationContext())
                    .load(post.getUrl())
                    .placeholder(R.drawable.memories_grey_big)
                    .bitmapTransform(new BlurTransformation(context,50))
                    .into(image);
        }else{

            request_btn.setVisibility(View.INVISIBLE);
            caption_text.setVisibility(View.VISIBLE);
            caption_text.setText(post.getCaption());
            caption_text.setTypeface(null, Typeface.ITALIC);

            Glide.with(getActivity().getApplicationContext())
                    .load(post.getUrl())
                    .placeholder(R.drawable.memories_grey_big)
                    .fitCenter()
                    .into(image);
        }

//        Uri uri = Uri.parse(post.getUrl());
//        ImageRequest request = ImageRequestBuilder
//                .newBuilderWithSource(uri)
//                .setLocalThumbnailPreviewsEnabled(true)
//                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
//                .setProgressiveRenderingEnabled(false)
//                .build();
//        image.setImageURI(post.getUrl());


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_video, menu);
//        final boolean check = sharedPref.getBoolean(article.getId(), false);
//        if(check){
//            menu.getItem(3).setIcon(R.drawable.ic_bookmark);}
//        else{
//            menu.getItem(3).setIcon(R.drawable.ic_bookmark_not);
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
