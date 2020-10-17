//this class will handle visualization by windows, e.g. the plotting of mass spectra

import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.Border;


public class Visualization {
    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");
    private static DecimalFormat scientific = new DecimalFormat("0.0E0");

    static ArrayList<Color> featureColors = new ArrayList<>();
    static double zoomFactor = 1;
    static MySpectrum toPlot;
    static int currentSpectrum;
    static int minMass;
    static int maxMass;
    static int fullRoundedMinMass;
    static int fullRoundedMaxMass;
    static boolean showLabels = false;


    //helper class to handle changing mass ranges
    private static class MassRange {
        double minMass;
        double maxMass;
        double massRange;

        MassRange(double mass1In, double mass2In) {
            if (mass1In <= mass2In) {
                this.minMass = mass1In;
                this.maxMass = mass2In;
            } else {
                this.minMass = mass2In;
                this.maxMass = mass1In;
            }
            massRanger();
        }

        private void massRanger() {
            this.massRange = this.maxMass - this.minMass;
        }

        public void setNewMasses(double mass1In, double mass2In) {
            if (mass1In <= mass2In) {
                this.minMass = mass1In;
                this.maxMass = mass2In;
            } else {
                this.minMass = mass2In;
                this.maxMass = mass1In;
            }
            massRanger();
        }

        public double getMinMass() {
            return minMass;
        }

        public double getMaxMass() {
            return maxMass;
        }

        public double getMassRange() {
            return massRange;
        }
    }

    //helper class to help with changing canvas sizes
    private static class PlotSize {
        //axes coordinates
        //y-axis: 100, 75, 100, 900
        //x-axis: 100, 900, 1900, 900
        double yX; // = 0.04*canvasWidth;
        double yY1; //= 0.9*canvasHeight;
        double yY2; //= 0.075*canvasHeight;

        double xY; //0.9*canvasHeight;
        double xX1; //0.04*canvasWidth;
        double xX2;//0.96*canvasWidth;

        double yAxisLength;//= Math.abs(yY1-yY2);
        double xAxisLength;//= Math.abs(xX1-xX2);

        PlotSize(int canvasWidthIn, int canvasHeightIn) {
            coordCalc(canvasWidthIn, canvasHeightIn);
        }

        private void coordCalc(int canvasWidthIn, int canvasHeightIn) {
            this.yX = 0.04 * canvasWidthIn;
            this.yY1 = 0.9 * canvasHeightIn;
            this.yY2 = 0.075 * canvasHeightIn;
            this.xY = 0.9 * canvasHeightIn;
            this.xX1 = 0.04 * canvasWidthIn;
            this.xX2 = 0.96 * canvasWidthIn;
            this.yAxisLength = Math.abs(this.yY1 - this.yY2);
            this.xAxisLength = Math.abs(this.xX1 - this.xX2);
        }

        public void changeCanvasSize(int newCanvasWidthIn, int newCanvasHeightIn) {
            coordCalc(newCanvasWidthIn, newCanvasHeightIn);
        }

        public double getyX() {
            return yX;
        }

        public double getyY1() {
            return yY1;
        }

        public double getyY2() {
            return yY2;
        }

        public double getxY() {
            return xY;
        }

        public double getxX1() {
            return xX1;
        }

        public double getxX2() {
            return xX2;
        }

        public double getyAxisLength() {
            return yAxisLength;
        }

        public double getxAxisLength() {
            return xAxisLength;
        }
    }

    //method in which mass spectra are produced
    public static void spectrumPlotter(MzXMLFile runIn, double ppmDevIn) throws JMzReaderException, MzXMLParsingException {
        //setup the Spectrum to look at
        //long is so annoying, fuck performance -.-
        java.util.List<Long> scanNumberListLong = runIn.getScanNumbers();
        ArrayList<Integer> scanNumberList = new ArrayList<>();
        for (Long number : scanNumberListLong) {
            scanNumberList.add(Math.toIntExact(number));
        }

        currentSpectrum = scanNumberList.get(0);
        //first spectrum is the first one in the file
        toPlot = MzXMLReadIn.mzXMLToMySpectrum(runIn, "" + currentSpectrum);
        //toPlot.assignChargeStates(ppmDevIn);
        //toPlot.assignFeatures(ppmDevIn);
        toPlot.assignZAndFeatures(10);

        //general JFrame setup
        String title = "Spectrum Plotter";
        JFrame window = new JFrame(title);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //Here is the canvas upon which the spectrum is drawn
        SpectrumDraw comp = new SpectrumDraw();
        comp.setPreferredSize(new Dimension(2500, 1000));
        window.getContentPane().add(comp, BorderLayout.CENTER);

        //set up buttons so that you can specify a scan number
        JPanel input = new JPanel();
        JButton plotButton = new JButton("Plot");
        JTextField scanToPlot = new JTextField("Scan-#");
        Font textFieldFont = new Font("Arial", Font.BOLD, 12);
        scanToPlot.setFont(textFieldFont);
        JLabel instructions = new JLabel("Choose a spectrum to Plot! Current spectrum: " + currentSpectrum + "     " + toPlot.getScanHeader() + "      TIC:" + scientific.format(toPlot.getSpectrumTIC()));
        Font labelFont = new Font("Arial", Font.BOLD, 18);
        instructions.setFont(labelFont);
        input.add(scanToPlot);
        input.add(plotButton);
        input.add(instructions);
        window.getContentPane().add(input, BorderLayout.SOUTH);
        comp.setFocusable(true);
        comp.requestFocusInWindow();

        //initialize random colors
        Random random = new Random();
        for (int i = 0; i < 250; i++) {
            featureColors.add(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat()));
        }

        //set original border masses
        minMass = minMass();
        maxMass = maxMass();
        //figures out next nearest 100ths to min mass and max mass
        fullRoundedMinMass = roundedMinMass(minMass);
        fullRoundedMaxMass = roundedMaxMass(maxMass);


        //these variables are dynamic
        MassRange massRange = new MassRange(fullRoundedMinMass, fullRoundedMaxMass);
        PlotSize plotSize = new PlotSize(2500, 1000);

        //actually plotting the spectra
        graphProducer(comp, toPlot, massRange, plotSize);

        //deals with resizing the window
        comp.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int canvasWidth = (int) Math.round(comp.getWidth());
                int canvasHeight = (int) Math.round(comp.getHeight());
                plotSize.changeCanvasSize(canvasWidth, canvasHeight);
                comp.clearLines();
                graphProducer(comp, toPlot, massRange, plotSize);
            }
        });

        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1) {
                    massRange.setNewMasses(fullRoundedMinMass, fullRoundedMaxMass);
                    zoomFactor = 1;
                    comp.clearLines();
                    graphProducer(comp, toPlot, massRange, plotSize);
                }

            }

            @Override
            public void mousePressed(MouseEvent e) {
                comp.setMouseStart(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                comp.setMouseEnd(e.getPoint());
                if (comp.getMouseStart() != comp.getMouseEnd()) {
                    double mass1 = ((comp.getMouseStart().getX() - plotSize.getyX()) / (plotSize.getxAxisLength() / massRange.getMassRange())) + massRange.getMinMass();
                    double mass2 = ((e.getX() - plotSize.getyX()) / (plotSize.getxAxisLength() / massRange.getMassRange())) + massRange.getMinMass();
                    //TODO: error handling if the range is outside the mass range?
                    massRange.setNewMasses(mass1, mass2);
                    comp.clearLines();
                    graphProducer(comp, toPlot, massRange, plotSize);
                    //to avoid the drawing of the zoom line
                    comp.setMouseStart(null);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        comp.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                comp.setMouseEnd(e.getPoint());
                if (comp.getMouseStart() != comp.getMouseEnd())
                    comp.redraw();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
            }
        });

        comp.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    double oldZoomfactor = zoomFactor;
                    zoomFactor += 0.4;
                    comp.clearLines();
                    graphProducer(comp, toPlot, massRange, plotSize);
                }

                if (e.getWheelRotation() > 0) {
                    double oldZoomfactor = zoomFactor;
                    zoomFactor -= 0.4;
                    if (zoomFactor <= 0)
                        zoomFactor = 0.1;
                    comp.clearLines();
                    graphProducer(comp, toPlot, massRange, plotSize);
                }
            }
        });

        //Event handlers for the buttons
        //event handler to delete textfield text if clicked
        scanToPlot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                scanToPlot.setText("");
            }
        });
        //ActionListener for the button
        plotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //error handling
                try {
                    currentSpectrum = Integer.parseInt(scanToPlot.getText());
                } catch (NumberFormatException f) {
                    scanToPlot.setText("Scan-#");
                    instructions.setText("Please input a whole number!");
                }
                //more error handling: scan number entered out of range
                if (!scanNumberList.contains(currentSpectrum)) {
                    currentSpectrum = scanNumberList.get(0);
                    scanToPlot.setText("Scan-#");
                    instructions.setText("Out of Range! Scans available: " + scanNumberList.get(0) + " - " + scanNumberList.get(scanNumberList.size() - 1));
                } else {
                    try {
                        toPlot = MzXMLReadIn.mzXMLToMySpectrum(runIn, Integer.toString(currentSpectrum));

                    } catch (JMzReaderException | MzXMLParsingException ex) {
                        instructions.setText("Spectrum Read In went wrong! Spectrum: " + currentSpectrum);
                    }
                    //toPlot.assignChargeStates(ppmDevIn);
                    //toPlot.assignFeatures(ppmDevIn);
                    toPlot.assignZAndFeatures(ppmDevIn);
                    //change parameters in massRange
                    minMass = minMass();
                    maxMass = maxMass();
                    //figures out next nearest 100ths to min mass and max mass
                    fullRoundedMinMass = roundedMinMass(minMass);
                    fullRoundedMaxMass = roundedMaxMass(maxMass);
                    massRange.setNewMasses(fullRoundedMinMass, fullRoundedMaxMass);
                    scanToPlot.setText("Scan-#");
                    String fragMethod = "     ";
                    fragMethod += toPlot.getFragmentationMethod();
                    if (fragMethod.equals("     NA"))
                        fragMethod = "";
                    instructions.setText("Spectrum plotted! Current spectrum: " + currentSpectrum + "     " + toPlot.getScanHeader() + fragMethod + "      TIC:" + scientific.format(toPlot.getSpectrumTIC()));
                    zoomFactor = 1;
                    comp.clearLines();
                    //toPlot and massRange was changed
                    graphProducer(comp, toPlot, massRange, plotSize);
                    comp.requestFocusInWindow();
                }
            }
        });

        comp.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //next spectrum
                if (e.getKeyCode() == 39) {
                    currentSpectrum += 1;
                    scanToPlot.setText(Integer.toString(currentSpectrum));
                    plotButton.doClick();
                }

                //previousSpectrum
                if (e.getKeyCode() == 37) {
                    currentSpectrum -= 1;
                    scanToPlot.setText(Integer.toString(currentSpectrum));
                    plotButton.doClick();
                }

                //toggle labels
                if (e.getKeyCode() == 76) {
                    if (showLabels)
                        showLabels = false;
                    else
                        showLabels = true;
                    //actually replot
                    comp.clearLines();
                    graphProducer(comp, toPlot, massRange, plotSize);
                    comp.requestFocusInWindow();
                }

                //seed new random colors
                if (e.getKeyCode() == 67) {
                    featureColors.clear();
                    for (int i = 0; i < 250; i++) {
                        featureColors.add(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat()));
                    }
                    //actually replot
                    comp.clearLines();
                    graphProducer(comp, toPlot, massRange, plotSize);
                    comp.requestFocusInWindow();
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });


        window.pack();
        window.setVisible(true);
    }


    private static int minMass() {
        return (int) Math.round(toPlot.getPeakList().get(0).getMass());
    }

    private static int maxMass() {
        return (int) Math.round(toPlot.getPeakList().get(toPlot.getPeakList().size() - 1).getMass());
    }

    private static int roundedMinMass(int minMassIn) {
        return minMassIn / 100 * 100;
    }

    private static int roundedMaxMass(int maxMassIn) {
        return (maxMassIn + 99) / 100 * 100;
    }


    public static void graphProducer(SpectrumDraw comp, MySpectrum toPlot, MassRange massRange, PlotSize plotSize) {
        for (int i = 0; i < 5; i++) {
            double axisSteps = 25 / zoomFactor;
            comp.addTicks(plotSize.getyX(), (plotSize.getyY1() - (plotSize.getyAxisLength() / 100) * i * 25), plotSize.getyX() - 10, (plotSize.getyY1() - (plotSize.getyAxisLength() / 100) * i * 25), 3, twoDec.format(i * axisSteps) + "%", "y");
        }
        //ticks for the mass axis
        for (int i = 0; i < 6; i++) {
            double xCord = (plotSize.getyX() + (plotSize.getxAxisLength() / massRange.getMassRange()) * (massRange.getMassRange()) * i * 0.2);
            double xMass = massRange.getMassRange() * i * 0.2 + massRange.getMinMass();
            comp.addTicks(xCord, plotSize.getxY(), xCord, plotSize.getxY() + 10, 3, twoDec.format(xMass), "x");
        }
        //draw main axes
        //y-axis
        comp.addAxis(plotSize.getyX(), plotSize.getyY1(), plotSize.getyX(), plotSize.getyY2(), 3, "y-Axis", "y");
        //x-axis
        comp.addAxis(plotSize.getxX1(), plotSize.getxY(), plotSize.getxX2(), plotSize.getxY(), 3, "x-Axis", "x");

        //draw mass peaks
        //for (Peak peak : toPlot.getPeakList()){
        for (int i = 0; i < toPlot.getPeakList().size(); i++) {
            Peak peak = toPlot.getPeakList().get(i);
            Peak prevPeak;
            try {
                prevPeak = toPlot.getPeakList().get(i - 1);
            } catch (IndexOutOfBoundsException ex2) {
                prevPeak = peak;
            }
            Peak nextPeak;
            try {
                nextPeak = toPlot.getPeakList().get(i + 1);
            } catch (IndexOutOfBoundsException ex3) {
                nextPeak = peak;
            }

            double mass = peak.getMass();
            //skip if out of mass range
            if (mass < massRange.getMinMass() || mass > massRange.getMaxMass())
                continue;

            double relInt = peak.getRelIntensity();
            double xCord = (plotSize.getyX() + (plotSize.getxAxisLength() / massRange.getMassRange()) * (mass - massRange.getMinMass()));
            double yCordMax = plotSize.yY1 - (plotSize.yAxisLength / 100) * relInt * zoomFactor;
            String diffPrev = "<--  " + fourDec.format(peak.getMass() - prevPeak.getMass());
            String diffNext = fourDec.format(nextPeak.getMass() - peak.getMass()) + "  -->";
            String label = "";
            //only plot if labels are toggled
            if (showLabels) {
                if (relInt >= 0) {
                    label += fourDec.format(mass) + " m/z;" + twoDec.format(relInt) + "%;" + peak.getCharge() + "+;" + diffPrev + ";" + diffNext;
                    if (peak.isPartOfFeature()) {
                        label += ";F#:" + peak.getFeature().getFeatureNumber();
                    }
                }
            }
            comp.addPeak(xCord, plotSize.xY, xCord, yCordMax, label, Color.black);
        }


        int colChanger = 0;
        for (Feature feature : toPlot.getFeatureList()) {
            Color color;
            try {
                color = featureColors.get(colChanger);
            } catch (IndexOutOfBoundsException e) {
                color = Color.red;
                colChanger = 0;
            }
            for (Peak peak : feature.getPeakList()) {
                double mass = peak.getMass();
                //skip if out of mass range
                if (mass < massRange.getMinMass() || mass > massRange.getMaxMass()) {
                    colChanger++;
                    continue;
                }

                double relInt = peak.getRelIntensity();
                double xCord = (plotSize.getyX() + (plotSize.getxAxisLength() / massRange.getMassRange()) * (mass - massRange.getMinMass()));
                double yCordMax = plotSize.yY1 - (plotSize.yAxisLength / 100) * relInt * zoomFactor;
                String label = "";
                comp.addPeak(xCord, plotSize.xY, xCord, yCordMax, label, color);
                colChanger++;
            }
        }
        comp.redraw();
    }


}







