package net.adarw;

import net.adarw.Utils.KeyValuePair;
import org.joda.time.DateTime;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Reminders {
    public static class Reminder{
        public String uuid;
        public ArrayList<KeyValuePair<Template.Component, Object>> entries;
        public boolean enabled;
        public String date;
        public String sound;

        public Reminder(String uuid, ArrayList<KeyValuePair<Template.Component, Object>> entries, String date, boolean enabled){
            this.uuid = uuid;
            this.entries = entries;
            this.date = date;
            this.enabled = enabled;
        }
    }
    public ArrayList<Reminder> reminders = new ArrayList<>();
}
