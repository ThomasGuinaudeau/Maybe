package com.maybe.maybe.fragments.category.editing;

import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_PLAYLIST;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.MutableSelection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maybe.maybe.fragments.category.CategoryItem;
import com.maybe.maybe.R;
import com.maybe.maybe.fragments.category.grid.CategoryGridFragment;
import com.maybe.maybe.database.entity.MusicWithArtists;

import java.util.ArrayList;
import java.util.List;

public class ListEditingFragment extends Fragment {
    private static final String TAG = "ListEditingFragment";
    private List<MusicWithArtists> list, listAll;
    private CategoryGridFragment.CategoriesFragmentListener callback;
    private SelectionTracker<Long> tracker;
    private ImageButton buttonVisibility;
    private boolean isVisible;
    private String name;
    private CategoryItem categoryItem;
    private CategoryEditingListAdapter adapter;

    public static ListEditingFragment newInstance() {
        return new ListEditingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isVisible = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editing_list, container, false);

        TextView title = view.findViewById(R.id.category_editing_list_main_title);
        title.setText(name);

        adapter = new CategoryEditingListAdapter();
        adapter.setList(listAll);
        RecyclerView recyclerView = view.findViewById(R.id.editing_lists_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        //recyclerView.setItemAnimator(null);

        ImageButton buttonBack = view.findViewById(R.id.category_editing_list_back);
        buttonBack.setOnClickListener(view1 -> callback.back());

        ImageButton buttonPlay = view.findViewById(R.id.category_editing_list_play);
        buttonPlay.setOnClickListener(view1 -> callback.changeList(categoryItem.getId(), name));
        Log.e(TAG, categoryItem.getId() + "");
        if (categoryItem.getId() == CATEGORY_PLAYLIST && !name.equals("All Musics")) {
            tracker = new SelectionTracker.Builder<>(
                    "my-selection-id",
                    recyclerView,
                    new CustomItemKeyProvider(listAll),
                    new CustomItemsDetailsLookup(recyclerView),
                    StorageStrategy.createLongStorage())
                    .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                    .build();
            adapter.setTracker(tracker);
            tracker.addObserver(new SelectionTracker.SelectionObserver() {
                @Override
                public void onItemStateChanged(@NonNull Object key, boolean selected) {
                    super.onItemStateChanged(key, selected);
                }

                @Override
                public void onSelectionRefresh() {
                    super.onSelectionRefresh();
                }

                @Override
                public void onSelectionChanged() {
                    super.onSelectionChanged();
                }
            });
            list.forEach((item) -> {
                tracker.select(item.music.getMusic_id());
            });

            buttonVisibility = view.findViewById(R.id.category_editing_list_visibility);
            if (list.size() == 0) {
                isVisible = true;
                changeVisibility();
            }
            buttonVisibility.setOnClickListener(view1 -> {
                isVisible = !isVisible;
                changeVisibility();
            });

            ImageButton buttonSave = view.findViewById(R.id.category_editing_list_save);
            buttonSave.setOnClickListener(view1 -> {
                MutableSelection<Long> snapshot = new MutableSelection<Long>();
                tracker.copySelection(snapshot);
                ArrayList<Long> keyList = new ArrayList<>();
                snapshot.forEach(keyList::add);//keyList::add = aLong -> keyList.add(aLong)
                callback.saveToList(keyList, name);
            });

            ImageButton buttonDelete = view.findViewById(R.id.category_editing_list_delete);
            buttonDelete.setOnClickListener(view1 -> {
                ArrayList<Long> keyList = new ArrayList<>();
                callback.saveToList(keyList, name);
            });

            ImageButton buttonExport = view.findViewById(R.id.category_editing_list_export);
            buttonExport.setOnClickListener(view1 -> {
                MutableSelection<Long> snapshot = new MutableSelection<Long>();
                tracker.copySelection(snapshot);
                ArrayList<Long> keyList = new ArrayList<>();
                snapshot.forEach(keyList::add);//keyList::add = aLong -> keyList.add(aLong)
                callback.exportPlaylist(keyList, name);
            });
        } else {
            LinearLayout buttonLayout = view.findViewById(R.id.category_editing_list_buttons_layout);
            buttonLayout.removeView(view.findViewById(R.id.category_editing_list_visibility));
            buttonLayout.removeView(view.findViewById(R.id.category_editing_list_save));
            buttonLayout.removeView(view.findViewById(R.id.category_editing_list_delete));
            buttonLayout.removeView(view.findViewById(R.id.category_editing_list_export));
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setListAll(List<MusicWithArtists> listAll) {
        this.listAll = listAll;
    }

    public void setList(List<MusicWithArtists> list) {
        this.list = list;
    }

    public void setCategoryAndName(CategoryItem categoryItem, String name) {
        this.categoryItem = categoryItem;
        this.name = name;
    }

    public void setCallback(CategoryGridFragment.CategoriesFragmentListener callback) {
        this.callback = callback;
    }

    private void changeVisibility() {
        //getResources().getDrawable(, getContext().getTheme())
        buttonVisibility.setImageResource(isVisible ? R.drawable.ic_round_visibility_off_24 : R.drawable.ic_round_visibility_24);
        adapter.setVisible(isVisible);
        adapter.notifyDataSetChanged();
    }
}
