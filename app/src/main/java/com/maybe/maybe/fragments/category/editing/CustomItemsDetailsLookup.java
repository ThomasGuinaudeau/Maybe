package com.maybe.maybe.fragments.category.editing;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class CustomItemsDetailsLookup extends ItemDetailsLookup<Long> {
    private RecyclerView recyclerView;

    public CustomItemsDetailsLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent event) {
        View view = recyclerView.findChildViewUnder(event.getX(), event.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof CategoryEditingListAdapter.ViewHolder) {
                return ((CategoryEditingListAdapter.ViewHolder) viewHolder).getItemDetails();
            }
        }
        return null;
    }
}
