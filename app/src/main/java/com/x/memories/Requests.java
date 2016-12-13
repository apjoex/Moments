package com.x.memories;

import android.app.NotificationManager;
import android.app.ProgressDialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.x.memories.adapters.RequestAdapter;
import com.x.memories.models.Request;
import com.x.memories.reusables.Utilities;

import java.util.ArrayList;
import java.util.Collections;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Requests extends AppCompatActivity {

    Context context;
    FirebaseDatabase database;
    Query notfRef;
    ArrayList<Request> requests = new ArrayList<>();
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView request_list;
    LinearLayout request_placeholder;
    String action = "demo";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        context = this;
        request_list = (RecyclerView)findViewById(R.id.request_list);
        request_placeholder = (LinearLayout)findViewById(R.id.request_placeholder);

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
        getSupportActionBar().setTitle("Permission requests");

        Intent intent = getIntent();
        // Get the extras (if there are any)
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d("MOMENTS_intent",extras.getString("action"));
            action = extras.getString("action");
        }else{
            Log.d("MOMENTS_intent","nothing");
        }
        Log.d("MOMENTS_ACTION",action);

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
                            if(request.getStatus().equals("sent")){
                                requests.add(request);
                            }

                    }catch (Exception  e){
                        Log.d("CastError","could cast objects");
                    };
                }
                if(!requests.isEmpty()) {
                    showRequests(requests);
                }

//                Log.d("FirebaseNotify",dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        checkList();
        registerReceiver(deleteReceiver, new IntentFilter("LISTENER"));

        //Dismiss notifications
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        action = "demo";
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(progressDialog!= null){
            progressDialog.dismiss();
        }
    }

    private void showRequests(ArrayList<Request> requests) {

        Collections.reverse(requests);

        layoutManager = new LinearLayoutManager(context);
        request_list.setLayoutManager(layoutManager);
        adapter = new RequestAdapter(context,requests,"pending");
        request_list.setAdapter(adapter);

        //Hide loading screen
        if(request_placeholder.isShown()){
            request_placeholder.setVisibility(View.INVISIBLE);
        }

        if(action.equals("accept")){
            acceptRequest(0);
        }else if(action.equals("decline")){
            declineRequest(0);
        }
    }

    private void declineRequest(int position) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String uid = sharedPref.getString("LOGGEDIN_UID", "");
        progressDialog = ProgressDialog.show(context, null, "Declining request...", false, false);
        FirebaseDatabase.getInstance().getReference().child("notifications").child(uid).child(requests.get(position).getUid()+"_"+requests.get(position).getTime()).setValue(null, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Toast.makeText(context,"Request declined successfully",Toast.LENGTH_SHORT).show();
                if(progressDialog.isShowing()){ progressDialog.dismiss(); }
                adapter.notifyDataSetChanged();
                checkList();
                action = "demo";
            }
        });

    }

    private void acceptRequest(final int position) {
        progressDialog = ProgressDialog.show(context, null, "Accepting request...", false, false);
        Request request = new Request(requests.get(position).name,requests.get(position).getUid(), Utilities.getTime(),"photo","accepted",requests.get(position).getPost_id(),requests.get(position).getCaption(),requests.get(position).getBitmapString());
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
                        adapter.notifyDataSetChanged();
                        checkList();
                        action = "demo";
                    }
                });

            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_requests, menu);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
        }

        if(id == R.id.action_accepted){
            Intent intent = new Intent(context, GrantedRequests.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkList() {
        if(requests.size() == 0){
            request_placeholder.setVisibility(View.VISIBLE);
        }else{
            request_placeholder.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
