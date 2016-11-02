package com.x.memories;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.x.memories.models.User;
import com.x.memories.services.NotificationService;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashScreen extends AppCompatActivity {

    Context context;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    SharedPreferences preferences;
    private FirebaseDatabase database;
    SharedPreferences.Editor editor;
    AppCompatButton signin_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        context = this;
        signin_btn = (AppCompatButton)findViewById(R.id.signin_btn);
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
                    editor = preferences.edit();
                    editor.putString("LOGGEDIN_UID",user.getUid());
                    editor.apply();

                    //Start notification service
                    Intent myIntent = new Intent(context, NotificationService.class);
                    context.startService(myIntent);

                    //Start timer
                    startTimer();

                } else {
                    editor = preferences.edit();
                    editor.putString("LOGGEDIN_UID","");
                    editor.putString("LOGGEDIN_NAME","");
                    editor.apply();
                    Log.d("Memories", "onAuthStateChanged:signed_out");

                }
            }

        };

        //Set light status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));
        }

        ColorStateList stateList =  ColorStateList.valueOf(Color.rgb(0,151,214));
        signin_btn.setSupportBackgroundTintList(stateList);
        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignIn();
            }
        });

        //Start timer
        if(preferences.getBoolean("Moments_signin",false)){
            signin_btn.setVisibility(View.INVISIBLE);
            startTimer();
        }
    }

    private void showSignIn() {
        View signinView = getLayoutInflater().inflate(R.layout.signin_form, null);
        final EditText emailText = (EditText)signinView.findViewById(R.id.email);
        final EditText passwordText = (EditText)signinView.findViewById(R.id.password);
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("Sign in")
                .setView(signinView)
                .setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(validateSignIn(emailText.getText().toString(),passwordText.getText().toString()))
                            signIn(emailText.getText().toString(),passwordText.getText().toString());
                    }
                })
                .setNeutralButton("SIGN UP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        showSignUp();
                    }
                }).create();
        dialog.show();
    }

    private void showSignUp() {
        View signupView = getLayoutInflater().inflate(R.layout.signup_form, null);
        final EditText fullnameText = (EditText)signupView.findViewById(R.id.fullname);
        final EditText usernameText = (EditText)signupView.findViewById(R.id.username);
        final EditText emailText = (EditText)signupView.findViewById(R.id.email);
        final EditText passwordText = (EditText)signupView.findViewById(R.id.password);
        final EditText confirmpasswordText = (EditText)signupView.findViewById(R.id.confirm_password);
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("Sign up")
                .setView(signupView)
                .setPositiveButton("SIGN UP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(validateSignUp(fullnameText,usernameText,emailText,passwordText,confirmpasswordText))
                            signUp(emailText.getText().toString(),passwordText.getText().toString(), fullnameText.getText().toString(), usernameText.getText().toString());
                    }
                })
                .setNeutralButton("SIGN IN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        showSignIn();
                    }
                }).create();
        dialog.show();
    }

    private void signUp(String email, String password, final String name, final String username) {
        final ProgressDialog progressDialog = ProgressDialog.show(context,null,"Signing up",false,false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Oops! Let's try that again", Toast.LENGTH_SHORT).show();
                        }else{
                            //create user details
                            createUser(task.getResult().getUser().getUid(), task.getResult().getUser().getEmail(), name, username);
                        }
                        if(progressDialog.isShowing()){progressDialog.dismiss();}
                        // ...
                    }
                });
    }

    private void createUser(final String uid, String email, String name, String username) {

        final DatabaseReference myRef = database.getReference();
        User user = new User(name, uid, email, username);
        myRef.child("users").child(uid).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                myRef.child("notifications").child(uid).child("initial").setValue("set", new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    }
                });
            }
        });

    }

    private boolean validateSignUp(EditText s, EditText s1, EditText s2, EditText s3, EditText s4) {
        boolean valid = true;

        if (TextUtils.isEmpty(s.getText().toString())) {
            Toast.makeText(context, "Please enter full name", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (TextUtils.isEmpty(s1.getText().toString())) {
            Toast.makeText(context, "Please enter username", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (TextUtils.isEmpty(s2.getText().toString()) || !s2.getText().toString().contains("@")) {
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (TextUtils.isEmpty(s3.getText().toString()) || s3.getText().toString().length() < 6) {
            Toast.makeText(context, "Please enter a valid password of a minimum of 6 characters", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (!s3.getText().toString().equals(s4.getText().toString())) {
            Toast.makeText(context, "Please confirm passwords match", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private boolean validateSignIn(String s, String s1) {
        boolean valid = true;

        if (TextUtils.isEmpty(s)) {
            Toast.makeText(context, "Please enter email address", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (TextUtils.isEmpty(s1)) {
            Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void signIn(String email, String password) {
        final ProgressDialog progressDialog = ProgressDialog.show(context,null,"Signing in",false,false);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, "Sign in successful", Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(context, NotificationService.class);
                    context.startService(myIntent);

                    editor = preferences.edit();
                    editor.putBoolean("Moments_signin",true);
                    editor.apply();

                    Intent intent = new Intent(context, Home.class);
                    startActivity(intent);
                    finish();

                }else{
                    Toast.makeText(context, "Sign in wasn't successful", Toast.LENGTH_SHORT).show();
                }
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void startTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, Home.class);
                startActivity(intent);
                finish();
            }
        }, 1250);
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
