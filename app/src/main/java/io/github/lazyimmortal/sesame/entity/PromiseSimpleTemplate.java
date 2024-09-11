package io.github.lazyimmortal.sesame.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.lazyimmortal.sesame.util.PromiseSimpleTemplateIdMap;

public class PromiseSimpleTemplate extends IdAndName {
    private static final String TAG = PromiseSimpleTemplate.class.getSimpleName();
    private static List<PromiseSimpleTemplate> list;

    public PromiseSimpleTemplate(String i, String n) {
        id = i;
        name = n;
    }

    public static List<PromiseSimpleTemplate> getList() {
        if (list == null) {
            list = new ArrayList<>();
            Set<Map.Entry<String, String>> idSet = PromiseSimpleTemplateIdMap.getMap().entrySet();
            for (Map.Entry<String, String> entry: idSet) {
                list.add(new PromiseSimpleTemplate(entry.getKey(), entry.getValue()));
            }
        }
        return list;
//        if (list == null) {
//            JSONArray ja;
//            String promiseSimpleTemplate = "[" +
//                    "{\"templateId\":\"save_energy_new\",\"promiseName\":\"坚持在蚂蚁森林收能量\"}," +
//                    "{\"templateId\":\"mazy_feed_animal_new\",\"promiseName\":\"坚持在蚂蚁庄园喂小鸡\"}," +
//                    "{\"templateId\":\"spread_manure_new\",\"promiseName\":\"坚持在芭芭农场施肥\"}," +
//                    "{\"templateId\":\"collect_village_coin_new\",\"promiseName\":\"坚持收木兰币\"}," +
//                    "{\"templateId\":\"go_alipay_sports_route\",\"promiseName\":\"坚持锻炼，走运动路线\"}," +
//                    "{\"templateId\":\"collect_member_point\",\"promiseName\":\"坚持领会员积分\"}," +
//                    "{\"templateId\":\"xiaofeijin_visit\",\"promiseName\":\"坚持攒消费金金币\"}," +
//                    "{\"templateId\":\"save_ins_universal\",\"promiseName\":\"坚持攒保障金\"}" +
//                    "]";
//            try {
//                ja = new JSONArray(promiseSimpleTemplate);
//            } catch (JSONException ex) {
//                ja = new JSONArray();
//            }
//            list = new ArrayList<>();
//            try {
//                for (int i = 0; i < ja.length(); i++) {
//                    JSONObject jo = ja.getJSONObject(i);
//                    list.add(new PromiseSimpleTemplate(jo.getString("templateId"), jo.getString("promiseName")));
//                }
//            } catch (Throwable th) {
//                Log.printStackTrace(TAG, th);
//            }
//        }
//        return list;
    }
}
