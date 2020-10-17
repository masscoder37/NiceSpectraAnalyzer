//this class makes it possible to display progress bars

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Random;

public class ProgressBar extends JProgressBar {
    //standard, practical variables
    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");
    private static DecimalFormat scientific = new DecimalFormat("0.0E0");

    //actual variables for the progress bar
    static int current = 0;
    static int max;
    static JProgressBar pb;
    static JFrame window;

    public static void progressBar(String title, int maxNumber){
        //set up window
        window = new JFrame(title);
        //use dispose because program might continue running
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //put window roughly in middle of screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation(screenSize.width/2-window.getSize().width/2 -300, screenSize.height/2-window.getSize().height/2 -500);
        window.setPreferredSize(new Dimension(500, 100));
        //set up the progress bar
        max = maxNumber;
        //the progress bar is in percent, so everytime between 0 and 100
        pb = new JProgressBar(0, 100);
        pb.setValue((int) (((double) current / (double) max)*100));
        pb.setPreferredSize(new Dimension(400, 25));
        pb.setStringPainted(true);
        JPanel panel = new JPanel();
        panel.add(pb);
        window.getContentPane().add(panel, SwingConstants.CENTER);
        //packing and displaying
        window.pack();
        window.setVisible(true);
//        Random random = new Random();
//            try {
//                Thread.sleep(random.nextInt(3000));
//            } catch (InterruptedException ignore) {
//            }
//            progress();

    }

    //TODO fix this mess....
    public static void progress(){
        current = current +1;
        int newSet = (int) (((double) current / (double) max)*100);
        pb.setValue(newSet);
    }
    public static int getSet(){
        return (int) (((double) current / (double) max)*100);
    }
    public static int getCurrent() {return current;}
    public static void reset(){
        current = 0;
        pb.setValue(0);
    }

    public static void close(){
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        reset();
    }

}
