package net.adarw;

import net.adarw.Utils.KeyValuePair;
import net.adarw.Utils.StorageUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FxImporter {


    public static void importFx(String path, ArrayList<KeyValuePair<String, Template.Component>> componentMapping, Settings.ImportMapping importMapping) throws IOException, ParseException {
        Reader in = new FileReader(path);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader("Id", "Start", "Name", "Impact", "Currency")
                .withFirstRecordAsHeader()
                .parse(in);
        Reminders reminders = StorageUtils.getReminders();
        for (CSVRecord record : records) {

            ArrayList<KeyValuePair<Template.Component, Object>> entries = new ArrayList<>();
            for(KeyValuePair<String, Template.Component> entry : componentMapping){
                entries.add(new KeyValuePair<>(entry.getValue(), record.get(entry.getKey())));
            }

            SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = parser.parse(record.get(importMapping.date));
            date.setTime(date.getTime() + (1000*60*60)*(Settings.current.gmt + (Settings.current.daylightSaving ? 1 : 0)));
            Reminders.Reminder reminder = new Reminders.Reminder(
                    record.get(importMapping.id),
                    entries,
                    TimerEx.getString(date),
                    true
            );
            reminder.sound = Settings.current.alertDefaultSoundFile;

            reminders.reminders.add(reminder);
        }
        StorageUtils.writeReminders(reminders);
        in.close();
    }

}
