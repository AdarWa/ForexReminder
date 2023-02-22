package net.adarw;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.adarw.Utils.JsonUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.stream.StreamSupport;

public interface Command {

    enum CommandType{
        WRITE(WriteCommand.class),
        READ(null),
        INIT(InitializeCommand.class),
        EDIT(null);

        private Class<?> commandClass;

        public Class<?> getCommandClass(){
            return commandClass;
        }

        CommandType(Class<?> commandClass){
            this.commandClass = commandClass;
        }

    }

    CommandType getType();



    class InitializeCommand implements Command {

        @Override
        public CommandType getType() {
            return CommandType.INIT;
        }

        public Template template;

        public InitializeCommand(Template template){
            this.template = template;
        }
    }

    class WriteCommand implements Command{

        @Override
        public CommandType getType() {
            return CommandType.WRITE;
        }


        public Reminders.Reminder reminder;


        public WriteCommand(Reminders.Reminder reminder){
            this.reminder = reminder;
        }

    }

    class CommandParser{
        public Command parseCommand(String command){
            if(!JsonUtils.isValidJson(command))
                return null;
            try{
                //workaround because gson doesn't parse interfaces...
                JSONObject object = new JSONObject(command);
                CommandType type = CommandType.valueOf(object.get("type").toString());

                return (Command) new Gson().fromJson(command, type.getCommandClass());
            }catch (Exception e){
                System.err.println(e.getClass().getSimpleName() + " while parsing command \""+command+"\": " + e.getMessage());
                return null;
            }
        }
    }
}
