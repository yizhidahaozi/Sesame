package io.github.lazyimmortal.sesame.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.github.lazyimmortal.sesame.util.FileUtil;
import io.github.lazyimmortal.sesame.util.JsonUtil;
import io.github.lazyimmortal.sesame.util.Log;
import io.github.lazyimmortal.sesame.util.RandomUtil;
import io.github.lazyimmortal.sesame.util.StringUtil;
import lombok.Data;

@Data
public class TokenConfig {

    private static final String TAG = TokenConfig.class.getSimpleName();

    public static final TokenConfig INSTANCE = new TokenConfig();

    @JsonIgnore
    private boolean init;

    private Set<Map<String, String> > dishImageList = new HashSet<>();

    public static Map<String, String> getRandomDishImage() {
        List<Map<String, String> > list = new ArrayList<>(INSTANCE.dishImageList);
        if (list.isEmpty()) {
            return null;
        }
        int pos = RandomUtil.nextInt(0, list.size() - 1);
        Map<String, String> dishImage = list.get(pos);
        return checkDishImage(dishImage) ? dishImage : null;
    }

    public static void saveDishImage(Map<String, String> dishImage) {
        if (!checkDishImage(dishImage)) {
            return;
        }
        TokenConfig tokenConfig = INSTANCE;
        if (!tokenConfig.dishImageList.contains(dishImage)) {
            tokenConfig.dishImageList.add(dishImage);
            save();
        }
    }

    public static Boolean checkDishImage(Map<String, String> dishImage) {
        if (dishImage == null) {
            return false;
        }
        String beforeMealsImageId = dishImage.get("BEFORE_MEALS");
        String afterMealsImageId = dishImage.get("AFTER_MEALS");
        return !StringUtil.isEmpty(beforeMealsImageId)
                && !StringUtil.isEmpty(afterMealsImageId)
                && !Objects.equals(beforeMealsImageId, afterMealsImageId);
    }

    public static Boolean save() {
        Log.record("保存Token配置");
        return FileUtil.setTokenConfigFile(toSaveStr());
    }

    public static synchronized TokenConfig load() {
        File tokenConfigFile = FileUtil.getTokenConfigFile();
        try {
            if (tokenConfigFile.exists()) {
                String json = FileUtil.readFromFile(tokenConfigFile);
                JsonUtil.copyMapper().readerForUpdating(INSTANCE).readValue(json);
                String formatted = toSaveStr();
                if (formatted != null && !formatted.equals(json)) {
                    Log.i(TAG, "格式化Token配置");
                    Log.system(TAG, "格式化Token配置");
                    FileUtil.write2File(formatted, tokenConfigFile);
                }
            } else {
                unload();
                Log.i(TAG, "初始Token配置");
                Log.system(TAG, "初始Token配置");
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "重置Token配置");
            Log.system(TAG, "重置Token配置");
            try {
                unload();
                FileUtil.write2File(toSaveStr(), tokenConfigFile);
            } catch (Exception e) {
                Log.printStackTrace(TAG, t);
            }
        }
        INSTANCE.setInit(true);
        return INSTANCE;
    }

    public static synchronized void unload() {
        try {
            JsonUtil.copyMapper().updateValue(INSTANCE, new TokenConfig());
        } catch (JsonMappingException e) {
            Log.printStackTrace(TAG, e);
        }
    }

    public static String toSaveStr() {
        return JsonUtil.toFormatJsonString(INSTANCE);
    }
}
