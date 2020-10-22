//This class handles TMTProC quantification
//in Principle, the old SOT-stuff could be adjusted, but feels very clunky and awkward to use, so create new one instead
//handle TMTProC cluster detection & quantification

import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TMTProC {
    //class to hold the general info about the PSM
    private static class PSMInfo {
        private int scanF;
        private String reference;
        private int allowedDeviation;
        private boolean isTMTPro0; //true: TMTPro0; false: TMTProC 8plex
        private int precursorZ;
        private Peptide precursor;
        private SumFormula precursorFormula; //with added protons according to charge state
        private Ion precursorIon;
        private double isolatedMZ;
        private int precursorIsoOffset;
        private double precursorSN;
        private double ms1InjectionTime;
        private double msnInjectionTime;

        PSMInfo(int scanFIn, int zIn, String referenceIn, Peptide peptideIn, int allowedDeviationIn, boolean tmtPro0In, double isolatedMzIn, double precursorSNIn, double ms1ITIn, double msnITIn) {
            this.scanF = scanFIn;
            this.reference = referenceIn;
            this.allowedDeviation = allowedDeviationIn;
            this.isTMTPro0 = tmtPro0In;
            this.precursorZ = zIn;
            this.precursor = peptideIn;
            //add protons to precursor according to z
            this.precursorFormula = this.precursor.getSumFormula();
            for (int i = 0; i < this.precursorZ; i++) {
                this.precursorFormula = SumFormula.sumFormulaJoiner(this.precursorFormula, SumFormula.getProtonFormula());
            }
            this.precursorIon = new Ion(this.precursorFormula, this.precursorZ);
            //determine precursorIsoOffset
            this.isolatedMZ = isolatedMzIn;
            this.precursorIsoOffset = RandomTools.determineIsolationOffset(this.precursorIon.getMToZ(), isolatedMzIn, allowedDeviationIn, zIn);
            this.precursorSN = precursorSNIn;
            this.ms1InjectionTime = ms1ITIn;
            this.msnInjectionTime = msnITIn;
        }

        public int getScanF() {
            return scanF;
        }

        public String getReference() {
            return reference;
        }

        public int getAllowedDeviation() {
            return allowedDeviation;
        }

        public boolean isTMTPro0() {
            return isTMTPro0;
        }

        public int getPrecursorZ() {
            return precursorZ;
        }

        public Peptide getPrecursor() {
            return precursor;
        }

        public SumFormula getPrecursorFormula() {
            return precursorFormula;
        }

        public Ion getPrecursorIon() {
            return precursorIon;
        }

        public double getIsolatedMZ() {
            return isolatedMZ;
        }

        public int getPrecursorIsoOffset() {
            return precursorIsoOffset;
        }

        public double getPrecursorSN() {
            return precursorSN;
        }

        public double getMs1InjectionTime() {
            return ms1InjectionTime;
        }

        public double getMsnInjectionTime() {
            return msnInjectionTime;
        }
    }


    //this class identifies TMTProC complementary clusters
    //requires: Allosaurus mzXML run File, Peptide list with scan number
    //TODO: only TMTpro0 supported for now, change for TMTproC 8plex
    public static void tmtproCCLusterID(String runFileLocIn, String idListIn, int allowedPPMDeviance, boolean tmtPro0, String alkylationIn) throws MzXMLParsingException, FileNotFoundException {
        ArrayList<String> allowedCysProt = new ArrayList<>();
        allowedCysProt.add("NEM");
        allowedCysProt.add("IAA");
        if (!allowedCysProt.contains(alkylationIn))
            throw new IllegalArgumentException("Alkylation method not recognized. Please use NEM or IAA. Input: " + alkylationIn);
        //prepare access to .mzxml file
        File runFile = new File(runFileLocIn);
        MzXMLFile run = new MzXMLFile(runFile);

        //read ID list
        File idFile = new File(idListIn);
        Scanner scanner = null;
        try {
            scanner = new Scanner(idFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! Location: " + idFile.getAbsolutePath());
        }
        //variable to see how many PSMs are in a file
        int numberOfPSMs = 0;
        assert scanner != null;
        while (scanner.hasNext()) {
            scanner.next();
            numberOfPSMs++;
        }
        //reset scanner by reconstruction
        scanner = new Scanner(idFile);
        //skip first line, where the header is
        //relevant positions: [0] is ScanF, [1] is precursor z, [4] is peptide, [3] is reference, but detect dynamically
        String header = scanner.nextLine();
        String[] headerCaptions = header.split(",");
        HashMap<String, Integer> captionPositions = new HashMap<>();
        int index = 0;
        for (String captions : headerCaptions) {
            switch (captions) {
                case "ScanF":
                    captionPositions.put("ScanF", index);
                    break;
                case "z":
                    captionPositions.put("z", index);
                    break;
                case "Peptide":
                    captionPositions.put("Peptide", index);
                    break;
                case "Reference":
                    captionPositions.put("Reference", index);
                    break;
            }
            index++;
        }
        if (captionPositions.size() != 4)
            throw new IllegalArgumentException("Something went wrong with reading in the caption positions of the allosaurus results file!");


        //prepare output file
        //output file should act as container: store all complementary ions in there
        //******************************************************//
        //*********************format of file*******************//
        //regex to split individual entries: §
        //regex to split fields: <........>
        //regex to split individual properties: #
        //regex to split ion properties: ~
        //§<PSMINFO >[-10]ScanF[0]#PrecursorMass[1]#PrecursorZ[2]#PrecursorIsoOffset[3]#Peptide[4]#Reference[5]#AllowedDeviation[6]#TMTPro0(true/false; if false, 8 channels TMTPro)
        //<COMPIONS>[-10]Sumformula[0]~detectedMass[1]~detectedRawInt[2]~detectedS/N[3]~tmtAdded[4]~formationEfficiency[5]#next ion
        //<COMPFRAG>[-10]Sumformula[0]~detectedMass[1]~detectedRawInt[2]~detectedS/N[3]~fragion(b4, y3,...)[4]~tmtAdded[5]~mixed(cleavage of multiple TMTs, false or true)[6]~formationEfficiency[7]#next ion
        //******************************************************//
        String newFilePath = idFile.getAbsolutePath().replace(".csv", "") + "_CompIons.txt";
        File outputCSV = new File(newFilePath);
        PrintWriter resultWriter = new PrintWriter(outputCSV);

        //generate ScanLists for iteration through MS-scans
        ScanLists scanList = new ScanLists(run);

        //initialize progress bar
        ProgressBar.progressBar("Finding complementary (fragment) ions.", numberOfPSMs);
        int detectedMatches = 0;

        //loop through Scanner
        while (scanner.hasNext()) {
            String[] currentValues = scanner.next().split(",");
            //generate Peptide with all modifications
            //this sequence has . in between, remove them
            //remove trailing dots by using method in RandomTools
            String peptideSequence = RandomTools.sequenceOnly(currentValues[captionPositions.get("Peptide")]);
            //prepare modification of precursor peptide - this List doesn't include the TMTpro modification
            ArrayList<Modification> modList = new ArrayList<>();
            //check if sequence has Cysteines which need to be protected
            if (peptideSequence.contains("C")) {
                if (alkylationIn.equals("IAA"))
                    modList.add(Modification.carbamidomethylation());
                else
                    modList.add(Modification.nemModification());
            }
            //check for presence of variable ox.
            if (peptideSequence.contains("*")) {
                modList.addAll(RandomTools.oxidizedMethionineModification(peptideSequence));
                peptideSequence = RandomTools.removeOxidationSigns(peptideSequence);
            }
            //modlist now contains alkylation and oxidation, last thing to add is TMTPro-mod
            //copy to new List to add TMTPro modification
            ArrayList<Modification> modWithTMTList = new ArrayList<>(modList);

            if (tmtPro0) {
                modWithTMTList.add(Modification.tmtPro0());
                //1 is for N-Terminus
                modWithTMTList.add(Modification.tmtPro0(1));
            } else {
                modWithTMTList.add(Modification.tmtPro());
                //1 is for N-Terminus
                modWithTMTList.add(Modification.tmtPro(1));
            }
            Peptide unmodPeptide = new Peptide(peptideSequence, AminoAcid.getAminoAcidList());
            Peptide modPeptideTMTUncleaved = unmodPeptide.peptideModifier(modWithTMTList);

            //get Scan information for PSMInfo: Precursor SN, MS1 IT, MSn IT
            int currentScanNumber = Integer.parseInt(currentValues[captionPositions.get("ScanF")]);
            int previousMS1ScanNumber = ScanLists.getPreviousMS1ScanNumber(scanList, currentScanNumber);
            //load Scans to extract info
            Scan precursorScan = run.getScanByNum((long) previousMS1ScanNumber);
            Scan msNScan = run.getScanByNum((long) currentScanNumber);
            double ms1IT = precursorScan.getIonInjectionTime();
            double msnIT = msNScan.getIonInjectionTime();
            //for precursor SN, transform to MySpectrum
            MySpectrum precursorSpectrum;
            try {
                precursorSpectrum = MzXMLReadIn.mzXMLToMySpectrum(run, Integer.toString(previousMS1ScanNumber));
            } catch (JMzReaderException e) {
                throw new IllegalArgumentException("Spectrum couldn't be found! ScanNumber: " + previousMS1ScanNumber);
            }
            //figure out precursor
            if (msNScan.getMsLevel() == (long) 1)
                throw new IllegalArgumentException("ScanF was pointing to MS1 scan, no MSn scan!");
            double isoPrecursorMZ = msNScan.getPrecursorMz().get(0).getValue();
            //get precursor Peak and read out SN
            if (!precursorSpectrum.isNoisePresent())
                throw new IllegalArgumentException("No noise band present in .mzXML file!");
            double precursorSN = 0;
            Peak precursorPeak = precursorSpectrum.getMatchingPeak(isoPrecursorMZ, allowedPPMDeviance);
            if (precursorPeak != null)
                precursorSN = precursorPeak.getSignalToNoise();
            int precursorZ = Integer.parseInt(currentValues[captionPositions.get("z")]);
            PSMInfo psmInfo = new PSMInfo(currentScanNumber, precursorZ, currentValues[captionPositions.get("Reference")],
                    modPeptideTMTUncleaved, allowedPPMDeviance, tmtPro0, isoPrecursorMZ, precursorSN, ms1IT, msnIT);

            //generate MS2 spectra objects to search through
            MySpectrum msnSpectrum;
            try {
                msnSpectrum = MzXMLReadIn.mzXMLToMySpectrum(run, currentScanNumber);
            } catch (JMzReaderException e) {
                throw new IllegalArgumentException("Spectrum couldn't be found! ScanNumber: " + previousMS1ScanNumber);
            }
            //next, create list of all theoretical complementary ions which have at least 1 cleaved TMTpro tag and are non redundant
            //this includes all full length ions & fragment ions
            //todo: call method which returns the non redundant list of comp ions
            ArrayList<TMTproCCompIon> compIonsList = new ArrayList<>();


            //at the end, advance progress bar
            ProgressBar.progress();
        }
        //todo: just for testing
        ProgressBar.close();
        System.out.println("Number of MS2 spectra: " + scanList.getNumberMS2());
        System.out.println("Number of PSMs: " + numberOfPSMs);
        System.out.println("Number of detected comp. Ions: " + detectedMatches);

    }



    //TODO: update and finish this
    private static String compContainerStringProducer() {
        //output file should act as container: store all complementary ions in there
        //******************************************************//
        //*********************format of file*******************//
        //regex to split individual entries: §
        //regex to split fields: <........>
        //regex to split individual properties: #
        //regex to split ion properties: ~
        //§<PSMINFO >[-10]ScanF[0]#PrecursorMass[1]#PrecursorZ[2]#PrecursorIsoOffset[3]#Peptide[4]#Reference[5]#AllowedDeviation[6]#TMTPro0(true/false; if false, 8 channels TMTPro)
        //<COMPIONS>[-10]Sumformula[0]~detectedMass[1]~detectedRawInt[2]~detectedS/N[3]~tmtAdded[4]~formationEfficiency[5]#next ion
        //<COMPFRAG>[-10]Sumformula[0]~detectedMass[1]~detectedRawInt[2]~detectedS/N[3]~fragion(b4, y3,...)[4]~tmtAdded[5]~mixed(cleavage of multiple TMTs, false or true)[6]~formationEfficiency[7]#next ion
        //******************************************************//
        StringBuilder sb = new StringBuilder();
        //new entry
        sb.append("§").append("<PSMINFO >");

        return sb.toString();
    }


}
