package com.infinite.brokenlinechart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by inf on 2016/11/30.
 */

public class BrokenLineChart extends View{

    private Paint mXAxisPaint,mYAxisPaint,mLinePaint,mTextPaint;
    private static final int AXIS_WIDTH=3;
    private static final int DIAL_HEIGHT=8;
    /**
     * Y轴刻度数量
     */
    private static final int Y_DIAL_NUM=10;

    private int mWidth,mHeight;

    private List<ILineElement> mElements;
    /**
     * x轴值
     */
    private List<Float> mXValues=new ArrayList<>();
    /**
     * y轴值
     */
    private List<Float> mYValues=new ArrayList<>();
    /**
     * x轴坐标
     */
    private List<Float> mXPoint=new ArrayList<>();
    /**
     * y轴坐标
     */
    private List<Float> mYPoint=new ArrayList<>();

    public BrokenLineChart(Context context) {
        this(context,null);
    }

    public BrokenLineChart(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BrokenLineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mXAxisPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mXAxisPaint.setStrokeWidth(AXIS_WIDTH);
        mXAxisPaint.setColor(Color.BLACK);
        mXAxisPaint.setStyle(Paint.Style.STROKE);
        mYAxisPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mYAxisPaint.setStrokeWidth(AXIS_WIDTH);
        mYAxisPaint.setColor(Color.BLACK);
        mYAxisPaint.setStyle(Paint.Style.STROKE);
        mLinePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(3);

        mTextPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(30);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode==MeasureSpec.AT_MOST){
            widthSize= getDefaultSize(widthSize,widthMeasureSpec);

        }
        if (heightMode==MeasureSpec.AT_MOST){
            heightSize= (int) Math.min(widthSize/3*2, getDefaultSize(heightSize,heightMeasureSpec));
        }

        setMeasuredDimension(widthSize,heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth=w-getPaddingRight()-getPaddingLeft();
        mHeight=h-getPaddingTop()-getPaddingBottom();
    }

    private Path mPath=new Path();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAxis(canvas);
        drawXDial(canvas);
        drawValue(canvas);
        drawLine(canvas);
    }

    /**
     * 画坐标轴
     * @param canvas
     */
    private void drawAxis(Canvas canvas){
        canvas.translate(getPaddingLeft(),getPaddingBottom());
        mPath.reset();
        mPath.moveTo(0,mHeight);
        mPath.lineTo(0,0);
        mPath.moveTo(0,mHeight);
        mPath.lineTo(mWidth,mHeight);
        mPath.close();
        canvas.drawPath(mPath,mXAxisPaint);
    }

    /**
     * 画x轴刻度
     * @param canvas
     */
    private void drawXDial(Canvas canvas){
        if (mElements!=null&&mElements.size()>0){
            int xIndex=mElements.size();
            int xRatio= (int) Math.ceil(mWidth/xIndex);
            for(int i=0;i<mElements.size();i++){
                float xPoint=i*xRatio;
                if (xPoint-mLeftOffset<0){
                    mLeftOffset=0;
                }
                mPath.reset();
                mPath.moveTo(xPoint-mLeftOffset,mHeight);
                mPath.lineTo(xPoint-mLeftOffset,mHeight-DIAL_HEIGHT);
                mPath.close();
                canvas.drawPath(mPath,mXAxisPaint);
                String text=String.valueOf(mXValues.get(i).intValue());
                mTextPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(text, xPoint-mTextPaint.measureText(text)/2-mLeftOffset,
                                (float) (mHeight+getTextHeight(text, mTextPaint)*1.2), mTextPaint);
                mXPoint.add(xPoint);
            }

        }
    }

    /**
     * 数量级
     */
    private int magnitude=1;

    /**
     * 最大数对数量级取模
     */
    private int m;

    private float mActualHeight,mActualWidth;
    private void drawValue(Canvas canvas){
        float maxValue=getMaxValue();
        getMagnitude(1,maxValue);
        m= (int) (maxValue/magnitude);
        Log.e("num","max="+maxValue+"    magnitude="+magnitude+"   m="+m);
        //y轴刻度间数量差，最大数对数量级取模，再加一个数量级的然后除以y轴刻度的个数，例如最大数是180，数量级是100，公式就是（1*100+100）/10=20
        float yAxis=mHeight/Y_DIAL_NUM;
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mPath.reset();

        for(int i=0;i<=Y_DIAL_NUM;i++){
            mPath.moveTo(0,mHeight-yAxis*i);
            mPath.lineTo(DIAL_HEIGHT,mHeight-yAxis*i);
            mPath.close();
            canvas.drawPath(mPath,mYAxisPaint);
            String text=String.valueOf(i);
            if (i==0){
                continue;
            }
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(text,
                            -(float)5, mHeight-yAxis*i+getTextHeight(text, mTextPaint)/2, mTextPaint);
        }
        mActualHeight=yAxis*Y_DIAL_NUM;
        String label="单位:"+magnitude;
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(label,getTextWidth(label,mTextPaint)+10,getTextHeight(label,mTextPaint),mTextPaint);
    }

    private void drawLine(Canvas canvas) {
        calculateYPoints();
        mPath.reset();
        for(int i=0;i<mElements.size();i++){
            float x=mXPoint.get(i);
            float y=mYPoint.get(i);
            if (x-mLeftOffset<0){
                mLeftOffset=0;
            }

            if (i==0){
                mPath.moveTo(x-mLeftOffset,y);
            }else {
                mPath.lineTo(x-mLeftOffset,y);
            }
            canvas.drawCircle(x-mLeftOffset,y,5,mTextPaint);

        }
        canvas.drawPath(mPath,mLinePaint);
        mPath.close();

    }

    private void calculateYPoints(){
        BigDecimal height=new BigDecimal(mActualHeight);
        BigDecimal ratio=height.divide(new BigDecimal(magnitude*(m+1)), 8, RoundingMode.DOWN);
        for(int i=0;mElements!=null&&i<mElements.size();i++){
            ILineElement ele=mElements.get(i);
            mYPoint.add(mActualHeight-ratio.multiply(new BigDecimal(ele.getYValue())).floatValue());
        }
    }

    private void getMagnitude(int base,float value){
        Log.e("mmm",value/base+"");
        if (value/base>=10){
            base*=10;
            magnitude=base;
            getMagnitude(base,value);
        }
    }

    private float getMaxValue(){
        if (mElements!=null&&mElements.size()>0){
            float maxValue=0;
            for(ILineElement ele:mElements){
                float value=ele.getYValue();
                if (value>maxValue){
                    maxValue=value;
                }
            }
            return maxValue;
        }
        return 0;
    }

    private float getScreenWidth(){
        Display display= ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        float widht=display.getWidth();
        float height=display.getHeight();
        return widht;
    }

    private float getScreenHeight(){
        Display display= ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        float widht=display.getWidth();
        float height=display.getHeight();
        return height;
    }

    public void setData(List<ILineElement> elements){
        mElements=elements;
        float maxY=0;
        if (mElements!=null&&mElements.size()>0){
            for(ILineElement element:mElements){
                if (element.getYValue()>maxY){
                    maxY=element.getYValue();
                }
                mXValues.add(element.getXValue());
                mYValues.add(element.getYValue());
            }
        }
        invalidate();
    }

    private float getTextHeight(String text ,Paint paint){
        Rect rect=new Rect();
        paint.getTextBounds(text,0,text.length(),rect);
        return rect.height();
    }

    private float getTextWidth(String text ,Paint paint){
        Rect rect=new Rect();
        paint.getTextBounds(text,0,text.length(),rect);
        return rect.width();
    }


    private int mLastX;
    /**
     * 画x轴和画线时，左侧的偏移量，即手指滑动的总距离
     */
    private float mLeftOffset;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x= (int) event.getX();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastX= (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
//                mLeftOffset+=-(x-mLastX);
//                invalidate();
//                mLastX=x;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }
}
