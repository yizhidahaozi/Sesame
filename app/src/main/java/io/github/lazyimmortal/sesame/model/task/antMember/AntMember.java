package io.github.lazyimmortal.sesame.model.task.antMember;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.github.lazyimmortal.sesame.data.ModelFields;
import io.github.lazyimmortal.sesame.data.ModelGroup;
import io.github.lazyimmortal.sesame.data.modelFieldExt.BooleanModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.SelectModelField;
import io.github.lazyimmortal.sesame.data.task.ModelTask;
import io.github.lazyimmortal.sesame.entity.MemberBenefit;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.util.*;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class AntMember extends ModelTask {
    private static final String TAG = AntMember.class.getSimpleName();

    @Override
    public String getName() {
        return "会员";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.MEMBER;
    }

    private BooleanModelField memberSign;
    private BooleanModelField collectSesame;
    private BooleanModelField memberPointExchangeBenefit;
    private SelectModelField memberPointExchangeBenefitList;
    private BooleanModelField LifeRecords;
    private BooleanModelField saveinsuniversal;
    private BooleanModelField goalipaysportsroute;
    private BooleanModelField collectmemberpoint;
    private BooleanModelField xiaofeijinvisit;
    private BooleanModelField KuaiDiFuLiJia;
    private BooleanModelField signinCalendar;
    private BooleanModelField enableGoldTicket;
    private BooleanModelField enableGameCenter;
    private BooleanModelField zcjSignIn;
    private BooleanModelField merchantKmdk;
    private BooleanModelField beanSignIn;
    private BooleanModelField beanExchangeBubbleBoost;
    private BooleanModelField beanExchangeGoldenTicket;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(memberSign = new BooleanModelField("memberSign", "会员 | 签到", false));
        modelFields.addField(memberPointExchangeBenefit = new BooleanModelField("memberPointExchangeBenefit", "会员积分 | 兑换开启", false));
        modelFields.addField(memberPointExchangeBenefitList = new SelectModelField("memberPointExchangeBenefitList", "会员积分 | 兑换列表", new LinkedHashSet<>(), MemberBenefit::getList));
        modelFields.addField(collectSesame = new BooleanModelField("collectSesame", "芝麻粒 | 领取", false));
        modelFields.addField(LifeRecords = new BooleanModelField("LifeRecords", "生活记录 | 开启", false));
        modelFields.addField(saveinsuniversal = new BooleanModelField("saveinsuniversal", "生活记录 | 坚持攒保障金", false));
        modelFields.addField(goalipaysportsroute = new BooleanModelField("goalipaysportsroute", "生活记录 | 坚持锻炼，走运动路线", false));
        modelFields.addField(collectmemberpoint = new BooleanModelField("collectmemberpoint", "生活记录 | 坚持领会员积分", false));
        modelFields.addField(xiaofeijinvisit = new BooleanModelField("xiaofeijinvisit", "生活记录 | 坚持攒消费金金币", false));
        modelFields.addField(KuaiDiFuLiJia = new BooleanModelField("KuaiDiFuLiJia", "我的快递 | 任务", false));
        modelFields.addField(signinCalendar = new BooleanModelField("signinCalendar", "消费金 | 签到", false));
        modelFields.addField(enableGoldTicket = new BooleanModelField("enableGoldTicket", "黄金票 | 签到", false));
        modelFields.addField(enableGameCenter = new BooleanModelField("enableGameCenter", "游戏中心 | 签到", false));
        modelFields.addField(zcjSignIn = new BooleanModelField("zcjSignIn", "招财金 | 签到", false));
        modelFields.addField(merchantKmdk = new BooleanModelField("merchantKmdk", "商户开门 | 打卡", false));
        modelFields.addField(beanSignIn = new BooleanModelField("beanSignIn", "安心豆 | 签到", false));
        modelFields.addField(beanExchangeBubbleBoost = new BooleanModelField("beanExchangeBubbleBoost", "安心豆 | 兑换时光加速器", false));
        modelFields.addField(beanExchangeGoldenTicket = new BooleanModelField("beanExchangeGoldenTicket", "安心豆 | 兑换黄金票", false));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            if (memberSign.getValue()) {
                memberSign();
            }
            if (collectSesame.getValue()) {
                collectSesame();
            }
            // 生活记录
            if (LifeRecords.getValue()) {
                LifeRecords();
            }
            // 我的快递任务
            if (KuaiDiFuLiJia.getValue()) {
                RecommendTask();
                OrdinaryTask();
            }
            if (enableGoldTicket.getValue()) {
                goldTicket();
            }
            if (enableGameCenter.getValue()) {
                enableGameCenter();
            }
            if (beanSignIn.getValue()) {
                beanSignIn();
            }
            if (beanExchangeBubbleBoost.getValue()) {
                beanExchangeBubbleBoost();
            }
            // 安心豆兑换黄金票
            if (beanExchangeGoldenTicket.getValue()) {
                beanExchangeGoldenTicket();
            }
            if (memberPointExchangeBenefit.getValue()) {
                memberPointExchangeBenefit();
            }
            // 消费金签到
            if (signinCalendar.getValue()) {
                signinCalendar();
            }

            if (zcjSignIn.getValue() || merchantKmdk.getValue()) {
                JSONObject jo = new JSONObject(AntMemberRpcCall.transcodeCheck());
                if (!jo.optBoolean("success")) {
                    return;
                }
                JSONObject data = jo.getJSONObject("data");
                if (!data.optBoolean("isOpened")) {
                    Log.record("商家服务👪未开通");
                    return;
                }
                if (zcjSignIn.getValue()) {
                    zcjSignIn();
                }
                if (merchantKmdk.getValue()) {
                    if (TimeUtil.isNowAfterTimeStr("0600") && TimeUtil.isNowBeforeTimeStr("1200")) {
                        kmdkSignIn();
                    }
                    kmdkSignUp();
                    taskListQuery();
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    private void memberSign() {
        try {
            if (Status.canMemberSignInToday(UserIdMap.getCurrentUid())) {
                String s = AntMemberRpcCall.queryMemberSigninCalendar();
                TimeUtil.sleep(500);
                JSONObject jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    Log.other("每日签到📅[" + jo.getString("signinPoint") + "积分]#已签到" + jo.getString("signinSumDay")
                            + "天");
                    Status.memberSignInToday(UserIdMap.getCurrentUid());
                } else {
                    Log.record(jo.getString("resultDesc"));
                    Log.i(s);
                }
            }

            queryPointCert(1, 8);

            signPageTaskList();

            queryAllStatusTaskList();
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    private static void queryPointCert(int page, int pageSize) {
        try {
            String s = AntMemberRpcCall.queryPointCert(page, pageSize);
            TimeUtil.sleep(500);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                boolean hasNextPage = jo.getBoolean("hasNextPage");
                JSONArray jaCertList = jo.getJSONArray("certList");
                for (int i = 0; i < jaCertList.length(); i++) {
                    jo = jaCertList.getJSONObject(i);
                    String bizTitle = jo.getString("bizTitle");
                    String id = jo.getString("id");
                    int pointAmount = jo.getInt("pointAmount");
                    s = AntMemberRpcCall.receivePointByUser(id);
                    jo = new JSONObject(s);
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.other("领取奖励🎖️[" + bizTitle + "]#" + pointAmount + "积分");
                    } else {
                        Log.record(jo.getString("resultDesc"));
                        Log.i(s);
                    }
                }
                if (hasNextPage) {
                    queryPointCert(page + 1, pageSize);
                }
            } else {
                Log.record(jo.getString("resultDesc"));
                Log.i(s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryPointCert err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void kmdkSignIn() {
        try {
            String s = AntMemberRpcCall.queryActivity();
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                if ("SIGN_IN_ENABLE".equals(jo.getString("signInStatus"))) {
                    String activityNo = jo.getString("activityNo");
                    JSONObject joSignIn = new JSONObject(AntMemberRpcCall.signIn(activityNo));
                    if (joSignIn.optBoolean("success")) {
                        Log.other("商家服务🕴🏻[开门打卡签到成功]");
                    } else {
                        Log.record(joSignIn.getString("errorMsg"));
                        Log.i(joSignIn.toString());
                    }
                }
            } else {
                Log.record("queryActivity" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "kmdkSignIn err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void kmdkSignUp() {
        try {
            for (int i = 0; i < 5; i++) {
                JSONObject jo = new JSONObject(AntMemberRpcCall.queryActivity());
                if (jo.optBoolean("success")) {
                    String activityNo = jo.getString("activityNo");
                    if (!Log.getFormatDate().replace("-", "").equals(activityNo.split("_")[2])) {
                        break;
                    }
                    if ("SIGN_UP".equals(jo.getString("signUpStatus"))) {
                        Log.record("开门打卡今日已报名！");
                        break;
                    }
                    if ("UN_SIGN_UP".equals(jo.getString("signUpStatus"))) {
                        String activityPeriodName = jo.getString("activityPeriodName");
                        JSONObject joSignUp = new JSONObject(AntMemberRpcCall.signUp(activityNo));
                        if (joSignUp.optBoolean("success")) {
                            Log.other("商家服务🕴🏻[" + activityPeriodName + "开门打卡报名]");
                            return;
                        } else {
                            Log.record(joSignUp.getString("errorMsg"));
                            Log.i(joSignUp.toString());
                        }
                    }
                } else {
                    Log.record("queryActivity");
                    Log.i(jo.toString());
                }
                Thread.sleep(500);
            }
        } catch (Throwable t) {
            Log.i(TAG, "kmdkSignUp err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void zcjSignIn() {
        try {
            String s = AntMemberRpcCall.zcjSignInQuery();
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                JSONObject button = jo.getJSONObject("data").getJSONObject("button");
                if ("UNRECEIVED".equals(button.getString("status"))) {
                    jo = new JSONObject(AntMemberRpcCall.zcjSignInExecute());
                    if (jo.optBoolean("success")) {
                        JSONObject data = jo.getJSONObject("data");
                        int todayReward = data.getInt("todayReward");
                        String widgetName = data.getString("widgetName");
                        Log.other("商家服务🕴🏻[" + widgetName + "]#" + todayReward + "积分");
                    }
                }
            } else {
                Log.record("zcjSignInQuery" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "zcjSignIn err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 商家服务任务 */
    private static void taskListQuery() {
        String s = AntMemberRpcCall.taskListQuery();
        try {
            boolean doubleCheck = false;
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                JSONArray taskList = jo.getJSONObject("data").getJSONArray("taskList");
                for (int i = 0; i < taskList.length(); i++) {
                    JSONObject task = taskList.getJSONObject(i);
                    if (!task.has("status")) {
                        continue;
                    }
                    String title = task.getString("title");
                    String reward = task.getString("reward");
                    String taskStatus = task.getString("status");
                    if ("NEED_RECEIVE".equals(taskStatus)) {
                        if (task.has("pointBallId")) {
                            jo = new JSONObject(AntMemberRpcCall.ballReceive(task.getString("pointBallId")));
                            if (jo.optBoolean("success")) {
                                Log.other("商家服务🕴🏻[" + title + "]#" + reward);
                            }
                        }
                    } else if ("PROCESSING".equals(taskStatus) || "UNRECEIVED".equals(taskStatus)) {
                        if (task.has("extendLog")) {
                            JSONObject bizExtMap = task.getJSONObject("extendLog").getJSONObject("bizExtMap");
                            jo = new JSONObject(AntMemberRpcCall.taskFinish(bizExtMap.getString("bizId")));
                            if (jo.optBoolean("success")) {
                                Log.other("商家服务🕴🏻[" + title + "]#" + reward);
                            }
                            doubleCheck = true;
                        } else {
                            String taskCode = task.getString("taskCode");
                            switch (taskCode) {
                                case "XCZBJLLRWCS_TASK":
                                    // 逛一逛精彩内容
                                    taskReceive(taskCode, "XCZBJLL_VIEWED", title);
                                    break;
                                case "BBNCLLRWX_TASK":
                                    // 逛一逛芭芭农场
                                    taskReceive(taskCode, "GYG_BBNC_VIEWED", title);
                                    break;
                                case "LLSQMDLB_TASK":
                                    // 浏览收钱码大礼包
                                    taskReceive(taskCode, "LL_SQMDLB_VIEWED", title);
                                    break;
                                case "SYH_CPC_FIXED_2":
                                    // 逛一逛商品橱窗
                                    taskReceive(taskCode, "MRCH_CPC_FIXED_VIEWED", title);
                                    break;
                                case "SYH_CPC_ALMM_1":
                                    taskReceive(taskCode, "MRCH_CPC_ALMM_VIEWED", title);
                                    break;
                                case "TJBLLRW_TASK":
                                    // 逛逛淘金币，购物可抵钱
                                    taskReceive(taskCode, "TJBLLRW_TASK_VIEWED", title);
                                    break;
                                case "HHKLLRW_TASK":
                                    // 49999元花呗红包集卡抽
                                    taskReceive(taskCode, "HHKLLX_VIEWED", title);
                                    break;
                                case "ZCJ_VIEW_TRADE":
                                    // 浏览攻略，赚商家积分
                                    taskReceive(taskCode, "ZCJ_VIEW_TRADE_VIEWED", title);
                                    break;
                            }
                        }
                    }
                }
                if (doubleCheck) {
                    taskListQuery();
                }
            } else {
                Log.i("taskListQuery err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "taskListQuery err:");
            Log.printStackTrace(TAG, t);
        } finally {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    private static void taskReceive(String taskCode, String actionCode, String title) {
        try {
            String s = AntMemberRpcCall.taskReceive(taskCode);
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                jo = new JSONObject(AntMemberRpcCall.actioncode(actionCode));
                if (jo.optBoolean("success")) {
                    jo = new JSONObject(AntMemberRpcCall.produce(actionCode));
                    if (jo.optBoolean("success")) {
                        Log.other("完成任务🕴🏻[" + title + "]");
                    }
                }
            } else {
                Log.record("taskReceive" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "taskReceive err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /**
     * 做任务赚积分
     */
    private void signPageTaskList() {
        try {
            do {
                String s = AntMemberRpcCall.signPageTaskList();
                TimeUtil.sleep(500);
                JSONObject jo = new JSONObject(s);
                boolean doubleCheck = false;
                if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                    Log.i(TAG, "queryAllStatusTaskList err:" + jo.getString("resultDesc"));
                    return;
                }
                if (!jo.has("categoryTaskList")) {
                    return;
                }
                JSONArray categoryTaskList = jo.getJSONArray("categoryTaskList");
                for (int i = 0; i < categoryTaskList.length(); i++) {
                    jo = categoryTaskList.getJSONObject(i);
                    if (!"BROWSE".equals(jo.getString("type"))) {
                        continue;
                    }
                    JSONArray taskList = jo.getJSONArray("taskList");
                    doubleCheck = doTask(taskList);
                }
                if (doubleCheck) {
                    continue;
                }
                break;
            } while (true);
        } catch (Throwable t) {
            Log.i(TAG, "signPageTaskList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /**
     * 查询所有状态任务列表
     */
    private void queryAllStatusTaskList() {
        try {
            String str = AntMemberRpcCall.queryAllStatusTaskList();
            TimeUtil.sleep(500);
            JSONObject jsonObject = new JSONObject(str);
            if (!"SUCCESS".equals(jsonObject.getString("resultCode"))) {
                Log.i(TAG, "queryAllStatusTaskList err:" + jsonObject.getString("resultDesc"));
                return;
            }
            if (!jsonObject.has("availableTaskList")) {
                return;
            }
            if (doTask(jsonObject.getJSONArray("availableTaskList"))) {
                queryAllStatusTaskList();
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryAllStatusTaskList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 生活记录
    private void LifeRecords() {
        try {
            String str = AntMemberRpcCall.promiseQueryHome();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.optBoolean("success")) {
                return;
            }
            JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jsonObject, "data.processingPromises");
            if (jsonArray == null) {
                return;
            }
            boolean isSaveinsuniVersal = true;
            boolean isSportsRoute = true;
            boolean isCollectMemberPoint = true;
            boolean isXiaofeijinVisit = true;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                String recordId = jsonObject.getString("recordId");
                boolean isRepeat = jsonObject.getInt("totalNums") - jsonObject.getInt("finishNums") == 1;
                String promiseName = jsonObject.getString("promiseName");
                if ("坚持攒保障金".equals(promiseName) && saveinsuniversal.getValue()) {
                    isSaveinsuniVersal = false;
                    promiseQueryDetail(recordId);
                    securityFund(isRepeat, recordId);
                    promiseQueryDetail(recordId);
                    promiseQueryDetail(recordId);
                }
                if ("坚持锻炼，走运动路线".equals(promiseName)) {
                    isSportsRoute = false;
                }
                if ("坚持领会员积分".equals(promiseName)) {
                    isCollectMemberPoint = false;
                }
                if ("坚持攒消费金金币".equals(promiseName)) {
                    isXiaofeijinVisit = false;
                }
            }// 坚持攒保障金
            if (isSaveinsuniVersal && saveinsuniversal.getValue()) {
                joinsaveinsuniversal();
            }
            // 坚持锻炼，走运动路线
            if (isSportsRoute && goalipaysportsroute.getValue()) {
                joingoalipaysportsroute();
            }
            // 坚持领会员积分
            if (isCollectMemberPoint && collectmemberpoint.getValue()) {
                joincollectmemberpoint();
            }
            // 坚持攒消费金金币
            if (isXiaofeijinVisit && xiaofeijinvisit.getValue()) {
                joinxiaofeijinvisit();
            }
        } catch (Throwable t) {
            Log.i(TAG, "LifeRecords err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 生活记录-加入坚持攒保障金
    private void joinsaveinsuniversal() {
        try {
            String str = AntMemberRpcCall.promiseJoin("{\"autoRenewStatus\":true,\"dataSourceRule\":{\"selectValue\":\"ins_universal\"}," +
                    "\"joinFromOuter\":false,\"joinGuarantyRule\":{\"joinGuarantyRuleType\":\"POINT\",\"selectValue\":\"1\"}," +
                    "\"joinRule\":{\"joinRuleType\":\"DYNAMIC_DAY\",\"selectValue\":\"7\"},\"periodTargetRule\":{\"periodTargetRuleType\":\"CAL_COUNT\",\"selectValue\":\"3\"}," +
                    "\"templateId\":\"save_ins_universal\"}");
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.optBoolean("success")) {
                return;
            }
            Log.other("生活记录📝[加入:" + JsonUtil.getValueByPath(jsonObject, "data.promiseName") + "]");
        } catch (Throwable t) {
            Log.i(TAG, "joinsaveinsuniversal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 生活记录-坚持攒保障金
    private void securityFund(boolean isRepeat, String recordId) {
        try {
            String str = AntMemberRpcCall.queryMultiSceneWaitToGainList();
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.optBoolean("success")) {
                return;
            }
            jsonObject = jsonObject.getJSONObject("data");
            // 使用 keys() 方法获取所有键
            Iterator<String> keys = jsonObject.keys();
            // 遍历所有键
            while (keys.hasNext()) {
                String key = keys.next();
                // 获取键对应的值
                Object propertyValue = jsonObject.get(key);
                if (propertyValue instanceof JSONArray) {
                    // 如eventToWaitDTOList、helpChildSumInsuredDTOList
                    JSONArray jsonArray = ((JSONArray) propertyValue);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        isRepeat = gainMyAndFamilySumInsured(jsonArray.getJSONObject(i), isRepeat, recordId);
                    }
                } else if (propertyValue instanceof JSONObject) {
                    // 如signInDTO、priorityChannelDTO
                    JSONObject jo = ((JSONObject) propertyValue);
                    if (jo.length() == 0) {
                        continue;
                    }
                    isRepeat = gainMyAndFamilySumInsured(jo, isRepeat, recordId);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "securityFund err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 领取保障金
    private boolean gainMyAndFamilySumInsured(JSONObject jsonObject, boolean isRepeat, String recordId) throws JSONException {
        JSONObject jo = new JSONObject(AntMemberRpcCall.gainMyAndFamilySumInsured(jsonObject));
        if (!jo.optBoolean("success")) {
            return true;
        }
        Log.other("攒保障金💰[领取:" + JsonUtil.getValueByPath(jo, "data.gainSumInsuredDTO.gainSumInsuredYuan") + "元保障金]");
        if (isRepeat) {
            promiseQueryDetail(recordId);
            promiseQueryDetail(recordId);
            return false;
        }
        return true;
    }

    // 查询持续做明细任务
    private JSONObject promiseQueryDetail(String recordId) throws JSONException {
        JSONObject jo = new JSONObject(AntMemberRpcCall.promiseQueryDetail(recordId));
        if (!jo.optBoolean("success")) {
            return null;
        }
        return jo;
    }

    // 生活记录-加入坚持锻炼，走运动路线
    private void joingoalipaysportsroute() {
        try {
            String str = AntMemberRpcCall.promiseJoin("{\"autoRenewStatus\":false,\"dataSourceRule\":{\"selectValue\":\"alipay_sports\"}," +
                    "\"joinFromOuter\":false,\"joinGuarantyRule\":{\"joinGuarantyRuleType\":\"POINT\",\"selectValue\":\"1\"}," +
                    "\"joinRule\":{\"joinRuleType\":\"DYNAMIC_DAY\",\"selectValue\":\"7\"},\"periodTargetRule\":{\"periodTargetRuleType\":\"CAL_COUNT\",\"selectValue\":\"3\"}," +
                    "\"templateId\":\"go_alipay_sports_route\"}");
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.optBoolean("success")) {
                return;
            }
            Log.other("生活记录📝[加入:" + JsonUtil.getValueByPath(jsonObject, "data.promiseName") + "]");
        } catch (Throwable t) {
            Log.i(TAG, "joingoalipaysportsroute err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 生活记录-加入坚持领会员积分
    private void joincollectmemberpoint() {
        try {
            String str = AntMemberRpcCall.promiseJoin("{\"autoRenewStatus\":false,\"dataSourceRule\":{\"selectValue\":\"alipay_member\"}," +
                    "\"joinFromOuter\":false,\"joinGuarantyRule\":{\"joinGuarantyRuleType\":\"POINT\",\"selectValue\":\"1\"}," +
                    "\"joinRule\":{\"joinRuleType\":\"DYNAMIC_DAY\",\"selectValue\":\"7\"},\"periodTargetRule\":{\"periodTargetRuleType\":\"CAL_COUNT\",\"selectValue\":\"3\"}," +
                    "\"templateId\":\"collect_member_point\"}");
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.optBoolean("success")) {
                return;
            }
            Log.other("生活记录📝[加入:" + JsonUtil.getValueByPath(jsonObject, "data.promiseName") + "]");
        } catch (Throwable t) {
            Log.i(TAG, "joincollectmemberpoint err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 生活记录-加入坚持攒消费金金币
    private void joinxiaofeijinvisit() {
        try {
            String str = AntMemberRpcCall.promiseJoin("{\"autoRenewStatus\":false,\"dataSourceRule\":{\"selectValue\":\"xiaofeijin\"}," +
                    "\"joinFromOuter\":false,\"joinGuarantyRule\":{\"joinGuarantyRuleType\":\"POINT\",\"selectValue\":\"1\"}," +
                    "\"joinRule\":{\"joinRuleType\":\"DYNAMIC_DAY\",\"selectValue\":\"7\"},\"periodTargetRule\":{\"periodTargetRuleType\":\"CAL_COUNT\",\"selectValue\":\"4\"}," +
                    "\"templateId\":\"xiaofeijin_visit\"}");
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.optBoolean("success")) {
                return;
            }
            Log.other("生活记录📝[加入:" + JsonUtil.getValueByPath(jsonObject, "data.promiseName") + "]");
        } catch (Throwable t) {
            Log.i(TAG, "joinxiaofeijinvisit err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 蚂蚁积分-做浏览任务
    private boolean doTask(JSONArray taskList) {
        boolean doubleCheck = false;
        try {
            for (int j = 0; j < taskList.length(); j++) {
                JSONObject task = taskList.getJSONObject(j);
                int count = 1;
                boolean hybrid = task.getBoolean("hybrid");
                int periodCurrentCount = 0;
                int periodTargetCount = 0;
                if (hybrid) {
                    periodCurrentCount = Integer.parseInt(task.getJSONObject("extInfo").getString("PERIOD_CURRENT_COUNT"));
                    periodTargetCount = Integer.parseInt(task.getJSONObject("extInfo").getString("PERIOD_TARGET_COUNT"));
                    count = periodTargetCount > periodCurrentCount ? periodTargetCount - periodCurrentCount : 0;
                }
                if (count <= 0) {
                    continue;
                }
                JSONObject taskConfigInfo = task.getJSONObject("taskConfigInfo");
                String name = taskConfigInfo.getString("name");
                Long id = taskConfigInfo.getLong("id");
                String awardParamPoint = taskConfigInfo.getJSONObject("awardParam")
                        .getString("awardParamPoint");
                String targetBusiness = taskConfigInfo.getJSONArray("targetBusiness").getString(0);
                for (int k = 0; k < count; k++) {
                    JSONObject jo = new JSONObject(AntMemberRpcCall.applyTask(name, id));
                    TimeUtil.sleep(300);
                    if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.i(TAG, "signPageTaskList.applyTask err:" + jo.optString("resultDesc"));
                        continue;
                    }
                    String[] targetBusinessArray = targetBusiness.split("#");
                    String bizParam;
                    String bizSubType;
                    if (targetBusinessArray.length > 2) {
                        bizParam = targetBusinessArray[2];
                        bizSubType = targetBusinessArray[1];
                    } else {
                        bizParam = targetBusinessArray[1];
                        bizSubType = targetBusinessArray[0];
                    }
                    jo = new JSONObject(AntMemberRpcCall.executeTask(bizParam, bizSubType));
                    TimeUtil.sleep(300);
                    if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.i(TAG, "signPageTaskList.executeTask err:" + jo.optString("resultDesc"));
                        continue;
                    }
                    String ex = "";
                    if (hybrid) {
                        ex = "(" + (periodCurrentCount + k + 1) + "/" + periodTargetCount + ")";
                    }
                    Log.other("会员任务🎖️[" + name + ex + "]#" + awardParamPoint + "积分");
                    doubleCheck = true;
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "signPageTaskList err:");
            Log.printStackTrace(TAG, t);
        }
        return doubleCheck;
    }

    private void goldTicket() {
        try {
            // 签到
            goldBillCollect("\"campId\":\"CP1417744\",\"directModeDisableCollect\":true,\"from\":\"antfarm\",");
            // 收取其他
            goldBillCollect("");
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    /**
     * 收取黄金票
     */
    private void goldBillCollect(String signInfo) {
        try {
            String str = AntMemberRpcCall.goldBillCollect(signInfo);
            JSONObject jsonObject = new JSONObject(str);
            if (!jsonObject.optBoolean("success")) {
                Log.i(TAG + ".goldBillCollect.goldBillCollect", jsonObject.optString("resultDesc"));
                return;
            }
            JSONObject object = jsonObject.getJSONObject("result");
            JSONArray jsonArray = object.getJSONArray("collectedList");
            int length = jsonArray.length();
            if (length == 0) {
                return;
            }
            for (int i = 0; i < length; i++) {
                Log.other("黄金票🙈[" + jsonArray.getString(i) + "]");
            }
            Log.other("黄金票🏦本次总共获得[" + JsonUtil.getValueByPath(object, "collectedCamp.amount") + "]");
        } catch (Throwable th) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void enableGameCenter() {
        try {
            try {
                String str = AntMemberRpcCall.querySignInBall();
                JSONObject jsonObject = new JSONObject(str);
                if (!jsonObject.optBoolean("success")) {
                    Log.i(TAG + ".signIn.querySignInBall", jsonObject.optString("resultDesc"));
                    return;
                }
                str = JsonUtil.getValueByPath(jsonObject, "data.signInBallModule.signInStatus");
                if (String.valueOf(true).equals(str)) {
                    return;
                }
                str = AntMemberRpcCall.continueSignIn();
                TimeUtil.sleep(300);
                jsonObject = new JSONObject(str);
                if (!jsonObject.optBoolean("success")) {
                    Log.i(TAG + ".signIn.continueSignIn", jsonObject.optString("resultDesc"));
                    return;
                }
                Log.other("游戏中心🎮签到成功");
            } catch (Throwable th) {
                Log.i(TAG, "signIn err:");
                Log.printStackTrace(TAG, th);
            }
            try {
                String str = AntMemberRpcCall.queryPointBallList();
                JSONObject jsonObject = new JSONObject(str);
                if (!jsonObject.optBoolean("success")) {
                    Log.i(TAG + ".batchReceive.queryPointBallList", jsonObject.optString("resultDesc"));
                    return;
                }
                JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jsonObject, "data.pointBallList");
                if (jsonArray == null || jsonArray.length() == 0) {
                    return;
                }
                str = AntMemberRpcCall.batchReceivePointBall();
                TimeUtil.sleep(300);
                jsonObject = new JSONObject(str);
                if (jsonObject.optBoolean("success")) {
                    Log.other("游戏中心🎮全部领取成功[" + JsonUtil.getValueByPath(jsonObject, "data.totalAmount") + "]乐豆");
                } else {
                    Log.i(TAG + ".batchReceive.batchReceivePointBall", jsonObject.optString("resultDesc"));
                }
            } catch (Throwable th) {
                Log.i(TAG, "batchReceive err:");
                Log.printStackTrace(TAG, th);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    private void collectSesame() {
        try {
            String s = AntMemberRpcCall.queryHome();
            JSONObject jo = new JSONObject(s);
            if (!jo.optBoolean("success")) {
                Log.i(TAG + ".run.queryHome", jo.optString("errorMsg"));
                return;
            }
            JSONObject entrance = jo.getJSONObject("entrance");
            if (!entrance.optBoolean("openApp")) {
                Log.other("芝麻信用💌未开通");
                return;
            }
            JSONObject jo2 = new JSONObject(AntMemberRpcCall.queryCreditFeedback());
            TimeUtil.sleep(300);
            if (!jo2.optBoolean("success")) {
                Log.i(TAG + ".collectSesame.queryCreditFeedback", jo2.optString("resultView"));
                return;
            }
            JSONArray ojbect = jo2.getJSONArray("creditFeedbackVOS");
            for (int i = 0; i < ojbect.length(); i++) {
                jo2 = ojbect.getJSONObject(i);
                if (!"UNCLAIMED".equals(jo2.getString("status"))) {
                    continue;
                }
                String title = jo2.getString("title");
                String creditFeedbackId = jo2.getString("creditFeedbackId");
                String potentialSize = jo2.getString("potentialSize");
                jo2 = new JSONObject(AntMemberRpcCall.collectCreditFeedback(creditFeedbackId));
                TimeUtil.sleep(300);
                if (!jo2.optBoolean("success")) {
                    Log.i(TAG + ".collectSesame.collectCreditFeedback", jo2.optString("resultView"));
                    continue;
                }
                Log.other("收芝麻粒🙇🏻‍♂️[" + title + "]#" + potentialSize + "粒");
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    private void beanSignIn() {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.querySignInProcess("AP16242232", "INS_BLUE_BEAN_SIGN"));
            if (!jo.optBoolean("success")) {
                Log.i(jo.toString());
                return;
            }
            if (jo.getJSONObject("result").getBoolean("canPush")) {
                jo = new JSONObject(AntMemberRpcCall.signInTrigger("AP16242232", "INS_BLUE_BEAN_SIGN"));
                if (jo.optBoolean("success")) {
                    String prizeName = jo.getJSONObject("result").getJSONArray("prizeSendOrderDTOList").getJSONObject(0)
                            .getString("prizeName");
                    Log.record("安心豆🫘[" + prizeName + "]");
                } else {
                    Log.i(jo.toString());
                }
            }

        } catch (Throwable t) {
            Log.i(TAG, "beanSignIn err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void beanExchangeBubbleBoost() {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.queryUserAccountInfo("INS_BLUE_BEAN"));
            if (!jo.optBoolean("success")) {
                Log.i(jo.toString());
                return;
            }
            int userCurrentPoint = jo.getJSONObject("result").getInt("userCurrentPoint");
            jo = new JSONObject(AntMemberRpcCall.beanExchangeDetail("IT20230214000700069722"));
            if (!jo.optBoolean("success")) {
                Log.i(jo.toString());
                return;
            }
            jo = jo.getJSONObject("result").getJSONObject("rspContext").getJSONObject("params").getJSONObject("exchangeDetail");
            String itemId = jo.getString("itemId");
            String itemName = jo.getString("itemName");
            jo = jo.getJSONObject("itemExchangeConsultDTO");
            int realConsumePointAmount = jo.getInt("realConsumePointAmount");
            if (!jo.getBoolean("canExchange") || realConsumePointAmount > userCurrentPoint) {
                return;
            }
            jo = new JSONObject(AntMemberRpcCall.beanExchange(itemId, realConsumePointAmount));
            if (jo.optBoolean("success")) {
                Log.record("安心豆🫘[兑换:" + itemName + "]");
            } else {
                Log.i(jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "beanExchangeBubbleBoost err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 兑换黄金票
    private void beanExchangeGoldenTicket() {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.queryUserAccountInfo("INS_BLUE_BEAN"));
            if (!jo.optBoolean("success")) {
                Log.i(jo.toString());
                return;
            }
            int userCurrentPoint = jo.getJSONObject("result").getInt("userCurrentPoint");
            jo = new JSONObject(AntMemberRpcCall.beanExchangeDetail("IT20240322000100086304"));
            if (!jo.optBoolean("success")) {
                Log.i(jo.toString());
                return;
            }
            jo = jo.getJSONObject("result").getJSONObject("rspContext").getJSONObject("params").getJSONObject("exchangeDetail");
            String itemId = jo.getString("itemId");
            String itemName = jo.getString("itemName");
            jo = jo.getJSONObject("itemExchangeConsultDTO");
            int realConsumePointAmount = jo.getInt("realConsumePointAmount");
            if (!jo.getBoolean("canExchange") || realConsumePointAmount > userCurrentPoint) {
                return;
            }
            jo = new JSONObject(AntMemberRpcCall.beanExchange(itemId, realConsumePointAmount));
            if (jo.optBoolean("success")) {
                Log.record("安心豆🫘[兑换:" + itemName + "]");
            } else {
                Log.i(jo.toString());
            }
        } catch (Throwable t) {
            Log.i(TAG, "beanExchangeGoldenTicket err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 会员积分兑换
    private void memberPointExchangeBenefit() {
        try {
            String userId = UserIdMap.getCurrentUid();
//            JSONObject jo = new JSONObject(AntMemberRpcCall.queryIndexNaviBenefitFlowV2(userId, "14"));
            JSONObject jo = new JSONObject(AntMemberRpcCall.queryDeliveryZoneDetail(userId, "全积分"));
            if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                Log.record(jo.getString("resultDesc"));
                Log.i(jo.getString("resultDesc"), jo.toString());
                return;
            }
            if (!jo.has("entityInfoList")) {
                Log.record("会员积分[未实名账号无可兑换福利]");
                return;
            }
            JSONArray entityInfoList = jo.getJSONArray("entityInfoList");
            for (int i = 0; i < entityInfoList.length(); i++) {
                JSONObject entityInfo = entityInfoList.getJSONObject(i);
                JSONObject benefitInfo = entityInfo.getJSONObject("benefitInfo");
                JSONObject pricePresentation = benefitInfo.getJSONObject("pricePresentation");
                if (!"POINT_PAY".equals(pricePresentation.optString("strategyType"))) {
                    continue;
                }
                String name = benefitInfo.getString("name");
                String benefitId = benefitInfo.getString("benefitId");
                MemberBenefitIdMap.add(benefitId, name);
                if (!Status.canMemberPointExchangeBenefit(benefitId)
                        || !memberPointExchangeBenefitList.getValue().contains(benefitId)) {
                    continue;
                }
                String itemId = benefitInfo.getString("itemId");
                if (exchangeBenefit(benefitId, itemId)) {
                    String point = pricePresentation.getString("point");
                    Log.record("会员积分🎐兑换福利[" + name + "]#花费" + point + "积分");
                }
            }
            MemberBenefitIdMap.save(userId);
        } catch (Throwable t) {
            Log.i(TAG, "pointExchangeBenefit err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean exchangeBenefit(String benefitId, String itemId) {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.exchangeBenefit(benefitId, itemId));
            if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                Log.record(jo.getString("resultDesc"));
                Log.i(jo.getString("resultDesc"), jo.toString());
                return false;
            }
            return true;
        } catch (Throwable t) {
            Log.i(TAG, "exchangeBenefit err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    // 我的快递任务
    private void RecommendTask() {
        try {
            // 调用 AntMemberRpcCall.queryRecommendTask() 获取 JSON 数据
            String response = AntMemberRpcCall.queryRecommendTask();
            JSONObject jsonResponse = new JSONObject(response);
            // 获取 taskDetailList 数组
            JSONArray taskDetailList = jsonResponse.getJSONArray("taskDetailList");
            // 遍历 taskDetailList
            for (int i = 0; i < taskDetailList.length(); i++) {
                JSONObject taskDetail = taskDetailList.getJSONObject(i);
                // 检查 "canAccess" 的值是否为 true
                boolean canAccess = taskDetail.optBoolean("canAccess", false);
                if (!canAccess) {
                    // 如果 "canAccess" 不为 true，跳过
                    continue;
                }
                // 获取 taskMaterial 对象
                JSONObject taskMaterial = taskDetail.optJSONObject("taskMaterial");
                // 获取 taskBaseInfo 对象
                JSONObject taskBaseInfo = taskDetail.optJSONObject("taskBaseInfo");
                // 获取 taskCode
                String taskCode = taskMaterial.optString("taskCode", "");
                // 根据 taskCode 执行不同的操作
                if ("WELFARE_PLUS_ANT_FOREST".equals(taskCode) || "WELFARE_PLUS_ANT_OCEAN".equals(taskCode)) {
                    if ("WELFARE_PLUS_ANT_FOREST".equals(taskCode)) {
                        //String forestHomePageResponse = AntMemberRpcCall.queryforestHomePage();
                        //TimeUtil.sleep(2000);
                        String forestTaskResponse = AntMemberRpcCall.forestTask();
                        TimeUtil.sleep(500);
                        String forestreceiveTaskAward = AntMemberRpcCall.forestreceiveTaskAward();
                    } else if ("WELFARE_PLUS_ANT_OCEAN".equals(taskCode)) {
                        //String oceanHomePageResponse = AntMemberRpcCall.queryoceanHomePage();
                        //TimeUtil.sleep(2000);
                        String oceanTaskResponse = AntMemberRpcCall.oceanTask();
                        TimeUtil.sleep(500);
                        String oceanreceiveTaskAward = AntMemberRpcCall.oceanreceiveTaskAward();
                    }
                    if (taskBaseInfo != null) {
                        String appletName = taskBaseInfo.optString("appletName", "Unknown Applet");
                        Log.record("我的快递💌[完成:" + appletName + "]");
                    }
                }
                if (taskMaterial == null || !taskMaterial.has("taskId")) {
                    // 如果 taskMaterial 为 null 或者不包含 taskId，跳过
                    continue;
                }
                // 获取 taskId
                String taskId = taskMaterial.getString("taskId");
                // 调用 trigger 方法
                String triggerResponse = AntMemberRpcCall.trigger(taskId);
                JSONObject triggerResult = new JSONObject(triggerResponse);
                // 检查 success 字段
                boolean success = triggerResult.getBoolean("success");
                if (success) {
                    // 从 triggerResponse 中获取 prizeSendInfo 数组
                    JSONArray prizeSendInfo = triggerResult.getJSONArray("prizeSendInfo");
                    if (prizeSendInfo.length() > 0) {
                        JSONObject prizeInfo = prizeSendInfo.getJSONObject(0);
                        JSONObject extInfo = prizeInfo.getJSONObject("extInfo");
                        // 获取 promoCampName
                        String promoCampName = extInfo.optString("promoCampName", "Unknown Promo Campaign");
                        // 输出日志信息
                        Log.record("我的快递💌[完成:" + promoCampName + "]");
                    }
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "RecommendTask err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void OrdinaryTask() {
        try {
            // 调用 AntMemberRpcCall.queryOrdinaryTask() 获取 JSON 数据
            String response = AntMemberRpcCall.queryOrdinaryTask();
            JSONObject jsonResponse = new JSONObject(response);
            // 检查是否请求成功
            if (jsonResponse.getBoolean("success")) {
                // 获取任务详细列表
                JSONArray taskDetailList = jsonResponse.getJSONArray("taskDetailList");
                // 遍历任务详细列表
                for (int i = 0; i < taskDetailList.length(); i++) {
                    // 获取当前任务对象
                    JSONObject task = taskDetailList.getJSONObject(i);
                    // 提取任务 ID、处理状态和触发类型
                    String taskId = task.optString("taskId");
                    String taskProcessStatus = task.optString("taskProcessStatus");
                    String sendCampTriggerType = task.optString("sendCampTriggerType");
                    // 检查任务状态和触发类型，执行触发操作
                    if (!"RECEIVE_SUCCESS".equals(taskProcessStatus) && !"EVENT_TRIGGER".equals(sendCampTriggerType)) {
                        // 调用 signuptrigger 方法
                        String signuptriggerResponse = AntMemberRpcCall.signuptrigger(taskId);
                        // 调用 sendtrigger 方法
                        String sendtriggerResponse = AntMemberRpcCall.sendtrigger(taskId);
                        // 解析 sendtriggerResponse
                        JSONObject sendTriggerJson = new JSONObject(sendtriggerResponse);
                        // 判断任务是否成功
                        if (sendTriggerJson.getBoolean("success")) {
                            // 从 sendtriggerResponse 中获取 prizeSendInfo 数组
                            JSONArray prizeSendInfo = sendTriggerJson.getJSONArray("prizeSendInfo");
                            // 获取 prizeName
                            String prizeName = prizeSendInfo.getJSONObject(0).getString("prizeName");
                            Log.record("我的快递💌[完成:" + prizeName + "]");
                        } else {
                            Log.i(TAG, "sendtrigger failed for taskId: " + taskId);
                        }
                        TimeUtil.sleep(1000);
                    }
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "OrdinaryTask err:");
            Log.printStackTrace(TAG, th);
        }
    }

    // 消费金签到
    private void signinCalendar() {
        try {
            String s = AntMemberRpcCall.signinCalendar();
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                boolean signed = jo.optBoolean("isSignInToday");
                if (!signed) {
                    jo = new JSONObject(AntMemberRpcCall.openBoxAward());
                    if (jo.optBoolean("success")) {
                        int amount = jo.getInt("amount");
                        Log.record("攒消费金💰[签到:获得" + amount + "金币]");
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "signinCalendar err:");
            Log.printStackTrace(TAG, t);
        }
    }

}
