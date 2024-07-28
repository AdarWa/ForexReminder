package net.adarw.Utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.logging.Logger;

public class MiscUtils {
    private static Logger logger = Logger.getLogger(MiscUtils.class.getName());

    public enum OSType{
        LINUX,
        WINDOWS,
        MAC,
        OTHER
    }

    private static OSType detectedOS = null;

    public static void openBrowser(String url){
        if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                logger.severe("Exception while trying to open browser on Windows.");
            }
        }else{
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                logger.severe("Exception while trying to open browser on Linux.");
            }
        }
    }

    public static void openTextEditor(File file){
        try {
            if(Desktop.isDesktopSupported()){
                Desktop.getDesktop().open(file);
            }else if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                String cmd = "rundll32 url.dll,FileProtocolHandler " + file.getCanonicalPath();
                Runtime.getRuntime().exec(cmd);
            }
        }catch (Exception e){
            logger.severe("Exception while trying to open the text editor: " + e.getMessage());
        }
    }

    public static void openDirectory(File dir){
        if(Desktop.isDesktopSupported()){
            try {
                Desktop.getDesktop().open(dir);
            } catch (IOException e) {
                logger.severe("IOException while trying to open a directory in File Explorer: " + e.getMessage());
            }
        }else {
            logger.severe("The Desktop interface isn't supported on this machine!");
        }
    }

    public static OSType getOperatingSystemType() {
        if (detectedOS == null) {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
                detectedOS = OSType.MAC;
            } else if (OS.indexOf("win") >= 0) {
                detectedOS = OSType.WINDOWS;
            } else if (OS.indexOf("nux") >= 0) {
                detectedOS = OSType.LINUX;
            } else {
                detectedOS = OSType.OTHER;
            }
        }
        return detectedOS;
    }
}
