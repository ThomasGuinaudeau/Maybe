package com.maybe.maybe.database.async_tasks.settings;

import android.os.AsyncTask;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.SettingsDao;
import com.maybe.maybe.database.entity.Settings;

public class SettingsAsyncTaskNull extends AsyncTask<Object, Object, Void> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Object... objects) {
        //PARAMS 0=database 1=query
        SettingsDao dao = ((AppDatabase) objects[0]).settingsDao();
        String query = (String) objects[1];
        if (query.equals("insert"))//2=musidIds
            dao.insert((Settings) objects[2]);
        else if (query.equals("deleteAll"))
            dao.deleteAll();
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
    }

}
