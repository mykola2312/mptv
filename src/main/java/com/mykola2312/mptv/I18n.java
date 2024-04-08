package com.mykola2312.mptv;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle bundle;

    public static String get(String key) {
        if (bundle == null) {
            String locale = Locale.getDefault().toString();
            bundle = ResourceBundle.getBundle("i18n_" + locale);
            if (bundle == null) {
                bundle = ResourceBundle.getBundle("i18n_en_US");
            }
        }

        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println(e.toString());
            return key;
        }
    }
}
