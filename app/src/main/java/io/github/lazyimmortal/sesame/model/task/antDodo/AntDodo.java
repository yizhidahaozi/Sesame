package io.github.lazyimmortal.sesame.model.task.antDodo;

import org.json.JSONArray;
import org.json.JSONObject;
import io.github.lazyimmortal.sesame.data.ModelFields;
import io.github.lazyimmortal.sesame.data.ModelGroup;
import io.github.lazyimmortal.sesame.data.modelFieldExt.BooleanModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.ChoiceModelField;
import io.github.lazyimmortal.sesame.data.modelFieldExt.SelectModelField;
import io.github.lazyimmortal.sesame.data.task.ModelTask;
import io.github.lazyimmortal.sesame.entity.AlipayUser;
import io.github.lazyimmortal.sesame.entity.AntDodoProp;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.model.task.antFarm.AntFarm.TaskStatus;
import io.github.lazyimmortal.sesame.util.Log;
import io.github.lazyimmortal.sesame.util.TimeUtil;
import io.github.lazyimmortal.sesame.util.UserIdMap;

import java.util.LinkedHashSet;
import java.util.Set;

public class AntDodo extends ModelTask {
    private static final String TAG = AntDodo.class.getSimpleName();

    @Override
    public String getName() {
        return "神奇物种";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.FOREST;
    }

    private BooleanModelField collectToFriend;
    private ChoiceModelField collectToFriendType;
    private SelectModelField collectToFriendList;
    private SelectModelField sendFriendCard;
    private BooleanModelField useProp;
    private SelectModelField usePropList;
    private ChoiceModelField bookStatusType;
    private ChoiceModelField fantasticLevelType;
    private BooleanModelField generateBookMedal;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(collectToFriend = new BooleanModelField("collectToFriend", "帮抽卡 | 开启", false));
        modelFields.addField(collectToFriendType = new ChoiceModelField("collectToFriendType", "帮抽卡 | 动作", CollectToFriendType.COLLECT, CollectToFriendType.nickNames));
        modelFields.addField(collectToFriendList = new SelectModelField("collectToFriendList", "帮抽卡 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(sendFriendCard = new SelectModelField("sendFriendCard", "送卡片 | 好友列表(当前图鉴所有卡片)", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(useProp = new BooleanModelField("useProp", "使用道具 | 开启", false));
        modelFields.addField(usePropList = new SelectModelField("usePropList", "使用道具 | 道具列表", new LinkedHashSet<>(), AntDodoProp::getList));
        modelFields.addField(bookStatusType = new ChoiceModelField("bookStatusType", "万能卡片 | 使用图鉴类型", BookStatusType.END, BookStatusType.nickNames));
        modelFields.addField(fantasticLevelType = new ChoiceModelField("fantasticLevelType", "万能卡片 | 使用最低等级", FantasticLevelType.MAGIC, FantasticLevelType.nickNames));
        modelFields.addField(generateBookMedal = new BooleanModelField("generateBookMedal", "合成图鉴", false));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            collect();
            receiveTaskAward();
            if (useProp.getValue()) {
                propList();
            }
            if (collectToFriend.getValue()) {
                collectToFriend();
            }
            if (generateBookMedal.getValue()) {
                generateBookMedal();
            }
        } catch (Throwable t) {
            Log.i(TAG, "start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /*
     * 神奇物种
     */
    private boolean lastDay(String endDate) {
        long timeStemp = System.currentTimeMillis();
        long endTimeStemp = Log.timeToStamp(endDate);
        return timeStemp < endTimeStemp && (endTimeStemp - timeStemp) < 86400000L;
    }

    public boolean in8Days(String endDate) {
        long timeStemp = System.currentTimeMillis();
        long endTimeStemp = Log.timeToStamp(endDate);
        return timeStemp < endTimeStemp && (endTimeStemp - timeStemp) < 691200000L;
    }

    private void collect() {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryAnimalStatus());
            if (checkMessage(jo)) {
                JSONObject data = jo.getJSONObject("data");
                if (data.getBoolean("collect")) {
                    Log.record("神奇物种卡片今日收集完成！");
                } else {
                    collectAnimalCard();
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo Collect err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void collectAnimalCard() {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.homePage());
            if (checkMessage(jo)) {
                JSONObject data = jo.getJSONObject("data");
                JSONObject animalBook = data.getJSONObject("animalBook");
                String bookId = animalBook.getString("bookId");
                String endDate = animalBook.getString("endDate") + " 23:59:59";
                receiveTaskAward();
                if (!in8Days(endDate) || lastDay(endDate))
                    propList();
                JSONArray ja = data.getJSONArray("limit");
                int index = -1;
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if ("DAILY_COLLECT".equals(jo.getString("actionCode"))) {
                        index = i;
                        break;
                    }
                }
                Set<String> set = sendFriendCard.getValue();
                if (index >= 0) {
                    int leftFreeQuota = jo.getInt("leftFreeQuota");
                    for (int j = 0; j < leftFreeQuota; j++) {
                        jo = new JSONObject(AntDodoRpcCall.collect());
                        if (checkMessage(jo)) {
                            data = jo.getJSONObject("data");
                            JSONObject animal = data.getJSONObject("animal");
                            String ecosystem = animal.getString("ecosystem");
                            String name = animal.getString("name");
                            Log.forest("神奇物种🦕[" + ecosystem + "]#" + name);
                            if (!set.isEmpty()) {
                                for (String userId : set) {
                                    if (!UserIdMap.getCurrentUid().equals(userId)) {
                                        int fantasticStarQuantity = animal.optInt("fantasticStarQuantity", 0);
                                        if (fantasticStarQuantity == 3) {
                                            sendCard(animal, userId);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (!set.isEmpty()) {
                    for (String userId : set) {
                        if (!UserIdMap.getCurrentUid().equals(userId)) {
                            sendCard(bookId, userId);
                            break;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo CollectAnimalCard err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveTaskAward() {
        try {
            th:do {
                String s = AntDodoRpcCall.taskList();
                JSONObject jo = new JSONObject(s);
                if (checkMessage(jo)) {
                    JSONArray taskGroupInfoList = jo.getJSONObject("data").optJSONArray("taskGroupInfoList");
                    if (taskGroupInfoList == null)
                        return;
                    for (int i = 0; i < taskGroupInfoList.length(); i++) {
                        JSONObject antDodoTask = taskGroupInfoList.getJSONObject(i);
                        JSONArray taskInfoList = antDodoTask.getJSONArray("taskInfoList");
                        for (int j = 0; j < taskInfoList.length(); j++) {
                            JSONObject taskInfo = taskInfoList.getJSONObject(j);
                            JSONObject taskBaseInfo = taskInfo.getJSONObject("taskBaseInfo");
                            JSONObject bizInfo = new JSONObject(taskBaseInfo.getString("bizInfo"));
                            String taskType = taskBaseInfo.getString("taskType");
                            String taskTitle = bizInfo.optString("taskTitle", taskType);
                            String awardCount = bizInfo.optString("awardCount", "1");
                            String sceneCode = taskBaseInfo.getString("sceneCode");
                            String taskStatus = taskBaseInfo.getString("taskStatus");
                            if (TaskStatus.FINISHED.name().equals(taskStatus)) {
                                JSONObject joAward = new JSONObject(
                                        AntDodoRpcCall.receiveTaskAward(sceneCode, taskType));
                                if (joAward.optBoolean("success"))
                                    Log.forest("任务奖励🎖️[" + taskTitle + "]#" + awardCount + "个");
                                else
                                    Log.record("领取失败，" + s);
                                Log.i(joAward.toString());
                            } else if (TaskStatus.TODO.name().equals(taskStatus)) {
                                if ("SEND_FRIEND_CARD".equals(taskType) || "AD_BIODIVERSITY_MASTERCARD".equals(taskType)) {
                                    JSONObject joFinishTask = new JSONObject(
                                            AntDodoRpcCall.finishTask(sceneCode, taskType));
                                    if (joFinishTask.optBoolean("success")) {
                                        Log.forest("物种任务🧾️[" + taskTitle + "]");
                                        continue th;
                                    } else {
                                        Log.record("完成任务失败，" + taskTitle);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            } while (true);
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo ReceiveTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void propList() {
        try {
            th:
            do {
                JSONObject jo = new JSONObject(AntDodoRpcCall.propList());
                if (!checkMessage(jo)) {
                    break;
                }
                jo = jo.getJSONObject("data");
                JSONArray propList = jo.getJSONArray("propList");
                for (int i = 0; i < propList.length(); i++) {
                    JSONObject prop = propList.getJSONObject(i);
                    String propType = prop.getString("propType");
                    JSONArray propIdList = prop.getJSONArray("propIdList");
                    String propId = propIdList.getString(0);
                    boolean isUseProp = usePropList.getValue().contains(propType);
                    if (!isUseProp) {
                        continue;
                    }
                    if ("UNIVERSAL_CARD_7_DAYS".equals(propType)) {
                        if (!usePropUniversalCard(propId, propType)) {
                            continue;
                        }
                    } else {
                        // COLLECT_TIMES_7_DAYS
                        // COLLECT_HISTORY_ANIMAL_7_DAYS
                        // COLLECT_TO_FRIEND_TIMES_7_DAYS
                        if (!consumeProp(propId, propType)) {
                            continue;
                        }
                    }
                    if (prop.optInt("holdsNum", 1) > 1) {
                        continue th;
                    }
                }
                break;
            } while (true);
        } catch (Throwable th) {
            Log.i(TAG, "AntDodo PropList err:");
            Log.printStackTrace(TAG, th);
        }
    }

    // 使用万能卡
    private Boolean usePropUniversalCard(String propId, String propType) {
        try {
            boolean hasMore;
            int pageStart = 0;
            JSONObject animal = null;
            do {
                JSONObject jo = new JSONObject(AntDodoRpcCall.queryBookList(9, pageStart));
                if (!checkMessage(jo)) {
                    break;
                }
                jo = jo.getJSONObject("data");
                hasMore = jo.getBoolean("hasMore");
                pageStart += 9;
                JSONArray bookForUserList = jo.getJSONArray("bookForUserList");
                for (int i = 0; i < bookForUserList.length(); i++) {
                    jo = bookForUserList.getJSONObject(i);
                    if (isQueryBookInfo(jo.optString("bookStatus"), bookStatusType.getValue())) {
                        JSONObject animalBookResult = jo.getJSONObject("animalBookResult");
                        String bookId = animalBookResult.getString("bookId");
                        animal = queryUniversalAnimal(bookId, animal);
                    }
                }
            } while (hasMore);
            if (animal != null && consumeProp(propId, propType, animal.getString("animalId"))) {
                return true;
            }
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo UsePropUniversalCard err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private static Boolean isQueryBookInfo(String bookStatus, int type) {
        return ("END".equals(bookStatus)
                && (type == BookStatusType.ALL || type == BookStatusType.END))
                ||
                ("DOING".equals(bookStatus)
                && (type == BookStatusType.ALL || type == BookStatusType.DOING));
    }

    private JSONObject queryUniversalAnimal(String bookId, JSONObject animal) {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryBookInfo(bookId));
            if (!checkMessage(jo)) {
                return animal;
            }
            // data: animalBookResult{}
            // data: animalForUserList[]
            JSONArray animalForUserList = jo.getJSONObject("data").getJSONArray("animalForUserList");
            for (int i = 0; i < animalForUserList.length(); i++) {
                jo = animalForUserList.getJSONObject(i);
                int star = jo.getInt("star");
                if (star < FantasticLevelType.stars[fantasticLevelType.getValue()]) {
                    break;
                }
                JSONObject collectDetail = jo.getJSONObject("collectDetail");
                int count = collectDetail.optInt("count", 1 << 30);
                if (animal == null
                        || count < animal.getInt("count")
                        || (count == animal.getInt("count")
                        && star > animal.getInt("star"))) {
                    animal = jo.getJSONObject("animal");
                    animal.put("star", star);
                    animal.put("count", count);
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo QueryUniversalAnimal err:");
            Log.printStackTrace(TAG, t);
        }
        return animal;
    }

    private Boolean consumeProp(String propId, String propType) {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.consumeProp(propId, propType));
            if (!checkMessage(jo)) {
                return false;
            }

            jo = jo.getJSONObject("data");
            String propName = jo.getJSONObject("propConfig").getString("propName");

            if ("COLLECT_TIMES_7_DAYS".equals(propType)
                    || "COLLECT_HISTORY_ANIMAL_7_DAYS".equals(propType)) {
                JSONObject animal = jo.getJSONObject("useResult").getJSONObject("animal");
                String ecosystem = animal.getString("ecosystem");
                String name = animal.getString("name");
                int fantasticStarQuantity = animal.optInt("fantasticStarQuantity", 0);
                String fantasticLevel = "未知";
                if (fantasticStarQuantity == 1) {
                    fantasticLevel = "普通";
                } else if (fantasticStarQuantity == 2) {
                    fantasticLevel = "稀有";
                } else if (fantasticStarQuantity == 3) {
                    fantasticLevel = "神奇";
                }
                Log.forest("使用道具🎭[" + propName + "]#" + ecosystem + "-" + name
                        + "[" + fantasticLevel +  "]");
                if (fantasticStarQuantity == 3) {
                    sendCard(animal);
                }
                return true;
            } else {
                // COLLECT_TO_FRIEND_TIMES_7_DAYS
                Log.forest("使用道具🎭[" + propName + "]");
            }
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo consumeProp err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean consumeProp(String propId, String propType, String animalId) {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.consumeProp(propId, propType, animalId));
            if (!checkMessage(jo)) {
                return false;
            }
            jo = jo.getJSONObject("data");
            String propName = jo.getJSONObject("propConfig").getString("propName");
            JSONObject animal = jo.getJSONObject("useResult").getJSONObject("animal");
            String ecosystem = animal.getString("ecosystem");
            String name = animal.getString("name");
            int fantasticStarQuantity = animal.optInt("fantasticStarQuantity", 0);
            String fantasticLevel = "未知";
            if (fantasticStarQuantity == 1) {
                fantasticLevel = "普通";
            } else if (fantasticStarQuantity == 2) {
                fantasticLevel = "稀有";
            } else if (fantasticStarQuantity == 3) {
                fantasticLevel = "神奇";
            }
            Log.forest("使用道具🎭[" + propName + "]#" + ecosystem + "-" + name
                    + "[" + fantasticLevel +  "]");
            if (fantasticStarQuantity == 3) {
                sendCard(animal);
            }
            return true;
        } catch (Throwable th) {
            Log.i(TAG, "AntDodo consumeProp err:");
            Log.printStackTrace(TAG, th);
        }
        return false;
    }

    private void sendCard(JSONObject animal) {
        try {
            Set<String> map = sendFriendCard.getValue();
            for (String userId : map) {
                if (!UserIdMap.getCurrentUid().equals(userId)) {
                    sendCard(animal, userId);
                    break;
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "AntDodo SendCard err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void sendCard(JSONObject animal, String targetUser) {
        try {
            String animalId = animal.getString("animalId");
            String ecosystem = animal.getString("ecosystem");
            String name = animal.getString("name");
            JSONObject jo = new JSONObject(AntDodoRpcCall.social(animalId, targetUser));
            if (checkMessage(jo)) {
                Log.forest("赠送卡片🦕[" + UserIdMap.getMaskName(targetUser) + "]#" + ecosystem + "-" + name);
            }
        } catch (Throwable th) {
            Log.i(TAG, "AntDodo SendCard err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void sendCard(String bookId, String targetUser) {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryBookInfo(bookId));
            if (checkMessage(jo)) {
                JSONArray animalForUserList = jo.getJSONObject("data").optJSONArray("animalForUserList");
                if (animalForUserList == null) {
                    return;
                }
                for (int i = 0; i < animalForUserList.length(); i++) {
                    JSONObject animalForUser = animalForUserList.getJSONObject(i);
                    int count = animalForUser.getJSONObject("collectDetail").optInt("count");
                    if (count <= 0)
                        continue;
                    JSONObject animal = animalForUser.getJSONObject("animal");
                    for (int j = 0; j < count; j++) {
                        sendCard(animal, targetUser);
                        TimeUtil.sleep(500L);
                    }
                }
            }
        } catch (Throwable th) {
            Log.i(TAG, "AntDodo SendCard err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private void collectToFriend() {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryFriend());
            if (checkMessage(jo)) {
                int count = 0;
                JSONArray limitList = jo.getJSONObject("data").getJSONObject("extend").getJSONArray("limit");
                for (int i = 0; i < limitList.length(); i++) {
                    JSONObject limit = limitList.getJSONObject(i);
                    if (limit.getString("actionCode").equals("COLLECT_TO_FRIEND")) {
                        if (limit.getLong("startTime") > System.currentTimeMillis()) {
                            return;
                        }
                        count = limit.getInt("leftLimit");
                        break;
                    }

                }
                JSONArray friendList = jo.getJSONObject("data").getJSONArray("friends");
                for (int i = 0; i < friendList.length() && count > 0; i++) {
                    JSONObject friend = friendList.getJSONObject(i);
                    if (friend.getBoolean("dailyCollect")) {
                        continue;
                    }
                    String useId = friend.getString("userId");
                    boolean isCollectToFriend = collectToFriendList.getValue().contains(useId);
                    if (collectToFriendType.getValue() == CollectToFriendType.DONT_COLLECT) {
                        isCollectToFriend = !isCollectToFriend;
                    }
                    if (!isCollectToFriend) {
                        continue;
                    }
                    jo = new JSONObject(AntDodoRpcCall.collect(useId));
                    if (checkMessage(jo)) {
                        String ecosystem = jo.getJSONObject("data").getJSONObject("animal").getString("ecosystem");
                        String name = jo.getJSONObject("data").getJSONObject("animal").getString("name");
                        String userName = UserIdMap.getMaskName(useId);
                        Log.forest("神奇物种🦕帮好友[" + userName + "]抽卡[" + ecosystem + "]#" + name);
                        count--;
                    }
                }

            }
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo CollectHelpFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void generateBookMedal() {
        // 图鉴合成状态 合成 可以合成 不能合成
        // medalGenerationStatus: GENERATED CAN_GENERATE CAN_NOT_GENERATE

        // 卡片收集情况 完成 未完成
        // bookCollectedStatus: COMPLETED NOT_COMPLETED

        // 卡片收集进度
        // collectProgress 10/10 2/10
        try {
            boolean hasMore;
            int pageStart = 0;
            do {
                JSONObject jo = new JSONObject(AntDodoRpcCall.queryBookList(9, pageStart));
                if (!checkMessage(jo)) {
                    break;
                }
                jo = jo.getJSONObject("data");
                hasMore = jo.getBoolean("hasMore");
                pageStart += 9;
                JSONArray bookForUserList = jo.getJSONArray("bookForUserList");
                for (int i = 0; i < bookForUserList.length(); i++) {
                    jo = bookForUserList.getJSONObject(i);
                    if (!"CAN_GENERATE".equals(jo.getString("medalGenerationStatus"))) {
                        continue;
                    }
                    JSONObject animalBookResult = jo.getJSONObject("animalBookResult");
                    String bookId = animalBookResult.getString("bookId");
                    String ecosystem = animalBookResult.getString("ecosystem");
                    jo = new JSONObject(AntDodoRpcCall.generateBookMedal(bookId));
                    if (!checkMessage(jo)) {
                        break;
                    }
                    Log.forest("神奇物种🦕合成图鉴[" + ecosystem + "]");
                }
            } while (hasMore);
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo GenerateBookMedal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean checkMessage(JSONObject jo) {
        try {
            if (!"SUCCESS".equals(jo.optString("resultCode"))) {
                if (jo.has("resultCode")) {
                    Log.record(jo.getString("resultDesc"));
                    Log.i(jo.getString("resultDesc"), jo.toString());
                } else {
                    Log.i(jo.toString());
                }
                return false;
            }
            return true;
        } catch (Throwable t) {
            Log.i(TAG, "AntDodo CheckMessage err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    public interface CollectToFriendType {

        int COLLECT = 0;
        int DONT_COLLECT = 1;

        String[] nickNames = {"选中帮抽卡", "选中不帮抽卡"};

    }

    public interface BookStatusType {
        int ALL = 0;
        int END = 1;
        int DOING = 2;

        String[] nickNames = {"全部图鉴", "往期图鉴", "本期图鉴"};
    }

    public interface FantasticLevelType {
        int COMMON = 0;
        int RARE = 1;
        int MAGIC = 2;

        String[] nickNames = {"普通", "稀有", "神奇"};
        int[] stars = {1, 2, 3};
    }
}