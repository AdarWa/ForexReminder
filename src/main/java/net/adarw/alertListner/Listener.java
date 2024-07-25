package net.adarw.alertListner;

import net.adarw.*;
import net.adarw.Utils.StorageUtils;
import net.adarw.alertListner.gui.Alert;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

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
                TimerManager.addTimer(reminder.uuid.toString() ,new TimerEx(()->{
                    logger.info("Triggered Timer " + reminder.uuid.toString() + " for " + reminder.date);
                    new Alert(reminder);
                    TimerManager.removeTimer(reminder.uuid.toString());
                }, TimerEx.getDate(reminder.date)));
            }
            working = false;
        }
    }
}
