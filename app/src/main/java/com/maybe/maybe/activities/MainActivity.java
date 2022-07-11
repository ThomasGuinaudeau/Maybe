package com.maybe.maybe.activities;

import static com.maybe.maybe.CustomViewPager.CAT_POS;
import static com.maybe.maybe.CustomViewPager.MAIN_POS;
import static com.maybe.maybe.CustomViewPager.PLAY_POS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.maybe.maybe.CustomViewPager;
import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.async_tasks.FillDbAsyncTask;
import com.maybe.maybe.database.async_tasks.OnFillDbAsyncTaskFinish;
import com.maybe.maybe.database.entity.Music;
import com.maybe.maybe.database.entity.MusicWithArtists;
import com.maybe.maybe.fragments.CategoryFragment;
import com.maybe.maybe.fragments.MainFragment;
import com.maybe.maybe.fragments.PlayerFragment;
import com.maybe.maybe.utils.ColorsConstants;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements CategoryFragment.CategoryFragmentListener, MainFragment.MainFragmentListener, PlayerFragment.PlayerFragmentListener, OnFillDbAsyncTaskFinish {

    public static final int DATABASE_VERSION = 1;
    private static final String TAG = "MainActivity";
    private ViewPager2 viewPager;
    private CustomViewPager pagerAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private int time = 0;
    private final ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (position == CAT_POS) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);

        //StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
        else checkForUpdate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            checkForUpdate();
    }

    private void checkForUpdate() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int dbVersion = sharedPref.getInt(getString(R.string.db_version), 1);

        if (dbVersion < DATABASE_VERSION) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.db_version), DATABASE_VERSION);
            editor.apply();

            AppDatabase appDatabase = AppDatabase.getInstance(this);
            new FillDbAsyncTask(this).execute(this, this, appDatabase);
        } else start();
    }

    @Override
    public void onFillDbAsyncTaskFinish() {
        start();
    }

    public void start() {
        ColorsConstants.loadColors(this);

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        navigationView = findViewById(R.id.nav_view);
        updateColors();
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START, true);
            CategoryFragment fragment = (CategoryFragment) pagerAdapter.getRegisteredFragment(CAT_POS);
            if (item.getItemId() == R.id.sync_database) fragment.syncDatabase();
            else if (item.getItemId() == R.id.add_playlist) fragment.addPlaylist();
            else if (item.getItemId() == R.id.delete_playlist) fragment.deletePlaylist();
            else if (item.getItemId() == R.id.settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                activityResultLauncher.launch(intent);
            }
            return true;
        });
        viewPager = findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(2);
        pagerAdapter = new CustomViewPager(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(MAIN_POS);
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Intent data = result.getData();
                        settingsUpdated();
                    }
                }
            });

    public void settingsUpdated() {
        ColorsConstants.loadColors(this);

        updateColors();

        CategoryFragment categoryFragment = (CategoryFragment) pagerAdapter.getRegisteredFragment(CAT_POS);
        categoryFragment.updateColors();

        MainFragment mainFragment = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
        mainFragment.updateColors();

        PlayerFragment playerFragment = (PlayerFragment) pagerAdapter.getRegisteredFragment(PLAY_POS);
        playerFragment.updateColors();
    }

    public void updateColors() {
        View background = (View) findViewById(R.id.view);
        background.setBackgroundColor(ColorsConstants.BACKGROUND_COLOR);
        navigationView.setBackgroundColor(ColorsConstants.PRIMARY_DARK_COLOR);
        ColorStateList colorStateList = new ColorStateList(new int[][]{ new int[]{ android.R.attr.state_enabled } }, new int[]{ ColorsConstants.PRIMARY_TEXT_COLOR });

        Menu navMenu = navigationView.getMenu();
        MenuItem tools1 = navMenu.findItem(R.id.subtitle_music);
        SpannableString spannableString1 = new SpannableString(tools1.getTitle());
        TextAppearanceSpan textAppearanceSpan1 = new TextAppearanceSpan(null, 0, 40, colorStateList, null);
        spannableString1.setSpan(textAppearanceSpan1, 0, spannableString1.length(), 0);
        tools1.setTitle(spannableString1);

        MenuItem tools2 = navMenu.findItem(R.id.subtitle_database);
        SpannableString spannableString2 = new SpannableString(tools2.getTitle());
        TextAppearanceSpan textAppearanceSpan2 = new TextAppearanceSpan(null, 0, 40, colorStateList, null);
        spannableString2.setSpan(textAppearanceSpan2, 0, spannableString2.length(), 0);
        tools2.setTitle(spannableString2);

        MenuItem tools3 = navMenu.findItem(R.id.subtitle_other);
        SpannableString spannableString3 = new SpannableString(tools3.getTitle());
        TextAppearanceSpan textAppearanceSpan3 = new TextAppearanceSpan(null, 0, 40, colorStateList, null);
        spannableString3.setSpan(textAppearanceSpan3, 0, spannableString3.length(), 0);
        tools3.setTitle(spannableString3);

        navigationView.setItemIconTintList(colorStateList);
        navigationView.setItemTextColor(colorStateList);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        activityResultLauncher.unregister();
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pagerAdapter != null) {
            MainFragment fragment = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
            fragment.onAppBackground();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pagerAdapter != null && time != 0) {
            MainFragment fragment = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
            if (fragment != null)
                fragment.onAppForeground();
        }
        time = 1;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == MAIN_POS) {
            finish();
        } else {
            viewPager.setCurrentItem(MAIN_POS);
        }
    }

    @Override
    public void changeListInMain(String column, String category) {
        MainFragment fragment = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
        fragment.change2(column, category);
    }

    @Override
    public void changeMusicInPlayer(MusicWithArtists musicWithArtists) {
        PlayerFragment fragment = (PlayerFragment) pagerAdapter.getRegisteredFragment(PLAY_POS);
        fragment.changeMusic(musicWithArtists);
    }

    @Override
    public void addToPlaylist(ArrayList<Music> musics) {
        CategoryFragment fragment = (CategoryFragment) pagerAdapter.getRegisteredFragment(CAT_POS);
        fragment.addToPlaylist(musics);
    }

    @Override
    public void changeDurationInPlayer(long currentDuration) {
        PlayerFragment fragment = (PlayerFragment) pagerAdapter.getRegisteredFragment(PLAY_POS);
        fragment.updateDuration(currentDuration);
    }

    @Override
    public void changePlayPauseInPlayer(boolean isPlaying) {
        PlayerFragment fragment = (PlayerFragment) pagerAdapter.getRegisteredFragment(PLAY_POS);
        fragment.updatePlayPause(isPlaying);
    }

    @Override
    public void action(String action, String value) {
        MainFragment fragment = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
        fragment.action(action, value);
    }

    @Override
    public void finishEdit() {
        MainFragment fragment = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
        fragment.editChange();
    }

    @Override
    public void resetList() {
        CategoryFragment fragment = (CategoryFragment) pagerAdapter.getRegisteredFragment(CAT_POS);
        fragment.resetList();
    }

    @Override
    public void disableButtons(boolean buttonEnable) {
        PlayerFragment fragment = (PlayerFragment) pagerAdapter.getRegisteredFragment(PLAY_POS);
        fragment.disableButtons(buttonEnable);
    }

    @Override
    public void swipeToMain() {
        viewPager.setCurrentItem(MAIN_POS, true);
    }

    //On crash send stack trace
    /*public void handleUncaughtException(Thread thread, Throwable e) {
        String exStackTrace = Log.getStackTraceString(e);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{ "guinaudt@hotmail.com" });
        i.putExtra(Intent.EXTRA_SUBJECT, "Crash Logs " + currentDate);
        i.putExtra(Intent.EXTRA_TEXT, "" + exStackTrace);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "Cant send email");
        }

        try {
            Thread.sleep(3000);
            finish();
            System.exit(0);
        } catch (InterruptedException interruptedException) {
            finish();
            System.exit(0);
        }
    }*/
}