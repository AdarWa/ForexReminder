package net.adarw.Utils;

import com.google.gson.Gson;
import net.adarw.Reminders;
import net.adarw.Settings;
import net.adarw.Template;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class StorageUtils {
    static Logger logger = Logger.getLogger(StorageUtils.class.getName());

    public static Path home = Paths.get(System.getProperty("user.home"));
    public static Path dataDir = Paths.get(home.toString(), "ForexReminderDaemon");
    public static Path template = Paths.get(dataDir.toString(), "template.json");
    public static Path reminders = Paths.get(dataDir.toString(), "reminders.json");
    public static Path settings = Paths.get(dataDir.toString(), "settings.yaml");
    public static Path logsDir = Paths.get(dataDir.toString(), "logs");
    public static Path soundsDir = Paths.get(dataDir.toString(), "sounds");

    public static void initStorage() {
        try {
            File jarFile = new File(StorageUtils.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            logger.info(jarFile.getAbsolutePath());
            if (!dataDir.toFile().exists())
                dataDir.toFile().mkdirs();

            if(!logsDir.toFile().exists())
                logsDir.toFile().mkdirs();

            if(!soundsDir.toFile().exists()){
                soundsDir.toFile().mkdirs();
                extractResourceFolder("sounds", soundsDir.toFile().getAbsolutePath());
            }

            if (!template.toFile().exists()){
                template.toFile().createNewFile();
                Template t = new Template();
                t.components.add(new Template.Component(Template.Component.ComponentType.STRING, "Subject"));
                t.components.add(new Template.Component(Template.Component.ComponentType.STRING, "Importance"));
                Template.Component currencyComponent = new Template.Component(Template.Component.ComponentType.STRING, "Currency");
                currencyComponent.choices.add("USD");
                currencyComponent.choices.add("EUR");
                currencyComponent.choices.add("GBP");
                t.components.add(currencyComponent);
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

    public static void createShortcut() throws IOException, InterruptedException {
        if(MiscUtils.getOperatingSystemType() != MiscUtils.OSType.WINDOWS){
            JOptionPane.showMessageDialog(null, "Adding to startup is not currently supported on linux platforms.\nFeel free to open an issue on Github so we can work on that!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Path of the running JAR
        File jarFile = new File(StorageUtils.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath());
        File parentDir = jarFile.getParentFile();

        // The EXE in the parent folder
        File exeFile = new File(parentDir, "ForexReminder.exe");
        if (!exeFile.exists()) {
            logger.severe("Executable not found: " + exeFile.getAbsolutePath());
            return;
        }

        // Windows Startup folder
        String startupFolder = System.getProperty("user.home") +
                "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        File shortcutFile = new File(startupFolder, "ForexReminder.lnk");

        // Temporary VBScript to create the shortcut
        File vbs = File.createTempFile("createShortcut", ".vbs");
        String vbsContent =
                "Set oWS = WScript.CreateObject(\"WScript.Shell\")\n" +
                        "sLinkFile = \"" + shortcutFile.getAbsolutePath() + "\"\n" +
                        "Set oLink = oWS.CreateShortcut(sLinkFile)\n" +
                        "oLink.TargetPath = \"" + exeFile.getAbsolutePath() + "\"\n" +
                        "oLink.WorkingDirectory = \"" + parentDir.getAbsolutePath() + "\"\n" +
                        "oLink.Save\n";

        try (FileWriter writer = new FileWriter(vbs)) {
            writer.write(vbsContent);
        }

        // Execute VBScript to create the shortcut
        Process p = new ProcessBuilder("wscript", vbs.getAbsolutePath()).start();
        p.waitFor();

        // Delete temporary script
        vbs.delete();

        logger.info("Shortcut created: " + shortcutFile.getAbsolutePath());
        JOptionPane.showMessageDialog(null, "Successfully added startup entry", "Success", JOptionPane.WARNING_MESSAGE);
    }

    public static void extractResourceFolder(String resourceFolder, String outputDir) throws Exception {
        ClassLoader cl = StorageUtils.class.getClassLoader();
        URL folderUrl = cl.getResource(resourceFolder);
        if (folderUrl == null) {
            throw new IllegalArgumentException("Resource folder not found: " + resourceFolder);
        }

        if (folderUrl.getProtocol().equals("file")) {
            // Running from IDE: resources are plain files on disk
            Path srcPath = Paths.get(folderUrl.toURI());
            Files.walk(srcPath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Path relative = srcPath.relativize(path);
                            Path dest = Paths.get(outputDir, relative.toString());
                            Files.createDirectories(dest.getParent());
                            Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                            logger.info("Copied: " + dest);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } else if (folderUrl.getProtocol().equals("jar")) {
            // Running from JAR: scan inside JAR entries
            String jarPath = folderUrl.getPath().substring(5, folderUrl.getPath().indexOf("!"));
            try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                jar.stream()
                        .filter(e -> e.getName().startsWith(resourceFolder + "/") && !e.isDirectory())
                        .forEach(entry -> {
                            try (InputStream in = jar.getInputStream(entry)) {
                                String relativePath = entry.getName().substring((resourceFolder + "/").length());
                                Path outFile = Paths.get(outputDir, relativePath);
                                Files.createDirectories(outFile.getParent());
                                Files.copy(in, outFile, StandardCopyOption.REPLACE_EXISTING);
                                logger.info("Extracted: " + outFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        } else {
            throw new UnsupportedOperationException("Unsupported resource protocol: " + folderUrl.getProtocol());
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

    public static Template getTemplate() {
        if (!isInitialized())
            return null;
        try {
            return new Gson().fromJson(new FileReader(template.toFile()), Template.class);
        } catch (Exception e) {
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
