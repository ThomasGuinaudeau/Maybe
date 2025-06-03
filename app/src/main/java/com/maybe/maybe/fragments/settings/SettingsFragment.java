package com.maybe.maybe.fragments.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.maybe.maybe.R;
import com.maybe.maybe.utils.CustomButton;

public class SettingsFragment extends PreferenceFragmentCompat implements ITheme, IArtist {
    private CustomButton restartButton;
    private ITheme themeCallback;
    private IArtist artistCallback;

    public SettingsFragment(ITheme themeCallback, IArtist artistCallback) {
        this.themeCallback = themeCallback;
        this.artistCallback = artistCallback;
    }

    public void setRestartButton(CustomButton restartButton) {
        this.restartButton = restartButton;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference themePreference = new Preference(context);
        themePreference.setKey(getString(R.string.key_category_theme));
        themePreference.setTitle(R.string.themes_title);
        themePreference.setOnPreferenceClickListener(preference -> {
            ThemeFragment themeFragment = new ThemeFragment(restartButton, SettingsFragment.this);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, themeFragment)
                    .addToBackStack("theme_settings")
                    .commit();
            return true;
        });
        screen.addPreference(themePreference);

        Preference artistPreference = new Preference(context);
        artistPreference.setKey(getString(R.string.key_category_artist));
        artistPreference.setTitle(R.string.artist_view_title);
        artistPreference.setOnPreferenceClickListener(preference -> {
            ArtistFragment artistFragment = new ArtistFragment(SettingsFragment.this);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, artistFragment)
                    .addToBackStack("artist_settings")
                    .commit();
            return true;
        });
        screen.addPreference(artistPreference);
        setPreferenceScreen(screen);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        ((TextView) getActivity().findViewById(R.id.settings_title)).setText(R.string.settings_title);
        restartButton.setVisibility(View.GONE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setTheme(Theme theme) {
        themeCallback.setTheme(theme);
    }

    @Override
    public void setArtistPref(int artistView) {
        artistCallback.setArtistPref(artistView);
    }
}