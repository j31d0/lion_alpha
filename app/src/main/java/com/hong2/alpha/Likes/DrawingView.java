package com.hong2.alpha.Likes;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.hong2.alpha.R;

/**
 * This is demo code to accompany the Mobiletuts+ tutorial series:
 * - Android SDK: Create a Drawing App
 * - extended for follow-up tutorials on using patterns and opacity
 *
 * Sue Smith
 * August 2013 / September 2013
 *
 */
public class DrawingView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint, mergePaint, tempPaint, effectPaint;
    //initial color
    private int paintColor = 0xFF660000, paintAlpha = 255, blurSize = 0;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap, savedBitmap;
    //brush sizes
    private float brushSize, lastBrushSize;
    //erase flag
    private boolean erase=false;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    //setup drawing
    private void setupDrawing(){

        //prepare for drawing and setup paint stroke properties
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
        drawPath = new Path();
        drawPaint = new Paint();
        tempPaint = new Paint();
        mergePaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);


        tempPaint.setColor(paintColor);
        tempPaint.setAntiAlias(true);
        tempPaint.setStrokeWidth(brushSize);
        tempPaint.setStyle(Paint.Style.STROKE);
        tempPaint.setStrokeJoin(Paint.Join.ROUND);
        tempPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    //size assigned to view
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w > 0 && h > 0) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
        }
    }

    protected void saveBitmap(String tag)
    {

        PorterDuff.Mode destMode = null;
        if(tag.equals("xor"))
        {
            destMode = PorterDuff.Mode.XOR;
        }
        else if(tag.equals("lighten"))
        {
            destMode = PorterDuff.Mode.LIGHTEN;
        }
        else if(tag.equals("overlay"))
        {
            destMode = PorterDuff.Mode.OVERLAY;
        }
        else if(tag.equals("darken"))
        {
            destMode = PorterDuff.Mode.DARKEN;
        }
        else if(tag.equals("multiply")){
            destMode = PorterDuff.Mode.MULTIPLY;
        }
        else if(tag.equals("screen"))
        {
            destMode = PorterDuff.Mode.SCREEN;
        }
        if(destMode != null)
            mergePaint.setXfermode(new PorterDuffXfermode(destMode));
        else {
            mergePaint.setXfermode(null);
        }
        if(savedBitmap != null)
        {
            savedBitmap.recycle();
        }
        savedBitmap = canvasBitmap;
        canvasBitmap = Bitmap.createBitmap(savedBitmap.getWidth(), savedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }

    public void setEffect(String tag)
    {
        effectPaint = new Paint();
        if(tag.equals("negate"))
        {
            ColorMatrix cm = new ColorMatrix(new float[]{
                    -1, 0, 0, 0, 255,
                    0, -1, 0, 0, 255,
                    0, 0, -1, 0, 255,
                    0, 0, 0, 1, 0});
            effectPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        }else if(tag.equals("embose")) {
            EmbossMaskFilter emboss = new EmbossMaskFilter(new float[] {2, 2, 2}, 0.5f, 6, 5);
            effectPaint.setMaskFilter(emboss);
        }else if(tag.equals("grayscale"))
        {
            ColorMatrix cm = new ColorMatrix(new float[]{
                0.299f, 0.587f, 0.114f, 0, 0,
                0.299f, 0.587f, 0.114f, 0, 0,
                0.299f, 0.587f, 0.114f, 0, 0,
                0, 0, 0, 1, 0 });
            effectPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        }
        drawCanvas.drawBitmap(canvasBitmap, 0, 0, effectPaint);
        invalidate();
    }

    protected void mergeBitmap()
    {
        drawCanvas = new Canvas(savedBitmap);
        drawCanvas.drawBitmap(canvasBitmap, 0, 0, mergePaint);
        canvasBitmap.recycle();
        canvasBitmap = savedBitmap;
        savedBitmap = null;
        invalidate();
    }


    //draw the view - will be called after touch event
    @Override
    protected void onDraw(Canvas canvas) {
        if (savedBitmap != null)
        {
            canvas.drawBitmap(savedBitmap, 0, 0, canvasPaint);
        }
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, tempPaint);
    }

    //register user touches as drawing action
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        //redraw
        invalidate();
        return true;

    }

    //update color
    public void setColor(String newColor){
        invalidate();
        //check whether color value or pattern name
        if(newColor.startsWith("#")){
            paintColor = Color.parseColor(newColor);
            drawPaint.setColor(paintColor);
            drawPaint.setShader(null);

            tempPaint.setColor(paintColor);
            tempPaint.setShader(null);
        }
        else{
            //pattern
            int patternID = getResources().getIdentifier(
                    newColor, "drawable", "com.hong2.alpha");
            //decode
            Bitmap patternBMP = BitmapFactory.decodeResource(getResources(), patternID);
            //create shader
            BitmapShader patternBMPshader = new BitmapShader(patternBMP,
                    Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            //color and shader
            drawPaint.setColor(0xFFFFFFFF);
            drawPaint.setShader(patternBMPshader);

            tempPaint.setColor(0xFFFFFFFF);
            tempPaint.setShader(patternBMPshader);
        }
    }

    public void setColor(int newColor)
    {
        paintColor = newColor;
        drawPaint.setColor(newColor);
        drawPaint.setShader(null);

        tempPaint.setColor(newColor);
        tempPaint.setShader(null);
    }

    public void setBlur(int bluri)
    {
        blurSize = bluri;
        if(bluri > 0) {
            BlurMaskFilter blur = new BlurMaskFilter(bluri, BlurMaskFilter.Blur.NORMAL);
            drawPaint.setMaskFilter(blur);
            tempPaint.setMaskFilter(blur);
        }
        else {
            drawPaint.setMaskFilter(null);
            drawPaint.setMaskFilter(null);
        }
    }

    public int getBlur()
    {
        return blurSize;
    }

    public int getColor()
    {
        return paintColor;
    }

    //set brush size
    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);

        tempPaint.setStrokeWidth(brushSize);

    }
    public float getBrushSize()
    {
        float dpAmount = pxToDp(brushSize);
        return dpAmount;
    }
    public static float pxToDp(float px)
    {
        return (px / Resources.getSystem().getDisplayMetrics().density);
    }

    //get and set last brush size
    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }

    //set erase true or false
    public void setErase(boolean isErase){
        erase=isErase;
        if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }

    public void setXfermode(String tag)
    {
        PorterDuff.Mode destMode = null;
        if(tag.equals("xor"))
        {
            destMode = PorterDuff.Mode.XOR;
        }
        else if(tag.equals("lighten"))
        {
            destMode = PorterDuff.Mode.LIGHTEN;
        }
        else if(tag.equals("overlay"))
        {
            destMode = PorterDuff.Mode.OVERLAY;
        }
        else if(tag.equals("darken"))
        {
            destMode = PorterDuff.Mode.DARKEN;
        }
        else if(tag.equals("multiply")){
            destMode = PorterDuff.Mode.MULTIPLY;
        }
        else if(tag.equals("screen"))
        {
            destMode = PorterDuff.Mode.SCREEN;
        }
        if(destMode != null)
            drawPaint.setXfermode(new PorterDuffXfermode(destMode));
        else {
            drawPaint.setXfermode(null);
            ColorMatrix cm = new ColorMatrix(new float[]{
                    0.299f, 0.587f, 0.114f, 0, 0,
                    0.299f, 0.587f, 0.114f, 0, 0,
                    0.299f, 0.587f, 0.114f, 0, 0,
                    0, 0, 0, 1, 0 });
            drawPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        }
    }


    //start new drawing
    public void startNew(){
        savedBitmap = null;
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void importImage(Bitmap bm){
        drawCanvas.drawBitmap(bm, null, new Rect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight()), null);
    }

    //return current alpha
    public int getPaintAlpha(){
        return paintAlpha;
    }

    //set alpha
    public void setPaintAlpha(int newAlpha){
        paintAlpha=newAlpha;
        drawPaint.setColor(paintColor);
        drawPaint.setAlpha(paintAlpha);
        tempPaint.setColor(paintColor);
        tempPaint.setAlpha(paintAlpha);

    }
}

