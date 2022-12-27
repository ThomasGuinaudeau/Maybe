package com.maybe.maybe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.maybe.maybe.R;

import java.util.ArrayList;

public class CategoryGridAdapter extends ArrayAdapter {
    private ArrayList<String> categories;

    public CategoryGridAdapter(@NonNull Context context, int resource, ArrayList objects) {
        super(context, resource, objects);
        categories = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.grid_row_item, null);
        TextView textView = (TextView) v.findViewById(R.id.category_title);
        ImageView imageView = (ImageView) v.findViewById(R.id.category_icon);
        textView.setText(categories.get(position));
        imageView.setImageResource(R.drawable.round_album_24);
        return v;
    }
}
