package com.maybe.maybe.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.maybe.maybe.R;

public class CustomButton extends LinearLayout {
    private View view;
    private ImageView icon;
    private TextView textView;

    public CustomButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CustomButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomButton, defStyleAttr, 0);
        String text = a.getString(R.styleable.CustomButton_text);
        boolean hasIcon = a.getBoolean(R.styleable.CustomButton_hasIcon, false);
        int src = a.getResourceId(R.styleable.CustomButton_src, -1);

        int textColor = Color.BLACK;
        TypedValue typedValue = new TypedValue();
        boolean valueExists = a.getValue(R.styleable.CustomButton_textColor, typedValue);
        if (valueExists && typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            textColor = typedValue.data;
        }

        int background = Color.BLACK;
        TypedValue typedValue2 = new TypedValue();
        boolean valueExists2 = a.getValue(R.styleable.CustomButton_background, typedValue2);
        if (valueExists2 && typedValue2.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue2.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            background = typedValue2.data;
        }
        a.recycle();

        view = LayoutInflater.from(context).inflate(R.layout.custom_button, this, true);
        view.setContentDescription(text);
        view.setBackgroundResource(R.drawable.ripple);
        view.setBackgroundTintList(ColorStateList.valueOf(background));

        icon = view.findViewById(R.id.custom_button_icon);
        if (hasIcon) {
            icon.setImageResource(src);
            icon.setImageTintList(ColorStateList.valueOf(textColor));
        } else {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 0);
            icon.setLayoutParams(layoutParams);
        }

        textView = view.findViewById(R.id.custom_button_text);
        textView.setText(text);
        textView.setTextColor(textColor);
    }

    public void setIcon(@DrawableRes int resId) {
        icon.setImageResource(resId);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled)
            view.setAlpha(0.50f);
    }
}
