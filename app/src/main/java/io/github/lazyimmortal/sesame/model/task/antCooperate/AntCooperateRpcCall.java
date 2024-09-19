package io.github.lazyimmortal.sesame.model.task.antCooperate;

import io.github.lazyimmortal.sesame.hook.ApplicationHook;

public class AntCooperateRpcCall {
    private static final String VERSION = "20230501";

    public static String queryUserCooperatePlantList() {
        return ApplicationHook.requestString("alipay.antmember.forest.h5.queryUserCooperatePlantList", "[{}]");
    }

    public static String queryCooperatePlant(String cooperationId) {
        String args1 = "[{\"cooperationId\":\"" + cooperationId + "\"}]";
        return ApplicationHook.requestString("alipay.antmember.forest.h5.queryCooperatePlant", args1);
    }

    public static String cooperateWater(String uid, String cooperationId, int energyCount) {
        return ApplicationHook.requestString("alipay.antmember.forest.h5.cooperateWater",
                "[{\"bizNo\":\"" + uid + "_" + cooperationId + "_" + System.currentTimeMillis() + "\",\"cooperationId\":\""
                        + cooperationId + "\",\"energyCount\":" + energyCount + ",\"source\":\"\",\"version\":\"" + VERSION
                        + "\"}]");
    }

    /**
     * 获取合种浇水量排行
     * @param bizType 参数：D/A,“D”为查询当天，“A”为查询所有
     * @param cooperationId 合种ID
     * @return
     */
    public static String queryCooperateRank(String bizType, String cooperationId) {
        return  ApplicationHook.requestString("alipay.antmember.forest.h5.queryCooperateRank",
                "[{\"bizType\":\""+ bizType + "\",\"cooperationId\":\"" + cooperationId + "\",\"source\":\"chInfo_ch_url-https://render.alipay.com/p/yuyan/180020010001247580/home.html\"}]");

    }
}
