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

    private static String[] flattenComponentMapping(ArrayList<KeyValuePair<String, Template.Component>> componentMapping){
        ArrayList<String> newMapping = new ArrayList<>();
        for (KeyValuePair<String, Template.Component> mapping : componentMapping){
            newMapping.add(mapping.key);
        }
        String[] arr = new String[newMapping.size()];
        arr = newMapping.toArray(arr);
        return arr;
    }

    public static void importFx(String path, ArrayList<KeyValuePair<String, Template.Component>> componentMapping, Settings.ImportMapping importMapping) throws IOException, ParseException {
        Reader in = new FileReader(path);

        // Doing some shit to merge the arrays.
        String[] flattenedComponentMapping = flattenComponentMapping(componentMapping);
        String[] importMappingArr = {importMapping.id, importMapping.date};
        String[] headerMapping = new String[importMappingArr.length+flattenedComponentMapping.length];
        System.arraycopy(importMappingArr, 0, headerMapping, 0, importMappingArr.length);
        System.arraycopy(flattenedComponentMapping, 0, headerMapping, importMappingArr.length, flattenedComponentMapping.length);

        Iterable<CSVRecord> records = CSVFormat.DEFAULT
                .withHeader(headerMapping)
                .withFirstRecordAsHeader()
                .parse(in);
        Reminders reminders = StorageUtils.getReminders();
        for (CSVRecord record : records) {

            ArrayList<KeyValuePair<Template.Component, Object>> entries = new ArrayList<>();
            for(KeyValuePair<String, Template.Component> entry : componentMapping){
                entries.add(new KeyValuePair<>(entry.getValue(), record.get(entry.getKey())));
            }

            SimpleDateFormat parser = new SimpleDateFormat(Settings.current.importDateFormat);
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
