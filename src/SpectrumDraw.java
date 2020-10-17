
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.*;

public class SpectrumDraw extends JComponent {
    //Line class to deal with drawing of lines
    private static class Line {
        //coordinates
        final double x1;
        final double y1;
        final double x2;
        final double y2;
        //style
        final double width;
        final Color color;
        //text label
        final String label;
        //orientation for axes & tickmarks
        final String orientation;


        //basic constructor
        public Line(double x1In, double y1In, double x2In, double y2In, double widthIn, Color colorIn, String labelIn, String orientationIn) {
            this.x1 = x1In;
            this.y1 = y1In;
            this.x2 = x2In;
            this.y2 = y2In;
            this.color = colorIn;
            this.width = widthIn;
            this.label = labelIn;
            this.orientation = orientationIn;

        }
    }

    //Axes to draw
    //in this list, the two axes are added which should be drawn
    private final LinkedList<Line> axisToDraw;

    //Tickmarks to draw
    private final LinkedList<Line> ticksToDraw;

    //mass peaks to draw
    private final LinkedList<Line> massPeaksToDraw;

    //other variables
    private double zoomfactor = 1;
    private Point mouseStart;
    private Point mouseEnd;

    public SpectrumDraw(){
        axisToDraw = new LinkedList<>();
        ticksToDraw = new LinkedList<>();
        massPeaksToDraw = new LinkedList<>();
    }


    //these functions add lines to the lists
    public void addAxis(double x1In, double y1In, double x2In, double y2In, double widthIn, String labelIn, String orientationIn) {
        axisToDraw.add(new Line(x1In, y1In, x2In, y2In, widthIn, Color.black, labelIn, orientationIn));
    }

    public void addTicks(double x1In, double y1In, double x2In, double y2In, double widthIn, String labelIn, String orientationIn) {
        ticksToDraw.add(new Line(x1In, y1In, x2In, y2In, widthIn, Color.black, labelIn, orientationIn));
    }

    public void addPeak(double x1In, double y1In, double x2In, double y2In, String labelIn, Color colorIn) {
        massPeaksToDraw.add(new Line(x1In, y1In, x2In, y2In, 1.5, colorIn, labelIn, ""));
    }

    public void clearLines() {
        axisToDraw.clear();
        ticksToDraw.clear();
        massPeaksToDraw.clear();
    }

    public void redraw() {
        repaint();
    }

    public void setZoomfactor(double zoomfactorIn) {
        zoomfactor = zoomfactorIn;
    }

    public double getZoomfactor() {
        return zoomfactor;
    }

    public Point getMouseStart() {
        return mouseStart;
    }

    public void setMouseStart(Point mouseStartIn) {
        this.mouseStart = mouseStartIn;

    }

    public Point getMouseEnd() {
        return mouseEnd;
    }

    public void setMouseEnd(Point mouseEnd) {
        this.mouseEnd = mouseEnd;
    }

    //Here, all the components are drawn
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        AffineTransform at = new AffineTransform();
        at.scale(zoomfactor, zoomfactor);
        Graphics2D g2 = (Graphics2D) g;
        Graphics2D t = (Graphics2D) g;
        g2.transform(at);
        for (Line line : ticksToDraw) {
            g2.setStroke(new BasicStroke((float) line.width));
            g2.setColor(line.color);
            g2.draw(new Line2D.Double(line.x1, line.y1, line.x2, line.y2));
            t.setFont(new Font("Arial", Font.BOLD, 18));
            t.setColor(Color.black);
            int textwidth = t.getFontMetrics().stringWidth(line.label);
            int textheight = t.getFontMetrics().getHeight();
            if (line.orientation.equals("y")) {
                t.drawString(line.label, (float) (line.x2 - 2 - textwidth), (float) (line.y1 + textheight * 0.34));
            } else {
                t.drawString(line.label, (float) (line.x1 - textwidth * 0.5), (float) (line.y2 + 5 + textheight));
            }
        }
        for (Line line : massPeaksToDraw) {
            //Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke((float) line.width));
            g2.setColor(line.color);
            g2.draw(new Line2D.Double(line.x1, line.y1, line.x2, line.y2));
            t.setFont(new Font("Arial", Font.PLAIN, 12));
            t.setColor(Color.black);
            /*int textwidth = t.getFontMetrics().stringWidth(line.label);
            int textheight = t.getFontMetrics().getHeight();
            t.drawString(line.label, (float) (line.x1 - textwidth * 0.5), (float) (line.y2 - textheight));*/
            //handle multiline text
            String[] labelLines = line.label.split(";");
            if (labelLines.length == 0)
                continue;
            int textHeight = t.getFontMetrics().getHeight();
            for (int i = 0; i < labelLines.length; i++){
                //offset to display that text as highest line
                int m = labelLines.length - i -1;
                int textWidth = t.getFontMetrics().stringWidth(labelLines[i]);
                t.drawString(labelLines[i], (float) (line.x1-textWidth*0.5), (float) (line.y2 - (textHeight + 2) * m -5));
            }
        }
        for (Line line : axisToDraw) {
            //Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke((float) line.width));
            g2.setColor(line.color);
            g2.draw(new Line2D.Double(line.x1, line.y1, line.x2, line.y2));
        }

        if (mouseStart != null) {
            g2.setStroke(new BasicStroke((float) 3));
            g2.setColor(Color.black);
            g2.draw(new Line2D.Double(mouseStart.getX(), mouseStart.getY(), mouseEnd.getX(), mouseStart.getY()));
        }
    }
}
