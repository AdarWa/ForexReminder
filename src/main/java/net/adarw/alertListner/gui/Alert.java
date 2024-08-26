package net.adarw.alertListner.gui;

import net.adarw.*;
import net.adarw.Utils.KeyValuePair;
import net.adarw.alertListner.AlertQueuer;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class Alert extends JDialog {
    Logger logger = Logger.getLogger(Main.class.getName());
    private static final int screenWidth = 250;
    private static final int screenHeight =  180;
    private static final String fontName = "Verdana";
    private static final int borderRadius = 15;
    private static ArrayList<KeyValuePair<String, Boolean>> currentReminders = new ArrayList<>();
    private static volatile int reminderCount = 0;
    private static final int screenFrameWidth = 8;
    private static final int screenFrameHeight = 18;
    private static final int textBoxHeightOffset = 10;
    private static final int alertTitleOffset =15;
    public static volatile Queue<String> queue = new ConcurrentLinkedQueue<>();
    public int localCount = 0;
    public static volatile boolean working = false;
    public Reminders.Reminder reminder;

    public Alert(Reminders.Reminder reminder){
        AlertQueuer.queue.add(()->init(reminder, false, null, "", -1));
    }

    private void init(Reminders.Reminder reminder, boolean remindBefore, @Nullable String soundPath, String title, int closeTime){
//        currentRemindersCount++;
        this.reminder = reminder;
        if(!queue.isEmpty()){
            queue.add(reminder.uuid);
            while(queue.contains(reminder.uuid));
        }
        reminderCount++;
        localCount = reminderCount;
        JLabel titleLabel=new JLabel(remindBefore?title:Settings.current.alertTitle);

        titleLabel.setFont(new Font(fontName, Font.BOLD, 16));
        titleLabel.setBounds(0,-screenHeight/2+ alertTitleOffset,screenWidth, screenHeight);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        setLayout(null);
        setupWindowDecorations();
        add(titleLabel);
        createScrollableList(reminder);
        createXButton();
        setSize(screenWidth,screenHeight);
        setPosition(reminder.uuid, remindBefore);
        setVisible(true);
        setAlwaysOnTop(true);
        getContentPane().setBackground(Color.decode(Settings.current.alertBgColor));
        setOpacity(Settings.current.alertOpacity /100f);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
//                for(KeyValuePair<String, Boolean> entry : currentReminders){
//                    if(entry.getKey().equals(reminder.uuid)){
//                        currentReminders.set(currentReminders.indexOf(entry), new KeyValuePair<>(reminder.uuid, false));
//                        break;
//                    }
//                }
                reminderCount--;
            }
        });


        if(soundPath != null){
            Main.getInstance().player.queue.add(soundPath);
            logger.info("Playing remind before sound");
        }else if(!reminder.sound.isEmpty()){
            logger.info("Playing reminder's sound");
            Main.getInstance().player.queue.add(reminder.sound);
        }
        if(!queue.isEmpty()){
            queue.poll();
        }
        working = false;
        logger.info(reminder.uuid);
        if((Settings.current.secondsUntilAlertDisappear > 0 && !remindBefore)|| (closeTime > 0 && remindBefore)){
            new Thread(()->{
                try {
                    Thread.sleep((remindBefore?closeTime:Settings.current.secondsUntilAlertDisappear)* 1000L);
                } catch (InterruptedException e) {
                    logger.severe("Interrupted sleep");
                }
                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            }).start();
        }
    }

    public Alert(Reminders.Reminder reminder, boolean remindBefore, String soundPath, String title, int closeTime){
        AlertQueuer.queue.add(()->init(reminder, remindBefore, soundPath, title, closeTime));
    }

    public void setupWindowDecorations(){
        setUndecorated(true);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                try {
                    ((Graphics2D) getGraphics()).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), borderRadius, borderRadius));
                }catch (NullPointerException ignored){}
            }
        });
    }

    public void createScrollableList(Reminders.Reminder reminder){
        JPanel panel = new JPanel(new BorderLayout());
        ArrayList<String> l = new ArrayList<>();
        int i = 0;
        String match = "";
        for(KeyValuePair<Template.Component, Object> entry : reminder.entries){
            if(i == Settings.current.timeFieldPlacementInAlert){
                DateFormat format = new SimpleDateFormat("EEEE");
                l.add(reminder.date.split(" ")[1] + " " + format.format(TimerEx.getDate(reminder.date)));
            }
            l.add(entry.getKey().name + ": " + entry.getValue().toString());
            if(entry.getKey().name.equals(Settings.current.ImpactFieldColoring)){
                match = entry.getKey().name + ": " + entry.getValue().toString();
            }
            i++;
        }
        if(reminder.entries.size() < (Settings.current.timeFieldPlacementInAlert+1)){
            DateFormat format = new SimpleDateFormat("EEEE");
            l.add(reminder.date.split(" ")[1] + " " + format.format(TimerEx.getDate(reminder.date)));
        }
        final JList<String> list = new JList<>(l.toArray(new String[0]));
        if(!Settings.current.ImpactFieldColoring.isEmpty()){
            list.setCellRenderer(new AlertListCellRenderer(match));
        }
        list.setFont(new Font(fontName, Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        list.setLayoutOrientation(JList.VERTICAL);
        panel.add(scrollPane);
        final int width = screenWidth - screenFrameWidth*2;
        final int height = screenHeight - screenFrameHeight*2;
        panel.setBounds(screenWidth/2 - width/2,screenHeight/2 - height/2 + textBoxHeightOffset, width, height);
        add(panel);
    }

    public void createXButton(){
        final int width = 15;
        final int height = 15;
        JButton btn = new JButton("X");
        btn.setFont(new Font("Arial",Font.BOLD, 14));
        btn.setBounds(screenWidth - width, 0, width, height);
        btn.addActionListener(this::close);
        add(btn);
    }

    public void setPosition(String uuid, boolean remindBefore){
        Dimension screenSize = Settings.current.screenSize;
        Dimension windowSize = getSize();
        double maxRemindersPerRow = Math.floor((screenSize.height-Settings.current.alertVerticalOffset)/windowSize.height);
//        int index = 0;
//        for(KeyValuePair<String, Boolean> entry : currentReminders){
//            if(entry.getKey().equals(uuid) && entry.getValue()){
//                index = currentReminders.indexOf(entry)+1;
//                break;
//            }
//        }
//        for(KeyValuePair<String, Boolean> entry : currentReminders){
//            if(index == 0) break;
//            if(!entry.getValue()){
//                index = currentReminders.indexOf(entry)+1;
//                currentReminders.set(index-1, new KeyValuePair<>(uuid, true));
//                break;
//            }
//        }
//        if(index == 0){
//            index = currentReminders.size()+1;
//            currentReminders.add(new KeyValuePair<>(uuid, true));
//        }
//        int y = screenSize.height - windowSize.height*index - ((int)Settings.current.alertVerticalOffset);
//        int x = 1;
//        if(y < 0){
//            int alertsPerWindow = screenSize.height/windowSize.height;
//            x = index/alertsPerWindow+1;
//            y = screenSize.height - windowSize.height*(index-alertsPerWindow*(x-1)) - ((int)Settings.current.alertVerticalOffset);
//        }
        boolean invalid = false;
        int count = reminderCount;
        int x = (int) (Math.floor((reminderCount-1)/maxRemindersPerRow)+1);
        int finalX = screenSize.width - (windowSize.width*x);
        int finalY = (int) (screenSize.height - windowSize.height*(reminderCount-(x-1)*(maxRemindersPerRow)) - ((int)Settings.current.alertVerticalOffset));
        if(finalY > screenSize.height-windowSize.height){
            logger.severe("Invalid Y position on reminder with uuid " + reminder.uuid);
            invalid = true;
        }
        if(finalX > screenSize.width-windowSize.width){
            logger.severe("Invalid X position on reminder with uuid " + reminder.uuid);
            invalid = true;
        }
        if(!invalid)
            setLocation(finalX, finalY);
    }

    @Override
    public void setLocation(int x,int y){
        revalidate();
        setVisible(true);
        SwingUtilities.invokeLater(()-> super.setLocation(x,y));
    }

    public void close(ActionEvent e){
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    static class AlertListCellRenderer extends JLabel implements ListCellRenderer {
        String match;
        public AlertListCellRenderer(String match) {
            this.match = match;
            setOpaque(true);
        }
        public Component getListCellRendererComponent(JList paramlist, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//            setText(String.format("<html><div style=\"width:%dpx; font-size:14px; margin: 0;\">%s</div></html>", paramlist.getBounds().width, value.toString()));
            if(value.toString().split(":")[0].equals(Settings.current.descFieldName)){
                String text = "<html>";
                char[] chars = value.toString().toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if(i % Settings.current.charsToBreakAfter == 0){
                        text += "<br/>";
                    }
                    text += chars[i];
                }
                text += "</html>";
                setText(text);
            }else
                setText(value.toString());
//            setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
            if (value.toString().equals(match)) {
                setFont(new Font("Arial", Font.BOLD, 16));
                String impact = match.split(":")[1];
                if(impact.equals(" LOW")){
                    setForeground(Color.decode("#008000"));
                }else if(impact.equals(" MEDIUM")){
                    setForeground(Color.decode("#ffa500"));
                }else if(impact.equals(" HIGH")){
                    setForeground(Color.decode("#AA4A44"));
                }
            }else{
                setFont(new Font("Arial", Font.PLAIN, 14));
                setForeground(Color.BLACK);
            }
            setBackground(Color.WHITE);
            return this;
        }
    }

}
