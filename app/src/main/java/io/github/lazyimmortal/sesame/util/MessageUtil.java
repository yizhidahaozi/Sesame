package io.github.lazyimmortal.sesame.util;

import org.json.JSONObject;

public class MessageUtil {
    private static final String TAG = MessageUtil.class.getSimpleName();
    private static final String UNKNOWN_TAG = "Unknown TAG";

    public static Boolean checkMemo(JSONObject jo) {
        return checkMemo(UNKNOWN_TAG, jo);
    }

    public static Boolean checkMemo(String tag, JSONObject jo) {
        try {
            if (!"SUCCESS".equals(jo.optString("memo"))) {
                if (jo.has("memo")) {
                    Log.record(jo.getString("memo"));
                    Log.i(jo.getString("memo"), jo.toString());
                } else {
                    Log.i(tag, jo.toString());
                }
                return false;
            }
            return true;
        } catch (Throwable t) {
            Log.i(TAG, "checkMemo err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    public static Boolean checkResultCode(JSONObject jo) {
        return checkResultCode(UNKNOWN_TAG, jo);
    }

    public static Boolean checkResultCode(String tag, JSONObject jo) {
        try {
            if (!"SUCCESS".equals(jo.optString("resultCode"))) {
                if (jo.has("resultDesc")) {
                    Log.record(jo.getString("resultDesc"));
                    Log.i(jo.getString("resultDesc"), jo.toString());
                } else if (jo.has("resultView")) {
                    Log.record(jo.getString("resultView"));
                    Log.i(jo.getString("resultView"), jo.toString());
                } else {
                    Log.i(tag, jo.toString());
                }
                return false;
            }
            return true;
        } catch (Throwable t) {
            Log.i(TAG, "checkResultCode err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }
}
