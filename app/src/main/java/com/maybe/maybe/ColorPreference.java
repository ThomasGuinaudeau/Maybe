package com.maybe.maybe;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ColorPreference extends Preference {
    private View view;
    private int color;

    public void setColor(int color) {
        this.color = color;
    }

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

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(true);

        view = (View) holder.findViewById(R.id.color_square);
        updateColor();
    }

    public void updateColor() {
        int[][] states = new int[][]{
                new int[]{ android.R.attr.state_enabled }
        };
        int[] colors = new int[]{ color };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        view.setBackgroundTintList(colorStateList);
    }
}
