package net.adarw.alertListner;

import net.adarw.*;
import net.adarw.Utils.StorageUtils;
import net.adarw.alertListner.gui.Alert;

import java.util.Date;
import java.util.logging.Logger;

public class Listener extends Thread{
    Logger logger = Logger.getLogger(Listener.class.getName());
    public static boolean working = false;

    @Override
    public void run(){
        while (true){
            while (!Thread.interrupted()){
                try
                {
                    Thread.sleep(50);
                }
                catch (Exception e)
                {
                    break;
                }
            }
            working = true;
            Reminders reminders = StorageUtils.getReminders();
            if(reminders == null){
                logger.severe("Got thread interrupt but with no new data.");
                working = false;
                continue;
            }
            TimerManager.clear();
            for(Reminders.Reminder reminder : reminders.reminders){
                if(!reminder.enabled){
                    continue;
                }
                if(TimerEx.getDate(reminder.date).before(new Date())){
                    reminders.reminders.get(reminders.reminders.indexOf(reminder)).enabled = false;
                    StorageUtils.writeReminders(reminders);
                    logger.info("Disabled overdue timer with id " + reminder.uuid);
                    continue;
                }
                TimerManager.addTimer(reminder.uuid,new TimerEx(()->{
                    logger.info("Triggered Timer " + reminder.uuid + " for " + reminder.date);
                    new Alert(reminder);
                    TimerManager.removeTimer(reminder.uuid);
                }, TimerEx.getDate(reminder.date)));
                if(Settings.current.remindBefore2.enabled){
                    Date date = TimerEx.getDate(reminder.date);
                    date.setTime((long) (date.getTime()- Settings.current.remindBefore2.minutesBeforeAlert*60000));
                    if(!date.before(new Date())){
                        TimerManager.addTimer(reminder.uuid, new TimerEx(()->{
                            new Alert(reminder,true, Settings.current.remindBefore2.soundPath);
                            logger.info("Triggered Remind Before Timer " + reminder.uuid + " at " + TimerEx.getString(date));
                            Main.getInstance().listener.interrupt();
                        }, date));
                    }
                }

                if(Settings.current.remindBefore1.enabled){
                    Date date = TimerEx.getDate(reminder.date);
                    date.setTime((long) (date.getTime()- Settings.current.remindBefore1.minutesBeforeAlert*60000));
                    if(!date.before(new Date())){
                        TimerManager.addTimer(reminder.uuid, new TimerEx(()->{
                            new Alert(reminder,true, Settings.current.remindBefore1.soundPath);
                            logger.info("Triggered Remind Before Timer " + reminder.uuid + " at " + TimerEx.getString(date));
                            Main.getInstance().listener.interrupt();
                        }, date));
                    }
                }
            }
            working = false;
        }
    }
}
