package com.maybe.maybe.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maybe.maybe.CustomRecyclerViewRow;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.maybe.maybe.utils.Constants.SORT_ALPHA;
import static com.maybe.maybe.utils.Constants.SORT_NUM;
import static com.maybe.maybe.utils.Constants.removeDiacritic;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private boolean isEven = false;
    private final OnMusicListItemClick onMusicListItemClick;
    private List<MusicWithArtists> musics;
    private ArrayList<Integer> selectedPos;
    private String sort;
    private boolean editMode;

    public void setMusics(List<MusicWithArtists> musics) {
        this.musics = musics;
    }

    public int getMusicPosition(long fileId) {
        for(int i = 0; i < musics.size(); i++)
            if(musics.get(i).music.getMusic_id() == fileId)
                return i;
        return -1;
    }

    public void setSelectedPos(ArrayList<Integer> selectedPos) {
        this.selectedPos = selectedPos;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setEditMode(Boolean editMode) {
        this.editMode = editMode;
    }

    public MainRecyclerViewAdapter(OnMusicListItemClick onMusicListItemClick, List<MusicWithArtists> musics) {
        this.musics = musics;
        this.onMusicListItemClick = onMusicListItemClick;
        this.selectedPos = new ArrayList<Integer>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CustomRecyclerViewRow itemView = new CustomRecyclerViewRow(parent.getContext());
        itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getCustomRecyclerViewRow().setMusicWithArtists(musics.get(position));
        if(position % 2 == 0)
            holder.getCustomRecyclerViewRow().setEven(true);
        holder.getCustomRecyclerViewRow().setSelected(selectedPos.contains(position), editMode);
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
        if(sort.equals(SORT_ALPHA))
            return removeDiacritic(musics.get(position).music.getMusic_title().charAt(0)).toUpperCase();
        else if(sort.equals(SORT_NUM))
            return musics.get(position).music.getMusic_track() + "";
        else
            return "";
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final CustomRecyclerViewRow customRecyclerViewRow;

        public ViewHolder(View view) {
            super(view);
            customRecyclerViewRow = (CustomRecyclerViewRow) view;
        }

        public CustomRecyclerViewRow getCustomRecyclerViewRow() {
            return customRecyclerViewRow;
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