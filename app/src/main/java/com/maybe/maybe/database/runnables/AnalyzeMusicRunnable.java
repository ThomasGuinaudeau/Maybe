package com.maybe.maybe.database.runnables;

import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.dao.MusicDao;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.utils.AudioUtils;

public class AnalyzeMusicRunnable implements Runnable {
    private final AppDatabase appDatabase;
    private final long musicId;

    public AnalyzeMusicRunnable(AppDatabase appDatabase, long musicId) {
        this.appDatabase = appDatabase;
        this.musicId = musicId;
    }

    @Override
    public void run() {
        MusicDao dao = appDatabase.musicDao();
        MusicWithArtists musicWithArtist = dao.selectMusicFromId(musicId);
        if (musicWithArtist.music.getMusic_rms() == 0) {
            int[] sampleRateAndChannels = AudioUtils.getSampleRateAndChannels(musicWithArtist.music.getMusic_path());
            byte[] pcm = AudioUtils.audioToPCM(musicWithArtist.music.getMusic_path());
            double rms = AudioUtils.getRMS(pcm, sampleRateAndChannels[0], sampleRateAndChannels[1]);

            musicWithArtist.music.setMusic_rms(rms);
            dao.update(musicWithArtist.music);
        }
    }
}
