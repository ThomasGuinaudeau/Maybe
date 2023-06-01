package com.maybe.maybe.fragments;

import static com.maybe.maybe.CategoryItem.CATEGORY_ALBUM;
import static com.maybe.maybe.CategoryItem.CATEGORY_ARTIST;
import static com.maybe.maybe.CategoryItem.CATEGORY_FOLDER;
import static com.maybe.maybe.CategoryItem.CATEGORY_PLAYLIST;
import static com.maybe.maybe.CategoryItem.CATEGORY_SETTING;
import static com.maybe.maybe.CategoryItem.CATEGORY_SYNC;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maybe.maybe.CategoryItem;
import com.maybe.maybe.R;
import com.maybe.maybe.adapters.CategoryGridAdapter;

import java.util.ArrayList;

public class CategoriesFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "CategoriesFragment";
    private GridView gridView;
    private ArrayList<CategoryItem> categoryList;
    private CategoriesFragmentListener callback;

    public static CategoriesFragment newInstance() {
        return new CategoriesFragment();
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryList = new ArrayList<CategoryItem>();
        categoryList.add(new CategoryItem(CATEGORY_PLAYLIST, "Playlists", R.drawable.ic_round_queue_music_24));
        categoryList.add(new CategoryItem(CATEGORY_ARTIST, "Artists", R.drawable.ic_round_person_24));
        categoryList.add(new CategoryItem(CATEGORY_ALBUM, "Albums", R.drawable.ic_round_album_24));
        categoryList.add(new CategoryItem(CATEGORY_FOLDER, "Folders", R.drawable.ic_round_folder_24));
        categoryList.add(new CategoryItem(CATEGORY_SETTING, "Settings", R.drawable.ic_round_settings_24));
        categoryList.add(new CategoryItem(CATEGORY_SYNC, "Sync Musics", R.drawable.ic_round_sync_24));
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        CategoryGridAdapter gridAdapter = new CategoryGridAdapter(getContext(), R.layout.grid_row_item, categoryList);
        gridView = view.findViewById(R.id.category_grid_view);
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        adapterView.getItemAtPosition(i);
        callback.changeFragment((CategoryItem) adapterView.getItemAtPosition(i), "", false);
    }

    public void setCallback(CategoriesFragmentListener callback) {
        this.callback = callback;
    }

    public interface CategoriesFragmentListener {
        //void changeFragmentToCategory(int categoryIndex, String which, String name);
        //void changeFragmentToMusicList(int categoryIndex, String which, String name);
        void changeFragment(CategoryItem categoryItem, String name, boolean isEditable);

        void changeList(int categoryId, String name);

        void saveToList(ArrayList<Long> keyList, String name);

        void exportPlaylist(ArrayList<Long> keyList, String name);

        void importPlaylist();

        void back();

        /*default void changeFragment(int categoryIndex) {
            changeFragment(categoryIndex, "", "");
        }*/
    }
}
