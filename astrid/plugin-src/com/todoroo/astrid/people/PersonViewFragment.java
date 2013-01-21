/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.people;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.view.Menu;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timsu.astrid.R;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.utility.DateUtilities;
import com.todoroo.andlib.utility.Preferences;
import com.todoroo.astrid.actfm.sync.ActFmPreferenceService;
import com.todoroo.astrid.actfm.sync.ActFmSyncService;
import com.todoroo.astrid.actfm.sync.ActFmSyncThread;
import com.todoroo.astrid.actfm.sync.messages.BriefMe;
import com.todoroo.astrid.activity.TaskListFragment;
import com.todoroo.astrid.dao.UserDao;
import com.todoroo.astrid.data.User;
import com.todoroo.astrid.helper.AsyncImageView;
import com.todoroo.astrid.service.SyncV2Service;
import com.todoroo.astrid.service.ThemeService;
import com.todoroo.astrid.utility.ResourceDrawableCache;

public class PersonViewFragment extends TaskListFragment {

    public static final String EXTRA_USER_ID_LOCAL = "user_local_id"; //$NON-NLS-1$

    public static final String EXTRA_HIDE_QUICK_ADD = "hide_quickAdd"; //$NON-NLS-1$

    private static final String LAST_FETCH_KEY = "actfm_last_user_"; //$NON-NLS-1$

    protected static final int MENU_REFRESH_ID = MENU_SUPPORT_ID + 1;

    @Autowired UserDao userDao;

    @Autowired SyncV2Service syncService;

    @Autowired ActFmPreferenceService actFmPreferenceService;

    @Autowired ActFmSyncService actFmSyncService;

    private AsyncImageView userImage;
    private TextView userSubtitle;
    private TextView userStatusButton;
    private TextView emptyView;

    private User user;

    @Override
    protected void initializeData() {
        super.initializeData();
        if (extras.containsKey(EXTRA_USER_ID_LOCAL)) {
            user = userDao.fetch(extras.getLong(EXTRA_USER_ID_LOCAL), User.PROPERTIES);
        }
        emptyView = ((TextView) getView().findViewById(android.R.id.empty));
        emptyView.setText(getEmptyDisplayString());

        setupUserHeader();
    }

    private void setupUserHeader() {
        if (user != null) {
            userImage.setDefaultImageDrawable(ResourceDrawableCache.getImageDrawableFromId(resources, R.drawable.icn_default_person_image));
            userImage.setUrl(user.getValue(User.PICTURE));
            userSubtitle.setText(getUserSubtitleText());
            setupUserStatusButton();
        } else {
            getView().findViewById(R.id.user_header).setVisibility(View.GONE);
            userStatusButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setupQuickAddBar() {
        super.setupQuickAddBar();
        quickAddBar.setUsePeopleControl(false);
        if (user != null)
            quickAddBar.getQuickAddBox().setHint(getString(R.string.TLA_quick_add_hint_assign, user.getDisplayName()));

        if (extras.containsKey(EXTRA_HIDE_QUICK_ADD))
            quickAddBar.setVisibility(View.GONE);

        // set listener for astrid icon
        emptyView.setOnClickListener(null);

    }

    private String getUserSubtitleText() {
        String status = user.getValue(User.STATUS);
        String userName = user.getDisplayName();
        if (User.STATUS_PENDING.equals(status))
            return getString(R.string.actfm_friendship_pending, userName);
        else if (User.STATUS_BLOCKED.equals(status))
            return getString(R.string.actfm_friendship_blocked, userName);
        else if (User.STATUS_FRIENDS.equals(status))
            return getString(R.string.actfm_friendship_friends, userName);
        else if (User.STATUS_OTHER_PENDING.equals(status))
            return getString(R.string.actfm_friendship_other_pending, userName);
        else return getString(R.string.actfm_friendship_no_status, userName);

    }

    private void setupUserStatusButton() {
        String status = user.getValue(User.STATUS);
        String pendingStatus = user.getValue(User.PENDING_STATUS);
        userStatusButton.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(pendingStatus))
            userStatusButton.setVisibility(View.GONE);
        else if (TextUtils.isEmpty(status) || "null".equals(status)) //$NON-NLS-1$
            userStatusButton.setText(getString(R.string.actfm_friendship_connect));
        else if (User.STATUS_OTHER_PENDING.equals(status))
            userStatusButton.setText(getString(R.string.actfm_friendship_accept));
        else
            userStatusButton.setVisibility(View.GONE);
    }

    @Override
    protected void setUpUiComponents() {
        super.setUpUiComponents();
        userImage = (AsyncImageView) getView().findViewById(R.id.user_image);
        userSubtitle = (TextView) getView().findViewById(R.id.user_subtitle);
        userStatusButton = (TextView) getActivity().findViewById(R.id.person_image);
    }

    @Override
    protected View getListBody(ViewGroup root) {
        ViewGroup parent = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.task_list_body_user, root, false);

        View taskListView = super.getListBody(parent);
        parent.addView(taskListView);

        return parent;
    }

    public void handleStatusButtonClicked() {
        if (user != null) { // Just in case
            String status = user.getValue(User.STATUS);
            if (TextUtils.isEmpty(status) || "null".equals(status)) { // Add friend case //$NON-NLS-1$
                user.setValue(User.PENDING_STATUS, User.PENDING_REQUEST);
            } else if (User.STATUS_OTHER_PENDING.equals(status)) { // Accept friend case
                user.setValue(User.PENDING_STATUS, User.PENDING_APPROVE);
            }

            ContentValues setValues = user.getSetValues();
            if (setValues != null && setValues.containsKey(User.PENDING_STATUS.name)) {
                userDao.saveExisting(user);
                userStatusButton.setVisibility(View.GONE);
                refreshData(false);
            }
        }
    }

    @Override
    protected void addSyncRefreshMenuItem(Menu menu, int themeFlags) {
        if(actFmPreferenceService.isLoggedIn()) {
            addMenuItem(menu, R.string.actfm_TVA_menu_refresh,
                    ThemeService.getDrawable(R.drawable.icn_menu_refresh, themeFlags), MENU_REFRESH_ID, true);
        } else {
            super.addSyncRefreshMenuItem(menu, themeFlags);
        }
    }

    @Override
    public boolean handleOptionsMenuItemSelected(int id, Intent intent) {
        switch (id) {
        case MENU_REFRESH_ID:
            refreshData(true);
            return true;
        }
        return super.handleOptionsMenuItemSelected(id, intent);
    }

    @Override
    protected void initiateAutomaticSyncImpl() {
        if (!isCurrentTaskListFragment())
            return;
        if (user != null) {
            long lastAutoSync = Preferences.getLong(LAST_FETCH_KEY + user.getId(), 0);
            if (DateUtilities.now() - lastAutoSync > DateUtilities.ONE_HOUR)
                refreshData(false);
        }
    }

    @Override
    protected void refresh() {
        super.refresh();
        setupUserHeader();
    }

    private void refreshData(final boolean manual) {
        if (user != null) {
            emptyView.setText(R.string.DLG_loading);
            new Thread() {
                @Override
                public void run() {
                    // TODO: Fix friend status logic for new sync scheme
                    if (!TextUtils.isEmpty(user.getValue(User.PENDING_STATUS))) {
                        actFmSyncService.pushUser(user);
                        user = userDao.fetch(user.getId(), User.PROPERTIES);
                    }
//                    SyncResultCallback callback;
//                    try {
//                        if (getActivity() == null)
//                            throw new NullPointerException("Person view activity is null"); //$NON-NLS-1$
//                        callback = new ProgressBarSyncResultCallback(getActivity(), PersonViewFragment.this,
//                                R.id.progressBar, new Runnable() {
//                            @Override
//                            public void run() {
//                                if (getActivity() != null) {
//                                    if (manual)
//                                        ContextManager.getContext().sendBroadcast(new Intent(AstridApiConstants.BROADCAST_EVENT_REFRESH));
//                                    else
//                                        refresh();
//                                    emptyView.setText(getEmptyDisplayString());
//                                }
//                            }
//                        });
//                    } catch (Exception e) { // Fragment no longer attached, but sync and refresh anyways
//                        callback = new SyncResultCallbackAdapter() {
//                            @Override
//                            public void finished() {
//                                ContextManager.getContext().sendBroadcast(new Intent(AstridApiConstants.BROADCAST_EVENT_REFRESH));
//                            };
//                        };
//                    }
//
//                    syncService.synchronizeList(user, manual, callback);
                    ActFmSyncThread.getInstance().enqueueMessage(new BriefMe<User>(User.class, user.getValue(User.UUID), user.getValue(User.PUSHED_AT)));
                    // TODO: Refresh
                }
            }.start();
        }
    }

    private String getEmptyDisplayString() {
        String userName = user != null ? user.getDisplayName() : null;
        return TextUtils.isEmpty(userName) ? getString(R.string.actfm_my_shared_tasks_empty) : getString(R.string.TLA_no_items_person, userName);
    }

}
