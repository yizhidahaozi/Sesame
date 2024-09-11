package io.github.lazyimmortal.sesame.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.lazyimmortal.sesame.util.AntFarmOrnamentsIdMap;

public class AntFarmOrnaments extends IdAndName{
    private static final String TAG = AntFarmOrnaments.class.getSimpleName();
    private static List<AntFarmOrnaments> list;

    public AntFarmOrnaments(String i, String n) {
        id = i;
        name = n;
    }

    public static List<AntFarmOrnaments> getList() {
        if (list == null) {
            list = new ArrayList<>();
            Set<Map.Entry<String, String>> idSet = AntFarmOrnamentsIdMap.getMap().entrySet();
            for (Map.Entry<String, String> entry: idSet) {
                list.add(new AntFarmOrnaments(entry.getKey(), entry.getValue()));
            }
        }
        return list;
    }
}
