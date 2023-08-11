package com.maybe.maybe.fragments.player.service;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;

import java.io.IOException;

public class CustomMediaPlayer implements MediaPlayer.OnPreparedListener {
    private MediaPlayer[] mediaPlayers;
    private int currentMediaPlayer;
    private CustomOnPreparedListener customOnPreparedListener;

    public CustomMediaPlayer(Context context) {
        currentMediaPlayer = 0;
        mediaPlayers = new MediaPlayer[2];
        for (int i = 0; i < mediaPlayers.length; i++) {
            mediaPlayers[i] = new MediaPlayer();
            mediaPlayers[i].setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayers[i].setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            mediaPlayers[i].setOnErrorListener((MediaPlayer.OnErrorListener) context);
            mediaPlayers[i].setOnPreparedListener(this);
            customOnPreparedListener = (CustomOnPreparedListener) context;
            mediaPlayers[i].setOnCompletionListener((MediaPlayer.OnCompletionListener) context);
            mediaPlayers[i].setOnSeekCompleteListener((MediaPlayer.OnSeekCompleteListener) context);
        }
    }

    public void goToNextMediaPlayer() {
        if (currentMediaPlayer == mediaPlayers.length - 1)
            currentMediaPlayer = 0;
        else
            currentMediaPlayer++;
    }

    private int getNextMediaPlayer() {
        if (currentMediaPlayer == mediaPlayers.length - 1)
            return 0;
        else
            return currentMediaPlayer + 1;
    }

    public void setNextMediaPlayer(boolean isNull) {
        mediaPlayers[currentMediaPlayer].setNextMediaPlayer(isNull ? null : mediaPlayers[getNextMediaPlayer()]);
    }

    public void setDataSource(Context context, Uri uri, boolean isCurrentPlayer) throws IOException {
        mediaPlayers[isCurrentPlayer ? currentMediaPlayer : getNextMediaPlayer()].setDataSource(context, uri);
    }

    public void prepareAsync(boolean isCurrentPlayer) {
        mediaPlayers[isCurrentPlayer ? currentMediaPlayer : getNextMediaPlayer()].prepareAsync();
    }

    public void start() {
        mediaPlayers[currentMediaPlayer].start();
    }

    public void pause() {
        mediaPlayers[currentMediaPlayer].pause();
    }

    public void stop(boolean isCurrentPlayer) {
        mediaPlayers[isCurrentPlayer ? currentMediaPlayer : getNextMediaPlayer()].stop();
    }

    public void reset(boolean isCurrentPlayer) {
        mediaPlayers[isCurrentPlayer ? currentMediaPlayer : getNextMediaPlayer()].reset();
    }

    public void destroy() {
        for (int i = 0; i < mediaPlayers.length; i++) {
            mediaPlayers[i].reset();
            mediaPlayers[i].release();
            mediaPlayers[i] = null;
        }
    }

    public void next() {

    }

    public void previous() {

    }

    public boolean isPlaying() {
        return mediaPlayers[currentMediaPlayer].isPlaying();
    }

    public void seekTo(int milliseconds) {
        mediaPlayers[currentMediaPlayer].seekTo(milliseconds);
    }

    public int getCurrentPosition() {
        return mediaPlayers[currentMediaPlayer].getCurrentPosition();
    }

    public boolean isLooping() {
        return mediaPlayers[currentMediaPlayer].isLooping();
    }

    public void setLooping(boolean looping) {
        for (MediaPlayer m : mediaPlayers)
            m.setLooping(looping);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        customOnPreparedListener.onCustomOnPreparedListener(mediaPlayer == mediaPlayers[currentMediaPlayer]);
    }
}
