package net.adarw;

import net.adarw.Utils.KeyValuePair;
import net.adarw.Utils.StorageUtils;

import java.util.Comparator;

public class TemplateParser {
    public static String getReminderTable(){
        String str = "";
        Reminders reminders = StorageUtils.getReminders();
        reminders.reminders.sort(Comparator.comparing(o -> TimerEx.getDate(o.date)));
        for (Reminders.Reminder reminder : reminders.reminders){
            String reminderStr = "<tr class=\"reminder\" uuid=\""+reminder.uuid+"\"><td>";
            reminderStr += "<input class=\"form-check-input\" type=\"checkbox\" "+ (reminder.enabled ? "checked " : "")+"disabled></td><td>";
            reminderStr += reminder.date + "</td>";
            for(KeyValuePair<Template.Component, Object> pair : reminder.entries){
                if(pair.key.type == Template.Component.ComponentType.BOOLEAN){
                    reminderStr += "<td><input class=\"form-check-input\" type=\"checkbox\" "+ (Boolean.parseBoolean(pair.value.toString()) ? "checked " : "")+"disabled></td>";
                }else {
                    reminderStr += "<td>"+pair.value.toString()+"</td>";
                }
            }
            str += reminderStr + "</tr>";
        }
        return str;
    }

    public static String getTemplateTable(){
        String str = "<th>Enabled</th><th>Date</th>";
        Template template = StorageUtils.getTemplate();
        for(Template.Component comp : template.components){
            str += "<th>"+comp.name+"</th>";
        }
        return str;
    }

    public static String getControlTemplate(){
        String str = "";
        Template template = StorageUtils.getTemplate();
        for(Template.Component comp : template.components){
            if(comp.type == Template.Component.ComponentType.STRING){
                if(comp.choices.isEmpty()) {
                    String name = comp.name;
                    String id = name.replace(" ", "_");
                    str += "<div class=\"form-group\">\n" +
                            "            <label for=\"" + id + "\">" + name + "</label>\n" +
                            "            <input type=\"text\" class=\"form-control template-control\" id=\"" + id + "\" placeholder=\"Enter " + name + "\">\n" +
                            "          </div>";
                }else{
                    String name = comp.name;
                    String id = name.replace(" ", "_");
                    str += "<div class=\"form-group\" style=\"cursor: pointer;\">\n" +
                            "            <label for=\""+id+"\">"+name+"</label>\n" +
                            "            <select class=\"form-select\" id=\""+id+"\">\n";
                    for(String choice : comp.choices){
                        str += "<option value=\""+choice+"\">"+choice+"</option>\n";
                    }
                    str += "              </select>\n" +
                            "        </div>";
                }
            }else {
                String name = comp.name;
                String id = name.replace(" ", "_");
                str += "<div class=\"form-check\">\n" +
                        "            <input type=\"checkbox\" class=\"form-check-input\" id=\""+id+"\">\n" +
                        "            <label class=\"form-check-label\" for=\""+id+"\">"+name+"</label>\n" +
                        "        </div>";
            }
        }
        return str;
    }
}
