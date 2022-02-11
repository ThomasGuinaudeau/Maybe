package com.maybe.maybe.fragments;

import static android.provider.MediaStore.Files.getContentUri;
import static com.maybe.maybe.utils.Constants.REPEAT_ALL;
import static com.maybe.maybe.utils.Constants.REPEAT_NONE;
import static com.maybe.maybe.utils.Constants.REPEAT_ONE;
import static com.maybe.maybe.utils.Constants.SORT_ALPHA;
import static com.maybe.maybe.utils.Constants.SORT_NUM;
import static com.maybe.maybe.utils.Constants.SORT_RANDOM;
import static com.maybe.maybe.utils.Constants.STATE_PAUSE;
import static com.maybe.maybe.utils.Constants.STATE_PLAY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
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

import com.maybe.maybe.utils.ColorsConstants;
import com.maybe.maybe.CycleStateResource;
import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskPlaylist;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskPlaylistResponse;
import com.maybe.maybe.database.entity.Artist;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.database.entity.Playlist;
import com.maybe.maybe.databinding.FragmentPlayerBinding;

import java.util.ArrayList;
import java.util.List;

public class PlayerFragment extends Fragment implements PlaylistAsyncTaskPlaylistResponse {

    private static final String TAG = "PlayerFragment";
    private PlayerFragmentListener callback;
    private long totalDuration;
    private CycleStateResource shuffleCycle, repeatCycle, playPauseCycle;
    private FragmentPlayerBinding binding;
    private SeekValueBinding seekValueBinding;

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

        String[] states = new String[]{ REPEAT_ALL, REPEAT_ONE, REPEAT_NONE };
        int[] resources = new int[]{ R.drawable.round_repeat_24, R.drawable.round_repeat_one_24, R.drawable.ic_round_horizontal_rule_24 };
        repeatCycle = new CycleStateResource(states, resources);

        states = new String[]{ SORT_ALPHA, SORT_RANDOM, SORT_NUM };
        resources = new int[]{ R.drawable.round_sort_by_alpha_24, R.drawable.round_shuffle_24, R.drawable.round_plus_one_24 };
        shuffleCycle = new CycleStateResource(states, resources);
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        shuffleCycle.goToState(sharedPref.getString(getString(R.string.sort), SORT_ALPHA));

        states = new String[]{ STATE_PLAY, STATE_PAUSE };
        resources = new int[]{ R.drawable.round_play_arrow_24, R.drawable.round_pause_24 };
        playPauseCycle = new CycleStateResource(states, resources);

        seekValueBinding = new SeekValueBinding();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false);

        Music music = new Music(0, 0, "Title", "Album", 60000, "Title.mp3");
        ArrayList<Artist> artists = new ArrayList<>();
        artists.add(new Artist("Artist"));
        MusicWithArtists musicWithArtists = new MusicWithArtists(music, artists);
        totalDuration = music.getMusic_duration();
        binding.setMusicWithArtists(musicWithArtists);
        binding.setCurrentDuration(0);
        binding.setCurrentDurationStr(convertMilliToTime(0));
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
        binding.setDescCurrentDuration(getString(R.string.desc_current_duration, 0));
        binding.setDescTotalDuration(getString(R.string.desc_total_duration, musicWithArtists.music.getMusic_duration()));
        binding.setDescPlaylists(getString(R.string.desc_playlists, ""));
        binding.setPlaylists("");
        return binding.getRoot();
    }

    public void updateColors() {
        binding.setPrimaryTextColor(ColorsConstants.PRIMARY_TEXT_COLOR);
        binding.setSecondaryTextColor(ColorsConstants.SECONDARY_TEXT_COLOR);
        binding.setSecondaryColor(ColorsConstants.SECONDARY_COLOR);
        binding.setSecondaryDarkColor(ColorsConstants.SECONDARY_DARK_COLOR);//was secondarydarktransparent
    }

    public void onClick(View v) {
        if (v.getId() == R.id.player_repeat) {
            repeatCycle.goNext();
            binding.setRepeat(repeatCycle.getResource());
            callback.action("loop", repeatCycle.getState());
        } else if (v.getId() == R.id.player_shuffle) {
            shuffleCycle.goNext();
            binding.setShuffle(shuffleCycle.getResource());
            callback.action("sort", shuffleCycle.getState());
        } else if (v.getId() == R.id.player_play)
            callback.action("state", "play_pause");
        else if (v.getId() == R.id.player_previous)
            callback.action("skip", "previous");
        else if (v.getId() == R.id.player_next)
            callback.action("skip", "next");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainFragment.MainFragmentListener) {
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
        totalDuration = musicWithArtists.music.getMusic_duration();
        binding.setMusicWithArtists(musicWithArtists);
        binding.setDescTitle(getString(R.string.desc_title, musicWithArtists.music.getMusic_title()));
        binding.setDescArtist(getString(R.string.desc_artist, musicWithArtists.artistsToString()));
        binding.setDescAlbum(getString(R.string.desc_album, musicWithArtists.music.getMusic_album()));
        binding.setDescTotalDuration(getString(R.string.desc_total_duration, musicWithArtists.music.getMusic_duration()));

        MediaMetadataRetriever receiver = new MediaMetadataRetriever();
        receiver.setDataSource(getContext(), getContentUri("external", musicWithArtists.music.getMusic_id()));
        byte[] data = receiver.getEmbeddedPicture();
        if (data != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            binding.setImage(new BitmapDrawable(getContext().getResources(), bitmap));
        } else
            binding.setImage(null);

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

    public void updateDuration(long currentDuration) {
        binding.setCurrentDuration((int) (currentDuration * 1000 / totalDuration));
        binding.setCurrentDurationStr(convertMilliToTime(currentDuration));
        binding.setDescCurrentDuration(getString(R.string.desc_current_duration, currentDuration / 60000));
    }

    @SuppressLint("DefaultLocale")
    public String convertMilliToTime(long milliseconds) {
        long second = (milliseconds / 1000) % 60;
        int minute = (int) (milliseconds / (1000 * 60)) % 60;
        int hour = (int) (milliseconds / (1000 * 60 * 60)) % 24;
        String time;
        if (totalDuration >= 3600000)
            time = String.format("%02d:%02d:%02d", hour, minute, second);
        else
            time = String.format("%02d:%02d", minute, second);
        return time;
    }

    public void updatePlayPause(boolean isPlaying) {
        if (isPlaying)
            playPauseCycle.goToState(STATE_PAUSE);
        else
            playPauseCycle.goToState(STATE_PLAY);
        binding.setState(playPauseCycle.getResource());
    }

    public void disableButtons(boolean buttonEnable) {
        binding.setEnable(buttonEnable);
    }

    public interface PlayerFragmentListener {
        void action(String action, String value);
    }

    public class SeekValueBinding {
        //public ObservableInt seekBarValue = new ObservableInt();

        public void onValueChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
            if (fromUser) {
                callback.action("seekBar", (int) (progresValue * totalDuration / 1000) + "");
            }
        }
    }
}