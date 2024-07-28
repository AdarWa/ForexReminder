package net.adarw.alertListner;

import net.adarw.Settings;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class SoundPlayer extends Thread{
    Logger logger = Logger.getLogger(SoundPlayer.class.getName());
    public Queue<String> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void run(){
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.severe("Sleep interrupted. " + e.getMessage());
            }
            while(!queue.isEmpty()){
                try {
                    String path = queue.poll();
                    String url = new File(path).toURI().toURL().toString();

                    Clip clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(new URL(url)));
                    clip.start();
                    while(clip.getMicrosecondLength() != clip.getMicrosecondPosition()) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            logger.severe("Sleep interrupted. " + e.getMessage());
                        }
                    }
                    try {
                        Thread.sleep((long) (1000* Settings.current.sequentialSoundInterval));
                    } catch (InterruptedException e) {
                        logger.severe("Sleep interrupted. " + e.getMessage());
                    }
                }catch (Exception e){
                    logger.severe("Exception while trying to play sound. " + e.getMessage());
                }
            }
        }
    }
}
