package net.adarw;

import java.util.HashMap;
import java.util.Map;

public class TimerManager {
    public static HashMap<String,TimerEx> timers = new HashMap<>();

    private static TimerEx noCollision(TimerEx timer){
        for (Map.Entry<String, TimerEx> entry : timers.entrySet()) {
            if(entry.getValue().date.getTime() == timer.date.getTime()){
                timer.date.setTime(timer.date.getTime()+1000);
                return noCollision(timer);
            }
        }
        return timer;
    }

    public static void addTimer(String uuid,TimerEx timer){
        TimerEx t = noCollision(timer);
        timers.put(uuid,t);
        t.startTimer();
    }

    public static void removeTimer(String uuid){
        for (Map.Entry<String, TimerEx> entry : timers.entrySet()) {
            if(entry.getKey().equals(uuid)){
                entry.getValue().stopTimer();
            }
        }
        timers.remove(uuid);
    }

    public static void clear(){
        for (Map.Entry<String, TimerEx> entry : timers.entrySet()) {
            entry.getValue().stopTimer();
        }
        timers.clear();
    }

}
