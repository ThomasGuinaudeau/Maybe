package com.maybe.maybe.fragments.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.maybe.maybe.R;
import com.maybe.maybe.utils.CustomButton;

import java.util.ArrayList;

public class ThemeFragment extends PreferenceFragmentCompat {
    private CustomButton restartButton;
    private ITheme themeCallback;
    private ArrayList<Theme> themeList;
    private int currentTheme;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        ((TextView) getActivity().findViewById(R.id.settings_title)).setText(R.string.themes_title);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        themeList = new ArrayList<>();

        themeList.add(new Theme(R.style.Light_Pink, getString(R.string.key_light_theme_pink), R.string.light_theme_pink_title, false, true));
        themeList.add(new Theme(R.style.Light_Blue, getString(R.string.key_light_theme_blue), R.string.light_theme_blue_title, false, true));
        themeList.add(new Theme(R.style.Light_Green, getString(R.string.key_light_theme_green), R.string.light_theme_green_title, false, true));
        themeList.add(new Theme(R.style.Light_Yellow, getString(R.string.key_light_theme_yellow), R.string.light_theme_yellow_title, false, true));
        themeList.add(new Theme(R.style.Dark_Pink, getString(R.string.key_dark_theme_pink), R.string.dark_theme_pink_title, false, false));
        themeList.add(new Theme(R.style.Dark_Blue, getString(R.string.key_dark_theme_blue), R.string.dark_theme_blue_title, false, false));
        themeList.add(new Theme(R.style.Dark_Green, getString(R.string.key_dark_theme_green), R.string.dark_theme_green_title, false, false));
        themeList.add(new Theme(R.style.Dark_Yellow, getString(R.string.key_dark_theme_yellow), R.string.dark_theme_yellow_title, false, false));

        themeList.add(new Theme(R.style.Light_Default, getString(R.string.key_light_theme_default), R.string.light_theme_title, false, true));
        themeList.add(new Theme(R.style.Dark_Default, getString(R.string.key_dark_theme_default), R.string.dark_theme_title, false, false));
        themeList.add(new Theme(R.style.Light_Sunset, getString(R.string.key_light_theme_sunset), R.string.light_theme_blue2_title, false, true));
        themeList.add(new Theme(R.style.Dark_Sunset, getString(R.string.key_dark_theme_sunset), R.string.dark_theme_blue2_title, false, false));
        themeList.add(new Theme(R.style.Light_Purple, getString(R.string.key_light_theme_purple), R.string.light_theme_purple_title, false, true));
        themeList.add(new Theme(R.style.Dark_Purple, getString(R.string.key_dark_theme_purple), R.string.dark_theme_purple_title, false, false));
        themeList.add(new Theme(R.style.Light_Leather_1, getString(R.string.key_light_theme_leather_1), R.string.light_theme_leather_1_title, false, true));
        themeList.add(new Theme(R.style.Light_Leather_2, getString(R.string.key_light_theme_leather_2), R.string.light_theme_leather_2_title, false, true));
        themeList.add(new Theme(R.style.Dark_Leather_1, getString(R.string.key_dark_theme_leather_1), R.string.dark_theme_leather_1_title, false, false));
        themeList.add(new Theme(R.style.Dark_Leather_2, getString(R.string.key_dark_theme_leather_2), R.string.dark_theme_leather_2_title, false, false));
        themeList.add(new Theme(R.style.Light_Mountain_1, getString(R.string.key_light_theme_mountain_1), R.string.light_theme_mountain_1_title, false, true));
        themeList.add(new Theme(R.style.Light_Mountain_2, getString(R.string.key_light_theme_mountain_2), R.string.light_theme_mountain_2_title, false, true));
        themeList.add(new Theme(R.style.Dark_Mountain_1, getString(R.string.key_dark_theme_mountain_1), R.string.dark_theme_mountain_1_title, false, false));
        themeList.add(new Theme(R.style.Dark_Mountain_2, getString(R.string.key_dark_theme_mountain_2), R.string.dark_theme_mountain_2_title, false, false));

        themeList.add(new Theme(R.style.Light_Terracotta_1, getString(R.string.key_light_theme_terracotta_1), R.string.light_theme_terracotta_1_title, false, true));
        themeList.add(new Theme(R.style.Light_Terracotta_2, getString(R.string.key_light_theme_terracotta_2), R.string.light_theme_terracotta_2_title, false, true));
        themeList.add(new Theme(R.style.Dark_Terracotta_1, getString(R.string.key_dark_theme_terracotta_1), R.string.dark_theme_terracotta_1_title, false, false));
        themeList.add(new Theme(R.style.Dark_Terracotta_2, getString(R.string.key_dark_theme_terracotta_2), R.string.dark_theme_terracotta_2_title, false, false));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int themeResource = sharedPreferences.getInt(getString(R.string.key_theme), themeList.get(9).getThemeResource());
        currentTheme = themeResource;
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
            screen.addPreference(colorPreference);
            theme.setColorPreference(colorPreference);
        });

        setPreferenceScreen(screen);
    }

    public ThemeFragment(CustomButton restartButton, ITheme themeCallback) {
        this.restartButton = restartButton;
        this.themeCallback = themeCallback;
    }

    private ColorPreference newColorPreference(Context context, Theme theme, int[] colors) {
        ColorPreference colorPreference = new ColorPreference(context);
        colorPreference.setLayoutResource(R.layout.color_preference);
        colorPreference.setColors(colors);
        colorPreference.setKey(theme.getKey());
        colorPreference.setTitle(theme.getTitle());
        colorPreference.setSelected(theme.isActive());
        colorPreference.setOnPreferenceClickListener(preference -> {
            themeCallback.setTheme(theme);
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
                if (themeResource != currentTheme)
                    restartButton.setVisibility(View.VISIBLE);
                else
                    restartButton.setVisibility(View.GONE);
            } else if (theme.isActive()) {
                theme.setActive(false);
                theme.getColorPreference().setSelected(false);
            }
        });
    }
}
