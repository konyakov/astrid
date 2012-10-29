package com.todoroo.astrid.tags.reusable;

import java.util.List;

import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Functions;
import com.todoroo.andlib.sql.Order;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.core.PluginServices;
import com.todoroo.astrid.data.TagData;
import com.todoroo.astrid.data.TaskApiDao.TaskCriteria;
import com.todoroo.astrid.tags.TagService;
import com.todoroo.astrid.tags.TagService.Tag;

public class ReusableListFilterExposer extends CloneableTagFilterExposer {

    public static Filter getDefaultFilter() {
        TodorooCursor<TagData> firstFilter = PluginServices.getTagDataService()
        .query(Query.select(TagData.PROPERTIES)
                .where(Criterion.and(
                        Functions.bitwiseAnd(TagData.FLAGS, TagData.FLAG_REUSABLE).gt(0),
                        TagData.DELETION_DATE.eq(0),
                        TagData.NAME.isNotNull(),
                        TagData.NAME.neq(""))) //$NON-NLS-1$
                        .orderBy(Order.asc(TagData.NAME))
                        .limit(1));
        try {
            if (firstFilter.getCount() > 0) {
                firstFilter.moveToFirst();
                TagData tagData = new TagData(firstFilter);
                Tag tag = new Tag(tagData);
                return filterFromCloneableList(tag, TaskCriteria.activeAndVisible(), ReusableTaskListFragment.class);
            } else {
                TagData test = new TagData();
                test.setValue(TagData.NAME, "Reusable List!"); //$NON-NLS-1$
                test.setFlag(TagData.FLAGS, TagData.FLAG_REUSABLE, true);
                PluginServices.getTagDataService().save(test);
                Tag tag = new Tag(test);
                return filterFromCloneableList(tag, TaskCriteria.activeAndVisible(), ReusableTaskListFragment.class);
            }
        } finally {
            firstFilter.close();
        }
    }

    @Override
    protected List<Tag> getTagList() {
        return TagService.getInstance().getReusableLists();
    }

    @Override
    public Class<? extends CloneableTagViewFragment> getFragmentClass() {
        return ReusableTaskListFragment.class;
    }

}
