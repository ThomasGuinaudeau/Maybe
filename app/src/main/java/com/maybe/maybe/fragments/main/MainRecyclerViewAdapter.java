package com.maybe.maybe.fragments.main;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.utils.Constants;
import com.maybe.maybe.utils.Methods;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private final OnMusicListItemClick onMusicListItemClick;
    private boolean isEven = false;
    private List<MusicWithArtists> musics;
    private long id;
    private String sort;
    private ArrayList<Long> foundIds;

    public MainRecyclerViewAdapter(OnMusicListItemClick onMusicListItemClick, List<MusicWithArtists> musics) {
        this.musics = musics;
        this.onMusicListItemClick = onMusicListItemClick;
        foundIds = new ArrayList<>();
    }

    public void setMusics(List<MusicWithArtists> musics) {
        this.musics = musics;
    }

    public int getMusicPosition(long fileId) {
        for (int i = 0; i < musics.size(); i++)
            if (musics.get(i).music.getMusic_id() == fileId)
                return i;
        return -1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setFoundIds(ArrayList<Long> foundIds) {
        this.foundIds = foundIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CustomMainRecyclerViewRow itemView = new CustomMainRecyclerViewRow(parent.getContext());
        itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getCustomRecyclerViewRow().setMusicWithArtists(musics.get(position));
        holder.getCustomRecyclerViewRow().setFoundColor(foundIds.contains(musics.get(position).music.getMusic_id()));
        holder.getCustomRecyclerViewRow().setSelected(id == musics.get(position).music.getMusic_id());
        holder.setItemClickCallback(musics.get(position).music, onMusicListItemClick);
        holder.setLongItemClickCallback(musics.get(position).music, onMusicListItemClick);
    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        switch (sort) {
            case Constants.SORT_ALPHA:
                return Methods.removeDiacritic(musics.get(position).music.getMusic_title().charAt(0)).toUpperCase();
            case Constants.SORT_NUM:
                return musics.get(position).music.getMusic_track() + "";
            default:
                return "";
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final CustomMainRecyclerViewRow customMainRecyclerViewRow;

        public ViewHolder(View view) {
            super(view);
            customMainRecyclerViewRow = (CustomMainRecyclerViewRow) view;
        }

        public CustomMainRecyclerViewRow getCustomRecyclerViewRow() {
            return customMainRecyclerViewRow;
        }

        public void setItemClickCallback(final Music music, final OnMusicListItemClick callback) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onItemClick(music);
                }
            });
        }

        public void setLongItemClickCallback(final Music music, final OnMusicListItemClick callback) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    callback.onLongItemClick(music);
                    return true;
                }
            });
        }
    }
}