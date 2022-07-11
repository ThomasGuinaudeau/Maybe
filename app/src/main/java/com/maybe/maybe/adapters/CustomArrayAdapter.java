package com.maybe.maybe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.maybe.maybe.R;
import com.maybe.maybe.utils.ColorsConstants;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter {
    public CustomArrayAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        } else {
            TextView textView = (TextView) convertView.findViewById(R.id.text1);
            textView.setBackgroundColor(ColorsConstants.BACKGROUND_COLOR);
            textView.setTextColor(ColorsConstants.PRIMARY_TEXT_COLOR);
        }
        return super.getView(position, convertView, parent);
    }
}
