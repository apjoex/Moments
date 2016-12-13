package com.x.memories;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.x.memories.fragments.EventFragment;
import com.x.memories.fragments.PhotoFragment;
import com.x.memories.fragments.VideoFragment;
import com.x.memories.models.User;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity {

    Context context;
    BottomBar bottomBar;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    SharedPreferences preferences;
    DatabaseReference ref;
    GeoFire geoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("geofire/photos");
        geoFire = new GeoFire(ref);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        //Set light status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));
        }

        //Set up toolbar
        assert getSupportActionBar() != null;
        View view = getLayoutInflater().inflate(R.layout.toolbar_layout, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        layout.setMargins(0,0,60,0);
        getSupportActionBar().setCustomView(view, layout);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.eye_mask);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Memories", "onAuthStateChanged:signed_in:" + user.getUid());
                    DatabaseReference myRef = database.getReference();
                    myRef.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User activeUser = dataSnapshot.getValue(User.class);
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("LOGGEDIN_NAME",activeUser.getName());
                            editor.apply();
//                            Toast.makeText(context, activeUser.getName(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    // User is signed out
                    Log.d("Memories", "onAuthStateChanged:signed_out");
                }
                // ...
            }

        };


        //Set up bottom bar
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                //Image tab selected
                if (tabId == R.id.tab_image) {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.contentContainer, new PhotoFragment()).commit();
                }

                //Video tab selected
                if (tabId == R.id.tab_video) {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.contentContainer, new VideoFragment()).commit();
                }

                //Favourite tab selected
                if (tabId == R.id.tab_event) {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.contentContainer, new EventFragment()).commit();
                }
            }
        });

//        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
//            @Override
//            public void onTabReSelected(@IdRes int tabId) {
//                //Image tab selected
//                if (tabId == R.id.tab_image) {
//                    FragmentManager manager = getSupportFragmentManager();
//                    FragmentTransaction transaction = manager.beginTransaction();
//                    transaction.replace(R.id.contentContainer, new PhotoFragment()).commit();
//                }
//
//                //Video tab selected
//                if (tabId == R.id.tab_video) {
//                    FragmentManager manager = getSupportFragmentManager();
//                    FragmentTransaction transaction = manager.beginTransaction();
//                    transaction.replace(R.id.contentContainer, new VideoFragment()).commit();
//                }
//
//                //Favourite tab selected
//                if (tabId == R.id.tab_fav) {
//                    FragmentManager manager = getSupportFragmentManager();
//                    FragmentTransaction transaction = manager.beginTransaction();
//                    transaction.replace(R.id.contentContainer, new FavFragment()).commit();
//                }
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        menu.getItem(0).setTitle(preferences.getString("LOGGEDIN_NAME","Profile").equals("")?"Profile":preferences.getString("LOGGEDIN_NAME","Profile"));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            Intent intent = new Intent(context, Requests.class);
            startActivity(intent);
        }

//        if (id == R.id.action_feedback) {
//            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                    "mailto", "apjoex@gmail.com", null));
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Moments");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
//            startActivity(Intent.createChooser(emailIntent, "Send feedback"));
//        }

        if (id == R.id.action_about) {
            View view = getLayoutInflater().inflate(R.layout.toolbar_layout, null);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setCustomTitle(view)
                    .setMessage("Moments was created in the moment for your moments!👍\n\nVersion: "+BuildConfig.VERSION_NAME)
                    .setPositiveButton("GET IN TOUCH", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "apjoex@gmail.com", null));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Moments");
                            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                            startActivity(Intent.createChooser(emailIntent, "Get in touch"));
                        }
                    })
                    .create();
            dialog.show();
        }

        if(id == R.id.sign_out){
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage("Are you sure?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                            FirebaseAuth.getInstance().signOut();
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("Moments_signin",false);
                            editor.putString("LOGGEDIN_UID","");
                            editor.putString("LOGGEDIN_NAME","");
                            editor.apply();
                            Intent intent = new Intent(context, SplashScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
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
    public void onBackPressed() {
        showExit();
    }

    private void showExit() {
        AlertDialog.Builder exit_builder = new AlertDialog.Builder(context);
        exit_builder.setTitle("Exit")
                .setMessage("Do you really want to exit this app?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeApp();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    public void closeApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
