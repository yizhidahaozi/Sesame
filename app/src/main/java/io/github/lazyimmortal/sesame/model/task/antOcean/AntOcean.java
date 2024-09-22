package io.github.lazyimmortal.sesame.model.task.antOcean;

import org.json.JSONArray;
import org.json.JSONObject;
import io.github.lazyimmortal.sesame.data.ModelFields;
import io.github.lazyimmortal.sesame.data.ModelGroup;
import io.github.lazyimmortal.sesame.data.modelFieldExt.BooleanModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.ChoiceModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.SelectAndCountModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.SelectModelField;
import io.github.lazyimmortal.sesame.data.task.ModelTask;
import io.github.lazyimmortal.sesame.entity.AlipayBeach;
import io.github.lazyimmortal.sesame.entity.AlipayUser;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.model.task.antFarm.AntFarm.TaskStatus;
import io.github.lazyimmortal.sesame.model.task.antForest.AntForestRpcCall;
import io.github.lazyimmortal.sesame.util.Log;
import io.github.lazyimmortal.sesame.util.MessageUtil;
import io.github.lazyimmortal.sesame.util.Statistics;
import io.github.lazyimmortal.sesame.util.StringUtil;
import io.github.lazyimmortal.sesame.util.TimeUtil;
import io.github.lazyimmortal.sesame.util.UserIdMap;

import java.util.*;

/**
 * @author Constanline
 * @since 2023/08/01
 */
public class AntOcean extends ModelTask {
    private static final String TAG = AntOcean.class.getSimpleName();

    @Override
    public String getName() {
        return "海洋";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.FOREST;
    }

    private BooleanModelField dailyOceanTask;
    private BooleanModelField receiveOceanTaskAward;
    private BooleanModelField cleanOcean;
    private ChoiceModelField cleanOceanType;
    private SelectModelField cleanOceanList;
    private BooleanModelField exchangeUniversalPiece;
    private BooleanModelField useUniversalPiece;
    private BooleanModelField protectOcean;
    private SelectAndCountModelField protectOceanList;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(dailyOceanTask = new BooleanModelField("dailyOceanTask", "完成海洋任务", false));
        modelFields.addField(receiveOceanTaskAward = new BooleanModelField("receiveOceanTaskAward", "领取碎片奖励", false));
        modelFields.addField(cleanOcean = new BooleanModelField("cleanOcean", "清理 | 开启", false));
        modelFields.addField(cleanOceanType = new ChoiceModelField("cleanOceanType", "清理 | 动作", CleanOceanType.DONT_CLEAN, CleanOceanType.nickNames));
        modelFields.addField(cleanOceanList = new SelectModelField("cleanOceanList", "清理 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(exchangeUniversalPiece = new BooleanModelField("exchangeUniversalPiece", "神奇海洋 | 制作万能拼图", false));
        modelFields.addField(useUniversalPiece = new BooleanModelField("useUniversalPiece", "神奇海洋 | 使用万能拼图", false));
        modelFields.addField(protectOcean = new BooleanModelField("protectOcean", "保护 | 开启", false));
        modelFields.addField(protectOceanList = new SelectAndCountModelField("protectOceanList", "保护 | 海洋列表", new LinkedHashMap<>(), AlipayBeach::getList));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryOceanStatus());
            if (MessageUtil.checkResultCode(TAG, jo)) {
                if (jo.getBoolean("opened")) {
                    queryHomePage();
                } else {
                    getEnableField().setValue(false);
                    Log.record("请先开启神奇海洋，并完成引导教程");
                }
            }
            if (protectOcean.getValue()) {
                protectOcean();
            }
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void queryHomePage() {
        try {
            JSONObject joHomePage = new JSONObject(AntOceanRpcCall.queryHomePage());
            if (!MessageUtil.checkResultCode(TAG, joHomePage)) {
                return;
            }
            if (joHomePage.has("bubbleVOList")) {
                collectEnergy(joHomePage.getJSONArray("bubbleVOList"));
            }

            JSONObject userInfoVO = joHomePage.getJSONObject("userInfoVO");
            int rubbishNumber = userInfoVO.optInt("rubbishNumber", 0);
            String userId = userInfoVO.getString("userId");
            cleanOcean(userId, rubbishNumber);

            JSONObject ipVO = userInfoVO.optJSONObject("ipVO");
            if (ipVO != null) {
                int surprisePieceNum = ipVO.optInt("surprisePieceNum", 0);
                if (surprisePieceNum > 0) {
                    ipOpenSurprise();
                }
            }

            queryReplicaHome();

            queryMiscInfo();

            queryUserRanking();

            querySeaAreaDetailList();

            if (dailyOceanTask.getValue()) {
                doOceanDailyTask();
            }

            if (receiveOceanTaskAward.getValue()) {
                receiveTaskAward();
            }
            // 制作万能碎片
            if (exchangeUniversalPiece.getValue()) {
                exchangeUniversalPiece();
            }
            // 使用万能拼图
            if (useUniversalPiece.getValue()) {
                useUniversalPiece();
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryHomePage err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void collectEnergy(JSONArray bubbleVOList) {
        try {
            for (int i = 0; i < bubbleVOList.length(); i++) {
                JSONObject bubble = bubbleVOList.getJSONObject(i);
                if (!"ocean".equals(bubble.getString("channel"))) {
                    continue;
                }
                if ("AVAILABLE".equals(bubble.getString("collectStatus"))) {
                    long bubbleId = bubble.getLong("id");
                    String userId = bubble.getString("userId");
                    JSONObject jo = new JSONObject(AntForestRpcCall.collectEnergy(null, userId, bubbleId));
                    if (MessageUtil.checkResultCode(TAG, jo)) {
                        JSONArray retBubbles = jo.optJSONArray("bubbles");
                        if (retBubbles != null) {
                            for (int j = 0; j < retBubbles.length(); j++) {
                                JSONObject retBubble = retBubbles.optJSONObject(j);
                                if (retBubble != null) {
                                    int collectedEnergy = retBubble.getInt("collectedEnergy");
                                    Log.forest("神奇海洋🐳收取[" + UserIdMap.getMaskName(userId) + "]的海洋能量#"
                                            + collectedEnergy + "g");
                                    Statistics.addData(Statistics.DataType.COLLECTED, collectedEnergy);
                                }
                            }
                            Statistics.save();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryHomePage err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void cleanOcean(String userId, int rubbishNumber) {
        try {
            for (int i = 0; i < rubbishNumber; i++) {
                JSONObject jo = new JSONObject(AntOceanRpcCall.cleanOcean(userId));
                if (MessageUtil.checkResultCode(TAG, jo)) {
                    JSONArray cleanRewardVOS = jo.getJSONArray("cleanRewardVOS");
                    checkReward(cleanRewardVOS);
                    Log.forest("神奇海洋🐳[清理:" + UserIdMap.getMaskName(userId) + "海域]");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "cleanOcean err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void ipOpenSurprise() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.ipOpenSurprise());
            if (MessageUtil.checkResultCode(TAG, jo)) {
                JSONArray rewardVOS = jo.getJSONArray("surpriseRewardVOS");
                checkReward(rewardVOS);
            }
        } catch (Throwable t) {
            Log.i(TAG, "ipOpenSurprise err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void combineFish(String fishId) {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.combineFish(fishId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                JSONObject fishDetailVO = jo.getJSONObject("fishDetailVO");
                String name = fishDetailVO.getString("name");
                Log.forest("神奇海洋🐳[" + name + "]合成成功");
            }
        } catch (Throwable t) {
            Log.i(TAG, "combineFish err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void checkReward(JSONArray rewards) {
        try {
            for (int i = 0; i < rewards.length(); i++) {
                JSONObject reward = rewards.getJSONObject(i);
                String name = reward.getString("name");
                JSONArray attachReward = reward.getJSONArray("attachRewardBOList");
                if (attachReward.length() > 0) {
                    Log.forest("神奇海洋🐳[获得:" + name + "碎片]");
                    boolean canCombine = true;
                    for (int j = 0; j < attachReward.length(); j++) {
                        JSONObject detail = attachReward.getJSONObject(j);
                        if (detail.optInt("count", 0) == 0) {
                            canCombine = false;
                            break;
                        }
                    }
                    if (canCombine && reward.optBoolean("unlock", false)) {
                        String fishId = reward.getString("id");
                        combineFish(fishId);
                    }
                }

            }
        } catch (Throwable t) {
            Log.i(TAG, "checkReward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void collectReplicaAsset(int canCollectAssetNum) {
        try {
            for (int i = 0; i < canCollectAssetNum; i++) {
                JSONObject jo = new JSONObject(AntOceanRpcCall.collectReplicaAsset());
                if (MessageUtil.checkResultCode(TAG, jo)) {
                    Log.forest("神奇海洋🐳[学习海洋科普知识]#潘多拉能量+1");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "collectReplicaAsset err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void unLockReplicaPhase(String replicaCode, String replicaPhaseCode) {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.unLockReplicaPhase(replicaCode, replicaPhaseCode));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                String name = jo.getJSONObject("currentPhaseInfo").getJSONObject("extInfo").getString("name");
                Log.forest("神奇海洋🐳迎回[" + name + "]");
            }
        } catch (Throwable t) {
            Log.i(TAG, "unLockReplicaPhase err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryReplicaHome() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryReplicaHome());
            if (MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            if (jo.has("userReplicaAssetVO")) {
                JSONObject userReplicaAssetVO = jo.getJSONObject("userReplicaAssetVO");
                int canCollectAssetNum = userReplicaAssetVO.getInt("canCollectAssetNum");
                collectReplicaAsset(canCollectAssetNum);
            }

            if (jo.has("userCurrentPhaseVO")) {
                JSONObject userCurrentPhaseVO = jo.getJSONObject("userCurrentPhaseVO");
                String phaseCode = userCurrentPhaseVO.getString("phaseCode");
                String code = jo.getJSONObject("userReplicaInfoVO").getString("code");
                if ("COMPLETED".equals(userCurrentPhaseVO.getString("phaseStatus"))) {
                    unLockReplicaPhase(code, phaseCode);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryReplicaHome err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryOceanPropList() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryOceanPropList());
            if (MessageUtil.checkResultCode(TAG, jo)) {
                AntOceanRpcCall.repairSeaArea();
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryOceanPropList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void switchOceanChapter() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryOceanChapterList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            String currentChapterCode = jo.getString("currentChapterCode");
            JSONArray chapterVOs = jo.getJSONArray("userChapterDetailVOList");
            boolean isFinish = false;
            String dstChapterCode = "";
            String dstChapterName = "";
            for (int i = 0; i < chapterVOs.length(); i++) {
                JSONObject chapterVO = chapterVOs.getJSONObject(i);
                int repairedSeaAreaNum = chapterVO.getInt("repairedSeaAreaNum");
                int seaAreaNum = chapterVO.getInt("seaAreaNum");
                if (chapterVO.getString("chapterCode").equals(currentChapterCode)) {
                    isFinish = repairedSeaAreaNum >= seaAreaNum;
                } else {
                    if (repairedSeaAreaNum >= seaAreaNum || !chapterVO.getBoolean("chapterOpen")) {
                        continue;
                    }
                    dstChapterName = chapterVO.getString("chapterName");
                    dstChapterCode = chapterVO.getString("chapterCode");
                }
            }
            if (isFinish && !StringUtil.isEmpty(dstChapterCode)) {
                jo = new JSONObject(AntOceanRpcCall.switchOceanChapter(dstChapterCode));
                if (MessageUtil.checkResultCode(TAG, jo)) {
                    Log.forest("神奇海洋🐳切换到[" + dstChapterName + "]系列");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryUserRanking err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void querySeaAreaDetailList() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.querySeaAreaDetailList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            int seaAreaNum = jo.getInt("seaAreaNum");
            int fixSeaAreaNum = jo.getInt("fixSeaAreaNum");
            int currentSeaAreaIndex = jo.getInt("currentSeaAreaIndex");
            if (currentSeaAreaIndex < fixSeaAreaNum && seaAreaNum > fixSeaAreaNum) {
                queryOceanPropList();
            }
            JSONArray seaAreaVOs = jo.getJSONArray("seaAreaVOs");
            for (int i = 0; i < seaAreaVOs.length(); i++) {
                JSONObject seaAreaVO = seaAreaVOs.getJSONObject(i);
                JSONArray fishVOs = seaAreaVO.getJSONArray("fishVO");
                for (int j = 0; j < fishVOs.length(); j++) {
                    JSONObject fishVO = fishVOs.getJSONObject(j);
                    if (!fishVO.getBoolean("unlock") && "COMPLETED".equals(fishVO.getString("status"))) {
                        String fishId = fishVO.getString("id");
                        combineFish(fishId);
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "querySeaAreaDetailList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryMiscInfo() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryMiscInfo());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONObject miscHandlerVOMap = jo.getJSONObject("miscHandlerVOMap");
            JSONObject homeTipsRefresh = miscHandlerVOMap.getJSONObject("HOME_TIPS_REFRESH");
            if (homeTipsRefresh.optBoolean("fishCanBeCombined") || homeTipsRefresh.optBoolean("canBeRepaired")) {
                querySeaAreaDetailList();
            }
            switchOceanChapter();
        } catch (Throwable t) {
            Log.i(TAG, "queryMiscInfo err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void cleanFriendOcean(JSONObject fillFlag) {
        if (!fillFlag.optBoolean("canClean")) {
            return;
        }
        try {
            String userId = fillFlag.getString("userId");
            boolean isOceanClean = cleanOceanList.getValue().contains(userId);
            if (cleanOceanType.getValue() == CleanOceanType.DONT_CLEAN) {
                isOceanClean = !isOceanClean;
            }
            if (!isOceanClean) {
                return;
            }
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryFriendPage(userId));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            jo = new JSONObject(AntOceanRpcCall.cleanFriendOcean(userId));
            Log.forest("神奇海洋🐳[帮助:" + UserIdMap.getMaskName(userId) + "清理海域]");
            if (MessageUtil.checkResultCode(TAG, jo)) {
                JSONArray cleanRewardVOS = jo.getJSONArray("cleanRewardVOS");
                checkReward(cleanRewardVOS);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryMiscInfo err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void queryUserRanking() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryUserRanking());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONArray fillFlagVOList = jo.getJSONArray("fillFlagVOList");
            for (int i = 0; i < fillFlagVOList.length(); i++) {
                JSONObject fillFlag = fillFlagVOList.getJSONObject(i);
                if (cleanOcean.getValue()) {
                    cleanFriendOcean(fillFlag);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryMiscInfo err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static boolean isTargetTask(String taskType) {
        // 在这里添加其他任务类型，以便后续扩展
        return "DAOLIU_TAOJINBI".equals(taskType) // 去逛淘金币看淘金仔
                || "DAOLIU_NNYY".equals(taskType) // 逛余额宝新春活动
                || "ANTOCEAN_TASK#DAOLIU_GUANGHUABEIBANGHAI".equals(taskType) // 逛逛花呗活动会场
                || "BUSINESS_LIGHTS01".equals(taskType) // 逛一逛市集15s
                || "DAOLIU_ELEMEGUOYUAN".equals(taskType) // 去逛饿了么夺宝
                || "ZHUANHUA_NONGCHANGYX".equals(taskType) // 去玩趣味小游戏
                || "ZHUANHUA_HUIYUN_OZB".equals(taskType); // 一键传球欧洲杯

    }

    private static void doOceanDailyTask() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryTaskList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONArray jaTaskList = jo.getJSONArray("antOceanTaskVOList");
            for (int i = 0; i < jaTaskList.length(); i++) {
                JSONObject taskJson = jaTaskList.getJSONObject(i);
                if (!TaskStatus.TODO.name().equals(taskJson.getString("taskStatus")))
                    continue;
                JSONObject bizInfo = new JSONObject(taskJson.getString("bizInfo"));
                if (!taskJson.has("taskType"))
                    continue;
                String taskType = taskJson.getString("taskType");
                if (bizInfo.optBoolean("autoCompleteTask", false) || taskType.startsWith("DAOLIU_")) {
                    String sceneCode = taskJson.getString("sceneCode");
                    jo = new JSONObject(AntOceanRpcCall.finishTask(sceneCode, taskType));
                    TimeUtil.sleep(500);
                    if (jo.optBoolean("success")) {
                        String taskTitle = bizInfo.optString("taskTitle", taskType);
                        Log.forest("海洋任务🧾[完成:" + taskTitle + "]");
                        // 答题操作
                        answerQuestion();
                    } else {
                        Log.record(jo.getString("desc"));
                        Log.i(jo.toString());
                    }
                }
                // 多个任务类型的处理逻辑
                if (isTargetTask(taskType)) {
                    String sceneCode = taskJson.getString("sceneCode");
                    jo = new JSONObject(AntOceanRpcCall.finishTask(sceneCode, taskType));
                    TimeUtil.sleep(500);
                    if (jo.optBoolean("success")) {
                        String taskTitle = bizInfo.optString("taskTitle", taskType);
                        Log.forest("海洋任务🧾[完成:" + taskTitle + "]");
                        // 答题操作
                        answerQuestion();
                    } else {
                        Log.record(jo.getString("desc"));
                        Log.i(jo.toString());
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "doOceanDailyTask err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void receiveTaskAward() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryTaskList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONArray jaTaskList = jo.getJSONArray("antOceanTaskVOList");
            for (int i = 0; i < jaTaskList.length(); i++) {
                jo = jaTaskList.getJSONObject(i);
                if (!TaskStatus.FINISHED.name().equals(jo.getString("taskStatus")))
                    continue;
                JSONObject bizInfo = new JSONObject(jo.getString("bizInfo"));
                String taskType = jo.getString("taskType");
                String sceneCode = jo.getString("sceneCode");
                jo = new JSONObject(AntOceanRpcCall.receiveTaskAward(sceneCode, taskType));
                TimeUtil.sleep(500);
                if (jo.optBoolean("success")) {
                    String taskTitle = bizInfo.optString("taskTitle", taskType);
                    String awardCount = bizInfo.optString("awardCount", "0");
                    Log.forest("海洋奖励🎖️[领取:" + taskTitle + "]获得:" + awardCount + "块碎片");
                    // 潘多拉任务领取
                    doOceanPDLTask();
                } else {
                    Log.record(jo.getString("desc"));
                    Log.i(jo.toString());
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 海洋答题任务
    private static void answerQuestion() {
        try {
            String questionResponse = AntOceanRpcCall.getQuestion();
            JSONObject questionJson = new JSONObject(questionResponse);
            if (questionJson.getBoolean("answered")) {
                Log.record("问题已经被回答过，跳过答题流程");
                return;
            }
            if (questionJson.getInt("resultCode") == 200) {
                String questionId = questionJson.getString("questionId");
                JSONArray options = questionJson.getJSONArray("options");
                String answer = options.getString(0);
                String submitResponse = AntOceanRpcCall.submitAnswer(answer, questionId);
                TimeUtil.sleep(500);
                JSONObject submitJson = new JSONObject(submitResponse);
                if (submitJson.getInt("resultCode") == 200) {
                    Log.record("海洋答题成功");
                } else {
                    Log.record("答题失败：" + submitJson.getString("resultMsg"));
                }
            } else {
                Log.record("获取问题失败：" + questionJson.getString("resultMsg"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "answerQuestion err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 潘多拉海洋任务领取
    private static void doOceanPDLTask() {
        try {
            String homeResponse = AntOceanRpcCall.PDLqueryReplicaHome();
            JSONObject homeJson = new JSONObject(homeResponse);
            if (!MessageUtil.checkResultCode(TAG, homeJson)) {
                return;
            }
            String taskListResponse = AntOceanRpcCall.PDLqueryTaskList();
            TimeUtil.sleep(300);
            JSONObject taskListJson = new JSONObject(taskListResponse);
            JSONArray antOceanTaskVOList = taskListJson.getJSONArray("antOceanTaskVOList");
            for (int i = 0; i < antOceanTaskVOList.length(); i++) {
                JSONObject task = antOceanTaskVOList.getJSONObject(i);
                String taskStatus = task.getString("taskStatus");
                if ("FINISHED".equals(taskStatus)) {
                    String bizInfoString = task.getString("bizInfo");
                    JSONObject bizInfo = new JSONObject(bizInfoString);
                    String taskTitle = bizInfo.getString("taskTitle");
                    int awardCount = bizInfo.getInt("awardCount");
                    String taskType = task.getString("taskType");
                    String receiveTaskResponse = AntOceanRpcCall.PDLreceiveTaskAward(taskType);
                    TimeUtil.sleep(300);
                    JSONObject receiveTaskJson = new JSONObject(receiveTaskResponse);
                    int code = receiveTaskJson.getInt("code");
                    if (code == 100000000) {
                        Log.forest("海洋奖励🎖️[领取:" + taskTitle + "]获得潘多拉能量x" + awardCount);
                    } else {
                        if (receiveTaskJson.has("message")) {
                            Log.record("领取任务奖励失败: " + receiveTaskJson.getString("message"));
                        } else {
                            Log.record("领取任务奖励失败，未返回错误信息");
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "doOceanPDLTask err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void protectOcean() {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryCultivationList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONArray ja = jo.getJSONArray("cultivationItemVOList");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                if (!jo.has("templateSubType")) {
                    continue;
                }
                if (!"BEACH".equals(jo.getString("templateSubType"))
                        && !"COOPERATE_SEA_TREE".equals(jo.getString("templateSubType")) && !"SEA_ANIMAL".equals(jo.getString("templateSubType"))) {
                    continue;
                }
                if (!"AVAILABLE".equals(jo.getString("applyAction"))) {
                    continue;
                }
                String cultivationName = jo.getString("cultivationName");
                String templateCode = jo.getString("templateCode");
                JSONObject projectConfig = jo.getJSONObject("projectConfigVO");
                String projectCode = projectConfig.getString("code");
                Map<String, Integer> map = protectOceanList.getValue();
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    if (Objects.equals(entry.getKey(), templateCode)) {
                        Integer count = entry.getValue();
                        if (count != null && count > 0) {
                            oceanExchangeTree(templateCode, projectCode, cultivationName, count);
                        }
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "protectBeach err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void oceanExchangeTree(String cultivationCode, String projectCode, String itemName, int count) {
        try {
            JSONObject jo;
            int appliedTimes = queryCultivationDetail(cultivationCode, projectCode, count);
            if (appliedTimes < 0)
                return;
            for (int applyCount = 1; applyCount <= count; applyCount++) {
                jo = new JSONObject(AntOceanRpcCall.oceanExchangeTree(cultivationCode, projectCode));
                if (!MessageUtil.checkResultCode(TAG, jo)) {
                    Log.forest("保护海洋🏖️[" + itemName + "]#发生未知错误，停止申请");
                    break;
                }
                JSONArray awardInfos = jo.getJSONArray("rewardItemVOs");
                StringBuilder award = new StringBuilder();
                for (int i = 0; i < awardInfos.length(); i++) {
                    jo = awardInfos.getJSONObject(i);
                    award.append(jo.getString("name")).append("*").append(jo.getInt("num"));
                }
                Log.forest("保护海洋🏖️[" + itemName + "]#第" + appliedTimes + "次-获得奖励" + award);
                TimeUtil.sleep(300);
                appliedTimes = queryCultivationDetail(cultivationCode, projectCode, count);
                if (appliedTimes < 0) {
                    break;
                } else {
                    TimeUtil.sleep(300);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "oceanExchangeTree err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static int queryCultivationDetail(String cultivationCode, String projectCode, int count) {
        int appliedTimes = -1;
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryCultivationDetail(cultivationCode, projectCode));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                JSONObject userInfo = jo.getJSONObject("userInfoVO");
                int currentEnergy = userInfo.getInt("currentEnergy");
                jo = jo.getJSONObject("cultivationDetailVO");
                String applyAction = jo.getString("applyAction");
                int certNum = jo.getInt("certNum");
                if ("AVAILABLE".equals(applyAction)) {
                    if (currentEnergy >= jo.getInt("energy")) {
                        if (certNum < count) {
                            appliedTimes = certNum + 1;
                        }
                    } else {
                        Log.forest("保护海洋🏖️[" + jo.getString("cultivationName") + "]#能量不足停止申请");
                    }
                } else {
                    Log.forest("保护海洋🏖️[" + jo.getString("cultivationName") + "]#似乎没有了");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryCultivationDetail err:");
            Log.printStackTrace(TAG, t);
        }
        return appliedTimes;
    }

    // 制作万能拼图
    private static void exchangeUniversalPiece() {
        try {
            // 获取道具兑换列表的JSON数据
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryOceanPropList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            // 获取重复拼图数量
            int duplicatePieceNum = jo.getInt("duplicatePieceNum");
            while (duplicatePieceNum >= 10) {
                // 如果重复拼图数量大于等于10，则执行道具兑换操作
                int exchangeNum = Math.min(duplicatePieceNum  / 10, 50);
                if (!exchangeUniversalPiece(exchangeNum)) {
                    break;
                }
                TimeUtil.sleep(1000);
                duplicatePieceNum -= exchangeNum * 10;
            }
        } catch (Throwable t) {
            Log.i(TAG, "exchangeUniversalPiece error:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static Boolean exchangeUniversalPiece(int number) {
        try {
            JSONObject jo = new JSONObject(AntOceanRpcCall.exchangeUniversalPiece(number));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                String duplicatePieceNum = jo.getString("duplicatePieceNum");
                String exchangeNum = jo.getString("exchangeNum");
                Log.forest("神奇海洋🏖️制作[万能拼图]" + exchangeNum + "张,剩余" + duplicatePieceNum + "张重复拼图");
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "exchangeUniversalPiece error:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    // 使用万能拼图
    private static void useUniversalPiece() {
        try {
            // 获取道具使用类型列表的JSON数据
            JSONObject jo = new JSONObject(AntOceanRpcCall.queryOceanPropList("UNIVERSAL_PIECE"));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            // 获取道具类型列表中的holdsNum值
            JSONArray oceanPropVOByTypeList = jo.getJSONArray("oceanPropVOByTypeList");
            // 遍历每个道具类型信息
            for (int i = 0; i < oceanPropVOByTypeList.length(); i++) {
                JSONObject oceanPropVO = oceanPropVOByTypeList.getJSONObject(i);
                int holdsNum = oceanPropVO.getInt("holdsNum");
                int pageNum = 0;
                boolean hasMore = true;
                while (holdsNum > 0 && hasMore) {
                    // 查询鱼列表的JSON数据
                    pageNum++;
                    jo = new JSONObject(AntOceanRpcCall.queryFishList(pageNum));
                    // 检查是否成功获取到鱼列表并且 hasMore 为 true
                    if (!MessageUtil.checkResultCode(TAG, jo)) {
                        // 如果没有成功获取到鱼列表或者 hasMore 为 false，则停止后续操作
                        return;
                    }
                    hasMore = jo.optBoolean("hasMore");
                    // 获取鱼列表中的fishVOS数组
                    if (!jo.has("fishVOS")) {
                        return;
                    }
                    JSONArray fishVOS = jo.getJSONArray("fishVOS");
                    holdsNum -= useUniversalPiece(fishVOS, holdsNum);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "useUniversalPiece error:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static int useUniversalPiece(JSONArray fishVOS, int holdsNum) {
        int count = 0;
        try {
            for (int i = 0; i < fishVOS.length() && count < holdsNum; i++) {
                JSONObject fishVO = fishVOS.getJSONObject(i);
                if (!fishVO.has("pieces")) {
                    continue;
                }
                count += useUniversalPiece(fishVO, holdsNum - count);
            }
        } catch (Throwable t) {
            Log.i(TAG, "useUniversalPiece error:");
            Log.printStackTrace(TAG, t);
        }
        return count;
    }

    private static int useUniversalPiece(JSONObject fishVO, int holdsNum) {
        JSONArray assetsDetails = new JSONArray();
        try {
            int order = fishVO.getInt("order");
            String name = fishVO.getString("name");
            JSONArray pieces = fishVO.getJSONArray("pieces");
            for (int i = 0; i < pieces.length(); i++) {
                JSONObject piece = pieces.getJSONObject(i);
                if (piece.getInt("num") > 1) {
                    continue;
                }
                JSONObject assetsDetail = new JSONObject();
                assetsDetail.put("assets", order);
                assetsDetail.put("assetsNum", 1);
                assetsDetail.put("attachAssets", Integer.parseInt(piece.getString("id")));
                assetsDetail.put("propCode", "UNIVERSAL_PIECE");
                assetsDetails.put(assetsDetail);
                if (assetsDetails.length() == holdsNum) {
                    break;
                }
            }
            if (useUniversalPiece(assetsDetails, name, holdsNum - assetsDetails.length())) {
                TimeUtil.sleep(1000);
                return assetsDetails.length();
            }
        } catch (Throwable t) {
            Log.i(TAG, "useUniversalPiece error:");
            Log.printStackTrace(TAG, t);
        }
        return 0;
    }

    private static Boolean useUniversalPiece(JSONArray assetsDetails, String name, int holdsNum) {
        try {
            if (assetsDetails.length() == 0) {
                return false;
            }
            JSONObject jo = new JSONObject(AntOceanRpcCall.useUniversalPiece(assetsDetails));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                int userCount = assetsDetails.length();
                Log.forest("神奇海洋🏖️使用[万能拼图]" + userCount + "张,获得[" + name + "],剩余" + holdsNum + "张");
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "useUniversalPiece error:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }


    public interface CleanOceanType {

        int CLEAN = 0;
        int DONT_CLEAN = 1;

        String[] nickNames = {"选中清理", "选中不清理"};

    }

}
