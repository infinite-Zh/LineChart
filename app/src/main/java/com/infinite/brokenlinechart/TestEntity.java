package com.infinite.brokenlinechart;

/**
 * Created by inf on 2016/11/30.
 */

public class TestEntity implements ILineElement{

    private float mXValue,mYValue;
    public TestEntity(float mXValue,float mYValue){
        this.mXValue=mXValue;
        this.mYValue=mYValue;
    }
    @Override
    public float getXValue() {
        return mXValue;
    }

    @Override
    public float getYValue() {
        return mYValue;
    }
}
