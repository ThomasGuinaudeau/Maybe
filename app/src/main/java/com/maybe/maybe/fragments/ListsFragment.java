package com.maybe.maybe.fragments;

import static com.maybe.maybe.CategoryItem.CATEGORY_PLAYLIST;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maybe.maybe.CategoryItem;
import com.maybe.maybe.ListItem;
import com.maybe.maybe.R;
import com.maybe.maybe.adapters.CategoryRecyclerViewAdapter;
import com.maybe.maybe.adapters.OnListItemClick;

import java.util.ArrayList;

public class ListsFragment extends Fragment implements OnListItemClick {
    private static final String TAG = "ListsFragment";
    private ArrayList<ListItem> list;
    private CategoryItem categoryItem;
    private CategoriesFragment.CategoriesFragmentListener callback;
    private CategoryRecyclerViewAdapter adapter;

    public static ListsFragment newInstance() {
        return new ListsFragment();
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lists, container, false);

        TextView textView = view.findViewById(R.id.category_list_title);
        textView.setText(categoryItem.getName());

        adapter = new CategoryRecyclerViewAdapter(this);
        adapter.setList(list);
        RecyclerView recyclerView = view.findViewById(R.id.lists_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        Button buttonBack = view.findViewById(R.id.category_list_back);
        buttonBack.setOnClickListener(view1 -> callback.back());

        if (categoryItem.getId() == CATEGORY_PLAYLIST) {
            Button buttonAdd = view.findViewById(R.id.category_list_add);
            buttonAdd.setOnClickListener(view1 -> {
                //Create AlertDialog to choose a playlist name
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                EditText editText = new EditText(getContext());
                editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                editText.setGravity(Gravity.CENTER);
                LinearLayout ll = new LinearLayout(getContext());
                ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                ll.setPadding(80, 0, 80, 0);
                ll.setGravity(Gravity.CENTER);
                ll.addView(editText);
                builder.setView(ll);
                builder.setTitle(R.string.dialog_playlist_name);
                builder.setPositiveButton(R.string.dialog_button_add, (dialog, id) -> {
                    String name = editText.getText().toString();
                    if (!name.equals("") && !name.equals("All Musics")) {
                        callback.changeFragment(categoryItem, name, true);
                    } else
                        Toast.makeText(getContext(), "Invalid name!", Toast.LENGTH_SHORT).show();
                });
                builder.setNegativeButton(R.string.dialog_button_cancel, (dialog, id) -> {});
                builder.create().show();
            });

            Button buttonImport = view.findViewById(R.id.category_list_import);
            buttonImport.setOnClickListener(view1 -> {
                callback.importPlaylist();
            });
        } else {
            LinearLayout buttonLayout = view.findViewById(R.id.category_list_buttons_layout);
            buttonLayout.removeView(view.findViewById(R.id.category_list_add));
            buttonLayout.removeView(view.findViewById(R.id.category_list_import));
        }

        return view;
    }

    public void setList(ArrayList<ListItem> list) {
        this.list = list;
        if (adapter != null) {
            adapter.setList(this.list);
        }
    }

    public void setCategory(CategoryItem categoryItem) {
        this.categoryItem = categoryItem;
    }

    public void setCallback(CategoriesFragment.CategoriesFragmentListener callback) {
        this.callback = callback;
    }

    public void updateRecyclerView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(ListItem item) {
        callback.changeFragment(categoryItem, item.getName(), true);
    }
}
