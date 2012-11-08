package com.todoroo.astrid.activity;

import android.content.Context;

import com.timsu.astrid.R;
import com.todoroo.andlib.utility.Preferences;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.core.CoreFilterExposer;
import com.todoroo.astrid.ui.MainMenuPopover;

public class DefaultFilterMode implements FilterModeSpec {

    @Override
    public int[] getForbiddenMenuItems() {
        if (Preferences.getBoolean(R.string.p_show_featured_lists, false))
            return new int[0];
        else
            return new int[] { MainMenuPopover.MAIN_MENU_ITEM_FEATURED_LISTS };
    }

    @Override
    public Class<? extends FilterListFragment> getFilterListClass() {
        return FilterListFragment.class;
    }

    @Override
    public Filter getDefaultFilter(Context context) {
        return CoreFilterExposer.buildInboxFilter(context.getResources());
    }

    @Override
    public int getMainMenuIconAttr() {
        return R.attr.asMainMenu;
    }

    @Override
    public boolean showComments() {
        return true;
    }

}
