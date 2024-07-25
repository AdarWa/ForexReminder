package net.adarw;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.Gson;
import net.adarw.Utils.MessageUtils;
import net.adarw.Utils.MiscUtils;
import net.adarw.Utils.StorageUtils;
import net.adarw.alertListner.Listener;
import net.adarw.alertListner.SoundPlayer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Logger;

public class Main implements MainInterface{
    Logger logger = Logger.getLogger(Main.class.getName());
    Command.CommandParser parser;
    Listener listener;
    public SoundPlayer player;
    private static Main instance;

    public static Main getInstance(){
        return instance;
    }



    public Main() {
        StorageUtils.initStorage();
        try {
            File file = Paths.get(StorageUtils.logsDir.toString(), TimerEx.getString(new Date()).split(" ")[0] + ".log").toFile();
            if(!file.exists()) file.createNewFile();
            TeeOutputStream stream = new TeeOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)), System.out);
            PrintStream ps = new PrintStream(stream);
            System.setErr(ps);
        } catch (IOException e) {
            logger.severe("Exception while trying to setup logs: " + e.getMessage());
        }
        instance = this;
        logger.info("Initializing Server...");
        try {
            UIManager.setLookAndFeel( new FlatLightLaf());
        } catch( Exception ex ) {
            logger.severe( "Failed to initialize LaF");
        }
        Settings.current = Settings.SettingsManager.readSettings();
        Settings.SettingsManager.writeSettings(Settings.current);
        Server server = new Server(this);
        parser = new Command.CommandParser();
        listener = new Listener();
        player = new SoundPlayer();

        player.start();
        listener.start();

        logger.info("Server is active in port 8579!");
        if(Settings.current.openBrowserOnStart)
            MiscUtils.openBrowser("http://localhost:8579");
        if(Settings.current.showTrayIcon) {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/accept.png"));
                ActionListener listener = e -> MiscUtils.openBrowser("http://localhost:8579");
                TrayIcon trayIcon = new TrayIcon(image, "Super Reminder");
                trayIcon.addActionListener(listener);
                try {
                    tray.add(trayIcon);
                } catch (AWTException e) {
                    logger.severe("Exception while trying to add icon to tray: " + e.getMessage());
                }
            } else {
                logger.severe("System tray isn't supported on this machine.");
            }
        }
    }

    public static void main(String[] args) {

        new Main();
    }

    @Override
    public String OnCommand(String msg) {
        try{
            Command command = parser.parseCommand(msg);
            if(command.getType() == Command.CommandType.INIT){
                Command.InitializeCommand cmd = (Command.InitializeCommand)command;
                StorageUtils.setTemplate(cmd.template);
                return MessageUtils.getSuccessMessage();
            }else if(command.getType() == Command.CommandType.WRITE){
                Command.WriteCommand cmd = (Command.WriteCommand) command;
                Reminders reminders = StorageUtils.getReminders();
                if(reminders == null){
                    reminders = new Reminders();
                }
                reminders.reminders.add(cmd.reminder);
                StorageUtils.writeReminders(reminders);
                listener.interrupt();
                return MessageUtils.getSuccessMessage();
            }else if(command.getType() == Command.CommandType.IMPORTFX){
                Command.ImportFXCommand cmd = (Command.ImportFXCommand) command;
                FxImporter.importFx(cmd.path, cmd.componentMapping);
                listener.interrupt();
                return MessageUtils.getSuccessMessage();
            }else if(command.getType() == Command.CommandType.READ){
                if(!StorageUtils.reminders.toFile().exists())
                    return MessageUtils.getMessage(4, "File doesn't exist.");
                return MessageUtils.getMessage(2, new JSONObject(new Gson().toJson(StorageUtils.getReminders())));
            }else if(command.getType() == Command.CommandType.READTEMPLATE) {
                if(!StorageUtils.template.toFile().exists())
                    return MessageUtils.getMessage(4, "File doesn't exist.");
                logger.info(new Gson().toJson(StorageUtils.getTemplate()));
                return MessageUtils.getMessage(2, new JSONObject(new Gson().toJson(StorageUtils.getTemplate())));
            }else if(command.getType() == Command.CommandType.CLEAR){
                StorageUtils.clearReminders();
                return MessageUtils.getSuccessMessage();
            }else if(command.getType() == Command.CommandType.CLEARTEMPLATE){
                StorageUtils.clearTemplate();
                return MessageUtils.getSuccessMessage();
            }else if(command.getType() == Command.CommandType.SETTEMPLATE){
                Command.SetTemplateCommand cmd = (Command.SetTemplateCommand) command;
                StorageUtils.setTemplate(cmd.template);
                return MessageUtils.getSuccessMessage();
            }else if(command.getType() == Command.CommandType.DELETE){
                Command.DeleteCommand cmd = (Command.DeleteCommand)command;
                StorageUtils.deleteReminder(cmd.uuid);
                Main.getInstance().listener.interrupt();
                return MessageUtils.getSuccessMessage();
            }else if(command.getType() == Command.CommandType.CHOOSEFILE){
                final JFrame frame = new JFrame();
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.requestFocus();
                FileFilter filter = new FileNameExtensionFilter("WAV File","wav");
                final JFileChooser fc = new JFileChooser(Settings.current.initialSoundFolder);
                fc.setFileFilter(filter);
                int res = fc.showOpenDialog(frame);
                if(res == JFileChooser.APPROVE_OPTION){
                    return MessageUtils.getMessage(2, fc.getSelectedFile().getAbsolutePath());
                }else {
                    return MessageUtils.getSuccessMessage();
                }
            }else if(command.getType() == Command.CommandType.HEARTBEAT){
                return MessageUtils.getSuccessMessage();
            }else if(command.getType() == Command.CommandType.SETTINGS){
                Command.SettingsCommand cmd = (Command.SettingsCommand) command;
                String operation = cmd.operation;
                if(operation.equals("reload")){
                    Settings.current = Settings.SettingsManager.readSettings();
                }else if(operation.equals("open")){
                    MiscUtils.openTextEditor(StorageUtils.settings.toFile());
                }else if(operation.equals("openTemplate")){
                    MiscUtils.openTextEditor(StorageUtils.template.toFile());
                } else if(operation.equals("quit")){
                    System.exit(0);
                    return MessageUtils.getSuccessMessage();
                }else if(operation.equals("default")){
                    FileUtils.deleteDirectory(StorageUtils.dataDir.toFile());
                    System.exit(0);
                    return MessageUtils.getSuccessMessage();
                }else if(operation.equals("openDir")){
                    MiscUtils.openDirectory(StorageUtils.dataDir.toFile());
                    return MessageUtils.getSuccessMessage();
                }else{
                    return MessageUtils.getMessage(3, "Invalid settings operation: " + operation);
                }
                return MessageUtils.getSuccessMessage();
            }
        }catch (Exception e){
            String err = e.getClass().getSimpleName() + " while in main loop: " + e.getMessage();
            logger.severe(err);
            return MessageUtils.getMessage(3, "ERROR: " + err);
        }
        return MessageUtils.getMessage(4, "Unknown Command.");
    }
}