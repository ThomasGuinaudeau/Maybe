package com.maybe.maybe.database.async_tasks.settings;

import com.maybe.maybe.database.entity.Settings;

import java.util.List;

public interface SettingsAsyncTaskSettingsResponse {
    void onSettingsAsyncTaskSettingsFinish(List<Settings> settings);
}
