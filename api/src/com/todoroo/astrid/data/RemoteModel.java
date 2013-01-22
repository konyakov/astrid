/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.data;

import android.text.TextUtils;

import com.todoroo.andlib.data.AbstractModel;
import com.todoroo.andlib.data.Property.LongProperty;
import com.todoroo.andlib.data.Property.StringProperty;

/**
 * A model that is synchronized to a remote server and has a remote id
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
abstract public class RemoteModel extends AbstractModel {

    /** remote id property common to all remote models */
    public static final String REMOTE_ID_PROPERTY_NAME = "remoteId"; //$NON-NLS-1$

    /** remote id property */
    public static final LongProperty REMOTE_ID_PROPERTY = new LongProperty(null, REMOTE_ID_PROPERTY_NAME);

    /** user id property common to all remote models */
    protected static final String USER_ID_PROPERTY_NAME = "userId"; //$NON-NLS-1$

    /** user id property */
    public static final LongProperty USER_ID_PROPERTY = new LongProperty(null, USER_ID_PROPERTY_NAME);

    /** user json property common to all remote models */
    protected static final String USER_JSON_PROPERTY_NAME = "user"; //$NON-NLS-1$

    /** user json property */
    public static final StringProperty USER_JSON_PROPERTY = new StringProperty(null, USER_JSON_PROPERTY_NAME);

    /** pushed at date property name */
    public static final String PUSHED_AT_PROPERTY_NAME = "pushedAt"; //$NON-NLS-1$

    /** pushed at date property name */
    public static final LongProperty PUSHED_AT_PROPERTY = new LongProperty(null, PUSHED_AT_PROPERTY_NAME);

    /** new uuid property */
    public static final String UUID_PROPERTY_NAME = "uuid"; //$NON-NLS-1$

    /** new uuid property */
    public static final StringProperty UUID_PROPERTY = new StringProperty(null, UUID_PROPERTY_NAME);

    /** constant value for no uuid */
    public static final String NO_UUID = "0"; //$NON-NLS-1$

    public static boolean isUuidEmpty(String uuid) {
        return NO_UUID.equals(uuid) || TextUtils.isEmpty(uuid);
    }

    /**
     * Utility method to get the identifier of the model, if it exists.
     *
     * @return {@value #NO_ID} if this model was not added to the database
     */
    abstract public String getUuid();

    protected String getUuidHelper(StringProperty uuid) {
        if(setValues != null && setValues.containsKey(uuid.name))
            return setValues.getAsString(uuid.name);
        else if(values != null && values.containsKey(uuid.name))
            return values.getAsString(uuid.name);
        else
            return NO_UUID;
    }
}
