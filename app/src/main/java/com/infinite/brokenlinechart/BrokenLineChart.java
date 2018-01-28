package com.infinite.brokenlinechart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
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
import android.view.animation.AccelerateDecelerateInterpolator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by inf on 2016/11/30.
 */

public class BrokenLineChart extends View {

    private Paint mXAxisPaint, mYAxisPaint, mLinePaint, mTextPaint;
    private static final int AXIS_WIDTH = 3;
    private static final int DIAL_HEIGHT = 8;
    /**
     * Y轴刻度数量
     */
    private static final int Y_DIAL_NUM = 10;

    private int mWidth, mHeight;

    private List<ILineElement> mElements;
    /**
     * x轴值
     */
    private List<Float> mXValues = new ArrayList<>();
    /**
     * y轴值
     */
    private List<Float> mYValues = new ArrayList<>();
    /**
     * x轴坐标
     */
    private List<Float> mXPoint = new ArrayList<>();
    /**
     * y轴坐标
     */
    private List<Float> mYPoint = new ArrayList<>();


    public BrokenLineChart(Context context) {
        this(context, null);
    }

    public BrokenLineChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BrokenLineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mXAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXAxisPaint.setStrokeWidth(AXIS_WIDTH);
        mXAxisPaint.setColor(Color.BLACK);
        mXAxisPaint.setStyle(Paint.Style.STROKE);
        mYAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mYAxisPaint.setStrokeWidth(AXIS_WIDTH);
        mYAxisPaint.setColor(Color.BLACK);
        mYAxisPaint.setStyle(Paint.Style.STROKE);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(3);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(30);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = getDefaultSize(widthSize, widthMeasureSpec);

        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = (int) Math.min(widthSize / 3 * 2, getDefaultSize(heightSize, heightMeasureSpec));
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w - getPaddingRight() - getPaddingLeft();
        mHeight = h - getPaddingTop() - getPaddingBottom();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                drawLine();
            }
        }, 500);
    }

    private Path mPath = new Path();
    private Path mLinePath = new Path();

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        drawAxis(canvas);
        drawXDial(canvas);
        drawValue(canvas);
        calculateYPoints();
        drawLine(canvas);
    }

    public void setData(List<ILineElement> elements) {
        mElements = elements;
        float maxY = 0;
        if (mElements != null && mElements.size() > 0) {
            for (ILineElement element : mElements) {
                if (element.getYValue() > maxY) {
                    maxY = element.getYValue();
                }
                mXValues.add(element.getXValue());
                mYValues.add(element.getYValue());
            }
        }
        invalidate();
    }

    /**
     * 画坐标轴
     *
     * @param canvas
     */
    private void drawAxis(Canvas canvas) {
        canvas.translate(getPaddingLeft(), getPaddingBottom());
        mPath.reset();
        mPath.moveTo(0, mHeight);
        mPath.lineTo(0, 0);
        mPath.moveTo(0, mHeight);
        mPath.lineTo(mWidth, mHeight);
        mPath.close();
        canvas.drawPath(mPath, mXAxisPaint);
    }

    /**
     * 画x轴刻度
     *
     * @param canvas
     */
    private void drawXDial(Canvas canvas) {
        mXPoint.clear();
        if (mElements != null && mElements.size() > 0) {
            int xIndex = mElements.size();
            int xRatio = (int) Math.ceil(mWidth / xIndex);
            for (int i = 0; i < mElements.size(); i++) {
                float xPoint = i * xRatio;
                if (xPoint - mLeftOffset < 0) {
                    mLeftOffset = 0;
                }
                mPath.reset();
                mPath.moveTo(xPoint - mLeftOffset, mHeight);
                mPath.lineTo(xPoint - mLeftOffset, mHeight - DIAL_HEIGHT);
                mPath.close();
                canvas.drawPath(mPath, mXAxisPaint);
                String text = String.valueOf(mXValues.get(i).intValue());
                mTextPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(text, xPoint - mTextPaint.measureText(text) / 2 - mLeftOffset,
                        (float) (mHeight + getTextHeight(text, mTextPaint) * 1.2), mTextPaint);
                mXPoint.add(xPoint);
            }

        }
    }

    /**
     * 数量级
     */
    private int magnitude = 1;

    /**
     * 最大数对数量级取模
     */
    private int m;

    private float mActualHeight, mActualWidth;

    private void drawValue(Canvas canvas) {
        float maxValue = getMaxValue();
        getMagnitude(1, maxValue);
        m = (int) (maxValue / magnitude);
        Log.e("num", "max=" + maxValue + "    magnitude=" + magnitude + "   m=" + m);
        //y轴刻度间数量差，最大数对数量级取模，再加一个数量级的然后除以y轴刻度的个数，例如最大数是180，数量级是100，公式就是（1*100+100）/10=20
        float yAxis = mHeight / Y_DIAL_NUM;
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mPath.reset();

        for (int i = 0; i <= Y_DIAL_NUM; i++) {
            mPath.moveTo(0, mHeight - yAxis * i);
            mPath.lineTo(DIAL_HEIGHT, mHeight - yAxis * i);
            mPath.close();
            canvas.drawPath(mPath, mYAxisPaint);
            String text = String.valueOf(i);
            if (i == 0) {
                continue;
            }
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(text,
                    -(float) 5, mHeight - yAxis * i + getTextHeight(text, mTextPaint) / 2, mTextPaint);
        }
        mActualHeight = yAxis * Y_DIAL_NUM;
        String label = "单位:" + magnitude;
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(label, getTextWidth(label, mTextPaint) + 10, getTextHeight(label, mTextPaint), mTextPaint);
    }

    private static final String PROPERTY_X = "PROPERTYX";
    private static final String PROPERTY_Y = "PROPERTYY";
    private float mEndX = 0, mEndY = 0;

    private void drawLine(Canvas canvas) {
        mLinePath.lineTo(mEndX, mEndY);
        canvas.drawPath(mLinePath, mLinePaint);
    }

    private void drawLine() {
//        mPath.reset();
//        for(int i=0;i<mElements.size();i++){
//            float x=mXPoint.get(i);
//            float y=mYPoint.get(i);
//            if (x-mLeftOffset<0){
//                mLeftOffset=0;
//            }
//
//            if (i==0){
//                mPath.moveTo(x-mLeftOffset,y);
//            }else {
//                mPath.lineTo(x-mLeftOffset,y);
//            }
//            canvas.drawCircle(x-mLeftOffset,y,5,mTextPaint);
//
//        }
//        canvas.drawPath(mPath,mLinePaint);
//        mPath.close();

        mPath.reset();
        AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animators = new ArrayList<>();
        for (int i = 0; i < mElements.size(); i++) {
            float startX = mXPoint.get(i);
            float startY = mYPoint.get(i);
            float endX = 0, endY = 0;
            final int mCurrentPos=i;
            if (i==0){
                mLinePath.moveTo(startX,startY);
            }
            if (i < mElements.size() - 1) {
                endX = mXPoint.get(i + 1);
                endY = mYPoint.get(i + 1);
            }else {
                continue;
            }
            PropertyValuesHolder propertyX = PropertyValuesHolder.ofFloat(PROPERTY_X, startX, endX);
            PropertyValuesHolder propertyY = PropertyValuesHolder.ofFloat(PROPERTY_Y, startY, endY);

            ValueAnimator anim = new ValueAnimator();
            anim.setValues(propertyX, propertyY);
            anim.setDuration(1500 / mElements.size());

            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mEndX = (float) animation.getAnimatedValue(PROPERTY_X);
                    mEndY = (float) animation.getAnimatedValue(PROPERTY_Y);
                    invalidate();
                }

            });
            animators.add(anim);
        }
        animatorSet.playSequentially(animators);
        animatorSet.start();

    }

    private void calculateYPoints() {
        mYPoint.clear();
        BigDecimal height = new BigDecimal(mActualHeight);
        BigDecimal ratio = height.divide(new BigDecimal(magnitude * (m + 1)), 8, RoundingMode.DOWN);
        for (int i = 0; mElements != null && i < mElements.size(); i++) {
            ILineElement ele = mElements.get(i);
            mYPoint.add(mActualHeight - ratio.multiply(new BigDecimal(ele.getYValue())).floatValue());
        }
    }

    private void calculateXPoints() {
        mXPoint.clear();
        BigDecimal height = new BigDecimal(mActualWidth);
        BigDecimal ratio = height.divide(new BigDecimal(magnitude * (m + 1)), 8, RoundingMode.DOWN);
        for (int i = 0; mElements != null && i < mElements.size(); i++) {
            ILineElement ele = mElements.get(i);
            mXPoint.add(mActualWidth - ratio.multiply(new BigDecimal(ele.getXValue())).floatValue());
        }
    }
    private void getMagnitude(int base, float value) {
        if (value / base >= 10) {
            base *= 10;
            magnitude = base;
            getMagnitude(base, value);
        }
    }

    private float getMaxValue() {
        if (mElements != null && mElements.size() > 0) {
            float maxValue = 0;
            for (ILineElement ele : mElements) {
                float value = ele.getYValue();
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            return maxValue;
        }
        return 0;
    }

    private float getScreenWidth() {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        float widht = display.getWidth();
        float height = display.getHeight();
        return widht;
    }

    private float getScreenHeight() {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        float widht = display.getWidth();
        float height = display.getHeight();
        return height;
    }



    private float getTextHeight(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    private float getTextWidth(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.width();
    }


    private int mLastX;
    /**
     * 画x轴和画线时，左侧的偏移量，即手指滑动的总距离
     */
    private float mLeftOffset;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
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
