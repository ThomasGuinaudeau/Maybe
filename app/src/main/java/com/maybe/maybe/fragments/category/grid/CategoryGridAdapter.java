package com.maybe.maybe.fragments.category.grid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.maybe.maybe.R;
import com.maybe.maybe.fragments.category.CategoryItem;

import java.util.ArrayList;

public class CategoryGridAdapter extends ArrayAdapter<CategoryItem> {
    private final ArrayList<CategoryItem> categoryList;

    public CategoryGridAdapter(@NonNull Context context, int resource, ArrayList<CategoryItem> categoryList) {
        super(context, resource, categoryList);
        this.categoryList = categoryList;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_row_item, null);

            TextView textView = (TextView) convertView.findViewById(R.id.category_title);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.category_icon);
            textView.setText(categoryList.get(position).getName());
            imageView.setImageResource(categoryList.get(position).getIcon());
        }

        return convertView;
    }
}
