package com.example.alan.smartvanitysbc;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    public static final int NUMBER_OF_WIDGET_FIELD = 5;

    SharedPreferences sharedpreferences;
    // Variables for Firebase Database Widget
    private FirebaseDatabase database;
    int notFirst;

    // Variables for FIrebase Database Video
    int vid_notFirst;

    Intent login_intent;
    String uid;

    Gson gson;

    List<AppWidgetProviderInfo> infoList;

    // Variables for Widgets
    ViewGroup mainLayout;
    AppWidgetManager mAppWidgetManager;
    AppWidgetHost mAppWidgetHost;


    int widgetCount;
    ArrayList<ComponentName> providerList;
    ArrayList<String> providerStringList;
    ArrayList<Integer> posListL;
    ArrayList<Integer> posListT;
    ArrayList<Integer> appWidgetIdList;
    ArrayList<AppWidgetProviderInfo> appWidgetInfoList;
    ArrayList<Integer> sbc_appWidgetIdList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gson = new Gson();

        SharedPreferences id_sharedpreferences = getSharedPreferences("id", Context.MODE_PRIVATE);
        sharedpreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        widgetCount = sharedpreferences.getInt("WidgetCount", 0);

        Log.d("DEBUG123", widgetCount +"");

        providerList = new ArrayList<>();
        posListL = new ArrayList<>();
        posListT = new ArrayList<>();
        appWidgetIdList = new ArrayList<>();
        appWidgetInfoList = new ArrayList<>();
        sbc_appWidgetIdList = new ArrayList<>();
        providerStringList = new ArrayList<>();

        // Init for Widget
        mainLayout = (ViewGroup) findViewById(R.id.main_layout);
        mAppWidgetHost = new AppWidgetHost(this, R.id.APPWIDGET_HOST_ID);
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        infoList = mAppWidgetManager.getInstalledProviders();

        // Init for Firebase Database Widget
        uid = id_sharedpreferences.getString("uid", "");
        Log.d("DEBUG123", uid +"");
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef = myRef.child(uid).child("widgets");
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
                if (notFirst == 1) {
                    if (dataSnapshot.child("updated").exists()) {
                        Log.d("onDataChange", "Change Detected");
                        loadDataFromDatabase(dataSnapshot);
                        examineData();

                        bindWidgets();

                        //putWidget();

                    } else {
                        Log.d("onDataChange", "Change Detected: deleted?");
                    }
                } else {
                    notFirst = 1;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        loadWidgets();
    }

    public void loadWidgets() {
        loadData();
        putWidget();
    }


    public void loadDataFromDatabase(DataSnapshot dataSnapshot) {
        providerList.clear();
        posListL.clear();
        posListT.clear();
        appWidgetIdList.clear();
        sbc_appWidgetIdList.clear();

        widgetCount = Integer.parseInt(dataSnapshot.child("widget_count").child("val").getValue().toString());
        Log.d("load data", Integer.toString(widgetCount));

        for (int i = 0; i < widgetCount; i++) {
            Log.d("load data", dataSnapshot.child("provider").child("val"+i).getValue().toString());
            providerList.add(gson.fromJson(dataSnapshot.child("provider").child("val"+i).getValue().toString(), ComponentName.class));
            Log.d("load data", dataSnapshot.child("positionL").child("val"+i).getValue().toString());
            posListL.add(Integer.parseInt(dataSnapshot.child("positionL").child("val"+i).getValue().toString()));
            Log.d("load data", dataSnapshot.child("positionT").child("val"+i).getValue().toString());
            posListT.add(Integer.parseInt(dataSnapshot.child("positionT").child("val"+i).getValue().toString()));
            Log.d("load data", dataSnapshot.child("id").child("val"+i).getValue().toString());
            appWidgetIdList.add(Integer.parseInt(dataSnapshot.child("id").child("val"+i).getValue().toString()));


            sbc_appWidgetIdList.add(this.mAppWidgetHost.allocateAppWidgetId());
        }
    }

    public void examineData() {
        appWidgetInfoList.clear();

        for (int i = 0; i < widgetCount; i++) {
            for (int j = 0; j < infoList.size(); j++) {
                if (providerList.get(i).toString().equals(infoList.get(j).provider.toString())) {
                    appWidgetInfoList.add(infoList.get(j));
                    break;
                }
            }
        }

        Log.d("examine data", providerList + "");
    }

//    public void configureData() {
//        for (int i = 0; i < widgetCount; i++) {
////            Log.d("configuredata", appWidgetInfoList.get(i) + "");
//            Log.d("configuredata", sbc_appWidgetIdList.get(i) + "");
//            Log.d("configuredata", appWidgetIdList.get(i) + "");
//
////            configureWidget(appWidgetInfoList.get(i), sbc_appWidgetIdList.get(i));
//            configureWidget(sbc_appWidgetIdList.get(i));
//        }
//    }

    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo newInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (newInfo.configure != null) {
            Log.d("configureWidget", "null");
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(newInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, R.integer.REQUEST_CREATE_APPWIDGET);
            Log.d("configureWidget", "null ended");
        } else {
            Log.d("configureWidget", "else started");
            putWidget();
            Log.d("configureWidget", "else ended");
        }
        Log.d("configureWidget", "Ended");
    }

    public void putWidget() {
        int appWidgetId;
        String data;
        WidgetHolder holder;
        AppWidgetProviderInfo info = null;
        RelativeLayout.LayoutParams params;
        AppWidgetHostView hostView;

        mainLayout.removeAllViews();

        for (int i = 0; i < widgetCount; i++) {
            appWidgetId = sbc_appWidgetIdList.get(i);

            info = AppWidgetManager.getInstance(this.getApplicationContext()).getAppWidgetInfo(appWidgetId);

            hostView = mAppWidgetHost.createView(MainActivity.this.getApplicationContext(), appWidgetId, info);
            hostView.setAppWidget(appWidgetId, info);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = posListL.get(i);
            params.topMargin = posListT.get(i);

            hostView.setId(appWidgetId);

            mainLayout.addView(hostView, i, params);
        }
    }


    public void bindWidgets() {
        Intent intent;
        for (int i = 0; i < widgetCount; i++) {
            intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, sbc_appWidgetIdList.get(i));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerList.get(i));
            //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, appWidgetInfoList.get(i).getProfile());
            startActivityForResult(intent, R.integer.REQUEST_BIND_APPWIDGET);

//            boolean temp = mAppWidgetManager.bindAppWidgetIdIfAllowed(sbc_appWidgetIdList.get(i), providerList.get(i));
//            Log.d("load data", temp + "");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "Started");
        if (resultCode == RESULT_OK) {
            if (requestCode == R.integer.REQUEST_CREATE_APPWIDGET) {
                Log.d("onActivityResult", "REQUEST_CREATE_APPWIDGET");
                Log.d("pika", "3");
                createWidget(data);
                Log.d("onActivityResult", "REQUEST_CREATE_APPWIDGET END");
            } else if (requestCode == R.integer.REQUEST_BIND_APPWIDGET) {
                Log.d("bind", "success");
                configureWidget(data);
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            Log.d("onActivityResult", "RESULT_CANCELED");
        }
        Log.d("onActivityResult", "Ended");
    }

    public void loadData() {
        if (sharedpreferences.contains("id0")) {
            for (int i = 0; i < widgetCount; i++) {
                //providerList.add(sharedpreferences.getString("selected" + i, null));
                sbc_appWidgetIdList.add(sharedpreferences.getInt("id" + i, -1));
                posListL.add(sharedpreferences.getInt("positionL" + i, -1));
                posListT.add(sharedpreferences.getInt("positionT" + i, -1));
                //savedInfoList.add(sharedpreferences.getString("info" + i, null));
            }
        }
    }

    public void saveData() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear().commit();
        editor.putInt("WidgetCount", widgetCount);

        for (int i = 0; i < widgetCount; i++) {
            String key_id = "id" + i;
            String key_positionL = "positionL" + i;
            String key_positionT = "positionT" + i;

            editor.putInt(key_id, sbc_appWidgetIdList.get(i));
            editor.putInt(key_positionL, posListL.get(i));
            editor.putInt(key_positionT, posListT.get(i));
        }
        editor.commit();
    }

    public void createWidget(Intent data) {
        Log.d("createWidget", "started");
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        AppWidgetProviderInfo info = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        AppWidgetHostView hostView = mAppWidgetHost.createView(MainActivity.this.getApplicationContext(), appWidgetId, info);
        hostView.setAppWidget(appWidgetId, info);
        hostView.setId(appWidgetId);
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//        params.leftMargin = posListL.get(i);
//        params.topMargin = posListT.get(i);
        hostView.setId(appWidgetId);

        saveData();
        loadData();
        putWidget();
        //mainLayout.addView(hostView, widgetCount, params);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAppWidgetHost.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAppWidgetHost.stopListening();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("foobar", requestCode + "");
        Log.d("foobar", permissions + "");
        Log.d("foobar", grantResults + "");

//        if (requestCode == REQUEST_PERMISSION) {
//            // for each permission check if the user granted/denied them
//            // you may want to group the rationale in a single dialog,
//            // this is just an example
//            for (int i = 0, len = permissions.length; i < len; i++) {
//                String permission = permissions[i];
//                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
//                    // user rejected the permission
//                    boolean showRationale = shouldShowRequestPermissionRationale( permission );
//                    if (! showRationale) {
//                        // user also CHECKED "never ask again"
//                        // you can either enable some fall back,
//                        // disable features of your app
//                        // or open another dialog explaining
//                        // again the permission and directing to
//                        // the app setting
//                    } else if (Manifest.permission.WRITE_CONTACTS.equals(permission)) {
//                        showRationale(permission, R.string.permission_denied_contacts);
//                        // user did NOT check "never ask again"
//                        // this is a good place to explain the user
//                        // why you need the permission and ask if he wants
//                        // to accept it (the rationale)
//                    } else if ( /* possibly check more permissions...*/ ) {
//                    }
//                }
//            }
//        }
    }
}
