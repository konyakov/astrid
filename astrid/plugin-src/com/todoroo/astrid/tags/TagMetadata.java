package com.todoroo.astrid.tags;

import com.todoroo.andlib.data.Property.LongProperty;
import com.todoroo.andlib.data.Property.StringProperty;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.Task;

public class TagMetadata {

    /** Metadata key for tag data */
    public static final String KEY = "tags-tag"; //$NON-NLS-1$

    /** Property for reading tag values */
    public static final StringProperty TAG_NAME = Metadata.VALUE1;

    /** Tag uuid */
    public static final StringProperty TAG_UUID = new StringProperty(
            Metadata.TABLE, Metadata.VALUE2.name);

    /** Pushed at time */
    public static final LongProperty PUSHED_AT = new LongProperty(
            Metadata.TABLE, Metadata.VALUE4.name);


    // Creation date and deletion date are already included as part of the normal metadata entity

    /**
     * New metadata object for linking a task to the specified tag. The task
     * object should be saved and have the uuid property. All parameters
     * are manditory
     * @param task
     * @param tagName
     * @param tagUuid
     * @return
     */
    public static Metadata newTagMetadata(Task task, String tagName, String tagUuid) {
        return newTagMetadata(task.getValue(Task.UUID), tagName, tagUuid);
    }

    public static Metadata newTagMetadata(String taskUuid, String tagName, String tagUuid) {
        Metadata link = new Metadata();
        link.setValue(Metadata.KEY, KEY);
        link.setValue(TAG_NAME, tagName);
        link.setValue(Metadata.TASK_UUID, taskUuid);
        link.setValue(TAG_UUID, tagUuid);
        link.setValue(Metadata.DELETION_DATE, 0L);
        return link;
    }
}
