package com.maybe.maybe.fragments.player;

import static com.maybe.maybe.utils.Constants.REPEAT_ALL;
import static com.maybe.maybe.utils.Constants.REPEAT_NONE;
import static com.maybe.maybe.utils.Constants.REPEAT_ONE;
import static com.maybe.maybe.utils.Constants.SORT_ALPHA;
import static com.maybe.maybe.utils.Constants.SORT_NUM;
import static com.maybe.maybe.utils.Constants.SORT_RANDOM;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.entity.Artist;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.database.entity.Playlist;
import com.maybe.maybe.database.runnables.AnalyzeMusicRunnable;
import com.maybe.maybe.database.runnables.playlist.PlaylistRunnableNull;
import com.maybe.maybe.database.runnables.playlist.PlaylistRunnableObject;
import com.maybe.maybe.database.runnables.playlist.PlaylistRunnablePlaylist;
import com.maybe.maybe.databinding.FragmentPlayerBinding;
import com.maybe.maybe.fragments.category.ListItem;
import com.maybe.maybe.fragments.player.service.MediaPlayerService;
import com.maybe.maybe.utils.Constants;
import com.maybe.maybe.utils.CustomButton;
import com.maybe.maybe.utils.CycleStateResource;
import com.maybe.maybe.utils.Methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PlayerFragment extends Fragment {

    private static final String TAG = "PlayerFragment";
    private final Handler mHandler = new Handler();
    private int totalDuration, currentDuration;
    private boolean isFirstLoad = true;
    private ArrayList<Long> idList;
    private List<Playlist> partOfPlaylist;
    private long musicId;
    private double lufs;
    private CycleStateResource shuffleCycle, repeatCycle;
    private FragmentPlayerBinding binding;
    private SeekValueBinding seekValueBinding;
    private PlayerFragmentListener callback;
    private boolean isPlaying;
    private Runnable updateTimeTask;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    Log.d(TAG, "onMetadataChanged " + metadata.getDescription().getTitle());
                    binding.setTitle(metadata.getDescription().getTitle().toString());
                    binding.setArtist(metadata.getDescription().getSubtitle().toString());
                    binding.setAlbum(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
                    binding.setImage(new BitmapDrawable(getContext().getResources(), metadata.getDescription().getIconBitmap()));

                    totalDuration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);//(int) musicWithArtists.music.getMusic_duration();
                    binding.setTotalDuration(totalDuration / 1000);
                    int[] timeArray = convertMilliToTime(totalDuration);
                    binding.setTotalDurationStr(formatTime(timeArray));

                    binding.setDescTitle(metadata.getDescription().getTitle().toString());
                    binding.setDescArtist(metadata.getDescription().getSubtitle().toString());
                    binding.setDescAlbum(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
                    binding.setDescTotalDuration(getString(R.string.desc_total_duration, timeArray[0], timeArray[1], timeArray[2]));
                    lufs = Double.parseDouble(metadata.getString(Constants.METADATA_KEY_LUFS));
                    updateLufsIcon();

                    musicId = Long.parseLong(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
                    callback.changeCurrentMusic(musicId);
                    updatePlaylist(musicId);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    Log.d(TAG, "onPlaybackStateChanged" + state);
                    if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                        binding.setState(R.drawable.ic_round_pause_24);
                        isPlaying = true;
                        addRunnable();
                    } else {
                        binding.setState(R.drawable.ic_round_play_arrow_24);
                        removeRunnable();
                        isPlaying = false;
                    }
                    int position = (int) state.getPosition();
                    currentDuration = position;
                    updateDuration(position);
                }

                @Override
                public void onShuffleModeChanged(int shuffleMode) {
                    super.onShuffleModeChanged(shuffleMode);
                }

                @Override
                public void onSessionDestroyed() {
                    mediaBrowser.disconnect();
                }
            };
    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "CONNECTING");
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                    MediaControllerCompat mediaController = new MediaControllerCompat(getContext(), token);
                    MediaControllerCompat.setMediaController(getActivity(), mediaController);
                    buildTransportControls();

                    if (isFirstLoad && idList != null) {
                        sendListToService(idList);
                        idList = null;
                    } else {
                        Methods.newServiceIntent(getContext(), Constants.ACTION_APP_FOREGROUND, null);
                    }
                    isFirstLoad = false;
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                }
            };

    public static PlayerFragment newInstance() {
        return new PlayerFragment();
    }

    @BindingAdapter({ "android:src" })
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter({ "isVisible" })
    public static void setIsVisible(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "oncreate player");

        updateTimeTask = new Runnable() {
            public void run() {
                currentDuration += 500;
                updateDuration(currentDuration);
                mHandler.postDelayed(this, 500);
            }
        };

        Intent playerIntent = new Intent(getContext(), MediaPlayerService.class);
        playerIntent.setAction(Constants.ACTION_CREATE_SERVICE);
        getContext().startForegroundService(playerIntent);

        mediaBrowser = new MediaBrowserCompat(getContext(), new ComponentName(getContext(), MediaPlayerService.class), connectionCallbacks, null);

        String[] states = new String[]{ REPEAT_ALL, REPEAT_ONE, REPEAT_NONE };
        int[] resources = new int[]{ R.drawable.round_repeat_24, R.drawable.round_repeat_one_24, R.drawable.ic_round_horizontal_rule_24 };
        repeatCycle = new CycleStateResource(states, resources);

        states = new String[]{ SORT_ALPHA, SORT_RANDOM, SORT_NUM };
        resources = new int[]{ R.drawable.ic_round_sort_by_alpha_24, R.drawable.round_shuffle_24, R.drawable.round_plus_one_24 };
        shuffleCycle = new CycleStateResource(states, resources);
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        shuffleCycle.goToState(sharedPref.getString(getString(R.string.sort), SORT_ALPHA));

        seekValueBinding = new SeekValueBinding();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false);

        Music music = new Music(0, 0, "Title", "Album", 60000, "Title.mp3", "path/Title.mp3", "folder", 0);
        ArrayList<Artist> artists = new ArrayList<>();
        artists.add(new Artist("Artist"));
        MusicWithArtists musicWithArtists = new MusicWithArtists(music, artists);
        totalDuration = (int) music.getMusic_duration();
        binding.setTotalDuration(totalDuration / 1000);
        int[] timeArray = convertMilliToTime(totalDuration);
        binding.setTotalDurationStr(formatTime(timeArray));
        binding.setDescTotalDuration(getString(R.string.desc_total_duration, timeArray[0], timeArray[1], timeArray[2]));
        updateDuration(0);
        binding.setTitle("Title");
        binding.setArtist("Artist");
        binding.setAlbum("Album");
        binding.setImage(null);
        binding.setState(R.drawable.ic_round_play_arrow_24);
        binding.setPrevious(R.drawable.round_skip_previous);
        binding.setNext(R.drawable.round_skip_next);
        binding.setRepeat(repeatCycle.getResource());
        binding.setShuffle(shuffleCycle.getResource());
        binding.setMenu(R.drawable.round_more_horiz_24);
        binding.setShowLoading(false);
        binding.setFragment(this);
        binding.setSeekValue(seekValueBinding);
        binding.setEnable(false);
        binding.setDescTitle(getString(R.string.desc_title, musicWithArtists.music.getMusic_title()));
        binding.setDescArtist(getString(R.string.desc_artist, musicWithArtists.artistsToString()));
        binding.setDescAlbum(getString(R.string.desc_album, musicWithArtists.music.getMusic_album()));
        binding.setDescPlaylists(getString(R.string.desc_playlists, ""));
        binding.setPlaylists("");
        lufs = 0;
        updateLufsIcon();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mediaBrowser.connect();
        addRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        addRunnable();

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(getActivity());
        int position = (int) mediaController.getPlaybackState().getPosition();
        currentDuration = position;
        updateDuration(position);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        removeRunnable();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (MediaControllerCompat.getMediaController(getActivity()) != null) {
            MediaControllerCompat.getMediaController(getActivity()).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
        removeRunnable();
    }

    void buildTransportControls() {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(getActivity());

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback);
    }

    private void addRunnable() {
        if (isPlaying && !mHandler.hasCallbacks(updateTimeTask))
            mHandler.postDelayed(updateTimeTask, 500);
    }

    private void removeRunnable() {
        if (mHandler.hasCallbacks(updateTimeTask))
            mHandler.removeCallbacks(updateTimeTask);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Methods.newServiceIntent(getContext(), Constants.ACTION_END_SERVICE, null);
        removeRunnable();
    }

    public void onClick(View v) {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(getActivity());
        switch (v.getId()) {
            case R.id.player_repeat:
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.key_parcelable_data), changeState(repeatCycle, null));
                MediaControllerCompat.getMediaController(getActivity()).sendCommand(Constants.ACTION_SORT, bundle, null);
                break;
            case R.id.player_shuffle:
                callback.changeListOrder(changeState(shuffleCycle, null));
                break;
            case R.id.player_play:
                int pbState = mediaController.getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.getTransportControls().pause();
                } else {
                    mediaController.getTransportControls().play();
                }
                break;
            case R.id.player_previous:
                mediaController.getTransportControls().skipToPrevious();
                break;
            case R.id.player_next:
                mediaController.getTransportControls().skipToNext();
                break;
            case R.id.player_menu:
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setForceShowIcon(true);

                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    AppDatabase appDatabase = AppDatabase.getInstance(getContext());
                    if (menuItem.getItemId() == R.id.player_menu_add_to_playlist) {
                        Executors.newSingleThreadExecutor().execute(new PlaylistRunnableObject(objects -> {
                            objects.remove(0);
                            // Remove playlist if it already contains the song to not have the option to add again to same playlist
                            Set<String> excludedPlaylists = partOfPlaylist.stream().map(Playlist::getPlaylist_name).collect(Collectors.toSet());
                            List<String> playlistsStr = objects.stream().map(ListItem::getName).filter(name -> !excludedPlaylists.contains(name)).collect(Collectors.toList());
                            Executor mainThreadExecutor = ContextCompat.getMainExecutor(getContext());
                            mainThreadExecutor.execute(() -> popupAddOrRemoveFromPlaylist(playlistsStr, Constants.PLAYLIST_ADD));
                        }, appDatabase));
                    } else if (menuItem.getItemId() == R.id.player_menu_remove_from_playlist) {
                        List<String> playlistsStr = partOfPlaylist.stream().map(Playlist::getPlaylist_name).collect(Collectors.toList());
                        popupAddOrRemoveFromPlaylist(playlistsStr, Constants.PLAYLIST_REMOVE);
                    } else if (menuItem.getItemId() == R.id.player_menu_analyze_loudness) {
                        binding.setShowLoading(true);
                        Executors.newSingleThreadExecutor().execute(new AnalyzeMusicRunnable(lufs1 -> {
                            lufs = lufs1;
                            updateLufsIcon();
                            binding.setShowLoading(false);
                            Executor mainThreadExecutor = ContextCompat.getMainExecutor(getContext());
                            mainThreadExecutor.execute(() -> Toast.makeText(getContext(), getString(lufs1 < 0 ? R.string.toast_analyzed_loudness_success : R.string.toast_analyzed_loudness_fail), Toast.LENGTH_SHORT).show());
                        }, appDatabase, musicId));
                    }
                    return true;
                });
                popupMenu.show();
        }
    }

    private void popupAddOrRemoveFromPlaylist(List<String> playlists, int type) {
        //Create AlertDialog to choose a playlist to add/remove current song
        playlists = playlists.stream().sorted().collect(Collectors.toList());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = builder.create();//Used to be able to dismiss the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.add_to_playlist_dialog, null);

        Spinner spinner = dialogView.findViewById(R.id.playlist_dialog_playlist_name);
        SpinnerAdapter adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, playlists);
        spinner.setAdapter(adapter);

        CustomButton btnAdd = dialogView.findViewById(R.id.playlist_dialog_add);
        if (playlists.isEmpty())
            btnAdd.setEnabled(false);

        if (type == Constants.PLAYLIST_ADD) {
            btnAdd.setIcon(R.drawable.ic_round_playlist_add_24);
            btnAdd.setText(getString(R.string.popup_btn_add));
        } else if (type == Constants.PLAYLIST_REMOVE) {
            btnAdd.setIcon(R.drawable.round_playlist_remove_24);
            btnAdd.setText(getString(R.string.popup_btn_remove));
        }
        btnAdd.setOnClickListener(v -> {
            String name = spinner.getSelectedItem().toString();
            List<Playlist> listOfPlaylist = new ArrayList<>();
            listOfPlaylist.add(new Playlist(musicId, name));
            Executors.newSingleThreadExecutor().execute(new PlaylistRunnableNull(() -> {
                updatePlaylist(musicId);
                Executor mainThreadExecutor = ContextCompat.getMainExecutor(getContext());
                mainThreadExecutor.execute(() -> Toast.makeText(getContext(), getString(type == Constants.PLAYLIST_ADD ? R.string.toast_add_song_to_playlist : R.string.toast_remove_song_from_playlist), Toast.LENGTH_SHORT).show());
            }, AppDatabase.getInstance(getContext()), name, listOfPlaylist, type));
            alertDialog.dismiss();
        });
        Button btnCancel = dialogView.findViewById(R.id.playlist_dialog_cancel);
        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    private String changeState(CycleStateResource cycle, String state) {
        if (state == null) cycle.goNext();
        else cycle.goToState(state);

        if (cycle == repeatCycle) binding.setRepeat(repeatCycle.getResource());
        else if (cycle == shuffleCycle) binding.setShuffle(shuffleCycle.getResource());
        return (cycle.getState());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlayerFragmentListener) {
            callback = (PlayerFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement MainFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    //Get which playlist contains this song
    private void updatePlaylist(long musicId) {
        AppDatabase appDatabase = AppDatabase.getInstance(getContext());
        Executors.newSingleThreadExecutor().execute(new PlaylistRunnablePlaylist(playlists -> {
            partOfPlaylist = playlists;
            String playlistStr = playlists.isEmpty() ? "" : "Part of playlist:\n";
            String descPlaylistsStr = "Part of playlist: ";
            for (int i = 0; i < playlists.size(); i++) {
                playlistStr += playlists.get(i).getPlaylist_name();
                descPlaylistsStr += playlists.get(i).getPlaylist_name();
                if (i != playlists.size() - 1) {
                    playlistStr += "\n";
                    descPlaylistsStr += ", ";
                }
            }
            binding.setDescPlaylists(getString(R.string.desc_playlists, descPlaylistsStr));
            binding.setPlaylists(playlistStr);
        }, appDatabase, "selectAllPlaylistsOfId", musicId));
    }

    public void updateDuration(int currentDuration) {
        binding.setCurrentDuration(currentDuration / 1000);
        int[] timeArray = convertMilliToTime(currentDuration);
        binding.setDescCurrentDuration(getString(R.string.desc_current_duration, timeArray[0], timeArray[1], timeArray[2]));
        binding.setCurrentDurationStr(formatTime(timeArray));
    }

    public int[] convertMilliToTime(int milliseconds) {
        int second = (milliseconds / 1000) % 60;
        int minute = (milliseconds / (1000 * 60)) % 60;
        int hour = (milliseconds / (1000 * 60 * 60)) % 24;
        return new int[]{ hour, minute, second };
    }

    public String formatTime(int[] timeArray) {
        String time;
        if (totalDuration >= 3600000)
            time = String.format(Locale.getDefault(), "%02d:%02d:%02d", timeArray[0], timeArray[1], timeArray[2]);
        else time = String.format(Locale.getDefault(), "%02d:%02d", timeArray[1], timeArray[2]);
        return time;
    }

    public void updateLufsIcon() {
        if (lufs < 0)
            binding.setLufs(R.drawable.round_eq_pass);
        else
            binding.setLufs(R.drawable.round_eq_fail);
    }

    public void updateListInService(ArrayList<Long> idList) {
        if (!isFirstLoad)
            sendListToService(idList);
        else
            this.idList = idList;
    }

    private void sendListToService(ArrayList<Long> idList) {
        long[] tempIdList = new long[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            tempIdList[i] = idList.get(i);
        }
        Bundle bundle = new Bundle();
        bundle.putLongArray(getString(R.string.key_parcelable_data), tempIdList);
        MediaControllerCompat.getMediaController(getActivity()).sendCommand(Constants.ACTION_CHANGE_LIST, bundle, null);
    }

    public void disableButtons(boolean buttonEnable) {
        binding.setEnable(buttonEnable);
    }

    public interface PlayerFragmentListener {
        void changeCurrentMusic(long id);

        void changeListOrder(String sort);
    }

    public class SeekValueBinding {
        public void onValueChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
            if (fromUser) {
                MediaControllerCompat.getMediaController(getActivity()).getTransportControls().seekTo((progressValue * 1000L));
            }
        }
    }
}