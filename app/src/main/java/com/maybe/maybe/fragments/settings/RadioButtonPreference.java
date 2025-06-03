package com.maybe.maybe.fragments.settings;

import android.content.Context;
import android.widget.RadioButton;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;

import com.maybe.maybe.R;

public class RadioButtonPreference extends CheckBoxPreference {
    private boolean isChecked = false;

    public RadioButtonPreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.radio_preference);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        RadioButton radioButton = (RadioButton) holder.findViewById(R.id.radio_button);
        radioButton.setChecked(isChecked);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        isChecked = checked;
    }
}
