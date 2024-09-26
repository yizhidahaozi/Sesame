package io.github.lazyimmortal.sesame.model.task.antSports;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.lazyimmortal.sesame.data.ModelFields;
import io.github.lazyimmortal.sesame.data.ModelGroup;
import io.github.lazyimmortal.sesame.data.modelFieldExt.BooleanModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.ChoiceModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.IntegerModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.SelectModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.StringModelField;
import io.github.lazyimmortal.sesame.data.task.ModelTask;
import io.github.lazyimmortal.sesame.entity.AlipayUser;
import io.github.lazyimmortal.sesame.hook.ApplicationHook;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.util.*;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AntSports extends ModelTask {

    private static final String TAG = AntSports.class.getSimpleName();

    private int tmpStepCount = -1;
    private BooleanModelField walk;
    private BooleanModelField walkDayReward;
    private ChoiceModelField walkPathTheme;
    private BooleanModelField walkCustomPath;
    private StringModelField walkCustomPathId;
    private BooleanModelField receiveCoinAsset;
    private BooleanModelField donateCharityCoin;
    private ChoiceModelField donateCharityCoinType;
    private IntegerModelField donateCharityCoinAmount;
    private BooleanModelField coinExchangeDoubleCard;
    private IntegerModelField minExchangeCount;
    private IntegerModelField latestExchangeTime;
    private IntegerModelField syncStepCount;
    private BooleanModelField tiyubiz;
    private BooleanModelField battleForFriends;
    private ChoiceModelField trainItemType;
    private ChoiceModelField battleForFriendType;
    private SelectModelField originBossIdList;
    private BooleanModelField sportsTasks;

    @Override
    public String getName() {
        return "运动";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.SPORTS;
    }

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(walk = new BooleanModelField("walk", "行走路线 | 开启", false));
        modelFields.addField(walkDayReward = new BooleanModelField("walkDayReward", "行走路线 | 每日夺宝", false));
        modelFields.addField(walkPathTheme = new ChoiceModelField("walkPathTheme", "行走路线 | 路线主题", WalkPathTheme.DA_MEI_ZHONG_GUO, WalkPathTheme.nickNames));
        modelFields.addField(walkCustomPath = new BooleanModelField("walkCustomPath", "行走路线 | 开启自定义路线", false));
        modelFields.addField(walkCustomPathId = new StringModelField("walkCustomPathId", "行走路线 | 自定义路线代码(debug)", "p000202408231708"));
        modelFields.addField(sportsTasks = new BooleanModelField("sportsTasks", "运动任务", false));
        modelFields.addField(receiveCoinAsset = new BooleanModelField("receiveCoinAsset", "收运动币", false));
        modelFields.addField(donateCharityCoin = new BooleanModelField("donateCharityCoin", "捐运动币 | 开启", false));
        modelFields.addField(donateCharityCoinType = new ChoiceModelField("donateCharityCoinType", "捐运动币 | 方式", DonateCharityCoinType.ONE, DonateCharityCoinType.nickNames));
        modelFields.addField(donateCharityCoinAmount = new IntegerModelField("donateCharityCoinAmount", "捐运动币 | 数量(每次)", 100));
        modelFields.addField(coinExchangeDoubleCard = new BooleanModelField("coinExchangeDoubleCard", "运动币兑换限时能量双击卡", false));
        modelFields.addField(battleForFriends = new BooleanModelField("battleForFriends", "抢好友 | 开启", false));
        modelFields.addField(trainItemType = new ChoiceModelField("trainItemType", "抢好友 | 训练项目", TrainItemType.BARBELL, TrainItemType.nickNames));
        modelFields.addField(battleForFriendType = new ChoiceModelField("battleForFriendType", "抢好友 | 动作", BattleForFriendType.ROB, BattleForFriendType.nickNames));
        modelFields.addField(originBossIdList = new SelectModelField("originBossIdList", "抢好友 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(tiyubiz = new BooleanModelField("tiyubiz", "文体中心", false));
        modelFields.addField(minExchangeCount = new IntegerModelField("minExchangeCount", "行走捐 | 最小捐步步数", 0));
        modelFields.addField(latestExchangeTime = new IntegerModelField("latestExchangeTime", "行走捐 | 最晚捐步时间(24小时制)", 22));
        modelFields.addField(syncStepCount = new IntegerModelField("syncStepCount", "自定义同步步数", 22000));
        return modelFields;
    }

    @Override
    public void boot(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod("com.alibaba.health.pedometer.core.datasource.PedometerAgent", classLoader,
                    "readDailyStep", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            int originStep = (Integer) param.getResult();
                            int step = tmpStepCount();
                            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6 || originStep >= step) {
                                return;
                            }
                            param.setResult(step);
                        }
                    });
            Log.i(TAG, "hook readDailyStep successfully");
        } catch (Throwable t) {
            Log.i(TAG, "hook readDailyStep err:");
            Log.printStackTrace(TAG, t);
        }
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            if (Status.canSyncStepToday() && TimeUtil.isNowAfterOrCompareTimeStr("0600")) {
                addChildTask(new ChildModelTask("syncStep", () -> {
                    int step = tmpStepCount();
                    try {
                        ClassLoader classLoader = ApplicationHook.getClassLoader();
                        if ((Boolean) XposedHelpers.callMethod(XposedHelpers.callStaticMethod(classLoader.loadClass("com.alibaba.health.pedometer.intergation.rpc.RpcManager"), "a"), "a", new Object[]{step, Boolean.FALSE, "system"})) {
                            Log.other("同步步数🏃🏻‍♂️[" + step + "步]");
                        } else {
                            Log.record("同步运动步数失败:" + step);
                        }
                        Status.SyncStepToday();
                    } catch (Throwable t) {
                        Log.printStackTrace(TAG, t);
                    }
                }));
            }

            if (walk.getValue()) {
                walk();
            }

            if (donateCharityCoin.getValue())
                queryProjectList();

            if (coinExchangeDoubleCard.getValue()) {
                coinExchangeItem("AMS2024032927086104");
            }

            if (minExchangeCount.getValue() > 0)
                queryWalkStep();

            if (tiyubiz.getValue()) {
                userTaskGroupQuery("SPORTS_DAILY_SIGN_GROUP");
                userTaskGroupQuery("SPORTS_DAILY_GROUP");
                userTaskRightsReceive();
                pathFeatureQuery();
                participate();
            }

            if (battleForFriends.getValue()) {
                queryClubHome();
            }

            if (sportsTasks.getValue())
                sportsTasks();

            if (receiveCoinAsset.getValue())
                receiveCoinAsset();
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    public int tmpStepCount() {
        if (tmpStepCount >= 0) {
            return tmpStepCount;
        }
        tmpStepCount = syncStepCount.getValue();
        if (tmpStepCount > 0) {
            tmpStepCount = RandomUtil.nextInt(tmpStepCount, tmpStepCount + 2000);
            if (tmpStepCount > 100000) {
                tmpStepCount = 100000;
            }
        }
        return tmpStepCount;
    }

    // 运动
    private void sportsTasks() {
        try {
            signInCoinTask();
            queryCoinTaskPanel:
            do {
                TimeUtil.sleep(1000);
                JSONObject jo = new JSONObject(AntSportsRpcCall.queryCoinTaskPanel());
                if (!MessageUtil.checkSuccess(TAG, jo)) {
                    return;
                }
                jo = jo.getJSONObject("data");
                JSONArray taskList = jo.getJSONArray("taskList");
                for (int i = 0; i < taskList.length(); i++) {
                    jo = taskList.getJSONObject(i);

                    String taskStatus = jo.getString("taskStatus");
                    if (TaskStatus.HAS_RECEIVED.name().equals(taskStatus)) {
                        return;
                    }

                    String taskName = jo.getString("taskName");
                    if (TaskStatus.WAIT_RECEIVE.name().equals(taskStatus)) {
                        String assetId = jo.getString("assetId");
                        int prizeAmount = jo.getInt("prizeAmount");
                        if (!receiveCoinAsset(assetId, prizeAmount, taskName)) {
                            continue;
                        }
                        continue queryCoinTaskPanel;
                    }

                    if (!jo.has("taskAction")) {
                        continue;
                    }
                    if (TaskStatus.WAIT_COMPLETE.name().equals(taskStatus)) {
                        String taskAction = jo.getString("taskAction");
                        String taskId = jo.getString("taskId");
                        if (jo.optBoolean("multiTask")) {
                            int currentNum = jo.getInt("currentNum") + 1;
                            int limitConfigNum = jo.getInt("limitConfigNum");
                            taskName = taskName.replaceAll("（.*/.*）", "(" + currentNum + "/" + limitConfigNum + ")");
                        }
                        if (jo.optBoolean("needSignUp") && !signUpTask(taskId)) {
                            continue ;
                        }
                        if (!completeTask(taskAction, taskId, taskName)) {
                            continue;
                        }
                        if ("SHOW_AD".equals(taskAction)) {
                            TimeUtil.sleep(15000);
                        }
                        continue queryCoinTaskPanel;
                    }

                    Log.record("Found New Sport TaskStatus:" + taskStatus);
                }
                break;
            } while (true);
        } catch (Throwable t) {
            Log.i(TAG, "sportsTasks err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean signUpTask(String taskId) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.signUpTask(taskId));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "signUpTask err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean completeTask(String taskAction, String taskId, String taskName) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.completeTask(taskAction, taskId));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                Log.other("运动任务🧾完成[做任务得运动币:" + taskName + "]");
                jo = jo.getJSONObject("data");
                String assetId = jo.getString("assetId");
                int assetCoinAmount = jo.getInt("assetCoinAmount");
                receiveCoinAsset(assetId, assetCoinAmount, taskName);
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "completeTask err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void signInCoinTask() {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.signInCoinTask());

            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            JSONObject data = jo.getJSONObject("data");
            if (!data.getBoolean("signed")) {
                JSONObject subscribeConfig;
                if (data.has("subscribeConfig")) {
                    subscribeConfig = data.getJSONObject("subscribeConfig");
                    Log.other("运动任务🧾[做任务得运动币:签到"
                            + subscribeConfig.getString("subscribeExpireDays") + "天]奖励"
                            + data.getString("toast") + "运动币");
                } else {
//                        Log.record("没有签到");
                }
            } else {
                Log.record("运动签到今日已签到");
            }
        } catch (Throwable t) {
            Log.i(TAG, "signInCoinTask err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveCoinAsset() {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryCoinBubbleModule());
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            JSONObject data = jo.getJSONObject("data");
            if (!data.has("receiveCoinBubbleList"))
                return;
            JSONArray ja = data.getJSONArray("receiveCoinBubbleList");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                String assetId = jo.getString("assetId");
                int coinAmount = jo.getInt("coinAmount");
                String simpleSourceName = jo.optString("simpleSourceName");
                if (receiveCoinAsset(assetId, coinAmount, simpleSourceName)) {
                    TimeUtil.sleep(500);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveCoinAsset err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean receiveCoinAsset(String assetId, int coinAmount, String title) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.receiveCoinAsset(assetId, coinAmount));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                Log.other("收运动币💰领取[" + title + "]奖励[" + coinAmount + "运动币]");
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveCoinAsset err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    /*
     * 新版行走路线 -- begin
     */
    private void walk() {
        String goingPathId = queryGoingPathId();
        if (walkDayReward.getValue()) {
            String joinPathId = "p000202407261531001";
            if (checkJoinPathId(joinPathId)) {
                if (!joinPath(joinPathId)) {
                    return;
                }
                goingPathId = joinPathId;
            }
        }
        do {
            TimeUtil.sleep(1000);
            if (isNeedJoinNewPath(goingPathId)) {
                String joinPathId = queryJoinPathId();
                if (checkJoinPathId(joinPathId)) {
                    if (!joinPath(joinPathId)) {
                        return;
                    }
                    goingPathId = joinPathId;
                }
            }
        } while (walkGo(queryPath(goingPathId)));
    }

    private Boolean isNeedJoinNewPath(String goingPathId) {
        if (goingPathId.isEmpty()) {
            return true;
        }
        try {
            JSONObject jo = queryPath(goingPathId);
            jo = jo.getJSONObject("userPathStep");
            if (jo.optBoolean("dayLimit")) {
                return true;
            }
            String pathCompleteStatus = jo.getString("pathCompleteStatus");
            if (PathCompleteStatus.COMPLETED.name().equals(pathCompleteStatus)) {
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "isNeedJoinNewPath err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean hasTreasureBox() {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryMailList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return false;
            }
            JSONArray ja = jo.getJSONArray("userMailList");
            int count = 0;
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                if (!"SPORTSPROD_GOPATH_AWARD_BOX".equals(jo.getString("templateId"))) {
                    continue;
                }
                if (!TimeUtil.isToday(jo.getLong("receiveTime"))) {
                    break;
                }
                count++;
            }
            return count < 20;
        } catch (Throwable t) {
            Log.i(TAG, "hasTreasureBox err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean walkGo(JSONObject pathData) {
        try {
            JSONObject path = pathData.getJSONObject("path");
            JSONObject userPathStep = pathData.getJSONObject("userPathStep");
            int minGoStepCount = path.getInt("minGoStepCount");
            int pathStepCount = path.getInt("pathStepCount");
            if (path.has("dailyMaxGoStepCount")) {
                pathStepCount = path.getInt("dailyMaxGoStepCount");
            }
            int forwardStepCount = userPathStep.getInt("forwardStepCount");
            int remainStepCount = userPathStep.getInt("remainStepCount");
            boolean dayLimit = userPathStep.getBoolean("dayLimit");
            int useStepCount = Math.min(
                    Math.min(remainStepCount, hasTreasureBox() ? RandomUtil.nextInt(500, 1000) : remainStepCount),
                    Math.max(pathStepCount - forwardStepCount % pathStepCount, minGoStepCount)
            );
            if (useStepCount < minGoStepCount || dayLimit) {
                return false;
            }
            String pathId = path.getString("pathId");
            String pathName = path.getString("name");
            return walkGo(pathName, pathId, useStepCount);
        } catch (Throwable t) {
            Log.i(TAG, "walkGo err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean walkGo(String pathName, String pathId, int useStepCount) {
        boolean result = false;
        try {
            String date = Log.getFormatDate();
            JSONObject jo = new JSONObject(AntSportsRpcCall.walkGo(date, pathId, useStepCount));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                result = true;
                Log.other("行走路线🚶🏻‍♂️行走[" + pathName + "]#前进了" + useStepCount + "步");
                jo = jo.getJSONObject("data");
                if (jo.has("completeInfo")) {
                    Log.other("行走路线🚶🏻‍♂️完成[" + pathName + "]");
                }
                parseRewardsByJSONObjectData(jo);
            }
        } catch (Throwable t) {
            Log.i(TAG, "walkGo err:");
            Log.printStackTrace(TAG, t);
        }
        return result;
    }

    private JSONObject queryWorldMap(String themeId) {
        JSONObject theme = null;
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryWorldMap(themeId));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                theme = jo.getJSONObject("data");
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryWorldMap err:");
            Log.printStackTrace(TAG, t);
        }
        return theme;
    }

    private JSONObject queryCityPath(String cityId) {
        JSONObject city = null;
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryCityPath(cityId));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                city = jo.getJSONObject("data");
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryCityPath err:");
            Log.printStackTrace(TAG, t);
        }
        return city;
    }

    private JSONObject queryPath(String pathId) {
        JSONObject path = null;
        try {
            String date = Log.getFormatDate();
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryPath(date, pathId));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                path = jo.getJSONObject("data");
                parseRewardsByJSONObjectData(path);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryPath err:");
            Log.printStackTrace(TAG, t);
        }
        return path;
    }

    private void openTreasureBox(JSONArray treasureBoxList) {
        try {
            for (int i = 0; i < treasureBoxList.length(); i++) {
                JSONObject treasureBox = treasureBoxList.getJSONObject(i);
                receiveEvent(treasureBox.getString("boxNo"));
                TimeUtil.sleep(1000);
            }
        } catch (Throwable t) {
            Log.i(TAG, "openTreasureBox err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveEvent(String eventBillNo) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.receiveEvent(eventBillNo));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                jo = jo.getJSONObject("data");
                parseRewardsByJSONArrayRewards(jo.getJSONArray("rewards"), 0);
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveEvent err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void parseRewardsByJSONArrayRewards(JSONArray rewards, int rewardsType) {
        String rewardsTypeName;
        switch (rewardsType) {
            case 0:
                rewardsTypeName = "宝箱奖励";
                break;
            case 1:
                rewardsTypeName = "中奖奖励";
                break;
            case 2:
                rewardsTypeName = "终点奖励";
                break;
            default:
                rewardsTypeName = "未知奖励";
                break;
        }
        try {
            for (int i = 0; i < rewards.length(); i++) {
                JSONObject jo = rewards.getJSONObject(i);
                if (jo.has("rewardStatus")
                        && !"SUCCESS".equals(jo.getString("rewardStatus"))) {
                    // rewardStatus : SUCCESS NOT_HIT
                    continue;
                }
                Log.other("行走路线🚶🏻‍♂️收获" + rewardsTypeName
                        + "[" + jo.getString("rewardName") + "*" + jo.getInt("count") + "]"
                );
            }
        } catch (Throwable t) {
            Log.i(TAG, "parseRewardsByJSONArrayRewards err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void parseRewardsByJSONObjectData(JSONObject data) {
        try {
            JSONArray treasureBoxList = data.getJSONArray("treasureBoxList");
            openTreasureBox(treasureBoxList);
            if (data.has("brandRewardVOs")) {
                JSONArray brandRewardVOs = data.getJSONArray("brandRewardVOs");
                parseRewardsByJSONArrayRewards(brandRewardVOs, 1);
            }
            if (data.has("completeInfo")) {
                data = data.getJSONObject("completeInfo");
                JSONArray completeRewards = data.getJSONArray("completeRewards");
                parseRewardsByJSONArrayRewards(completeRewards, 2);
            }
        } catch (Throwable t) {
            Log.i(TAG, "parseRewardsByJSONObjectData err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private String queryGoingPathId() {
        String goingPathId = "";
        try {
            String date = Log.getFormatDate();
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryPath(date, ""));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                jo = jo.getJSONObject("data");
                goingPathId = jo.optString("goingPathId");
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryGoingPathId err:");
            Log.printStackTrace(TAG, t);
        }
        return goingPathId;
    }

    private String queryJoinPathId() {
        if (walkCustomPath.getValue()) {
            return walkCustomPathId.getValue();
        }

        String pathId = null;
        try {
            String themeId = WalkPathTheme.walkPathThemeIds[walkPathTheme.getValue()];
            JSONObject theme = queryWorldMap(themeId);
            if (theme == null) {
                return pathId;
            }
            JSONArray cityList = theme.getJSONArray("cityList");
            for (int i = 0; i < cityList.length(); i++) {
                String cityId = cityList.getJSONObject(i).getString("cityId");
                JSONObject city = queryCityPath(cityId);
                if (city == null) {
                    continue;
                }
                JSONArray cityPathList = city.getJSONArray("cityPathList");
                for (int j = 0; j < cityPathList.length(); j++) {
                    JSONObject cityPath = cityPathList.getJSONObject(j);
                    pathId = cityPath.getString("pathId");
                    String pathCompleteStatus = cityPath.getString("pathCompleteStatus");
                    if (!PathCompleteStatus.COMPLETED.name().equals(pathCompleteStatus)) {
                        return pathId;
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryJoinPathId err:");
            Log.printStackTrace(TAG, t);
        }
        return pathId;
    }

    private Boolean checkJoinPathId(String joinPathId) {
        try {
            JSONObject jo = queryPath(joinPathId);
            String goingPathId = jo.optString("goingPathId");
            if (Objects.equals(goingPathId, joinPathId)) {
                return false;
            }
            jo = jo.getJSONObject("userPathStep");
            return !jo.optBoolean("dayLimit");
        } catch (Throwable t) {
            Log.i(TAG, "checkJoinPathId err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean joinPath(String pathId) {
        if (pathId == null) {
            // 守护体育梦
            pathId = "p000202408231708";
        }
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.joinPath(pathId));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                JSONObject pathData = queryPath(pathId);
                String pathName = pathData.getJSONObject("path").getString("name");
                Log.other("行走路线🚶🏻‍♂️加入[" + pathName + "]");
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "joinPath err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    /*
     * 新版行走路线 -- end
     */
    private Boolean canDonateCharityCoinToday() {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryDonateRecord());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return false;
            }
            JSONArray footballFieldLongModel = jo.getJSONArray("footballFieldLongModel");
            if (footballFieldLongModel.length() == 0) {
                return true;
            }
            jo = footballFieldLongModel.getJSONObject(0);
            jo = jo.getJSONObject("personStatModel");
            long lastDonationTime = jo.getLong("lastDonationTime");
            if (TimeUtil.isLessThanNowOfDays(lastDonationTime)) {
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "canDonateCharityCoinToday err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void queryProjectList() {
        if (!canDonateCharityCoinToday()) {
            return;
        }
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryProjectList(0));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            int charityCoinCount = jo.getInt("charityCoinCount");
            int donateCharityCoin = donateCharityCoinAmount.getValue();
            if (charityCoinCount < donateCharityCoin) {
                return;
            }
            JSONArray ja = jo.getJSONObject("projectPage").getJSONArray("data");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i).getJSONObject("basicModel");
                // footballFieldStatus: OPENING_DONATE DONATE_COMPLETED
                if ("DONATE_COMPLETED".equals(jo.getString("footballFieldStatus"))) {
                    break;
                }
                if (donate(donateCharityCoin, jo.getString("projectId"), jo.getString("title"))) {
                    charityCoinCount -= donateCharityCoin;
                    if (donateCharityCoinType.getValue() == DonateCharityCoinType.ONE) {
                        break;
                    }
                    if (charityCoinCount < donateCharityCoin) {
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryProjectList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean donate(int donateCharityCoin, String projectId, String title) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.donate(donateCharityCoin, projectId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                Log.other("公益捐赠❤️[捐赠运动币:" + title + "]捐赠" + donateCharityCoin + "运动币");
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "donate err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean canDonateWalkExchangeToday() {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.donateExchangeRecord());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return false;
            }
            JSONArray userExchangeRecords = jo.getJSONArray("userExchangeRecords");
            if (userExchangeRecords.length() == 0) {
                return true;
            }
            jo = userExchangeRecords.getJSONObject(0);
            long gmtCreate = jo.getLong("gmtCreate");
            if (TimeUtil.isLessThanNowOfDays(gmtCreate)) {
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "canDonateWalkExchangeToday err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void queryWalkStep() {
        if (!canDonateWalkExchangeToday()) {
            return;
        }
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryWalkStep());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            jo = jo.getJSONObject("dailyStepModel");
            int produceQuantity = jo.getInt("produceQuantity");
            int hour = Integer.parseInt(Log.getFormatTime().split(":")[0]);
            if (produceQuantity < minExchangeCount.getValue() && hour < latestExchangeTime.getValue()) {
                return;
            }

            AntSportsRpcCall.walkDonateSignInfo(produceQuantity);
            jo = new JSONObject(AntSportsRpcCall.donateWalkHome(produceQuantity));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONObject walkDonateHomeModel = jo.getJSONObject("walkDonateHomeModel");
            JSONObject walkUserInfoModel = walkDonateHomeModel.getJSONObject("walkUserInfoModel");
            if (!walkUserInfoModel.has("exchangeFlag")) {
                return;
            }

            String donateToken = walkDonateHomeModel.getString("donateToken");
            JSONObject walkCharityActivityModel = walkDonateHomeModel.getJSONObject("walkCharityActivityModel");
            String activityId = walkCharityActivityModel.getString("activityId");

            jo = new JSONObject(AntSportsRpcCall.donateWalkExchange(activityId, produceQuantity, donateToken));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONObject donateExchangeResultModel = jo.getJSONObject("donateExchangeResultModel");
            int userCount = donateExchangeResultModel.getInt("userCount");
            double amount = donateExchangeResultModel.getJSONObject("userAmount").getDouble("amount");
            String donateTitle = donateExchangeResultModel.getString("donateTitle");
            Log.other("公益捐赠❤️[捐步做公益:" + donateTitle + "]捐赠" + userCount + "步,兑换" + amount + "元公益金");
        } catch (Throwable t) {
            Log.i(TAG, "queryWalkStep err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 文体中心 */// SPORTS_DAILY_SIGN_GROUP SPORTS_DAILY_GROUP
    private void userTaskGroupQuery(String groupId) {
        try {
            String s = AntSportsRpcCall.userTaskGroupQuery(groupId);
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                jo = jo.getJSONObject("group");
                JSONArray userTaskList = jo.getJSONArray("userTaskList");
                for (int i = 0; i < userTaskList.length(); i++) {
                    jo = userTaskList.getJSONObject(i);
                    if (!"TODO".equals(jo.getString("status")))
                        continue;
                    JSONObject taskInfo = jo.getJSONObject("taskInfo");
                    String bizType = taskInfo.getString("bizType");
                    String taskId = taskInfo.getString("taskId");
                    jo = new JSONObject(AntSportsRpcCall.userTaskComplete(bizType, taskId));
                    if (jo.optBoolean("success")) {
                        String taskName = taskInfo.optString("taskName", taskId);
                        Log.other("完成任务🧾[" + taskName + "]");
                    } else {
                        Log.record("文体每日任务" + " " + jo);
                    }
                }
            } else {
                Log.record("文体每日任务" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "userTaskGroupQuery err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void participate() {
        try {
            String s = AntSportsRpcCall.queryAccount();
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                double balance = jo.getDouble("balance");
                if (balance < 100)
                    return;
                jo = new JSONObject(AntSportsRpcCall.queryRoundList());
                if (jo.optBoolean("success")) {
                    JSONArray dataList = jo.getJSONArray("dataList");
                    for (int i = 0; i < dataList.length(); i++) {
                        jo = dataList.getJSONObject(i);
                        if (!"P".equals(jo.getString("status")))
                            continue;
                        if (jo.has("userRecord"))
                            continue;
                        JSONArray instanceList = jo.getJSONArray("instanceList");
                        int pointOptions = 0;
                        String roundId = jo.getString("id");
                        String InstanceId = null;
                        String ResultId = null;
                        for (int j = instanceList.length() - 1; j >= 0; j--) {
                            jo = instanceList.getJSONObject(j);
                            if (jo.getInt("pointOptions") < pointOptions)
                                continue;
                            pointOptions = jo.getInt("pointOptions");
                            InstanceId = jo.getString("id");
                            ResultId = jo.getString("instanceResultId");
                        }
                        jo = new JSONObject(AntSportsRpcCall.participate(pointOptions, InstanceId, ResultId, roundId));
                        if (jo.optBoolean("success")) {
                            jo = jo.getJSONObject("data");
                            String roundDescription = jo.getString("roundDescription");
                            int targetStepCount = jo.getInt("targetStepCount");
                            Log.other("走路挑战🚶🏻‍♂️[" + roundDescription + "]#" + targetStepCount);
                        } else {
                            Log.record("走路挑战赛" + " " + jo);
                        }
                    }
                } else {
                    Log.record("queryRoundList" + " " + jo);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "participate err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void userTaskRightsReceive() {
        try {
            String s = AntSportsRpcCall.userTaskGroupQuery("SPORTS_DAILY_GROUP");
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                jo = jo.getJSONObject("group");
                JSONArray userTaskList = jo.getJSONArray("userTaskList");
                for (int i = 0; i < userTaskList.length(); i++) {
                    jo = userTaskList.getJSONObject(i);
                    if (!"COMPLETED".equals(jo.getString("status")))
                        continue;
                    String userTaskId = jo.getString("userTaskId");
                    JSONObject taskInfo = jo.getJSONObject("taskInfo");
                    String taskId = taskInfo.getString("taskId");
                    jo = new JSONObject(AntSportsRpcCall.userTaskRightsReceive(taskId, userTaskId));
                    if (jo.optBoolean("success")) {
                        String taskName = taskInfo.optString("taskName", taskId);
                        JSONArray rightsRuleList = taskInfo.getJSONArray("rightsRuleList");
                        StringBuilder award = new StringBuilder();
                        for (int j = 0; j < rightsRuleList.length(); j++) {
                            jo = rightsRuleList.getJSONObject(j);
                            award.append(jo.getString("rightsName")).append("*").append(jo.getInt("baseAwardCount"));
                        }
                        Log.other("领取奖励🎖️[" + taskName + "]#" + award);
                    } else {
                        Log.record("文体中心领取奖励");
                        Log.i(jo.toString());
                    }
                }
            } else {
                Log.record("文体中心领取奖励");
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "userTaskRightsReceive err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void pathFeatureQuery() {
        try {
            String s = AntSportsRpcCall.pathFeatureQuery();
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                JSONObject path = jo.getJSONObject("path");
                String pathId = path.getString("pathId");
                String title = path.getString("title");
                int minGoStepCount = path.getInt("minGoStepCount");
                if (jo.has("userPath")) {
                    JSONObject userPath = jo.getJSONObject("userPath");
                    String userPathRecordStatus = userPath.getString("userPathRecordStatus");
                    if ("COMPLETED".equals(userPathRecordStatus)) {
                        pathMapHomepage(pathId);
                        pathMapJoin(title, pathId);
                    } else if ("GOING".equals(userPathRecordStatus)) {
                        pathMapHomepage(pathId);
                        String countDate = Log.getFormatDate();
                        jo = new JSONObject(AntSportsRpcCall.stepQuery(countDate, pathId));
                        if (jo.optBoolean("success")) {
                            int canGoStepCount = jo.getInt("canGoStepCount");
                            if (canGoStepCount >= minGoStepCount) {
                                String userPathRecordId = userPath.getString("userPathRecordId");
                                tiyubizGo(countDate, title, canGoStepCount, pathId, userPathRecordId);
                            }
                        }
                    }
                } else {
                    pathMapJoin(title, pathId);
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "pathFeatureQuery err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void pathMapHomepage(String pathId) {
        try {
            String s = AntSportsRpcCall.pathMapHomepage(pathId);
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                if (!jo.has("userPathGoRewardList"))
                    return;
                JSONArray userPathGoRewardList = jo.getJSONArray("userPathGoRewardList");
                for (int i = 0; i < userPathGoRewardList.length(); i++) {
                    jo = userPathGoRewardList.getJSONObject(i);
                    if (!"UNRECEIVED".equals(jo.getString("status")))
                        continue;
                    String userPathRewardId = jo.getString("userPathRewardId");
                    jo = new JSONObject(AntSportsRpcCall.rewardReceive(pathId, userPathRewardId));
                    if (jo.optBoolean("success")) {
                        jo = jo.getJSONObject("userPathRewardDetail");
                        JSONArray rightsRuleList = jo.getJSONArray("userPathRewardRightsList");
                        StringBuilder award = new StringBuilder();
                        for (int j = 0; j < rightsRuleList.length(); j++) {
                            jo = rightsRuleList.getJSONObject(j).getJSONObject("rightsContent");
                            award.append(jo.getString("name")).append("*").append(jo.getInt("count"));
                        }
                        Log.other("文体宝箱🎁[" + award + "]");
                    } else {
                        Log.record("文体中心开宝箱");
                        Log.i(jo.toString());
                    }
                }
            } else {
                Log.record("文体中心开宝箱");
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "pathMapHomepage err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void pathMapJoin(String title, String pathId) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.pathMapJoin(pathId));
            if (jo.optBoolean("success")) {
                Log.other("加入线路🚶🏻‍♂️[" + title + "]");
                pathFeatureQuery();
            } else {
                Log.i(TAG, jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "pathMapJoin err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void tiyubizGo(String countDate, String title, int goStepCount, String pathId,
                           String userPathRecordId) {
        try {
            String s = AntSportsRpcCall.tiyubizGo(countDate, goStepCount, pathId, userPathRecordId);
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                jo = jo.getJSONObject("userPath");
                Log.other("行走线路🚶🏻‍♂️[" + title + "]#前进了" + jo.getInt("userPathRecordForwardStepCount") + "步");
                pathMapHomepage(pathId);
                boolean completed = "COMPLETED".equals(jo.getString("userPathRecordStatus"));
                if (completed) {
                    Log.other("完成线路🚶🏻‍♂️[" + title + "]");
                    pathFeatureQuery();
                }
            } else {
                Log.i(TAG, s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "tiyubizGo err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 抢好友大战 */
    private void queryClubHome() {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryClubHome());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONArray roomList = jo.getJSONArray("roomList");
            for (int i = 0; i < roomList.length(); i++) {
                JSONObject room = roomList.getJSONObject(i);
                String roomId = room.getString("roomId");
                queryClubRoom(roomId);
                TimeUtil.sleep(1000);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryClubHome err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void queryClubRoom(String roomId) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryClubRoom(roomId));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            if (jo.has("bubbleList")) {
                JSONArray bubbleList = jo.getJSONArray("bubbleList");
                for (int i = 0; i < bubbleList.length(); i++) {
                    JSONObject bubble = bubbleList.getJSONObject(i);
                    collectBubble(bubble);
                }
            }
            JSONArray memberDetailList = jo.getJSONArray("memberDetailList");
            if (memberDetailList.length() == 0) {
                queryMemberPriceRanking(roomId);
                return;
            }
            for (int i = 0; i < memberDetailList.length(); i++) {
                JSONObject member = memberDetailList.getJSONObject(i);
                member = member.getJSONObject("memberModel");
                trainMember(member);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryClubRoom err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 抢好友大战-收集运动币
    private void collectBubble(JSONObject bubble) {
        try {
            String bubbleId = bubble.getString("bubbleId");
            JSONObject jo = new JSONObject(AntSportsRpcCall.collectBubble(bubbleId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                int collectCoin = jo.getInt("collectCoin");
                Log.other("训练好友💰️获得[" + collectCoin + "运动币]");
            }
        } catch (Throwable t) {
            Log.i(TAG, "collectBubble err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 抢好友大战-训练好友
    private void trainMember(JSONObject member) {
        try {
            String roomId = member.getString("roomId");
            String memberId = member.getString("memberId");
            String originBossId = member.getString("originBossId");
            JSONObject trainInfo = member.getJSONObject("trainInfo");

            String userName = UserIdMap.getMaskName(originBossId);
            if (!trainInfo.getBoolean("training")) {
                String itemType = TrainItemType.itemTypes[trainItemType.getValue()];
                JSONObject jo = new JSONObject(AntSportsRpcCall.trainMember(itemType, memberId, originBossId));
                if (!MessageUtil.checkResultCode(TAG, jo)) {
                    return;
                }
                String name = TrainItemType.nickNames[trainItemType.getValue()];
                Log.other("训练好友🥋训练[" + userName + "]" + name);
                trainInfo = jo.getJSONObject("trainInfo");
            }

            Long gmtEnd = trainInfo.getLong("gmtEnd");
            long updateTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
            addChildTask(new ChildModelTask(roomId, "", () -> {
                autoTrainMember(roomId, gmtEnd);
            }, updateTime));
        } catch (Throwable t) {
            Log.i(TAG, "trainMember err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 抢好友大战-蹲点训练
    private void autoTrainMember(String roomId, Long gmtEnd) {
        String taskId = "TRAIN|" + roomId;
        if (!hasChildTask(taskId)) {
            addChildTask(new ChildModelTask(taskId, "TRAIN", () -> {
                queryClubRoom(roomId);
            }, gmtEnd));
            int roomIdInt = Integer.parseInt(roomId.substring(2, 8));
            Log.record("添加蹲点训练🥋[" + roomIdInt + "号房嘉宾]在[" + TimeUtil.getCommonDate(gmtEnd) + "]执行");
        }
    }

    // 抢好友大战-抢购好友
    private void queryMemberPriceRanking(String roomId) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryMemberPriceRanking());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            int coinBalance = jo.getInt("coinBalance");
            jo = jo.getJSONObject("rank");
            JSONArray ja = jo.getJSONArray("data");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                int price = jo.getInt("price");
                if (price > coinBalance) {
                    continue;
                }
                if (buyMember(roomId, jo)) {
                    return;
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryMemberPriceRanking err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private JSONObject queryClubMember(String memberId, String originBossId) {
        JSONObject priceInfo = null;
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryClubMember(memberId, originBossId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                priceInfo = jo.getJSONObject("member").getJSONObject("priceInfo");
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryClubMember err:");
            Log.printStackTrace(TAG, t);
        }
        return priceInfo;
    }

    private Boolean buyMember(String roomId, JSONObject member) {
        try {
            String originBossId = member.getString("originBossId");
            boolean isBattleForFriend = originBossIdList.getValue().contains(originBossId);
            if (battleForFriendType.getValue() == BattleForFriendType.DONT_ROB) {
                isBattleForFriend = !isBattleForFriend;
            }
            if (!isBattleForFriend) {
                return false;
            }

            String currentBossId = member.getString("currentBossId");
            String memberId = member.getString("memberId");
            int price = member.getInt("price");
            JSONObject priceInfo = queryClubMember(memberId, originBossId);
            if (priceInfo == null) {
                return false;
            }

            JSONObject jo = new JSONObject(AntSportsRpcCall.buyMember(currentBossId, memberId, originBossId, priceInfo, roomId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                String userName = UserIdMap.getMaskName(originBossId);
                Log.other("抢购好友🥋抢购[" + userName + "]花费[" + price + "运动币]");
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "buyMember err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void coinExchangeItem(String itemId) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryItemDetail(itemId));
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            jo = jo.getJSONObject("data");
            if (!"OK".equals(jo.optString("exchangeBtnStatus"))) {
                return;
            }
            jo = jo.getJSONObject("itemBaseInfo");
            String itemTitle = jo.getString("itemTitle");
            int valueCoinCount = jo.getInt("valueCoinCount");
            jo = new JSONObject(AntSportsRpcCall.exchangeItem(itemId, valueCoinCount));
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            jo = jo.getJSONObject("data");
            if (jo.optBoolean("exgSuccess")) {
                Log.other("运动好礼🎐兑换[" + itemTitle + "]花费" + valueCoinCount + "运动币");
            }
        } catch (Throwable t) {
            Log.i(TAG, "trainMember err:");
            Log.printStackTrace(TAG, t);
        }
    }

    public enum PathCompleteStatus {
        NOT_JOIN, JOIN, NOT_COMPLETED, COMPLETED, INTERRUPT;
    }

    public enum TaskStatus {
        WAIT_COMPLETE, WAIT_RECEIVE, HAS_RECEIVED;
    }

    public interface WalkPathTheme {
        int DA_MEI_ZHONG_GUO = 0;
        int GONG_YI_YI_XIAO_BU = 1;
        int DENG_DING_ZHI_MA_SHAN = 2;
        int WEI_C_DA_TIAO_ZHAN = 3;
        int LONG_NIAN_QI_FU = 4;
        int SHOU_HU_TI_YU_MENG = 5;

        String[] nickNames = {"大美中国", "公益一小步", "登顶芝麻山", "维C大挑战", "龙年祈福", "守护体育梦"};
        String[] walkPathThemeIds = {"M202308082226", "M202401042147", "V202405271625", "202404221422", "WF202312050200", "V202409061650"};
    }

    public interface DonateCharityCoinType {

        int ONE = 0;
        int ALL = 1;

        String[] nickNames = {"捐赠一个项目", "捐赠所有项目"};

    }

    public interface BattleForFriendType {

        int ROB = 0;
        int DONT_ROB = 1;

        String[] nickNames = {"选中抢", "选中不抢"};

    }

    public interface TrainItemType {

        int BALLET = 0;
        int SANDBAG = 1;
        int BARBELL = 2;
        int YANGKO = 3;
        int SKATE = 4;
        int MUD = 5;

        String[] nickNames = {"跳芭蕾", "打沙包", "举杠铃", "扭秧歌", "玩滑板", "踩泥坑"};
        String[] itemTypes = {"ballet", "sandbag", "barbell", "yangko", "skate", "mud"};

    }
}