package com.example.alan.smartvanity_sbc;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int NUMBER_OF_WIDGET_FIELD = 5;

    // Variables for Firebase Database Widget
    private FirebaseDatabase database;
    int notFirst;

    // Variables for FIrebase Database Video
    int vid_notFirst;

    Intent login_intent;
    String uid;


    // Variables for Widgets
    ViewGroup mainLayout;
    AppWidgetManager mAppWidgetManager;
    AppWidgetHost mAppWidgetHost;
    List<AppWidgetProviderInfo> infoList;
    int widgetCount;
    AppWidgetProviderInfo appWidgetInfo;
    ArrayList<String> providerList;
    ArrayList<Integer> posListL;
    ArrayList<Integer> posListT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init for Widget
        mainLayout = (ViewGroup) findViewById(R.id.main_layout);
        mAppWidgetHost = new AppWidgetHost(this, R.id.APPWIDGET_HOST_ID);
        mAppWidgetManager = AppWidgetManager.getInstance(this);

        // Init for Firebase Database Widget
        login_intent = getIntent();
        uid = login_intent.getStringExtra("uid");
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef = myRef.child(uid).child("widgets");
        widgetCount = 0;
        final TextView textView = (TextView) findViewById(R.id.text) ;
        textView.setText("Activity Started");
        notFirst = 0;

        // Init for Firebase Database Video
        DatabaseReference vidRef = database.getReference("users");
        vidRef = vidRef.child(uid).child("video");
        vid_notFirst = 0;
        final Intent vid_intent = new Intent(this, Video.class);

        vidRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (vid_notFirst == 1) {
                    if (dataSnapshot.exists()) {
                        Log.d("onDataChange vid", "Change Detected");
                        Log.d("updated vid: ", dataSnapshot.getValue().toString());
                        vid_intent.putExtra("link", dataSnapshot.getValue().toString());
                        startActivity(vid_intent);
                    } else {
                        Log.d("onDataChange vid", "Change Detected: deleted?");
                    }
                } else {
                    Log.d("onDataChange vid", "First");
                    vid_notFirst = 1;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("updated").exists()) {
                        textView.setText("change detected");
                        Log.d("onDataChange", "Change Detected");
                        Log.d("updated: ", dataSnapshot.child("updated").getValue().toString());
                        installWidget(dataSnapshot);
                    } else {
                        Log.d("onDataChange", "Change Detected: deleted?");
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void installWidget(DataSnapshot dataSnapshot) {
        // put data in to lists
        providerList = new ArrayList<>();
        posListL = new ArrayList<>();
        posListT = new ArrayList<>();
        int widgetCount = Integer.parseInt(dataSnapshot.child("widget count").child("val").getValue().toString());

        Log.d("install Widget", Integer.toString(widgetCount));

        for (int i =0; i < widgetCount; i++) {
            Log.d("install Widget", dataSnapshot.child("selected").child("val"+i).getValue().toString());
            providerList.add(dataSnapshot.child("selected").child("val"+i).getValue().toString());
            Log.d("install Widget", dataSnapshot.child("positionL").child("val"+i).getValue().toString());
            posListL.add(Integer.parseInt(dataSnapshot.child("positionL").child("val"+i).getValue().toString()));

            posListT.add(Integer.parseInt(dataSnapshot.child("positionT").child("val"+i).getValue().toString()));
            Log.d("install Widget", dataSnapshot.child("positionT").child("val"+i).getValue().toString());

        }

        Log.d("install Widget", providerList.toString());
        Log.d("install Widget", posListL.toString());
        Log.d("install Widget", posListT.toString());



        String temp;
        infoList = mAppWidgetManager.getInstalledProviders();

        Log.d("install Widget", infoList.toString());

        for (int j = 0; j < widgetCount; j++) {
            for (int i = 0; i < infoList.size(); i++) {
                temp = infoList.get(i).provider.toString();

                if (providerList.get(j).equals(temp)) {
                    appWidgetInfo = infoList.get(i);
                    break;
                }
            }

            AppWidgetHostView hostView = new AppWidgetHostView(this);
            hostView = mAppWidgetHost.createView(this, 0, appWidgetInfo);
            hostView.setAppWidget(0, appWidgetInfo);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = posListL.get(j);
            params.topMargin = posListT.get(j);

            mainLayout.addView(hostView, j, params);
        }
    }
}
