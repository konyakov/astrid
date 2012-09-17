/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.tags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.timsu.astrid.R;
import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.Property.CountProperty;
import com.todoroo.andlib.data.Property.LongProperty;
import com.todoroo.andlib.data.Property.StringProperty;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Field;
import com.todoroo.andlib.sql.Functions;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.Order;
import com.todoroo.andlib.sql.Query;
import com.todoroo.andlib.sql.QueryTemplate;
import com.todoroo.andlib.utility.DateUtilities;
import com.todoroo.astrid.actfm.TagViewFragment;
import com.todoroo.astrid.api.AstridApiConstants;
import com.todoroo.astrid.core.PluginServices;
import com.todoroo.astrid.dao.MetadataDao;
import com.todoroo.astrid.dao.MetadataDao.MetadataCriteria;
import com.todoroo.astrid.dao.TaskDao.TaskCriteria;
import com.todoroo.astrid.dao.TaskToTagDao;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.TagData;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.data.TaskApiDao;
import com.todoroo.astrid.data.TaskToTag;
import com.todoroo.astrid.service.MetadataService;
import com.todoroo.astrid.service.TagDataService;
import com.todoroo.astrid.service.TaskService;
import com.todoroo.astrid.utility.Flags;

/**
 * Provides operations for working with tags
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
@SuppressWarnings("nls")
public final class TagService {

    public static final String TOKEN_TAG_SQL = "tagSql"; //$NON-NLS-1$
    public static final String SHOW_ACTIVE_TASKS = "show_main_task_view"; //$NON-NLS-1$

    // --- public constants

    /** Metadata key for tag data */
    public static final String KEY = "tags-tag";

    /** Property for reading tag values */
    public static final StringProperty TAG = Metadata.VALUE1;

    /** Property for astrid.com remote id */
    public static final LongProperty REMOTE_ID = new LongProperty(Metadata.TABLE, Metadata.VALUE2.name);

    public static final CountProperty COUNT = new CountProperty();

    // --- singleton

    private static TagService instance = null;

    private static int[] default_tag_images = new int[] {
        R.drawable.default_list_0,
        R.drawable.default_list_1,
        R.drawable.default_list_2,
        R.drawable.default_list_3
    };

    public static synchronized TagService getInstance() {
        if(instance == null)
            instance = new TagService();
        return instance;
    }

    // --- implementation details

    @Autowired
    private MetadataDao metadataDao;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TagDataService tagDataService;

    @Autowired
    private TaskToTagDao taskToTagDao;

    public TagService() {
        DependencyInjectionService.getInstance().inject(this);
    }

    /**
     * Helper class for returning a tag/task count pair
     *
     * @author Tim Su <tim@todoroo.com>
     *
     */
    public static final class Tag {
        public String tag;
        public int count;
        public long id;
        public long remoteId;
        public String image;
        public long userId;
        public long memberCount;

        @Deprecated
        private Tag(String tag, int count, long remoteId) {
            this.tag = tag;
            this.count = count;
            this.remoteId = remoteId;
        }

        public Tag(TagData tagData) {
            id = tagData.getId();
            tag = tagData.getValue(TagData.NAME);
            count = tagData.getValue(TagData.TASK_COUNT);
            remoteId = tagData.getValue(TagData.REMOTE_ID);
            image = tagData.getValue(TagData.PICTURE);
            userId = tagData.getValue(TagData.USER_ID);
            memberCount = tagData.getValue(TagData.MEMBER_COUNT);
        }

        @Override
        public String toString() {
            return tag;
        }

        private static final String LINK_TABLE_ALIAS = "taglinks";
        private static final String TAG_TABLE_ALIAS = "tagTableAlias";
        /**
         * Return SQL selector query for getting tasks with a given tagData
         *
         * @param tagData
         * @return
         */
        public QueryTemplate queryTemplate(Criterion criterion) {
            String linkPrefix = LINK_TABLE_ALIAS + ".";
            String tagPrefix = TAG_TABLE_ALIAS + ".";

            Field tagIdField = Field.field(tagPrefix + TagData.ID.name);
            Field tagRemoteIdField = Field.field(tagPrefix + TagData.REMOTE_ID.name);

            return new QueryTemplate()
                .join(
                    Join.inner(TaskToTag.TABLE.as(LINK_TABLE_ALIAS),
                            Criterion.or(
                                    Task.ID.eq(Field.field(linkPrefix + TaskToTag.TASK_ID.name)),
                                    Task.REMOTE_ID.eq(Field.field(linkPrefix + TaskToTag.TASK_REMOTEID)))))
                .join(
                    Join.inner(TagData.TABLE.as(TAG_TABLE_ALIAS),
                            Criterion.or(
                                    Field.field(linkPrefix + TaskToTag.TAG_ID.name).eq(tagIdField),
                                    Field.field(linkPrefix + TaskToTag.TAG_REMOTEID.name).eq(tagRemoteIdField))))
                .where(Criterion.and(
                        Criterion.or(tagIdField.eq(id), tagRemoteIdField.eq(remoteId)),
                        criterion));
        }

    }

    public static Criterion memberOfTagData(long tagDataId, long tagDataRemoteId) {
        Criterion criterion = Criterion.none;
        if (tagDataId > 0 && tagDataRemoteId > 0) {
            criterion = Criterion.or(TaskToTag.TAG_ID.eq(tagDataId), TaskToTag.TAG_REMOTEID.eq(tagDataRemoteId));
        } else if (tagDataId > 0) {
            criterion = TaskToTag.TAG_ID.eq(tagDataId);
        } else if (tagDataRemoteId > 0) {
            criterion = TaskToTag.TAG_REMOTEID.eq(tagDataRemoteId);
        }

        return Task.ID.in(Query.select(TaskToTag.TASK_ID).from(TaskToTag.TABLE).where(criterion));
    }

    @Deprecated
    public static Criterion tagEq(String tag, Criterion additionalCriterion) {
        return Criterion.and(
                MetadataCriteria.withKey(KEY), TAG.eq(tag),
                additionalCriterion);
    }

    @Deprecated
    public static Criterion tagEqIgnoreCase(String tag, Criterion additionalCriterion) {
        return Criterion.and(
                MetadataCriteria.withKey(KEY), TAG.eqCaseInsensitive(tag),
                additionalCriterion);
    }

    public QueryTemplate untaggedTemplate() {
        Long[] emergentTagIds = getEmergentTagIds();

        return new QueryTemplate().where(Criterion.and(
                Criterion.not(Task.ID.in(Query.select(TaskToTag.TASK_ID).from(TaskToTag.TABLE).where(Criterion.not(TaskToTag.TAG_ID.in(emergentTagIds))))),
                TaskCriteria.isActive(),
                TaskApiDao.TaskCriteria.ownedByMe(),
                TaskCriteria.isVisible()));
    }

    /**
     * Return all tags ordered by given clause
     *
     * @param order ordering
     * @param activeStatus criterion for specifying completed or uncompleted
     * @return empty array if no tags, otherwise array
     */
    @Deprecated
    public Tag[] getGroupedTags(Order order, Criterion activeStatus, boolean includeEmergent) {
        Criterion criterion;
        if (includeEmergent)
            criterion = Criterion.and(activeStatus, MetadataCriteria.withKey(KEY));
        else
            criterion = Criterion.and(activeStatus, MetadataCriteria.withKey(KEY), Criterion.not(TAG.in(getEmergentTags())));
        Query query = Query.select(TAG, REMOTE_ID, COUNT).
            join(Join.inner(Task.TABLE, Metadata.TASK.eq(Task.ID))).
            where(criterion).
            orderBy(order).groupBy(TAG);
        TodorooCursor<Metadata> cursor = metadataDao.query(query);
        try {
            Tag[] array = new Tag[cursor.getCount()];
            for (int i = 0; i < array.length; i++) {
                cursor.moveToNext();
                array[i] = new Tag(cursor.get(TAG), cursor.get(COUNT), cursor.get(REMOTE_ID));
            }
            return array;
        } finally {
            cursor.close();
        }
    }

    public String[] getEmergentTags() {
        TodorooCursor<TagData> emergent = tagDataService.query(Query.select(TagData.NAME)
                .where(Functions.bitwiseAnd(TagData.FLAGS, TagData.FLAG_EMERGENT).gt(0)));
        try {
            String[] tags = new String[emergent.getCount()];
            TagData data = new TagData();
            for (int i = 0; i < emergent.getCount(); i++) {
                emergent.moveToPosition(i);
                data.readFromCursor(emergent);
                tags[i] = data.getValue(TagData.NAME);
            }
            return tags;
        } finally {
            emergent.close();
        }
    }

    public Long[] getEmergentTagIds() {
        TodorooCursor<TagData> emergent = tagDataService.query(Query.select(TagData.ID)
                .where(Functions.bitwiseAnd(TagData.FLAGS, TagData.FLAG_EMERGENT).gt(0)));
        try {
            Long[] tags = new Long[emergent.getCount()];
            TagData data = new TagData();
            for (int i = 0; i < emergent.getCount(); i++) {
                emergent.moveToPosition(i);
                data.readFromCursor(emergent);
                tags[i] = data.getId();
            }
            return tags;
        } finally {
            emergent.close();
        }
    }

    /**
     * Return tags on the given task
     *
     * @param taskId
     * @return cursor. PLEASE CLOSE THE CURSOR!
     */
    public TodorooCursor<TagData> getTags(long taskId, boolean includeEmergent, Property<?>... properties) {
        Criterion criterion;
        if (includeEmergent) {
            criterion = Criterion.all;
        } else {
            criterion = Criterion.not(TagData.ID.in(getEmergentTagIds()));
        }
        Query query = Query.select(properties)
                .join(Join.inner(TaskToTag.TABLE, Criterion.or(TaskToTag.TAG_ID.eq(TagData.ID), TaskToTag.TAG_REMOTEID.eq(TagData.REMOTE_ID))))
                .where(Criterion.and(TaskToTag.TASK_ID.eq(taskId), criterion));

        return tagDataService.query(query);
    }

    /**
     * Return tags as a comma-separated list of strings
     *
     * @param taskId
     * @return empty string if no tags, otherwise string
     */
    public String getTagsAsString(long taskId, boolean includeEmergent) {
        return getTagsAsString(taskId, ", ", includeEmergent);
    }

    /**
     * Return tags as a list of strings separated by given separator
     *
     * @param taskId
     * @return empty string if no tags, otherwise string
     */
    public String getTagsAsString(long taskId, String separator, boolean includeEmergent) {
        StringBuilder tagBuilder = new StringBuilder();
        TodorooCursor<TagData> tags = getTags(taskId, includeEmergent, TagData.NAME);
        try {
            int length = tags.getCount();
            TagData tag = new TagData();
            for (int i = 0; i < length; i++) {
                tags.moveToNext();
                tag.readFromCursor(tags);
                tagBuilder.append(tag.getValue(TagData.NAME));
                if (i < length - 1)
                    tagBuilder.append(separator);
            }
        } finally {
            tags.close();
        }
        return tagBuilder.toString();
    }

    public boolean deleteOrLeaveTag(Context context, String tag, String sql) {
        int deleted = deleteTagMetadata(tag);
        TagData tagData = PluginServices.getTagDataService().getTag(tag, TagData.ID, TagData.DELETION_DATE, TagData.MEMBER_COUNT, TagData.USER_ID);
        boolean shared = false;
        if(tagData != null) {
            tagData.setValue(TagData.DELETION_DATE, DateUtilities.now());
            PluginServices.getTagDataService().save(tagData);
            shared = tagData.getValue(TagData.MEMBER_COUNT) > 0 && tagData.getValue(TagData.USER_ID) != 0; // Was I a list member and NOT owner?
        }
        Toast.makeText(context, context.getString(shared ? R.string.TEA_tags_left : R.string.TEA_tags_deleted, tag, deleted),
                Toast.LENGTH_SHORT).show();

        Intent tagDeleted = new Intent(AstridApiConstants.BROADCAST_EVENT_TAG_DELETED);
        tagDeleted.putExtra(TagViewFragment.EXTRA_TAG_NAME, tag);
        tagDeleted.putExtra(TOKEN_TAG_SQL, sql);
        context.sendBroadcast(tagDeleted);
        return true;
    }

    public void linkTaskToTag(Task task, String tag) {
        TodorooCursor<TagData> existing = tagDataService.query(Query.select(TagData.ID, TagData.REMOTE_ID).where(TagData.NAME.eqCaseInsensitive(tag)));
        try {
            TagData tagData;
            if (existing.getCount() > 0) {
                tagData = new TagData(existing);
            } else {
                tagData = new TagData();
                tagData.setValue(TagData.NAME, tag);
                tagDataService.save(tagData);
            }

            TaskToTag link = new TaskToTag();
            link.setValue(TaskToTag.TASK_ID, task.getId());
            link.setValue(TaskToTag.TASK_REMOTEID, task.getValue(Task.REMOTE_ID));
            link.setValue(TaskToTag.TAG_ID, tagData.getId());
            link.setValue(TaskToTag.TAG_REMOTEID, tagData.getValue(TagData.REMOTE_ID));
            taskToTagDao.createNew(link);
        } finally {
            existing.close();
        }
    }

    /**
     * Return all tags (including metadata tags and TagData tags) in an array list
     * @return
     */
    public ArrayList<Tag> getTagList() {
        Query query = Query.select(TagData.PROPERTIES)
                .where(Criterion.and(Criterion.or(TagData.DELETION_DATE.eq(0), TagData.DELETION_DATE.isNull()),
                        Functions.bitwiseAnd(TagData.FLAGS, TagData.FLAG_EMERGENT).eq(0),
                        Functions.bitwiseAnd(TagData.FLAGS, TagData.FLAG_FEATURED).eq(0)))
                .orderBy(Order.asc(TagData.NAME));

        return getTagListFromQuery(query);
    }

    public ArrayList<Tag> getFeaturedLists() {
        Query query = Query.select(TagData.PROPERTIES)
                .where(Criterion.and(Functions.bitwiseAnd(TagData.FLAGS, TagData.FLAG_FEATURED).gt(0),
                        Criterion.or(TagData.DELETION_DATE.eq(0), TagData.DELETION_DATE.isNull())))
                .orderBy(Order.asc(TagData.NAME));
        return getTagListFromQuery(query);
    }

    private ArrayList<Tag> getTagListFromQuery(Query query) {
        ArrayList<Tag> tags = new ArrayList<Tag>();

        TodorooCursor<TagData> cursor = tagDataService.query(query);
        try {
            TagData tagData = new TagData();
            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                tagData.readFromCursor(cursor);
                Tag tag = new Tag(tagData);
                if(TextUtils.isEmpty(tag.tag))
                    continue;
                tags.add(tag);
            }
        } finally {
            cursor.close();
        }
        return tags;
    }

    /**
     * Save the given array of tags into the database
     * @param taskId
     * @param tags
     */
    public boolean synchronizeTags(long taskId, Set<String> tags) {
        HashSet<Long> existingLinks = new HashSet<Long>();
        TodorooCursor<TaskToTag> links = taskToTagDao.query(Query.select(TaskToTag.PROPERTIES).where(TaskToTag.TASK_ID.eq(taskId)));
        try {
            for (links.moveToFirst(); !links.isAfterLast(); links.moveToNext()) {
                TaskToTag link = new TaskToTag(links);
                existingLinks.add(link.getValue(TaskToTag.TAG_REMOTEID));
            }
        } finally {
            links.close();
        }

        for (String tag : tags) {
            TagData tagData = getTagDataWithCase(tag, TagData.NAME, TagData.REMOTE_ID);
            if (tagData == null) {
                tagData = new TagData();
                tagData.setValue(TagData.NAME, tag);
                tagDataService.save(tagData);
            }
            if (existingLinks.contains(tagData.getValue(TagData.REMOTE_ID))) {
                existingLinks.remove(tagData.getValue(TagData.REMOTE_ID));
            } else {
                TaskToTag newLink = new TaskToTag();
                newLink.setValue(TaskToTag.TASK_ID, taskId);
                newLink.setValue(TaskToTag.TAG_REMOTEID, tagData.getValue(TagData.REMOTE_ID));
                taskToTagDao.createNew(newLink);
            }
        }

        // Mark as deleted links that don't exist anymore
        TaskToTag deletedLinkTemplate = new TaskToTag();
        deletedLinkTemplate.setValue(TaskToTag.DELETED_AT, DateUtilities.now());
        taskToTagDao.update(Criterion.and(TaskToTag.TASK_ID.eq(taskId),
                TaskToTag.TAG_REMOTEID.in(existingLinks.toArray(new Long[existingLinks.size()]))), deletedLinkTemplate);

        return true;
    }

    /**
     * If a tag already exists in the database that case insensitively matches the
     * given tag, return that. Otherwise, return the argument
     * @param tag
     * @return
     */
    public String getTagWithCase(String tag) {
        String tagWithCase = tag;

        TodorooCursor<TagData> tagData = tagDataService.query(Query.select(TagData.NAME).where(TagData.NAME.eqCaseInsensitive(tag)));
        try {
            if (tagData.getCount() > 0) {
                tagData.moveToFirst();
                tagWithCase = new TagData(tagData).getValue(TagData.NAME);
            }
        } finally {
            tagData.close();
        }
        return tagWithCase;
    }

    public TagData getTagDataWithCase(String tag, Property<?>... properties) {
        TodorooCursor<TagData> tagData = tagDataService.query(Query.select(properties).where(TagData.NAME.eqCaseInsensitive(tag)));
        try {
            if (tagData.getCount() > 0) {
                tagData.moveToFirst();
                return new TagData(tagData);
            }
        } finally {
            tagData.close();
        }
        return null;
    }

    public int deleteTagMetadata(String tag) {
        invalidateTaskCache(tag);
        return PluginServices.getMetadataService().deleteWhere(tagEqIgnoreCase(tag, Criterion.all));
    }

    @Deprecated
    public int renameCaseSensitive(String oldTag, String newTag) { // Need this for tag case migration process
        return renameHelper(oldTag, newTag, true);
    }

    @Deprecated
    private int renameHelper(String oldTag, String newTag, boolean caseSensitive) {
     // First remove newTag from all tasks that have both oldTag and newTag.
        MetadataService metadataService = PluginServices.getMetadataService();
        metadataService.deleteWhere(
                Criterion.and(
                        Metadata.VALUE1.eq(newTag),
                        Metadata.TASK.in(rowsWithTag(oldTag, Metadata.TASK))));

        // Then rename all instances of oldTag to newTag.
        Metadata metadata = new Metadata();
        metadata.setValue(TAG, newTag);
        int ret;
        if (caseSensitive)
            ret = metadataService.update(tagEq(oldTag, Criterion.all), metadata);
        else
            ret = metadataService.update(tagEqIgnoreCase(oldTag, Criterion.all), metadata);
        invalidateTaskCache(newTag);
        return ret;
    }


    private Query rowsWithTag(String tag, Field... projections) {
        return Query.select(projections).from(Metadata.TABLE).where(Metadata.VALUE1.eq(tag));
    }

    private void invalidateTaskCache(String tag) {
        taskService.clearDetails(Task.ID.in(rowsWithTag(tag, Task.ID)));
        Flags.set(Flags.REFRESH);
    }

    public static int getDefaultImageIDForTag(long remoteID) {
        if (remoteID <= 0) {
            int random = (int)(Math.random()*4);
            return default_tag_images[random];
        }
        return default_tag_images[((int)remoteID)%4];
    }
    public static int getDefaultImageIDForTag(String title) {
        return getDefaultImageIDForTag(Math.abs(title.hashCode()));
    }
}
