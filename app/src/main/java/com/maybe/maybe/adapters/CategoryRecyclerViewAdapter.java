package com.maybe.maybe.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.maybe.maybe.ListItem;
import com.maybe.maybe.R;

import java.util.ArrayList;

public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder> {
    private final OnListItemClick onListItemClick;
    private ArrayList<ListItem> list;

    public CategoryRecyclerViewAdapter(OnListItemClick onListItemClick) {
        this.onListItemClick = onListItemClick;
    }

    public void setList(ArrayList<ListItem> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category_recycler_view_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getNameTextView().setText(list.get(position).getName());
        viewHolder.getQuantityTextView().setText("" + list.get(position).getQuantity());
        viewHolder.setOnItemClickCallback(list.get(position), onListItemClick);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, quantity;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.category_item_name);
            quantity = (TextView) view.findViewById(R.id.category_item_quantity);
        }

        public TextView getNameTextView() {
            return name;
        }

        public TextView getQuantityTextView() {
            return quantity;
        }

        public void setOnItemClickCallback(ListItem item, OnListItemClick callback) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onItemClick(item);
                }
            });
        }
    }
}