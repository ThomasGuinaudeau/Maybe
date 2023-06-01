package com.maybe.maybe.fragments.category.editing;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class CustomItemDetails extends ItemDetailsLookup.ItemDetails<Long> {
    private final int position;
    private final long key;

    public CustomItemDetails(int position, long key) {
        this.position = position;
        this.key = key;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        return key;
    }
}