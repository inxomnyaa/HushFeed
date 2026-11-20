/*
 * Copyright 2026 Hushfeed contributors
 * https://github.com/SysAdminDoc/hushfeed
 */
package app.morphe.extension.tiktok.font;

import android.content.Context;
import android.content.res.Configuration;

import app.morphe.extension.shared.Logger;
import app.morphe.extension.tiktok.settings.Settings;

/**
 * Shrinks TikTok's whole interface. Every activity's context gets a lower density, so layouts,
 * icons and text (TikTok's Tux text is sized in dp) all get smaller together.
 */
@SuppressWarnings("unused")
public final class UiScale {
    private UiScale() {}

    /** An activity's base context with the UI size picked in Hushfeed settings applied. */
    public static Context wrap(Context base) {
        try {
            int percent = Settings.UI_SCALE_PERCENT.get();
            if (base == null || percent <= 0 || percent >= 100) return base;
            float factor = percent / 100f;
            Configuration config = new Configuration(base.getResources().getConfiguration());
            config.densityDpi = Math.round(config.densityDpi * factor);
            config.screenWidthDp = Math.round(config.screenWidthDp / factor);
            config.screenHeightDp = Math.round(config.screenHeightDp / factor);
            config.smallestScreenWidthDp = Math.round(config.smallestScreenWidthDp / factor);
            return base.createConfigurationContext(config);
        } catch (Throwable failure) {
            Logger.printException(() -> "UI scale failed", failure);
            return base;
        }
    }
}
