package io.github.lazyimmortal.sesame.util;

import org.json.JSONObject;

public class LibraryUtil {
    private static final String TAG = LibraryUtil.class.getSimpleName();

    // native code
    private static native boolean libraryCheckFarmTaskStatus(JSONObject farmTask);
    public static Boolean checkFarmTaskStatus(JSONObject farmTask) {
        return libraryCheckFarmTaskStatus(farmTask); // 注释此行，重写实现
    }

    private static native boolean libraryDoFarmTask(JSONObject task);
    public static Boolean doFarmTask(JSONObject task) {
        return libraryDoFarmTask(task);
    }

    private static native boolean libraryDoFarmDrawTimesTask(JSONObject task);
    public static Boolean doFarmDrawTimesTask(JSONObject task) {
        return libraryDoFarmDrawTimesTask(task);
    }
}
