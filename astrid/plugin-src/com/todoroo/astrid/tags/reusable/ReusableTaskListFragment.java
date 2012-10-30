package com.todoroo.astrid.tags.reusable;

import android.view.View;
import android.view.ViewGroup;

import com.timsu.astrid.R;



public class ReusableTaskListFragment extends CloneableTagViewFragment {

    @Override
    protected View getListBody(ViewGroup root) {
        taskListView = getActivity().getLayoutInflater().inflate(
                R.layout.task_list_body_standard, root, false);
        return taskListView;
    }

    @Override
    protected void setupQuickAddBar() {
        super.setupQuickAddBar();
        quickAddBar.setShouldUseControlSets(false);
    }

    @Override
    protected void setupHeaderView() {
        // Do nothing--there isn't one yet!
    }

    @Override
    public void onTaskListItemClicked(long taskId, boolean editable) {
        mListener.onTaskListItemClicked(taskId, editable, true);
    }

}
