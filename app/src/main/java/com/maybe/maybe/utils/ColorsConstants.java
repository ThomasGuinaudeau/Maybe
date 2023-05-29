package com.maybe.maybe.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.maybe.maybe.R;

public class ColorsConstants {
    public static int PRIMARY_COLOR;
    public static int PRIMARY_LIGHT_COLOR;
    public static int PRIMARY_DARK_COLOR;
    public static int PRIMARY_TEXT_COLOR;
    public static int SECONDARY_COLOR;
    public static int SECONDARY_LIGHT_COLOR;
    public static int SECONDARY_DARK_COLOR;
    public static int SECONDARY_TEXT_COLOR;
    public static int NOTIFICATION_BACKGROUND_COLOR;
    public static int NOTIFICATION_TEXT_TITLE_COLOR;
    public static int NOTIFICATION_TEXT_ARTIST_COLOR;
    public static int BACKGROUND_COLOR;
    public static int SELECTED_COLOR;

    public static final int DEFAULT_PRIMARY_COLOR = Color.parseColor("#207667");
    public static final int DEFAULT_PRIMARY_LIGHT_COLOR = Color.parseColor("#54a595");
    public static final int DEFAULT_PRIMARY_DARK_COLOR = Color.parseColor("#004a3d");
    public static final int DEFAULT_PRIMARY_TEXT_COLOR = Color.parseColor("#FFFFFF");
    public static final int DEFAULT_SECONDARY_COLOR = Color.parseColor("#76202e");
    public static final int DEFAULT_SECONDARY_LIGHT_COLOR = Color.parseColor("#a94e57");
    public static final int DEFAULT_SECONDARY_DARK_COLOR = Color.parseColor("#450004");
    public static final int DEFAULT_SECONDARY_TEXT_COLOR = Color.parseColor("#BBBBBB");
    public static final int DEFAULT_NOTIFICATION_BACKGROUND_COLOR = DEFAULT_SECONDARY_DARK_COLOR;
    public static final int DEFAULT_NOTIFICATION_TEXT_TITLE_COLOR = DEFAULT_PRIMARY_TEXT_COLOR;
    public static final int DEFAULT_NOTIFICATION_TEXT_ARTIST_COLOR = DEFAULT_SECONDARY_TEXT_COLOR;
    public static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#2a2828");
    public static final int DEFAULT_SELECTED_COLOR = Color.parseColor("#8876202e");

    public static void loadColors(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        PRIMARY_COLOR = sharedPref.getInt(context.getString(R.string.key_primary_color), DEFAULT_PRIMARY_COLOR);
        PRIMARY_LIGHT_COLOR = sharedPref.getInt(context.getString(R.string.key_primary_light_color), DEFAULT_PRIMARY_LIGHT_COLOR);
        PRIMARY_DARK_COLOR = sharedPref.getInt(context.getString(R.string.key_primary_dark_color), DEFAULT_PRIMARY_DARK_COLOR);
        PRIMARY_TEXT_COLOR = sharedPref.getInt(context.getString(R.string.key_primary_text_color), DEFAULT_PRIMARY_TEXT_COLOR);
        SECONDARY_COLOR = sharedPref.getInt(context.getString(R.string.key_secondary_color), DEFAULT_SECONDARY_COLOR);
        SECONDARY_LIGHT_COLOR = sharedPref.getInt(context.getString(R.string.key_secondary_light_color), DEFAULT_SECONDARY_LIGHT_COLOR);
        SECONDARY_DARK_COLOR = sharedPref.getInt(context.getString(R.string.key_secondary_dark_color), DEFAULT_SECONDARY_DARK_COLOR);
        SECONDARY_TEXT_COLOR = sharedPref.getInt(context.getString(R.string.key_secondary_text_color), DEFAULT_SECONDARY_TEXT_COLOR);
        NOTIFICATION_BACKGROUND_COLOR = sharedPref.getInt(context.getString(R.string.key_notification_background_color), DEFAULT_NOTIFICATION_BACKGROUND_COLOR);
        NOTIFICATION_TEXT_TITLE_COLOR = sharedPref.getInt(context.getString(R.string.key_notification_text_music_title_color), DEFAULT_NOTIFICATION_TEXT_TITLE_COLOR);
        NOTIFICATION_TEXT_ARTIST_COLOR = sharedPref.getInt(context.getString(R.string.key_notification_text_music_artist_color), DEFAULT_NOTIFICATION_TEXT_ARTIST_COLOR);
        BACKGROUND_COLOR = sharedPref.getInt(context.getString(R.string.key_background_color), DEFAULT_BACKGROUND_COLOR);
        SELECTED_COLOR = sharedPref.getInt(context.getString(R.string.key_selected_color), DEFAULT_SELECTED_COLOR);
    }

    public static void resetColors(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.key_primary_color), DEFAULT_PRIMARY_COLOR);
        editor.putInt(context.getString(R.string.key_primary_light_color), DEFAULT_PRIMARY_LIGHT_COLOR);
        editor.putInt(context.getString(R.string.key_primary_dark_color), DEFAULT_PRIMARY_DARK_COLOR);
        editor.putInt(context.getString(R.string.key_primary_text_color), DEFAULT_PRIMARY_TEXT_COLOR);
        editor.putInt(context.getString(R.string.key_secondary_color), DEFAULT_SECONDARY_COLOR);
        editor.putInt(context.getString(R.string.key_secondary_light_color), DEFAULT_SECONDARY_LIGHT_COLOR);
        editor.putInt(context.getString(R.string.key_secondary_dark_color), DEFAULT_SECONDARY_DARK_COLOR);
        editor.putInt(context.getString(R.string.key_secondary_text_color), DEFAULT_SECONDARY_TEXT_COLOR);
        editor.putInt(context.getString(R.string.key_notification_background_color), DEFAULT_NOTIFICATION_BACKGROUND_COLOR);
        editor.putInt(context.getString(R.string.key_notification_text_music_title_color), DEFAULT_NOTIFICATION_TEXT_TITLE_COLOR);
        editor.putInt(context.getString(R.string.key_notification_text_music_artist_color), DEFAULT_NOTIFICATION_TEXT_ARTIST_COLOR);
        editor.putInt(context.getString(R.string.key_background_color), DEFAULT_BACKGROUND_COLOR);
        editor.putInt(context.getString(R.string.key_selected_color), DEFAULT_SELECTED_COLOR);
        editor.apply();
    }
}