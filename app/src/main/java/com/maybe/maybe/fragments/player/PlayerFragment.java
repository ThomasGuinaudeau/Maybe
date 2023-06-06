package com.maybe.maybe.fragments.player;

import static android.provider.MediaStore.Files.getContentUri;
import static com.maybe.maybe.utils.Constants.ACTION_PAUSE;
import static com.maybe.maybe.utils.Constants.ACTION_PLAY;
import static com.maybe.maybe.utils.Constants.REPEAT_ALL;
import static com.maybe.maybe.utils.Constants.REPEAT_NONE;
import static com.maybe.maybe.utils.Constants.REPEAT_ONE;
import static com.maybe.maybe.utils.Constants.SORT_ALPHA;
import static com.maybe.maybe.utils.Constants.SORT_NUM;
import static com.maybe.maybe.utils.Constants.SORT_RANDOM;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskPlaylist;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskPlaylistResponse;
import com.maybe.maybe.database.entity.Artist;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.database.entity.Playlist;
import com.maybe.maybe.databinding.FragmentPlayerBinding;
import com.maybe.maybe.fragments.main.service.MediaPlayerService;
import com.maybe.maybe.utils.ColorsConstants;
import com.maybe.maybe.utils.Constants;
import com.maybe.maybe.utils.CycleStateResource;
import com.maybe.maybe.utils.Methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerFragment extends Fragment implements PlaylistAsyncTaskPlaylistResponse {

    private static final String TAG = "PlayerFragment";
    private int totalDuration;
    private CycleStateResource shuffleCycle, repeatCycle, playPauseCycle;
    private FragmentPlayerBinding binding;
    private SeekValueBinding seekValueBinding;
    private PlayerFragmentListener callback;
    private boolean mBound = false;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBound = true;
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) iBinder;
            MediaPlayerService mediaPlayerService = binder.getService();

            mediaPlayerService.getCurrentDuration().observe(getActivity(), currentDuration -> updateDuration(currentDuration));
            mediaPlayerService.getIsLoop().observe(getActivity(), loopState -> changeState(repeatCycle, loopState));
            mediaPlayerService.getIsPlaying().observe(getActivity(), playingState -> changePlayingState(playingState));
            mediaPlayerService.getCurrentMusic().observe(getActivity(), musicWithArtists -> changeMusic(musicWithArtists));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //Unbinding works but not but onServiceDisconnected() is not called.
            mBound = false;
        }
    };

    public static PlayerFragment newInstance() {
        return new PlayerFragment();
    }

    @BindingAdapter({ "android:src" })
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter("customtint")
    public static void setImageTint(ImageView imageView, int color) {
        imageView.setColorFilter(color);
    }

    @BindingAdapter("android:progressBackgroundTint")
    public static void setProgressBackgroundTint(SeekBar seekBarView, int color) {
        seekBarView.setProgressBackgroundTintList(new ColorStateList(new int[][]{ new int[]{ android.R.attr.state_enabled } }, new int[]{ color }));
    }

    @BindingAdapter("android:progressTint")
    public static void setProgressTint(SeekBar seekBarView, int color) {
        seekBarView.setProgressTintList(new ColorStateList(new int[][]{ new int[]{ android.R.attr.state_enabled } }, new int[]{ color }));
    }

    @BindingAdapter("android:thumbTint")
    public static void setThumbTint(SeekBar seekBarView, int color) {
        seekBarView.setThumbTintList(new ColorStateList(new int[][]{ new int[]{ android.R.attr.state_enabled } }, new int[]{ color }));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindToService();
        Intent playerIntent = new Intent(getContext(), MediaPlayerService.class);
        playerIntent.setAction(Constants.ACTION_CREATE_SERVICE);
        getContext().startForegroundService(playerIntent);

        String[] states = new String[]{ REPEAT_ALL, REPEAT_ONE, REPEAT_NONE };
        int[] resources = new int[]{ R.drawable.round_repeat_24, R.drawable.round_repeat_one_24, R.drawable.ic_round_horizontal_rule_24 };
        repeatCycle = new CycleStateResource(states, resources);

        states = new String[]{ SORT_ALPHA, SORT_RANDOM, SORT_NUM };
        resources = new int[]{ R.drawable.round_sort_by_alpha_24, R.drawable.round_shuffle_24, R.drawable.round_plus_one_24 };
        shuffleCycle = new CycleStateResource(states, resources);
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        shuffleCycle.goToState(sharedPref.getString(getString(R.string.sort), SORT_ALPHA));

        states = new String[]{ ACTION_PLAY, ACTION_PAUSE };
        resources = new int[]{ R.drawable.round_play_arrow_24, R.drawable.round_pause_24 };
        playPauseCycle = new CycleStateResource(states, resources);

        seekValueBinding = new SeekValueBinding();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false);

        Music music = new Music(0, 0, "Title", "Album", 60000, "Title.mp3", "path/Title.mp3");
        ArrayList<Artist> artists = new ArrayList<>();
        artists.add(new Artist("Artist"));
        MusicWithArtists musicWithArtists = new MusicWithArtists(music, artists);
        totalDuration = (int) music.getMusic_duration();
        binding.setMusicWithArtists(musicWithArtists);
        binding.setTotalDuration(totalDuration / 1000);
        int[] timeArray = convertMilliToTime(totalDuration);
        binding.setTotalDurationStr(formatTime(timeArray));
        binding.setDescTotalDuration(getString(R.string.desc_total_duration, timeArray[0], timeArray[1], timeArray[2]));
        updateDuration(0);
        binding.setImage(null);
        binding.setState(playPauseCycle.getResource());
        binding.setPrevious(R.drawable.round_skip_previous);
        binding.setNext(R.drawable.round_skip_next);
        binding.setRepeat(repeatCycle.getResource());
        binding.setShuffle(shuffleCycle.getResource());
        updateColors();
        binding.setFragment(this);
        binding.setSeekValue(seekValueBinding);
        binding.setEnable(false);
        binding.setDescTitle(getString(R.string.desc_title, musicWithArtists.music.getMusic_title()));
        binding.setDescArtist(getString(R.string.desc_artist, musicWithArtists.artistsToString()));
        binding.setDescAlbum(getString(R.string.desc_album, musicWithArtists.music.getMusic_album()));
        binding.setDescPlaylists(getString(R.string.desc_playlists, ""));
        binding.setPlaylists("");

        return binding.getRoot();
    }

    public void onAppForeground() {
        Methods.newServiceIntent(getContext(), Constants.ACTION_APP_FOREGROUND, null);
        bindToService();
    }

    public void onAppBackground() {
        unbindToService();
        Methods.newServiceIntent(getContext(), Constants.ACTION_APP_BACKGROUND, null);
    }

    private void bindToService() {
        if (!mBound) {
            mBound = true;
            Intent playerIntent = new Intent(getContext(), MediaPlayerService.class);
            getContext().bindService(playerIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindToService() {
        if (mBound) {
            mBound = false;
            getContext().unbindService(connection);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Methods.newServiceIntent(getContext(), Constants.ACTION_END_SERVICE, null);
    }

    public void updateColors() {
        binding.setPrimaryTextColor(ColorsConstants.PRIMARY_TEXT_COLOR);
        binding.setSecondaryTextColor(ColorsConstants.SECONDARY_TEXT_COLOR);
        binding.setSecondaryColor(ColorsConstants.SECONDARY_COLOR);
        binding.setSecondaryDarkColor(ColorsConstants.SECONDARY_DARK_COLOR);//was secondarydarktransparent
    }

    public void onClick(View v) {
        String action = null;
        if (v.getId() == R.id.player_repeat) {
            action = changeState(repeatCycle, null);
        } else if (v.getId() == R.id.player_shuffle) {
            callback.changeListOrder(changeState(shuffleCycle, null));
            return;
        } else if (v.getId() == R.id.player_play) {
            action = Constants.ACTION_PLAY_PAUSE;
            changePlayingState(playPauseCycle.getState());
        } else if (v.getId() == R.id.player_previous) action = Constants.ACTION_PREVIOUS;
        else if (v.getId() == R.id.player_next) action = Constants.ACTION_NEXT;
        if (action != null) Methods.newServiceIntent(getContext(), action, null);
    }

    private String changeState(CycleStateResource cycle, String state) {
        if (state == null) cycle.goNext();
        else cycle.goToState(state);

        if (cycle == repeatCycle) binding.setRepeat(repeatCycle.getResource());
        else if (cycle == shuffleCycle) binding.setShuffle(shuffleCycle.getResource());
        return (cycle.getState());
    }

    private void changePlayingState(String previousState) {
        if (previousState.equals(Constants.ACTION_PLAY))
            playPauseCycle.goToState(Constants.ACTION_PAUSE);
        else playPauseCycle.goToState(Constants.ACTION_PLAY);
        binding.setState(playPauseCycle.getResource());
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

    //recieved from CategoryFragment
    public void changeMusic(MusicWithArtists musicWithArtists) {
        totalDuration = (int) musicWithArtists.music.getMusic_duration();
        binding.setTotalDuration(totalDuration / 1000);
        int[] timeArray = convertMilliToTime(totalDuration);
        binding.setTotalDurationStr(formatTime(timeArray));
        binding.setDescTotalDuration(getString(R.string.desc_total_duration, timeArray[0], timeArray[1], timeArray[2]));

        binding.setMusicWithArtists(musicWithArtists);
        binding.setDescTitle(getString(R.string.desc_title, musicWithArtists.music.getMusic_title()));
        binding.setDescArtist(getString(R.string.desc_artist, musicWithArtists.artistsToString()));
        binding.setDescAlbum(getString(R.string.desc_album, musicWithArtists.music.getMusic_album()));

        MediaMetadataRetriever receiver = new MediaMetadataRetriever();
        receiver.setDataSource(getContext(), getContentUri("external", musicWithArtists.music.getMusic_id()));
        byte[] data = receiver.getEmbeddedPicture();
        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            binding.setImage(new BitmapDrawable(getContext().getResources(), bitmap));
        } else binding.setImage(null);

        callback.changeCurrentMusic(musicWithArtists.music.getMusic_id());

        AppDatabase appDatabase = AppDatabase.getInstance(getContext());
        new PlaylistAsyncTaskPlaylist().execute(this, appDatabase, "selectAllPlaylistsOfId", musicWithArtists.music.getMusic_id());
    }

    @Override
    public void onPlaylistAsyncTaskPlaylistFinish(List<Playlist> playlists) {
        String playlistStr = "";
        String descPlaylistsStr = "";
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
                Bundle bundle = new Bundle();
                bundle.putInt(getString(R.string.key_parcelable_data), (int) (progressValue * 1000));
                Methods.newServiceIntent(getContext(), Constants.ACTION_SEEK_TO, bundle);
            }
        }
    }
}