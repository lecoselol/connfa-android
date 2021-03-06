package com.ls.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class BoldFontTextView extends TextView {
    private static Typeface font;

    public BoldFontTextView(Context context) {
        super(context);
        setTypeface(getFont(context));
    }

    public BoldFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(getFont(context));
    }

    public BoldFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(getFont(context));
    }

    public static Typeface getFont(Context context) {
        if (font == null)
            font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");

        return font;
    }
}
