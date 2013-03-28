package com.todoroo.astrid.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.timsu.astrid.R;
import com.todoroo.astrid.helper.TypefaceCache;

public class CustomFontEditText extends EditText {

    private Typeface defaultTypeface = null;
    private static final String DEFAULT_FONT = "HandmadeTypewriter.ttf"; //$NON-NLS-1$

    public CustomFontEditText(Context context) {
        super(context);
    }

    public CustomFontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomFontEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
        boolean setCustomFont = false;
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int index = ta.getIndex(i);
            if (index == R.styleable.CustomFontEditText_customEditFont) {
                String customFont = ta.getString(index);
                if (customFont != null) {
                    Typeface tf = TypefaceCache.getTypeface(customFont);
                    if (tf != null) {
                        setCustomFont = true;
                        defaultTypeface = tf;
                        setTypeface(tf);
                    }
                }
                break;
            }
        }

        if (!setCustomFont) {
            Typeface defaultTf = TypefaceCache.getTypeface(DEFAULT_FONT);
            if (defaultTf != null) {
                defaultTypeface = defaultTf;
                setTypeface(defaultTf);
            }
        }

        ta.recycle();
    }

    @Override
    public void setTextAppearance(Context context, int resid) {
        super.setTextAppearance(context, resid);
        if (defaultTypeface != null)
            setTypeface(defaultTypeface);
    }

}
