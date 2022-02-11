package com.maybe.maybe.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorTreeAdapter;

import com.maybe.maybe.R;

/*public class CursorTreeAdapter extends SimpleCursorTreeAdapter {

    private DatabaseHelper databaseHelper;

    public CursorTreeAdapter(Context context, Cursor cursor) {
        super(context, cursor, R.layout.category_list_group, new String[]{"name"}, new int[]{R.id.expanded_list_title},
                R.layout.category_list_item, new String[]{"col", "count"}, new int[]{R.id.expanded_list_item, R.id.expanded_list_count});
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        Cursor cursor = null;
        if(groupCursor.getString(groupCursor.getColumnIndex("name")).equals("Playlists"))
            cursor = databaseHelper.selectAllPlaylistsWithAll();
        else if(groupCursor.getString(groupCursor.getColumnIndex("name")).equals("Artists"))
            cursor = databaseHelper.selectAllArtists();
        else if(groupCursor.getString(groupCursor.getColumnIndex("name")).equals("Albums"))
            cursor = databaseHelper.selectAllAlbums();
        return cursor;
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        super.bindGroupView(view, context, cursor, isExpanded);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        super.bindChildView(view, context, cursor, isLastChild);
        view.setTag(cursor.getString(cursor.getColumnIndex("col")));
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

}*/