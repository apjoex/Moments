package com.x.memories;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.x.memories.models.Post;

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
    String uid, localtime;
    Boolean privacy = false;
    SharedPreferences preferences;

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

        imageUri= Uri.parse(getIntent().getExtras().getString("image_uri"));
        time = getIntent().getStringExtra("time");
        preview = (CropImageView) findViewById(R.id.preview);
        send_btn = (Button)findViewById(R.id.send_btn);
        caption_box = (EditText)findViewById(R.id.caption_box);

        assert getSupportActionBar() != null;
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);


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
//        preview.startCrop(imageUri,
//                new CropCallback() {
//                    @Override
//                    public void onSuccess(Bitmap cropped) {}
//
//                    @Override
//                    public void onError() {}
//                },
//
//                new SaveCallback() {
//                    @Override
//                    public void onSuccess(Uri outputUri) {}
//
//                    @Override
//                    public void onError() {}
//                }
//        );

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
                preview.startCrop(imageUri,
                        new CropCallback() {
                            @Override
                            public void onSuccess(Bitmap cropped) {
                                upload(cropped);
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

    private void upload(Bitmap bmp) {
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bmp, null, null);
            Uri compressedUri =  Uri.parse(path);
            final ProgressDialog progressDialog = ProgressDialog.show(context,null,"Posting your moment...",false,false);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imagesRef = storage.getReferenceFromUrl("gs://memories-ec966.appspot.com/").child("images");
            imagesRef.child(time).putFile(compressedUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String photoUrl = taskSnapshot.getDownloadUrl().toString();
                            postToFirebase(photoUrl, progressDialog);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            if(progressDialog.isShowing()) {progressDialog.dismiss();}
                            Toast.makeText(context, "Something went wrong somewhere. Please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private void postToFirebase(String photoUrl, final ProgressDialog progressDialog) {

//        localtime = Utilities.getTime();
        localtime = String.valueOf(System.currentTimeMillis());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Post post = new Post(photoUrl,privacy,uid,localtime,preferences.getString("LOGGEDIN_NAME","Someone")+"\n"+caption_box.getText().toString());
        myRef.child("photos").child(uid+"_"+localtime).setValue(post, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(progressDialog.isShowing()){ progressDialog.dismiss(); }
                if (databaseError != null) {
                    Toast.makeText(context, "Something went wrong somewhere. Please try again", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show();
                    finish();
                }
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
