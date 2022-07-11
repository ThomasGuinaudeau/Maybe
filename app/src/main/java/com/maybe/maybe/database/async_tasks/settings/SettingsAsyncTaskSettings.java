package com.maybe.maybe.database.async_tasks.settings;

import android.os.AsyncTask;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.SettingsDao;
import com.maybe.maybe.database.entity.Settings;

import java.util.List;

public class SettingsAsyncTaskSettings extends AsyncTask<Object, Object, List<Settings>> {

    private SettingsAsyncTaskSettingsResponse callback;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Settings> doInBackground(Object... objects) {
        //PARAMS 0=callback 1=database 2=query
        List<Settings> settings = null;
        callback = (SettingsAsyncTaskSettingsResponse) objects[0];
        SettingsDao dao = ((AppDatabase) objects[1]).settingsDao();
        String query = (String) objects[2];
        if (query.equals("selectAll"))
            settings = dao.selectAll();
        return settings;
    }

    @Override
    protected void onPostExecute(List<Settings> settings) {
        super.onPostExecute(settings);
        callback.onSettingsAsyncTaskSettingsFinish(settings);
    }

}
