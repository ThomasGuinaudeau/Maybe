package com.maybe.maybe.database.async_tasks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.entity.Artist;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicArtistCrossRef;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FillDbAsyncTask extends AsyncTask<Object, Integer, Object> {

    private final WeakReference<Context> context;
    private OnFillDbAsyncTaskFinish callback;
    private AlertDialog dialog;
    private int id;
    private boolean maxSet = false;

    public FillDbAsyncTask(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        Log.d("update", "start");
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(context.get());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(context.get(), null, android.R.attr.progressBarStyleHorizontal);
        id = View.generateViewId();
        progressBar.setId(id);
        progressBar.setPadding(0, 0, 0, 0);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        progressBar.setMax(100);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(context.get());
        tvText.setText("Updating Musics");
        tvText.setTextColor(Color.parseColor("#32A852"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        ll.addView(tvText);
        ll.addView(progressBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(context.get());
        builder.setCancelable(false);
        builder.setView(ll);

        dialog = builder.create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
    }

    @Override
    protected Object doInBackground(Object... objects) {
        //PARAMS: 0=context 1=callback 2=db
        callback = (OnFillDbAsyncTaskFinish) objects[1];
        AppDatabase appDatabase = (AppDatabase) objects[2];
        //appDatabase.musicArtistCrossRefDao().deleteAll();
        //appDatabase.musicDao().deleteAll();
        //appDatabase.artistDao().deleteAll();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = ((Context) objects[0]).getContentResolver().query(uri, projection, selection, null, null);
        Log.d("TAG", "totcount " + cursor.getCount());
        if (cursor.getCount() > 0) {
            publishProgress(cursor.getCount());
            maxSet = true;
            List<Long> oldMusicIds = appDatabase.musicDao().selectAllIds();
            List<String> oldMusicPaths = appDatabase.musicDao().selectAllPaths();
            boolean[] deletedMusics = new boolean[oldMusicIds.size()];
            Arrays.fill(deletedMusics, true);

            while (cursor.moveToNext()) {
                publishProgress(cursor.getPosition());

                long newMusicId = longIsNull(cursor.getString(0));
                int oldMusicsIdPosition = oldMusicIds.indexOf(newMusicId);
                String newMusicPath = stringIsNull(cursor.getString(7));
                int oldMusicsPathPosition = oldMusicPaths.indexOf(newMusicPath);
                //Log.d("none", newMusicId + " o " + stringIsNull(cursor.getString(2)));
                if (newMusicPath != null && oldMusicsPathPosition == -1) {
                    Music music = new Music(longIsNull(cursor.getString(0)), trackIsNull(cursor.getString(1)), stringIsNull(cursor.getString(2)), stringIsNull(cursor.getString(3)), longIsNull(cursor.getString(4)), stringIsNull(cursor.getString(5)), stringIsNull(cursor.getString(7)));
                    if ((newMusicId != 0 && oldMusicsIdPosition == -1)) {
                        Log.d("tag", "new music " + newMusicId);
                        //Add music
                        appDatabase.musicDao().insert(music);

                        //Add artists
                        String artistStr = stringIsNull(cursor.getString(6));
                        if (artistStr != null) {
                            String[] artistsStr = artistStr.split("/");
                            List<Artist> existingArtists = appDatabase.artistDao().selectAll();
                            List<Artist> newArtists = new ArrayList<>();
                            List<Long> existingIds = new ArrayList<>();
                            List<Long> returnIds;

                            for (String s : artistsStr) {
                                boolean exists = false;
                                for (Artist a : existingArtists) {
                                    if (s.equals(a.getArtist_name())) {
                                        exists = true;
                                        existingIds.add(a.getArtist_id());
                                    }
                                }
                                if (!exists)
                                    newArtists.add(new Artist(s));
                            }
                            if (newArtists.size() > 0) {
                                returnIds = appDatabase.artistDao().insertAll(newArtists);
                                existingIds.addAll(returnIds);
                            }

                            //Add relation
                            List<MusicArtistCrossRef> musicArtistCrossRefs = new ArrayList<>();
                            for (long l : existingIds)
                                musicArtistCrossRefs.add(new MusicArtistCrossRef(music.getMusic_id(), l));
                            appDatabase.musicArtistCrossRefDao().insertAll(musicArtistCrossRefs);
                        }
                    } else {
                        Log.d("tag", "update music " + newMusicId);
                        appDatabase.musicDao().update(music);
                        deletedMusics[oldMusicsIdPosition] = false;
                    }
                } else {
                    deletedMusics[oldMusicsIdPosition] = false;
                }
            }
            for (int i = 0; i < deletedMusics.length; i++) {
                if (deletedMusics[i]) {
                    //appDatabase.musicArtistCrossRefDao().deleteById(oldMusicIds.get(i));
                    appDatabase.musicDao().deleteById(oldMusicIds.get(i));
                }
            }
            List<Long> oldArtists = appDatabase.artistDao().selectAllArtistsId();
            List<Long> newArtists = appDatabase.musicArtistCrossRefDao().selectAllArtistsId();
            for (long artistId : oldArtists) {
                if (!newArtists.contains(artistId)) {
                    appDatabase.artistDao().deleteById(artistId);
                }
            }
        }
        cursor.close();
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        ProgressBar progressBar = (ProgressBar) dialog.findViewById(id);
        if (!maxSet)
            progressBar.setMax(values[0]);
        progressBar.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        dialog.dismiss();
        callback.onFillDbAsyncTaskFinish();
    }

    private String stringIsNull(String str) {
        return str == null || str.equals("<unknown>") ? null : str.replace("'", "''");
    }

    private int intIsNull(String str) {
        return str == null || str.equals("<unknown>") ? 0 : Integer.parseInt(str);
    }

    private long longIsNull(String str) {
        return str == null || str.equals("<unknown>") ? 0 : Long.parseLong(str);
    }

    private int trackIsNull(String str) {
        int trackNumber;
        if (str == null)
            return 0;
        if (str.length() == 4)
            trackNumber = Integer.parseInt(str.substring(0, 1));
        else if (str.length() == 5)
            trackNumber = Integer.parseInt(str.substring(0, 2));
        else
            trackNumber = Integer.parseInt(str);
        return trackNumber;
    }
}
