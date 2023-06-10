package com.maybe.maybe.fragments.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.maybe.maybe.ColorPreference;
import com.maybe.maybe.R;
import com.maybe.maybe.utils.Constants;

import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragmentCompat {
    private ArrayList<Theme> themeList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        PreferenceCategory preferenceCategory = new PreferenceCategory(context);
        preferenceCategory.setKey(getString(R.string.key_category_theme));
        preferenceCategory.setTitle(R.string.category_theme_title);
        preferenceCategory.setSummary(R.string.category_theme_subtitle);
        screen.addPreference(preferenceCategory);

        themeList = new ArrayList<>();
        themeList.add(new Theme(R.style.AppTheme_Dark_Default, getString(R.string.key_dark_theme), R.string.dark_theme_title, false));
        themeList.add(new Theme(R.style.AppTheme_Light_Default, getString(R.string.key_light_theme), R.string.light_theme_title, false));
        themeList.add(new Theme(R.style.AppTheme_Dark_Blue, getString(R.string.key_dark_theme_blue), R.string.dark_theme_blue_title, false));
        themeList.add(new Theme(R.style.AppTheme_Light_Blue, getString(R.string.key_light_theme_blue), R.string.light_theme_blue_title, false));
        themeList.add(new Theme(R.style.AppTheme_Dark_Purple, getString(R.string.key_dark_theme_purple), R.string.dark_theme_purple_title, false));
        themeList.add(new Theme(R.style.AppTheme_Light_Purple, getString(R.string.key_light_theme_purple), R.string.light_theme_purple_title, false));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int themeResource = sharedPreferences.getInt(getString(R.string.key_theme), themeList.get(0).getThemeResource());
        themeList.forEach(theme -> {
            if (themeResource == theme.getThemeResource()) theme.setActive(true);
        });

        themeList.forEach(theme -> {
            int[] attributeArray = {
                    R.attr.colorPrimary,
                    R.attr.colorSecondary,
                    android.R.attr.colorBackground
            };
            TypedArray a = getActivity().getTheme().obtainStyledAttributes(theme.getThemeResource(), attributeArray);
            int[] themeColors = {
                    a.getColor(0, 0),
                    a.getColor(1, 0),
                    a.getColor(2, 0)
            };
            a.recycle();
            ColorPreference colorPreference = newColorPreference(context, theme, themeColors);
            preferenceCategory.addPreference(colorPreference);
            theme.setColorPreference(colorPreference);
        });

        setPreferenceScreen(screen);
    }

    private ColorPreference newColorPreference(Context context, Theme theme, int[] colors) {
        ColorPreference colorPreference = new ColorPreference(context);
        colorPreference.setLayoutResource(R.layout.color_preference);
        colorPreference.setColors(colors);
        colorPreference.setKey(theme.getKey());
        colorPreference.setTitle(theme.getTitle());
        colorPreference.setSelected(theme.isActive());
        colorPreference.setOnPreferenceClickListener(preference -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putInt(getString(R.string.key_theme), theme.getThemeResource()).apply();
            updateThemes(theme.getThemeResource());
            return true;
        });
        return colorPreference;
    }

    public void updateThemes(int themeResource) {
        themeList.forEach(theme -> {
            if (themeResource == theme.getThemeResource()) {
                theme.setActive(true);
                theme.getColorPreference().setSelected(true);
            } else if (theme.isActive()) {
                theme.setActive(false);
                theme.getColorPreference().setSelected(false);
            }
        });
    }
}