package com.x.memories;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.transition.ArcMotion;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.x.memories.adapters.EventAdapter;
import com.x.memories.models.Event;
import com.x.memories.transitions.MorphDialogToFab;
import com.x.memories.transitions.MorphFabToDialog;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DialogActivity extends AppCompatActivity {

    private ViewGroup container;
    FrameLayout body;
    DatabaseReference ref;
    GeoFire geoFire;
    Context context;
    FirebaseDatabase database;
    Query query;
    AppCompatButton search_btn;
    LinearLayout no_event_box;
    LinearLayout event_box;
    List<String> eventIds = new ArrayList<>();
    ArrayList<Event> events = new ArrayList<>();
    ListView event_list;
    EventAdapter adapter;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        context = this;
        ref = FirebaseDatabase.getInstance().getReference("geofire/events");
        geoFire = new GeoFire(ref);
        body = (FrameLayout)findViewById(R.id.body);
        search_btn = (AppCompatButton)findViewById(R.id.search_btn);
        container = (ViewGroup) findViewById(R.id.container);
        no_event_box = (LinearLayout)findViewById(R.id.no_event_box);
        event_box = (LinearLayout)findViewById(R.id.event_box);
        event_list = (ListView)findViewById(R.id.event_list);
        database = FirebaseDatabase.getInstance();
        query = database.getReference("events").orderByChild("name").startAt("De").endAt("Dezzz");

        //Set light status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));
        }

        //Check if location turned on
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "Please turn on GPS on your device", Toast.LENGTH_SHORT).show();
        }

        //Location
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d("MOMENTS_LOCATION","Connection reach here");
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                            Log.d("MOMENTS_LOCATION","E no reach here");
                            no_event_box.setVisibility(View.VISIBLE);
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
                            Log.d("MOMENTS_LOCATION","E reach here");
                            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLatitude, currentLongitude), 0.6);
                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
//                                    Toast.makeText(context, key, Toast.LENGTH_SHORT).show();
                                    eventIds.add(key);
                                }

                                @Override
                                public void onKeyExited(String key) {
                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {

                                }

                                @Override
                                public void onGeoQueryReady() {
                                    if(eventIds.size() == 0){
                                        no_event_box.setVisibility(View.VISIBLE);
                                        event_box.setVisibility(View.INVISIBLE);
                                    }else{
                                        for (int i = 0; i < eventIds.size(); i++){
                                            final int finalI = i;
                                            database.getReference().child("events").child(eventIds.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    events.add(dataSnapshot.getValue(Event.class));
                                                    if(finalI == eventIds.size() - 1){
                                                        event_box.setVisibility(View.VISIBLE);
                                                        adapter = new EventAdapter(DialogActivity.this,context, events);
                                                        event_list.setAdapter(adapter);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d("MOMENTS_LOCATION","Connection no reach here");
                        if (connectionResult.hasResolution()) {
                            try {
                                // Start an Activity that tries to resolve the error
                                connectionResult.startResolutionForResult(DialogActivity.this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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


        search_btn.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        search_btn.setTextColor(getResources().getColor(R.color.colorPrimary));

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        setupSharedElementTransitions();

        View.OnClickListener dismissListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        };

        body.setOnClickListener(dismissListener);

//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Toast.makeText(context, dataSnapshot.toString(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


////
////        new CountDownTimer(2000, 1000) {
////            @Override
////            public void onTick(long l) {
////
////            }
////
////            @Override
////            public void onFinish() {
////                container.findViewById(R.id.progressBar).setVisibility(View.GONE);
////            }
////        }.start();
//
////        close_btn.setOnClickListener(dismissListener);
////
////        close_btn.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
////        close_btn.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setupSharedElementTransitions() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);

        Interpolator easeInOut = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in);

        MorphFabToDialog sharedEnter = new MorphFabToDialog();
        sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);

        MorphDialogToFab sharedReturn = new MorphDialogToFab();
        sharedReturn.setPathMotion(arcMotion);
        sharedReturn.setInterpolator(easeInOut);

        if (container != null) {
            sharedEnter.addTarget(container);
            sharedReturn.addTarget(container);
        }
        getWindow().setSharedElementEnterTransition(sharedEnter);
        getWindow().setSharedElementReturnTransition(sharedReturn);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        dismiss();
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
            events.clear();
            mGoogleApiClient.disconnect();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void dismiss() {
        setResult(Activity.RESULT_CANCELED);
        finishAfterTransition();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
