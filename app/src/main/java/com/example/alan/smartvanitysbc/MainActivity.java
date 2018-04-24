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
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.DataOutputStream;
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

    ArrayList<Integer> rowSizeList;
    ArrayList<Integer> colSizeList;

    // mouse pointer
    OverlayView mView;
    WindowManager wm;
    WindowManager.LayoutParams wmParams;

    int control_notFirst;
    int tok;



    int width, height;

    boolean last;

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


        rowSizeList = new ArrayList<>();
        colSizeList = new ArrayList<>();

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


        DatabaseReference controlRef = database.getReference("users");
        controlRef = controlRef.child(uid).child("control");
        control_notFirst = 0;
        tok = 0;

        last = false;

        controlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (control_notFirst == 1) {

                    String control = dataSnapshot.child("controller").getValue().toString();

                    if (control.substring(0, 3).equals("@01")) {
                        mView.Update(-10, 0);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@02")) {
                        mView.Update(-30, 0);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@03")) {
                        mView.Update(-100, 0);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@04")) {
                        mView.Update(10, 0);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@05")) {
                        mView.Update(30, 0);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@06")) {
                        mView.Update(100, 0);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@07")) {
                        mView.Update(0, -10);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@08")) {
                        mView.Update(0, -30);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@09")) {
                        mView.Update(0, -100);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@10")) {
                        mView.Update(0, 10);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@11")) {
                        mView.Update(0, 30);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@12")) {
                        mView.Update(0, 100);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("@13")) {

                        int loc[] = new int[2];
                        //mView.getLocationOnScreen(loc);
                        loc[0] = mView.x;
                        loc[1] = mView.y;

                        try {
                            Process process = Runtime.getRuntime().exec("su");
                            DataOutputStream os = new DataOutputStream(process.getOutputStream());
                            String cmd = "/system/bin/input tap " + mView.x + " " + mView.y + "\n";
                            os.writeBytes(cmd);
                            os.writeBytes("exit\n");
                            os.flush();
                            os.close();
                            process.waitFor();
                        } catch (Exception e) {
                            Log.e("OKOK", e.getMessage());
                        }

                        Log.d("Debug", "x: " + loc[0] + ", y: " + loc[1]);
                        Log.d("control1", "15");

                    } else if (control.substring(0, 3).equals("@14")) {
                        if (tok % 2 == 0) {
                            String stringInput = dataSnapshot.child("input").getValue().toString();
                            Log.d("control1", "012: " + stringInput);
                            processStringInput(stringInput);
                            Log.d("control1", "12: " + stringInput);

                        }
                        tok++;
                    }
                } else {
                    control_notFirst = 1;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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

        //toggle for system app
//        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//        myIntent.setData(Uri.parse("package:" + getPackageName()));
//        startActivityForResult(myIntent, 1234);

        //toggle for system app
        //draw MP

        loadWidgets();


//        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//        myIntent.setData(Uri.parse("package:" + getPackageName()));
//        startActivityForResult(myIntent, 1234);
        drawMP();

        Log.d("DEBUG44", "pika");


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        Log.d("DEBUG456", "height: " + height);
        Log.d("DEBUG456", "width: " + width);



    }



    public void processStringInput(String stringInput) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String cmd = "/system/bin/input text " + stringInput;
            os.writeBytes(cmd);
//            os.writeBytes("exit\n");
            os.flush();
            os.close();
            process.waitFor();
        } catch (Exception e) {
            Log.e("OKOK", e.getMessage());
        }
    }

//    public void drawMP() {
//        mView = new OverlayView(this);
//        mView.setWillNotDraw(false);
//
//        wmParams = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,//TYPE_SYSTEM_ALERT,//TYPE_SYSTEM_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
//                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, //will cover status bar as well!!!
//                PixelFormat.TRANSLUCENT);
//        wmParams.gravity = Gravity.TOP | Gravity.LEFT;
//        wmParams.x = mView.x;
//        wmParams.y = mView.y;
//        Log.d("DEBUG123", mView.x + "");
//        Log.d("DEBUG123", mView.y + "");
//        //params.setTitle("Cursor");
//        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
//        wm.addView(mView, wmParams);
//
//        mView.ShowCursor(true);
//    }

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
        rowSizeList.clear();
        colSizeList.clear();

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


            Log.d("DEBUG33", Integer.parseInt(dataSnapshot.child("rowSize").child("val"+i).getValue().toString()) + "");
            rowSizeList.add(Integer.parseInt(dataSnapshot.child("rowSize").child("val"+i).getValue().toString()));

            Log.d("load data", dataSnapshot.child("colSize").child("val"+i).getValue().toString());
            colSizeList.add(Integer.parseInt(dataSnapshot.child("colSize").child("val"+i).getValue().toString()));

            int sbc_appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
            sbc_appWidgetIdList.add(sbc_appWidgetId);
            Log.d("load data", "SBC ID: " + sbc_appWidgetId);
        }

        for (int i = 0; i < rowSizeList.size(); i++) {
            Log.d("DEBUG22", "rowsizelist: " +  rowSizeList.get(i));
        }
    }

    public void examineData() {
        appWidgetInfoList.clear();

        for (int i = 0; i < widgetCount; i++) {
            for (int j = 0; j < infoList.size(); j++) {
                if (providerList.get(i).toString().equals(infoList.get(j).provider.toString())) {
                    Log.d("SBCDEBUG", providerList.get(i).toString());
                    Log.d("SBCDEBUG", infoList.get(j).provider.toString());
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

    private void configureWidget(int appWidgetId) {
//        Bundle extras = data.getExtras();
        Log.d("DEBUG123", "pikachu: " + appWidgetId);
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
            createWidget();
            Log.d("configureWidget", "else ended");
        }
        Log.d("configureWidget", "Ended");
    }

    public void putWidget() {
        int appWidgetId;
        AppWidgetProviderInfo info = null;
        AbsoluteLayout.LayoutParams params;
        AppWidgetHostView hostView;

        mainLayout.removeAllViews();

        for (int i = 0; i < widgetCount; i++) {
            appWidgetId = sbc_appWidgetIdList.get(i);

//            info = AppWidgetManager.getInstance(this.getApplicationContext()).getAppWidgetInfo(appWidgetId);
            info = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

            hostView = mAppWidgetHost.createView(MainActivity.this, appWidgetId, info);
//            hostView.setAppWidget(appWidgetId, info);

            params = new AbsoluteLayout.LayoutParams(rowSizeList.get(i), colSizeList.get(i), posListL.get(i), posListT.get(i));

            Log.d("DEBUG22", "kk: " +  rowSizeList.get(i));
            Log.d("DEBUG22", "kk: " + colSizeList.get(i));

//            hostView.setId(appWidgetId);
            mainLayout.addView(hostView, i, params);
        }
    }


    public void bindWidgets() {
        Intent intent;
        for (int i = 0; i < widgetCount; i++) {
//            intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, sbc_appWidgetIdList.get(i));
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, appWidgetInfoList.get(i).provider);
//            //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, appWidgetInfoList.get(i).getProfile());
//            startActivityForResult(intent, R.integer.REQUEST_BIND_APPWIDGET);

            boolean temp = mAppWidgetManager.bindAppWidgetIdIfAllowed(sbc_appWidgetIdList.get(i), appWidgetInfoList.get(i).provider);
//            Log.d("load data", temp + "");

            if (i == widgetCount-1) {
                last = true;
            }

            configureWidget(sbc_appWidgetIdList.get(i));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "Started");
        if (resultCode == RESULT_OK) {
            if (requestCode == R.integer.REQUEST_CREATE_APPWIDGET) {
                Log.d("onActivityResult", "REQUEST_CREATE_APPWIDGET");
                Log.d("pika", "3: ");
                createWidget();
                Log.d("onActivityResult", "REQUEST_CREATE_APPWIDGET END");
            } else if (requestCode == R.integer.REQUEST_BIND_APPWIDGET) {
                Log.d("bind", "success");
//                configureWidget(data);
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

                rowSizeList.add(sharedpreferences.getInt("rowSize" + i, -1));
                colSizeList.add(sharedpreferences.getInt("colSize" + i, -1));

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
            String key_rowSize = "rowSize" + i;
            String key_colSize = "colSize" + i;


            editor.putInt(key_id, sbc_appWidgetIdList.get(i));
            editor.putInt(key_positionL, posListL.get(i));
            editor.putInt(key_positionT, posListT.get(i));
            editor.putInt(key_rowSize, rowSizeList.get(i));
            editor.putInt(key_colSize, colSizeList.get(i));
        }
        editor.commit();
    }

    public void createWidget() {
        Log.d("createWidget", "started");
        Log.d("DEBUG123", sbc_appWidgetIdList + "");

        //fix
//        putWidget();

        if (last == true) {
            last = false;
            putWidget();
        }
        saveData();

//        loadData();
//        putWidget();
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

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        Log.d("foobar", requestCode + "");
//        Log.d("foobar", permissions + "");
//        Log.d("foobar", grantResults + "");
//    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void drawMP() {
        mView = new OverlayView(this);
        mView.setWillNotDraw(false);

        wmParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,//TYPE_SYSTEM_ALERT,//TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, //will cover status bar as well!!!
                PixelFormat.TRANSLUCENT);
        wmParams.gravity = Gravity.TOP | Gravity.LEFT;
        wmParams.x = mView.x;
        wmParams.y = mView.y;
        Log.d("DEBUG123", mView.x + "");
        Log.d("DEBUG123", mView.y + "");
        //params.setTitle("Cursor");
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(mView, wmParams);

        mView.ShowCursor(true);
    }
}
