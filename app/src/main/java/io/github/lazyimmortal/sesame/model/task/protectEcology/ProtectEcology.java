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
import io.github.lazyimmortal.sesame.entity.CooperateUser;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.util.CooperationIdMap;
import io.github.lazyimmortal.sesame.util.Log;
import io.github.lazyimmortal.sesame.util.MessageUtil;
import io.github.lazyimmortal.sesame.util.Status;
import io.github.lazyimmortal.sesame.util.TimeUtil;
import io.github.lazyimmortal.sesame.util.UserIdMap;

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

    private BooleanModelField cooperateWater;
    private SelectAndCountModelField cooperateWaterList;
    private SelectAndCountModelField cooperateWaterTotalLimitList;
    private BooleanModelField protectTree;
    private BooleanModelField protectReserve;
    private SelectAndCountModelField protectReserveList;
    private BooleanModelField protectBeach;
    private SelectAndCountModelField protectBeachList;
    private BooleanModelField protectAnimal;
    private BooleanModelField protectMarathon;
    private BooleanModelField protectAncientTree;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(cooperateWater = new BooleanModelField("cooperateWater", "合种 | 浇水", false));
        modelFields.addField(cooperateWaterList = new SelectAndCountModelField("cooperateWaterList", "合种 | 日浇水量列表", new LinkedHashMap<>(), CooperateUser::getList));
        modelFields.addField(cooperateWaterTotalLimitList = new SelectAndCountModelField("cooperateWaterTotalLimitList", "合种 | 总浇水量列表", new LinkedHashMap<>(), CooperateUser::getList));
        modelFields.addField(protectReserve = new BooleanModelField("protectReserve", "保护动物 | 保护地", false));
        modelFields.addField(protectReserveList = new SelectAndCountModelField("reserveList", "保护动物 | 保护地列表", new LinkedHashMap<>(), AlipayReserve::getList));
        modelFields.addField(protectBeach = new BooleanModelField("protectBeach", "保护海洋 | 海滩", false));
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
        if (protectReserve.getValue()) {
            protectReserve();
        }
        if (protectBeach.getValue()) {
            queryCultivationList();
        }
    }

    private void cooperateWater() {
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

    private void queryCooperatePlant(String userId, String cooperationId) {
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

    private int getEnergyCount(String userId, String cooperationId, int waterDayLimit) {
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

    private static JSONArray queryTreeItemsForExchange(String applyActions, String itemTypes) {
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

    private void protectReserve() {
        Map<String, Integer> map = protectReserveList.getValue();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            Integer count = entry.getValue();
            if (count == null || count < 0) {
                continue;
            }
            int projectId = Integer.parseInt(entry.getKey());
            while (Status.canExchangeReserveToday(projectId, count) && queryTreeForExchange(projectId)) {
                TimeUtil.sleep(300);
            }
        }
    }

    private static Boolean queryTreeForExchange(int projectId) {
        try {
            JSONObject jo = new JSONObject(ReserveRpcCall.queryTreeForExchange(projectId));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return false;
            }
            int currentEnergy = jo.getInt("currentEnergy");
            jo = jo.getJSONObject("exchangeableTree");
            String projectName = jo.getString("projectName");
            if (!Objects.equals("AVAILABLE", jo.getString("applyAction"))) {
                Log.record("保护动物🏕️保护[" + projectName + "]停止:数量不足");
                if (currentEnergy >= jo.getInt("energy")) {
                    return true;
                }
            }
            if (currentEnergy < jo.getInt("energy")) {
                Log.record("保护动物🏕️保护[" + projectName + "]停止:能量不足");
            }
            return exchangeTree(projectId, projectName);
        } catch (Throwable t) {
            Log.i(TAG, "queryTreeForExchange err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private static Boolean exchangeTree(int projectId, String itemName) {
        try {
            JSONObject jo = new JSONObject(ReserveRpcCall.exchangeTree(projectId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                Status.exchangeReserveToday(projectId);
                int vitalityAmount = jo.optInt("vitalityAmount", 0);
                Log.forest("保护动物🏕️保护[" + itemName + "]第" + Status.getExchangeReserveCountToday(projectId) + "次"
                        + (vitalityAmount > 0 ? "奖励[" + vitalityAmount + "活力值]" : ""));
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "exchangeTree err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void queryCultivationList() {
        Map<String, Integer> map = protectBeachList.getValue();
        try {
            JSONObject jo = new JSONObject(BeachRpcCall.queryCultivationList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            int currentEnergy = jo.getJSONObject("userInfoVO").getInt("currentEnergy");
            JSONArray ja = jo.getJSONArray("cultivationItemVOList");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                if (!Objects.equals("AVAILABLE", jo.getString("applyAction"))) {
                    continue;
                }
                String cultivationName = jo.getString("cultivationName");
                String cultivationCode = jo.getString("cultivationCode");
                String projectCode = jo.getJSONObject("projectConfigVO").getString("code");
                int certNum = jo.getInt("certNum");
                Integer count = map.get(cultivationCode);
                if (count == null || count <= certNum) {
                    continue;
                }
                int energy = jo.getInt("energy");
                if (currentEnergy < energy) {
                    continue;
                }
                count = certNum + Math.min(currentEnergy / energy, count - certNum);
                count = oceanExchangeTree(cultivationCode, projectCode, cultivationName, certNum, count);
                currentEnergy -= energy * count;
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryCultivationList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static int oceanExchangeTree(String cultivationCode, String projectCode, String cultivationName, int certNum, int protectNum) {
        int count = 0;
        try {
            for (int i = certNum + 1; i <= protectNum; i++) {
                Log.forest("保护海洋🏖️支持[" + cultivationName + "]第" + i + "次");
                if (!oceanExchangeTree(cultivationCode, projectCode, cultivationName)) {
                    break;
                }
                count++;
                TimeUtil.sleep(300);
            }
        } catch (Throwable t) {
            Log.i(TAG, "oceanExchangeTree err:");
            Log.printStackTrace(TAG, t);
        }
        return count;
    }

    private static Boolean oceanExchangeTree(String cultivationCode, String projectCode, String cultivationName) {
        try {
            JSONObject jo = new JSONObject(BeachRpcCall.oceanExchangeTree(cultivationCode, projectCode));
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
            Log.forest("保护海洋🏖️保护[" + cultivationName + "]获得奖励[" + award + "]");
            return true;
        } catch (Throwable t) {
            Log.i(TAG, "oceanExchangeTree err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }
}
