package com.maybe.maybe.fragments.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.maybe.maybe.R;

public class ArtistFragment extends PreferenceFragmentCompat {
    private final IArtist artistCallback;
    private int currentChoice;
    private RadioButtonPreference artist, album, artistAlbum;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        ((TextView) getActivity().findViewById(R.id.settings_title)).setText(R.string.artist_view_title);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        currentChoice = sharedPreferences.getInt(getString(R.string.artist_view), 0);

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference text = new Preference(context);
        text.setKey(getString(R.string.key_summary_artist));
        text.setSummary(getString(R.string.artist_summary));
        screen.addPreference(text);

        artist = new RadioButtonPreference(context);
        artist.setKey(getString(R.string.key_artist_pref));
        artist.setTitle(getString(R.string.artist_pref_title));
        artist.setChecked(currentChoice == 0);
        artist.setOnPreferenceClickListener(preference -> {
            currentChoice = 0;
            updateArtistView();
            return true;
        });
        screen.addPreference(artist);

        album = new RadioButtonPreference(context);
        album.setKey(getString(R.string.key_album_pref));
        album.setTitle(getString(R.string.album_pref_title));
        album.setChecked(currentChoice == 1);
        album.setOnPreferenceClickListener(preference -> {
            currentChoice = 1;
            updateArtistView();
            return true;
        });
        screen.addPreference(album);

        artistAlbum = new RadioButtonPreference(context);
        artistAlbum.setKey(getString(R.string.key_artist_album_pref));
        artistAlbum.setTitle(getString(R.string.artist_album_pref_title));
        artistAlbum.setChecked(currentChoice == 2);
        artistAlbum.setOnPreferenceClickListener(preference -> {
            currentChoice = 2;
            updateArtistView();
            return true;
        });
        screen.addPreference(artistAlbum);

        setPreferenceScreen(screen);
    }

    public ArtistFragment(IArtist artistCallback) {
        this.artistCallback = artistCallback;
    }

    public void updateArtistView() {
        artist.setChecked(currentChoice == 0);
        album.setChecked(currentChoice == 1);
        artistAlbum.setChecked(currentChoice == 2);
        artistCallback.setArtistPref(currentChoice);
    }
}
