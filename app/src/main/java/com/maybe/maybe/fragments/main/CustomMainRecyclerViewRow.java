package com.maybe.maybe.fragments.main;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.maybe.maybe.R;
import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.Locale;

public class CustomMainRecyclerViewRow extends ConstraintLayout {

    private final TextView track;
    private final TextView title;
    private final TextView artist;

    public CustomMainRecyclerViewRow(Context context) {
        this(context, null);
    }

    public CustomMainRecyclerViewRow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomMainRecyclerViewRow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomMainRecyclerViewRow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflate(context, R.layout.main_recycler_view_row, this);
        track = (TextView) findViewById(R.id.main_item_track);
        title = (TextView) findViewById(R.id.main_item_title);
        artist = (TextView) findViewById(R.id.main_item_artist);
    }

    public void setMusicWithArtists(MusicWithArtists musicWithArtists) {
        long second = (musicWithArtists.music.getMusic_duration() / 1000) % 60;
        long minute = (musicWithArtists.music.getMusic_duration() / (1000 * 60)) % 60;
        track.setText(getContext().getString(R.string.recycler_row_duration, minute, String.format(Locale.getDefault(), "%02d", second)));
        title.setContentDescription(musicWithArtists.music.getMusic_title());
        title.setText(musicWithArtists.music.getMusic_title());
        artist.setContentDescription("Artist " + musicWithArtists.artistsToString());
        artist.setText(musicWithArtists.artistsToString());
        //setBackgroundColor(ContextCompat.getColor(getContext(), R.color.odd));
        setBackgroundColor(MaterialColors.getColor(getContext(), android.R.attr.colorBackground, 0x00000000));
        track.setTextColor(MaterialColors.getColor(getContext(), R.attr.textColorFaded, 0x00000000));
        title.setTextColor(MaterialColors.getColor(getContext(), android.R.attr.textColor, 0x00000000));
        artist.setTextColor(MaterialColors.getColor(getContext(), R.attr.textColorFaded, 0x00000000));
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            setBackgroundColor(MaterialColors.getColor(getContext(), R.attr.selectedMusicBackground, 0xFFFFFFFF));
            track.setTextColor(MaterialColors.getColor(getContext(), R.attr.selectedMusicArtist, 0xFF000000));
            title.setTextColor(MaterialColors.getColor(getContext(), R.attr.selectedMusicTitle, 0xFF000000));
            artist.setTextColor(MaterialColors.getColor(getContext(), R.attr.selectedMusicArtist, 0xFF000000));
        }
    }

    public void setEven(boolean isEven) {
        if (isEven)
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.even));
    }

    public void setFoundColor(boolean isFound) {
        if (isFound)
            setBackgroundColor(MaterialColors.getColor(getContext(), android.R.attr.colorPrimary, 0x00000000));
    }
}