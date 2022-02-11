package com.maybe.maybe.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.maybe.maybe.R;

import java.util.ArrayList;
import java.util.HashMap;

/*public class CardsAdapter extends CursorAdapter implements SectionIndexer {

    private static final String TAG = "CardsAdapter";
    HashMap<Character, Integer> alphaIndexer;
    Character[] sections;

    public CardsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);

        alphaIndexer = new HashMap<>();
        ArrayList<Character> temp = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                char letter = cursor.getString(cursor.getColumnIndex("title")).toUpperCase().charAt(0);
                if (!alphaIndexer.containsKey(letter)) {
                    alphaIndexer.put(letter, cursor.getPosition());
                    temp.add(letter);
                    Log.d(TAG, "section = " + letter);
                }
            }
        }

        sections = new Character[temp.size()];
        temp.toArray(sections);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.main_recycler_view_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView mainItemTrack, mainItemTitle, mainItemArtist;//, mainItemAlbum;

        //ConstraintLayout mainCardLayout = (ConstraintLayout) view.findViewById(R.id.main_card_layout);
        mainItemTrack = (TextView) view.findViewById(R.id.main_item_track);
        mainItemTitle = (TextView) view.findViewById(R.id.main_item_title);
        mainItemArtist = (TextView) view.findViewById(R.id.main_item_artist);
        //mainItemAlbum = (TextView) view.findViewById(R.id.main_item_album);

        view.setTag(R.string.index, cursor.getPosition());
        view.setTag(R.string.db_file_id, Integer.valueOf(cursor.getInt(cursor.getColumnIndex("id"))));
        if (cursor.getPosition() % 2 == 0)
            view.setBackgroundColor(Color.argb(40, 0, 0, 0));
        else
            view.setBackgroundColor(Color.argb(0, 0, 0, 0));
        mainItemTrack.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("music_track"))));
        mainItemTitle.setText(cursor.getString(cursor.getColumnIndex("title")));
        mainItemArtist.setText(cursor.getString(cursor.getColumnIndex("artist")));
        //mainItemAlbum.setText(cursor.getString(cursor.getColumnIndex("music_album")));
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        //Log.d("position for section", "" + alphaIndexer.get(sections[section]));
        return alphaIndexer.get(sections[section]);
    }

    @Override
    public int getSectionForPosition(int position) {
        for (char c : alphaIndexer.keySet())
            if (alphaIndexer.get(c) == position) {
                for (int i = 0; i < sections.length; i++)
                    if (sections[i] == c)
                        return i;
            }
        return 0;
    }
}*/
