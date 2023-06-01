package com.maybe.maybe.fragments.main;

import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_ALBUM;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_ARTIST;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_PLAYLIST;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.async_tasks.MusicAsyncTask;
import com.maybe.maybe.database.async_tasks.OnSaveCurrentListAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnSearchMusicAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnSelectMusicAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.SaveCurrentListAsyncTask;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.fragments.main.service.MediaPlayerService;
import com.maybe.maybe.utils.ColorsConstants;
import com.maybe.maybe.utils.Constants;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements View.OnClickListener, OnMusicListItemClick, OnSelectMusicAsyncTaskFinish, OnSaveCurrentListAsyncTaskFinish, TextWatcher {

    private static final String TAG = "MainFragment";
    private MainFragmentListener callback;
    private MainRecyclerViewAdapter adapter;
    private FastScrollRecyclerView mainRecyclerView;
    private TextView main_title;
    private EditText main_search_edit;
    private Button main_search_button;
    private ProgressBar main_progress_bar;
    private AppDatabase appDatabase;
    private boolean searchEnable = false, buttonEnable = false, isStart = true;
    private String sort, currentName;
    private int currentCategoryId;
    private boolean isBroadcastReceiverRegistered;
    private ArrayList<Long> searchIdList;
    private ArrayList<Music> selectedId;
    private int currentSearchId;
    private SpeedyLinearLayoutManager sllm;
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            Object[] objects = (Object[]) extras.get(Constants.BROADCAST_EXTRAS);
            switch (extras.getString(Constants.BROADCAST_DESTINATION)) {
                case "change_metadata":
                    MusicWithArtists musicWithArtists = (MusicWithArtists) objects[0];
                    callback.changeMusicInPlayer(musicWithArtists);
                        selectedId.clear();
                        selectedId.add(musicWithArtists.music);
                        setSelected(true);
                    break;
                case "change_duration":
                    callback.changeDurationInPlayer((long) (int) objects[0]);
                    break;
                case "change_state":
                    callback.changePlayPauseInPlayer((Boolean) objects[0]);
                    break;
            }
        }
    };

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());

        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        sort = sharedPref.getString(getString(R.string.sort), Constants.SORT_ALPHA);
        selectedId = new ArrayList<>();

        Intent playerIntent = new Intent(getContext(), MediaPlayerService.class);
        playerIntent.setAction(Constants.ACTION_CREATE_SERVICE);
        getContext().startForegroundService(playerIntent);

        registerReceiver(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mainRecyclerView = view.findViewById(R.id.main_recycler_view);
        mainRecyclerView.setPopupBgColor(ColorsConstants.SECONDARY_COLOR);
        mainRecyclerView.setThumbColor(ColorsConstants.SECONDARY_COLOR);
        mainRecyclerView.setThumbInactiveColor(ColorsConstants.SECONDARY_COLOR);
        sllm = new SpeedyLinearLayoutManager(getContext());
        sllm.setActivity(getActivity());
        mainRecyclerView.setLayoutManager(sllm);
        adapter = new MainRecyclerViewAdapter(this, new ArrayList<>());
        mainRecyclerView.setAdapter(adapter);
        mainRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                sllm.setHeight(mainRecyclerView.getHeight());
            }
        });

        main_title = (TextView) view.findViewById(R.id.main_title);
        main_search_edit = (EditText) view.findViewById(R.id.main_search_edit);
        main_search_button = (Button) view.findViewById(R.id.main_search_button);
        main_progress_bar = (ProgressBar) view.findViewById(R.id.main_progress_bar);

        updateColors();
        main_title.setContentDescription(currentName);
        main_title.setText(currentName);
        main_search_edit.addTextChangedListener(this);
        if (main_search_edit.isFocused()) {
            main_search_edit.clearFocus();
        }
        main_search_button.setText("-");
        main_search_button.setOnClickListener(this);

        changeList(0, "", true);
        return view;
    }

    public void updateColors() {
        main_title.setBackgroundColor(ColorsConstants.PRIMARY_COLOR);//was primaryColorTrans
        main_title.setTextColor(ColorsConstants.SECONDARY_TEXT_COLOR);//was expandableParent

        main_search_edit.setBackgroundColor(ColorsConstants.PRIMARY_COLOR);//was primaryColorTrans
        main_search_edit.setTextColor(ColorsConstants.SECONDARY_TEXT_COLOR);//was expandableParent
        main_search_edit.setHintTextColor(ColorsConstants.SECONDARY_TEXT_COLOR);//was expandableParent

        main_search_button.setBackgroundColor(ColorsConstants.PRIMARY_COLOR);//was primaryColorTrans
        main_search_button.setTextColor(ColorsConstants.SECONDARY_TEXT_COLOR);
        main_progress_bar.setProgressTintList(new ColorStateList(new int[][]{ new int[]{ android.R.attr.state_enabled } }, new int[]{ ColorsConstants.SECONDARY_COLOR }));

        newServiceIntent(Constants.ACTION_UPDATE_COLORS);
    }

    @Override
    public void onItemClick(Music music) {
            sendBroadcast("change_music", music);
    }

    @Override
    public void onLongItemClick(Music music) {}

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (main_search_edit.isFocused()) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            main_search_edit.clearFocus();
        }
    }

    public void onAppForeground() {
        Log.d(TAG, "onAppForeground");
        registerReceiver(isBroadcastReceiverRegistered);
        newServiceIntent(Constants.ACTION_APP_FOREGROUND);
        if (Build.BRAND.equals("HUAWEI")) {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    main_search_edit.clearFocus();
                }
            });
            t.start();
        }
    }

    public void onAppBackground() {
        Log.d(TAG, "onAppBackground");
        newServiceIntent(Constants.ACTION_APP_BACKGROUND);
        unregisterReceiver();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        newServiceIntent(Constants.ACTION_END_SERVICE);
        unregisterReceiver();
    }

    private void newServiceIntent(String action) {
        Intent stopIntent = new Intent(getContext(), MediaPlayerService.class);
        stopIntent.setAction(action);
        getContext().startService(stopIntent);
    }

    private void registerReceiver(boolean isRegistered) {
        if (!isRegistered) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTION_TO_ACTIVITY));
            isBroadcastReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (isBroadcastReceiverRegistered) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
            isBroadcastReceiverRegistered = false;
        }
    }

    @SuppressLint("SetTextI18n")
    public void search() {
        if (searchEnable) {
            if (currentSearchId == searchIdList.size() - 1)
                currentSearchId = 0;
            else
                currentSearchId++;
            main_search_button.setText((currentSearchId + 1) + "/" + searchIdList.size() + " ➟");
            int position = adapter.getMusicPosition(searchIdList.get(currentSearchId));
            if (position != -1)
                smoothScroll(position);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_search_button) {
            search();
        }
    }

    private void setSelected(boolean scroll) {
        ArrayList<Integer> positions = new ArrayList<>();
        for (Music m : selectedId) {
            positions.add(adapter.getMusicPosition(m.getMusic_id()));
            Log.e(TAG, m.getMusic_title());
        }
        adapter.setSelectedPos(positions);
        adapter.notifyDataSetChanged();
        if (scroll && positions.size() > 0) {
            smoothScroll(positions.get(0));
        }
    }

    private void smoothScroll(int position) {
        if (position != -1) {
            sllm.setChildCount(mainRecyclerView.getChildCount());
            int currentPosition = ((LinearLayoutManager) mainRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            sllm.setPosDiff(Math.abs(position - currentPosition));
            mainRecyclerView.smoothScrollToPosition(position);
        }
    }

    public void changeList(int categoryId, String name, boolean isFirstLoad) {
        currentCategoryId = categoryId;
        currentName = name;

        String query = "";
        if (isFirstLoad) {
            SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String savedName = sharedPref.getString(getString(R.string.current_name), "<unknown>");
            if (savedName.equals("<unknown>")) {
                query = "selectAll";
                currentCategoryId = CATEGORY_PLAYLIST;
                currentName = "All Musics";
            } else {
                query = "selectAllMusicsOfCurrentPlaylist";
                currentCategoryId = sharedPref.getInt(getString(R.string.current_category_id), CATEGORY_PLAYLIST);
                currentName = savedName;
            }
        } else if (categoryId == CATEGORY_PLAYLIST) {
            if (name.equals("All Musics"))
                query = "selectAll";
            else
                query = "selectAllMusicsOfPlaylist";
        } else if (categoryId == CATEGORY_ARTIST) {
            query = "selectAllMusicsOfArtist";
        } else if (categoryId == CATEGORY_ALBUM) {
            query = "selectAllMusicsOfAlbum";
        }
        //Log.d(TAG, "category=" + currentCategoryId + " name=" + currentName + " sort=" + sort);
        new MusicAsyncTask().execute(this, appDatabase, query, sort, name);
    }

    @Override
    public void onSelectMusicAsyncFinish(List<Object> objects) {
        List<MusicWithArtists> musicWithArtists = (List<MusicWithArtists>) (Object) objects;
        if (musicWithArtists.size() > 0) {
            if (!buttonEnable) {
                buttonEnable = true;
                callback.disableButtons(true);
            }
            adapter.setMusics(musicWithArtists);
            adapter.setSort(sort);
            adapter.notifyDataSetChanged();
            main_title.setText(currentName);
            sendBroadcast("change_music_list", new ArrayList<>(musicWithArtists));

            new SaveCurrentListAsyncTask(musicWithArtists, this).execute(appDatabase, getContext(), currentCategoryId, currentName, sort, isStart);
        } else if (buttonEnable) {
            buttonEnable = false;
            callback.disableButtons(false);
        }
        isStart = false;
    }

    //recieved from CategoryFragment
    public void change(int categoryId, String category) {
        changeList(categoryId, category, false);
    }

    public void change2(int categoryId, String category) {
        if (category.equals("All Musics"))
            sort = Constants.SORT_ALPHA;
        changeList(categoryId, category, false);
    }

    public void action(String action, String value) {
        if (action.equals("sort")) {
            sort = value;
            change(currentCategoryId, currentName);
        }
        sendBroadcast("action", action, value);
    }

    @Override
    public void onSaveCurrentListAsyncTaskStart(int max) {
        main_progress_bar.setMax(max);
    }

    @Override
    public void onSaveCurrentListAsyncTaskProgress(int progress) {
        main_progress_bar.setProgress(progress);
    }

    @Override
    public void onSaveCurrentListAsyncTaskFinish() {
        main_progress_bar.setProgress(0);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainFragmentListener) {
            callback = (MainFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement MainFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    private void sendBroadcast(String destination, Object... objects) {
        Intent new_intent = new Intent(Constants.ACTION_TO_SERVICE);
        new_intent.putExtra(Constants.BROADCAST_DESTINATION, destination);
        new_intent.putExtra(Constants.BROADCAST_EXTRAS, objects);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new_intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String search = s.toString();
        if (!search.isEmpty()) {
            new MusicAsyncTask().execute((OnSearchMusicAsyncTaskFinish) objects -> {
                List<Long> longs = (List<Long>) (Object) objects;
                searchIdList = new ArrayList<>(longs);
                if (!searchIdList.isEmpty()) {
                    searchEnable = true;
                    currentSearchId = -1;
                    search();
                } else {
                    searchEnable = false;
                    main_search_button.setText("-");
                }
            }, appDatabase, "selectAllIdsByTitle", search);
        } else {
            searchEnable = false;
            main_search_button.setText("-");
        }
    }

    //COMMUNICATING
    public interface MainFragmentListener {
        void changeMusicInPlayer(MusicWithArtists musicWithArtists);

        void changeDurationInPlayer(long currentDuration);

        void addToPlaylist(ArrayList<Music> musics);

        void changePlayPauseInPlayer(boolean isPlaying);

        void resetList();

        void disableButtons(boolean enable);
    }
}