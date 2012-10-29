package com.todoroo.astrid.tags.reusable;

import android.app.Activity;

import com.timsu.astrid.R;
import com.todoroo.astrid.activity.FilterListFragment;
import com.todoroo.astrid.adapter.FilterAdapter;
import com.todoroo.astrid.utility.AstridPreferences;

public class ReusableListFragment extends FilterListFragment {

    @Override
    protected FilterAdapter instantiateAdapter() {
        return new ReusableListFilterAdapter(getActivity(), null, R.layout.filter_adapter_row, false);
    }

    @Override
    protected int getLayout(Activity activity) {
        if (AstridPreferences.useTabletLayout(activity))
            return R.layout.filter_list_fragment_alternative_3pane;
        else
            return R.layout.filter_list_fragment_alternative;
    }

}
