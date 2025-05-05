package com.example.functioninglogin.NavDrawer;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper {

    public static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static Context setLocale(Context context) {
        String language = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("language", "en");
        return updateResources(context, language);
    }

    public static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}
