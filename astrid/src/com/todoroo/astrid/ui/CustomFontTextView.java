package com.todoroo.astrid.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.timsu.astrid.R;
import com.todoroo.astrid.helper.TypefaceCache;

public class CustomFontTextView extends TextView {

    private Typeface regularTypeface = null;
    private Typeface boldTypeface = null;
    private static final String DEFAULT_FONT = "JosefinSans"; //$NON-NLS-1$

    public CustomFontTextView(Context context) {
        super(context);
        setupDefaultFont();
        setStyledTypeface();
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int index = ta.getIndex(i);
            if (index == R.styleable.CustomFontTextView_customTextFont) {
                String customFont = ta.getString(index);
                setupTypefaces(customFont);
                break;
            }
        }

        if (regularTypeface == null) {
            setupDefaultFont();
        }

        setStyledTypeface();

        ta.recycle();
    }

    private void setupTypefaces(String name) {
        boldTypeface = TypefaceCache.getTypeface(name + "-Bold.ttf"); //$NON-NLS-1$
        regularTypeface = TypefaceCache.getTypeface(name + "-Regular.ttf"); //$NON-NLS-1$
    }

    private void setStyledTypeface() {
        if (getTypeface() != null && (getTypeface().getStyle() & Typeface.BOLD) > 0 && boldTypeface != null)
            setTypeface(boldTypeface);
        else if (regularTypeface != null)
            setTypeface(regularTypeface);
    }

    private void setupDefaultFont() {
       setupTypefaces(DEFAULT_FONT);
    }

    @Override
    public void setTextAppearance(Context context, int resid) {
        super.setTextAppearance(context, resid);
        setStyledTypeface();
    }

}
