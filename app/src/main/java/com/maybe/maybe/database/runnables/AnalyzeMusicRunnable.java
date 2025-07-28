package com.maybe.maybe.database.runnables;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.MusicDao;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.utils.AudioUtils;

public class AnalyzeMusicRunnable implements Runnable {
    private IAnalyzeMusicRunnable callback;
    private final AppDatabase appDatabase;
    private final long musicId;

    public AnalyzeMusicRunnable(IAnalyzeMusicRunnable callback, AppDatabase appDatabase, long musicId) {
        this.callback = callback;
        this.appDatabase = appDatabase;
        this.musicId = musicId;
    }

    @Override
    public void run() {
        MusicDao dao = appDatabase.musicDao();
        MusicWithArtists musicWithArtist = dao.selectMusicFromId(musicId);
        if (musicWithArtist.music.getMusic_rms() == 0 || callback != null) {
            double rms = AudioUtils.decodeAndAnalyzeLoudness(musicWithArtist.music.getMusic_path());
            musicWithArtist.music.setMusic_rms(rms);
            dao.update(musicWithArtist.music);
            if(callback != null)
                callback.onFinishAnalyzing(rms);
        }
    }
}
