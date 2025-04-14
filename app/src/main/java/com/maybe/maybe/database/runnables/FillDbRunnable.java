package com.maybe.maybe.database.runnables;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
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

public class FillDbRunnable implements Runnable {
    private static final String TAG = "FillDbRunnable";
    private final WeakReference<Context> context;
    private final IFillDbRunnable callback;
    private final AppDatabase appDatabase;
    private AlertDialog dialog;
    private int id;
    private boolean maxSet = false;

    public FillDbRunnable(Context context, IFillDbRunnable callback, AppDatabase appDatabase) {
        this.context = new WeakReference<>(context);
        this.callback = callback;
        this.appDatabase = appDatabase;
        init();
    }

    private void init() {
        Log.d(TAG, "init");
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
    public void run() {
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
        Cursor cursor = context.get().getContentResolver().query(uri, projection, selection, null, null);
        Log.d(TAG, "music number = " + cursor.getCount());
        if (cursor.getCount() > 0) {
            publishProgress(cursor.getCount());
            maxSet = true;
            List<Long> oldMusicIds = appDatabase.musicDao().selectAllIds();
            List<String> oldMusicPaths = appDatabase.musicDao().selectAllPaths();
            List<String> oldMusicFolders = appDatabase.musicDao().selectAllFolders();
            boolean[] deletedMusics = new boolean[oldMusicIds.size()];
            Arrays.fill(deletedMusics, true);

            while (cursor.moveToNext()) {
                publishProgress(cursor.getPosition());

                long newMusicId = longIsNull(cursor.getString(0));
                int oldMusicsIdPosition = oldMusicIds.indexOf(newMusicId);
                String newMusicPath = stringIsNull(cursor.getString(7));
                int oldMusicsPathPosition = oldMusicPaths.indexOf(newMusicPath);

                String newMusicFolder = stringIsNull(cursor.getString(7));
                int toIndex = newMusicFolder.lastIndexOf("/");
                int fromIndex = newMusicFolder.lastIndexOf("/", toIndex - 1);
                newMusicFolder = newMusicFolder.substring(fromIndex + 1, toIndex);
                int oldMusicsFolderPosition = oldMusicFolders.indexOf(newMusicFolder);
                if (newMusicPath != null && (oldMusicsPathPosition == -1 || oldMusicsFolderPosition == -1)) {
                    Music music = new Music(longIsNull(cursor.getString(0)), trackIsNull(cursor.getString(1)), stringIsNull(cursor.getString(2)), stringIsNull(cursor.getString(3)), longIsNull(cursor.getString(4)), stringIsNull(cursor.getString(5)), stringIsNull(cursor.getString(7)), newMusicFolder);
                    if ((newMusicId != 0 && oldMusicsIdPosition == -1)) {
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
                        appDatabase.musicDao().update(music);
                        deletedMusics[oldMusicsIdPosition] = false;
                    }
                } else {
                    deletedMusics[oldMusicsIdPosition] = false;
                }
            }
            for (int i = 0; i < deletedMusics.length; i++) {
                if (deletedMusics[i]) {
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
        dialog.dismiss();
        callback.onFinish();
    }

    private void publishProgress(int value) {
        ProgressBar progressBar = (ProgressBar) dialog.findViewById(id);
        if (!maxSet)
            progressBar.setMax(value);
        progressBar.setProgress(value);
    }

    private String stringIsNull(String str) {
        return str == null || str.equals("<unknown>") ? null : str;//.replace("'", "''");
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
