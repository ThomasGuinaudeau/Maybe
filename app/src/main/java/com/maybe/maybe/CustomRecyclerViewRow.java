package com.maybe.maybe;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.utils.ColorsConstants;

public class CustomRecyclerViewRow extends ConstraintLayout {

    private final TextView track;
    private final TextView title;
    private final TextView artist;

    public CustomRecyclerViewRow(Context context) {
        this(context, null);
    }

    public CustomRecyclerViewRow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomRecyclerViewRow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomRecyclerViewRow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflate(context, R.layout.main_recycler_view_row, this);
        track = (TextView) findViewById(R.id.main_item_track);
        title = (TextView) findViewById(R.id.main_item_title);
        artist = (TextView) findViewById(R.id.main_item_artist);
    }

    public void setMusicWithArtists(MusicWithArtists musicWithArtists) {
        track.setTextColor(ColorsConstants.SECONDARY_TEXT_COLOR);//was expandableparent
        track.setText("" + musicWithArtists.music.getMusic_track());
        title.setContentDescription(musicWithArtists.music.getMusic_title());
        title.setTextColor(ColorsConstants.PRIMARY_TEXT_COLOR);//was expandableparent
        title.setText(musicWithArtists.music.getMusic_title());
        artist.setContentDescription("Artist " + musicWithArtists.artistsToString());
        artist.setTextColor(ColorsConstants.SECONDARY_TEXT_COLOR);//was expandableparent
        artist.setText(musicWithArtists.artistsToString());
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.odd));
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected)
            setBackgroundColor(ColorsConstants.SELECTED_COLOR);//was primaryColorTrans : secondaryColorTrans
    }

    public void setEven(boolean isEven) {
        if (isEven)
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.even));
    }
}