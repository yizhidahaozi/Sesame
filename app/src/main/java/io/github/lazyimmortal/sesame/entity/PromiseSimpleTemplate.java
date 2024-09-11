package io.github.lazyimmortal.sesame.entity;

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
    }
}
