package net.adarw.alertListner.gui;

import com.formdev.flatlaf.FlatLightLaf;
import net.adarw.Main;
import net.adarw.Reminders;
import net.adarw.Settings;
import net.adarw.Template;
import net.adarw.Utils.KeyValuePair;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;
import javazoom.jl.player.Player;


public class Alert extends JDialog {
    Logger logger = Logger.getLogger(Main.class.getName());
    private static int currentRemindersCount = 0;
    private static final int screenWidth = 400;
    private static final int screenHeight =  230;
    private static final String fontName = "Verdana";
    private static final int borderRadius = 15;


    public Alert(Reminders.Reminder reminder){
        currentRemindersCount++;

        JLabel titleLabel=new JLabel(Settings.current.alertTitle);

        titleLabel.setFont(new Font(fontName, Font.PLAIN, 18));
        titleLabel.setBounds(0,-screenHeight/2+ 25,screenWidth, screenHeight);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        setupWindowDecorations();
        createScrollableList(reminder);
        createXButton();
        add(titleLabel);
        setSize(screenWidth,screenHeight);
        setPosition();
        setLayout(null);
        setVisible(true);
        setAlwaysOnTop(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                currentRemindersCount--;
            }
        });
        if(!reminder.sound.isEmpty())
            Main.getInstance().player.queue.add(reminder.sound);
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
        l.add("Time: " + reminder.date.split(" ")[1]);
        for(KeyValuePair<Template.Component, Object> entry : reminder.entries){
            l.add(entry.getKey().name + ": " + entry.getValue().toString());
        }
        final JList<String> list = new JList<>(l.toArray(new String[0]));
        list.setFont(new Font(fontName, Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        list.setLayoutOrientation(JList.VERTICAL);
        panel.add(scrollPane);
        final int width = 300;
        final int height = 120;
        panel.setBounds(screenWidth/2 - width/2,50, width,height);
        add(panel);
    }

    public void createXButton(){
        final int width = 20;
        final int height = 20;
        JButton btn = new JButton("X");
        btn.setBounds(screenWidth - width, 0, width, height);
        btn.addActionListener(this::close);
        add(btn);
    }

    public void setPosition(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();

        setLocation(screenSize.width - windowSize.width, screenSize.height - windowSize.height*currentRemindersCount);
    }

    public void close(ActionEvent e){
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

}
