package com.ls.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class MediumFontTextView extends TextView {

    public MediumFontTextView(Context context) {
        super(context);
        setTypeface(getFont(context));
    }

    public MediumFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(getFont(context));
    }

    public MediumFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(getFont(context));
    }

    private Typeface getFont(Context context) {
        return FontHelper.getInstance(context).getFontRobotoMedium();
    }
}
