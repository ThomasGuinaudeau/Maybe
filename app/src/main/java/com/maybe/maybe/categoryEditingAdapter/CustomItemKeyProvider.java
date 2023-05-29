package com.maybe.maybe.categoryEditingAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.List;

public class CustomItemKeyProvider extends ItemKeyProvider<Long> {
    private List<MusicWithArtists> list;

    public CustomItemKeyProvider(List<MusicWithArtists> list) {
        super(ItemKeyProvider.SCOPE_CACHED);
        this.list = list;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public Long getKey(int position) {
        return list.get(position).music.getMusic_id();
    }

    @Override
    public int getPosition(@NonNull Long key) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).music.getMusic_id() == key)
                return i;
        }
        return -1;
    }
}
