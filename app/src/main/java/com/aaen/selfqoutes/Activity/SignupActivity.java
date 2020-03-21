package com.aaen.selfqoutes.Activity;

import androidx.annotation.NonNull;
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
import android.widget.TextView;

import com.aaen.selfqoutes.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText password, mobile, name;
    private AutoCompleteTextView email;
    ProgressDialog progressDialog;

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
        setContentView(R.layout.activity_signup);


        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        Objects.requireNonNull(getSupportActionBar()).hide();

        Button signUp = findViewById(R.id.signup);
        TextView logIn = findViewById(R.id.createAccount);
        password = findViewById(R.id.password);
        mobile = findViewById(R.id.mobileNumber);
        email = findViewById(R.id.email);
        name = findViewById(R.id.fullName);

        signUp.setOnClickListener(this);
        logIn.setOnClickListener(this);


        //Firebase initialization
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
            case R.id.signup:

                String userName = name.getText().toString().trim();
                String userMobile = mobile.getText().toString().trim();
                String userEmail = email.getText().toString().trim();
                String userPassword = password.getText().toString().trim();
                createUserEmailAccount(userEmail, userPassword, userName, userMobile);

                break;

            case R.id.login:
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
                break;
        }
    }

    private void createUserEmailAccount(final String userEmail, final String userPassword, final String userName, final String userMobile) {

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Please write email");
            return;
        }
        if (TextUtils.isEmpty(userPassword)) {
            email.setError("Please write password");
            return;
        }
        if (TextUtils.isEmpty(userName)) {
            email.setError("Please write username");
            return;
        }
        if (TextUtils.isEmpty(userMobile)) {
            email.setError("Please write mobile no.");
            return;
        }

        progressDialog = ProgressDialog.show(SignupActivity.this,"Processing...", "Please wait", false, true);
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            //we take user to our journal activity
                            currentUser = firebaseAuth.getCurrentUser();
                            if (currentUser != null) {
                                final String currentUserId = currentUser.getUid();


                                //create a user Map so we can create a user in the user collection
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId", currentUserId);
                                userObj.put("userName", userName);
                                userObj.put("userMobile", userMobile);
                                userObj.put("userEmail", userEmail);
                                userObj.put("userPassword", userPassword);

                                //save to our firestore database
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (Objects.requireNonNull(task.getResult()).exists()) {
                                                                    if (progressDialog.isShowing())
                                                                        progressDialog.dismiss();
                                                                    String name = task.getResult().getString("userName");
                                                                    String mobile = task.getResult().getString("userMobile");
                                                                    String email = task.getResult().getString("userEmail");
                                                                    String pass = task.getResult().getString("userPassword");

                                                                    JournalApi journalApi = JournalApi.getInstance();
                                                                    journalApi.setUserId(currentUserId);
                                                                    journalApi.setUserName(name);
                                                                    journalApi.setUserEmail(email);
                                                                    journalApi.setUserMobile(mobile);

                                                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                                                    intent.putExtra("userId", currentUserId);
                                                                    intent.putExtra("userName", name);
                                                                    intent.putExtra("userMobile", mobile);
                                                                    intent.putExtra("userEmail", email);
                                                                    intent.putExtra("userPassword", pass);
                                                                    startActivity(intent);
                                                                }else {

                                                                }
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });
                            }
                        } else {
                            //something went wrong
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
        startActivity(new Intent(SignupActivity.this, ChooseActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}
