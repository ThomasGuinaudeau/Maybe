package com.maybe.maybe.fragments.settings;

public class Theme {
    private int themeResource, title;
    private String key;
    private ColorPreference colorPreference;
    private boolean isActive;

    public Theme(int themeResource, String key, int title, boolean isActive) {
        this.themeResource = themeResource;
        this.key = key;
        this.title = title;
        this.isActive = isActive;
    }

    public int getThemeResource() {
        return themeResource;
    }

    public void setThemeResource(int themeResource) {
        this.themeResource = themeResource;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public ColorPreference getColorPreference() {
        return colorPreference;
    }

    public void setColorPreference(ColorPreference colorPreference) {
        this.colorPreference = colorPreference;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
