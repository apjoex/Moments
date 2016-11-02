package com.x.memories;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.x.memories.adapters.RequestAdapter;
import com.x.memories.models.Request;

import java.util.ArrayList;
import java.util.Collections;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GrantedRequests extends AppCompatActivity {

    Context context;
    RecyclerView granted_request_list;
    RelativeLayout granted_placeholder;
    FirebaseDatabase database;
    Query notfRef;
    ArrayList<Request> requests = new ArrayList<>();
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_granted_requests);
        context = this;

        granted_request_list = (RecyclerView)findViewById(R.id.granted_request_list);
        granted_placeholder = (RelativeLayout)findViewById(R.id.granted_placeholder);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String uid = sharedPref.getString("LOGGEDIN_UID", "");
        database = FirebaseDatabase.getInstance();
        notfRef = database.getReference("notifications").child(uid).orderByChild("time");

        //Set light status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));
        }

        assert getSupportActionBar()!= null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Granted requests");
    }

    @Override
    protected void onResume() {
        super.onResume();
        notfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                requests.clear();
                for(DataSnapshot datasnapshot: dataSnapshot.getChildren()){
                    try{
                        Request request = datasnapshot.getValue(Request.class);
                        if(request.getStatus().equals("accepted")){
                            requests.add(request);
                        }

                    }catch (Exception  e){
                        Log.d("CastError","could cast objects");
                    };
                }
                if(!requests.isEmpty()) {
                    showRequests(requests);
                }

                Log.d("FirebaseNotify",dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        checkList();
        registerReceiver(deleteReceiver, new IntentFilter("LISTENER"));
    }

    private BroadcastReceiver deleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String delete_action = intent.getExtras().getString("delete_action");
            if (delete_action != null && delete_action.equals("yes")) {
                checkList();
            }
        }
    };

    private void showRequests(ArrayList<Request> requests) {

        Collections.reverse(requests);

        layoutManager = new LinearLayoutManager(context);
        granted_request_list.setLayoutManager(layoutManager);
        adapter = new RequestAdapter(context,requests,"granted");
        granted_request_list.setAdapter(adapter);


        //Hide loading screen
        if(granted_placeholder.isShown()){
            granted_placeholder.setVisibility(View.INVISIBLE);
        }

    }

    private void checkList() {
        if(requests.size() == 0){
            granted_placeholder.setVisibility(View.VISIBLE);
        }else{
            granted_placeholder.setVisibility(View.INVISIBLE);
        }
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
