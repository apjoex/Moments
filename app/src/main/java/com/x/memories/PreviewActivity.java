package com.x.memories;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.x.memories.reusables.Utilities;
import com.x.memories.services.UploadService;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PreviewActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    Context context;
    private CropImageView preview;
    private Uri imageUri;
    private String time;
    Button send_btn;
    EditText caption_box;
    String uid;
    Boolean privacy = false;
    SharedPreferences preferences;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_preview);
        context = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    uid = user.getUid();
                    Log.d("Memories", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("Memories", "onAuthStateChanged:signed_out");
                }
            }

        };

        imageUri = Uri.parse(getIntent().getExtras().getString("image_uri"));
        time = getIntent().getStringExtra("time");
        preview = (CropImageView) findViewById(R.id.preview);
        send_btn = (Button) findViewById(R.id.send_btn);
        caption_box = (EditText) findViewById(R.id.caption_box);

        assert getSupportActionBar() != null;
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

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
//                                    Toast.makeText(context, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
                                }
                            });

                        } else {
                            //If everything went fine lets get latitude and longitude
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

//                            Toast.makeText(context, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
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
                                connectionResult.startResolutionForResult(PreviewActivity.this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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


        preview.setInitialFrameScale(0.75f);
        preview.startLoad(imageUri, new LoadCallback() {
                    @Override
                    public void onSuccess() {

                        preview.setHandleShowMode(CropImageView.ShowMode.SHOW_ON_TOUCH);
                        preview.setGuideShowMode(CropImageView.ShowMode.SHOW_ON_TOUCH);
//                        preview.setCropMode(CropImageView.CropMode.SQUARE);
//                        preview.setMinFrameSizeInDp(300);
                        preview.setAnimationEnabled(true);
//                        preview.setOutputMaxSize(700, 700);
                        preview.setCompressFormat(Bitmap.CompressFormat.JPEG);

                    }

                    @Override
                    public void onError() {}
                });

//        try {
//            imageStream = getContentResolver().openInputStream(imageUri);
//            bmp = BitmapFactory.decodeStream(imageStream);
//
//            Matrix matrix = new Matrix();
//            matrix.postRotate(90);
//            rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//
//            int nh = (int) ( rotatedBitmap.getHeight() * (512.0 / rotatedBitmap.getWidth()) );
//            scaled = Bitmap.createScaledBitmap(rotatedBitmap, 512, nh, true);
//
//            preview.setImageBitmap(scaled);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String caption = caption_box.getText().toString();
                Toast.makeText(context, "Posting your moment...", Toast.LENGTH_SHORT).show();
                preview.startCrop(imageUri,
                        new CropCallback() {
                            @Override
                            public void onSuccess(Bitmap cropped) {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("cropped_bmp", Utilities.BitMapToString(cropped));
                                editor.apply();

                                Intent myIntent = new Intent(context, UploadService.class);
                                myIntent.putExtra("caption",caption);
                                myIntent.putExtra("time",time);
                                myIntent.putExtra("privacy",privacy);
                                myIntent.putExtra("lat",currentLatitude);
                                myIntent.putExtra("long",currentLongitude);
                                startService(myIntent);
                                finish();
                            }

                            @Override
                            public void onError() {}
                        },
                        new SaveCallback() {
                            @Override
                            public void onSuccess(Uri outputUri) {}

                            @Override
                            public void onError() {}
                        });
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preview_menu, menu);
        MenuItem item = menu.findItem(R.id.action_private);
        item.setActionView(R.layout.switch_layout);
        final SwitchCompat priv_switch = (SwitchCompat)MenuItemCompat.getActionView(menu.findItem(R.id.action_private));
        priv_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                privacy = b;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
        }

        if(id == R.id.action_rotate_left){
            preview.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D); // rotate counter-clockwise by 90 degrees
        }

        if(id == R.id.action_rotate_right){
            preview.rotateImage(CropImageView.RotateDegrees.ROTATE_90D); // rotate clockwise by 90 degrees
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

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
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
