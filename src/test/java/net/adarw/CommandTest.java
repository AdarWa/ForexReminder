package net.adarw;

import com.google.gson.Gson;
import net.adarw.Utils.KeyValuePair;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.rmi.server.UID;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {
    
    @Test
    void test(){
        Template template = new Template();
        Template.Component name = new Template.Component(Template.Component.ComponentType.STRING, "AlertName");
        Template.Component impact = new Template.Component(Template.Component.ComponentType.STRING, "Importance");
        Template.Component currency = new Template.Component(Template.Component.ComponentType.STRING, "Curr");
        template.components.add(name);
        template.components.add(impact);
        template.components.add(currency);
        Command.InitializeCommand command =new Command.InitializeCommand(template);
        System.out.println(new Gson().toJson(command));

    }

    @Test
    void test2(){
        Template.Component component = new Template.Component(Template.Component.ComponentType.BOOLEAN, "SomeName");
        Template.Component component1 = new Template.Component(Template.Component.ComponentType.STRING, "SomeName2");
        ArrayList<KeyValuePair<Template.Component, Object>> entries = new ArrayList<>();
        entries.add(new KeyValuePair<Template.Component,Object>(component, true));
        entries.add(new KeyValuePair<Template.Component,Object>(component1, "Hello"));
        Reminders.Reminder reminder = new Reminders.Reminder(UUID.randomUUID().toString(), entries, new Date().toString(), true);
        Command.WriteCommand command = new Command.WriteCommand(reminder);
        System.out.println(new Gson().toJson(command));
    }

    @Test
    void test3(){
        Template.Component name = new Template.Component(Template.Component.ComponentType.BOOLEAN, "AlertName");
        Template.Component impact = new Template.Component(Template.Component.ComponentType.BOOLEAN, "Importance");
        Template.Component currency = new Template.Component(Template.Component.ComponentType.STRING, "Curr");
        ArrayList<KeyValuePair<String,Template.Component>> entries = new ArrayList<>();
        entries.add(new KeyValuePair<>("Name", name));
        entries.add(new KeyValuePair<>("Impact", impact));
        entries.add(new KeyValuePair<>("Currency", currency));
        Command.ImportFXCommand command = new Command.ImportFXCommand("/home/adarw/Downloads/calendar-event-list.csv");
        System.out.println(new Gson().toJson(command));
    }

    @Test
    void serializeReminder(){
//        Template.Component component = new Template.Component(Template.Component.ComponentType.STRING, "AlertName");
//        Template.Component component1 = new Template.Component(Template.Component.ComponentType.STRING, "Importance");
//        Template.Component component2 = new Template.Component(Template.Component.ComponentType.STRING, "Curr");
//        ArrayList<KeyValuePair<Template.Component, Object>> entries = new ArrayList<>();
//        entries.add(new KeyValuePair<>(component, "Test Alert"));
//        entries.add(new KeyValuePair<>(component1, "LOW"));
//        entries.add(new KeyValuePair<>(component2, "ILS"));
//        Reminders.Reminder reminder = new Reminders.Reminder(UUID.randomUUID(), entries, new Date("Jul 22, 2024, 2:06:05 PM"), true);
//        Command.WriteCommand command = new Command.WriteCommand(reminder);
//        System.out.println(new Gson().toJson(command));
//        System.out.println(DateTime.parse("17-04-2024 15:08").toString());
        System.out.println(TimerEx.getString(new Date()));
    }

}