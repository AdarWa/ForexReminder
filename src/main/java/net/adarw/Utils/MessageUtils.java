package net.adarw.Utils;

import com.google.gson.JsonObject;
import org.json.JSONObject;

public class MessageUtils {

    public static String getMessage(int code, String message){
        JSONObject object = new JSONObject();
        object.put("code", code);
        object.put("message", message);
        return object.toString();
    }

    public static String getSuccessMessage(){
        return getMessage(0,"Success");
    }

}
