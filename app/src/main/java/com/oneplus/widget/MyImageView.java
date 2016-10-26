package com.oneplus.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Oneplus on 2016/10/22.
 */
public class MyImageView extends ImageView{

    WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    int width = wm.getDefaultDisplay().getWidth();
    int height = wm.getDefaultDisplay().getWidth();
    int centerX = width/2;
    int centerY = height/2;

    public MyImageView(Context context) {
        super(context);
    }
    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (Math.sqrt(Math.pow(((int) event.getX() - centerX), 2)
                    + Math.pow(((int) event.getY() - centerY), 2)) >= width/2) {
                return true;
            } else {
                return super.onTouchEvent(event);
            }
        }
        return true;
    }

}
