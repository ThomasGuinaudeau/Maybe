package com.maybe.maybe;

import static android.provider.MediaStore.Files.getContentUri;
import static com.maybe.maybe.utils.Constants.ACTION_APP_BACKGROUND;
import static com.maybe.maybe.utils.Constants.ACTION_APP_FOREGROUND;
import static com.maybe.maybe.utils.Constants.ACTION_CREATE_SERVICE;
import static com.maybe.maybe.utils.Constants.ACTION_END_SERVICE;
import static com.maybe.maybe.utils.Constants.ACTION_NEXT;
import static com.maybe.maybe.utils.Constants.ACTION_PLAY_PAUSE;
import static com.maybe.maybe.utils.Constants.ACTION_PREVIOUS;
import static com.maybe.maybe.utils.Constants.ACTION_TO_ACTIVITY;
import static com.maybe.maybe.utils.Constants.ACTION_TO_SERVICE;
import static com.maybe.maybe.utils.Constants.ACTION_UPDATE_COLORS;
import static com.maybe.maybe.utils.Constants.BROADCAST_DESTINATION;
import static com.maybe.maybe.utils.Constants.BROADCAST_EXTRAS;
import static com.maybe.maybe.utils.Constants.REPEAT_ALL;
import static com.maybe.maybe.utils.Constants.REPEAT_NONE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.maybe.maybe.activities.MainActivity;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.utils.ColorsConstants;

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "MediaPlayerService";
    private static final String CHANNEL_ID = "musicChannelId";
    private static final int NOTIFICATION_ID = 1338;
    private final Handler mHandler = new Handler();
    private final IntentFilter becomeNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
    private MediaPlayer mediaPlayer;
    //private Equalizer equalizer;
    private final Runnable updateTimeTask = new Runnable() {
        public void run() {
            sendBroadcast("change_duration", mediaPlayer.getCurrentPosition());
            mHandler.postDelayed(this, 200);
        }
    };
    private boolean isServiceReceiverRegistered, isNoisyAudioStreamReceiverRegistered;
    private String isLoop;
    private int timestamp, begin; //begin-->> -1 = dont prepare, 0 = prepare but dont play(at start), 1 = prepare and play
    private NotificationManager notificationManager;
    private MediaSessionCompat mediaSession;
    private MusicList musicList;
    public BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            Log.d(TAG, "Brodcast receive, action = " + intent.getAction() + " destination = " + extras.getString(BROADCAST_DESTINATION));
            Object[] objects = (Object[]) extras.get(BROADCAST_EXTRAS);
            switch (extras.getString(BROADCAST_DESTINATION)) {
                case "change_music_list":
                    setMusicList((ArrayList<MusicWithArtists>) objects[0]);
                    break;
                case "change_music":
                    begin = 1;
                    Music music = (Music) objects[0];
                    musicList.changeForMusicWithId(music.getMusic_id());
                    initMediaPlayer(musicList.getCurrent().music.getMusic_id());
                    break;
                case "action":
                    if (objects[0].equals("change_selection")) {
                        if (musicList.getPointer() != -1)
                            sendBroadcast("change_selection", musicList.getCurrent().music);
                    } else if (objects[0].equals("loop"))
                        isLoop = (String) objects[1];
                    else if (objects[0].equals("skip") && objects[1].equals("previous"))
                        skipToPrevious();
                    else if (objects[0].equals("skip") && objects[1].equals("next"))
                        skipToNext();
                    else if (objects[0].equals("state") && objects[1].equals("play_pause")) {
                        if (mediaPlayer.isPlaying())
                            pauseMedia();
                        else
                            playMedia();
                    } else if (objects[0].equals("seekBar"))
                        mediaPlayer.seekTo(Integer.parseInt((String) objects[1]));
                    break;
            }
        }
    };
    private final MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            Log.d(TAG, "MediaSession onPlay");
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                mediaSession.setPlaybackState(buildPlaybackState(PlaybackStateCompat.STATE_PLAYING));
                //mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(0.5f));
                if (!isNoisyAudioStreamReceiverRegistered) {
                    registerReceiver(myNoisyAudioStreamReceiver, becomeNoisyIntentFilter);
                    isNoisyAudioStreamReceiverRegistered = true;
                }
                if (begin != -1) {
                    updateMetaData();
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification("all"));
                } else {
                    sendBroadcast("change_state", mediaPlayer.isPlaying());
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification("none"));
                }
                if (isServiceReceiverRegistered) {
                    mHandler.postDelayed(updateTimeTask, 200);
                    Log.d(TAG, "mHandler = on");
                }
            }
        }

        @Override
        public void onPause() {
            Log.d(TAG, "MediaSession onPause");
            if (mediaPlayer.isPlaying()) {
                mHandler.removeCallbacks(updateTimeTask);
                Log.d(TAG, "mHandler = off");
                mediaPlayer.pause();
                //mediaPlayer = null;
                mediaSession.setPlaybackState(buildPlaybackState(PlaybackStateCompat.STATE_PAUSED));
                if (begin != -1) {
                    updateMetaData();
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification("all"));
                } else {
                    sendBroadcast("change_state", mediaPlayer.isPlaying());
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification("none"));
                }
            }
        }

        @Override
        public void onStop() {
            Log.d(TAG, "MediaSession onStop");
            if (mediaPlayer.isPlaying()) {
                Log.d(TAG, "mHandler = off");
                if (isNoisyAudioStreamReceiverRegistered) {
                    unregisterReceiver(myNoisyAudioStreamReceiver);
                    isNoisyAudioStreamReceiverRegistered = false;
                }
                mHandler.removeCallbacks(updateTimeTask);
            }
            mediaPlayer.stop();
        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG, "MediaSession onSkipToPrevious");
            begin = 1;
            musicList.goPrevious(isLoop);
            if (isLoop.equals(REPEAT_NONE))
                if (musicList.getPointer() == -1) {
                    begin = 0;
                    musicList.changeForIndex(musicList.size() - 1);
                }
            initMediaPlayer(musicList.getCurrent().music.getMusic_id());
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "MediaSession onSkipToNext");
            begin = 1;
            musicList.goNext(isLoop);
            if (isLoop.equals(REPEAT_NONE))
                if (musicList.getPointer() == -1) {
                    begin = 0;
                    musicList.changeForIndex(0);
                }
            initMediaPlayer(musicList.getCurrent().music.getMusic_id());
        }
    };
    private final PhoneStateListener callStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                if (mediaPlayer.isPlaying())
                    pauseMedia();
            }
        }
    };

    public void onCreate() {
        Log.d(TAG, "onCreate");
        createNotificationChannel();

        isLoop = REPEAT_ALL;
        registerServiceReceiver();

        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setCallback(callback);
        mediaSession.setActive(true);
        mediaSession.setPlaybackState(buildPlaybackState(PlaybackStateCompat.STATE_PAUSED));
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        String action = intent.getAction();
        switch (action) {
            case ACTION_CREATE_SERVICE:
                startForeground(NOTIFICATION_ID, buildForegroundNotification("all"));
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                break;
            case ACTION_PREVIOUS:
                skipToPrevious();
                break;
            case ACTION_PLAY_PAUSE:
                if (mediaPlayer.isPlaying())
                    pauseMedia();
                else
                    playMedia();
                break;
            case ACTION_NEXT:
                skipToNext();
                break;
            case ACTION_APP_BACKGROUND:
                onAppBackground();
                break;
            case ACTION_APP_FOREGROUND:
                onAppForeground();
                break;
            case ACTION_END_SERVICE:
                stopForeground(true);
                stopSelf();
                break;
            case ACTION_UPDATE_COLORS:
                updateColors();
                break;
        }
        return START_NOT_STICKY;
    }

    private void registerServiceReceiver() {
        if (!isServiceReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, new IntentFilter(ACTION_TO_SERVICE));
            isServiceReceiverRegistered = true;
        }
    }

    private void unregisterServiceReceiver() {
        if (isServiceReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceReceiver);
            isServiceReceiverRegistered = false;
        }
    }

    private void setMusicList(ArrayList<MusicWithArtists> musicWithArtists) {
        if (musicList == null) {
            musicList = new MusicList();
            musicList.setMusics(musicWithArtists);
            musicList.resetPointer();
            begin = 0;
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            timestamp = sharedPref.getInt(getString(R.string.current_timestamp), 0);
            Log.e(TAG, "number loaded:" + sharedPref.getLong(getString(R.string.current_file_id), 0));
            if (!musicList.changeForMusicWithId(sharedPref.getLong(getString(R.string.current_file_id), 0)))
                timestamp = 0;
            initMediaPlayer(musicList.getCurrent().music.getMusic_id());
            notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification("all"));
        } else {
            musicList.setMusics(musicWithArtists);
            musicList.resetPointer();
            musicList.goNext(REPEAT_ALL);
            sendBroadcast("change_selection", musicList.getCurrent().music);
            musicList.resetPointer();
            begin = -1;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onAppBackground() {
        Log.d(TAG, "onAppBackground ");// + mediaPlayer.isPlaying());
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mHandler.removeCallbacks(updateTimeTask);
            Log.d(TAG, "mHandler = off");
        }
        unregisterServiceReceiver();
    }

    public void onAppForeground() {
        Log.d(TAG, "onAppForeground " + mediaPlayer.isPlaying());
        registerServiceReceiver();
        updateMetaData();
        if (mediaPlayer.isPlaying()) {
            mHandler.postDelayed(updateTimeTask, 200);
            Log.d(TAG, "mHandler = on");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        if (musicList != null) {
            if (musicList.getPointer() != -1) {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong(getString(R.string.current_file_id), musicList.getCurrent().music.getMusic_id());
                editor.putInt(getString(R.string.current_timestamp), mediaPlayer.getCurrentPosition());
                editor.apply();
                Log.e(TAG, "number saved:" + sharedPref.getLong(getString(R.string.current_file_id), 0));
            }
            stopMedia();
            /*equalizer.setEnabled(false);
            equalizer.release();
            equalizer = null;*/
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (isNoisyAudioStreamReceiverRegistered) {
            unregisterReceiver(myNoisyAudioStreamReceiver);
            isNoisyAudioStreamReceiverRegistered = false;
        }
        unregisterServiceReceiver();
    }

    public void onPrepared(MediaPlayer player) {
        Log.d(TAG, "onPrepared, begin = " + begin);
        if (begin == 1) {
            playMedia();
        } else if (begin == 0) {
            begin = 1;
            updateMetaData();
            mediaPlayer.seekTo(timestamp);
            sendBroadcast("change_duration", mediaPlayer.getCurrentPosition());
        } else {
            begin = 1;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError");
        return true;
    }

    public void initMediaPlayer(long fileId) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            /*equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
            equalizer.usePreset((short) 0);
            equalizer.setEnabled(true);*/
        }
        if (mediaPlayer.isPlaying())
            stopMedia();
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.reset();

        Uri uri = getContentUri("external", fileId);
        DocumentFile sourceFile = DocumentFile.fromSingleUri(this, uri);
        Log.e(TAG, fileId + " exist " + sourceFile.exists());
        if (sourceFile.exists()) {
            try {
                mediaPlayer.setDataSource(this, uri);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                onDestroy();
            }
        } else {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            skipToNext();
        }
    }

    public void updateColors() {
        notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification("colors"));
    }

    private void playMedia() {
        callback.onPlay();
    }

    private void pauseMedia() {
        callback.onPause();
    }

    private void stopMedia() {
        callback.onStop();
    }

    private void skipToPrevious() {
        callback.onSkipToPrevious();
    }

    private void skipToNext() {
        callback.onSkipToNext();
    }

    //Session

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");
        stopMedia();
        skipToNext();
    }

    public void updateMetaData() {
        Log.d(TAG, "updateMetaData");
        if (isServiceReceiverRegistered) {
            if (musicList.getPointer() != -1)
                sendBroadcast("change_metadata", musicList.getCurrent());
            sendBroadcast("change_state", mediaPlayer.isPlaying());
        }
    }

    private PlaybackStateCompat buildPlaybackState(int state) {
        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();
        builder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        builder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1);
        return (builder.build());
    }

    //Notification
    public PendingIntent pendingIntent(String action) {
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, (int) System.currentTimeMillis(), intent, 0);
    }

    public PendingIntent MainActivityPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
    }

    private void createNotificationChannel() {
        CharSequence name = "Playing Music";
        String description = "See the music that is currently playing";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(description);
        notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private Notification buildForegroundNotification(String updateType) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            contentView.setImageViewResource(R.id.notif_play_pause, R.drawable.round_pause_24);
        else
            contentView.setImageViewResource(R.id.notif_play_pause, R.drawable.round_play_arrow_24);

        switch (updateType) {
            case "all":
                contentView.setOnClickPendingIntent(R.id.notif_previous, pendingIntent(ACTION_PREVIOUS));
                contentView.setOnClickPendingIntent(R.id.notif_play_pause, pendingIntent(ACTION_PLAY_PAUSE));
                contentView.setOnClickPendingIntent(R.id.notif_next, pendingIntent(ACTION_NEXT));

                contentView.setTextViewText(R.id.notif_app_name, getString(R.string.app_name));
                if (musicList == null) {
                    contentView.setTextViewText(R.id.notif_title, "Title");
                    contentView.setTextViewText(R.id.notif_artist, "Artist");
                } else {
                    contentView.setTextViewText(R.id.notif_title, musicList.getCurrent().music.getMusic_title());
                    contentView.setTextViewText(R.id.notif_artist, musicList.getCurrent().artistsToString());
                }
            case "colors":
                contentView.setInt(R.id.notif_layout, "setBackgroundColor", ColorsConstants.NOTIFICATION_BACKGROUND_COLOR);
                contentView.setInt(R.id.notif_previous, "setColorFilter", ColorsConstants.NOTIFICATION_TEXT_TITLE_COLOR);
                contentView.setInt(R.id.notif_play_pause, "setColorFilter", ColorsConstants.NOTIFICATION_TEXT_TITLE_COLOR);
                contentView.setInt(R.id.notif_next, "setColorFilter", ColorsConstants.NOTIFICATION_TEXT_TITLE_COLOR);
                contentView.setTextColor(R.id.notif_app_name, ColorsConstants.NOTIFICATION_TEXT_TITLE_COLOR);
                contentView.setTextColor(R.id.notif_title, ColorsConstants.NOTIFICATION_TEXT_TITLE_COLOR);
                contentView.setTextColor(R.id.notif_artist, ColorsConstants.NOTIFICATION_TEXT_ARTIST_COLOR);
                contentView.setInt(R.id.notif_icon, "setColorFilter", ColorsConstants.NOTIFICATION_TEXT_TITLE_COLOR);
            default:
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.round_music_note_24);
        builder.setCategory(Notification.CATEGORY_SERVICE);
        builder.setContentIntent(MainActivityPendingIntent());
        builder.setContent(contentView);
        builder.setCustomContentView(contentView);
        builder.setCustomBigContentView(contentView);
        builder.setOnlyAlertOnce(true);
        return (builder.build());
    }

    private void sendBroadcast(String destination, Object... objects) {
        Intent new_intent = new Intent(ACTION_TO_ACTIVITY);
        new_intent.putExtra(BROADCAST_DESTINATION, destination);
        new_intent.putExtra(BROADCAST_EXTRAS, objects);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new_intent);
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (mediaPlayer.isPlaying())
                    pauseMedia();
            }
        }
    }
}