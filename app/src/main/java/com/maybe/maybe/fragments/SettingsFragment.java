package com.maybe.maybe.fragments;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.maybe.maybe.ColorPickerDialog;
import com.maybe.maybe.ColorPreference;
import com.maybe.maybe.R;
import com.maybe.maybe.utils.ColorsConstants;

public class SettingsFragment extends PreferenceFragmentCompat implements ColorCallback {

    private PreferenceScreen screen;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        screen = getPreferenceManager().createPreferenceScreen(context);

        Preference resetColorsPreference = new Preference(context);
        resetColorsPreference.setKey(getResources().getString(R.string.pref_key_resetColors));
        resetColorsPreference.setTitle(R.string.pref_title_resetColors);
        resetColorsPreference.setSummary(R.string.pref_summary_resetColors);
        resetColorsPreference.setOnPreferenceClickListener(preference -> {
            ColorsConstants.resetColors(getContext());
            Toast.makeText(getContext(), R.string.toast_reset_colors, Toast.LENGTH_SHORT).show();
            return true;
        });
        screen.addPreference(resetColorsPreference);

        int[] categoryKeys = new int[]{ R.string.key_category_primary_color, R.string.key_category_secondary_color, R.string.key_category_notification_color, R.string.key_category_other_color };
        int[] categoryTitles = new int[]{ R.string.title_category_primary_color, R.string.title_category_secondary_color, R.string.title_category_notification_color, R.string.title_category_other_color };
        int[][] keys = new int[][]{
                { R.string.key_primary_color, R.string.key_primary_light_color, R.string.key_primary_dark_color, R.string.key_primary_text_color },
                { R.string.key_secondary_color, R.string.key_secondary_light_color, R.string.key_secondary_dark_color, R.string.key_secondary_text_color },
                { R.string.key_notification_background_color, R.string.key_notification_text_music_title_color, R.string.key_notification_text_music_artist_color },
                { R.string.key_background_color, R.string.key_selected_color, R.string.key_editmode_selected_color }
        };
        int[][] colors = new int[][]{
                { ColorsConstants.PRIMARY_COLOR, ColorsConstants.PRIMARY_LIGHT_COLOR, ColorsConstants.PRIMARY_DARK_COLOR, ColorsConstants.PRIMARY_TEXT_COLOR },
                { ColorsConstants.SECONDARY_COLOR, ColorsConstants.SECONDARY_LIGHT_COLOR, ColorsConstants.SECONDARY_DARK_COLOR, ColorsConstants.SECONDARY_TEXT_COLOR },
                { ColorsConstants.NOTIFICATION_BACKGROUND_COLOR, ColorsConstants.NOTIFICATION_TEXT_TITLE_COLOR, ColorsConstants.NOTIFICATION_TEXT_ARTIST_COLOR },
                { ColorsConstants.BACKGROUND_COLOR, ColorsConstants.SELECTED_COLOR, ColorsConstants.EDITMODE_SELECTED_COLOR }
        };
        int[][] titles = new int[][]{
                { R.string.title_color, R.string.title_light_color, R.string.title_dark_color, R.string.title_text_color },
                { R.string.title_color, R.string.title_light_color, R.string.title_dark_color, R.string.title_text_color },
                { R.string.title_background_color, R.string.title_notification_text_music_title_color, R.string.title_notification_text_music_artist_color },
                { R.string.title_background_color, R.string.title_selected_color, R.string.title_editmode_selected_color }
        };
        int[][] summaries = new int[][]{
                { R.string.summary_color, R.string.summary_light_color, R.string.summary_dark_color, R.string.summary_text_color },
                { R.string.summary_color, R.string.summary_light_color, R.string.summary_dark_color, R.string.summary_text_color },
                { R.string.summary_notification_background_color, R.string.summary_notification_text_music_title_color, R.string.summary_notification_text_music_artist_color },
                { R.string.summary_background_color, R.string.summary_selected_color, R.string.summary_editmode_selected_color }
        };

        for (int cat = 0; cat < categoryKeys.length; cat++) {
            final int jat = cat;
            PreferenceCategory preferenceCategory = new PreferenceCategory(getContext());
            preferenceCategory.setKey(getResources().getString(categoryKeys[cat]));
            preferenceCategory.setTitle(categoryTitles[cat]);
            screen.addPreference(preferenceCategory);

            for (int i = 0; i < keys[cat].length; i++) {
                final int j = i;
                ColorPreference colorPreference = new ColorPreference(context);
                colorPreference.setLayoutResource(R.layout.color_preference);
                colorPreference.setColor(colors[cat][i]);
                colorPreference.setKey(getResources().getString(keys[cat][i]));
                colorPreference.setTitle(getResources().getString(titles[cat][i]));
                colorPreference.setSummary(getResources().getString(summaries[cat][i]));
                colorPreference.setOnPreferenceClickListener(preference -> {
                    DialogFragment colorPickerDialog = new ColorPickerDialog(getResources().getString(keys[jat][j]), getResources().getString(titles[jat][j]), colors[jat][j], this);
                    colorPickerDialog.show(getParentFragmentManager(), getResources().getString(keys[jat][j]));
                    return true;
                });
                preferenceCategory.addPreference(colorPreference);
            }
        }

        setPreferenceScreen(screen);
    }

    @Override
    public void updateColor(String key, int color) {
        ColorPreference colorPreference = (ColorPreference) screen.findPreference(key);
        colorPreference.setColor(color);
        colorPreference.updateColor();
    }
}