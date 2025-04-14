package com.maybe.maybe.fragments.player.service;

import static android.provider.MediaStore.Files.getContentUri;

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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.material.color.MaterialColors;
import com.maybe.maybe.R;
import com.maybe.maybe.activities.MainActivity;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.database.runnables.MusicRunnable;
import com.maybe.maybe.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MediaPlayerService extends MediaBrowserServiceCompat implements CustomOnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String TAG = "MediaPlayerService";
    private static final String CHANNEL_ID = "musicChannelId";
    private static final int NOTIFICATION_ID = 1338;
    private final IntentFilter becomeNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
    private final Handler mHandler = new Handler();
    protected String loopState, playingState;
    private Runnable updateTimeTask;
    private AppDatabase appDatabase;
    private MediaDescriptionCompat description;
    private PlaybackStateCompat.Builder playbackStateBuilder;
    private MediaSessionCompat mediaSession;
    private MediaMetadataCompat mediaMetadata;
    private MediaMetadataCompat.Builder mediaMetadataBuilder;
    private CustomMediaPlayer mediaPlayer;
    //private Equalizer equalizer;
    private boolean isNoisyAudioStreamReceiverRegistered, callStateListenerRegistered;
    private int currentDuration, begin; //begin-->> -1 = dont prepare, 0 = prepare but dont play(at start), 1 = prepare and play
    private NotificationManager notificationManager;
    private MusicList musicList;
    private TelephonyManager telephonyManager;
    private TelephonyCallback telephonyCallback;
    private PhoneStateListener callStateListener;
    private MusicWithArtists currentMusic;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        callStateListener = (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) ? new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (mediaPlayer.isPlaying())
                        callback.onPause();
                }
            }
        } : null;

        telephonyCallback = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? new CustomTelephonyCallback() {
            @Override
            public void onCallStateChanged(int i) {
                if (i == TelephonyManager.CALL_STATE_RINGING) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying())
                        callback.onPause();
                }
            }
        } : null;

        updateTimeTask = new Runnable() {
            public void run() {
                if (mediaPlayer.getCurrentPosition() < 500) {
                    updateMetaData(true);
                    updateCurrentDuration(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition());
                }
                if (mediaPlayer.isPlaying())
                    mHandler.postDelayed(this, 500);
                else
                    removeRunnable();
            }
        };

        appDatabase = AppDatabase.getInstance(this);
        createNotificationChannel();

        loopState = Constants.REPEAT_ALL;

        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setCallback(callback);
        mediaSession.setActive(true);
        setSessionToken(mediaSession.getSessionToken());
        MediaControllerCompat controller = mediaSession.getController();

        playbackStateBuilder = new PlaybackStateCompat.Builder();
        //playbackStateBuilder.addCustomAction(CUSTOM_ACTION_REPLAY, getString(R.string.custom_action_replay), R.drawable.ic_replay)
        playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO);
        playbackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1);
        mediaSession.setPlaybackState(playbackStateBuilder.build());

        mediaMetadataBuilder = new MediaMetadataCompat.Builder();
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Title");
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Artist");
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Album");
        mediaMetadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, -1);
        mediaMetadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, null);
        mediaSession.setMetadata(mediaMetadataBuilder.build());
        mediaMetadata = controller.getMetadata();
        description = mediaMetadata.getDescription();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "test2");
        String action = intent.getAction();
        Log.d(TAG, "onStartCommand " + action);
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        switch (action) {
            case Constants.ACTION_CREATE_SERVICE:
                startForeground(NOTIFICATION_ID, buildForegroundNotification());
                telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                registerCallStateListener();
                break;
            case Constants.ACTION_CHANGE_MUSIC:
                begin = 1;
                Music music = intent.getExtras().getParcelable(getString(R.string.key_parcelable_data));
                if (musicList != null)
                    musicList.changeForMusicWithId(music.getMusic_id());
                initMediaPlayer(true);
                break;
            case Constants.ACTION_APP_FOREGROUND:
                onConnect();
                break;
            case Constants.ACTION_END_SERVICE:
                stopForeground(true);
                stopSelf();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
        //return START_REDELIVER_INTENT;
    }

    private void addRunnable() {
        if (mediaPlayer.isPlaying() && !mHandler.hasCallbacks(updateTimeTask))
            mHandler.postDelayed(updateTimeTask, 500);
    }

    private void removeRunnable() {
        if (mHandler.hasCallbacks(updateTimeTask))
            mHandler.removeCallbacks(updateTimeTask);
    }

    private void registerCallStateListener() {
        if (!callStateListenerRegistered) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    telephonyManager.registerTelephonyCallback(getMainExecutor(), telephonyCallback);
                    callStateListenerRegistered = true;
                }
            } else {
                telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                callStateListenerRegistered = true;
            }
        }
    }

    private void unregisterCallStateListener() {
        if (callStateListenerRegistered) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.unregisterTelephonyCallback(telephonyCallback);
                callStateListenerRegistered = false;
            } else {
                telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
                callStateListenerRegistered = false;
            }
        }
    }

    public void setMusicList(long[] tempIdList) {
        ArrayList<Long> idList = new ArrayList<>();
        for (long id : tempIdList) {
            idList.add(id);
        }
        if (musicList == null) {
            musicList = new MusicList();
            musicList.setMusics(idList);
            musicList.resetPointer();
            begin = 0;
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            int timestamp = sharedPref.getInt(getString(R.string.current_timestamp), 0);
            long fileId = sharedPref.getLong(getString(R.string.current_file_id), 0);
            if (!musicList.changeForMusicWithId((int) fileId))
                timestamp = 0;
            currentDuration = timestamp;
            updateMetaData(true);
            Log.e(TAG, "setMusicList + " + musicList.size());
            initMediaPlayer(true);
            callback.onSeekTo(currentDuration);
            //notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification());
        } else {
            //MusicWithArtists music = musicList.getCurrent();
            musicList.setMusics(idList);
            musicList.resetPointer();
            //musicList.goNext(Constants.REPEAT_ALL);
            //currentMusic = musicList.getCurrent();
            //musicList.resetPointer();
            begin = -1;
        }
    }

    public void onConnect() {
        Log.d(TAG, "onConnect ");
        if (musicList != null) {
            updateMetaData(false);
            updateCurrentDuration(-1, mediaPlayer.getCurrentPosition());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (musicList != null) {
            if (musicList.getPointer() != -1) {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong(getString(R.string.current_file_id), musicList.getCurrent());
                editor.putInt(getString(R.string.current_timestamp), mediaPlayer.getCurrentPosition());
                editor.apply();
            }
            mediaSession.setActive(false);
            callback.onStop();
            /*equalizer.setEnabled(false);
            equalizer.release();
            equalizer = null;*/
            mediaPlayer.destroy();
        }
        if (isNoisyAudioStreamReceiverRegistered) {
            unregisterReceiver(myNoisyAudioStreamReceiver);
            isNoisyAudioStreamReceiverRegistered = false;
        }
        unregisterCallStateListener();
    }

    @Override
    public void onCustomOnPreparedListener(boolean isCurrentPlayer) {
        Log.d(TAG, "onPrepared, begin = " + begin + " " + isCurrentPlayer);
        if (isCurrentPlayer) {
            mediaPlayer.setLooping(loopState.equals(Constants.REPEAT_ONE));
            if (begin == 1) {
                callback.onPlay();
            } else if (begin == 0) {
                updateMetaData(false);
                mediaPlayer.seekTo(currentDuration);
                updateCurrentDuration(-1, mediaPlayer.getCurrentPosition());
            } else {
                begin = 1;
            }
            mediaPlayer.setLooping(loopState.equals(Constants.REPEAT_ONE));
            if (musicList.getPointer() != -1) {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong(getString(R.string.current_file_id), musicList.getCurrent());
                editor.putInt(getString(R.string.current_timestamp), 0);
                editor.apply();
            }
        } else {
            if (loopState.equals(Constants.REPEAT_NONE) && musicList.getPointer() == musicList.size() - 1)
                mediaPlayer.setNextMediaPlayer(true);
            else
                mediaPlayer.setNextMediaPlayer(false);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError");
        return true;
    }

    public void initMediaPlayer(boolean initBoth) {
        Log.d(TAG, "initMediaPlayer");
        if (initBoth) {
            if (mediaPlayer == null) {
                mediaPlayer = new CustomMediaPlayer(this);

            /*float volume = 1f;
            mediaPlayer.setVolume(volume, volume);
            equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
            equalizer.usePreset((short) 0);
            equalizer.setEnabled(true);*/
            } else if (mediaPlayer.isPlaying()) {
                callback.onStop();
            }
            mediaPlayer.reset(true);

            Uri uri = getContentUri("external", musicList.getCurrent());
            DocumentFile sourceFile = DocumentFile.fromSingleUri(this, uri);
            if (sourceFile.exists()) {
                try {
                    mediaPlayer.setDataSource(this, uri, true);
                    mediaPlayer.prepareAsync(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    onDestroy();
                }
            } else {
                callback.onSkipToNext();
            }
        }
        mediaPlayer.reset(false);
        Uri uriNext = getContentUri("external", musicList.getNext());
        DocumentFile sourceFileNext = DocumentFile.fromSingleUri(this, uriNext);
        if (sourceFileNext.exists()) {
            try {
                mediaPlayer.setDataSource(this, uriNext, false);
                mediaPlayer.prepareAsync(false);
            } catch (IOException e) {
                e.printStackTrace();
                onDestroy();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "onCompletion");
        callback.onStop();
        if (loopState.equals(Constants.REPEAT_NONE) && musicList.getPointer() == musicList.size() - 1)
            begin = 0;
        callback.onSkipToNext();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Log.w(TAG, "WOWOWOW onSeekComplete");
    }

    public void updateMetaData(boolean buildNotification) {
        Log.d(TAG, "updateMetaData");
        //Get metadata from database only if it's a new music
        Log.e(TAG, "" + musicList);
        if (musicList == null || currentMusic == null || musicList.getCurrent() != (int) currentMusic.music.getMusic_id()) {
            Executors.newSingleThreadExecutor().execute(new MusicRunnable(this, objects -> {
                ArrayList<MusicWithArtists> musicWithArtists = (ArrayList<MusicWithArtists>) (Object) objects;
                currentMusic = musicWithArtists.get(0);
                MediaPlayerService.this.updateMetadata2(currentMusic);
                if (buildNotification)
                    notificationManager.notify(NOTIFICATION_ID, MediaPlayerService.this.buildForegroundNotification());
            }, appDatabase, "selectMusicFromId", (long) musicList.getCurrent(), null, null, null));
        } else {
            updateMetadata2(currentMusic);
            if (buildNotification)
                notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification());
        }
    }

    private void updateMetadata2(MusicWithArtists musicWithArtists) {
        if (musicWithArtists == null || musicWithArtists.music == null)
            return;
        Log.d(TAG, "updateMetaData2 " + musicWithArtists.music.getMusic_title());

        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, Long.toString(musicWithArtists.music.getMusic_id()));
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicWithArtists.music.getMusic_title());
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicWithArtists.artistsToString());
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, musicWithArtists.music.getMusic_album());
        mediaMetadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, musicWithArtists.music.getMusic_duration());

        MediaMetadataRetriever receiver = new MediaMetadataRetriever();
        File file = new File(musicWithArtists.music.getMusic_path());
        Uri uri = Uri.fromFile(file);
        receiver.setDataSource(this, uri);
        byte[] data = receiver.getEmbeddedPicture();
        try {
            receiver.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapDrawable icon = null;
        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            icon = new BitmapDrawable(getResources(), bitmap);
        }
        mediaMetadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, icon != null ? icon.getBitmap() : null);

        mediaSession.setMetadata(mediaMetadataBuilder.build());
    }

    public void updateCurrentDuration(int state, int position) {
        Log.d(TAG, "updateCurrentDuration");
        int newState = state;
        if (state == -1 && mediaPlayer != null)
            newState = mediaPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        playbackStateBuilder.setState(newState, position, 1);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        currentDuration = position;
        playingState = newState == PlaybackStateCompat.STATE_PLAYING ? Constants.ACTION_PLAY : Constants.ACTION_PAUSE;
    }

    private void createNotificationChannel() {
        CharSequence name = "Playing Music";
        String description = "See the music that is currently playing";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(description);
        notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    public Notification buildForegroundNotification() {
        Log.d(TAG, "buildForegroundNotification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q || currentMusic == null) {
            builder.setContentTitle(description.getTitle());
            builder.setContentText(description.getSubtitle());
            builder.setLargeIcon(description.getIconBitmap());
        } else {
            builder.setContentTitle(currentMusic.music.getMusic_title());
            builder.setContentText(currentMusic.artistsToString());
        }

        Intent intent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE));
        builder.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        builder.setSmallIcon(R.drawable.round_music_note_24);
        builder.setColorized(true);
        builder.setColor(MaterialColors.getColor(this, R.attr.backgroundColor, 0x00000000));
        builder.addAction(new NotificationCompat.Action(R.drawable.round_skip_previous, "previous", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_round_pause_24, "pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)));
        } else {
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_round_play_arrow_24, "play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)));
        }
        builder.addAction(new NotificationCompat.Action(R.drawable.round_skip_next, "next", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));

        MediaStyle mediaStyle = new MediaStyle();
        mediaStyle.setMediaSession(mediaSession.getSessionToken());
        mediaStyle.setShowActionsInCompactView(0, 1, 2);
        mediaStyle.setShowCancelButton(false);
        builder.setStyle(mediaStyle);
        return (builder.build());
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable @org.jetbrains.annotations.Nullable Bundle rootHints) {
        return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        // https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice
        // https://developer.android.com/training/cars/media#build_hierarchy
        result.sendResult(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static abstract class CustomTelephonyCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        public void onCallStateChanged(int i) {}
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (mediaPlayer.isPlaying())
                    callback.onPause();
            }
        }
    }

    private final MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            Log.d(TAG, "MediaSession onPlay");
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                updateCurrentDuration(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition());
                addRunnable();
                if (!isNoisyAudioStreamReceiverRegistered) {
                    registerReceiver(myNoisyAudioStreamReceiver, becomeNoisyIntentFilter);
                    isNoisyAudioStreamReceiverRegistered = true;
                }
                if (begin != -1) {
                    updateMetaData(true);
                } else {
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification());
                }
                begin = 1;
            }
        }

        @Override
        public void onPause() {
            Log.d(TAG, "MediaSession onPause");
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                updateCurrentDuration(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition());
                removeRunnable();
                if (begin != -1) {
                    updateMetaData(true);
                } else {
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification());
                }
            }
        }

        @Override
        public void onStop() {
            Log.d(TAG, "MediaSession onStop");
            //if (mediaPlayer.isPlaying()) {
            updateCurrentDuration(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition());
            removeRunnable();
            if (isNoisyAudioStreamReceiverRegistered) {
                unregisterReceiver(myNoisyAudioStreamReceiver);
                isNoisyAudioStreamReceiverRegistered = false;
            }
            //}
            currentDuration = 0;
            mediaPlayer.stop(true);
        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG, "MediaSession onSkipToPrevious");
            begin = 1;
            musicList.goPrevious(loopState);
            if (loopState.equals(Constants.REPEAT_NONE))
                if (musicList.getPointer() == -1) {
                    begin = 0;
                    musicList.changeForIndex(musicList.size() - 1);
                }
            currentDuration = 0;
            initMediaPlayer(true);
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "MediaSession onSkipToNext " + begin);
            begin = begin == 1 ? 1 : 0;
            musicList.goNext(loopState);
            if (musicList.getPointer() == -1 && loopState.equals(Constants.REPEAT_NONE)) {
                begin = 0;
                musicList.changeForIndex(0);
            }
            currentDuration = 0;
            mediaPlayer.goToNextMediaPlayer();
            initMediaPlayer(true);
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            mediaPlayer.seekTo((int) pos);
            updateCurrentDuration(-1, (int) pos);
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            Log.d(TAG, "onCommand " + command);
            super.onCommand(command, extras, cb);
            if (command.equals(Constants.ACTION_CHANGE_LIST)) {
                setMusicList(extras.getLongArray(getString(R.string.key_parcelable_data)));
            } else if (command.equals(Constants.ACTION_SORT)) {
                String loop = extras.getString(getString(R.string.key_parcelable_data));
                mediaPlayer.setLooping(loop.equals(Constants.REPEAT_ONE));
                loopState = loop;
            }
        }
    };
}