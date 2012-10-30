package com.todoroo.astrid.tags.reusable;

import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.timsu.astrid.R;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.tags.reusable.CloneableTaskAdapter.ReusableTaskViewHolder;



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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) menuInfo;
        Task task = ((ReusableTaskViewHolder) adapterInfo.targetView.getTag()).task;
        if (task.getFlag(Task.FLAGS, Task.FLAG_IS_READONLY))
            return;

        int id = (int) task.getId();
        menu.setHeaderTitle(task.getValue(Task.TITLE));

        if (task.isDeleted()) {
            menu.add(id, CONTEXT_MENU_UNDELETE_TASK_ID, Menu.NONE,
                    R.string.TAd_contextUndeleteTask);

            menu.add(id, CONTEXT_MENU_PURGE_TASK_ID, Menu.NONE,
                    R.string.TAd_contextPurgeTask);
        } else {
            menu.add(id, CONTEXT_MENU_EDIT_TASK_ID, Menu.NONE,
                    R.string.TAd_contextEditTask);

            menu.add(id, CONTEXT_MENU_DELETE_TASK_ID, Menu.NONE,
                    R.string.TAd_contextDeleteTask);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == CONTEXT_MENU_EDIT_TASK_ID) {
            onTaskListItemClicked(item.getGroupId(), true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
