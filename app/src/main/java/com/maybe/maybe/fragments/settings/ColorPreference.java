package com.maybe.maybe.fragments.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;

import com.maybe.maybe.R;

public class ColorPreference extends CheckBoxPreference {
    private int[] colors;
    private RadioButton radioButton;
    private boolean isSelected;

    public ColorPreference(Context context) {
        this(context, null);
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        //holder.setDividerAllowedAbove(false);
        //holder.setDividerAllowedBelow(true);
        View colorViewPrimary = (View) holder.findViewById(R.id.color_square_primary);
        colorViewPrimary.setBackgroundTintList(getColorStateList(colors[0]));
        View colorViewSecondary = (View) holder.findViewById(R.id.color_square_secondary);
        colorViewSecondary.setBackgroundTintList(getColorStateList(colors[1]));
        View colorViewBackground = (View) holder.findViewById(R.id.color_square_background);
        colorViewBackground.setBackgroundTintList(getColorStateList(colors[2]));

        radioButton = (RadioButton) holder.findViewById(R.id.color_radio_button);
        radioButton.setChecked(isSelected);
        //updateColor();
    }

    public ColorStateList getColorStateList(int color) {
        int[][] states = new int[][]{
                new int[]{ android.R.attr.state_enabled }
        };
        int[] colors = new int[]{ color };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        return colorStateList;
    }
}
