package com.x.memories.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.x.memories.GrantedView;
import com.x.memories.R;
import com.x.memories.models.Request;
import com.x.memories.reusables.Utilities;

import java.util.ArrayList;

/**
 * Created by AKINDE-PETERS on 9/5/2016.
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Request> requests;
    private String status;

    public RequestAdapter(Context context, ArrayList<Request> requests, String status) {
        this.context = context;
        this.requests = requests;
        this.status = status;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        if(status.equals("pending")){

            //PENDING REQUESTS

            if(requests.get(position).getPost_type().equals("photo")){
                holder.video_request.setVisibility(View.GONE);
                holder.name_tag.setText(requests.get(position).getName()+" requests to see your private moment");
                holder.time_tag.setText(Utilities.daysAgo(requests.get(position).getTime()));
                Glide.with(context)
                        .load(requests.get(position).getPost_id())
                        .placeholder(R.drawable.memories_grey)
                        .centerCrop()
                        .into(holder.imagebox);

                holder.accept_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final ProgressDialog progressDialog = ProgressDialog.show(context, null, "Accepting request...", false, false);
                        Request request = new Request(requests.get(position).name,requests.get(position).getUid(), Utilities.getTime(),"photo","accepted",requests.get(position).getPost_id(),requests.get(position).getCaption(), requests.get(position).getBitmapString());
                        FirebaseDatabase.getInstance().getReference().child("notifications").child(requests.get(position).getUid()).child(requests.get(position).getUid()+"_"+Utilities.getTime()).setValue(request, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                String uid = sharedPref.getString("LOGGEDIN_UID", "");
                                FirebaseDatabase.getInstance().getReference().child("notifications").child(uid).child(requests.get(position).getUid()+"_"+requests.get(position).getTime()).setValue(null, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Toast.makeText(context, "Request accepted", Toast.LENGTH_SHORT).show();
                                        if (progressDialog.isShowing()) {progressDialog.dismiss();}
                                        notifyDataSetChanged();
                                        Intent intent = new Intent("LISTENER");
                                        intent.putExtra("delete_action", "yes");
                                        context.sendBroadcast(intent);
                                    }
                                });

                            }
                        });

                    }
                });

            }else if(requests.get(position).getPost_type().equals("video")){
                holder.photo_request.setVisibility(View.GONE);
                holder.name_tag_2.setText(requests.get(position).getName()+" requests to see your private moment");
                holder.time_tag_2.setText(Utilities.daysAgo(requests.get(position).getTime()));
                holder.caption_tag.setText(requests.get(position).getCaption());

                holder.accept_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final ProgressDialog progressDialog = ProgressDialog.show(context, null, "Accepting request...", false, false);
                        Request request = new Request(requests.get(position).name,requests.get(position).getUid(), Utilities.getTime(),"video","accepted",requests.get(position).getPost_id(),requests.get(position).getCaption(),"");
                        FirebaseDatabase.getInstance().getReference().child("notifications").child(requests.get(position).getUid()).child(requests.get(position).getUid()+"_"+Utilities.getTime()).setValue(request, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                String uid = sharedPref.getString("LOGGEDIN_UID", "");
                                FirebaseDatabase.getInstance().getReference().child("notifications").child(uid).child(requests.get(position).getUid()+"_"+requests.get(position).getTime()).setValue(null, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Toast.makeText(context, "Request accepted", Toast.LENGTH_SHORT).show();
                                        if (progressDialog.isShowing()) {progressDialog.dismiss();}
                                        notifyDataSetChanged();
                                        Intent intent = new Intent("LISTENER");
                                        intent.putExtra("delete_action", "yes");
                                        context.sendBroadcast(intent);
                                    }
                                });

                            }
                        });

                    }
                });
            }

            holder.decline_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                Toast.makeText(context, requests.get(position).getUid()+"_"+requests.get(position).getTime()+" request is declined", Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    String uid = sharedPref.getString("LOGGEDIN_UID", "");
                    final ProgressDialog progressDialog = ProgressDialog.show(context, null, "Declining request...", false, false);
                    FirebaseDatabase.getInstance().getReference().child("notifications").child(uid).child(requests.get(position).getUid()+"_"+requests.get(position).getTime()).setValue(null, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Toast.makeText(context,"Request declined successfully",Toast.LENGTH_SHORT).show();
                            if(progressDialog.isShowing()){ progressDialog.dismiss(); }
                            notifyDataSetChanged();
                            Intent intent = new Intent("LISTENER");
                            intent.putExtra("delete_action", "yes");
                            context.sendBroadcast(intent);
                        }
                    });
                }
            });

        }else{


            //GRANTED REQUESTS

            holder.button_box.setVisibility(View.GONE);

            if(requests.get(position).getPost_type().equals("photo")){
                holder.video_request.setVisibility(View.GONE);
                holder.name_tag.setText("You have been granted permission to see this moment");
                holder.time_tag.setText(Utilities.daysAgo(requests.get(position).getTime()));
                Glide.with(context)
                        .load(requests.get(position).getPost_id())
                        .placeholder(R.drawable.memories_grey)
                        .centerCrop()
                        .into(holder.imagebox);

                holder.photo_request.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                String uid = sharedPref.getString("LOGGEDIN_UID", "");
                                FirebaseDatabase.getInstance().getReference().child("notifications").child(uid).child(requests.get(position).getUid()+"_"+requests.get(position).getTime()).setValue(null, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                notifyDataSetChanged();
                                Intent intent = new Intent("LISTENER");
                                intent.putExtra("delete_action", "yes");
                                context.sendBroadcast(intent);
                            }
                        });

                        Intent intent = new Intent(context, GrantedView.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("type","photo");
                        bundle.putString("url",requests.get(position).getPost_id());
                        intent.putExtras(bundle);
                        context.startActivity(intent);

                    }
                });
            }else if(requests.get(position).getPost_type().equals("video")){
                holder.photo_request.setVisibility(View.GONE);
                holder.name_tag_2.setText("You have been granted permission to see this moment");
                holder.time_tag_2.setText(Utilities.daysAgo(requests.get(position).getTime()));
                holder.caption_tag.setText(requests.get(position).getCaption());

                holder.video_request.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                String uid = sharedPref.getString("LOGGEDIN_UID", "");
                                FirebaseDatabase.getInstance().getReference().child("notifications").child(uid).child(requests.get(position).getUid()+"_"+requests.get(position).getTime()).setValue(null, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                notifyDataSetChanged();
                                Intent intent = new Intent("LISTENER");
                                intent.putExtra("delete_action", "yes");
                                context.sendBroadcast(intent);
                            }
                        });

                        Intent intent = new Intent(context, GrantedView.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("type","video");
                        bundle.putString("url",requests.get(position).getPost_id());
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }
                });
            }
        }


    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout photo_request, video_request;
        ImageView imagebox;
        TextView name_tag, time_tag, name_tag_2, time_tag_2, caption_tag;
        Button accept_btn, decline_btn;
        LinearLayout button_box;


        ViewHolder(View itemView) {
            super(itemView);

            photo_request = (RelativeLayout)itemView.findViewById(R.id.photo_request);
            video_request = (RelativeLayout)itemView.findViewById(R.id.video_request);
            imagebox = (ImageView)itemView.findViewById(R.id.imagebox);
            name_tag = (TextView) itemView.findViewById(R.id.name_tag);
            time_tag = (TextView) itemView.findViewById(R.id.time_tag);
            name_tag_2 = (TextView) itemView.findViewById(R.id.name_tag_2);
            time_tag_2 = (TextView) itemView.findViewById(R.id.time_tag_2);
            caption_tag = (TextView) itemView.findViewById(R.id.caption_tag);
            accept_btn = (Button) itemView.findViewById(R.id.accept_btn);
            decline_btn = (Button)itemView.findViewById(R.id.decline_btn);
            button_box = (LinearLayout)itemView.findViewById(R.id.button_box);
        }
    }
}
