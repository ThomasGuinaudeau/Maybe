package com.maybe.maybe.fragments.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.maybe.maybe.R;

public class SettingsFragment extends PreferenceFragmentCompat implements ITheme {
    private LinearLayout restartButton;
    private ITheme themeCallback;

    public SettingsFragment(ITheme themeCallback) {
        this.themeCallback = themeCallback;
    }

    public void setRestartButton(LinearLayout restartButton) {
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
        themePreference.setTitle(R.string.category_theme_title);
        themePreference.setSummary(R.string.category_theme_subtitle);
        themePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                ThemeFragment themeFragment = new ThemeFragment(restartButton, SettingsFragment.this);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings_container, themeFragment)
                        .addToBackStack("theme_settings")
                        .commit();
                return true;
            }
        });
        screen.addPreference(themePreference);
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
}