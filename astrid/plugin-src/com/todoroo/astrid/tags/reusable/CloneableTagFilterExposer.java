package com.todoroo.astrid.tags.reusable;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.todoroo.andlib.service.ContextManager;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.QueryTemplate;
import com.todoroo.astrid.actfm.TagViewFragment;
import com.todoroo.astrid.api.AstridApiConstants;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.api.FilterListItem;
import com.todoroo.astrid.api.FilterWithCustomIntent;
import com.todoroo.astrid.api.FilterWithUpdate;
import com.todoroo.astrid.data.Metadata;
import com.todoroo.astrid.data.TaskApiDao.TaskCriteria;
import com.todoroo.astrid.tags.TagFilterExposer;
import com.todoroo.astrid.tags.TagService;
import com.todoroo.astrid.tags.TagService.Tag;

public abstract class CloneableTagFilterExposer extends TagFilterExposer {

    @Override
    public void onReceive(Context context, Intent intent) {
        addUntaggedFilter = false;
        FilterListItem[] listAsArray = prepareFilters(context);

        Intent broadcastIntent = new Intent(FeaturedListFilterAdapter.BROADCAST_SEND_FEATURED_LISTS);
        broadcastIntent.putExtra(AstridApiConstants.EXTRAS_RESPONSE, listAsArray);
        context.sendBroadcast(broadcastIntent, AstridApiConstants.PERMISSION_READ);
    }

    protected static FilterWithCustomIntent filterFromCloneableList(Tag tag, Criterion criterion, Class<?> fragmentClass) {
        String title = tag.tag;
        QueryTemplate tagTemplate = tag.queryTemplate(criterion);
        ContentValues contentValues = new ContentValues();
        contentValues.put(Metadata.KEY.name, TagService.KEY);
        contentValues.put(TagService.TAG.name, tag.tag);

        FilterWithUpdate filter = new FilterWithUpdate(tag.tag,
                title, tagTemplate,
                contentValues);

        filter.customTaskList = new ComponentName(ContextManager.getContext(), fragmentClass);
        if(tag.image != null)
            filter.imageUrl = tag.image;

        Bundle extras = new Bundle();
        extras.putString(TagViewFragment.EXTRA_TAG_NAME, tag.tag);
        extras.putLong(TagViewFragment.EXTRA_TAG_REMOTE_ID, tag.remoteId);
        filter.customExtras = extras;

        return filter;
    }

    public abstract Class<? extends CloneableTagViewFragment> getFragmentClass();

    @Override
    protected Filter constructFilter(Context context, Tag tag) {
        return filterFromCloneableList(tag, TaskCriteria.activeAndVisible(), getFragmentClass());
    }

}
