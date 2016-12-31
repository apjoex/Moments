package com.x.memories.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.x.memories.DialogActivity;
import com.x.memories.R;
import com.x.memories.models.Event;

import java.util.ArrayList;

/**
 * Created by AKINDE-PETERS on 12/5/2016.
 */

public class EventAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Event> events = new ArrayList<>();
    DialogActivity activity;

    public EventAdapter(DialogActivity dialogActivity, Context context, ArrayList<Event> events) {
        this.activity = dialogActivity;
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
            viewHolder.lock_img.setVisibility(View.VISIBLE);
        }else{
            viewHolder.lock_img.setVisibility(View.INVISIBLE);
        }

        viewHolder.item_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(events.get(i).getProtect()){
                    LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View passwordView = layoutInflater.inflate(R.layout.enter_passcode,null);
                    TextView description = (TextView)passwordView.findViewById(R.id.description);
                    final EditText passcodeText = (EditText)passwordView.findViewById(R.id.passcode);
                    description.setText(events.get(i).getDescription());
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle(events.get(i).getName())
                            .setView(passwordView)
                            .setPositiveButton("JOIN EVENT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int position) {
                                    if(!TextUtils.isEmpty(passcodeText.getText().toString())){
                                        int code = Integer.parseInt(passcodeText.getText().toString());
                                        if(code == events.get(i).getCode()){
                                            Toast.makeText(context, "Event joined", Toast.LENGTH_SHORT).show();
                                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            Gson gson = new Gson();
                                            String event_details = gson.toJson(events.get(i));
                                            editor.putString("Event_details",event_details);
                                            editor.putBoolean("Event_active",true);
                                            editor.apply();
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                activity.dismiss();
                                            }
                                        }else{
                                            Toast.makeText(context, "Incorrect passcode", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Toast.makeText(context, "Enter a passcode", Toast.LENGTH_SHORT).show();
                                    }

                                    dialogInterface.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                }else{
                    Toast.makeText(context, "Event joined", Toast.LENGTH_SHORT).show();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = preferences.edit();
                    Gson gson = new Gson();
                    String event_details = gson.toJson(events.get(i));
                    editor.putString("Event_details",event_details);
                    editor.putBoolean("Event_active",true);
                    editor.apply();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        activity.dismiss();
                    }
                }

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
