package com.maybe.maybe;

public class CategoryItem {
    public static final int CATEGORY_PLAYLIST = 0;
    public static final int CATEGORY_ARTIST = 1;
    public static final int CATEGORY_ALBUM = 2;
    public static final int CATEGORY_FOLDER = 3;
    public static final int CATEGORY_SETTING = 4;
    public static final int CATEGORY_SYNC = 5;
    private final int id, icon;
    private final String name;

    public CategoryItem(int id, String name, int icon) {
        this.id = id;
        this.icon = icon;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }
}
