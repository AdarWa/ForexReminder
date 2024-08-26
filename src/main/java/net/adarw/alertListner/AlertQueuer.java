package net.adarw.alertListner;

import net.adarw.Settings;
import net.adarw.alertListner.gui.Alert;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class AlertQueuer extends Thread{
    Logger logger = Logger.getLogger(AlertQueuer.class.getName());
    public static Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void run(){
        while(true){
            while(!queue.isEmpty()){
                try {
                    Runnable func = queue.poll();
                    Alert.working = true;
                    func.run();
                    while (Alert.working);
                }catch (Exception e){
                    logger.severe("Exception in AlertQueuer. " + e.getMessage());
                }
            }
        }
    }
}
