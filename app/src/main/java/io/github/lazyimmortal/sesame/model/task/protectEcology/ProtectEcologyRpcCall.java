package io.github.lazyimmortal.sesame.model.task.protectEcology;

import io.github.lazyimmortal.sesame.hook.ApplicationHook;
import io.github.lazyimmortal.sesame.util.RandomUtil;

public class ProtectEcologyRpcCall {

    public static String queryTreeItemsForExchange(String applyActions, String itemTypes) {
        // "applyActions": "LIMITED,NO_STOCK,AVAILABLE"
        // "itemTypes": "project,special"
        String args = "[{\"applyActions\":\"" + applyActions + "\",\"itemTypes\":\"" + itemTypes + "\"}]";
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryTreeItemsForExchange", args);
    }
}
