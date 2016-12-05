package com.infinite.brokenlinechart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private BrokenLineChart chart;
    private List<ILineElement> mEle=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chart= (BrokenLineChart) findViewById(R.id.lineChart);
        for(int i=0;i<20;i++){

            float x= i;
            Random random=new Random();

           float y= (float) (int)(Math.random()*100);
            TestEntity entity=new TestEntity(x,y);
            mEle.add(entity);
            Log.e("collection",y+"");
        }

        chart.setData(mEle);
    }
}
