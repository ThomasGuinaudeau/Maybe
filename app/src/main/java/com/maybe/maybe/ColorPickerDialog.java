package com.maybe.maybe;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.maybe.maybe.fragments.ColorCallback;

public class ColorPickerDialog extends DialogFragment {
    private final String key;
    private final String title;
    private int color;
    private final ColorCallback callback;

    public ColorPickerDialog(String key, String title, int color, ColorCallback callback) {
        super();
        this.key = key;
        this.title = title;
        this.color = color;
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_color_picker, null);

        LinearLayout layout = (LinearLayout) dialogView.findViewById(R.id.outer_linearlayout);

        String[] names = new String[]{
                getResources().getString(R.string.seek_bar_text_red),
                getResources().getString(R.string.seek_bar_text_green),
                getResources().getString(R.string.seek_bar_text_blue),
                getResources().getString(R.string.seek_bar_text_alpha)
        };
        int[] colors = new int[]{
                R.color.seek_bar_red,
                R.color.seek_bar_green,
                R.color.seek_bar_blue,
                R.color.seek_bar_alpha
        };
        int[] colorArray = new int[]{
                (int) (Color.valueOf(color).red() * 255),
                (int) (Color.valueOf(color).green() * 255),
                (int) (Color.valueOf(color).blue() * 255),
                (int) (Color.valueOf(color).alpha() * 255)
        };

        View colorPreview = (View) dialogView.findViewById(R.id.color_preview);

        for (int i = 0; i < names.length; i++) {
            final int j = i;
            LinearLayout singlePropLayout = new LinearLayout(getContext());
            singlePropLayout.setOrientation(LinearLayout.VERTICAL);
            singlePropLayout.setPadding(0, 10, 0, 10);

            TextView name = new TextView(getContext());
            name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            name.setText(names[i] + "\t\t" + colorArray[i]);
            name.setTextSize(14f);
            singlePropLayout.addView(name);

            SeekBar seekBar = new SeekBar(getContext());
            seekBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            seekBar.setMax(255);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    colorArray[j] = progress;
                    name.setText(names[j] + "\t\t" + colorArray[j]);
                    colorPreview.setBackgroundColor(Color.argb(colorArray[3], colorArray[0], colorArray[1], colorArray[2]));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    color = Color.argb(colorArray[3], colorArray[0], colorArray[1], colorArray[2]);
                }
            });
            seekBar.setProgress(colorArray[i]);
            seekBar.setThumbTintList(ColorStateList.valueOf(getResources().getInteger(colors[i])));
            seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getInteger(colors[i])));
            seekBar.setProgressTintList(ColorStateList.valueOf(getResources().getInteger(colors[i])));
            singlePropLayout.addView(seekBar);

            layout.addView(singlePropLayout);
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences sharedPref = getContext().getSharedPreferences(getContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(key, color);
                editor.apply();
                callback.updateColor(key, color);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        return builder.create();
    }
}