package io.github.lazyimmortal.sesame.model.task.antStall;

import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import io.github.lazyimmortal.sesame.data.ModelFields;
import io.github.lazyimmortal.sesame.data.ModelGroup;
import io.github.lazyimmortal.sesame.data.modelFieldExt.BooleanModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.ChoiceModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.IntegerModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.SelectModelField;
import io.github.lazyimmortal.sesame.data.task.ModelTask;
import io.github.lazyimmortal.sesame.entity.AlipayUser;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.model.task.readingDada.ReadingDada;
import io.github.lazyimmortal.sesame.util.*;

import java.util.*;

/**
 * @author Constanline
 * @since 2023/08/22
 */
public class AntStall extends ModelTask {
    private static final String TAG = AntStall.class.getSimpleName();

    private static class Seat {
        public String userId;
        public int hot;

        public Seat(String userId, int hot) {
            this.userId = userId;
            this.hot = hot;
        }
    }

    private static final List<String> taskTypeList;

    static {
        taskTypeList = new ArrayList<>();
        // 开启收新村收益提醒
        taskTypeList.add("ANTSTALL_NORMAL_OPEN_NOTICE");
        // 添加首页
        taskTypeList.add("tianjiashouye");
        // 【木兰市集】逛精选好物
//        taskTypeList.add("ANTSTALL_XLIGHT_VARIABLE_AWARD");
        // 去饿了么果园逛一逛
        taskTypeList.add("ANTSTALL_ELEME_VISIT");
        // 去点淘赚元宝提现
        taskTypeList.add("ANTSTALL_TASK_diantao202311");
        taskTypeList.add("ANTSTALL_TASK_nongchangleyuan");
    }

    @Override
    public String getName() {
        return "新村";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.STALL;
    }

    private ChoiceModelField stallOpenType;
    private SelectModelField stallOpenList;
    private BooleanModelField stallAutoClose;
    private ChoiceModelField stallTicketType;
    private SelectModelField stallTicketList;
    private BooleanModelField stallAutoTask;
    private BooleanModelField stallReceiveAward;
    private SelectModelField stallWhiteList;
    private SelectModelField stallBlackList;
    private BooleanModelField stallAllowOpenReject;
    private IntegerModelField stallAllowOpenTime;
    private IntegerModelField stallSelfOpenTime;
    private BooleanModelField stallDonate;
    private BooleanModelField stallInviteRegister;
    private ChoiceModelField stallThrowManureType;
    private SelectModelField stallThrowManureList;
    private ChoiceModelField stallInviteShopType;
    private SelectModelField stallInviteShopList;
    private BooleanModelField roadmap;
    /**
     * 邀请好友开通新村列表
     */
    private SelectModelField stallInviteRegisterList;
    /**
     * 助力好友列表
     */
    private SelectModelField assistFriendList;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(stallOpenType = new ChoiceModelField("stallOpenType", "摆摊 | 动作", StallOpenType.NONE, StallOpenType.nickNames));
        modelFields.addField(stallOpenList = new SelectModelField("stallOpenList", "摆摊 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(stallAutoClose = new BooleanModelField("stallAutoClose", "收摊 | 开启", false));
        modelFields.addField(stallSelfOpenTime = new IntegerModelField("stallSelfOpenTime", "收摊 | 摆摊时长(分钟)", 120));
        modelFields.addField(stallTicketType = new ChoiceModelField("stallTicketType", "贴罚单 | 动作", StallTicketType.NONE, StallTicketType.nickNames));
        modelFields.addField(stallTicketList = new SelectModelField("stallTicketList", "贴罚单 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(stallThrowManureType = new ChoiceModelField("stallThrowManureType", "丢肥料 | 动作", StallThrowManureType.NONE, StallThrowManureType.nickNames));
        modelFields.addField(stallThrowManureList = new SelectModelField("stallThrowManureList", "丢肥料 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(stallInviteShopType = new ChoiceModelField("stallInviteShopType", "邀请摆摊 | 动作", StallInviteShopType.NONE, StallInviteShopType.nickNames));
        modelFields.addField(stallInviteShopList = new SelectModelField("stallInviteShopList", "邀请摆摊 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(stallAllowOpenReject = new BooleanModelField("stallAllowOpenReject", "请走小摊 | 开启", false));
        modelFields.addField(stallAllowOpenTime = new IntegerModelField("stallAllowOpenTime", "请走小摊 | 允许摆摊时长(分钟)", 121));
        modelFields.addField(stallWhiteList = new SelectModelField("stallWhiteList", "请走小摊 | 白名单(超时也不赶)", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(stallBlackList = new SelectModelField("stallBlackList", "请走小摊 | 黑名单(不超时也赶)", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(stallAutoTask = new BooleanModelField("stallAutoTask", "自动任务", false));
        modelFields.addField(stallReceiveAward = new BooleanModelField("stallReceiveAward", "自动领奖", false));
        modelFields.addField(stallDonate = new BooleanModelField("stallDonate", "自动捐赠", false));
        modelFields.addField(roadmap = new BooleanModelField("roadmap", "自动进入下一村", false));
        modelFields.addField(stallInviteRegister = new BooleanModelField("stallInviteRegister", "邀请 | 邀请好友开通新村", false));
        modelFields.addField(stallInviteRegisterList = new SelectModelField("stallInviteRegisterList", "邀请 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(assistFriendList = new SelectModelField("assistFriendList", "助力好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            String s = AntStallRpcCall.home();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                if (!jo.getBoolean("hasRegister") || jo.getBoolean("hasQuit")) {
                    Log.farm("蚂蚁新村⛪请先开启蚂蚁新村");
                    return;
                }

                JSONObject astReceivableCoinVO = jo.getJSONObject("astReceivableCoinVO");
                if (astReceivableCoinVO.optBoolean("hasCoin")) {
                    settleReceivable();
                }

                if (stallThrowManureType.getValue() != StallThrowManureType.NONE) {
                    throwManure();
                }

                JSONObject seatsMap = jo.getJSONObject("seatsMap");
                settle(seatsMap);

                collectManure();

                sendBack(seatsMap);

                if (stallAutoClose.getValue()) {
                    closeShop();
                }

                openShop();

                if (stallAutoTask.getValue()) {
                    taskList();
                    TimeUtil.sleep(500);
                    taskList();
                }
                assistFriend();
                if (stallDonate.getValue() && Status.canStallDonateToday()) {
                    donate();
                }
                if (roadmap.getValue()) {
                    roadmap();
                }
                if (stallTicketType.getValue() != StallTicketType.NONE) {
                    pasteTicket();
                }
            } else {
                Log.record("home err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "home err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void sendBack(String billNo, String seatId, String shopId, String shopUserId) {
        String s = AntStallRpcCall.shopSendBackPre(billNo, seatId, shopId, shopUserId);
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject astPreviewShopSettleVO = jo.getJSONObject("astPreviewShopSettleVO");
                JSONObject income = astPreviewShopSettleVO.getJSONObject("income");
                int amount = (int) income.getDouble("amount");
                s = AntStallRpcCall.shopSendBack(seatId);
                jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    Log.farm("蚂蚁新村⛪请走[" + UserIdMap.getMaskName(shopUserId) + "]的小摊"
                            + (amount > 0 ? "获得金币" + amount : ""));
                } else {
                    Log.record("sendBack err:" + " " + s);
                }
                inviteOpen(seatId);
            } else {
                Log.record("sendBackPre err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "sendBack err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void inviteOpen(String seatId) {
        if (stallInviteShopType.getValue() == StallInviteShopType.NONE) {
            return;
        }
        String s = AntStallRpcCall.rankInviteOpen();
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray friendRankList = jo.getJSONArray("friendRankList");
                for (int i = 0; i < friendRankList.length(); i++) {
                    JSONObject friend = friendRankList.getJSONObject(i);
                    String friendUserId = friend.getString("userId");
                    boolean isInviteShop = stallInviteShopList.getValue().contains(friendUserId);
                    if (stallInviteShopType.getValue() != StallInviteShopType.INVITE) {
                        isInviteShop = !isInviteShop;
                    }
                    if (!isInviteShop) {
                        continue;
                    }
                    if (friend.getBoolean("canInviteOpenShop")) {
                        s = AntStallRpcCall.oneKeyInviteOpenShop(friendUserId, seatId);
                        jo = new JSONObject(s);
                        if ("SUCCESS".equals(jo.getString("resultCode"))) {
                            Log.farm("蚂蚁新村⛪邀请[" + UserIdMap.getMaskName(friendUserId) + "]开店成功");
                            return;
                        }
                    }
                }
            } else {
                Log.record("inviteOpen err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "inviteOpen err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void sendBack(JSONObject seatsMap) {
        try {
            for (int i = 1; i <= 2; i++) {
                JSONObject seat = seatsMap.getJSONObject("GUEST_0" + i);
                String seatId = seat.getString("seatId");
                if ("FREE".equals(seat.getString("status"))) {
                    inviteOpen(seatId);
                    continue;
                }
                // 请走小摊 未开启直接跳过
                if (!stallAllowOpenReject.getValue()) {
                    continue;
                }
                String rentLastUser = seat.optString("rentLastUser");
                if (StringUtil.isEmpty(rentLastUser)) {
                    continue;
                }
                // 白名单直接跳过
                if (stallWhiteList.getValue().contains(rentLastUser)) {
                    continue;
                }
                String rentLastBill = seat.getString("rentLastBill");
                String rentLastShop = seat.getString("rentLastShop");
                // 黑名单直接赶走
                if (stallBlackList.getValue().contains(rentLastUser)) {
                    sendBack(rentLastBill, seatId, rentLastShop, rentLastUser);
                    continue;
                }
                long bizStartTime = seat.getLong("bizStartTime");
                long endTime = bizStartTime + stallAllowOpenTime.getValue() * 60 * 1000;
                if (System.currentTimeMillis() > endTime) {
                    sendBack(rentLastBill, seatId, rentLastShop, rentLastUser);
                } else {
                    String taskId = "SB|" + seatId;
                    if (!hasChildTask(taskId)) {
                        addChildTask(new ChildModelTask(taskId, "SB", () -> {
                            if (stallAllowOpenReject.getValue()) {
                                sendBack(rentLastBill, seatId, rentLastShop, rentLastUser);
                            }
                        }, endTime));
                        Log.record("添加蹲点请走⛪在[" + TimeUtil.getCommonDate(endTime) + "]执行");
                    } /*else {
                        addChildTask(new ChildModelTask(taskId, "SB", () -> {
                            if (stallAllowOpenReject.getValue()) {
                                sendBack(rentLastBill, seatId, rentLastShop, rentLastUser);
                            }
                        }, endTime));
                    }*/
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "sendBack err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void settle(JSONObject seatsMap) {
        try {
            JSONObject seat = seatsMap.getJSONObject("MASTER");
            if (seat.has("coinsMap")) {
                JSONObject coinsMap = seat.getJSONObject("coinsMap");
                JSONObject master = coinsMap.getJSONObject("MASTER");
                String assetId = master.getString("assetId");
                int settleCoin = (int) (master.getJSONObject("money").getDouble("amount"));
                boolean fullShow = master.getBoolean("fullShow");
                if (fullShow || settleCoin > 100) {
                    String s = AntStallRpcCall.settle(assetId, settleCoin);
                    JSONObject jo = new JSONObject(s);
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.farm("蚂蚁新村⛪[收取金币]#" + settleCoin);
                    } else {
                        Log.record("settle err:" + " " + s);
                    }
                }
            }

        } catch (Throwable t) {
            Log.i(TAG, "settle err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void closeShop() {
        String s = AntStallRpcCall.shopList();
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray astUserShopList = jo.getJSONArray("astUserShopList");
                for (int i = 0; i < astUserShopList.length(); i++) {
                    JSONObject shop = astUserShopList.getJSONObject(i);
                    if ("OPEN".equals(shop.getString("status"))) {
                        JSONObject rentLastEnv = shop.getJSONObject("rentLastEnv");
                        long gmtLastRent = rentLastEnv.getLong("gmtLastRent");
                        long shopTime = gmtLastRent + stallSelfOpenTime.getValue() * 60 * 1000;
                        String shopId = shop.getString("shopId");
                        String rentLastBill = shop.getString("rentLastBill");
                        String rentLastUser = shop.getString("rentLastUser");
                        if (System.currentTimeMillis() > shopTime) {
                            shopClose(shopId, rentLastBill, rentLastUser);
                        } else {
                            String taskId = "SH|" + shopId;
                            if (!hasChildTask(taskId)) {
                                addChildTask(new ChildModelTask(taskId, "SH", () -> {
                                    if (stallAutoClose.getValue()) {
                                        shopClose(shopId, rentLastBill, rentLastUser);
                                    }
                                    TimeUtil.sleep(300L);
                                    openShop();
                                }, shopTime));
                                Log.record("添加蹲点收摊⛪在[" + TimeUtil.getCommonDate(shopTime) + "]执行");
                            } /*else {
                                addChildTask(new ChildModelTask(taskId, "SH", () -> {
                                    if (stallAutoClose.getValue()) {
                                        shopClose(shopId, rentLastBill, rentLastUser);
                                    }
                                }, shopTime));
                            }*/
                        }
                    }
                }
            } else {
                Log.record("closeShop err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "closeShop err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void openShop() {
        if (stallOpenType.getValue() == StallOpenType.NONE) {
            return;
        }
        String s = AntStallRpcCall.shopList();
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray astUserShopList = jo.getJSONArray("astUserShopList");
                Queue<String> shopIds = new LinkedList<>();
                for (int i = 0; i < astUserShopList.length(); i++) {
                    JSONObject astUserShop = astUserShopList.getJSONObject(i);
                    if ("FREE".equals(astUserShop.getString("status"))) {
                        shopIds.add(astUserShop.getString("shopId"));
                    }
                }
                rankCoinDonate(shopIds);
            } else {
                Log.record("closeShop err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "closeShop err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void rankCoinDonate(Queue<String> shopIds) {
        String s = AntStallRpcCall.rankCoinDonate();
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray friendRankList = jo.getJSONArray("friendRankList");
                List<Seat> seats = new ArrayList<>();
                for (int i = 0; i < friendRankList.length(); i++) {
                    JSONObject friendRank = friendRankList.getJSONObject(i);
                    if (friendRank.getBoolean("canOpenShop")) {
                        String userId = friendRank.getString("userId");
                        boolean isStallOpen = stallOpenList.getValue().contains(userId);
                        if (stallOpenType.getValue() != StallOpenType.OPEN) {
                            isStallOpen = !isStallOpen;
                        }
                        if (!isStallOpen) {
                            continue;
                        }
                        int hot = friendRank.getInt("hot");
                        seats.add(new Seat(userId, hot));
                    }
                }
                friendHomeOpen(seats, shopIds);
            } else {
                Log.record("rankCoinDonate err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "rankCoinDonate err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void openShop(String seatId, String userId, String shopId) {
        String s = AntStallRpcCall.shopOpen(seatId, userId, shopId);
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.optString("resultCode"))) {
                Log.farm("蚂蚁新村⛪在[" + UserIdMap.getMaskName(userId) + "]家摆摊");
            }
        } catch (Throwable t) {
            Log.i(TAG, "openShop err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void friendHomeOpen(List<Seat> seats, Queue<String> shopIds) {
        Collections.sort(seats, (e1, e2) -> e2.hot - e1.hot);
        for (Seat seat : seats) {
            String shopId = shopIds.poll();
            if (shopId == null) {
                return;
            }
            String userId = seat.userId;
            try {
                String s = AntStallRpcCall.friendHome(userId);
                JSONObject jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.optString("resultCode"))) {
                    JSONObject seatsMap = jo.getJSONObject("seatsMap");
                    JSONObject guest = seatsMap.getJSONObject("GUEST_01");
                    if (guest.getBoolean("canOpenShop")) {
                        openShop(guest.getString("seatId"), userId, shopId);
                    } else {
                        guest = seatsMap.getJSONObject("GUEST_02");
                        if (guest.getBoolean("canOpenShop")) {
                            openShop(guest.getString("seatId"), userId, shopId);
                        }
                    }
                } else {
                    Log.record("新村摆摊失败: " + s);
                    return;
                }
            } catch (Throwable t) {
                Log.printStackTrace(TAG, t);
            }
        }
    }

    private void shopClose(String shopId, String billNo, String userId) {
        String s = AntStallRpcCall.preShopClose(shopId, billNo);
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject income = jo.getJSONObject("astPreviewShopSettleVO").getJSONObject("income");
                s = AntStallRpcCall.shopClose(shopId);
                jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    Log.farm("蚂蚁新村⛪收取在[" + UserIdMap.getMaskName(userId) + "]的摊位获得" + income.getString("amount"));
                } else {
                    Log.record("shopClose err:" + " " + s);
                }
            } else {
                Log.record("shopClose  err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "shopClose  err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void taskList() {
        try {
            String s = AntStallRpcCall.taskList();
            JSONObject jo = new JSONObject(s);
            if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                Log.record("taskList err:" + " " + s);
                return;
            }
            JSONObject signListModel = jo.getJSONObject("signListModel");
            if (!signListModel.getBoolean("currentKeySigned")) {
                signToday();
            }

            JSONArray taskModels = jo.getJSONArray("taskModels");
            for (int i = 0; i < taskModels.length(); i++) {
                try {
                    JSONObject task = taskModels.getJSONObject(i);
                    String taskStatus = task.getString("taskStatus");
                    String taskType = task.getString("taskType");
                    if ("FINISHED".equals(taskStatus)) {
                        receiveTaskAward(taskType);
                        continue;
                    }
                    if (!"TODO".equals(taskStatus)) {
                        continue;
                    }
                    JSONObject bizInfo = new JSONObject(task.getString("bizInfo"));
                    String title = bizInfo.optString("title", taskType);
                    if ("VISIT_AUTO_FINISH".equals(bizInfo.getString("actionType"))
                            || taskTypeList.contains(taskType)) {
                        if (!finishTask(taskType)) {
                            continue;
                        }
                        Log.farm("蚂蚁新村👣任务[" + title + "]完成");
                        TimeUtil.sleep(200L);
                        continue;
                    }
                    switch (taskType) {
                        case "ANTSTALL_NORMAL_DAILY_QA":
                            if (ReadingDada.answerQuestion(bizInfo)) {
                                receiveTaskAward(taskType);
                            }
                            break;
                        case "ANTSTALL_NORMAL_INVITE_REGISTER":
                            if (inviteRegister()) {
                                TimeUtil.sleep(200L);
                                continue;
                            }
                            break;
                        case "ANTSTALL_P2P_DAILY_SHARER":
                            //                                shareP2P();
                            break;
                        case "ANTSTALL_TASK_taojinbihuanduan":
                            //进入淘宝芭芭农场
                            String sceneCode = JsonUtil.getValueByPath(task, "bizInfo.targetUrl")
                                    .replaceAll(".*sceneCode%3D([^&]+).*", "$1");
                            if (sceneCode.isEmpty()) {
                                continue;
                            }
                            s = AntStallRpcCall.queryCallAppSchema(sceneCode);
                            jo = new JSONObject(s);
                            if (!jo.optBoolean("success")) {
                                Log.i(TAG, "taskList.queryCallAppSchema err:" + jo.optString("resultDesc"));
                            }
                            TimeUtil.sleep(5000);
                            AntStallRpcCall.home();
                            AntStallRpcCall.taskList();
                            break;
                        case "ANTSTALL_XLIGHT_VARIABLE_AWARD":
                            //【木兰市集】逛精选好物
                            s = AntStallRpcCall.xlightPlugin();
                            jo = new JSONObject(s);
                            if (!jo.has("playingResult")) {
                                Log.i(TAG, "taskList.xlightPlugin err:" + jo.optString("resultDesc"));
                                continue;
                            }
                            jo = jo.getJSONObject("playingResult");
                            String pid = jo.getString("playingBizId");
                            JSONArray jsonArray = (JSONArray) JsonUtil.getValueByPathObject(jo, "eventRewardDetail.eventRewardInfoList");
                            if (jsonArray == null || jsonArray.length() == 0) {
                                continue;
                            }
                            TimeUtil.sleep(5000);
                            for (int j = 0; j < jsonArray.length(); j++) {
                                try{
                                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                                    s = AntStallRpcCall.finish(pid, jsonObject);
                                    TimeUtil.sleep(5000);
                                    jo = new JSONObject(s);
                                    if (!jo.optBoolean("success")) {
                                        Log.i(TAG, "taskList.finish err:" + jo.optString("resultDesc"));
                                    }
                                } catch (Throwable t) {
                                    Log.i(TAG, "taskList for err:");
                                    Log.printStackTrace(TAG, t);
                                }
                            }
                            break;
                    }
                    TimeUtil.sleep(200L);
                } catch (Throwable t) {
                    Log.i(TAG, "taskList for err:");
                    Log.printStackTrace(TAG, t);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "taskList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void signToday() {
        String s = AntStallRpcCall.signToday();
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.farm("蚂蚁新村⛪[签到成功]");
            } else {
                Log.record("signToday err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "signToday err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveTaskAward(String taskType) {
        if (!stallReceiveAward.getValue()) {
            return;
        }
        String s = AntStallRpcCall.receiveTaskAward(taskType);
        try {
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                Log.farm("蚂蚁新村⛪[领取奖励]");
            } else {
                Log.record("receiveTaskAward err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "receiveTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private boolean finishTask(String taskType) {
        // String s = AntStallRpcCall.finishTask(FriendIdMap.currentUid + "_" +
        // taskType, taskType);
        String s = AntStallRpcCall.finishTask(taskType + "_" + System.currentTimeMillis(), taskType);
        try {
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                return true;
            } else {
                Log.record("finishTask err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "finishTask err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private boolean inviteRegister() {
        if (!stallInviteRegister.getValue()) {
            return false;
        }
        try {
            String s = AntStallRpcCall.rankInviteRegister();
            JSONObject jo = new JSONObject(s);
            if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                Log.record("rankInviteRegister err:" + " " + s);
                return false;
            }
            JSONArray friendRankList = jo.optJSONArray("friendRankList");
            if (friendRankList == null || friendRankList.length() <= 0) {
                return false;
            }
            for (int i = 0; i < friendRankList.length(); i++) {
                JSONObject friend = friendRankList.getJSONObject(i);
                if (!friend.optBoolean("canInviteRegister", false)
                        || !"UNREGISTER".equals(friend.getString("userStatus"))) {
                    continue;
                }
                /* 名单筛选 */
                String userId = friend.getString("userId");
                if (!stallInviteRegisterList.getValue().contains(userId)) {
                    continue;
                }
                jo = new JSONObject(AntStallRpcCall.friendInviteRegister(userId));
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    Log.farm("蚂蚁新村⛪邀请好友[" + UserIdMap.getMaskName(userId) + "]#开通新村");
                    return true;
                } else {
                    Log.record("friendInviteRegister err:" + " " + jo);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "InviteRegister err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private String shareP2P() {
        try {
            String s = AntStallRpcCall.shareP2P();
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                String shareId = jo.getString("shareId");
                Log.record("蚂蚁新村⛪[分享助力]");
                return shareId;
            } else {
                Log.record("shareP2P err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "shareP2P err:");
            Log.printStackTrace(TAG, t);
        }
        return null;
    }

    /**
     * 助力好友
     */
    private void assistFriend() {
        try {
            if (!Status.canAntStallAssistFriendToday()) {
                return;
            }
            Set<String> friendSet = assistFriendList.getValue();
            for (String uid : friendSet) {
                String shareId = Base64.encodeToString((uid + "-" + RandomUtil.getRandom(5) + "ANUTSALTML_2PA_SHARE").getBytes(), Base64.NO_WRAP);
                String str = AntStallRpcCall.achieveBeShareP2P(shareId);
                JSONObject jsonObject = new JSONObject(str);
                TimeUtil.sleep(5000);
                String name = UserIdMap.getMaskName(uid);
                if (!jsonObject.optBoolean("success")) {
                    String code = jsonObject.getString("code");
                    if ("600000028".equals(code)) {
                        Log.record("新村助力🐮被助力次数上限[" + name + "]");
                        continue;
                    }
                    if ("600000027".equals(code)) {
                        Log.record("新村助力💪今日助力他人次数上限");
                        Status.antStallAssistFriendToday();
                        return;
                    }
                    //600000010 人传人邀请关系不存在
                    //600000015 人传人完成邀请，菲方用户
                    //600000031 人传人完成邀请过于频繁
                    //600000029 人传人分享一对一接受邀请达到限制
                    Log.record("新村助力😔失败[" + name + "]" + jsonObject.optString("desc"));
                    continue;
                }
                Log.farm("新村助力🎉成功[" + name + "]");
            }
            //暂时一天只做一次
            Status.antStallAssistFriendToday();
        } catch (Throwable t) {
            Log.i(TAG, "assistFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 捐赠项目
    private void donate() {
        try {
            // 调用远程接口获取项目列表信息
            String s = AntStallRpcCall.projectList();
            // 将返回的 JSON 字符串转换为 JSONObject 对象
            JSONObject jo = new JSONObject(s);
            // 检查返回结果是否成功
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                // 获取 astUserInfoVO 对象
                JSONObject astUserInfoVO = jo.getJSONObject("astUserInfoVO");
                // 获取当前余额的金额
                double currentCoinAmount = astUserInfoVO.getJSONObject("currentCoin").getDouble("amount");
                // 检查当前余额是否大于15000
                if (currentCoinAmount < 15000) {
                    // 当 currentCoinAmount 小于 15000 时，直接返回，不执行后续操作
                    return;
                }
                // 获取项目列表中的 astProjectVOS 数组
                JSONArray astProjectVOS = jo.getJSONArray("astProjectVOS");
                // 遍历项目列表
                for (int i = 0; i < astProjectVOS.length(); i++) {
                    // 获取每个项目的 JSONObject
                    JSONObject project = astProjectVOS.getJSONObject(i);
                    // 检查项目状态是否为 "ONLINE"
                    if ("ONLINE".equals(project.getString("status"))) {
                        // 获取项目的 projectId
                        String projectId = project.getString("projectId");
                        // 调用远程接口获取项目详情
                        s = AntStallRpcCall.projectDetail(projectId);
                        // 将返回的 JSON 字符串转换为 JSONObject 对象
                        JSONObject joProjectDetail = new JSONObject(s);
                        // 检查返回结果是否成功
                        if ("SUCCESS".equals(joProjectDetail.getString("resultCode"))) {
                            // 调用远程接口进行捐赠操作
                            s = AntStallRpcCall.projectDonate(projectId);
                            // 将返回的 JSON 字符串转换为 JSONObject 对象
                            JSONObject joProjectDonate = new JSONObject(s);
                            // 获取捐赠操作返回的 astProjectVO 对象
                            JSONObject astProjectVO = joProjectDonate.getJSONObject("astProjectVO");
                            // 获取 astProjectVO 对象中的 title 字段值
                            String title = astProjectVO.getString("title");
                            // 检查捐赠操作返回结果是否成功
                            if ("SUCCESS".equals(joProjectDonate.getString("resultCode"))) {
                                Log.farm("蚂蚁新村⛪[捐赠:" + title + "]");
                                Status.setStallDonateToday();
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "donate err:");
            Log.printStackTrace(TAG, t);
        }
    }


    // 进入下一村
    private void roadmap() {
        try {
            String roadmapResponse = AntStallRpcCall.roadmap();
            JSONObject roadmapJson = new JSONObject(roadmapResponse);
            if (!"SUCCESS".equals(roadmapJson.getString("resultCode"))) {
                return;
            }
            JSONArray roadList = roadmapJson.getJSONArray("roadList");
            for (int i = 0; i < roadList.length(); i++) {
                JSONObject road = roadList.getJSONObject(i);
                // 检查 status 字段是否为 "NEW"
                if ("NEW".equals(road.getString("status"))) {
                    String villageName = road.getString("villageName");
                    String nextVillageRESPONSE = AntStallRpcCall.nextVillage();
                    JSONObject nextVillageJson = new JSONObject(nextVillageRESPONSE);
                    if (!"SUCCESS".equals(nextVillageJson.getString("resultCode"))) {
                        return;
                    }
                    Log.farm("蚂蚁新村⛪[进入:" + villageName + "]成功");
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "roadmap err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void collectManure() {
        String s = AntStallRpcCall.queryManureInfo();
        try {
            JSONObject jo = new JSONObject(s);
            if (jo.optBoolean("success")) {
                JSONObject astManureInfoVO = jo.getJSONObject("astManureInfoVO");
                if (astManureInfoVO.optBoolean("hasManure")) {
                    int manure = astManureInfoVO.getInt("manure");
                    s = AntStallRpcCall.collectManure();
                    jo = new JSONObject(s);
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        Log.farm("蚂蚁新村⛪获得肥料" + manure + "g");
                    }
                }
            } else {
                Log.record("collectManure err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "collectManure err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void throwManure(JSONArray dynamicList) {
        try {
            String s = AntStallRpcCall.throwManure(dynamicList);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.farm("蚂蚁新村⛪扔肥料");
            }
        } catch (Throwable th) {
            Log.i(TAG, "throwManure err:");
            Log.printStackTrace(TAG, th);
        } finally {
            TimeUtil.sleep(1000);
        }
    }

    private void throwManure() {
        try {
            String s = AntStallRpcCall.dynamicLoss();
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray astLossDynamicVOS = jo.getJSONArray("astLossDynamicVOS");
                JSONArray dynamicList = new JSONArray();
                for (int i = 0; i < astLossDynamicVOS.length(); i++) {
                    JSONObject lossDynamic = astLossDynamicVOS.getJSONObject(i);
                    if (lossDynamic.has("specialEmojiVO")) {
                        continue;
                    }
                    String objectId = lossDynamic.getString("objectId");
                    boolean isThrowManure = stallThrowManureList.getValue().contains(objectId);
                    if (stallThrowManureType.getValue() != StallThrowManureType.THROW) {
                        isThrowManure = !isThrowManure;
                    }
                    if (!isThrowManure) {
                        continue;
                    }
                    JSONObject dynamic = new JSONObject();
                    dynamic.put("bizId", lossDynamic.getString("bizId"));
                    dynamic.put("bizType", lossDynamic.getString("bizType"));
                    dynamicList.put(dynamic);
                    if (dynamicList.length() == 5) {
                        throwManure(dynamicList);
                        dynamicList = new JSONArray();
                    }
                }
                if (dynamicList.length() > 0) {
                    throwManure(dynamicList);
                }
            } else {
                Log.record("throwManure err:" + " " + s);
            }
        } catch (Throwable t) {
            Log.i(TAG, "throwManure err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void settleReceivable() {
        String s = AntStallRpcCall.settleReceivable();
        try {
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.farm("蚂蚁新村⛪收取应收金币");
            }
        } catch (Throwable th) {
            Log.i(TAG, "settleReceivable err:");
            Log.printStackTrace(TAG, th);
        }
    }

    /**
     * 贴罚单
     */
    private void pasteTicket() {
        try {
            if (!Status.canPasteTicketTime()) {
                return;
            }
            while (true) {
                try {
                    String str = AntStallRpcCall.nextTicketFriend();
                    JSONObject jsonObject = new JSONObject(str);
                    if (!jsonObject.optBoolean("success")) {
                        Log.i(TAG, "pasteTicket.nextTicketFriend err:" + jsonObject.optString("resultDesc"));
                        return;
                    }
                    if (jsonObject.getInt("canPasteTicketCount") == 0) {
                        Log.farm("蚂蚁新村👍[今日罚单已贴完]");
                        Status.pasteTicketTime();
                        return;
                    }
                    String friendId = jsonObject.optString("friendUserId");
                    if (friendId.isEmpty()) {
                        return;
                    }
                    boolean isStallTicket = stallTicketList.getValue().contains(friendId);
                    if (stallTicketType.getValue() != StallTicketType.TICKET) {
                        isStallTicket = !isStallTicket;
                    }
                    if (!isStallTicket) {
                        continue;
                    }
                    str = AntStallRpcCall.friendHome(friendId);
                    jsonObject = new JSONObject(str);
                    if (!jsonObject.optBoolean("success")) {
                        Log.i(TAG, "pasteTicket.friendHome err:" + jsonObject.optString("resultDesc"));
                        return;
                    }
                    JSONObject object = jsonObject.getJSONObject("seatsMap");
                    // 使用 keys() 方法获取所有键
                    Iterator<String> keys = object.keys();
                    // 遍历所有键
                    while (keys.hasNext()) {
                        try {
                            String key = keys.next();
                            // 获取键对应的值
                            Object propertyValue = object.get(key);
                            if (!(propertyValue instanceof JSONObject)) {
                                continue;
                            }
                            //如signInDTO、priorityChannelDTO
                            JSONObject jo = ((JSONObject) propertyValue);
                            if (jo.length() == 0) {
                                continue;
                            }
                            if (jo.getBoolean("canOpenShop") || !"BUSY".equals(jo.getString("status")) || !jo.getBoolean("overTicketProtection")) {
                                continue;
                            }
                            String rentLastUser = jo.getString("rentLastUser");
                            str = AntStallRpcCall.ticket(jo.getString("rentLastBill"), jo.getString("seatId"),
                                    jo.getString("rentLastShop"), rentLastUser, jo.getString("userId"));
                            jo = new JSONObject(str);
                            if (!jo.optBoolean("success")) {
                                Log.i(TAG, "pasteTicket.ticket err:" + jo.optString("resultDesc"));
                                return;
                            }
                            Log.farm("蚂蚁新村🚫在[" + UserIdMap.getMaskName(friendId) + "]贴罚单");
                        } finally {
                            TimeUtil.sleep(1000);
                        }
                    }
                } finally {
                    TimeUtil.sleep(1000);
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "pasteTicket err:");
            Log.printStackTrace(TAG, th);
        }
    }

    public interface StallOpenType {

        int NONE = 0;
        int OPEN = 1;
        int NOT_OPEN = 2;

        String[] nickNames = {"不摆摊", "摆摊已选好友", "摆摊未选好友"};

    }

    public interface StallTicketType {

        int NONE = 0;
        int TICKET = 1;
        int NOT_TICKET = 2;

        String[] nickNames = {"不贴罚单", "贴已选好友", "贴未选好友"};

    }

    public interface StallThrowManureType {

        int NONE = 0;
        int THROW = 1;
        int NOT_THROW = 2;

        String[] nickNames = {"不丢肥料", "丢已选好友", "丢未选好友"};

    }

    public interface StallInviteShopType {

        int NONE = 0;
        int INVITE = 1;
        int NOT_INVITE = 2;

        String[] nickNames = {"不邀请摆摊", "邀请已选好友", "邀请未选好友"};
    }

}