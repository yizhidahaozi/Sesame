package io.github.lazyimmortal.sesame.util;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

import io.github.lazyimmortal.sesame.data.AppConfig;


public class LanguageUtil {
    public static Context setLocal(Context context) {
        AppConfig.load();
        if (AppConfig.INSTANCE.getLanguageSimplifiedChinese()) {
            // 忽略系统语言，强制使用简体中文
            Locale locale = new Locale("zh", "CN"); // 简体中文的区域代码
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        }
        return context;
    }
}
