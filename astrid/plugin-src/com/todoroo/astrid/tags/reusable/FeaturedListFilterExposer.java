package com.todoroo.astrid.tags.reusable;

import java.util.List;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.service.ContextManager;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Functions;
import com.todoroo.andlib.sql.Order;
import com.todoroo.andlib.sql.Query;
import com.todoroo.andlib.sql.QueryTemplate;
import com.todoroo.astrid.actfm.TagViewFragment;
import com.todoroo.astrid.api.AstridApiConstants;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.api.FilterListItem;
import com.todoroo.astrid.api.FilterWithCustomIntent;
import com.todoroo.astrid.api.FilterWithUpdate;
import com.todoroo.astrid.core.PluginServices;
import com.todoroo.astrid.data.TagData;
import com.todoroo.astrid.data.TaskApiDao.TaskCriteria;
import com.todoroo.astrid.tags.TagFilterExposer;
import com.todoroo.astrid.tags.TagService;
import com.todoroo.astrid.tags.TagService.Tag;

public class FeaturedListFilterExposer extends TagFilterExposer {

    @Override
    public void onReceive(Context context, Intent intent) {
        addUntaggedFilter = false;
        FilterListItem[] listAsArray = prepareFilters(context);

        Intent broadcastIntent = new Intent(FeaturedListFilterAdapter.BROADCAST_SEND_FEATURED_LISTS);
        broadcastIntent.putExtra(AstridApiConstants.EXTRAS_RESPONSE, listAsArray);
        context.sendBroadcast(broadcastIntent, AstridApiConstants.PERMISSION_READ);
    }

    private static FilterWithCustomIntent filterFromFeaturedList(Tag tag, Criterion criterion) {
        String title = tag.tag;
        QueryTemplate tagTemplate = tag.queryTemplate(criterion);
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TAG_REMOTE_ID, tag.remoteId);

        FilterWithUpdate filter = new FilterWithUpdate(tag.tag,
                title, tagTemplate,
                contentValues);

        Class<?> fragmentClass = FeaturedTaskListFragment.class;
        filter.customTaskList = new ComponentName(ContextManager.getContext(), fragmentClass);
        if(tag.image != null)
            filter.imageUrl = tag.image;

        Bundle extras = new Bundle();
        extras.putString(TagViewFragment.EXTRA_TAG_NAME, tag.tag);
        extras.putLong(TagViewFragment.EXTRA_TAG_REMOTE_ID, tag.remoteId);
        filter.customExtras = extras;

        return filter;
    }

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
                return filterFromFeaturedList(tag, TaskCriteria.activeAndVisible());
            } else {
                return null;
            }
        } finally {
            firstFilter.close();
        }
    }

    @Override
    protected Filter constructFilter(Context context, Tag tag) {
        return filterFromFeaturedList(tag, TaskCriteria.activeAndVisible());
    }

    @Override
    protected List<Tag> getTagList() {
        return TagService.getInstance().getFeaturedLists();
    }

}
