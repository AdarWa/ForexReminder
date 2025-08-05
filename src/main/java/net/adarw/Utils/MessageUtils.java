package net.adarw.Utils;

import com.google.gson.JsonObject;
import org.json.JSONObject;
/**
    MESSAGE CODES:
    CODE 0 - SUCCESS
    CODE 2 - DATA AVAILABLE
    CODE 3 - GENERAL ERROR
    CODE 4 - NOT FOUND
 **/
public class MessageUtils {



    public static String getMessage(int code, String message){
        JSONObject object = new JSONObject();
        object.put("code", code);
        object.put("message", message);
        return object.toString();
    }

    public static String getMessage(int code, JSONObject message){
        JSONObject object = new JSONObject();
        object.put("code", code);
        object.put("message", message);
        return object.toString();
    }



    public static String getSuccessMessage(){
        return getMessage(0,"Success");
    }



}
