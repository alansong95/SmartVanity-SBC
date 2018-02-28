package com.example.alan.smartvanity_sbc;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirstTimeUserActivity extends AppCompatActivity {

    ImageView mBackgroundImageView;
    EditText mFirstNameEditText;
    EditText mLastNameEditText;
    Button mGetStartedButton;


    public String firstName;
    public String lastName;
    public String uid;
    public String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_user);
        findViews();
        setupListeners();
    }

    private void setupListeners() {
        mBackgroundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyBoard();
            }
        });

        mGetStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (setUpData()) {
                    attemptGetStarted();
                } else {
                    Log.d(Constants.TAG, "Something went wrong...");
                }
            }
        });
    }

    private boolean setUpData() {
        Log.d(Constants.TAG, "Setting up the data...");
        firstName = mFirstNameEditText.getText().toString();
        lastName = mLastNameEditText.getText().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            email = user.getEmail();
        } else {
            return false;
        }
        return true;
    }

    private void attemptGetStarted() {
        if (mFirstNameEditText.getText().toString() != null || mLastNameEditText.getText().toString() != null) {
            Log.d(Constants.TAG, "Getting started...");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(firstName + " " + lastName).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(Constants.TAG, "Successfully acquired first and last name. Attempting to update database.");
                    attemptUpdateDatabase();
                    gotoHomeActivity();
                }
            });
        }
    }

    private void attemptUpdateDatabase() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(uid).child("email").setValue(email);
        mDatabase.child("users").child(uid).child("first-name").setValue(firstName);
        mDatabase.child("users").child(uid).child("last-name").setValue(lastName);
    }

    private void gotoHomeActivity() {
        Intent homeActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        Bundle bundle = new Bundle();
        startActivity(homeActivityIntent);
    }

    private void findViews() {
        mBackgroundImageView = findViewById(R.id.activity_login_background_image_view);
        mFirstNameEditText = findViewById(R.id.activity_first_time_user_first_name);
        mLastNameEditText = findViewById(R.id.activity_first_time_user_last_name);
        mGetStartedButton = findViewById(R.id.activity_first_time_user_get_started);
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
