package net.adarw.alertListner;

import net.adarw.Reminders;
import net.adarw.TimerEx;
import net.adarw.TimerManager;
import net.adarw.Utils.StorageUtils;

public class Listener extends Thread{

    @Override
    public void run(){
        while (true){
            while (!Thread.interrupted());
            Reminders reminders = StorageUtils.getReminders();
            if(reminders == null){
                System.err.println("Got thread interrupt but with no new data. might want to check it...");
                continue;
            }
            for(Reminders.Reminder reminder : reminders.reminders){
                TimerManager.addTimer(reminder.uuid.toString() ,new TimerEx(()->{
                    System.out.println("Triggered Timer " + reminder.uuid.toString() + " for " + reminder.date.toString());
                    TimerManager.removeTimer(reminder.uuid.toString());
                }, reminder.date));
            }
        }
    }
}
