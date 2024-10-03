package io.github.lazyimmortal.sesame.model.task.protectEcology;

import io.github.lazyimmortal.sesame.hook.ApplicationHook;

public class ReserveRpcCall {
    private static final String VERSION = "20230501";
    private static final String VERSION2 = "20230522";

    public static String queryTreeItemsForExchange() {
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryTreeItemsForExchange", "[{}]");
    }

    public static String queryTreeForExchange(int projectId) {
        String args = "[{\"projectId\":\"" + projectId + "\"}]";
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryTreeForExchange", args);
    }

    public static String exchangeTree(int projectId) {
        String args = "[{\"projectId\":" + projectId + ",\"sToken\":\"" + System.currentTimeMillis() + "\"]";
        return ApplicationHook.requestString("alipay.antmember.forest.h5.exchangeTree", args);
    }
}
