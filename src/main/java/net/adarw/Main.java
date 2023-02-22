package net.adarw;

import net.adarw.Utils.MessageUtils;
import net.adarw.Utils.StorageUtils;
import net.adarw.alertListner.Listener;
import org.zeromq.ZContext;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing Server...");
        try (ZContext context = new ZContext()) {

            Server server = new Server(context);
            Command.CommandParser parser = new Command.CommandParser();
            Listener listener = new Listener();

            listener.start();

            System.out.println("Server is active!");
            while (server.isActive()) {
                try{
                    Command command = parser.parseCommand(server.receiveMessage());
                    if(command.getType() == Command.CommandType.INIT){
                        Command.InitializeCommand cmd = (Command.InitializeCommand)command;
                        StorageUtils.setTemplate(cmd.template);
                        server.sendMessage(MessageUtils.getSuccessMessage());
                    }else if(command.getType() == Command.CommandType.WRITE){
                        Command.WriteCommand cmd = (Command.WriteCommand) command;
                        Reminders reminders = StorageUtils.getReminders();
                        if(reminders == null){
                            reminders = new Reminders();
                        }
                        reminders.reminders.add(cmd.reminder);
                        StorageUtils.writeReminders(reminders);
                        listener.interrupt();
                        server.sendMessage(MessageUtils.getSuccessMessage());
                    }
                }catch (Exception e){
                    System.err.println(e.getClass().getSimpleName() + " while in main loop: " + e.getMessage());
                }
            }
        }catch (Exception e){
            System.err.println(e.getClass().getSimpleName() + " while in main function: " + e.getMessage());
        }
    }
}