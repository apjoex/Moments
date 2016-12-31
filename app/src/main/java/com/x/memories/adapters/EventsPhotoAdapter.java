package com.x.memories.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.x.memories.R;
import com.x.memories.fragments.SlideShowDialogFragment;
import com.x.memories.models.EventPost;
import com.x.memories.models.Post;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropSquareTransformation;

/**
 * Created by apjoe on 12/23/2016.
 */
public class EventsPhotoAdapter extends RecyclerView.Adapter<EventsPhotoAdapter.ViewHolder> {

    private ArrayList<EventPost> posts;
    private Context context;

    public EventsPhotoAdapter(Context context, ArrayList<EventPost> posts) {
        this.context = context;
        this.posts = posts;
    }

    public void refresh(){
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
        return new EventsPhotoAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        holder.privacy_logo.setVisibility(View.INVISIBLE);
        Glide.with(context)
                .load(posts.get(position).getUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .bitmapTransform(new CropSquareTransformation(context))
                .into(holder.thumbnail);
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("event_posts", posts);
                bundle.putInt("position", position);

                FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                SlideShowDialogFragment newFragment = SlideShowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail,privacy_logo;
        public ViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView)itemView.findViewById(R.id.thumbnail);
            privacy_logo = (ImageView)itemView.findViewById(R.id.privacy_logo);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
