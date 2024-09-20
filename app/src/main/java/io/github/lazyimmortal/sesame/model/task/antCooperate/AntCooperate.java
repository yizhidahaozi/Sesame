package io.github.lazyimmortal.sesame.model.task.antCooperate;

import org.json.JSONArray;
import org.json.JSONObject;
import io.github.lazyimmortal.sesame.data.ModelFields;
import io.github.lazyimmortal.sesame.data.ModelGroup;
import io.github.lazyimmortal.sesame.data.modelFieldExt.BooleanModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.SelectAndCountModelField;
import io.github.lazyimmortal.sesame.data.task.ModelTask;
import io.github.lazyimmortal.sesame.entity.CooperateUser;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.util.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AntCooperate extends ModelTask {
    private static final String TAG = AntCooperate.class.getSimpleName();

    @Override
    public String getName() {
        return "合种";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.FOREST;
    }

    private final BooleanModelField cooperateWater = new BooleanModelField("cooperateWater", "合种浇水", false);
    private final SelectAndCountModelField cooperateWaterList = new SelectAndCountModelField("cooperateWaterList", "合种浇水列表", new LinkedHashMap<>(), CooperateUser::getList);
    private final SelectAndCountModelField cooperateWaterTotalLimitList = new SelectAndCountModelField("cooperateWaterTotalLimitList", "浇水总量限制列表", new LinkedHashMap<>(), CooperateUser::getList);
    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(cooperateWater);
        modelFields.addField(cooperateWaterList);
        modelFields.addField(cooperateWaterTotalLimitList);
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            if (cooperateWater.getValue()) {
                cooperateWater();
            }
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean checkMessage(JSONObject jo) {
        try {
            if (!"SUCCESS".equals(jo.optString("resultCode"))) {
                if (jo.has("resultDesc")) {
                    Log.record(jo.getString("resultDesc"));
                    Log.i(jo.getString("resultDesc"), jo.toString());
                } else {
                    Log.i(TAG, jo.toString());
                }
                return false;
            }
            return true;
        } catch (Throwable t) {
            Log.i(TAG, "checkMessage err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void cooperateWater() {
        try {
            JSONObject jo = new JSONObject(AntCooperateRpcCall.queryUserCooperatePlantList());
            if (!checkMessage(jo)) {
                return;
            }
            String selfId = UserIdMap.getCurrentUid();
            JSONArray cooperatePlants = jo.getJSONArray("cooperatePlants");
            for (int i = 0; i < cooperatePlants.length(); i++) {
                jo = cooperatePlants.getJSONObject(i);
                String cooperationId = jo.getString("cooperationId");
                int energyCount = calculatedWaterNum(selfId, cooperationId);
                String name = cooperatePlantList.get(cooperationId);
                CooperationIdMap.add(cooperationId, name);
                if (energyCount == 0) {
                    continue;
                }
                cooperateWater(selfId, cooperationId, energyCount, name);
                TimeUtil.sleep(1000);
            }
            CooperationIdMap.save(selfId);
        } catch (Throwable t) {
            Log.i(TAG, "cooperateWater err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void cooperateWater(String uid, String cooperationId, int energyCount, String name) {
        try {
            JSONObject jo = new JSONObject(AntCooperateRpcCall.cooperateWater(uid, cooperationId, energyCount));
            if (checkMessage(jo)) {
                Log.forest("合种浇水🚿[" + name + "]" + jo.getString("barrageText"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "cooperateWater err:");
            Log.printStackTrace(TAG, t);
        }
    }

    Map<String, String> cooperatePlantList = new HashMap<>();
    private int calculatedWaterNum(String uid, String cooperationId) {
        try {
            JSONObject jo = new JSONObject(AntCooperateRpcCall.queryCooperatePlant(cooperationId));
            if (!checkMessage(jo)) {
                return 0;
            }
            int userCurrentEnergy = jo.getInt("userCurrentEnergy");
            jo = jo.getJSONObject("cooperatePlant");
            int waterDayLimit = jo.getInt("waterDayLimit");
            String name = jo.getString("name");
            cooperatePlantList.put(cooperationId, name);

            Integer num = cooperateWaterList.getValue().get(cooperationId);
            int dayWater = getDayWater(uid, cooperationId);
            if (num == null || num - dayWater < 10) {
                return 0;
            }
            num = Math.min(num - dayWater, waterDayLimit);
            Integer limitNum = cooperateWaterTotalLimitList.getValue().get(cooperationId);
            if (limitNum != null) {
                num = Math.min(num, limitNum - getAllWater(uid, cooperationId));
            }
            if (num > userCurrentEnergy) {
                return 0;
            }
            return num < 10 ? 0 : num;
        } catch (Throwable t) {
            Log.i(TAG, "calculatedWaterNum err:");
            Log.printStackTrace(TAG, t);
        }
        return 0;
    }

    private int getDayWater(String uid, String cooperationId) {
        try {
            JSONObject jo = new JSONObject(AntCooperateRpcCall.queryCooperateRank("D", cooperationId));
            if (!checkMessage(jo)) {
                return 0;
            }
            JSONArray cooperateRankInfos = jo.getJSONArray("cooperateRankInfos");
            for (int i = 0; i < cooperateRankInfos.length(); i++) {
                jo = cooperateRankInfos.getJSONObject(i);
                String userId = jo.getString("userId");
                if (Objects.equals(userId, uid)) {
                    return jo.optInt("energySummation");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "getDayWater err:");
            Log.printStackTrace(TAG, t);
        }
        return 0;
    }

    private int getAllWater(String uid, String cooperationId) {
        try {
            JSONObject jo = new JSONObject(AntCooperateRpcCall.queryCooperateRank("A", cooperationId));
            if (!checkMessage(jo)) {
                return 0;
            }
            JSONArray cooperateRankInfos = jo.getJSONArray("cooperateRankInfos");
            for (int i = 0; i < cooperateRankInfos.length(); i++) {
                jo = cooperateRankInfos.getJSONObject(i);
                String userId = jo.getString("userId");
                if (Objects.equals(userId, uid)) {
                    return jo.optInt("energySummation");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "getAllWater err:");
            Log.printStackTrace(TAG, t);
        }
        return 0;
    }
}
