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
import io.github.lazyimmortal.sesame.entity.PromiseSimpleTemplate;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.util.*;
import io.github.lazyimmortal.sesame.util.idMap.MemberBenefitIdMap;
import io.github.lazyimmortal.sesame.util.idMap.PromiseSimpleTemplateIdMap;
import io.github.lazyimmortal.sesame.util.idMap.UserIdMap;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;

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
    private BooleanModelField promise;
    private SelectModelField promiseList;
    private BooleanModelField KuaiDiFuLiJia;
    private BooleanModelField signinCalendar;
    private BooleanModelField enableGoldTicket;
    private BooleanModelField enableGameCenter;
    private BooleanModelField zcjSignIn;
    private BooleanModelField merchantKmdk;
    private BooleanModelField beanSignIn;
    private BooleanModelField beanExchangeBubbleBoost;
    private BooleanModelField beanExchangeGoldenTicket;
    private BooleanModelField gainSumInsured;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(memberSign = new BooleanModelField("memberSign", "会员签到", false));
        modelFields.addField(memberPointExchangeBenefit = new BooleanModelField("memberPointExchangeBenefit", "会员积分 | 兑换权益", false));
        modelFields.addField(memberPointExchangeBenefitList = new SelectModelField("memberPointExchangeBenefitList", "会员积分 | 权益列表", new LinkedHashSet<>(), MemberBenefit::getList));
        modelFields.addField(collectSesame = new BooleanModelField("collectSesame", "芝麻粒 | 领取", false));
        modelFields.addField(promise = new BooleanModelField("promise", "生活记录 | 坚持做", false));
        modelFields.addField(promiseList = new SelectModelField("promiseList", "生活记录 | 坚持做列表", new LinkedHashSet<>(), PromiseSimpleTemplate::getList));
        modelFields.addField(KuaiDiFuLiJia = new BooleanModelField("KuaiDiFuLiJia", "我的快递 | 福利加", false));
        modelFields.addField(beanSignIn = new BooleanModelField("beanSignIn", "安心豆 | 签到", false));
        modelFields.addField(beanExchangeGoldenTicket = new BooleanModelField("beanExchangeGoldenTicket", "安心豆 | 兑换黄金票", false));
        modelFields.addField(beanExchangeBubbleBoost = new BooleanModelField("beanExchangeBubbleBoost", "安心豆 | 兑换时光加速器", false));
        modelFields.addField(gainSumInsured = new BooleanModelField("gainSumInsured", "保障金 | 领取", false));
        modelFields.addField(signinCalendar = new BooleanModelField("signinCalendar", "消费金 | 签到", false));
        modelFields.addField(enableGoldTicket = new BooleanModelField("enableGoldTicket", "黄金票 | 签到", false));
        modelFields.addField(zcjSignIn = new BooleanModelField("zcjSignIn", "招财金 | 签到", false));
        modelFields.addField(enableGameCenter = new BooleanModelField("enableGameCenter", "游戏中心 | 签到", false));
        modelFields.addField(merchantKmdk = new BooleanModelField("merchantKmdk", "商户开门 | 打卡", false));
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
            if (promise.getValue()) {
                promise();
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
            if (gainSumInsured.getValue()) {
                gainSumInsured();
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
            if (!Status.hasFlagToday("member::sign")) {
                JSONObject jo = new JSONObject(AntMemberRpcCall.queryMemberSigninCalendar());
                TimeUtil.sleep(500);
                if (MessageUtil.checkResultCode(TAG, jo)) {
                    if (jo.getBoolean("autoSignInSuccess")) {
                        Log.other("会员任务📅签到[坚持"
                                + jo.getString("signinSumDay") + "天]#获得["
                                + jo.getString("signinPoint") + "积分]");
                    }
                    Status.flagToday("member::sign");
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
            JSONObject jo = new JSONObject(AntMemberRpcCall.queryPointCert(page, pageSize));
            TimeUtil.sleep(500);
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            boolean hasNextPage = jo.getBoolean("hasNextPage");
            JSONArray jaCertList = jo.getJSONArray("certList");
            for (int i = 0; i < jaCertList.length(); i++) {
                jo = jaCertList.getJSONObject(i);
                String bizTitle = jo.getString("bizTitle");
                String id = jo.getString("id");
                int pointAmount = jo.getInt("pointAmount");
                jo = new JSONObject(AntMemberRpcCall.receivePointByUser(id));
                if (MessageUtil.checkResultCode(TAG, jo)) {
                    Log.other("会员任务🎖️领取[" + bizTitle + "]奖励#获得[" + pointAmount + "积分]");
                }
            }
            if (hasNextPage) {
                queryPointCert(page + 1, pageSize);
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryPointCert err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void kmdkSignIn() {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.queryActivity());
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            if (Objects.equals("SIGN_IN_ENABLE", jo.getString("signInStatus"))) {
                String activityNo = jo.getString("activityNo");
                jo = new JSONObject(AntMemberRpcCall.signIn(activityNo));
                if (MessageUtil.checkResultCode(TAG, jo)) {
                    Log.other("商家服务🕴🏻[开门打卡签到成功]");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "kmdkSignIn err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void kmdkSignUp() {
        if (Status.hasFlagToday("kmdk::signUp")) {
            return;
        }
        try {
            boolean hasSignUp = false;
            for (int i = 0; i < 5; i++) {
                JSONObject jo = new JSONObject(AntMemberRpcCall.queryActivity());
                if (!MessageUtil.checkSuccess(TAG, jo)) {
                    continue;
                }
                String activityNo = jo.getString("activityNo");
                if (!Log.getFormatDate().replace("-", "").equals(activityNo.split("_")[2])) {
                    break;
                } else if (Objects.equals("SIGN_UP", jo.getString("signUpStatus"))) {
                    Log.record("开门打卡今日已报名！");
                    hasSignUp = true;
                    break;
                } else if (Objects.equals("UN_SIGN_UP", jo.getString("signUpStatus"))) {
                    String activityPeriodName = jo.getString("activityPeriodName");
                    jo = new JSONObject(AntMemberRpcCall.signUp(activityNo));
                    if (MessageUtil.checkSuccess(TAG, jo)) {
                        Log.other("商家服务🕴🏻报名[" + activityPeriodName + "开门打卡]");
                        hasSignUp = true;
                        break;
                    }
                }
                TimeUtil.sleep(500);
            }
            if (hasSignUp) {
                Status.flagToday("kmdk::signUp");
            }
        } catch (Throwable t) {
            Log.i(TAG, "kmdkSignUp err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private static void zcjSignIn() {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.zcjSignInQuery());
            if (!MessageUtil.checkSuccess(TAG + " zcjSignInQuery", jo)) {
                return;
            }
            jo = jo.getJSONObject("data").getJSONObject("button");
            if (Objects.equals("UNRECEIVED", jo.getString("status"))) {
                jo = new JSONObject(AntMemberRpcCall.zcjSignInExecute());
                if (MessageUtil.checkSuccess(TAG, jo)) {
                    jo = jo.getJSONObject("data");
                    int todayReward = jo.getInt("todayReward");
                    String widgetName = jo.getString("widgetName");
                    Log.other("商家服务🕴🏻完成[" + widgetName + "]#获得[" + todayReward + "商家积分]");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "zcjSignIn err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 商家服务任务 */
    private static void taskListQuery() {
        try {
            boolean doubleCheck = false;
            JSONObject jo = new JSONObject(AntMemberRpcCall.taskListQuery());
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
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
        } catch (Throwable t) {
            Log.i(TAG, "taskListQuery err:");
            Log.printStackTrace(TAG, t);
        } finally {
            TimeUtil.sleep(1000);
        }
    }

    private static void taskReceive(String taskCode, String actionCode, String title) {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.taskReceive(taskCode));
            if (MessageUtil.checkSuccess(TAG, jo)) {
                jo = new JSONObject(AntMemberRpcCall.actioncode(actionCode));
                if (MessageUtil.checkSuccess(TAG, jo)) {
                    jo = new JSONObject(AntMemberRpcCall.produce(actionCode));
                    if (MessageUtil.checkSuccess(TAG, jo)) {
                        Log.other("商家服务🕴🏻完成[" + title + "]");
                    }
                }
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
                JSONObject jo = new JSONObject(AntMemberRpcCall.signPageTaskList());
                TimeUtil.sleep(500);
                boolean doubleCheck = false;
                if (!MessageUtil.checkResultCode(TAG + " signPageTaskList", jo)) {
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
                    doubleCheck = doBrowseTask(taskList);
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
            JSONObject jo = new JSONObject(AntMemberRpcCall.queryAllStatusTaskList());
            TimeUtil.sleep(500);
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONArray availableTaskList = jo.getJSONArray("availableTaskList");
            if (doBrowseTask(availableTaskList)) {
                queryAllStatusTaskList();
            }
        } catch (Throwable t) {
            Log.i(TAG, "queryAllStatusTaskList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 生活记录
    private void promise() {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.promiseQueryHome());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            jo = jo.getJSONObject("data");
            JSONArray promiseSimpleTemplates = jo.getJSONArray("promiseSimpleTemplates");
            for (int i = 0; i < promiseSimpleTemplates.length(); i++) {
                jo = promiseSimpleTemplates.getJSONObject(i);
                String templateId = jo.getString("templateId");
                String promiseName = jo.getString("promiseName");
                String status = jo.getString("status");
                if ("un_join".equals(status) && promiseList.getValue().contains(templateId)) {
                    promiseJoin(querySingleTemplate(templateId));
                }
                PromiseSimpleTemplateIdMap.add(templateId, promiseName);
            }
            PromiseSimpleTemplateIdMap.save(UserIdMap.getCurrentUid());
        } catch (Throwable t) {
            Log.i(TAG, "promise err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private JSONObject querySingleTemplate(String templateId) {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.querySingleTemplate(templateId));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return null;
            }
            jo = jo.getJSONObject("data");
            JSONObject result = new JSONObject();

            result.put("joinFromOuter", false);
            result.put("templateId", jo.getString("templateId"));
            result.put("autoRenewStatus", Boolean.valueOf(jo.getString("autoRenewStatus")));

            JSONObject joinGuarantyRule = jo.getJSONObject("joinGuarantyRule");
            joinGuarantyRule.put("selectValue", joinGuarantyRule.getJSONArray("canSelectValues").getString(0));
            joinGuarantyRule.remove("canSelectValues");
            result.put("joinGuarantyRule", joinGuarantyRule);

            JSONObject joinRule = jo.getJSONObject("joinRule");
            joinRule.put("selectValue", joinRule.getJSONArray("canSelectValues").getString(0));
            joinRule.remove("joinRule");
            result.put("joinRule", joinRule);

            JSONObject periodTargetRule = jo.getJSONObject("periodTargetRule");
            periodTargetRule.put("selectValue", periodTargetRule.getJSONArray("canSelectValues").getString(0));
            periodTargetRule.remove("canSelectValues");
            result.put("periodTargetRule", periodTargetRule);

            JSONObject dataSourceRule = jo.getJSONObject("dataSourceRule");
            dataSourceRule.put("selectValue", dataSourceRule.getJSONArray("canSelectValues").getJSONObject(0).getString("merchantId"));
            dataSourceRule.remove("canSelectValues");
            result.put("dataSourceRule", dataSourceRule);
            return result;
        } catch (Throwable t) {
            Log.i(TAG, "querySingleTemplate err:");
            Log.printStackTrace(TAG, t);
        }
        return null;
    }

    private void promiseJoin(JSONObject data) {
        if (data == null) {
            return;
        }
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.promiseJoin(data));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            jo = jo.getJSONObject("data");
            String promiseName = jo.getString("promiseName");
            Log.other("生活记录📝加入[" + promiseName + "]");
        } catch (Throwable t) {
            Log.i(TAG, "promiseJoin err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 领取保障金
    private void gainSumInsured() {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.queryMultiSceneWaitToGainList());
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            jo = jo.getJSONObject("data");
            Iterator<String> keys = jo.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object jsonDTO = jo.get(key);
                if (jsonDTO instanceof JSONArray) {
                    // 如eventToWaitDTOList、helpChildSumInsuredDTOList
                    JSONArray jsonArray = ((JSONArray) jsonDTO);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        gainMyAndFamilySumInsured(jsonArray.getJSONObject(i));
                    }
                } else if (jsonDTO instanceof JSONObject) {
                    // 如signInDTO、priorityChannelDTO
                    JSONObject jsonObject = ((JSONObject) jsonDTO);
                    if (jsonObject.length() == 0) {
                        continue;
                    }
                    gainMyAndFamilySumInsured(jsonObject);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "gainSumInsured err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void gainMyAndFamilySumInsured(JSONObject giftData) {
        if (giftData == null
                || giftData.optInt("sendType", 2) != 1) {
            return;
        }
        try {
            giftData.put("entrance", "jkj_zhima_dairy66");
            JSONObject jo = new JSONObject(AntMemberRpcCall.gainMyAndFamilySumInsured(giftData));
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            jo = jo.getJSONObject("data").getJSONObject("gainSumInsuredDTO");
            Log.other("攒保障金💰领取[" + jo.optString("gainSumInsuredYuan") + "元保额]");
        } catch (Throwable t) {
            Log.i(TAG, "gainMyAndFamilySumInsured err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 查询持续做明细任务
    private JSONObject promiseQueryDetail(String recordId) throws JSONException {
        JSONObject jo = new JSONObject(AntMemberRpcCall.promiseQueryDetail(recordId));
        if (!jo.optBoolean("success")) {
            return null;
        }
        return jo;
    }

    // 蚂蚁积分-做浏览任务
    private static Boolean doBrowseTask(JSONArray taskList) {
        boolean doubleCheck = false;
        try {
            for (int i = 0; i < taskList.length(); i++) {
                JSONObject task = taskList.getJSONObject(i);
                if (task.getBoolean("hybrid")) {
                    int periodCurrentCount = Integer.parseInt(task.getJSONObject("extInfo").getString("PERIOD_CURRENT_COUNT"));
                    int periodTargetCount = Integer.parseInt(task.getJSONObject("extInfo").getString("PERIOD_TARGET_COUNT"));
                    int count = periodTargetCount > periodCurrentCount ? periodTargetCount - periodCurrentCount : 0;
                    if (count > 0) {
                        doubleCheck = doubleCheck || doBrowseTask(task, periodTargetCount, periodTargetCount);
                    }
                } else {
                    doubleCheck = doubleCheck || doBrowseTask(task, 1, 1);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "doBrowseTask err:");
            Log.printStackTrace(TAG, t);
        }
        return doubleCheck;
    }

    private static Boolean doBrowseTask(JSONObject task, int left, int right) {
        boolean doubleCheck = false;
        try {
            JSONObject taskConfigInfo = task.getJSONObject("taskConfigInfo");
            String name = taskConfigInfo.getString("name");
            Long id = taskConfigInfo.getLong("id");
            String awardParamPoint = taskConfigInfo.getJSONObject("awardParam")
                    .getString("awardParamPoint");
            String targetBusiness = taskConfigInfo.getJSONArray("targetBusiness")
                    .getString(0);
            for (int i = left; i <= right; i++) {
                JSONObject jo = new JSONObject(AntMemberRpcCall.applyTask(name, id));
                TimeUtil.sleep(300);
                if (!MessageUtil.checkResultCode(TAG, jo)) {
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
                if (!MessageUtil.checkResultCode(TAG, jo)) {
                    continue;
                }
                String ex = left == right && left == 1 ? "" : "(" + (i + 1) + "/" + right + ")";
                Log.other("会员任务🎖️完成[" + name + ex + "]#获得[" + awardParamPoint + "积分]");
                doubleCheck = true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "doBrowseTask err:");
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
            JSONObject jo = new JSONObject(AntMemberRpcCall.queryHome());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONObject entrance = jo.getJSONObject("entrance");
            if (!entrance.optBoolean("openApp")) {
                Log.other("芝麻信用💌未开通");
                return;
            }
            jo = new JSONObject(AntMemberRpcCall.queryCreditFeedback());
            TimeUtil.sleep(300);
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONArray ja = jo.getJSONArray("creditFeedbackVOS");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                if (!"UNCLAIMED".equals(jo.getString("status"))) {
                    continue;
                }
                String title = jo.getString("title");
                String creditFeedbackId = jo.getString("creditFeedbackId");
                String potentialSize = jo.getString("potentialSize");
                jo = new JSONObject(AntMemberRpcCall.collectCreditFeedback(creditFeedbackId));
                TimeUtil.sleep(300);
                if (MessageUtil.checkResultCode(TAG, jo)) {
                    Log.other("收芝麻粒🙇🏻‍♂️领取[" + title + "]奖励[芝麻粒*" + potentialSize + "]");
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    private void beanSignIn() {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.querySignInProcess("AP16242232", "INS_BLUE_BEAN_SIGN"));
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            if (jo.getJSONObject("result").getBoolean("canPush")) {
                jo = new JSONObject(AntMemberRpcCall.signInTrigger("AP16242232", "INS_BLUE_BEAN_SIGN"));
                if (MessageUtil.checkSuccess(TAG, jo)) {
                    String prizeName = jo.getJSONObject("result").getJSONArray("prizeSendOrderDTOList").getJSONObject(0)
                            .getString("prizeName");
                    Log.other("安心豆🫘[" + prizeName + "]");
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
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            int userCurrentPoint = jo.getJSONObject("result").getInt("userCurrentPoint");
            jo = new JSONObject(AntMemberRpcCall.beanExchangeDetail("IT20230214000700069722"));
            if (!MessageUtil.checkSuccess(TAG, jo)) {
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
            if (MessageUtil.checkSuccess(TAG, jo)) {
                Log.other("安心豆🫘兑换[" + itemName + "]");
                return;
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
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            int userCurrentPoint = jo.getJSONObject("result").getInt("userCurrentPoint");
            jo = new JSONObject(AntMemberRpcCall.beanExchangeDetail("IT20240322000100086304"));
            if (!MessageUtil.checkSuccess(TAG, jo)) {
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
            if (MessageUtil.checkSuccess(TAG, jo)) {
                Log.other("安心豆🫘兑换[" + itemName + "]");
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
            JSONObject jo = new JSONObject(AntMemberRpcCall.queryDeliveryZoneDetail(userId, "94000SR2024011106752003"));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            if (!jo.has("entityInfoList")) {
                Log.record("会员积分[未实名账号无可兑换权益]");
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
                if (!Status.canMemberPointExchangeBenefitToday(benefitId)
                        || !memberPointExchangeBenefitList.getValue().contains(benefitId)) {
                    continue;
                }
                String itemId = benefitInfo.getString("itemId");
                if (exchangeBenefit(benefitId, itemId)) {
                    String point = pricePresentation.getString("point");
                    Log.other("会员积分🎐兑换[" + name + "]#花费[" + point + "积分]");
                }
            }
            MemberBenefitIdMap.save(userId);
        } catch (Throwable t) {
            Log.i(TAG, "memberPointExchangeBenefit err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean exchangeBenefit(String benefitId, String itemId) {
        try {
            JSONObject jo = new JSONObject(AntMemberRpcCall.exchangeBenefit(benefitId, itemId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
                Status.memberPointExchangeBenefitToday(benefitId);
                return true;
            }
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
                        Log.other("我的快递💌完成[" + appletName + "]");
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
                        Log.other("我的快递💌完成[" + promoCampName + "]");
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
                            Log.other("我的快递💌完成[" + prizeName + "]");
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
            JSONObject jo = new JSONObject(AntMemberRpcCall.signinCalendar());
            if (!MessageUtil.checkSuccess(TAG, jo)) {
                return;
            }
            boolean signed = jo.optBoolean("isSignInToday");
            if (!signed) {
                jo = new JSONObject(AntMemberRpcCall.openBoxAward());
                if (MessageUtil.checkSuccess(TAG, jo)) {
                    int amount = jo.getInt("amount");
                    int consecutiveSignInDays = jo.getInt("consecutiveSignInDays");
                    Log.other("攒消费金💰签到[坚持" + consecutiveSignInDays + "天]#获得[" + amount + "消费金]");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "signinCalendar err:");
            Log.printStackTrace(TAG, t);
        }
    }
}
