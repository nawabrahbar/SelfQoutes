package com.aaen.selfqoutes.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aaen.selfqoutes.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.Objects;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText password;
    private AutoCompleteTextView email;
    private ProgressDialog progressDialog;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseAuth.AuthStateListener authStateListener;

    //FireStore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        Objects.requireNonNull(getSupportActionBar()).hide();

        Button login = findViewById(R.id.login);
        TextView createAccount = findViewById(R.id.createAccount);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        login.setOnClickListener(this);
        createAccount.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    //user is already logged in..

                } else {
                    //no user yet logged in..

                }
            }
        };

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                loginEmailPasswordUser(email.getText().toString().trim(), password.getText().toString().trim());
                break;

            case R.id.createAccount:
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void loginEmailPasswordUser(String getEmail, String getPassword) {

        if (TextUtils.isEmpty(getEmail)) {
            email.setError("Please enter email");
            return;
        }
        if (TextUtils.isEmpty(getPassword)) {
            password.setError("Please enter password");
            return;
        }

        progressDialog = ProgressDialog.show(this, "Connecting..", "Please wait..", false, false);
        firebaseAuth.signInWithEmailAndPassword(getEmail, getPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            String currentUserId = user.getUid();

                            collectionReference
                                    .whereEqualTo("userId", currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                            assert queryDocumentSnapshots != null;
                                            if (!queryDocumentSnapshots.isEmpty()) {

                                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                    JournalApi journalApi = JournalApi.getInstance();
                                                    journalApi.setUserName(snapshot.getString("userName"));
                                                    String id = snapshot.getString("userId");
                                                    journalApi.setUserId(snapshot.getString("userId"));
                                                    journalApi.setUserEmail(snapshot.getString("userEmail"));
                                                    journalApi.setUserMobile(snapshot.getString("userMobile"));
                                                }
                                                //Go to list activity
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LoginActivity.this, ChooseActivity.class));
        finish();
    }


}
