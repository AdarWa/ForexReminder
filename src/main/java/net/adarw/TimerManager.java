package net.adarw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class TimerManager {
    public static HashMap<String,TimerEx> timers = new HashMap<>();

    public static void addTimer(String uuid,TimerEx timer){
        timers.put(uuid,timer);
        timer.startTimer();
    }

    public static void removeTimer(String uuid){
        timers.remove(uuid);
    }

    public static void clear(){
        for (Map.Entry<String, TimerEx> entry : timers.entrySet()) {
            entry.getValue().stopTimer();
        }
        timers.clear();
    }

}
