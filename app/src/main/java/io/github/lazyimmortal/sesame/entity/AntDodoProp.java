package io.github.lazyimmortal.sesame.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.lazyimmortal.sesame.util.FileUtil;
import io.github.lazyimmortal.sesame.util.Log;

public class AntDodoProp extends IdAndName {
    private static final String TAG = AntDodoProp.class.getSimpleName();
    private static List<AntDodoProp> list;

    public AntDodoProp(String i, String n) {
        id = i;
        name = n;
    }

    public static List<AntDodoProp> getList() {
        if (list == null) {
            String antDodoProp = FileUtil.readFromFile(FileUtil.getAntDodoPropFile());
            JSONArray ja;
            try {
                ja = new JSONArray(antDodoProp);
            } catch (Throwable e) {
                antDodoProp = "[" +
                        "{\"propType\":\"COLLECT_TIMES_7_DAYS\",\"propName\":\"抽卡道具\"}," +
                        "{\"propType\":\"COLLECT_HISTORY_ANIMAL_7_DAYS\",\"propName\":\"历史图鉴随机卡道具\"}," +
                        "{\"propType\":\"COLLECT_TO_FRIEND_TIMES_7_DAYS\",\"propName\":\"抽好友卡道具\"}," +
                        "{\"propType\":\"UNIVERSAL_CARD_7_DAYS\",\"propName\":\"万能卡道具\"}" +
                        "]";
                try {
                    ja = new JSONArray(antDodoProp);
                } catch (JSONException ex) {
                    ja = new JSONArray();
                }
            }

            list = new ArrayList<>();
            try {
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    list.add(new AntDodoProp(jo.getString("propType"), jo.getString("propName")));
                }
            } catch (Throwable th) {
                Log.printStackTrace(TAG, th);
            }
        }
        return list;
    }
}
