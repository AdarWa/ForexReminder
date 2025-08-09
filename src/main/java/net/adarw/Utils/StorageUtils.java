package net.adarw.Utils;

import com.google.gson.Gson;
import net.adarw.Reminders;
import net.adarw.Settings;
import net.adarw.Template;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class StorageUtils {
    static Logger logger = Logger.getLogger(StorageUtils.class.getName());

    public static Path home = Paths.get(System.getProperty("user.dir"));
    public static Path dataDir = Paths.get(home.toString(), "ForexReminderDaemon");
    public static Path template = Paths.get(dataDir.toString(), "template.json");
    public static Path reminders = Paths.get(dataDir.toString(), "reminders.json");
    public static Path settings = Paths.get(dataDir.toString(), "settings.yaml");
    public static Path logsDir = Paths.get(dataDir.toString(), "logs");

    public static void initStorage() {
        try {
            if (!dataDir.toFile().exists())
                dataDir.toFile().mkdirs();

            if(!logsDir.toFile().exists())
                logsDir.toFile().mkdirs();

            if (!template.toFile().exists()){
                template.toFile().createNewFile();
                Template t = new Template();
                t.components.add(new Template.Component(Template.Component.ComponentType.STRING, "Subject"));
                t.components.add(new Template.Component(Template.Component.ComponentType.STRING, "Importance"));
                t.components.add(new Template.Component(Template.Component.ComponentType.STRING, "Currency"));
                setTemplate(t);
            }

            if (!reminders.toFile().exists()){
                reminders.toFile().createNewFile();
                writeReminders(new Reminders());
            }

            if(!settings.toFile().exists()){
                settings.toFile().createNewFile();
                Settings.SettingsManager.writeSettings(new Settings());
            }

        }catch (Exception e){
            logger.severe(e.getClass().getSimpleName() + " while initializing storage: " + e.getMessage());
        }
    }

    public static boolean isInitialized(){
        return dataDir.toFile().exists() && template.toFile().exists() && reminders.toFile().exists();
    }

    public static void setTemplate(Template t){
        String json = new Gson().toJson(t);
        FileWriter w;
        try {
            w = new FileWriter(template.toFile());
            w.write(json);
            w.close();
        } catch (IOException e) {
            logger.severe(e.getClass().getSimpleName() + " while writing template file: " + e.getMessage());
        }
    }

    public static void writeReminders(Reminders r){
        String json = new Gson().toJson(r);
        FileWriter w;
        try {
            w = new FileWriter(reminders.toFile());
            w.write(json);
            w.close();
        } catch (IOException e) {
            logger.severe(e.getClass().getSimpleName() + " while writing reminder file: " + e.getMessage());
        }
    }

    public static Reminders getReminders() {
        if(!isInitialized())
            return null;
        try{
            return new Gson().fromJson(new FileReader(reminders.toFile()), Reminders.class);
        }catch (Exception e){
            logger.severe(e.getClass().getSimpleName() + " while reading reminder files: " + e.getMessage());
            return null;
        }
    }

    public static Template getTemplate(){
        if(!isInitialized())
            return null;
        try{
            return new Gson().fromJson(new FileReader(template.toFile()), Template.class);
        }catch (Exception e){
            logger.severe(e.getClass().getSimpleName() + " while reading template files: " + e.getMessage());
            return null;
        }
    }

    public static void clearReminders(){
        writeReminders(new Reminders());
        logger.info("Successfully cleared all reminders!");
    }
    public static void clearTemplate(){
        if(template.toFile().delete()){
            try {
                if(template.toFile().createNewFile()){
                    setTemplate(new Template());
                    logger.info("Successfully cleared template!");
                }else{
                    logger.severe("Had a problem while trying to clear template.");
                }
            } catch (IOException e) {
                logger.severe("Had a problem while trying to clear template: " + e.getMessage());
            }
        }else {
            logger.severe("Had a problem while trying to clear template.");
        }
    }

    public static void deleteReminder(String uuid){
        Reminders rems = getReminders();
        if(rems == null){
            logger.severe("Tried to delete reminder with uuid " + uuid + " but there are no reminders.");
            return;
        }
        for(Reminders.Reminder rem : rems.reminders){
            if(rem.uuid.equals(uuid)){
                rems.reminders.remove(rem);
                break;
            }
        }
        writeReminders(rems);
        logger.info("Successfully removed reminder with uuid " + uuid);
    }

}
