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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.alan.smartvanitysbc.Constants.TAG;


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

    // mouse pointer
    OverlayView mView;
    WindowManager wm;
    WindowManager.LayoutParams wmParams;

    int control_notFirst;
    int tok;

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

        DatabaseReference controlRef = database.getReference("users");
        controlRef = controlRef.child(uid).child("control");
        control_notFirst = 0;
        tok = 0;

        controlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (control_notFirst == 1) {

                    String control = dataSnapshot.child("controller").getValue().toString();

                    if (control.substring(0, 2).equals("up")) {
                        mView.Update(0, -10);
                        mView.postInvalidate();
                        Log.d("control1", "1");
                    } else if (control.substring(0, 3).equals("dup")) {
                        mView.Update(0, -30);
                        mView.postInvalidate();
                    } else if (control.substring(0, 3).equals("tup")) {
                        mView.Update(0, -100);
                        mView.postInvalidate();
                    } else if (control.substring(0, 4).equals("send")) {
                        if (tok % 2 == 0) {
                            String stringInput = dataSnapshot.child("StringInput").getValue().toString();
                            Log.d("control1", "012: " + stringInput);
                            processStringInput(stringInput);
                            Log.d("control1", "12: " + stringInput);

                        }
                        tok++;

                    } else if (control.substring(0, 4).equals("left")) {
                        mView.Update(-10, 0);
                        mView.postInvalidate();
                        Log.d("control1", "13");
                    } else if (control.substring(0, 4).equals("down")) {
                        mView.Update(0, +10);
                        mView.postInvalidate();
                        Log.d("control1", "14");
                    } else if (control.substring(0, 5).equals("click")) {


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
                    } else if (control.substring(0, 5).equals("right")) {
                        mView.Update(+10, 0);
                        mView.postInvalidate();
                        Log.d("control1", "16");
                    } else if (control.substring(0, 5).equals("dleft")) {
                        mView.Update(-30, 0);
                        mView.postInvalidate();
                    } else if (control.substring(0, 5).equals("tleft")) {
                        mView.Update(-100, 0);
                        mView.postInvalidate();
                    } else if (control.substring(0, 5).equals("ddown")) {
                        mView.Update(0, +30);
                        mView.postInvalidate();
                    } else if (control.substring(0, 5).equals("tdown")) {
                        mView.Update(0, +100);
                        mView.postInvalidate();
                    } else if (control.substring(0, 6).equals("dright")) {
                        mView.Update(+30, 0);
                        mView.postInvalidate();
                    } else if (control.substring(0, 6).equals("tright")) {
                        mView.Update(+100, 0);
                        mView.postInvalidate();
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
        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        myIntent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(myIntent, 1234);

        //toggle for system app
        //draw MP

        loadWidgets();
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

            int sbc_appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
            sbc_appWidgetIdList.add(sbc_appWidgetId);
            Log.d("load data", "SBC ID: " + sbc_appWidgetId);
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
            createWidget(data);
            Log.d("configureWidget", "else ended");
        }
        Log.d("configureWidget", "Ended");
    }

    public void putWidget() {
        int appWidgetId;
        AppWidgetProviderInfo info = null;
        RelativeLayout.LayoutParams params;
        AppWidgetHostView hostView;

        mainLayout.removeAllViews();

        for (int i = 0; i < widgetCount; i++) {
            appWidgetId = sbc_appWidgetIdList.get(i);

//            info = AppWidgetManager.getInstance(this.getApplicationContext()).getAppWidgetInfo(appWidgetId);
            info = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

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
        if (requestCode == 1234) {
            drawMP();
        }

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
        putWidget();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("foobar", requestCode + "");
        Log.d("foobar", permissions + "");
        Log.d("foobar", grantResults + "");
    }
}


class OverlayView extends ViewGroup {
    private Paint mLoadPaint;
    boolean mShowCursor;

    Bitmap cursor;
    public int x = 0,y = 0;

    public void Update(int nx, int ny) {
        x = x+nx; y = y+ny;
    }
    public void ShowCursor(boolean status) {
        mShowCursor = status;
    }
    public boolean isCursorShown() {
        return mShowCursor;
    }

    public OverlayView(Context context) {
        super(context);
        cursor = BitmapFactory.decodeResource(context.getResources(), R.drawable.mp);

        mLoadPaint = new Paint();
        mLoadPaint.setAntiAlias(true);
        mLoadPaint.setTextSize(10);
        mLoadPaint.setARGB(255, 255, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawText("Hello World", 0, 0, mLoadPaint);
        Log.d("debug13", "yo0");
        if (mShowCursor) {
            canvas.drawBitmap(cursor,x,y,null);
            Log.d("debug13", "yo");
        }
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}