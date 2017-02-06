package com.example.gaurav.sketchpad;

import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaurav on 22/1/17.
 */

public class DrawingView  extends View{

    private Path drawPath;
    private Paint drawPaint,canvasPaint;
    private int paintColor = 0xFF000000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float brushSize = 20, lastBrushSize = brushSize;
    private boolean erase = false;
    private ArrayList<Pair<Path,Paint>> pairs = new ArrayList<Pair<Path, Paint>>();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        }

    @Override
    protected void onDraw(Canvas canvas) {

        for(Pair<Path,Paint> p : pairs){
            canvas.drawPath(p.first,p.second);
        }

        //canvas.drawBitmap(canvasBitmap, 0, 0,drawPaint);
        canvas.drawPath(drawPath,drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch(event.getAction()){

            case MotionEvent.ACTION_DOWN:
                if(erase)
                    setErase(erase);
                drawPath.moveTo(touchX,touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX,touchY);
                break;

            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX,touchY);
                drawCanvas.drawPath(drawPath,drawPaint);
                pairs.add(new Pair<Path, Paint>(drawPath,drawPaint));
                setupDrawing();
                break;

            default: return false;
        }

        invalidate();
        return true;
    }

    public void setColor(String newColor){

        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        pairs.clear();
        invalidate();
    }

    public void setBrushSize(float newSize){

        float pixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize, getResources().getDisplayMetrics());
        brushSize = pixel;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize = lastSize;
    }

    public float getLastBrushSize(){
        return lastBrushSize;
    }

    public void setErase(boolean isErase){
        erase = isErase;
        if(erase)
            drawPaint.setColor(Color.WHITE);
    }

    public void undo(){
        if(pairs.size()>0){
            pairs.remove(pairs.size()-1);
            invalidate();
        }
    }
}
