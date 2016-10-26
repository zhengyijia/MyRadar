package com.oneplus.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Oneplus on 2016/10/13.
 */
public class MyTextView4 extends TextView{

    public MyTextView4(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()) {
            init();
        }
    }

    public MyTextView4(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {
            init();
        }
    }

    public MyTextView4(Context context) {
        super(context);
        if(!isInEditMode()) {
            init();
        }
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),"04B_03B_.TTF");
        setTypeface(tf);
    }

}
