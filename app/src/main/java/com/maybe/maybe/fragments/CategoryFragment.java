package com.maybe.maybe.fragments;

import static com.maybe.maybe.CategoryItem.CATEGORY_ALBUM;
import static com.maybe.maybe.CategoryItem.CATEGORY_ARTIST;
import static com.maybe.maybe.CategoryItem.CATEGORY_FOLDER;
import static com.maybe.maybe.CategoryItem.CATEGORY_PLAYLIST;
import static com.maybe.maybe.CategoryItem.CATEGORY_SETTING;
import static com.maybe.maybe.CategoryItem.CATEGORY_SYNC;
import static com.maybe.maybe.utils.Constants.SORT_ALPHA;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.maybe.maybe.CategoryItem;
import com.maybe.maybe.ListItem;
import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.async_tasks.ArtistAsyncTask;
import com.maybe.maybe.database.async_tasks.FillDbAsyncTask;
import com.maybe.maybe.database.async_tasks.MusicAsyncTask;
import com.maybe.maybe.database.async_tasks.OnArtistAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnFillDbAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnSelectAlbumAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnSelectMusicAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskNull;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskNullResponse;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskObject;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskObjectResponse;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskPlaylistResponse;
import com.maybe.maybe.database.entity.ArtistWithMusics;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.database.entity.Playlist;
import com.maybe.maybe.utils.ColorsConstants;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment implements PlaylistAsyncTaskPlaylistResponse, PlaylistAsyncTaskNullResponse, OnFillDbAsyncTaskFinish, CategoriesFragment.CategoriesFragmentListener {

    private static final String TAG = "CategoryFragment";
    private CategoryFragmentListener callback;
    private AppDatabase appDatabase;
    private ArrayList<Music> addPlaylist;
    private ArrayList<MusicWithArtists> tempMusics;

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        //SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        //currentPlaylistName = sharedPref.getString(getString(R.string.current_cat), "All Musics");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        updateColors();

        CategoriesFragment fragment = CategoriesFragment.newInstance();
        fragment.setCallback(this);
        getParentFragmentManager().beginTransaction()
                .add(R.id.category_fragment_list, fragment, getString(R.string.categories_fragment_tag))
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();

        return view;
    }

    public void syncDatabase() {
        new FillDbAsyncTask(getContext()).execute(getContext(), this, appDatabase);
    }

    private void openPlaylists(CategoryItem categoryItem) {
        new PlaylistAsyncTaskObject().execute((PlaylistAsyncTaskObjectResponse) objects -> {
            addFragment(objects, categoryItem);
        }, appDatabase, "selectAllPlaylistWithCount");
    }

    private void openArtists(CategoryItem categoryItem) {
        new ArtistAsyncTask().execute((OnArtistAsyncTaskFinish) objects -> {
            ArrayList<ListItem> artists = new ArrayList<>();
            List<ArtistWithMusics> artistWithMusics = (List<ArtistWithMusics>) (Object) objects;
            for (ArtistWithMusics am : artistWithMusics) {
                artists.add(new ListItem(am.artist.getArtist_id(), am.artist.getArtist_name(), am.musics.size()));
            }
            addFragment(artists, categoryItem);
        }, appDatabase, "selectAllArtistWithMusics");
    }

    private void openAlbums(CategoryItem categoryItem) {
        new MusicAsyncTask().execute((OnSelectAlbumAsyncTaskFinish) objects -> {
            ArrayList<ListItem> albums = new ArrayList<>((List<ListItem>) (Object) objects);
            addFragment(albums, categoryItem);
        }, appDatabase, "selectAllAlbumWithCount");
    }

    private void openMusicsFrom(CategoryItem categoryItem, String name) {
        String select = "selectAll";
        if (categoryItem.getId() == CATEGORY_ARTIST)
            select = "selectAllMusicsOfArtist";
        else if (categoryItem.getId() == CATEGORY_ALBUM)
            select = "selectAllMusicsOfAlbum";
        else if (categoryItem.getId() == CATEGORY_PLAYLIST && !name.equals("All Musics"))
            select = "selectAllMusicsOfPlaylist";

        if (select.equals("selectAllMusicsOfPlaylist")) {
            new MusicAsyncTask().execute((OnSelectMusicAsyncTaskFinish) objects -> {
                ArrayList<MusicWithArtists> musics = new ArrayList<>((List<MusicWithArtists>) (Object) objects);
                if (tempMusics == null)
                    tempMusics = musics;
                else {
                    addFragmentEdit(musics, tempMusics, categoryItem, name);
                    tempMusics = null;
                }
            }, appDatabase, "selectAll", SORT_ALPHA, name);
            new MusicAsyncTask().execute((OnSelectMusicAsyncTaskFinish) objects -> {
                ArrayList<MusicWithArtists> musics = new ArrayList<>((List<MusicWithArtists>) (Object) objects);
                if (tempMusics == null)
                    tempMusics = musics;
                else {
                    addFragmentEdit(tempMusics, musics, categoryItem, name);
                    tempMusics = null;
                }
            }, appDatabase, select, SORT_ALPHA, name);
        } else {
            new MusicAsyncTask().execute((OnSelectMusicAsyncTaskFinish) objects -> {
                ArrayList<MusicWithArtists> musics = new ArrayList<>((List<MusicWithArtists>) (Object) objects);
                addFragmentEdit(musics, null, categoryItem, name);
            }, appDatabase, select, SORT_ALPHA, name);
        }
    }

    private void addFragment(ArrayList<ListItem> list, CategoryItem categoryItem) {
        ListsFragment fragment = ListsFragment.newInstance();
        fragment.setCallback(this);
        fragment.setList(list);
        fragment.setCategory(categoryItem);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.category_fragment_list, fragment, getString(R.string.lists_fragment_tag))
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

    private void addFragmentEdit(ArrayList<MusicWithArtists> listAll, ArrayList<MusicWithArtists> list, CategoryItem categoryItem, String name) {
        ListEditingFragment fragment = ListEditingFragment.newInstance();
        fragment.setCallback(this);
        fragment.setListAll(listAll);
        fragment.setList(list);
        fragment.setCategoryAndName(categoryItem, name);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.category_fragment_list, fragment, null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void back() {
        onBack();
    }

    @Override
    public void saveToList(ArrayList<Long> keyList, String name) {
        List<Playlist> playlists = new ArrayList<>();
        for (long l : keyList) {
            playlists.add(new Playlist(l, name));
        }
        new PlaylistAsyncTaskNull().execute(this, appDatabase, "updatePlaylist", playlists, name);
    }

    @Override
    public void changeList(int categoryId, String name) {
        callback.changeListInMain(categoryId, name);
        getParentFragmentManager().popBackStack(1, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        callback.swipeToMain();
    }

    @Override
    public void changeFragment(CategoryItem categoryItem, String name, boolean isEditable) {
        if (isEditable)
            openMusicsFrom(categoryItem, name);
        else if (categoryItem.getId() == CATEGORY_PLAYLIST)
            openPlaylists(categoryItem);
        else if (categoryItem.getId() == CATEGORY_ARTIST)
            openArtists(categoryItem);
        else if (categoryItem.getId() == CATEGORY_ALBUM)
            openAlbums(categoryItem);
        else if (categoryItem.getId() == CATEGORY_FOLDER)
            Toast.makeText(getContext(), "Not working yet!", Toast.LENGTH_SHORT).show();
        else if (categoryItem.getId() == CATEGORY_SETTING)
            callback.openSettings();
        else if (categoryItem.getId() == CATEGORY_SYNC)
            syncDatabase();
    }

    public boolean onBack() {
        FragmentManager fragmentManager = getParentFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 1)
            return false;
        fragmentManager.popBackStack();
        return true;
    }

    public void updateColors() {
        Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.custom_expandable);
        icon.setTint(ColorsConstants.SECONDARY_TEXT_COLOR);
    }

    public void addToPlaylist(ArrayList<Music> musics) {
        addPlaylist = musics;
    }

    public void resetList() {
        if (addPlaylist != null)
            addPlaylist.clear();
    }

    @Override
    public void onFillDbAsyncTaskFinish() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CategoryFragmentListener) {
            callback = (CategoryFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement CategoryFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    //When deleting/inserting musics in a playlist is finished
    @Override
    public void onPlaylistAsyncTaskNullFinish() {
        new PlaylistAsyncTaskObject().execute((PlaylistAsyncTaskObjectResponse) objects -> {
            ListsFragment fragment = ((ListsFragment) getParentFragmentManager().findFragmentByTag(getString(R.string.lists_fragment_tag)));
            fragment.setList(objects);
            fragment.updateRecyclerView();
            onBack();
        }, appDatabase, "selectAllPlaylistWithCount");
    }

    @Override
    public void onPlaylistAsyncTaskPlaylistFinish(List<Playlist> playlists) {}

    //COMMUNICATING
    public interface CategoryFragmentListener {
        void changeListInMain(int column, String category);

        void swipeToMain();

        void openSettings();
    }
}