package com.maybe.maybe.activities;

import static com.maybe.maybe.CustomViewPager.CAT_POS;
import static com.maybe.maybe.CustomViewPager.MAIN_POS;
import static com.maybe.maybe.CustomViewPager.PLAY_POS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.maybe.maybe.CustomViewPager;
import com.maybe.maybe.R;
import com.maybe.maybe.database.AppDatabase;
import com.maybe.maybe.database.runnables.FillDbRunnable;
import com.maybe.maybe.database.runnables.IFillDbRunnable;
import com.maybe.maybe.fragments.category.CategoryFragment;
import com.maybe.maybe.fragments.main.MainFragment;
import com.maybe.maybe.fragments.player.PlayerFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends FragmentActivity implements CategoryFragment.CategoryFragmentListener, MainFragment.MainFragmentListener, PlayerFragment.PlayerFragmentListener, IFillDbRunnable {
    public static final int DATABASE_VERSION = 4;
    private static final String TAG = "MainActivity";
    private static final String[] PERMISSIONS = {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ? android.Manifest.permission.READ_EXTERNAL_STORAGE : android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_PHONE_STATE
    };
    private final ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
        }
    };
    private ViewPager2 viewPager;
    private CustomViewPager pagerAdapter;
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    //Intent data = result.getData();
                    //If settings have to update something
                }
            });
    private int time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int currentTheme = sharedPref.getInt(getString(R.string.key_theme), R.style.Dark_Default);
        setTheme(currentTheme);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "main oncreate");

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        insetsController.setAppearanceLightStatusBars(sharedPref.getBoolean(getString(R.string.key_is_light_theme), false));

        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);

        //StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build());
        int permissionLevel = hasPermissions();
        if (permissionLevel == 0 || permissionLevel == 1)
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        else
            checkForUpdate();
    }

    private int hasPermissions() {
        boolean permStorage = ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
        boolean permPhone = ContextCompat.checkSelfPermission(this, PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
        if (!permStorage && !permPhone) {
            return 0;
        } else if (!permStorage) {
            return 1;
        } else if (!permPhone) {
            return 2;
        }
        return 3;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionLevel = hasPermissions();
        if (permissionLevel == 2 || permissionLevel == 3)
            checkForUpdate();
    }

    private void checkForUpdate() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int dbVersion = sharedPref.getInt(getString(R.string.db_version), 1);

        if (dbVersion < DATABASE_VERSION) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.db_version), DATABASE_VERSION);
            editor.apply();
        }
        AppDatabase appDatabase = AppDatabase.getInstance(this);
        Executors.newSingleThreadExecutor().execute(new FillDbRunnable(this, this, appDatabase));
    }

    @Override
    public void onFinish() {
        start();
    }

    public void start() {
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(2);
        pagerAdapter = new CustomViewPager(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(MAIN_POS, false);
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewPager.getCurrentItem() == MAIN_POS) {
                    finish();
                } else if (viewPager.getCurrentItem() == CAT_POS) {
                    CategoryFragment fragment = (CategoryFragment) pagerAdapter.getRegisteredFragment(CAT_POS);
                    if (!fragment.onBack()) {
                        viewPager.setCurrentItem(MAIN_POS, true);
                    }
                } else {
                    viewPager.setCurrentItem(MAIN_POS, true);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        activityResultLauncher.unregister();
        if (viewPager != null)
            viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "main onpause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "main onresume");
        if (pagerAdapter != null && time != 0) {
            MainFragment fragment2 = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
            if (fragment2 != null)
                fragment2.onAppForeground();
        }
        time = 1;
    }

    @Override
    public void changeList(int categoryId, String name) {
        MainFragment fragment = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
        fragment.updateList(categoryId, name, null, false);
    }

    @Override
    public void disableButtons(boolean buttonEnable) {
        PlayerFragment fragment = (PlayerFragment) pagerAdapter.getRegisteredFragment(PLAY_POS);
        fragment.disableButtons(buttonEnable);
    }

    @Override
    public void swipeToMain() {
        if (viewPager.getCurrentItem() == MAIN_POS)
            viewPager.setCurrentItem(CAT_POS, false);
        viewPager.setCurrentItem(MAIN_POS, true);
    }

    @Override
    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        activityResultLauncher.launch(intent);
    }

    @Override
    public void changeCurrentMusic(long id) {
        MainFragment fragment = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
        fragment.changeCurrentMusic(id);
    }

    @Override
    public void changeListOrder(String sort) {
        MainFragment fragment = (MainFragment) pagerAdapter.getRegisteredFragment(MAIN_POS);
        fragment.updateList(-1, null, sort, false);
    }

    @Override
    public void updateListInService(ArrayList<Long> idList) {
        PlayerFragment fragment = (PlayerFragment) pagerAdapter.getRegisteredFragment(PLAY_POS);
        fragment.updateListInService(idList);
    }

    //On crash copy stack trace
    public void handleUncaughtException(Thread thread, Throwable e) {
        String exStackTrace = Log.getStackTraceString(e);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, "Crash Logs " + currentDate);
        i.putExtra(Intent.EXTRA_TEXT, "" + exStackTrace);
        try {
            startActivity(Intent.createChooser(i, "Send"));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "Cant send");
        }

        try {
            Thread.sleep(3000);
            finish();
            System.exit(0);
        } catch (InterruptedException interruptedException) {
            finish();
            System.exit(0);
        }
    }
}