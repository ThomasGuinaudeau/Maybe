package com.maybe.maybe.fragments.category.list;

import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_PLAYLIST;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maybe.maybe.R;
import com.maybe.maybe.fragments.category.CategoryItem;
import com.maybe.maybe.fragments.category.CategoryRecyclerViewAdapter;
import com.maybe.maybe.fragments.category.ListItem;
import com.maybe.maybe.fragments.category.OnListItemClick;
import com.maybe.maybe.fragments.category.grid.CategoryGridFragment;
import com.maybe.maybe.utils.CustomButton;

import java.util.ArrayList;

public class ListsFragment extends Fragment implements OnListItemClick {
    private static final String TAG = "ListsFragment";
    private ArrayList<ListItem> list;
    private CategoryItem categoryItem;
    private CategoryGridFragment.CategoriesFragmentListener callback;
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

        TextView title = view.findViewById(R.id.category_list_main_title);
        title.setText(categoryItem.getName());

        adapter = new CategoryRecyclerViewAdapter(this);
        adapter.setList(list);
        RecyclerView recyclerView = view.findViewById(R.id.lists_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        ImageButton buttonBack = view.findViewById(R.id.category_list_back);
        buttonBack.setOnClickListener(view1 -> callback.back());

        if (categoryItem.getId() == CATEGORY_PLAYLIST) {
            ImageButton buttonAdd = view.findViewById(R.id.category_list_add);
            buttonAdd.setOnClickListener(view1 -> {
                //Create AlertDialog to choose a playlist name
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                AlertDialog alertDialog = builder.create();//Used to be able to dismiss the dialog
                View dialogView = inflater.inflate(R.layout.new_playlist_dialog, null);

                EditText editText = dialogView.findViewById(R.id.new_playlist_dialog_playlist_name);
                editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                editText.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String name = v.getText().toString();
                        ListsFragment.this.onAdd(name);
                        alertDialog.dismiss();
                        return true;
                    }
                    return false;
                });

                CustomButton btnAdd = dialogView.findViewById(R.id.new_playlist_dialog_add);
                btnAdd.setOnClickListener(v -> {
                    String name = editText.getText().toString();
                    ListsFragment.this.onAdd(name);
                    alertDialog.dismiss();
                });
                Button btnCancel = dialogView.findViewById(R.id.new_playlist_dialog_cancel);
                btnCancel.setOnClickListener(v -> alertDialog.dismiss());

                alertDialog.setView(dialogView);
                alertDialog.show();
            });

            ImageButton buttonImport = view.findViewById(R.id.category_list_import);
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

    private void onAdd(String name) {
        if (!name.equals("") && !name.equals("All Musics"))
            callback.changeFragment(categoryItem, name, true);
        else
            Toast.makeText(getContext(), R.string.toast_invalid_name, Toast.LENGTH_SHORT).show();
    }

    public void setList(ArrayList<ListItem> list) {
        this.list = list;
        if (adapter != null)
            adapter.setList(this.list);
    }

    public void setCategory(CategoryItem categoryItem) {
        this.categoryItem = categoryItem;
    }

    public void setCallback(CategoryGridFragment.CategoriesFragmentListener callback) {
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
