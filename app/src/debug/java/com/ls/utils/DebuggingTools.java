package com.ls.utils;

import android.app.Application;
import com.facebook.stetho.Stetho;

public final class DebuggingTools {

    private DebuggingTools() {
    }

    public static void initialize(Application applicationContext) {
        Stetho.initializeWithDefaults(applicationContext);
    }
}
