package com.x.memories.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.x.memories.R;
import com.x.memories.SplashScreen;
import com.x.memories.models.User;

/**
 * Created by AKINDE-PETERS on 8/29/2016.
 */
public class FavFragment extends Fragment {

    Context context;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private TextView name;
    private TextView email;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("Memories", "onAuthStateChanged:signed_in:" + user.getUid());
//                    Toast.makeText(context, user.getEmail(), Toast.LENGTH_SHORT).show();
                    editor = preferences.edit();
                    editor.putString("LOGGEDIN_UID",user.getUid());
                    editor.apply();
                    email.setText(user.getEmail());
                    DatabaseReference myRef = database.getReference();
                    myRef.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User activeUser = dataSnapshot.getValue(User.class);
                            name.setText(activeUser.getName());
                            editor = preferences.edit();
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
                    editor = preferences.edit();
                    editor.putString("LOGGEDIN_UID","");
                    editor.putString("LOGGEDIN_NAME","");
                    editor.apply();
                    Log.d("Memories", "onAuthStateChanged:signed_out");
                }
                // ...
            }

        };
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_fav, container, false);
        Button signout_btn = (Button) v.findViewById(R.id.signout_btn);
        name = (TextView)v.findViewById(R.id.name);
        email = (TextView)v.findViewById(R.id.email);
        TextView fav_number = (TextView) v.findViewById(R.id.fav_number);

        name.setTypeface(null, Typeface.ITALIC);
        email.setTypeface(null, Typeface.ITALIC);
        fav_number.setTypeface(null, Typeface.ITALIC);

        //Sign Out
        signout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setMessage("Are you sure?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseAuth.getInstance().signOut();
                                editor = preferences.edit();
                                editor.putBoolean("Moments_signin",false);
                                editor.apply();
                                Intent intent = new Intent(getActivity(), SplashScreen.class);
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
        });
        return v;
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

}
