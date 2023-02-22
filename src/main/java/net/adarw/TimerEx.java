package net.adarw;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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



}
