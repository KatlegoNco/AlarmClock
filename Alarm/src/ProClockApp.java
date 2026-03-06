import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javazoom.jl.player.Player;
import java.io.FileInputStream;

public class ProClockApp extends JFrame {

    private final JLabel clockLabel;
    private DefaultListModel<String> alarmListModel;
    private JList<String> alarmList;
    private JTextField alarmField;

    private JLabel countdownLabel;
    private JTextField countdownField;

    private JLabel stopwatchLabel;
    private Timer stopwatchTimer;
    private int stopwatchSeconds = 0;

    private java.util.List<String> alarms = new ArrayList<>();
    private Player player;

    public ProClockApp(){

        setTitle("Pro Desktop Clock");
        setSize(550,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        getContentPane().setBackground(new Color(18,18,18));
        setLayout(new BorderLayout());

        //---------------------------------
        // DIGITAL CLOCK
        //---------------------------------

        clockLabel = new JLabel("",SwingConstants.CENTER);
        clockLabel.setFont(new Font("Consolas",Font.BOLD,60));
        clockLabel.setForeground(new Color(0,255,170));
        clockLabel.setBorder(BorderFactory.createEmptyBorder(20,10,20,10));

        add(clockLabel,BorderLayout.NORTH);

        Timer clockTimer = new Timer(1000,e->updateClock());
        clockTimer.start();

        //---------------------------------
        // TABS
        //---------------------------------

        JTabbedPane tabs = new JTabbedPane();

        tabs.add("Alarms", createAlarmPanel());
        tabs.add("Countdown", createCountdownPanel());
        tabs.add("Stopwatch", createStopwatchPanel());

        add(tabs,BorderLayout.CENTER);

        //---------------------------------
        // ALARM CHECK THREAD
        //---------------------------------

        new Thread(() -> {

            while(true){

                try{

                    String current = getCurrentTime();

                    for(String alarm : alarms){

                        if(current.equals(alarm)){
                            triggerAlarm();
                        }

                    }

                    Thread.sleep(1000);

                }catch(Exception ex){
                    ex.printStackTrace();
                }

            }

        }).start();

    }

    //---------------------------------
    // ALARM PANEL
    //---------------------------------

    private JPanel createAlarmPanel(){

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30,30,30));

        JPanel top = new JPanel();
        top.setBackground(new Color(30,30,30));

        alarmField = new JTextField(8);

        JButton add = new JButton("Add");
        JButton remove = new JButton("Remove");
        JButton snooze = new JButton("Snooze");

        top.add(new JLabel("HH:mm:ss"));
        top.add(alarmField);
        top.add(add);
        top.add(remove);
        top.add(snooze);

        alarmListModel = new DefaultListModel<>();
        alarmList = new JList<>(alarmListModel);

        panel.add(top,BorderLayout.NORTH);
        panel.add(new JScrollPane(alarmList),BorderLayout.CENTER);

        add.addActionListener(e->{

            String time = alarmField.getText();
            alarms.add(time);
            alarmListModel.addElement(time);

        });

        remove.addActionListener(e->{

            int index = alarmList.getSelectedIndex();

            if(index!=-1){
                alarms.remove(index);
                alarmListModel.remove(index);
            }

        });

        snooze.addActionListener(e->snoozeAlarm());

        return panel;

    }

    //---------------------------------
    // COUNTDOWN PANEL
    //---------------------------------

    private JPanel createCountdownPanel(){

        JPanel panel = new JPanel();
        panel.setBackground(new Color(30,30,30));

        countdownField = new JTextField(6);
        JButton start = new JButton("Start");

        countdownLabel = new JLabel("00:00");
        countdownLabel.setFont(new Font("Consolas",Font.BOLD,40));
        countdownLabel.setForeground(new Color(0,255,170));

        panel.add(new JLabel("Seconds"));
        panel.add(countdownField);
        panel.add(start);
        panel.add(countdownLabel);

        start.addActionListener(e->startCountdown());

        return panel;

    }

    //---------------------------------
    // STOPWATCH PANEL
    //---------------------------------

    private JPanel createStopwatchPanel(){

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30,30,30));

        stopwatchLabel = new JLabel("00:00:00",SwingConstants.CENTER);
        stopwatchLabel.setFont(new Font("Consolas",Font.BOLD,50));
        stopwatchLabel.setForeground(new Color(0,255,170));

        JPanel buttons = new JPanel();
        buttons.setBackground(new Color(30,30,30));

        JButton start = new JButton("Start");
        JButton pause = new JButton("Pause");
        JButton reset = new JButton("Reset");

        buttons.add(start);
        buttons.add(pause);
        buttons.add(reset);

        panel.add(stopwatchLabel,BorderLayout.CENTER);
        panel.add(buttons,BorderLayout.SOUTH);

        stopwatchTimer = new Timer(1000,e->updateStopwatch());

        start.addActionListener(e->stopwatchTimer.start());
        pause.addActionListener(e->stopwatchTimer.stop());

        reset.addActionListener(e->{

            stopwatchTimer.stop();
            stopwatchSeconds=0;
            stopwatchLabel.setText("00:00:00");

        });

        return panel;

    }

    //---------------------------------
    // CLOCK
    //---------------------------------

    private void updateClock(){

        clockLabel.setText(getCurrentTime());

    }

    private String getCurrentTime(){

        return new SimpleDateFormat("HH:mm:ss").format(new Date());

    }

    //---------------------------------
    // STOPWATCH
    //---------------------------------

    private void updateStopwatch(){

        stopwatchSeconds++;

        int hours = stopwatchSeconds/3600;
        int minutes = (stopwatchSeconds%3600)/60;
        int seconds = stopwatchSeconds%60;

        stopwatchLabel.setText(
                String.format("%02d:%02d:%02d",hours,minutes,seconds)
        );

    }

    //---------------------------------
    // COUNTDOWN
    //---------------------------------

    private void startCountdown(){

        final int[] seconds = {Integer.parseInt (countdownField.getText ())};

        new Thread(() -> {

            try{

                while(seconds[0] >=0){

                    int min = seconds[0] /60;
                    int sec = seconds[0] %60;

                    countdownLabel.setText(
                            String.format("%02d:%02d",min,sec)
                    );

                    Thread.sleep(1000);

                    seconds[0]--;

                }

                triggerAlarm();

            }catch(Exception ex){
                ex.printStackTrace();
            }

        }).start();

    }

    //---------------------------------
    // ALARM SOUND
    //---------------------------------

    private void triggerAlarm(){

        JOptionPane.showMessageDialog(this,"⏰ Alarm!");

        playMP3("alarm.mp3");

    }

    private void playMP3(String file){

        new Thread(() -> {

            try{

                FileInputStream fis = new FileInputStream(file);
                player = new Player(fis);
                player.play();

            }catch(Exception e){
                e.printStackTrace();
            }

        }).start();

    }

    //---------------------------------
    // SNOOZE
    //---------------------------------

    private void snoozeAlarm(){

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE,5);

        String time = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());

        alarms.add(time);
        alarmListModel.addElement(time);

    }

    public static void main(String[] args){

        SwingUtilities.invokeLater(() -> new ProClockApp().setVisible(true));

    }

}