package com.todoroo.astrid.activity;

import android.content.Context;

import com.todoroo.astrid.api.Filter;


public interface FilterModeSpec {

    public int[] getForbiddenMenuItems();
    public Class<? extends FilterListFragment> getFilterListClass();
    public Filter getDefaultFilter(Context context);
    public int getMainMenuIconAttr();
    public boolean showComments();

}
