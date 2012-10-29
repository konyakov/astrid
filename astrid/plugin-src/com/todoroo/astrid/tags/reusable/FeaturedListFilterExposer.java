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

public class FeaturedListFilterExposer extends CloneableTagFilterExposer {

    public static final String PREF_SHOULD_SHOW_FEATURED_LISTS = "show_featured_lists"; //$NON-NLS-1$

    public static Filter getDefaultFilter() {
        TodorooCursor<TagData> firstFilter = PluginServices.getTagDataService()
        .query(Query.select(TagData.PROPERTIES)
                .where(Criterion.and(
                        Functions.bitwiseAnd(TagData.FLAGS, TagData.FLAG_FEATURED).gt(0),
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
                return filterFromCloneableList(tag, TaskCriteria.activeAndVisible(), FeaturedTaskListFragment.class);
            } else {
                return null;
            }
        } finally {
            firstFilter.close();
        }
    }

    @Override
    protected List<Tag> getTagList() {
        return TagService.getInstance().getFeaturedLists();
    }

    @Override
    public Class<? extends CloneableTagViewFragment> getFragmentClass() {
        return FeaturedTaskListFragment.class;
    }

    @Override
    public String getBroadcastAction() {
        return FeaturedListFilterAdapter.BROADCAST_SEND_FEATURED_LISTS;
    }

}
