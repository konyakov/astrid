package com.todoroo.astrid.helper;

import java.util.HashMap;

import android.graphics.Typeface;

import com.todoroo.andlib.service.ContextManager;

public class TypefaceCache {

    private static final HashMap<String, Typeface> TYPEFACES = new HashMap<String, Typeface>();

    public static Typeface getTypeface(String name) {
        try {
            if (TYPEFACES.containsKey(name))
                return TYPEFACES.get(name);

            Typeface tv = Typeface.createFromAsset(ContextManager.getContext().getAssets(), "fonts/" + name); //$NON-NLS-1$
            if (tv != null)
                TYPEFACES.put(name, tv);
            return tv;
        } catch (Exception e) {
            return null;
        }
    }

}
