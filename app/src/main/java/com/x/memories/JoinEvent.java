package com.x.memories;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.x.memories.adapters.EventsPhotoAdapter;
import com.x.memories.models.Event;
import com.x.memories.models.EventPost;

import java.util.Collections;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class JoinEvent extends AppCompatActivity {

    Context context;
    String event_id;
    FirebaseDatabase database;
    Query query;
    Boolean mIsRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event);
        context = this;
        database = FirebaseDatabase.getInstance();


        //Set light status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));
        }

        assert getSupportActionBar()!= null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Join event");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black);

        event_id = getIntent().getStringExtra("event");

        getEvent(event_id);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsRunning = false;
    }

    private void getEvent(String event_id) {
        query = database.getReference("events").orderByChild("id").equalTo(event_id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot datasnapshot: dataSnapshot.getChildren()){
                    final Event event = datasnapshot.getValue(Event.class);
                    if(event.getProtect()){
                        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View passwordView = layoutInflater.inflate(R.layout.enter_passcode,null);
                        TextView description = (TextView)passwordView.findViewById(R.id.description);
                        final EditText passcodeText = (EditText)passwordView.findViewById(R.id.passcode);
                        description.setText(event.getDescription());
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle(event.getName())
                                .setCancelable(false)
                                .setView(passwordView)
                                .setPositiveButton("JOIN EVENT", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int position) {
                                        if(!TextUtils.isEmpty(passcodeText.getText().toString())){
                                            int code = Integer.parseInt(passcodeText.getText().toString());
                                            if(code == event.getCode()){
                                                Toast.makeText(context, "Event joined", Toast.LENGTH_SHORT).show();
                                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                                                SharedPreferences.Editor editor = preferences.edit();
                                                Gson gson = new Gson();
                                                String event_details = gson.toJson(event);
                                                editor.putString("Event_details",event_details);
                                                editor.putBoolean("Event_active",true);
                                                editor.apply();
                                                finish();
                                            }else{
                                                Toast.makeText(context, "Incorrect passcode", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }else{
                                            Toast.makeText(context, "Enter a passcode", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }

                                        dialogInterface.dismiss();
                                    }
                                })
                                .create();
                        if(mIsRunning){
                            dialog.show();
                        }
                    }else{
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = preferences.edit();
                        Gson gson = new Gson();
                        String event_details = gson.toJson(event);
                        editor.putString("Event_details",event_details);
                        editor.putBoolean("Event_active",true);
                        editor.apply();
                        if(mIsRunning){
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
