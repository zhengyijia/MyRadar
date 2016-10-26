package com.oneplus.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Oneplus on 2016/10/12.
 */
public class MyTextView3 extends TextView {
    public MyTextView3(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()) {
            init();
        }
    }

    public MyTextView3(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {
            init();
        }
    }

    public MyTextView3(Context context) {
        super(context);
        if(!isInEditMode()) {
            init();
        }
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),"04b24.ttf");
        setTypeface(tf);
    }
}

