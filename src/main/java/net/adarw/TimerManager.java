package net.adarw;

import net.adarw.Utils.KeyValuePair;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TimerManager {
    public static ArrayList<KeyValuePair<String,TimerEx>> timers = new ArrayList<>();

    private static TimerEx noCollision(TimerEx timer){
        for (KeyValuePair<String, TimerEx> entry : timers) {
            if(entry.getValue().date.getTime() == timer.date.getTime()){
                timer.date.setTime(timer.date.getTime()+1000);
                return noCollision(timer);
            }
        }
        return timer;
    }

    public static void addTimer(String uuid,TimerEx timer){
        TimerEx t = noCollision(timer);
        timers.add(new KeyValuePair<>(uuid,t));
        t.startTimer();
    }

    public static void removeTimer(String uuid){
        ArrayList<KeyValuePair<String, TimerEx>> clone = (ArrayList<KeyValuePair<String, TimerEx>>) timers.clone();
        for(KeyValuePair<String, TimerEx> timer : timers){
            if(timer.getKey().equals(uuid)){
                timer.getValue().stopTimer();
                clone.remove(timer);
            }
        }
        timers = clone;
    }

    public static void clear(){
        for (KeyValuePair<String, TimerEx> entry : timers) {
            entry.getValue().stopTimer();
        }
        timers.clear();
    }

}
