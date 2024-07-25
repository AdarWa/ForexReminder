package net.adarw;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class TimerEx {

    private Timer timer;
    public Runnable func;
    public Date date;

    public TimerEx(Runnable func, DateTime date){
        timer = new Timer();
        this.func = func;
        this.date = date.toDate();
    }

    public TimerEx(Runnable func, Date date){
        timer = new Timer();
        this.func = func;
        this.date = date;
    }


    public void startTimer(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                func.run();
            }
        }, date);
    }

    public void stopTimer(){
        timer.cancel();
    }

    public static Date getDate(String date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            Logger.getLogger(TimerEx.class.getName()).severe("Error while trying to parse date \"" + date + "\".\n" +
                    "Error message is: "+e.getMessage());
            return new Date();
        }
    }

    public static String getString(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        return formatter.format(date);
    }

}
