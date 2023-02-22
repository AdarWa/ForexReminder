package net.adarw.Utils;

import com.google.gson.Gson;
import net.adarw.Reminders;
import net.adarw.Template;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class StorageUtils {

    private static Path home = Paths.get(System.getProperty("user.home"));
    private static Path dataDir = Paths.get(home.toString(), "SuperReminderDaemon");
    private static Path template = Paths.get(dataDir.toString(), "template.json");
    private static Path reminders = Paths.get(dataDir.toString(), "reminders.json");

    public static void initStorage() {
        try {
            if (!dataDir.toFile().exists())
                dataDir.toFile().mkdirs();

            if (!template.toFile().exists())
                template.toFile().createNewFile();

            if (!reminders.toFile().exists())
                reminders.toFile().createNewFile();

        }catch (Exception e){
            System.out.println(e.getClass().getSimpleName() + " while initializing storage: " + e.getMessage());
        }
    }

    public static boolean isInitialized(){
        return dataDir.toFile().exists() && template.toFile().exists() && reminders.toFile().exists();
    }

    public static void setTemplate(Template t){
        if(!isInitialized())
            initStorage();

        String json = new Gson().toJson(t);
        FileWriter w;
        try {
            w = new FileWriter(template.toFile());
            w.write(json);
            w.close();
        } catch (IOException e) {
            System.err.println(e.getClass().getSimpleName() + " while writing template file: " + e.getMessage());
        }
    }

    public static void writeReminders(Reminders r){
        if(!isInitialized())
            initStorage();

        String json = new Gson().toJson(r);
        FileWriter w;
        try {
            w = new FileWriter(reminders.toFile());
            w.write(json);
            w.close();
        } catch (IOException e) {
            System.err.println(e.getClass().getSimpleName() + " while writing reminder file: " + e.getMessage());
        }
    }

    public static Reminders getReminders() {
        if(!isInitialized())
            return null;
        try{
            return new Gson().fromJson(new FileReader(reminders.toFile()), Reminders.class);
        }catch (Exception e){
            System.err.println(e.getClass().getSimpleName() + " while reading reminder files: " + e.getMessage());
            return null;
        }
    }

}
