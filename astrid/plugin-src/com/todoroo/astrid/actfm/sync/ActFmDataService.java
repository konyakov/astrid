/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.actfm.sync;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;

import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.service.ContextManager;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Functions;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.dao.MetadataDao.MetadataCriteria;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.dao.TaskDao.TaskCriteria;
import com.todoroo.astrid.dao.UserDao;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.TagData;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.data.User;
import com.todoroo.astrid.notes.NoteMetadata;
import com.todoroo.astrid.service.MetadataService;
import com.todoroo.astrid.service.TagDataService;

public final class ActFmDataService {

    // --- constants

    /** Utility for joining tasks with metadata */
    public static final Join METADATA_JOIN = Join.left(Metadata.TABLE, Task.ID.eq(Metadata.TASK));

    /** NoteMetadata provider string */
    public static final String NOTE_PROVIDER = "actfm-comment"; //$NON-NLS-1$

    // --- instance variables

    protected final Context context;

    @Autowired TaskDao taskDao;

    @Autowired UserDao userDao;

    @Autowired MetadataService metadataService;

    @Autowired ActFmPreferenceService actFmPreferenceService;

    @Autowired TagDataService tagDataService;

    public ActFmDataService() {
        this.context = ContextManager.getContext();
        DependencyInjectionService.getInstance().inject(this);
    }

    // --- task and metadata methods

    /**
     * Clears metadata information. Used when user logs out of service
     */
    public void clearMetadata() {
        ContentValues values = new ContentValues();
        values.putNull(Task.REMOTE_ID.name);
        taskDao.updateMultiple(values, Criterion.all);
    }

    /**
     * Currently, this method does nothing, there is an alternate method to create tasks
     * @param properties
     * @return
     */
    public TodorooCursor<Task> getLocallyCreated(Property<?>[] properties) {
        return taskDao.query(Query.select(properties).where(Criterion.and(TaskCriteria.isActive(),
                Task.REMOTE_ID.isNull())));
    }

    /**
     * Gets tasks that were modified since last sync
     * @param properties
     * @return null if never sync'd
     */
    public TodorooCursor<Task> getLocallyUpdated(Property<?>[] properties) {
        long lastSyncDate = actFmPreferenceService.getLastSyncDate();
        if(lastSyncDate == 0)
            return taskDao.query(Query.select(properties).where(Criterion.none));
        return
            taskDao.query(Query.select(properties).
                    where(Criterion.and(Task.REMOTE_ID.isNotNull(),
                            Task.MODIFICATION_DATE.gt(lastSyncDate),
                            Task.MODIFICATION_DATE.gt(Task.LAST_SYNC))).groupBy(Task.ID));
    }

    /**
     * Reads task notes out of a task
     */
    public TodorooCursor<Metadata> getTaskNotesCursor(long taskId) {
        TodorooCursor<Metadata> cursor = metadataService.query(Query.select(Metadata.PROPERTIES).
                where(MetadataCriteria.byTaskAndwithKey(taskId, NoteMetadata.METADATA_KEY)));
        return cursor;
    }

    /**
     * Save / Merge JSON tagData
     * @param tagObject
     * @throws JSONException
     */
    @SuppressWarnings("nls")
    public void saveTagData(JSONObject tagObject) throws JSONException {
        TodorooCursor<TagData> cursor = tagDataService.query(Query.select(TagData.PROPERTIES).where(
                Criterion.or(TagData.REMOTE_ID.eq(tagObject.get("id")),
                        Criterion.and(TagData.REMOTE_ID.eq(0),
                        TagData.NAME.eqCaseInsensitive(tagObject.getString("name"))))));
        try {
            cursor.moveToNext();
            TagData tagData = new TagData();
            if(!cursor.isAfterLast()) {
                tagData.readFromCursor(cursor);
                cursor.moveToNext();
            }
            ActFmSyncService.JsonHelper.tagFromJson(tagObject, tagData);
            tagDataService.save(tagData);

            // delete the rest

            for(; !cursor.isAfterLast(); cursor.moveToNext()) {
                tagData.readFromCursor(cursor);
                tagDataService.delete(tagData.getId());
            }
        } finally {
            cursor.close();
        }
    }

    @SuppressWarnings("nls")
    public void saveFeaturedList(JSONObject featObject) throws JSONException {
        TodorooCursor<TagData> cursor = tagDataService.query(Query.select(TagData.PROPERTIES).where(
                Criterion.and(Functions.bitwiseAnd(TagData.FLAGS, TagData.FLAG_FEATURED).gt(0), TagData.REMOTE_ID.eq(featObject.get("id")))));
        try {
            cursor.moveToNext();
            TagData tagData = new TagData();
            if (!cursor.isAfterLast()) {
                tagData.readFromCursor(cursor);
                cursor.moveToNext();
            }
            ActFmSyncService.JsonHelper.featuredListFromJson(featObject, tagData);
            tagDataService.save(tagData);

        } finally {
            cursor.close();
        }
    }

    /**
     * Save / Merge JSON user
     * @param userObject
     * @throws JSONException
     */
    @SuppressWarnings("nls")
    public void saveUserData(JSONObject userObject) throws JSONException {
        TodorooCursor<User> cursor = userDao.query(Query.select(User.PROPERTIES).where(
                Criterion.or(User.REMOTE_ID.eq(userObject.get("id")),
                        Criterion.and(User.EMAIL.isNotNull(), User.EMAIL.eq(userObject.optString("email"))))));
        try {
            cursor.moveToFirst();
            User user = new User();
            if (!cursor.isAfterLast()) {
                user.readFromCursor(cursor);
            }
            ActFmSyncService.JsonHelper.userFromJson(userObject, user);
            userDao.persist(user);

        } finally {
            cursor.close();
        }
    }

    public void addUserByEmail(String email) {
        TodorooCursor<User> cursor = userDao.query(Query.select(User.ID).where(User.EMAIL.eq(email)));
        try {
            if (cursor.getCount() == 0) {
                User user = new User();
                user.setValue(User.REMOTE_ID, Task.USER_ID_IGNORE);
                user.setValue(User.EMAIL, email);
                userDao.persist(user);
            }
        } finally {
            cursor.close();
        }
    }
}
