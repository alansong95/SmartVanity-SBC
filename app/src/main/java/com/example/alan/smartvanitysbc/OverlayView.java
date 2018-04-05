package com.example.alan.smartvanitysbc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * Created by Alan on 4/3/2018.
 */

public class OverlayView extends ViewGroup {
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
