package com.maybe.maybe.fragments.category.editing;

import static com.maybe.maybe.fragments.category.CategoryItem.CATEGORY_PLAYLIST;

import android.os.Bundle;
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

import com.maybe.maybe.R;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.fragments.category.CategoryItem;
import com.maybe.maybe.fragments.category.grid.CategoryGridFragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListEditingFragment extends Fragment implements CategoryEditingListCallback {
    private static final String TAG = "ListEditingFragment";
    private List<MusicWithArtists> listSelected, listAll;
    private CategoryGridFragment.CategoriesFragmentListener callback;
    private SelectionTracker<Long> tracker;
    private ImageButton buttonVisibility, buttonSort;
    private boolean isVisible;
    private String name, sortType;
    private CategoryItem categoryItem;
    private CategoryEditingListAdapter adapterSelected, adapterAll;
    private RecyclerView recyclerViewSelected, recyclerViewAll;

    public static ListEditingFragment newInstance() {
        return new ListEditingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isVisible = false;
        sortType = "title";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editing_list, container, false);

        TextView title = view.findViewById(R.id.category_editing_list_main_title);
        title.setText(name);

        adapterAll = new CategoryEditingListAdapter(null);
        adapterAll.setList(listAll);
        recyclerViewAll = view.findViewById(R.id.editing_lists_recycler_view_all);
        recyclerViewAll.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAll.setAdapter(adapterAll);

        ImageButton buttonBack = view.findViewById(R.id.category_editing_list_back);
        buttonBack.setOnClickListener(view1 -> callback.back());

        ImageButton buttonPlay = view.findViewById(R.id.category_editing_list_play);
        buttonPlay.setOnClickListener(view1 -> callback.changeList(categoryItem.getId(), name));
        if (categoryItem.getId() == CATEGORY_PLAYLIST && !name.equals("All Musics")) {
            adapterSelected = new CategoryEditingListAdapter(this);
            adapterSelected.setList(listSelected);
            recyclerViewSelected = view.findViewById(R.id.editing_lists_recycler_view_selected);
            recyclerViewSelected.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewSelected.setAdapter(adapterSelected);

            tracker = new SelectionTracker.Builder<>(
                    "my-selection-id",
                    recyclerViewAll,
                    new CustomItemKeyProvider(listAll),
                    new CustomItemsDetailsLookup(recyclerViewAll),
                    StorageStrategy.createLongStorage())
                    .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                    .build();
            adapterAll.setTracker(tracker);
            adapterSelected.setTracker(tracker);
            tracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
                @Override
                public void onItemStateChanged(@NonNull Long key, boolean selected) {
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

            listSelected.forEach((item) -> {
                tracker.select(item.music.getMusic_id());
            });

            buttonVisibility = view.findViewById(R.id.category_editing_list_visibility);
            isVisible = listSelected.size() == 0;
            changeVisibility();
            buttonVisibility.setOnClickListener(view1 -> {
                isVisible = !isVisible;
                changeVisibility();
            });

            buttonSort = view.findViewById(R.id.category_editing_list_sort);
            buttonSort.setOnClickListener(view1 -> {
                sort(sortType.equals("title") ? "artist" : "title");
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

    public void setList(List<MusicWithArtists> listSelected) {
        this.listSelected = listSelected;
    }

    public void setCategoryAndName(CategoryItem categoryItem, String name) {
        this.categoryItem = categoryItem;
        this.name = name;
    }

    public void setCallback(CategoryGridFragment.CategoriesFragmentListener callback) {
        this.callback = callback;
    }

    private void changeVisibility() {
        if (!isVisible) {
            MutableSelection<Long> snapshot = new MutableSelection<Long>();
            tracker.copySelection(snapshot);
            ArrayList<Long> keyList = new ArrayList<>();
            snapshot.forEach(keyList::add);

            listSelected = new ArrayList<>();
            listAll.forEach(item -> {
                if (keyList.contains(item.music.getMusic_id()))
                    listSelected.add(item);
            });
            adapterSelected.setList(listSelected);
            adapterSelected.notifyDataSetChanged();
        }
        buttonVisibility.setImageResource(isVisible ? R.drawable.ic_round_visibility_off_24 : R.drawable.ic_round_visibility_24);
        recyclerViewSelected.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        recyclerViewAll.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void sort(String type) {
        sortType = type;
        if (type.equals("title"))
            listAll.sort(Comparator.comparing(s -> s.music.getMusic_title()));
        else if (type.equals("artist"))
            listAll.sort(Comparator.comparing(MusicWithArtists::artistsToString));
        adapterAll.setList(listAll);
        adapterAll.notifyDataSetChanged();
        buttonSort.setImageResource(type.equals("title") ? R.drawable.ic_round_sort_by_alpha_24 : R.drawable.ic_round_sort_by_artist_24);
    }

    @Override
    public void onCategoryEditingListClick() {
        adapterSelected.notifyDataSetChanged();
    }
}
