package com.maybe.maybe.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.maybe.maybe.R;
import com.maybe.maybe.adapters.CategoryExpandableListViewAdapter;
import com.maybe.maybe.adapters.CategoryGridAdapter;
import com.maybe.maybe.adapters.CustomArrayAdapter;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.async_tasks.ArtistAsyncTask;
import com.maybe.maybe.database.async_tasks.FillDbAsyncTask;
import com.maybe.maybe.database.async_tasks.MusicAsyncTask;
import com.maybe.maybe.database.async_tasks.OnArtistAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnFillDbAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.OnSelectAlbumAsyncTaskFinish;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskNull;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskNullResponse;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskObject;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskObjectResponse;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskPlaylist;
import com.maybe.maybe.database.async_tasks.playlist.PlaylistAsyncTaskPlaylistResponse;
import com.maybe.maybe.database.entity.ArtistWithMusics;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.Playlist;
import com.maybe.maybe.utils.ColorsConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryFragment extends Fragment implements PlaylistAsyncTaskPlaylistResponse, PlaylistAsyncTaskNullResponse, OnFillDbAsyncTaskFinish {

    private static final String TAG = "CategoryFragment";
    private CategoryFragmentListener callback;
    private AppDatabase appDatabase;
    private ArrayList<String> elp;
    private HashMap<String, ArrayList<HashMap<String, Object>>> elh;
    private ArrayList<String> deletePlaylists;
    private View drawerHint;
    private final ExpandableListView.OnItemLongClickListener onLongChildClickListener = new ExpandableListView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                int childPosition = ExpandableListView.getPackedPositionChild(id);

                if (deletePlaylists == null)
                    deletePlaylists = new ArrayList<>();
                if (groupPosition == 0 && childPosition > 0) {
                    String name = (String) ((ArrayList<HashMap<String, Object>>) elh.get("playlist")).get(childPosition).get("name");
                    if (deletePlaylists.contains(name)) {
                        deletePlaylists.remove(name);
                        view.setBackgroundColor(Color.parseColor("#00000000"));
                    } else {
                        deletePlaylists.add(name);
                        view.setBackgroundColor(Color.parseColor("#44C90000"));
                    }
                    return true;
                }
            }
            return false;
        }
    };
    private ArrayList<Music> addPlaylist;
    private CategoryExpandableListViewAdapter adapter;
    //private ExpandableListView elv;
    private GridView gridView;
    private String currentPlaylistName;
    private final ExpandableListView.OnChildClickListener onChildClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            if (groupPosition == 0 && childPosition == 0) {
                currentPlaylistName = "All Musics";
                callback.changeListInMain("playlist", "All Musics");
            } else {
                if (groupPosition == 0)
                    currentPlaylistName = (String) v.getTag();
                callback.changeListInMain(elp.get(groupPosition), (String) v.getTag());
            }
            callback.swipeToMain();
            return true;
        }
    };
    private EditText edit;
    private AlertDialog alertDialog;

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = AppDatabase.getInstance(getContext());
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        currentPlaylistName = sharedPref.getString(getString(R.string.current_cat), "All Musics");
        fillList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        drawerHint = (View) view.findViewById(R.id.drawer_hint);
        adapter = new CategoryExpandableListViewAdapter(getContext(), elp, elh);

        ArrayList<String> catList = new ArrayList<>();
        catList.add("Playlists");
        catList.add("Artists");
        catList.add("Albums");
        catList.add("Folders");
        CategoryGridAdapter gridAdapter = new CategoryGridAdapter(getContext(), R.layout.grid_row_item, catList);
        gridView = (GridView) view.findViewById(R.id.category_grid_view);
        gridView.setAdapter(gridAdapter);
        //elv = (ExpandableListView) view.findViewById(R.id.category_list_view);
        //elv.setAdapter(adapter);
        //elv.setOnChildClickListener(onChildClickListener);
        //elv.setOnItemLongClickListener(onLongChildClickListener);
        updateColors();

        return view;
    }

    public void syncDatabase() {
        new FillDbAsyncTask(getContext()).execute(getContext(), this, appDatabase);
    }

    public void deletePlaylist() {
        boolean nothingToDelete = true;
        //Delete playlists
        if (deletePlaylists != null && deletePlaylists.size() > 0) {
            nothingToDelete = false;

            for (String s : deletePlaylists)
                Log.e(TAG, "gon del " + s);

            new PlaylistAsyncTaskNull().execute(this, appDatabase, "deleteAllFromPlaylists", deletePlaylists);

            //if (elv.getChildCount() > 1) {
            //    for (int i = 0; i < elv.getChildCount(); i++)
            //        elv.getChildAt(i).setBackgroundColor(Color.parseColor("#00000000"));
            //}
        }
        //Delete musics from current playlist
        if (!currentPlaylistName.equals("All Musics") && addPlaylist != null && addPlaylist.size() > 0) {
            nothingToDelete = false;
            ArrayList<Long> ids = new ArrayList<>();
            for (Music m : addPlaylist)
                ids.add(m.getMusic_id());
            new PlaylistAsyncTaskNull().execute(this, appDatabase, "deleteAllPlaylistsByIds", currentPlaylistName, ids);
            addPlaylist.clear();
            callback.finishEdit();
            callback.changeListInMain("playlist", "All Musics");
        }
        if (nothingToDelete)
            Toast.makeText(getContext(), R.string.delete_nothing, Toast.LENGTH_SHORT).show();
    }

    public void addPlaylist() {
        if (addPlaylist != null && addPlaylist.size() > 0)
            new PlaylistAsyncTaskPlaylist().execute(this, appDatabase, "selectAllPlaylist");
        else
            Toast.makeText(getContext(), R.string.add_nothing, Toast.LENGTH_SHORT).show();
    }

    private void fillList() {

        final ArrayList<HashMap<String, Object>> artists = new ArrayList<>();
        final ArrayList<HashMap<String, Object>> playlists = new ArrayList<>();
        final ArrayList<HashMap<String, Object>> albums = new ArrayList<>();

        new ArtistAsyncTask().execute((OnArtistAsyncTaskFinish) objects -> {
            List<ArtistWithMusics> artistWithMusics = (List<ArtistWithMusics>) (Object) objects;
            for (ArtistWithMusics am : artistWithMusics) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("name", am.artist.getArtist_name());
                hashMap.put("count", am.musics.size());
                artists.add(hashMap);
            }
        }, appDatabase, "selectAllArtistWithMusics");

        new PlaylistAsyncTaskObject().execute((PlaylistAsyncTaskObjectResponse) playlists::addAll, appDatabase, "selectAllPlaylistWithCount");

        new MusicAsyncTask().execute((OnSelectAlbumAsyncTaskFinish) objects -> {
            List<HashMap<String, Object>> hashMapList = (List<HashMap<String, Object>>) (Object) objects;
            albums.addAll(hashMapList);
        }, appDatabase, "selectAllAlbumWithCount");

        if (elp == null) elp = new ArrayList<>();
        else elp.clear();

        if (elh == null) elh = new HashMap<>();
        else elh.clear();

        elp.add("playlist");
        elp.add("artist");
        elp.add("album");

        elh.put(elp.get(0), playlists);
        elh.put(elp.get(1), artists);
        elh.put(elp.get(2), albums);
    }

    public void updateColors() {
        drawerHint.setBackgroundColor(ColorsConstants.PRIMARY_DARK_COLOR);

        Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.custom_expandable);
        icon.setTint(ColorsConstants.SECONDARY_TEXT_COLOR);
        //elv.setGroupIndicator(icon);
        adapter.notifyDataSetChanged();
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
        fillList();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CategoryFragmentListener) {
            callback = (CategoryFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement CategoryFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onPlaylistAsyncTaskNullFinish() {
        if (deletePlaylists != null)
            deletePlaylists.clear();
        fillList();
        adapter.notifyDataSetChanged();
        //for (int i = 0; i < 3; i++)
        //    elv.collapseGroup(i);
    }

    //Dialog to add musics to playlist
    @Override
    public void onPlaylistAsyncTaskPlaylistFinish(List<Playlist> playlists) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.category_add_playlist, null);
        TextView label = (TextView) dialogView.findViewById(R.id.dialog_playlist_label);
        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.dialog_playlist_spinner);
        edit = (EditText) dialogView.findViewById(R.id.dialog_playlist_edit);
        Button buttonCancel = (Button) dialogView.findViewById(R.id.dialog_playlist_button_cancel);
        Button buttonAdd = (Button) dialogView.findViewById(R.id.dialog_playlist_button_add);

        buttonAdd.setBackgroundColor(ColorsConstants.PRIMARY_COLOR);
        edit.setBackgroundColor(ColorsConstants.PRIMARY_COLOR);
        edit.setTextColor(ColorsConstants.PRIMARY_TEXT_COLOR);
        label.setText(R.string.add_a_playlist);
        label.setTextColor(ColorsConstants.PRIMARY_TEXT_COLOR);
        ArrayList<String> strPlaylists = new ArrayList<>();
        strPlaylists.add("-new-");
        for (Playlist playlist : playlists)
            strPlaylists.add(playlist.getPlaylist_name());
        spinner.setBackgroundTintList(new ColorStateList(new int[][]{ new int[]{ android.R.attr.state_enabled } }, new int[]{ ColorsConstants.PRIMARY_TEXT_COLOR }));
        spinner.setPopupBackgroundDrawable(new ColorDrawable(0xFF888888));//was darkgrey
        ArrayAdapter<String> adapter = new CustomArrayAdapter(getContext(), R.layout.dialog_playlist_spinner_1line, strPlaylists);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    edit.setEnabled(false);
                    edit.setText(strPlaylists.get(adapterView.getSelectedItemPosition()));
                } else {
                    edit.setEnabled(true);
                    edit.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        PlaylistAsyncTaskNullResponse thisCallback = this;

        buttonAdd.setOnClickListener(view -> {
            String name = edit.getText().toString();
            if (name != null && !name.equals("")) {
                ArrayList<Playlist> playlists1 = new ArrayList<>();
                for (Music m : addPlaylist)
                    playlists1.add(new Playlist(m.getMusic_id(), name));
                new PlaylistAsyncTaskNull().execute(thisCallback, appDatabase, "insertAll", playlists1);
                alertDialog.cancel();
                addPlaylist.clear();
                adapter.notifyDataSetChanged();
                callback.finishEdit();
                callback.swipeToMain();
            } else
                Toast.makeText(getContext(), "You must enter a name or choose one in the list", Toast.LENGTH_SHORT).show();
        });

        buttonCancel.setOnClickListener(view -> {
            alertDialog.cancel();
            addPlaylist.clear();
            callback.finishEdit();
            callback.swipeToMain();
        });

        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
    }

    //COMMUNICATING
    public interface CategoryFragmentListener {
        void changeListInMain(String column, String category);

        void finishEdit();

        void swipeToMain();
    }
}