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
public class Trapezoid2 extends View{

    private Paint paint = new Paint();

    public Trapezoid2(Context context) {
        super(context);
    }

    public Trapezoid2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Trapezoid2(Context context, AttributeSet attrs, int defStyle) {
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
        path.moveTo(getWidth()*15/220, 0);
        path.lineTo(getWidth()*205/220, 0);
        path.lineTo(getWidth(), getHeight());
        path.lineTo(0, getHeight());

        canvas.drawPath(path, paint);

        paint.setColor(getResources().getColor(R.color.metalLineGray1));
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawLine(0,getHeight(),getWidth()*15/220,0,paint);

        paint.setColor(getResources().getColor(R.color.metalLineGray3));

        canvas.drawLine(getWidth()*15/220,0,getWidth()*205/220,0,paint);
        canvas.drawLine(getWidth()*205/220,0,getWidth(),getHeight(),paint);

    }

}
