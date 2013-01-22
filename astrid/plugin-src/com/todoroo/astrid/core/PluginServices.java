/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.core;

import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.service.ExceptionService;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.dao.Database;
import com.todoroo.astrid.dao.MetadataDao.MetadataCriteria;
import com.todoroo.astrid.dao.StoreObjectDao;
import com.todoroo.astrid.dao.TagDataDao;
import com.todoroo.astrid.dao.TagOutstandingDao;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.dao.TaskOutstandingDao;
import com.todoroo.astrid.dao.UserDao;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.service.AddOnService;
import com.todoroo.astrid.service.AstridDependencyInjector;
import com.todoroo.astrid.service.MetadataService;
import com.todoroo.astrid.service.TagDataService;
import com.todoroo.astrid.service.TaskService;

/**
 * Utility class for getting dependency-injected services from plugins
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public final class PluginServices {

    @Autowired
    TaskService taskService;

    @Autowired
    TaskDao taskDao;

    @Autowired
    Database database;

    @Autowired
    ExceptionService exceptionService;

    @Autowired
    MetadataService metadataService;

    @Autowired
    AddOnService addOnService;

    @Autowired
    TagDataService tagDataService;

    @Autowired
    TagDataDao tagDataDao;

    @Autowired
    StoreObjectDao storeObjectDao;

    @Autowired
    UserDao userDao;

    @Autowired
    TaskOutstandingDao taskOutstandingDao;

    @Autowired
    TagOutstandingDao tagOutstandingDao;

    private static PluginServices instance;

    static {
        AstridDependencyInjector.initialize();
    }

    private PluginServices() {
        DependencyInjectionService.getInstance().inject(this);
    }

    private synchronized static PluginServices getInstance() {
        if(instance == null)
            instance = new PluginServices();
        return instance;
    }

    public static Database getDatabase() {
        getInstance().database.openForWriting();
        return getInstance().database;
    }

    public static TaskService getTaskService() {
        getInstance().database.openForWriting();
        return getInstance().taskService;
    }

    public static TaskDao getTaskDao() {
        return getInstance().taskDao;
    }

    public static TagDataService getTagDataService() {
        return getInstance().tagDataService;
    }

    public static TagDataDao getTagDataDao() {
        return getInstance().tagDataDao;
    }

    public static TaskOutstandingDao getTaskOutstandingDao() {
        return getInstance().taskOutstandingDao;
    }

    public static TagOutstandingDao getTagOutstandingDao() {
        return getInstance().tagOutstandingDao;
    }

    public static ExceptionService getExceptionService() {
        return getInstance().exceptionService;
    }

    public static MetadataService getMetadataService() {
        getInstance().database.openForWriting();
        return getInstance().metadataService;
    }

    public static AddOnService getAddOnService() {
        return getInstance().addOnService;
    }

    public static StoreObjectDao getStoreObjectDao() {
        return getInstance().storeObjectDao;
    }

    public static UserDao getUserDao() {
        return getInstance().userDao;
    }

    // -- helpers

    /**
     * Find the corresponding metadata for this task
     */
    public static Metadata getMetadataByTaskAndWithKey(String taskUuid, String metadataKey) {
        TodorooCursor<Metadata> cursor = PluginServices.getMetadataService().query(Query.select(
                Metadata.PROPERTIES).where(MetadataCriteria.byTaskAndwithKey(taskUuid, metadataKey)));
                try {
            if(cursor.getCount() > 0) {
                cursor.moveToNext();
                return new Metadata(cursor);
            } else
                return null;
        } finally {
            cursor.close();
        }

    }
}
