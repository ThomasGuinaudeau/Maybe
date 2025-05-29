package com.maybe.maybe.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;

import com.maybe.maybe.R;
import com.maybe.maybe.fragments.settings.ITheme;
import com.maybe.maybe.fragments.settings.SettingsFragment;
import com.maybe.maybe.fragments.settings.Theme;
import com.maybe.maybe.utils.CustomButton;

public class SettingsActivity extends AppCompatActivity implements ITheme {
    private Theme newTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int currentTheme = sharedPref.getInt(getString(R.string.key_theme), R.style.Dark_Default);
        setTheme(currentTheme);
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        insetsController.setAppearanceLightStatusBars(sharedPref.getBoolean(getString(R.string.key_is_light_theme), false));

        SettingsFragment settingsFragment = new SettingsFragment(this);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, settingsFragment)
                .commit();

        CustomButton restartButton = findViewById(R.id.restart_button);
        restartButton.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.edit()
                    .putInt(getString(R.string.key_theme), newTheme.getThemeResource())
                    .putBoolean(getString(R.string.key_is_light_theme), newTheme.isLight())
                    .commit();

            Context context = getApplicationContext();
            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
            ComponentName componentName = intent.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(componentName);
            mainIntent.setPackage(context.getPackageName());
            context.startActivity(mainIntent);
            Runtime.getRuntime().exit(0);
        });
        settingsFragment.setRestartButton(restartButton);
    }

    @Override
    public void setTheme(Theme theme) {
        newTheme = theme;
    }
}
