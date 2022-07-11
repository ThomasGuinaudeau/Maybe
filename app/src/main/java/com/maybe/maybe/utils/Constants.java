package com.maybe.maybe.utils;

public class Constants {
    public static final String SORT_ALPHA = "sort_alpha";
    public static final String SORT_RANDOM = "sort_random";
    public static final String SORT_NUM = "sort_num";
    public static final String REPEAT_ALL = "repeat_all";
    public static final String REPEAT_ONE = "repeat_one";
    public static final String REPEAT_NONE = "repeat_none";
    public static final String STATE_PLAY = "state_play";
    public static final String STATE_PAUSE = "state_pause";
    public static final String BROADCAST_DESTINATION = "destination";
    public static final String BROADCAST_EXTRAS = "extras";
    public static final String tab00c0 = "AAAAAAACEEEEIIII" + "DNOOOOO\u00d7\u00d8UUUUYI\u00df" + "aaaaaaaceeeeiiii" + "\u00f0nooooo\u00f7\u00f8uuuuy\u00fey" + "AaAaAaCcCcCcCcDd" + "DdEeEeEeEeEeGgGg" + "GgGgHhHhIiIiIiIi" + "IiJjJjKkkLlLlLlL" + "lLlNnNnNnnNnOoOo" + "OoOoRrRrRrSsSsSs" + "SsTtTtTtUuUuUuUu" + "UuUuWwYyYZzZzZzF";
    private static final String PACKAGE_NAME = "com.maybe.maybe";
    public static final String ACTION_CREATE_SERVICE = PACKAGE_NAME + ".ACTION_CREATE_SERVICE";
    public static final String ACTION_END_SERVICE = PACKAGE_NAME + ".ACTION_END_SERVICE";
    public static final String ACTION_TO_ACTIVITY = PACKAGE_NAME + ".ACTION_TO_ACTIVITY";
    public static final String ACTION_TO_SERVICE = PACKAGE_NAME + ".ACTION_TO_SERVICE";
    public static final String ACTION_PREVIOUS = PACKAGE_NAME + ".ACTION_PREVIOUS";
    public static final String ACTION_NEXT = PACKAGE_NAME + ".ACTION_NEXT";
    public static final String ACTION_PLAY_PAUSE = PACKAGE_NAME + ".ACTION_PLAY_PAUSE";
    public static final String ACTION_UPDATE_COLORS = PACKAGE_NAME + ".ACTION_UPDATE_COLORS";
    public static final String ACTION_APP_FOREGROUND = PACKAGE_NAME + ".ACTION_APP_FOREGROUND";
    public static final String ACTION_APP_BACKGROUND = PACKAGE_NAME + ".ACTION_APP_BACKGROUND";

    public static String removeDiacritic(char c) {
        if (c >= '\u00c0' && c <= '\u017f') {
            c = tab00c0.charAt((int) c - '\u00c0');
        }
        return new String(new char[]{ c });
    }
}
