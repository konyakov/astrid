package com.todoroo.astrid.tags.reusable;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.timsu.astrid.R;
import com.todoroo.andlib.utility.DialogUtilities;
import com.todoroo.astrid.data.TagData;
import com.todoroo.astrid.helper.AsyncImageView;

public class FeaturedTaskListFragment extends CloneableTagViewFragment {

    @Override
    protected int getTaskListBodyLayout() {
        return R.layout.task_list_body_featured_list;
    }

    @Override
    protected void setupQuickAddBar() {
        super.setupQuickAddBar();
        quickAddBar.setVisibility(View.GONE);
        getView().findViewById(android.R.id.empty).setOnClickListener(null);
    }

    @Override
    protected void setupHeaderView() {
        AsyncImageView imageView = (AsyncImageView) getView().findViewById(R.id.url_image);
        String imageUrl = tagData.getValue(TagData.PICTURE);
        if (!TextUtils.isEmpty(imageUrl)) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setDefaultImageResource(R.drawable.default_list_0);
            imageView.setUrl(imageUrl);
        } else {
            imageView.setVisibility(View.GONE);
        }

        final String description = tagData.getValue(TagData.TAG_DESCRIPTION);
        final Resources r = getActivity().getResources();
        TextView desc = (TextView) getView().findViewById(R.id.feat_list_desc);
        desc.setText(description);
        desc.setLines(4);
        desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtilities.okDialog(getActivity(), r.getString(R.string.DLG_information_title),
                        0, description, null);
            }
        });
    }
}
