package net.adarw;

import net.adarw.Utils.KeyValuePair;
import org.joda.time.DateTime;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Reminders {
    public static class Reminder{
        public UUID uuid;
        public ArrayList<KeyValuePair<Template.Component, Object>> entries;
        public Date date;

        public Reminder(UUID uuid, ArrayList<KeyValuePair<Template.Component, Object>> entries, Date date){
            this.uuid = uuid;
            this.entries = entries;
            this.date = date;
        }
    }
    public ArrayList<Reminder> reminders = new ArrayList<>();
}
