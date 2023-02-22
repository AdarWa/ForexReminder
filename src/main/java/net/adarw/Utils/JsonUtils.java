package net.adarw.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    public static boolean isValidJson(String json){
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }
}
