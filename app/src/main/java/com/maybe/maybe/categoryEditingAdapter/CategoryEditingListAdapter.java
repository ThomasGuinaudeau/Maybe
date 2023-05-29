package com.maybe.maybe.categoryEditingAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.maybe.maybe.R;
import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.List;

public class CategoryEditingListAdapter extends RecyclerView.Adapter<CategoryEditingListAdapter.ViewHolder> {
    private List<MusicWithArtists> list;
    private SelectionTracker<Long> tracker;
    private boolean isVisible;

    public CategoryEditingListAdapter() {
        setHasStableIds(true);
    }

    @Override
    public CategoryEditingListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category_editing_list_recycler_view_row, viewGroup, false);
        return new CategoryEditingListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicWithArtists item = list.get(position);
        holder.bind(item, tracker == null ? -1 : tracker.isSelected(item.music.getMusic_id()) ? 1 : 0);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).music.getMusic_id();
    }

    public void setList(List<MusicWithArtists> list) {
        this.list = list;
    }

    public void setTracker(SelectionTracker<Long> tracker) {
        this.tracker = tracker;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, artist, quantity;
        private final CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.category_editing_list_item_name);
            artist = (TextView) view.findViewById(R.id.category_editing_list_item_artist);
            quantity = (TextView) view.findViewById(R.id.category_editing_list_item_quantity);
            checkBox = (CheckBox) view.findViewById(R.id.category_editing_list_item_checkBox);
        }

        public void bind(MusicWithArtists musicWithArtists, int isActivated) {
            name.setText(musicWithArtists.music.getMusic_title());
            artist.setText(musicWithArtists.artistsToString());
            long duration = musicWithArtists.music.getMusic_duration();
            long totalSeconds = duration / 1000;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            String durationStr = String.format(minutes + ": %02d", seconds);
            quantity.setText(durationStr);

            if (isActivated == -1) {
                ((ViewGroup) itemView).removeView(checkBox);
            } else {
                checkBox.setChecked(isActivated == 1);
                itemView.setOnClickListener(view -> {
                    if (!tracker.hasSelection())
                        tracker.select(musicWithArtists.music.getMusic_id());
                });
                if (!isVisible && isActivated == 0) {
                    itemView.setVisibility(View.GONE);
                    itemView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                } else if (isVisible || isActivated == 1) {
                    itemView.setVisibility(View.VISIBLE);
                    itemView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }
            }
        }

        public ItemDetailsLookup.ItemDetails getItemDetails() {
            return new CustomItemDetails(getBindingAdapterPosition(), list.get(getBindingAdapterPosition()).music.getMusic_id());
        }
    }
}