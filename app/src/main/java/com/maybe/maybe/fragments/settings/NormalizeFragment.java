package com.maybe.maybe.fragments.settings;

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
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.maybe.maybe.R;

public class NormalizeFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        ((TextView) getActivity().findViewById(R.id.settings_title)).setText(R.string.normalization_title);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();

        SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean hasNormalization = sharedPreferences.getBoolean(getString(R.string.has_normalization), true);

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        Preference text = new Preference(context);
        text.setKey(getString(R.string.key_normalization_question1));
        text.setTitle(getString(R.string.normalization_question1_title));
        text.setSummary(getString(R.string.normalization_question1_summary));
        screen.addPreference(text);

        Preference text2 = new Preference(context);
        text2.setKey(getString(R.string.key_normalization_question2));
        text2.setTitle(getString(R.string.normalization_question2_title));
        text2.setSummary(getString(R.string.normalization_question2_summary));
        screen.addPreference(text2);

        SwitchPreferenceCompat normalization = new SwitchPreferenceCompat(context);
        normalization.setKey(getString(R.string.key_normalization_pref));
        normalization.setTitle(getString(R.string.normalization_pref_title));
        normalization.setChecked(hasNormalization);
        normalization.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit()
                    .putBoolean(NormalizeFragment.this.getString(R.string.has_normalization), (boolean) newValue)
                    .apply();
            return true;
        });
        screen.addPreference(normalization);

        setPreferenceScreen(screen);
    }
}
