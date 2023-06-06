package com.maybe.maybe.fragments.main.service;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.maybe.maybe.R;
import com.maybe.maybe.utils.ColorsConstants;

import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String TAG = "MediaPlaybackService";
    private static final String CHANNEL_ID = "musicChannelId";
    private static final int NOTIFICATION_ID = 1338;

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat controller;
    private MediaMetadataCompat mediaMetadata;
    private MediaDescriptionCompat description;
    private PlaybackStateCompat.Builder stateBuilder;
    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
    private MediaBrowserService service;
    private MediaPlayer mediaPlayer;
    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private MediaSessionCompat.Callback callback = new
            MediaSessionCompat.Callback() {
                @Override
                public void onPlay() {
                    mediaSession.setActive(true);
                    mediaPlayer.start();
                    registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
                    service.startForeground(NOTIFICATION_ID, buildNotification());
                }

                @Override
                public void onStop() {
                    unregisterReceiver(myNoisyAudioStreamReceiver);
                    mediaSession.setActive(false);
                    mediaPlayer.stop();
                    service.stopForeground(false);
                }

                @Override
                public void onPause() {
                    mediaPlayer.pause();
                    unregisterReceiver(myNoisyAudioStreamReceiver);
                    service.stopForeground(false);
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        mediaSession = new MediaSessionCompat(getApplicationContext(), TAG);
        stateBuilder = new PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setCallback(callback);
        controller = mediaSession.getController();
        mediaMetadata = controller.getMetadata();
        description = mediaMetadata.getDescription();
        setSessionToken(mediaSession.getSessionToken());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setContentTitle(description.getTitle());
        builder.setContentText(description.getSubtitle());
        builder.setSubText(description.getDescription());
        builder.setLargeIcon(description.getIconBitmap());
        builder.setContentIntent(controller.getSessionActivity());
        //builder.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_STOP));
        builder.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_STOP));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setSmallIcon(R.drawable.round_music_note_24);
        builder.setColor(ColorsConstants.NOTIFICATION_BACKGROUND_COLOR);
        builder.addAction(new NotificationCompat.Action(R.drawable.round_play_arrow_24, "pause", MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        //builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(0).setShowCancelButton(false));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(0).setShowCancelButton(false).setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_STOP)));
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
        //TODO https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice
        // https://developer.android.com/training/cars/media#build_hierarchy
        /*
        // Assume for example that the music catalog is already loaded/cached.
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);*/
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
}
