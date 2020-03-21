package com.aaen.selfqoutes.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaen.selfqoutes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

import model.Journal;
import util.JournalApi;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE = 1;
    private static final String TAG = "MainActivity";
    private ImageView photoJournal;
    private ProgressDialog progressDialog;
    private EditText title, thought;
    private ImageView cameraJournal, cameraJournalCorner;

    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    //Connection to firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("model");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        Objects.requireNonNull(getSupportActionBar()).hide();


        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        photoJournal = findViewById(R.id.wallJournal);
        cameraJournal = findViewById(R.id.cameraJournal);
        cameraJournalCorner = findViewById(R.id.cameraJournal2);
        cameraJournalCorner.setVisibility(View.INVISIBLE);
        Button saveJournal = findViewById(R.id.saveJournal);
        TextView userId = findViewById(R.id.userId);
        TextView userName = findViewById(R.id.userName);
        title = findViewById(R.id.title);
        thought = findViewById(R.id.thought);


        saveJournal.setOnClickListener(this);
        cameraJournal.setOnClickListener(this);

        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUserName();
            userName.setText(currentUserName);
            userId.setText(currentUserId);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {

                } else {

                }
            }
        };

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cameraJournal:
                //Get journal photo from gallery
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("imgae/*");
                startActivityForResult(gallery, GALLERY_CODE);

                if (photoJournal.getDrawable() == null) {
                    cameraJournalCorner.setVisibility(View.VISIBLE);
                    cameraJournal.setVisibility(View.INVISIBLE);
                }
                break;

            case R.id.cameraJournal2:
                //Get journal photo from gallery
                Intent gallery2 = new Intent(Intent.ACTION_GET_CONTENT);
                gallery2.setType("imgae/*");
                startActivityForResult(gallery2, GALLERY_CODE);

                break;


            case R.id.saveJournal:
                //Save journal
                saveOurJournal();
                break;
        }
    }

    private void saveOurJournal() {
        final String getTitle = title.getText().toString();
        final String getThought = thought.getText().toString();

        if (TextUtils.isEmpty(getTitle)) {
            title.setError("Please add title");
            return;
        }
        if (TextUtils.isEmpty(getThought)) {
            thought.setError("Please add thought");
            return;
        }

        if (imageUri != null) {

            progressDialog = ProgressDialog.show(this, "Saving...", "Please wait.", false, false);
            final StorageReference filepath = storageReference // ..../journal_images/our_image.jpeg
                    .child("journal_images")
                    .child("IMG_" + Timestamp.now().getSeconds()); // IMG_578877357 (Random name)

            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();

                                    Journal journal = new Journal();
                                    journal.setTitle(getTitle);
                                    journal.setThought(getThought);
                                    journal.setImageUrl(imageUrl);
                                    journal.setUserName(currentUserName);
                                    journal.setUserId(currentUserId);
                                    journal.setTimeAdded(new Timestamp(new Date()));

                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    if (progressDialog.isShowing())
                                                        progressDialog.dismiss();

                                                    startActivity(new Intent(MainActivity.this, JounalListActivity.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                            Log.d(TAG, "onFailure" + e.getMessage());
                        }
                    });
        } else {
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            Toast.makeText(this, "Please choose a pic", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                photoJournal.setImageURI(imageUri); //Showing image..
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
