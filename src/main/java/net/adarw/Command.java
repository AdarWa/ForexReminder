package net.adarw;

import com.google.gson.Gson;
import net.adarw.Utils.JsonUtils;
import net.adarw.Utils.KeyValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.logging.Logger;

public interface Command {

    enum CommandType{
        WRITE(WriteCommand.class),
        READ(ReadCommand.class),
        INIT(InitializeCommand.class),
        IMPORTFX(ImportFXCommand.class),
        EDIT(EditCommand.class),
        CLEAR(ClearCommand.class),
        READTEMPLATE(ReadTemplateCommand.class),
        CLEARTEMPLATE(ClearTemplateCommand.class),
        SETTEMPLATE(SetTemplateCommand.class),
        DELETE(DeleteCommand.class),
        CHOOSEFILE(ChooseFileCommand.class),
        HEARTBEAT(HeartbeatCommand.class),
        SETTINGS(SettingsCommand.class);

        private Class<?> commandClass;

        public Class<?> getCommandClass(){
            return commandClass;
        }

        CommandType(Class<?> commandClass){
            this.commandClass = commandClass;
        }

    }

    CommandType getType();



    class ReadCommand implements Command {

        @Override
        public CommandType getType() {
            return CommandType.READ;
        }
    }
    class ChooseFileCommand implements Command {

        public String file;

        @Override
        public CommandType getType() {
            return CommandType.CHOOSEFILE;
        }

        public ChooseFileCommand(String file){
            this.file = file;
        }
    }

    class HeartbeatCommand implements Command {

        @Override
        public CommandType getType() {
            return CommandType.HEARTBEAT;
        }
    }

    class DeleteCommand implements Command{
        @Override
        public CommandType getType() { return CommandType.DELETE;}

        public String uuid;

        public DeleteCommand(String uuid){
            this.uuid = uuid;
        }
    }

    class SettingsCommand implements Command{
        @Override
        public CommandType getType() { return CommandType.SETTINGS;}

        public String operation;

        public SettingsCommand(String operation){
            this.operation = operation;
        }
    }

    class ClearCommand implements Command {

        @Override
        public CommandType getType() {
            return CommandType.CLEAR;
        }
    }
    class ClearTemplateCommand implements Command {

        @Override
        public CommandType getType() {
            return CommandType.CLEARTEMPLATE;
        }
    }

    class ReadTemplateCommand implements Command {

        @Override
        public CommandType getType() {
            return CommandType.READTEMPLATE;
        }
    }

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

    class ImportFXCommand implements Command {

        @Override
        public CommandType getType() {
            return CommandType.IMPORTFX;
        }

        public String path;

        public ImportFXCommand(String path){
            this.path = path;
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
    class EditCommand implements Command{

        @Override
        public CommandType getType() {
            return CommandType.EDIT;
        }


        public Reminders.Reminder reminder;


        public EditCommand(Reminders.Reminder reminder){
            this.reminder = reminder;
        }

    }
    class SetTemplateCommand implements Command{

        @Override
        public CommandType getType() {
            return CommandType.SETTEMPLATE;
        }


        public Template template;


        public SetTemplateCommand(Template template){
            this.template = template;
        }

    }

    class CommandParser{
        Logger logger = Logger.getLogger(CommandParser.class.getName());
        public Command parseCommand(String command){
            if(!JsonUtils.isValidJson(command)) {
                logger.severe("Invalid JSON while parsing command: " + command);
                return null;
            }
            try{
                //workaround because gson doesn't parse interfaces...
                JSONObject object = new JSONObject(command);
                CommandType type = CommandType.valueOf(object.get("type").toString());

                return (Command) new Gson().fromJson(command, type.getCommandClass());
            }catch (Exception e){
                logger.severe(e.getClass().getSimpleName() + " while parsing command \""+command+"\": " + e.getMessage());
                return null;
            }
        }
    }
}
