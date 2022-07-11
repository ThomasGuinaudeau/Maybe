package com.maybe.maybe.database.async_tasks;

import android.os.AsyncTask;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.ArtistDao;

import java.util.ArrayList;
import java.util.List;

public class ArtistAsyncTask extends AsyncTask<Object, Object, List<Object>> {

    private OnArtistAsyncTaskFinish callback;
    private String query;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<Object> doInBackground(Object... objects) {
        //PARAMS 0=callback 1=database 2=query
        List<Object> list = new ArrayList<Object>();
        callback = (OnArtistAsyncTaskFinish) objects[0];
        ArtistDao dao = ((AppDatabase) objects[1]).artistDao();
        query = (String) objects[2];

        if (query.equals("selectAllArtistWithMusics"))//3=sort
            list = (List<Object>) (Object) dao.selectAllArtistWithMusics();
        else if (query.equals("selectOneArtistWithMusics"))//3=sort 4=artistName
            list.add((Object) dao.selectOneArtistWithMusics((String) objects[3], (String) objects[4]));

        return list;
    }

    @Override
    protected void onPostExecute(List<Object> list) {
        super.onPostExecute(list);
        callback.onSelectArtistAsyncFinish(list);
    }

}