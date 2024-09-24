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
            Object resultCode = jo.get("resultCode");
            if (resultCode instanceof Integer) {
                return checkResultCodeInteger(tag, jo);
            } else if (resultCode instanceof String) {
                return checkResultCodeString(tag, jo);
            }
            Log.i(tag, jo.toString());
            return false;
        } catch (Throwable t) {
            Log.i(TAG, "checkResultCode err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    public static Boolean checkResultCodeString(String tag, JSONObject jo) {
        try {
            String resultCode = jo.optString("resultCode");
            if (!resultCode.equals("SUCCESS") && !resultCode.equals("100")) {
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
            Log.i(TAG, "checkResultCodeString err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    public static Boolean checkResultCodeInteger(String tag, JSONObject jo) {
        try {
            int resultCode = jo.optInt("resultCode");
            if (resultCode != 200) {
                if (jo.has("resultMsg")) {
                    Log.record(jo.getString("resultMsg"));
                    Log.i(jo.getString("resultMsg"), jo.toString());
                } else {
                    Log.i(tag, jo.toString());
                }
                return false;
            }
            return true;
        } catch (Throwable t) {
            Log.i(TAG, "checkResultCodeInteger err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    public static Boolean checkSuccess(JSONObject jo) {
        return checkSuccess(UNKNOWN_TAG, jo);
    }

    public static Boolean checkSuccess(String tag, JSONObject jo) {
        try {
            if (!jo.optBoolean("success") && !jo.optBoolean("isSuccess")) {
                if (jo.has("errorMsg")) {
                    Log.record(jo.getString("errorMsg"));
                    Log.i(jo.getString("errorMsg"), jo.toString());
                } else if (jo.has("errorMessage")) {
                    Log.record(jo.getString("errorMessage"));
                    Log.i(jo.getString("errorMessage"), jo.toString());
                } else if (jo.has("desc")) {
                    Log.record(jo.getString("desc"));
                    Log.i(jo.getString("desc"), jo.toString());
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
            Log.i(TAG, "checkSuccess err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }
}
