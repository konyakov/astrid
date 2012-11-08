package com.todoroo.astrid.tags.reusable;

import android.content.Context;

import com.timsu.astrid.R;
import com.todoroo.astrid.activity.FilterListFragment;
import com.todoroo.astrid.activity.FilterModeSpec;
import com.todoroo.astrid.activity.TaskListFragment;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.core.CoreFilterExposer;
import com.todoroo.astrid.ui.MainMenuPopover;

public class ReusableListFilterMode implements FilterModeSpec {

    @Override
    public int[] getForbiddenMenuItems() {
        return FORBIDDEN_MENU_ITEMS;
    }

    private static final int[] FORBIDDEN_MENU_ITEMS = {
        TaskListFragment.MENU_NEW_FILTER_ID,
        TaskListFragment.MENU_ADDONS_ID,
        MainMenuPopover.MAIN_MENU_ITEM_FEATURED_LISTS
    };

    @Override
    public Class<? extends FilterListFragment> getFilterListClass() {
        return ReusableListFragment.class;
    }

    @Override
    public Filter getDefaultFilter(Context context) {
        Filter defaultFilter = ReusableListFilterExposer.getDefaultFilter();
        if (defaultFilter == null)
            return CoreFilterExposer.buildInboxFilter(context.getResources());
        else
            return defaultFilter;
    }

    @Override
    public int getMainMenuIconAttr() {
        return R.attr.asMainMenu;
    }

    @Override
    public boolean showComments() {
        return false;
    }

}
