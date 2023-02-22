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
        Template.Component component = new Template.Component(Template.Component.ComponentType.BOOLEAN, "SomeName");
        template.components.add(component);
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
        Reminders.Reminder reminder = new Reminders.Reminder(UUID.randomUUID(), entries, new Date());
        Command.WriteCommand command = new Command.WriteCommand(reminder);
        System.out.println(new Gson().toJson(command));
    }

}