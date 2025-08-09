package net.adarw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import net.adarw.Utils.KeyValuePair;
import net.adarw.Utils.MiscUtils;
import net.adarw.Utils.StorageUtils;

import java.awt.*;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

public class Settings {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SettingDescription {
        String name();
        String help();
    }

    @SettingDescription(name = "Alert Title", help = "The title shown at the top of each alert popup")
    public String alertTitle = "Alert";

    @SettingDescription(name = "Vertical Offset", help = "Pixels to offset alerts from bottom of screen")
    public double alertVerticalOffset = 0;

    @SettingDescription(name = "Heartbeat Interval (min)", help = "How often (in minutes) the app checks for updates")
    public double minutesPerHeartbeat = 1;

    @SettingDescription(name = "Show Confirmation on Delete", help = "Whether to show a dialog when deleting a reminder")
    public boolean showDialogOnDelete = true;

    @SettingDescription(name = "Open Browser on Start", help = "Whether to open the app's browser page at startup")
    public boolean openBrowserOnStart = true;

    @SettingDescription(name = "Show Tray Icon", help = "Whether to show a system tray icon")
    public boolean showTrayIcon = true;

    @SettingDescription(name = "Sound Interval", help = "Seconds between sequential sound plays")
    public double sequentialSoundInterval = 1;

    @SettingDescription(name = "Show Example Alert", help = "Show an example alert when the app starts")
    public boolean showExampleAlertOnStart = false;

    @SettingDescription(name = "Initial Sound Folder", help = "Default folder opened when selecting sounds")
    public String initialSoundFolder = System.getProperty("user.home");

    @SettingDescription(name = "Default Alert Sound", help = "Default sound file used for alerts if none is specified")
    public String alertDefaultSoundFile = "";

    @SettingDescription(name = "Page Reload Interval", help = "How often to reload web pages (in minutes)")
    public int pageReloadMinutesInterval = 2;

    @SettingDescription(name = "Alert Timeout", help = "How many seconds before an alert disappears automatically")
    public int secondsUntilAlertDisappear = 30;

    @SettingDescription(name = "Time Field Index", help = "Position of the time field in the alert component list")
    public int timeFieldPlacementInAlert = 1;

    @SettingDescription(name = "Description Field", help = "Which field should be treated as the main description")
    public String descFieldName = "Subject";

    @SettingDescription(name = "Wrap Text After N Characters", help = "Number of characters after which to break lines")
    public int charsToBreakAfter = 30;

    @SettingDescription(name = "Alert Background Color", help = "Hex color code used for alert background")
    public String alertBgColor = "#bed6c5";

    @SettingDescription(name = "Alert Opacity", help = "Opacity of the alert window from 0 to 100")
    public int alertOpacity = 80;

    @SettingDescription(name = "Time Zone (GMT offset)", help = "Your GMT offset without daylight saving")
    public int gmt = 2;

    @SettingDescription(name = "Daylight Saving", help = "Is daylight saving currently active?")
    public boolean daylightSaving = false;

    @SettingDescription(name = "Screen Size", help = "Width and height of your display (auto-set on startup)")
    public Dimension screenSize = new Dimension(0, 0);

    @SettingDescription(name = "Impact Field", help = "Which field name to color code for alert impact")
    public String ImpactFieldColoring = "Importance";

    @SettingDescription(name = "Remind Before (1)", help = "Time before event to send first reminder")
    public RemindBefore remindBefore1 = new RemindBefore(15, "");

    @SettingDescription(name = "Remind Before (2)", help = "Time before event to send second reminder")
    public RemindBefore remindBefore2 = new RemindBefore(5, "");

    @SettingDescription(name = "Import Mappings", help = "Mappings used when importing reminders from CSV or external systems")
    public ImportMappings importMappings = new ImportMappings();

    private static Logger logger = Logger.getLogger(Settings.class.getName());

    public Settings(){
        importMappings.templateMappings.add(new TemplateImportMapping("Importance", "Impact"));
        importMappings.templateMappings.add(new TemplateImportMapping("Subject", "Name"));
        importMappings.templateMappings.add(new TemplateImportMapping("Currency", "Currency"));

        if(MiscUtils.getOperatingSystemType() == MiscUtils.OSType.WINDOWS){
            Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
            screenSize = scrnSize;
            Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            alertVerticalOffset = scrnSize.height - winSize.height;
        }
    }

    public ArrayList<KeyValuePair<String, Template.Component>> generateComponentMappings() {
        ArrayList<KeyValuePair<String, Template.Component>> map = new ArrayList<>();
        for(TemplateImportMapping mapping : importMappings.templateMappings){
            map.add(new KeyValuePair<>(mapping.csvName, new Template.Component(Template.Component.ComponentType.STRING,mapping.templateName)));
        }
        return map;
    }

    public static Settings current;

    public static Settings deserialize(String jsonConfig) throws JsonProcessingException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Settings.class, new ValueOnlyDeserializer());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        return mapper.readValue(jsonConfig, Settings.class);
    }

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

    public String serializeToJson() throws IllegalAccessException, JsonProcessingException {
        ObjectNode obj = SettingsSerializer.serializeWithDescriptions(this);
        return new ObjectMapper().writeValueAsString(obj);
    }

    public static class SettingsSerializer {
        public static ObjectNode serializeWithDescriptions(Object obj) throws IllegalAccessException {
            return serializeObject(obj.getClass(), obj);
        }

        private static ObjectNode serializeObject(Class<?> clazz, Object instance) throws IllegalAccessException {
            ObjectNode node = JsonNodeFactory.instance.objectNode();

            for (Field field : clazz.getFields()) {
                field.setAccessible(true);
                Object value = field.get(instance);

                ObjectNode fieldNode = JsonNodeFactory.instance.objectNode();

                if (field.isAnnotationPresent(SettingDescription.class)) {
                    SettingDescription desc = field.getAnnotation(SettingDescription.class);
                    fieldNode.put("name", desc.name());
                    fieldNode.put("help", desc.help());
                }
                JsonNode valueNode = serializeValue(value);
                if(valueNode == null)
                    continue;
                fieldNode.set("value", valueNode);
                node.set(field.getName(), fieldNode);
            }

            return node;
        }

        private static JsonNode serializeValue(Object value) throws IllegalAccessException {
            if (value == null) {
                return NullNode.instance;
            }

            Class<?> valueClass = value.getClass();

            if(valueClass == Settings.class)
                return null;

            if(Number.class.isAssignableFrom(valueClass)){
                return new DoubleNode(((Number) value).doubleValue());
            }

            if(Boolean.class.isAssignableFrom(valueClass)){
                return BooleanNode.valueOf((boolean) value);
            }

            if (isSimple(valueClass)) {
                return new TextNode(value.toString());
            }

            if (value instanceof Map<?, ?> map) {
                ObjectNode mapNode = JsonNodeFactory.instance.objectNode();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    mapNode.set(String.valueOf(entry.getKey()), serializeValue(entry.getValue()));
                }
                return mapNode;
            }

            if (value instanceof Collection<?> col) {
                ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
                for (Object item : col) {
                    arrayNode.add(serializeValue(item));
                }
                return arrayNode;
            }

            if (valueClass.isArray()) {
                ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
                int length = java.lang.reflect.Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    Object item = java.lang.reflect.Array.get(value, i);
                    arrayNode.add(serializeValue(item));
                }
                return arrayNode;
            }

            return serializeObject(valueClass, value);
        }

        private static boolean isSimple(Class<?> type) {
            return type.isPrimitive()
                    || type == String.class
                    || type.isEnum();
        }
    }

    public static class RemindBefore{
        @SettingDescription(name = "Minutes Before Alert", help = "How many minutes before the main alert this should trigger")
        public double minutesBeforeAlert;

        @SettingDescription(name = "Sound Path", help = "Path to the sound file for this reminder")
        public String soundPath;

        @SettingDescription(name = "Early Alert Title", help = "Title used for this early alert")
        public String alertTitle = "Early Alert";

        @SettingDescription(name = "Duration", help = "Seconds until this early alert disappears")
        public int secondsUntilAlertDisappear = 30;

        @SettingDescription(name = "Enabled", help = "Whether this early reminder is active")
        public boolean enabled = true;

        public RemindBefore(double minutesBeforeAlert, String soundPath){
            this.minutesBeforeAlert = minutesBeforeAlert;
            this.soundPath = soundPath;
        }
        public RemindBefore(){

        }
    }
    public static class TemplateImportMapping {
        @SettingDescription(name = "Template Name", help = "Name of the template to match when importing")
        public String templateName = "";

        @SettingDescription(name = "CSV Column Name", help = "Corresponding column name in the CSV file")
        public String csvName = "";

        public TemplateImportMapping(String templateName, String csvName){
            this.templateName = templateName;
            this.csvName = csvName;
        }
        public TemplateImportMapping(){

        }

        @Override
        public String toString() {
            return "TemplateImportMapping{" +
                    "templateName='" + templateName + '\'' +
                    ", csvName='" + csvName + '\'' +
                    '}';
        }
    }

    public static class ImportMapping {
        @SettingDescription(name = "Date Field", help = "CSV column that contains the date/time")
        public String date = "Start";

        @SettingDescription(name = "ID Field", help = "CSV column that contains a unique identifier")
        public String id = "Id";

        public ImportMapping(String date, String id) {
            this.date = date;
            this.id = id;
        }

        public ImportMapping(){

        }

        @Override
        public String toString() {
            return "ImportMapping{" +
                    "date='" + date + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

    public static class ImportMappings {
        @SettingDescription(name = "Template Mappings", help = "List of field-to-column mappings for each template")
        public ArrayList<TemplateImportMapping> templateMappings = new ArrayList<>();

        @SettingDescription(name = "Default Mapping", help = "Default field-to-column mapping used if no template matches")
        public ImportMapping mapping = new ImportMapping();

        public ImportMappings(){

        }
    }
}
