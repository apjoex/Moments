package com.x.memories.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.x.memories.R;
import com.x.memories.models.Event;

import java.util.ArrayList;

/**
 * Created by AKINDE-PETERS on 12/5/2016.
 */

public class EventAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Event> events = new ArrayList<>();

    public EventAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;

        if(view == null){
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.event_item, null);
            viewHolder.event_name = (TextView) view.findViewById(R.id.name);
            viewHolder.lock_img = (ImageView)view.findViewById(R.id.lock_img);
            viewHolder.item_back = (LinearLayout)view.findViewById(R.id.item_back);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.event_name.setText(events.get(i).getName());
        if(events.get(i).getProtect()){
            viewHolder.lock_img.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.lock_img.setVisibility(View.VISIBLE);
        }

        viewHolder.item_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                Gson gson = new Gson();
                String event_details = gson.toJson(events.get(i));
                editor.putString("Event_details",event_details);
                editor.putBoolean("Event_active",true);
                editor.apply();
            }
        });


        return view;
    }

    private class ViewHolder{
        LinearLayout item_back;
        TextView event_name;
        ImageView lock_img;
    }
}
