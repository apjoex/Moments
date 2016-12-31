package com.x.memories;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.x.memories.models.Event;

import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CreateEvent extends AppCompatActivity {

    Context context;
    AppCompatButton create_btn;
    Boolean protect = false;
    int code;
    SwitchCompat priv_switch;
    EditText name, description;
    SharedPreferences preferences;
    ProgressDialog progressDialog;
    DatabaseReference ref;
    GeoFire geoFire;
    CoordinatorLayout backGround;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        context = this;
        create_btn= (AppCompatButton)findViewById(R.id.create_btn);
        priv_switch = (SwitchCompat)findViewById(R.id.switcher);
        name = (EditText)findViewById(R.id.event_name);
        description = (EditText)findViewById(R.id.event_description);
        backGround = (CoordinatorLayout)findViewById(R.id.activity_create_event);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        ref = FirebaseDatabase.getInstance().getReference("geofire/events");
        geoFire = new GeoFire(ref);

        //Set light status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));
        }
        //Set up toolbar
        assert getSupportActionBar()!= null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Event");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black);


        //Location
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                        if (location == null) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    currentLatitude = location.getLatitude();
                                    currentLongitude = location.getLongitude();
                                }
                            });

                        } else {
                            //If everything went fine lets get latitude and longitude
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        if (connectionResult.hasResolution()) {
                            try {
                                // Start an Activity that tries to resolve the error
                                connectionResult.startResolutionForResult(CreateEvent.this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
                            } catch (IntentSender.SendIntentException e) {
                                // Log the error
                                e.printStackTrace();
                            }
                        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
                            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
                        }
                    }
                })
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds



        create_btn.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        create_btn.setTextColor(getResources().getColor(R.color.colorPrimary));

        priv_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                protect = b;
            }
        });

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateFields()){
                    
                    if(protect){
                        Random ran = new Random();
                        code = (100000 + ran.nextInt(900000));
//                        Toast.makeText(context, ""+code, Toast.LENGTH_SHORT).show();
                    }else{
                        code = 0;
                    }

                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        Toast.makeText(context, "Please turn on GPS on your device", Toast.LENGTH_SHORT).show();
                    }else{
                        postEvent();
                    }
                }
            }
        });

    }

    private void postEvent() {
        progressDialog = ProgressDialog.show(context,null,"A moment please...",false,false);
        final String uid = preferences.getString("LOGGEDIN_UID", "");
        final String time = String.valueOf(System.currentTimeMillis());
        final Event event = new Event(uid+"_"+time,name.getText().toString(),description.getText().toString(),uid,time,protect,code);
        geoFire.setLocation(event.getId(), new GeoLocation(currentLatitude, currentLongitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.d("MOMENTS_SERVICE","GeoFire done!");
            }
        });
        FirebaseDatabase.getInstance().getReference().child("events").child(event.getId()).setValue(event, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                progressDialog.dismiss();
                if (databaseError != null) {
                    Log.d("MOMENTS_SERVICE","Error sele "+databaseError.getMessage());
                    Toast.makeText(context, "Something went wrong somewhere somehow. Let's try that again in a bit", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MOMENTS_SERVICE","Event created successful");
                    if(protect){
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle("Event Passcode")
                                .setCancelable(false)
                                .setMessage("The passcode for this event is "+code+". This passcode is required to join this event")
                                .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        Gson gson = new Gson();
                                        String event_details = gson.toJson(event);
                                        editor.putString("Event_details",event_details);
                                        editor.putBoolean("Event_active",true);
                                        editor.apply();
                                        finish();
                                    }
                                })
                                .setNegativeButton("COPY PASSCODE TO CLIPBOARD", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Copied Text", ""+code);
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        Gson gson = new Gson();
                                        String event_details = gson.toJson(event);
                                        editor.putString("Event_details",event_details);
                                        editor.putBoolean("Event_active",true);
                                        editor.apply();
                                        finish();
                                    }
                                })
                                .create();
                        dialog.show();
                    }else{
//                        name.setText("");
//                        description.setText("");
//                        Snackbar.make(backGround,"Event created successfully!",Snackbar.LENGTH_INDEFINITE).setAction("SHARE LINK", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Intent sendIntent = new Intent();
//                                sendIntent.setAction(Intent.ACTION_SEND);
//                                sendIntent.putExtra(Intent.EXTRA_TEXT, "http://moments.app/?event="+uid+"_"+time);
//                                sendIntent.setType("text/plain");
//                                startActivity(Intent.createChooser(sendIntent, "Share link to your event"));
//                                finish();
//                            }
//                        }).show();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = preferences.edit();
                        Gson gson = new Gson();
                        String event_details = gson.toJson(event);
                        editor.putString("Event_details",event_details);
                        editor.putBoolean("Event_active",true);
                        editor.apply();
                    }
                }
            }
        });
    }

    private boolean validateFields() {
        if(TextUtils.isEmpty(name.getText())){
            Toast.makeText(context, "Event name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(description.getText())){
            Toast.makeText(context, "Event description cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
//                    Toast.makeText(context, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
                }
            });
            mGoogleApiClient.disconnect();
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
