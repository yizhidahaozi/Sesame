package io.github.lazyimmortal.sesame.model.task.protectEcology;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.github.lazyimmortal.sesame.data.ModelFields;
import io.github.lazyimmortal.sesame.data.ModelGroup;
import io.github.lazyimmortal.sesame.data.modelFieldExt.BooleanModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.SelectAndCountModelField;
import io.github.lazyimmortal.sesame.data.task.ModelTask;
import io.github.lazyimmortal.sesame.entity.AlipayBeach;
import io.github.lazyimmortal.sesame.entity.AlipayReserve;
import io.github.lazyimmortal.sesame.entity.AlipayTree;
import io.github.lazyimmortal.sesame.entity.CooperateUser;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.util.*;
import io.github.lazyimmortal.sesame.util.idMap.*;

public class ProtectEcology extends ModelTask {
    private static final String TAG = ProtectEcology.class.getSimpleName();

    @Override
    public String getName() {
        return "生态保护";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.FOREST;
    }

    private static BooleanModelField cooperateWater;
    private static SelectAndCountModelField cooperateWaterList;
    private static SelectAndCountModelField cooperateWaterTotalLimitList;
    private static BooleanModelField protectTree;
    private static SelectAndCountModelField protectTreeList;
    private static BooleanModelField protectReserve;
    private static SelectAndCountModelField protectReserveList;
    private static BooleanModelField protectBeach;
    private static SelectAndCountModelField protectBeachList;
    private static BooleanModelField protectAnimal;
    private static BooleanModelField protectMarathon;
    private static BooleanModelField protectAncientTree;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(cooperateWater = new BooleanModelField("cooperateWater", "合种 | 浇水", false));
        modelFields.addField(cooperateWaterList = new SelectAndCountModelField("cooperateWaterList", "合种 | 日浇水量列表", new LinkedHashMap<>(), CooperateUser::getList));
        modelFields.addField(cooperateWaterTotalLimitList = new SelectAndCountModelField("cooperateWaterTotalLimitList", "合种 | 总浇水量列表", new LinkedHashMap<>(), CooperateUser::getList));
        modelFields.addField(protectTree = new BooleanModelField("protectTree", "保护森林 | 植树(总数)", false));
        modelFields.addField(protectTreeList = new SelectAndCountModelField("protectTreeList", "保护森林 | 植树列表", new LinkedHashMap<>(), AlipayTree::getList));
        modelFields.addField(protectReserve = new BooleanModelField("protectReserve", "保护动物 | 保护地(每天)", false));
        modelFields.addField(protectReserveList = new SelectAndCountModelField("reserveList", "保护动物 | 保护地列表", new LinkedHashMap<>(), AlipayReserve::getList));
        modelFields.addField(protectBeach = new BooleanModelField("protectBeach", "保护海洋 | 海滩(总数)", false));
        modelFields.addField(protectBeachList = new SelectAndCountModelField("protectOceanList", "保护海洋 | 海滩列表", new LinkedHashMap<>(), AlipayBeach::getList));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        if (cooperateWater.getValue()) {
            cooperateWater();
        }
        if (protectTree.getValue()) {
            protectTree();
        }
        if (protectReserve.getValue()) {
            protectReserve();
        }
        if (protectBeach.getValue()) {
            protectBeach();
        }
    }

    public static void initForest() {
        try {
            JSONArray treeItems = queryTreeItemsForExchange("AVAILABLE", "project");
            if (treeItems == null) {
                return;
            }
            ReserveIdMap.load();
            for (int i = 0; i < treeItems.length(); i++) {
                JSONObject jo = treeItems.getJSONObject(i);
                String itemId = jo.getString("itemId");
                String itemName = jo.getString("itemName");
                if (Objects.equals("TREE", jo.getString("projectType"))) {
                    String organization = jo.getString("organization");
                    String region = jo.getString("region");
                    itemName = itemName + "[" + region + "|" + organization + "]";
                    TreeIdMap.add(itemId, itemName + "(" + jo.getInt("energy") + "g)");
                } else if (Objects.equals("RESERVE", jo.getString("projectType"))) {
                    ReserveIdMap.add(itemId, itemName + "(" + jo.getInt("energy") + "g)");
                }
            }
            TreeIdMap.save();
            ReserveIdMap.save();
        } catch (Throwable t) {
            Log.i(TAG, "initForest err:");
            Log.printStackTrace(TAG, t);
        }
    }

    public static void initOcean() {
        try {
            JSONArray cultivationList = queryCultivationList();
            if (cultivationList == null) {
                return;
            }
            for (int i = 0; i < cultivationList.length(); i++) {
                JSONObject jo = cultivationList.getJSONObject(i);
                if (Objects.equals("AVAILABLE", jo.getString("applyAction"))) {
                    continue;
                }
                if (Objects.equals("BEACH", jo.optString("templateSubType"))) {
                    BeachIdMap.add(jo.getString("templateCode"), jo.getString("cultivationName") + "(" + jo.getInt("energy") + "g)");
                } else if (Objects.equals("COOPERATE_PLANT", jo.getString("templateType"))) {
                    BeachIdMap.add(jo.getString("templateCode"), jo.getString("cultivationName") + "(" + jo.getInt("energy") + "g)");
                } else if (Objects.equals("PROTECT", jo.getString("templateType"))) {
                    BeachIdMap.add(jo.getString("templateCode"), jo.getString("cultivationName") + "(" + jo.getInt("energy") + "g)");
                }
            }
            BeachIdMap.load();
        } catch (Throwable t) {
            Log.i(TAG, "initOcean err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void cooperateWater() {
        try {
            JSONObject jo = new JSONObject(CooperateRpcCall.queryUserCooperatePlantList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            String userId = UserIdMap.getCurrentUid();
            JSONArray cooperatePlants = jo.getJSONArray("cooperatePlants");
            for (int i = 0; i < cooperatePlants.length(); i++) {
                jo = cooperatePlants.getJSONObject(i);
                String cooperationId = jo.getString("cooperationId");
                queryCooperatePlant(userId, cooperationId);
            }
            CooperationIdMap.save(userId);
        } catch (Throwable t) {
            Log.i(TAG, "cooperateWater err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryCooperatePlant(String userId, String cooperationId) {
        try {
            JSONObject jo = new JSONObject(CooperateRpcCall.queryCooperatePlant(cooperationId));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            int userCurrentEnergy = jo.getInt("userCurrentEnergy");
            jo = jo.getJSONObject("cooperatePlant");
            String name = jo.getString("name");
            CooperationIdMap.add(cooperationId, name);
            int waterDayLimit = jo.getInt("waterDayLimit");
            int energyCount = getEnergyCount(userId, cooperationId, waterDayLimit);
            if (energyCount > 0 && energyCount <= userCurrentEnergy) {
                if (cooperateWater(userId, cooperationId, energyCount, name)) {
                    TimeUtil.sleep(300);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryCooperatePlant err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static Boolean cooperateWater(String userId, String cooperationId, int energyCount, String name) {
        try {
            JSONObject jo = new JSONObject(CooperateRpcCall.cooperateWater(userId, cooperationId, energyCount));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                Log.forest("合种浇水🚿[" + name + "]" + jo.getString("barrageText"));
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "cooperateWater err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private static int getEnergyCount(String userId, String cooperationId, int waterDayLimit) {
        Integer waterNum = cooperateWaterList.getValue().get(cooperationId);
        if (waterNum == null) {
            return 0;
        }
        int dayWater = getEnergySummation("D", cooperationId, userId);
        int allWater = getEnergySummation("A", cooperationId, userId);
        int energyCount = Math.min(waterNum - dayWater, waterDayLimit);
        Integer limitNum = cooperateWaterTotalLimitList.getValue().get(cooperationId);
        if (limitNum != null) {
            energyCount = Math.min(waterNum, limitNum - allWater);
        }
        return energyCount < 10 ? 0 : energyCount;
    }

    private static int getEnergySummation(String bizType, String cooperationId, String userId) {
        try {
            JSONObject jo = new JSONObject(CooperateRpcCall.queryCooperateRank(bizType, cooperationId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                JSONArray cooperateRankInfos = jo.getJSONArray("cooperateRankInfos");
                for (int i = 0; i < cooperateRankInfos.length(); i++) {
                    jo = cooperateRankInfos.getJSONObject(i);
                    if (Objects.equals(userId, jo.getString("userId"))) {
                        return jo.optInt("energySummation");
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "getEnergySummation err:");
            Log.printStackTrace(TAG, t);
        }
        return 0;
    }

    private static void protectTree() {
        Map<String, Integer> map = protectTreeList.getValue();
        try {
            JSONArray treeItems = queryTreeItemsForExchange("AVAILABLE", "project");
            if (treeItems == null) {
                return;
            }
            for (int i = 0; i < treeItems.length(); i++) {
                JSONObject jo = treeItems.getJSONObject(i);
                int projectId = jo.getInt("projectId");
                int certCountForAlias = jo.getInt("certCountForAlias");
                Integer count = map.get(String.valueOf(projectId));
                if (count == null) {
                    continue;
                }
                while (count > certCountForAlias && queryTreeForExchange(projectId)) {
                    certCountForAlias++;
                    TimeUtil.sleep(300);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "protectTree err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void protectReserve() {
        Map<String, Integer> map = protectReserveList.getValue();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            Integer count = entry.getValue();
            if (count == null || count < 0) {
                continue;
            }
            int projectId = Integer.parseInt(entry.getKey());
            while (Status.canExchangeReserveToday(projectId, count) && queryTreeForExchange(projectId)) {
                Status.exchangeReserveToday(projectId);
                TimeUtil.sleep(300);
            }
        }
    }

    public static JSONArray queryTreeItemsForExchange(String applyActions, String itemTypes) {
        try {
            JSONObject jo = new JSONObject(ProtectEcologyRpcCall.queryTreeItemsForExchange(applyActions, itemTypes));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                return jo.getJSONArray("treeItems");
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryTreeItemsForExchange err:");
            Log.printStackTrace(TAG, t);
        }
        return null;
    }

    private static Boolean queryTreeForExchange(int projectId) {
        try {
            JSONObject jo = new JSONObject(ProtectTreeRpcCall.queryTreeForExchange(projectId));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return false;
            }
            int currentEnergy = jo.getInt("currentEnergy");
            jo = jo.getJSONObject("exchangeableTree");
            int count = jo.getInt("certCountForAlias");
            String projectName = jo.getString("projectName");
            if (!Objects.equals("AVAILABLE", jo.getString("applyAction"))) {
                Log.record("生态保护🏕️保护[" + projectName + "]停止:数量不足");
                return false;
            }
            if (currentEnergy < jo.getInt("energy")) {
                Log.record("生态保护🏕️保护[" + projectName + "]停止:能量不足");
                return false;
            }
            if (Objects.equals("RESERVE", jo.getString("projectType"))) {
                count = Status.getExchangeReserveCountToday(projectId);
            }
            Log.forest("生态保护🏕️申请[" + projectName + "]第" + (count + 1) + "次");
            return exchangeTree(projectId, projectName);
        } catch (Throwable t) {
            Log.i(TAG, "queryTreeForExchange err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private static Boolean exchangeTree(int projectId, String projectName) {
        try {
            JSONObject jo = new JSONObject(ProtectTreeRpcCall.exchangeTree(projectId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                int vitalityAmount = jo.optInt("vitalityAmount", 0);
                Log.forest("生态保护🏕️保护[" + projectName + "]"
                        + (vitalityAmount > 0 ? "奖励[" + vitalityAmount + "活力值]" : ""));
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "exchangeTree err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private static JSONArray queryCultivationList() {
        try {
            JSONObject jo = new JSONObject(ProtectOceanRpcCall.queryCultivationList());
            if (MessageUtil.checkResultCode(TAG, jo)) {
                return jo.getJSONArray("cultivationItemVOList");
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryCultivationList err:");
            Log.printStackTrace(TAG, t);
        }
        return null;
    }

    private static void protectBeach() {
        Map<String, Integer> map = protectBeachList.getValue();
        try {
            JSONArray cultivationList = queryCultivationList();
            if (cultivationList == null) {
                return;
            }
            for (int i = 0; i < cultivationList.length(); i++) {
                JSONObject jo = cultivationList.getJSONObject(i);
                if (!Objects.equals("AVAILABLE", jo.getString("applyAction"))) {
                    continue;
                }
                String cultivationCode = jo.getString("cultivationCode");
                String projectCode = jo.getJSONObject("projectConfigVO").getString("code");
                int certNum = jo.getInt("certNum");
                Integer count = map.get(cultivationCode);
                if (count == null) {
                    continue;
                }
                while (count > certNum && queryCultivationDetail(cultivationCode, projectCode)) {
                    certNum++;
                    TimeUtil.sleep(300);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "protectBeach err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static Boolean queryCultivationDetail(String cultivationCode, String projectCode) {
        try {
            JSONObject jo = new JSONObject(ProtectOceanRpcCall.queryCultivationDetail(cultivationCode, projectCode));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return false;
            }
            int currentEnergy = jo.getJSONObject("userInfoVO").getInt("currentEnergy");
            jo = jo.getJSONObject("cultivationDetailVO");
            String cultivationName = jo.getString("cultivationName");
            if (!Objects.equals("AVAILABLE", jo.getString("applyAction"))) {
                Log.record("保护海洋🏖️保护[" + cultivationName + "]停止:数量不足");
                return false;
            }
            if (currentEnergy < jo.getInt("energy")) {
                Log.record("保护海洋🏖️保护[" + cultivationName + "]停止:能量不足");
                return false;
            }
            int count = jo.getInt("certNum") + 1;
            Log.forest("保护海洋🏖️申请[" + cultivationName + "]第" + count + "次");
            return oceanExchangeTree(cultivationCode, projectCode, cultivationName);
        } catch (Throwable t) {
            Log.i(TAG, "queryCultivationDetail err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private static Boolean oceanExchangeTree(String cultivationCode, String projectCode, String cultivationName) {
        try {
            JSONObject jo = new JSONObject(ProtectOceanRpcCall.oceanExchangeTree(cultivationCode, projectCode));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return false;
            }
            JSONArray awardInfos = jo.getJSONArray("rewardItemVOs");
            StringBuilder award = new StringBuilder();
            for (int i = 0; i < awardInfos.length(); i++) {
                jo = awardInfos.getJSONObject(i);
                if (i > 0) award.append(";");
                award.append(jo.getString("name")).append("*").append(jo.getInt("num"));
            }
            Log.forest("保护海洋🏖️保护[" + cultivationName + "]奖励[" + award + "]");
            return true;
        } catch (Throwable t) {
            Log.i(TAG, "oceanExchangeTree err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }
}
