package com.maybe.maybe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.maybe.maybe.utils.ColorsConstants;
import com.maybe.maybe.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> titles;
    private HashMap<String, ArrayList<HashMap<String, Object>>> hashMapList;

    public CategoryExpandableListViewAdapter(Context context, List<String> titles, HashMap<String, ArrayList<HashMap<String, Object>>> hashMapList) {
        this.context = context;
        this.titles = titles;
        this.hashMapList = hashMapList;
    }

    @Override
    public int getGroupCount() {
        return titles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return hashMapList.get(titles.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return titles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return hashMapList.get(titles.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.category_list_group, parent, false);

        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.expanded_list_title);
        listTitleTextView.setTextColor(ColorsConstants.SECONDARY_TEXT_COLOR);
        listTitleTextView.setText((String) getGroup(groupPosition));
        convertView.setTag((String) getGroup(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.category_list_item, parent, false);

        TextView expandedListItem = (TextView) convertView.findViewById(R.id.expanded_list_item);
        TextView expandedListCount = (TextView) convertView.findViewById(R.id.expanded_list_count);

        HashMap<String, Object> child = (HashMap<String, Object>) getChild(groupPosition, childPosition);
        expandedListItem.setTextColor(ColorsConstants.PRIMARY_TEXT_COLOR);
        expandedListItem.setText((String) child.get("name"));
        expandedListCount.setTextColor(ColorsConstants.PRIMARY_TEXT_COLOR);
        expandedListCount.setText("" + child.get("count"));
        convertView.setTag((String) child.get("name"));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
