package com.maybe.maybe.fragments.main.service;

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
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

import com.maybe.maybe.R;
import com.maybe.maybe.activities.MainActivity;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.utils.ColorsConstants;
import com.maybe.maybe.utils.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayerService extends MediaBrowserServiceCompat implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String TAG = "MediaPlayerService";
    private static final String CHANNEL_ID = "musicChannelId";
    private static final int NOTIFICATION_ID = 1338;
    private final Handler mHandler = new Handler();
    private final IntentFilter becomeNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
    private final IBinder binder = new LocalBinder();
    protected MutableLiveData<Integer> currentDuration;
    protected MutableLiveData<String> loopState, playingState;
    protected MutableLiveData<MusicWithArtists> currentMusic;
    private MediaDescriptionCompat description;
    private PlaybackStateCompat.Builder playbackStateBuilder;
    private MediaSessionCompat mediaSession;
    private MediaMetadataCompat mediaMetadata;
    private MediaMetadataCompat.Builder mediaMetadataBuilder;
    private MediaPlayer mediaPlayer;
    //private Equalizer equalizer;
    private Runnable updateTimeTask;
    private boolean isNoisyAudioStreamReceiverRegistered, callStateListenerRegistered;
    private int begin; //begin-->> -1 = dont prepare, 0 = prepare but dont play(at start), 1 = prepare and play
    private NotificationManager notificationManager;
    private MusicList musicList;
    private TelephonyManager telephonyManager;
    private TelephonyCallback telephonyCallback;
    private PhoneStateListener callStateListener;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        callStateListener = (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) ? new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (mediaPlayer.isPlaying())
                        pauseMedia();
                }
            }
        } : null;

        telephonyCallback = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? new CustomTelephonyCallback() {
            @Override
            public void onCallStateChanged(int i) {
                if (i == TelephonyManager.CALL_STATE_RINGING) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying())
                        pauseMedia();
                }
            }
        } : null;

        updateTimeTask = new Runnable() {
            public void run() {
                currentDuration.setValue(mediaPlayer.getCurrentPosition());
                mHandler.postDelayed(this, 1000);
            }
        };

        createNotificationChannel();

        currentDuration = new MutableLiveData<>();
        loopState = new MutableLiveData<>();
        playingState = new MutableLiveData<>();
        currentMusic = new MutableLiveData<>();
        loopState.setValue(Constants.REPEAT_ALL);

        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setCallback(callback);
        mediaSession.setActive(true);
        setSessionToken(mediaSession.getSessionToken());
        MediaControllerCompat controller = mediaSession.getController();

        playbackStateBuilder = new PlaybackStateCompat.Builder();
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
        String action = intent.getAction();
        Log.d(TAG, "onStartCommand " + action);
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        switch (action) {
            case Constants.ACTION_CREATE_SERVICE:
                startForeground(NOTIFICATION_ID, buildForegroundNotification());
                telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                registerCallStateListener();
                break;
            case Constants.ACTION_PREVIOUS:
                skipToPrevious();
                break;
            case Constants.ACTION_PLAY_PAUSE:
                if (mediaPlayer.isPlaying())
                    pauseMedia();
                else
                    playMedia();
                break;
            case Constants.ACTION_NEXT:
                skipToNext();
                break;
            case Constants.REPEAT_ONE:
                mediaPlayer.setLooping(true);
                loopState.setValue(action);
                break;
            case Constants.REPEAT_NONE:
            case Constants.REPEAT_ALL:
                mediaPlayer.setLooping(false);
                loopState.setValue(action);
                break;
            case Constants.ACTION_SEEK_TO:
                int position = intent.getExtras().getInt(getString(R.string.key_parcelable_data));
                mediaPlayer.seekTo(position);
                callback.onSeekTo(position);
                break;
            case Constants.ACTION_CHANGE_LIST:
                setMusicList(intent.getExtras().getParcelableArrayList(getString(R.string.key_parcelable_data)));
                break;
            case Constants.ACTION_CHANGE_MUSIC:
                begin = 1;
                Music music = intent.getExtras().getParcelable(getString(R.string.key_parcelable_data));
                musicList.changeForMusicWithId(music.getMusic_id());
                initMediaPlayer(musicList.getCurrent().music.getMusic_id());
                break;
            case Constants.ACTION_APP_BACKGROUND:
                onAppBackground();
                break;
            case Constants.ACTION_APP_FOREGROUND:
                onAppForeground();
                break;
            case Constants.ACTION_END_SERVICE:
                stopForeground(true);
                stopSelf();
                break;
            case Constants.ACTION_UPDATE_COLORS:
                updateColors();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
        //return START_REDELIVER_INTENT;
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

    private void setMusicList(ArrayList<MusicWithArtists> musicWithArtists) {
        if (musicList == null) {
            musicList = new MusicList();
            musicList.setMusics(musicWithArtists);
            musicList.resetPointer();
            begin = 0;
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            int timestamp = sharedPref.getInt(getString(R.string.current_timestamp), 0);
            long fileId = sharedPref.getLong(getString(R.string.current_file_id), 0);
            if (!musicList.changeForMusicWithId(fileId))
                timestamp = 0;
            currentDuration.setValue(timestamp);
            initMediaPlayer(musicList.getCurrent().music.getMusic_id());
            notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification());
        } else {
            //MusicWithArtists music = musicList.getCurrent();
            musicList.setMusics(musicWithArtists);
            musicList.resetPointer();
            //musicList.goNext(Constants.REPEAT_ALL);
            //currentMusic.setValue(musicList.getCurrent());
            //musicList.resetPointer();
            begin = -1;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onAppBackground() {
        Log.d(TAG, "onAppBackground ");// + mediaPlayer.playingState());
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mHandler.removeCallbacks(updateTimeTask);
            Log.d(TAG, "mHandler = off");
        }
    }

    public void onAppForeground() {
        Log.d(TAG, "onAppForeground " + mediaPlayer.isPlaying());
        updateMetaData();
        if (mediaPlayer.isPlaying()) {
            mHandler.postDelayed(updateTimeTask, 0);
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
            }
            mediaSession.setActive(false);
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
        unregisterCallStateListener();
    }

    public void onPrepared(MediaPlayer player) {
        Log.d(TAG, "onPrepared, begin = " + begin);
        mediaPlayer.setLooping(loopState.getValue().equals(Constants.REPEAT_ONE));
        if (begin == 1) {
            playMedia();
        } else if (begin == 0) {
            updateMetaData();
            mediaPlayer.seekTo(currentDuration.getValue());
            updateCurrentDuration(-1, mediaPlayer.getCurrentPosition());
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
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);

            /*float volume = 1f;
            mediaPlayer.setVolume(volume, volume);
            equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
            equalizer.usePreset((short) 0);
            equalizer.setEnabled(true);*/
        } else if (mediaPlayer.isPlaying()) {
            stopMedia();
        }

        mediaPlayer.reset();
        Uri uri = getContentUri("external", fileId);
        DocumentFile sourceFile = DocumentFile.fromSingleUri(this, uri);
        if (sourceFile.exists()) {
            try {
                mediaPlayer.setDataSource(this, uri);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                onDestroy();
            }
        } else {
            skipToNext();
        }
    }

    public void updateColors() {
        //notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification("colors"));
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");
        if (mediaPlayer.isLooping()) {
            playMedia();
        } else {
            stopMedia();
            skipToNext();
        }
    }

    public void updateMetaData() {
        Log.d(TAG, "updateMetaData");
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicList.getCurrent().music.getMusic_title());
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicList.getCurrent().artistsToString());
        mediaMetadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, musicList.getCurrent().music.getMusic_album());
        mediaMetadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, musicList.getCurrent().music.getMusic_duration());

        MediaMetadataRetriever receiver = new MediaMetadataRetriever();
        receiver.setDataSource(this, Uri.fromFile(new File(musicList.getCurrent().music.getMusic_path())));
        BitmapDrawable icon = null;
        byte[] data = receiver.getEmbeddedPicture();
        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            icon = new BitmapDrawable(getResources(), bitmap);
        }
        mediaMetadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, icon.getBitmap());

        mediaSession.setMetadata(mediaMetadataBuilder.build());
        if (binder.isBinderAlive()) {
            if (musicList.getPointer() != -1)
                currentMusic.setValue(musicList.getCurrent());
        }
    }

    public void updateCurrentDuration(int state, int position) {
        int newState = state;
        if (state == -1 && mediaPlayer != null)
            newState = mediaPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        playbackStateBuilder.setState(newState, position, 1);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        currentDuration.setValue(position);
        playingState.setValue(newState == PlaybackStateCompat.STATE_PLAYING ? Constants.ACTION_PLAY : Constants.ACTION_PAUSE);
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle(description.getTitle());
        builder.setContentText(description.getSubtitle());
        builder.setLargeIcon(mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ART));

        Intent intent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setSmallIcon(R.drawable.round_music_note_24);
        builder.setColor(ColorsConstants.NOTIFICATION_BACKGROUND_COLOR);
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
    }

    public LiveData<Integer> getCurrentDuration() {
        return currentDuration;
    }

    public LiveData<String> getIsLoop() {
        return loopState;
    }

    public LiveData<String> getIsPlaying() {
        return playingState;
    }

    public LiveData<MusicWithArtists> getCurrentMusic() {
        return currentMusic;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static abstract class CustomTelephonyCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        public void onCallStateChanged(int i) {}
    }

    public class LocalBinder extends android.os.Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
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

    private final MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            Log.d(TAG, "MediaSession onPlay");
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                updateCurrentDuration(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition());
                if (!isNoisyAudioStreamReceiverRegistered) {
                    registerReceiver(myNoisyAudioStreamReceiver, becomeNoisyIntentFilter);
                    isNoisyAudioStreamReceiverRegistered = true;
                }
                if (begin != -1) {
                    updateMetaData();
                    //playingState.setValue(Constants.ACTION_PLAY);
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification());
                } else {
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification());
                }
                if (binder.isBinderAlive()) {
                    mHandler.postDelayed(updateTimeTask, 0);
                    Log.d(TAG, "mHandler = on");
                }
                begin = 1;
            }
        }

        @Override
        public void onPause() {
            Log.d(TAG, "MediaSession onPause");
            if (mediaPlayer.isPlaying()) {
                mHandler.removeCallbacks(updateTimeTask);
                Log.d(TAG, "mHandler = off");
                mediaPlayer.pause();
                updateCurrentDuration(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition());
                if (begin != -1) {
                    updateMetaData();
                    //playingState.setValue(Constants.ACTION_PAUSE);
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification());
                } else {
                    notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification());
                }
            }
        }

        @Override
        public void onStop() {
            Log.d(TAG, "MediaSession onStop");
            if (mediaPlayer.isPlaying()) {
                updateCurrentDuration(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition());
                Log.d(TAG, "mHandler = off");
                if (isNoisyAudioStreamReceiverRegistered) {
                    unregisterReceiver(myNoisyAudioStreamReceiver);
                    isNoisyAudioStreamReceiverRegistered = false;
                }
                mHandler.removeCallbacks(updateTimeTask);
            }
            currentDuration.setValue(0);
            mediaPlayer.stop();
        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG, "MediaSession onSkipToPrevious");
            begin = 1;
            musicList.goPrevious(loopState.getValue());
            if (loopState.getValue().equals(Constants.REPEAT_NONE))
                if (musicList.getPointer() == -1) {
                    begin = 0;
                    musicList.changeForIndex(musicList.size() - 1);
                }
            currentDuration.setValue(0);
            initMediaPlayer(musicList.getCurrent().music.getMusic_id());
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "MediaSession onSkipToNext " + begin);
            begin = begin == 1 ? 1 : 0;
            musicList.goNext(loopState.getValue());
            if (musicList.getPointer() == -1 && loopState.getValue().equals(Constants.REPEAT_NONE)) {
                begin = 0;
                musicList.changeForIndex(0);
            }
            currentDuration.setValue(0);
            initMediaPlayer(musicList.getCurrent().music.getMusic_id());
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            mediaPlayer.seekTo((int) pos);
            updateCurrentDuration(-1, (int) pos);
        }
    };
}