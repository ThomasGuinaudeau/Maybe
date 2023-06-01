package com.maybe.maybe.fragments.category;

import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_ALBUM;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_ARTIST;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_FOLDER;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_PLAYLIST;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_SETTING;
import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_SYNC;
import static com.maybe.maybe.utils.Constants.SORT_ALPHA;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.maybe.maybe.ListItem;
import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.async_tasks.ArtistAsyncTask;
import com.maybe.maybe.database.async_tasks.FillDbAsyncTask;
import com.maybe.maybe.database.async_tasks.MusicAsyncTask;
import com.maybe.maybe.database.async_tasks.OnArtistAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnFillDbAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnSearchMusicAsyncTaskFinish;
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
import com.maybe.maybe.fragments.category.editing.ListEditingFragment;
import com.maybe.maybe.fragments.category.grid.CategoryGridFragment;
import com.maybe.maybe.fragments.category.list.ListsFragment;
import com.maybe.maybe.utils.ColorsConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CategoryFragment extends Fragment implements PlaylistAsyncTaskPlaylistResponse, PlaylistAsyncTaskNullResponse, OnFillDbAsyncTaskFinish, CategoryGridFragment.CategoriesFragmentListener, ActivityResultCallback<ActivityResult> {

    private static final String TAG = "CategoryFragment";
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
    private CategoryFragmentListener callback;
    private AppDatabase appDatabase;
    private ArrayList<Music> addPlaylist;
    private ArrayList<MusicWithArtists> tempMusicsEditList;
    private ArrayList<MusicWithArtists> tempMusicsExportList;
    private String tempPlaylistExportName;
    private String currentAction;
    private String playlistUpdateAction;

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

        CategoryGridFragment fragment = CategoryGridFragment.newInstance();
        fragment.setCallback(this);
        getParentFragmentManager().beginTransaction()
                .add(R.id.category_fragment_list, fragment, getString(R.string.categories_fragment_tag))
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityResultLauncher.unregister();
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
                if (tempMusicsEditList == null)
                    tempMusicsEditList = musics;
                else {
                    addFragmentEdit(musics, tempMusicsEditList, categoryItem, name);
                    tempMusicsEditList = null;
                }
            }, appDatabase, "selectAll", SORT_ALPHA, name);
            new MusicAsyncTask().execute((OnSelectMusicAsyncTaskFinish) objects -> {
                ArrayList<MusicWithArtists> musics = new ArrayList<>((List<MusicWithArtists>) (Object) objects);
                if (tempMusicsEditList == null)
                    tempMusicsEditList = musics;
                else {
                    addFragmentEdit(tempMusicsEditList, musics, categoryItem, name);
                    tempMusicsEditList = null;
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
        playlistUpdateAction = "back";
        new PlaylistAsyncTaskNull().execute(this, appDatabase, "updatePlaylist", playlists, name);
    }

    @Override
    public void exportPlaylist(ArrayList<Long> keyList, String name) {
        tempPlaylistExportName = name;
        tempMusicsExportList = new ArrayList<>();
        new MusicAsyncTask().execute((OnSelectMusicAsyncTaskFinish) objects -> {
            ArrayList<MusicWithArtists> musics = new ArrayList<>((List<MusicWithArtists>) (Object) objects);
            musics.forEach(item -> {
                if (keyList.contains(item.music.getMusic_id()))
                    tempMusicsExportList.add(item);
            });

            currentAction = Intent.ACTION_CREATE_DOCUMENT;
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/x-mpegurl");
            intent.putExtra(Intent.EXTRA_TITLE, name);
            activityResultLauncher.launch(intent);
        }, appDatabase, "selectAll", SORT_ALPHA);
    }

    @Override
    public void importPlaylist() {
        currentAction = Intent.ACTION_OPEN_DOCUMENT;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, file.getPath());
        activityResultLauncher.launch(intent);
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
            Toast.makeText(getContext(), R.string.toast_folder_category, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onPlaylistAsyncTaskNullFinish() {
        //When deleting/inserting musics in a playlist, update the recycler view
        new PlaylistAsyncTaskObject().execute((PlaylistAsyncTaskObjectResponse) objects -> {
            ListsFragment fragment = ((ListsFragment) getParentFragmentManager().findFragmentByTag(getString(R.string.lists_fragment_tag)));
            fragment.setList(objects);
            fragment.updateRecyclerView();
            if (playlistUpdateAction.equals("back")) {
                onBack();
            }
        }, appDatabase, "selectAllPlaylistWithCount");
    }

    @Override
    public void onPlaylistAsyncTaskPlaylistFinish(List<Playlist> playlists) {}

    @Override
    public void onActivityResult(ActivityResult result) {
        // file format information https://en.wikipedia.org/wiki/M3U
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null) {
                Uri uri = intent.getData();
                if (currentAction.equals(Intent.ACTION_CREATE_DOCUMENT)) {
                    writePlaylistToFile(uri);
                } else if (currentAction.equals(Intent.ACTION_OPEN_DOCUMENT)) {
                    parsePlaylistFile(uri);
                }
            }
        }
        currentAction = null;
    }

    private void writePlaylistToFile(Uri uri) {
        StringBuilder text = new StringBuilder("#EXTM3U\n");
        text.append("#PLAYLIST:").append(tempPlaylistExportName).append("\n");
        for (MusicWithArtists item : tempMusicsExportList) {
            String line = "#EXTINF:";
            line += item.music.getMusic_duration() / 1000 + ",";
            line += item.artistsToString() + " - ";
            line += item.music.getMusic_title() + "\n";
            line += Uri.fromFile(new File(item.music.getMusic_path())).getEncodedPath();
            text.append(line).append("\n");
        }

        try {
            ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(text.toString().getBytes());
            fileOutputStream.close();
            pfd.close();
            Toast.makeText(getContext(), R.string.toast_exportation_successful, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), R.string.toast_error, Toast.LENGTH_SHORT).show();
        }
        tempPlaylistExportName = null;
        tempMusicsExportList = null;
    }

    private void parsePlaylistFile(Uri uri) {
        //Get the name of the file without its extension
        String name = null;
        Cursor cursor = getContext().getContentResolver().query(uri, new String[]{ OpenableColumns.DISPLAY_NAME }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(0);
            name = fullName.substring(0, fullName.lastIndexOf('.'));
            cursor.close();
        }

        //Read the file line by line to get the path of each music
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
            ArrayList<String> lines = new ArrayList<>();
            String line = reader.readLine();
            if (line != null && line.matches("^#EXTM3U.*")) {
                while ((line = reader.readLine()) != null) {
                    if (line.matches("^#PLAYLIST:.*")) {
                        name = line.substring(10);
                    } else if (line.matches("^#EXTINF:.*")) {
                        if ((line = reader.readLine()) != null)
                            lines.add(Uri.decode(line));
                    }
                }
            }
            if (lines.size() > 0) {
                final String playlistName = name;
                //Get all music ids from the paths
                new MusicAsyncTask().execute((OnSearchMusicAsyncTaskFinish) objects -> {
                    ArrayList<Long> keyList = new ArrayList<>((List<Long>) (Object) objects);
                    List<Playlist> playlists = new ArrayList<>();
                    for (long l : keyList) {
                        playlists.add(new Playlist(l, playlistName));
                    }
                    //delete/insert all ids in the playlist
                    playlistUpdateAction = "nothing";
                    new PlaylistAsyncTaskNull().execute(this, appDatabase, "updatePlaylist", playlists, playlistName);
                    Toast.makeText(getContext(), R.string.toast_importation_successful, Toast.LENGTH_SHORT).show();
                }, appDatabase, "selectAllIdsByPath", lines);
            } else {
                Toast.makeText(getContext(), R.string.toast_invalid_file, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), R.string.toast_error, Toast.LENGTH_SHORT).show();
        }
    }

    //COMMUNICATING
    public interface CategoryFragmentListener {
        void changeListInMain(int column, String category);

        void swipeToMain();

        void openSettings();
    }
}