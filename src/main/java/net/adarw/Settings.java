package net.adarw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import net.adarw.Utils.StorageUtils;

import java.io.IOException;
import java.util.logging.Logger;

public class Settings {

    public String alertTitle = "Alert";
    public double minutesPerHeartbeat = 1;
    public boolean showDialogOnDelete = true;
    public boolean openBrowserOnStart = true;
    public boolean showTrayIcon = true;
    public double secondsAfterClip = 1;
    public String initialSoundFolder = System.getProperty("user.home");

    public static Settings current;

    public static class SettingsManager{
        private static final ObjectMapper mapper;
        private static final Logger logger = Logger.getLogger(SettingsManager.class.getName());
        static {
            mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        }

        public static void writeSettings(Settings settings){
            try {
                mapper.writeValue(StorageUtils.settings.toFile(), settings);
            } catch (IOException e) {
                logger.severe("Failed to write to settings file: " + e.getMessage());
            }
        }

        public static Settings readSettings(){
            try {
                return mapper.readValue(StorageUtils.settings.toFile(), Settings.class);
            } catch (IOException e) {
                logger.severe("Failed to read settings file: " + e.getMessage());
                return new Settings();
            }
        }
    }
}
