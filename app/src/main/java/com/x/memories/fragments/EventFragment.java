package com.x.memories.fragments;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.x.memories.CreateEvent;
import com.x.memories.DialogActivity;
import com.x.memories.R;

/**
 * Created by AKINDE-PETERS on 11/29/2016.
 */

public class EventFragment extends Fragment {
    Context context;
    AppCompatButton join_btn, create_btn;
    View join_event_view;
    SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event, container, false);
        join_event_view = inflater.inflate(R.layout.join_event,null);
        join_btn = (AppCompatButton)v.findViewById(R.id.join_btn);
        create_btn = (AppCompatButton)v.findViewById(R.id.create_btn);

        ColorStateList stateList =  ColorStateList.valueOf(Color.WHITE);
        join_btn.setSupportBackgroundTintList(stateList);
        create_btn.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        create_btn.setTextColor(getResources().getColor(R.color.colorPrimary));

        join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 8);
                }else{
                    showJoinDialog();
                }

            }
        });

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, CreateEvent.class));
            }
        });

        return  v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(context, "Resuming...", Toast.LENGTH_SHORT).show();
//        if(preferences.getBoolean("Event_active",false)){
//            join_btn.setVisibility(View.INVISIBLE);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 8: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showJoinDialog();
                } else {
                    Toast.makeText(getActivity(),"Permissions not granted",Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    private void showJoinDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(getActivity(), DialogActivity.class);
            ActivityOptions options = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), join_btn, "transition");
            }
            assert options != null;
            startActivityForResult(intent, 5, options.toBundle());
        }else{
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(join_event_view)
                    .create();
            dialog.show();
        }
    }
}
