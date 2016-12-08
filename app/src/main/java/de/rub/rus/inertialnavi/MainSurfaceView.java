package de.rub.rus.inertialnavi;


/**
 * Created by patrik on 08.12.2016.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * creates bar graph on surfaceView
 * Created by pbe on 25.11.15.
 * @author pbe
 */
public class MainSurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder myHolder;
    private Thread thread;

    /**
     * Constructor
     * @param context context of BarPlot - not used
     * @param sv surface object
     */
    public MainSurfaceView(Context context, SurfaceView sv){
        Log.d("MainSurfaceView", "constructor");
        myHolder = sv.getHolder();
        myHolder.addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        Log.d("MainSurfaceView", "surfaceCreated");
        Canvas canvas = arg0.lockCanvas();
        canvas.drawColor(Color.BLACK);
        arg0.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread=null;
    }

    @Override
    public void run() {

    }


    public void drawBarPlot(float valRect1, float valRect2, float valRect3){
        if (thread != null) {
            Log.d("MainSurfaceView", "drawStar");
            Canvas canvas = myHolder.lockCanvas();
            if (canvas != null) {
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);

                Paint paint1 = new Paint();
                paint1.setColor(Color.RED);
                Paint paint2 = new Paint();
                paint2.setColor(Color.YELLOW);
                Paint paint3 = new Paint();
                paint3.setColor(Color.GREEN);

                canvas.drawColor(Color.WHITE);

                float maxHeight = canvas.getHeight();
                float maxWidth = canvas.getWidth();

                float rectWidth = maxWidth/3.0f;
                float left1 = 0f;
                float left2 = rectWidth;
                float left3 = 2f * rectWidth;

                float right1 = left2;
                float right2 = left3;
                float right3 = maxWidth;

                float zeroHeight = maxHeight/2f;

                canvas.drawRect(left1, zeroHeight-((maxHeight-10)*valRect1/20), right1, zeroHeight, paint1);
                canvas.drawRect(left2, zeroHeight-((maxHeight-10)*valRect2/20), right2, zeroHeight, paint2);
                canvas.drawRect(left3, zeroHeight - ((maxHeight - 10) * valRect3 / 20), right3, zeroHeight, paint3);
                canvas.drawLine(0f, zeroHeight, maxWidth, zeroHeight, paint);
                canvas.drawLine(0f, 10f, maxWidth, 10f, paint);
                canvas.drawLine(0f, maxHeight-10f, maxWidth, maxHeight-10f, paint);

                myHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
