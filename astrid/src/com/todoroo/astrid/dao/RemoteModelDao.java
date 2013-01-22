package com.todoroo.astrid.dao;

import com.todoroo.andlib.data.DatabaseDao;
import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.Property.StringProperty;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.data.RemoteModel;
import com.todoroo.astrid.helper.UUIDHelper;

/**
 * This class is meant to be subclassed for daos whose models
 * require UUID generation (i.e., most RemoteModels). The createNew
 * method takes care of automatically generating a new UUID for each newly
 * created model if one doesn't already exist.
 * @author Sam
 *
 * @param <RTYPE>
 */
public abstract class RemoteModelDao<RTYPE extends RemoteModel> extends DatabaseDao<RTYPE> {

    public RemoteModelDao(Class<RTYPE> modelClass) {
        super(modelClass);
    }

    @Override
    public boolean createNew(RTYPE item) {
        if (!item.containsValue(RemoteModel.UUID_PROPERTY)) {
            item.setValue(RemoteModel.UUID_PROPERTY, UUIDHelper.newUUID());
        }
        return super.createNew(item);
    };

    protected TodorooCursor<RTYPE> fetchItem(String uuid, Property<?>... properties) {
        TodorooCursor<RTYPE> cursor = query(
                Query.select(properties).where(RemoteModel.UUID_PROPERTY.eq(uuid)));
        cursor.moveToFirst();
        return new TodorooCursor<RTYPE>(cursor, properties);
    }

    public abstract String uuidForLocalId(long localId);

    protected String uuidForLocalIdHelper(long localId, StringProperty uuidProperty) {
        RTYPE model = fetch(localId, uuidProperty);
        return model.getUuid();
    }

    public RTYPE fetchByUuid(String uuid, Property<?>... properties) {
        TodorooCursor<RTYPE> cursor = fetchItem(uuid, properties);
        return returnFetchResult(cursor);
    }


}
