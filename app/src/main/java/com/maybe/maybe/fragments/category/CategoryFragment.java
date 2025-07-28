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

import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.entity.ArtistWithMusics;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.database.entity.Playlist;
import com.maybe.maybe.database.runnables.FillDbRunnable;
import com.maybe.maybe.database.runnables.MusicRunnable;
import com.maybe.maybe.database.runnables.playlist.IPlaylistRunnableNull;
import com.maybe.maybe.database.runnables.playlist.PlaylistRunnableNull;
import com.maybe.maybe.database.runnables.playlist.PlaylistRunnableObject;
import com.maybe.maybe.fragments.category.editing.ListEditingFragment;
import com.maybe.maybe.fragments.category.grid.CategoryGridFragment;
import com.maybe.maybe.fragments.category.list.ListsFragment;
import com.maybe.maybe.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CategoryFragment extends Fragment implements IPlaylistRunnableNull, CategoryGridFragment.CategoriesFragmentListener, ActivityResultCallback<ActivityResult> {

    private static final String TAG = "CategoryFragment";
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
    private CategoryFragmentListener callback;
    private AppDatabase appDatabase;
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

        CategoryGridFragment fragment = CategoryGridFragment.newInstance();
        fragment.setCallback(this);
        getParentFragmentManager().beginTransaction()
                .add(R.id.category_fragment_list, fragment, getString(R.string.categories_fragment_tag))
                .setReorderingAllowed(true)
                .addToBackStack(getString(R.string.categories_fragment_tag))
                .commit();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityResultLauncher.unregister();
    }

    public void syncDatabase() {
        Executors.newSingleThreadExecutor().execute(new FillDbRunnable(getContext(), () -> {}, appDatabase));
    }

    private void openPlaylists(CategoryItem categoryItem) {
        Executors.newSingleThreadExecutor().execute(new PlaylistRunnableObject(objects -> {
            addFragment(objects, categoryItem);
        }, appDatabase));
    }

    private void openArtists(CategoryItem categoryItem) {
        Executors.newSingleThreadExecutor().execute(new MusicRunnable(getContext(), objects -> {
            ArrayList<ListItem> artists = new ArrayList<>();
            List<ArtistWithMusics> artistWithMusics = (List<ArtistWithMusics>) (Object) objects;
            for (ArtistWithMusics am : artistWithMusics) {
                artists.add(new ListItem(am.artist.getArtist_id(), am.artist.getArtist_name(), am.musics.size()));
            }
            addFragment(artists, categoryItem);
        }, appDatabase, "selectAllArtistWithMusics", -1, null, null, null));
    }

    private void openAlbums(CategoryItem categoryItem) {
        Executors.newSingleThreadExecutor().execute(new MusicRunnable(getContext(), objects -> {
            ArrayList<ListItem> albums = new ArrayList<>((List<ListItem>) (Object) objects);
            addFragment(albums, categoryItem);
        }, appDatabase, "selectAllAlbumWithCount", -1, null, null, null));
    }

    private void openFolders(CategoryItem categoryItem) {
        Executors.newSingleThreadExecutor().execute(new MusicRunnable(getContext(), objects -> {
            ArrayList<ListItem> folders = new ArrayList<>((List<ListItem>) (Object) objects);
            addFragment(folders, categoryItem);
        }, appDatabase, "selectAllFolderWithCount", -1, null, null, null));
    }

    private void openMusicsFrom(CategoryItem categoryItem, String name) {
        String select = "selectAll";
        if (categoryItem.getId() == CATEGORY_ARTIST)
            select = "selectAllMusicsOfArtist";
        else if (categoryItem.getId() == CATEGORY_ALBUM)
            select = "selectAllMusicsOfAlbum";
        else if (categoryItem.getId() == CATEGORY_FOLDER)
            select = "selectAllMusicsOfFolder";
        else if (categoryItem.getId() == CATEGORY_PLAYLIST && !name.equals("All Musics"))
            select = "selectAllMusicsOfPlaylist";

        if (select.equals("selectAllMusicsOfPlaylist")) {
            Executors.newSingleThreadExecutor().execute(new MusicRunnable(getContext(), objects -> {
                ArrayList<MusicWithArtists> musics = new ArrayList<>((List<MusicWithArtists>) (Object) objects);
                if (tempMusicsEditList == null)
                    tempMusicsEditList = musics;
                else {
                    addFragmentEdit(musics, tempMusicsEditList, categoryItem, name);
                    tempMusicsEditList = null;
                }
            }, appDatabase, "selectAll", -1, SORT_ALPHA, name, null));
            Executors.newSingleThreadExecutor().execute(new MusicRunnable(getContext(), objects -> {
                ArrayList<MusicWithArtists> musics = new ArrayList<>((List<MusicWithArtists>) (Object) objects);
                if (tempMusicsEditList == null)
                    tempMusicsEditList = musics;
                else {
                    addFragmentEdit(tempMusicsEditList, musics, categoryItem, name);
                    tempMusicsEditList = null;
                }
            }, appDatabase, select, -1, SORT_ALPHA, name, null));
        } else {
            Executors.newSingleThreadExecutor().execute(new MusicRunnable(getContext(), objects -> {
                ArrayList<MusicWithArtists> musics = new ArrayList<>((List<MusicWithArtists>) (Object) objects);
                addFragmentEdit(musics, null, categoryItem, name);
            }, appDatabase, select, -1, SORT_ALPHA, name, null));
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
                .addToBackStack(getString(R.string.lists_fragment_tag))
                .commit();
    }

    private void addFragmentEdit(ArrayList<MusicWithArtists> listAll, ArrayList<MusicWithArtists> list, CategoryItem categoryItem, String name) {
        ListEditingFragment fragment = ListEditingFragment.newInstance();
        fragment.setCallback(this);
        fragment.setListAll(listAll);
        fragment.setList(list);
        fragment.setCategoryAndName(categoryItem, name);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.category_fragment_list, fragment, getString(R.string.lists_editing_fragment_tag))
                .setReorderingAllowed(true)
                .addToBackStack(getString(R.string.lists_editing_fragment_tag))
                .commit();
    }

    @Override
    public void back() {
        onBack();
    }

    @Override
    public void saveToList(ArrayList<Long> keyList, String name, boolean isDelete) {
        List<Playlist> playlists = new ArrayList<>();
        for (long l : keyList) {
            playlists.add(new Playlist(l, name));
        }
        playlistUpdateAction = "back";
        Executors.newSingleThreadExecutor().execute(new PlaylistRunnableNull(this, appDatabase, name, playlists, Constants.PLAYLIST_REPLACE));
        if (isDelete)
            Toast.makeText(getContext(), R.string.toast_playlist_deleted, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), R.string.toast_playlist_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void exportPlaylist(ArrayList<Long> keyList, String name) {
        tempPlaylistExportName = name;
        tempMusicsExportList = new ArrayList<>();
        Executors.newSingleThreadExecutor().execute(new MusicRunnable(getContext(), objects -> {
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
        }, appDatabase, "selectAll", -1, SORT_ALPHA, null, null));
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
    public void changeList(ArrayList<Long> keyList, int categoryId, String name) {
        if (keyList != null) {
            //save list first then play it
            List<Playlist> playlists = new ArrayList<>();
            for (long l : keyList)
                playlists.add(new Playlist(l, name));
            Executors.newSingleThreadExecutor().execute(new PlaylistRunnableNull(() -> {
                Executor mainThreadExecutor = ContextCompat.getMainExecutor(getContext());
                mainThreadExecutor.execute(() -> goToMainAndPlay(categoryId, name));
            }, appDatabase, name, playlists, Constants.PLAYLIST_REPLACE));
        } else {
            goToMainAndPlay(categoryId, name);
        }
    }

    private void goToMainAndPlay(int categoryId, String name) {
        callback.changeList(categoryId, name);
        callback.swipeToMain();
        getParentFragmentManager().popBackStack(getString(R.string.categories_fragment_tag), 0);
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
            openFolders(categoryItem);
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
    public void onFinishNull() {
        //When deleting/inserting musics in a playlist, update the recycler view
        Executors.newSingleThreadExecutor().execute(new PlaylistRunnableObject(objects -> {
            getActivity().runOnUiThread(() -> {
                ListsFragment fragment = ((ListsFragment) CategoryFragment.this.getParentFragmentManager().findFragmentByTag(CategoryFragment.this.getString(R.string.lists_fragment_tag)));
                fragment.setList(objects);
                fragment.updateRecyclerView();
                if (playlistUpdateAction.equals("back")) {
                    onBack();
                }
            });
        }, appDatabase));
    }

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
                Executors.newSingleThreadExecutor().execute(new MusicRunnable(getContext(), objects -> {
                    ArrayList<Long> keyList = new ArrayList<>((List<Long>) (Object) objects);
                    List<Playlist> playlists = new ArrayList<>();
                    for (long l : keyList) {
                        playlists.add(new Playlist(l, playlistName));
                    }
                    //delete/insert all ids in the playlist
                    playlistUpdateAction = "nothing";
                    Executors.newSingleThreadExecutor().execute(new PlaylistRunnableNull(this, appDatabase, playlistName, playlists, Constants.PLAYLIST_REPLACE));
                    Toast.makeText(getContext(), R.string.toast_importation_successful, Toast.LENGTH_SHORT).show();
                }, appDatabase, "selectAllIdsByPath", -1, null, null, lines));
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
        void changeList(int column, String category);

        void swipeToMain();

        void openSettings();
    }
}