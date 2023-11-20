package com.maybe.maybe.fragments.main;

import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_ALBUM;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_ARTIST;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_FOLDER;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_PLAYLIST;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.color.MaterialColors;
import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.async_tasks.MusicAsyncTask;
import com.maybe.maybe.database.async_tasks.OnSaveCurrentListAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnSearchMusicAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnSelectMusicAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.SaveCurrentListAsyncTask;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.utils.Constants;
import com.maybe.maybe.utils.Methods;
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
    private int currentSearchId, currentCategoryId;
    private ArrayList<Long> searchIdList;
    private SpeedyLinearLayoutManager sllm;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());

        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        sort = sharedPref.getString(getString(R.string.sort), Constants.SORT_ALPHA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mainRecyclerView = view.findViewById(R.id.main_recycler_view);
        int colorSecondary = MaterialColors.getColor(getContext(), R.attr.colorSecondary, 0x00000000);
        mainRecyclerView.setPopupBgColor(colorSecondary);
        mainRecyclerView.setThumbColor(colorSecondary);
        mainRecyclerView.setThumbInactiveColor(colorSecondary);
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

        main_title.setContentDescription(currentName);
        main_title.setText(currentName);
        main_search_edit.addTextChangedListener(this);
        if (main_search_edit.isFocused()) {
            main_search_edit.clearFocus();
        }
        main_search_button.setText("-");
        main_search_button.setOnClickListener(this);

        updateList(0, "", null, true);
        return view;
    }

    @Override
    public void onItemClick(Music music) {
        changeCurrentMusic(music.getMusic_id());
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.key_parcelable_data), music);
        Methods.newServiceIntent(getContext(), Constants.ACTION_CHANGE_MUSIC, bundle);
    }

    @Override
    public void onLongItemClick(Music music) {}

    public void onAppForeground() {
        Log.d(TAG, "onAppForeground");
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

    private void smoothScroll(int position) {
        if (position != -1) {
            sllm.setChildCount(mainRecyclerView.getChildCount());
            int currentPosition = ((LinearLayoutManager) mainRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            sllm.setPosDiff(Math.abs(position - currentPosition));
            mainRecyclerView.smoothScrollToPosition(position + 7 < mainRecyclerView.getChildCount() ? position + 7 : position);
        }
    }

    public void changeList(boolean isFirstLoad) {
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
        } else if (currentCategoryId == CATEGORY_PLAYLIST) {
            if (currentName.equals("All Musics"))
                query = "selectAll";
            else
                query = "selectAllMusicsOfPlaylist";
        } else if (currentCategoryId == CATEGORY_ARTIST) {
            query = "selectAllMusicsOfArtist";
        } else if (currentCategoryId == CATEGORY_ALBUM) {
            query = "selectAllMusicsOfAlbum";
        } else if (currentCategoryId == CATEGORY_FOLDER) {
            query = "selectAllMusicsOfFolder";
        }
        Log.d(TAG, "category=" + currentCategoryId + " name=" + currentName + " sort=" + sort + " query=" + query);
        new MusicAsyncTask().execute(this, appDatabase, query, sort, currentName);
    }

    public void updateList(int categoryId, String name, String sort, boolean isFirstLoad) {
        Log.e(TAG, categoryId + " " + name + " " + sort + " " + isFirstLoad);
        currentCategoryId = categoryId != -1 ? categoryId : currentCategoryId;
        currentName = name != null ? name : currentName;
        this.sort = sort != null ? sort : this.sort;
        changeList(isFirstLoad);
    }

    public void changeCurrentMusic(long id) {
        adapter.setId(id);
        adapter.notifyDataSetChanged();
        int position = adapter.getMusicPosition(id);
        position = position != -1 ? position : 0;
        //adapter.setCurrentMusicPosition(position);
        smoothScroll(position);
    }

    @Override
    public void onSelectMusicAsyncFinish(List<Object> objects) {
        ArrayList<MusicWithArtists> musicWithArtists = (ArrayList<MusicWithArtists>) (Object) objects;
        if (musicWithArtists.size() > 0) {
            if (!buttonEnable) {
                buttonEnable = true;
                callback.disableButtons(true);
            }
            long currentMusicId = adapter.getId();
            Log.e(TAG, currentMusicId + "");
            adapter.setMusics(musicWithArtists);
            adapter.setSort(sort);
            changeCurrentMusic(currentMusicId);
            //adapter.notifyDataSetChanged();
            main_title.setText(currentName);

            ArrayList<Integer> idList = new ArrayList<>();
            musicWithArtists.forEach(item -> idList.add((int) item.music.getMusic_id()));

            callback.updateListInService(idList);

            new SaveCurrentListAsyncTask(musicWithArtists, this).execute(appDatabase, getContext(), currentCategoryId, currentName, sort, isStart);
        } else if (buttonEnable) {
            buttonEnable = false;
            callback.disableButtons(false);
        }
        isStart = false;
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
            throw new RuntimeException(context + " must implement MainFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
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
                    adapter.setFoundIds(searchIdList);
                    adapter.notifyDataSetChanged();
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

        void updateListInService(ArrayList<Integer> idList);

        void disableButtons(boolean enable);
    }
}