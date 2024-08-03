package io.github.lazyimmortal.sesame.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.lazyimmortal.sesame.util.MemberBenefitIdMap;

public class MemberBenefit extends IdAndName {
    private static List<MemberBenefit> list;

    public MemberBenefit(String i, String n) {
        id = i;
        name = n;
    }

    public static List<MemberBenefit> getList() {
        if (list == null) {
            list = new ArrayList<>();
            Set<Map.Entry<String, String>> idSet = MemberBenefitIdMap.getMap().entrySet();
            for (Map.Entry<String, String> entry: idSet) {
                list.add(new MemberBenefit(entry.getKey(), entry.getValue()));
            }
        }
        return list;
    }

    public static void remove(String id) {
        getList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(id)) {
                list.remove(i);
                break;
            }
        }
    }
}
