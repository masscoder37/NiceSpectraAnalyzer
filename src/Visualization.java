//this class will handle visualization by windows, e.g. the plotting of mass spectra
import java.awt.*;
import javax.swing.*;
public class Visualization {

    public static void massSpectrumTest(String textIn) {
        JFrame mainWindow = new JFrame("Mass spectra Visualization");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel text = new JLabel(textIn, SwingConstants.CENTER);
        text.setForeground(Color.BLUE);
        text.setFont(new Font("Arial", Font.BOLD, 30));


        text.setPreferredSize(new Dimension(500, 500));
        mainWindow.getContentPane().add(text, BorderLayout.CENTER);

        mainWindow.setLocationRelativeTo(null);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }


    public static class Line {
        private final int x1;
        private final int x2;
        private final int y1;
        private final int y2;

        public Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        public void paint(Graphics g) {
            g.drawLine(this.x1, this.y1, this.x2, this.y2);
        }

    }
}
