package com.chakfong.blog.utils;

import java.util.Locale;

public class LocaleUtils {

    private static final ThreadLocal<Locale> localeThreadLocal = ThreadLocal.withInitial(() -> Locale.CHINESE);

    public static void setLocale(String locale) {
        setLocale(new Locale(locale));
    }

    public static void setLocale(Locale locale) {
        localeThreadLocal.set(locale);
    }

    public static Locale getLocale() {
        return localeThreadLocal.get();
    }

    public static void clear() {
        localeThreadLocal.remove();
    }
}
