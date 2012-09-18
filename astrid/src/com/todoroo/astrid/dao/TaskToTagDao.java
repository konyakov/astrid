package com.todoroo.astrid.dao;

import com.todoroo.andlib.data.DatabaseDao;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Query;
import com.todoroo.andlib.utility.Preferences;
import com.todoroo.astrid.data.TaskToTag;
import com.todoroo.astrid.service.StatisticsConstants;
import com.todoroo.astrid.service.StatisticsService;
import com.todoroo.astrid.utility.AstridPreferences;

public class TaskToTagDao extends DatabaseDao<TaskToTag> {

    @Autowired
    private Database database;

    public TaskToTagDao() {
        super(TaskToTag.class);
        DependencyInjectionService.getInstance().inject(this);
        setDatabase(database);
    }

    @Override
    public boolean createNew(TaskToTag item) {
        if(Preferences.getBoolean(AstridPreferences.P_FIRST_LIST, true)) {
            StatisticsService.reportEvent(StatisticsConstants.USER_FIRST_LIST);
            Preferences.setBoolean(AstridPreferences.P_FIRST_LIST, false);
        }
        return super.createNew(item);
    }

    public boolean createLink(long taskRemoteId, long tagRemoteId) {
        TodorooCursor<TaskToTag> existing = query(Query.select(TaskToTag.ID).where(Criterion.and(
                TaskToTag.TASK_REMOTEID.eq(taskRemoteId),
                TaskToTag.TAG_REMOTEID.eq(tagRemoteId),
                Criterion.not(TaskToTag.DELETED_AT.gt(0)))));
        try {
            if (existing.getCount() > 0)
                return true;

            TaskToTag link = new TaskToTag();
            link.setValue(TaskToTag.TASK_REMOTEID, taskRemoteId);
            link.setValue(TaskToTag.TAG_REMOTEID, tagRemoteId);
            return createNew(link);
        } finally {
            existing.close();
        }
    }
}
