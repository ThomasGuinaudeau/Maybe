package com.maybe.maybe.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.maybe.maybe.fragments.player.service.MediaPlayerService;

public class Methods {

    public static void newServiceIntent(Context context, String action, Bundle bundle) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(action);
        if (bundle != null)
            intent.putExtras(bundle);
        context.startService(intent);
    }

    public static String removeDiacritic(char c) {
        if (c >= '\u00c0' && c <= '\u017f') {
            c = Constants.tab00c0.charAt((int) c - '\u00c0');
        }
        return String.valueOf(c);
    }
}
