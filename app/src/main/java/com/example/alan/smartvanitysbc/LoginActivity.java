package com.example.alan.smartvanitysbc;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Method;

public class LoginActivity extends AppCompatActivity {

    private static String KEY_SOME_INTEGER = "KEY_SOME_INTEGER";
    private static String TAG = "SmartVanity";

    private FirebaseAuth mAuth;
    private String uiState;

    ImageView mBackgroundImageView;
    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mConfirmPasswordEditText;
    TextView mSignUp;
    TextView mSignInTitle;
    Button mSignInButton;

    String uid;

    OverlayView mView;
    WindowManager wm;
    WindowManager.LayoutParams wmParams;

//    private BluetoothAdapter mBluetoothAdapter;
//    BluetoothConnectionService mBluetoothConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        uiState = "signup";
        setContentView(R.layout.activity_login);

        if ( savedInstanceState != null ) {
            int a = savedInstanceState.getInt(KEY_SOME_INTEGER);
        }

        findViews();
        setupListeners();
        populateUI();

//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        mBluetoothAdapter.enable();
//        mBluetoothAdapter.setName("SmartVanity-Song2018");
//
//        Method method;
//        try {
//            method = mBluetoothAdapter.getClass().getMethod("setScanMode", int.class, int.class);
//            method.invoke(mBluetoothAdapter,BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 0);
//            Log.e("invoke","method invoke successfully");
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//
//        mBluetoothConnection = new BluetoothConnectionService(LoginActivity.this);
//        FragmentManager



    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user is currently signed in.");
            // No user is signed in.
        } else {
            uid = currentUser.getUid();
            gotoHomeActivity(uid);
            // Go to home page.
        }
    }

    private void findViews() {
        mBackgroundImageView = findViewById(R.id.activity_login_background_image_view);
        mEmailEditText = findViewById(R.id.activity_login_email_edit_text);
        mPasswordEditText = findViewById(R.id.activity_login_password_edit_text);
        mConfirmPasswordEditText = findViewById(R.id.activity_login_confirm_password_edit_text);
        mSignInButton = findViewById(R.id.activity_login_signin_button);
        mSignUp = findViewById(R.id.activity_login_signup_text);
        mSignInTitle = findViewById(R.id.activity_login_signin_title_text_view);
    }

    private void setupListeners() {

//        mBackgroundImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // click callback
//                Intent someActivityIntent = new Intent(getApplicationContext(), SelectWidget.class);
//                Bundle stuff = new Bundle();
//                startActivity( someActivityIntent );
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // ui thread
//                    }
//                });
//            }
//        });

        mBackgroundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyBoard();
            }
        });

        mEmailEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Tapped the email edit text.");
            }
        });

        mPasswordEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Tapped the password edit text.");
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uiState == "signin") {
                    System.out.println("Attempted sign in.");
                    attemptSignIn();
                } else if (uiState == "signup") {
                    System.out.println("Attempting sign up.");
                    attemptSignUp();
                }
            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Smart Vanity", "Switching UI State");
                if (uiState == "signin") {
                    uiState = "signup";
                } else {
                    uiState = "signin";
                }
                populateUI();
            }
        });

    }

    private void populateUI() {
        Log.d("SmartVanity", "Populating UI with state: " + uiState);
        if (uiState == "signin") {
            mConfirmPasswordEditText.setVisibility(View.GONE);
            mSignInButton.setText("Sign In");
            mSignUp.setText("Don't have an account yet?\n\nSIGN UP");
            mSignInTitle.setText("Sign In");
        } else if (uiState == "signup") {
            mConfirmPasswordEditText.setVisibility(View.VISIBLE);
            mSignInButton.setText("Sign Up");
            mSignUp.setText("Already have an account?\n\nSIGN IN");
            mSignInTitle.setText("Sign Up");
        }
    }

    private void attemptSignUp() {
        String email = mEmailEditText.getText().toString();
        String pass1 = mPasswordEditText.getText().toString();
        String pass2 = mConfirmPasswordEditText.getText().toString();
        String password = "------";
        if (pass1.equals(pass2)) {
            password = pass1;
        } else {
            password = "";
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            gotoFirstTimeUserActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void attemptSignIn() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    gotoHomeActivity(user.getUid());
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void gotoHomeActivity(String uid) {
        Intent homeActivityIntent = new Intent(getApplicationContext(), MainActivity.class);

        SharedPreferences id_sharedpreferences = getSharedPreferences("id", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = id_sharedpreferences.edit();
        editor.putString("uid", uid);
        editor.commit();

        startActivity(homeActivityIntent);
    }

    private void gotoFirstTimeUserActivity() {
        Intent firstTimeUserActivityIntent = new Intent(getApplicationContext(), FirstTimeUserActivity.class);
        startActivity(firstTimeUserActivityIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int a = 1;
        outState.putInt( KEY_SOME_INTEGER, a );

        super.onSaveInstanceState(outState);
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


}
