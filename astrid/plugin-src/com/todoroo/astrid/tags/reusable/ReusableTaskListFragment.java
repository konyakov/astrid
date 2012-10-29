package com.todoroo.astrid.tags.reusable;


public class ReusableTaskListFragment extends CloneableTagViewFragment {

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
        // Better do something crazy here--template tasks need special editing
    }

}
