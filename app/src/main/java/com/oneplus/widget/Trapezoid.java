package com.oneplus.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.oneplus.myradar.R;

/**
 * Created by Oneplus on 2016/10/13.
 */
public class Trapezoid extends View{

    private Paint paint = new Paint();

    public Trapezoid(Context context) {
        super(context);
    }

    public Trapezoid(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Trapezoid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(getResources().getColor(R.color.metalGray));
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        Path path = new Path();
        path.reset();
        path.moveTo(0, 0);
        path.lineTo(getWidth(), 0);
        path.lineTo(getWidth()*205/220, getHeight());
        path.lineTo(getWidth()*15/220, getHeight());

        canvas.drawPath(path, paint);

        paint.setColor(getResources().getColor(R.color.metalLineGray1));
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawLine(getWidth(),0,getWidth()*205/220,getHeight(),paint);

        paint.setColor(getResources().getColor(R.color.metalLineGray2));

        canvas.drawLine(getWidth()*205/220,getHeight(),getWidth()*15/220,getHeight(),paint);
        canvas.drawLine(getWidth()*15/220,getHeight(),0,0,paint);

    }

}
