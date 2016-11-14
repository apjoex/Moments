package com.x.memories.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.x.memories.R;
import com.x.memories.VideoPlay;
import com.x.memories.models.Post;
import com.x.memories.models.Request;
import com.x.memories.reusables.Utilities;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropSquareTransformation;

/**
 * Created by AKINDE-PETERS on 8/24/2016.
 */
public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.ViewHolder> {

    private ArrayList<Post> posts;
    private Context context;
    private int type;

    public FeedsAdapter(Context context, ArrayList<Post> posts, int type) {
        this.context = context;
        this.posts = posts;
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
        View v2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);

        if(type == 1){
            return new ViewHolder(v);
        }else{
            return new ViewHolder(v2);
        }

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(type == 1){
            //For photos
                if(posts.get(position).getPrivacy() && !posts.get(position).getUid().equals(preferences.getString("LOGGEDIN_UID",""))){
                    if(holder.thumbnail != null){
                        holder.privacy_logo.setVisibility(View.INVISIBLE);
                        Glide.with(context)
                                .load(posts.get(position).getUrl())
                                .placeholder(R.drawable.memories_grey)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .bitmapTransform(new BlurTransformation(context,50), new CropSquareTransformation(context))
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        holder.privacy_logo.setVisibility(View.VISIBLE);
                                        return false;
                                    }
                                })
                                .into(holder.thumbnail);
                    }
                }else{
                    holder.privacy_logo.setVisibility(View.INVISIBLE);
                    if(holder.thumbnail != null){
                        Glide.with(context)
                                .load(posts.get(position).getUrl())
                                .placeholder(R.drawable.memories_grey)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .bitmapTransform(new CropSquareTransformation(context))
                                .into(holder.thumbnail);
                    }
                }
        }else{
            //For videos
            holder.caption.setText(posts.get(position).getCaption());
            if(posts.get(position).getPrivacy() && !posts.get(position).getUid().equals(preferences.getString("LOGGEDIN_UID",""))){
                holder.privacy_logo.setVisibility(View.VISIBLE);
            }else {
                holder.privacy_logo.setVisibility(View.INVISIBLE);
            }

            holder.video_item_box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(posts.get(position).getPrivacy()  && !posts.get(position).getUid().equals(preferences.getString("LOGGEDIN_UID",""))){
                        //Private video
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle("Private moment")
                                .setMessage("This is a private moment. You need permission to view this moment.")
                                .setPositiveButton("REQUEST PERMISSION", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        final ProgressDialog progressDialog = ProgressDialog.show(context, null, "Requesting...", false, false);
                                        Request request = new Request(preferences.getString("LOGGEDIN_NAME","Someone"),preferences.getString("LOGGEDIN_UID",""), Utilities.getTime(),"video","sent",posts.get(position).getUrl(),posts.get(position).getCaption());
                                        FirebaseDatabase.getInstance().getReference().child("notifications").child(posts.get(position).getUid()).child(request.getUid()+"_"+request.getTime()).setValue(request, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                Toast.makeText(context,"Request sent. You'll get notified when you are granted permission to view the moment",Toast.LENGTH_SHORT).show();
                                                if(progressDialog.isShowing()){
                                                    progressDialog.dismiss();
                                                }
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
                        //Public video
                        Intent intent = new Intent(context, VideoPlay.class);
                        intent.putExtra("video_url",posts.get(position).getUrl());
                        context.startActivity(intent);
                    }

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout body;
        ImageView thumbnail,privacy_logo;
        public TextView caption;
        RelativeLayout video_item_box,photo_box;


        public ViewHolder(View itemView) {
            super(itemView);
            body = (LinearLayout) itemView.findViewById(R.id.body);
            thumbnail = (ImageView)itemView.findViewById(R.id.thumbnail);
            privacy_logo = (ImageView)itemView.findViewById(R.id.privacy_logo);
            caption = (TextView)itemView.findViewById(R.id.caption);
            video_item_box = (RelativeLayout)itemView.findViewById(R.id.video_item_box);
            photo_box = (RelativeLayout)itemView.findViewById(R.id.photo_box);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Insert a new item to the RecyclerView on a predefined position
    public void insert(int position, Post post) {
        posts.add(position, post);
        notifyItemInserted(position);
    }

    // Remove a RecyclerView item containing a specified Data object
    public void remove(Post post) {
        int position = posts.indexOf(post);
        posts.remove(position);
        notifyItemRemoved(position);
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private FeedsAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final FeedsAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}
