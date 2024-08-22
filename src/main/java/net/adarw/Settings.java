package net.adarw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import net.adarw.Utils.KeyValuePair;
import net.adarw.Utils.MiscUtils;
import net.adarw.Utils.StorageUtils;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Settings {
    public String alertTitle = "Alert";
    public double alertVerticalOffset = 0;
    public double minutesPerHeartbeat = 1;
    public boolean showDialogOnDelete = true;
    public boolean openBrowserOnStart = true;
    public boolean showTrayIcon = true;
    public double sequentialSoundInterval = 1;
    public boolean showExampleAlertOnStart = false;
    public String initialSoundFolder = System.getProperty("user.home");
    public String alertDefaultSoundFile = "";
    public int secondsUntilAlertDisappear = 30;
    public int timeFieldPlacementInAlert = 1;
    public String descFieldName = "Subject";
    public int charsToBreakAfter = 30;
    public String alertBgColor = "#bed6c5";
    public int alertOpacity = 80;
    public int gmt = 2;
    public boolean daylightSaving = false;
    public Dimension screenSize = new Dimension(0,0);
    public String ImpactFieldColoring = "Importance";
    public RemindBefore remindBefore1 = new RemindBefore(15, "");
    public RemindBefore remindBefore2 = new RemindBefore(5, "");
    public ArrayList<ImportMapping> importMappings = new ArrayList<>();

    public Settings(){
        importMappings.add(new ImportMapping("Importance", "Level"));
        if(MiscUtils.getOperatingSystemType() == MiscUtils.OSType.WINDOWS){
            Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
            screenSize = scrnSize;
            Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            alertVerticalOffset = scrnSize.height - winSize.height;
        }
    }

    public ArrayList<KeyValuePair<String, Template.Component>> generateComponentMappings() {
        ArrayList<KeyValuePair<String, Template.Component>> map = new ArrayList<>();
        for(ImportMapping mapping : importMappings){
            map.add(new KeyValuePair<>(mapping.csvName, new Template.Component(Template.Component.ComponentType.STRING,mapping.templateName)));
        }
        return map;
    }

    public static Settings current;

    public static class SettingsManager{
        private static final ObjectMapper mapper;
        private static final Logger logger = Logger.getLogger(SettingsManager.class.getName());
        static {
            YAMLFactory factory = new YAMLFactory()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .disable(YAMLGenerator.Feature.SPLIT_LINES)
                    .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
            mapper = new ObjectMapper(factory);
        }

        public static void writeSettings(Settings settings){
            try {
                mapper.writeValue(StorageUtils.settings.toFile(), settings);
            } catch (IOException e) {
                logger.severe("Failed to write to settings file: " + e.getMessage());
            }
        }

        public static String getSettingsString(Settings settings){
            try {
                return mapper.writeValueAsString(settings);
            } catch (IOException e) {
                logger.severe("Failed to write to settings file: " + e.getMessage());
                return "";
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
    public static class RemindBefore{
        public double minutesBeforeAlert;
        public String alertTitle = "Early Alert";
        public int secondsUntilAlertDisappear = 30;
        public String soundPath;
        public boolean enabled = true;

        public RemindBefore(double minutesBeforeAlert, String soundPath){
            this.minutesBeforeAlert = minutesBeforeAlert;
            this.soundPath = soundPath;
        }
        public RemindBefore(){

        }
    }
    public static class ImportMapping{
        public String templateName = "";
        public String csvName = "";

        public ImportMapping(String templateName, String csvName){
            this.templateName = templateName;
            this.csvName = csvName;
        }
        public ImportMapping(){

        }
    }
}
