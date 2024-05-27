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
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.MappedByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class TMTProC {
    private static DecimalFormat twoDec = new DecimalFormat("0.00");
    private static DecimalFormat fiveDec = new DecimalFormat("0.00000");
    //class to hold the general info about the PSM
    private static class PSMInfo {
        private int scanF;
        private String reference;
        private int allowedDeviation;
        private boolean isTMTPro0; //true: TMTPro0; false: TMTProC 8plex
        private Peptide precursor;
        private SumFormula precursorFormula; //with added protons according to charge state
        private Ion precursorIon;
        private double isolatedMZ;
        private int precursorIsoOffset;
        private double precursorSN;
        private double ms1InjectionTime;
        private double msnInjectionTime;
        private ArrayList<Modification> otherModifications;
        private String alkylationType;

        PSMInfo(int scanFIn, int zIn, String referenceIn, Peptide peptideIn, int allowedDeviationIn, boolean tmtPro0In,
                double isolatedMzIn, double precursorSNIn, double ms1ITIn, double msnITIn, ArrayList<Modification> modListIn, String alkylationTypeIn) {
            this.scanF = scanFIn;
            this.reference = referenceIn;
            this.allowedDeviation = allowedDeviationIn;
            this.isTMTPro0 = tmtPro0In;
            this.precursor = peptideIn;
            //add protons to precursor according to z
            this.precursorFormula = this.precursor.getSumFormula();
            for (int i = 0; i < zIn; i++) {
                this.precursorFormula = SumFormula.sumFormulaJoiner(this.precursorFormula, SumFormula.getProtonFormula());
            }
            this.precursorIon = new Ion(this.precursorFormula, zIn);
            //determine precursorIsoOffset
            this.isolatedMZ = isolatedMzIn;
            this.precursorIsoOffset = RandomTools.determineIsolationOffset(this.precursorIon.getMToZ(), isolatedMzIn, allowedDeviationIn, zIn);
            this.precursorSN = precursorSNIn;
            this.ms1InjectionTime = ms1ITIn;
            this.msnInjectionTime = msnITIn;
            this.otherModifications = modListIn;
            this.alkylationType = alkylationTypeIn;
        }
    }

    //this class identifies TMTProC complementary clusters
    //requires: Allosaurus mzXML run File, Peptide list with scan number
    //TODO: only TMTpro0 supported for now, change for TMTproC 8plex
    public static void tmtproCCLusterID(String runFileLocIn, String idListIn, int allowedPPMDeviance, boolean tmtPro0, String alkylationIn) throws MzXMLParsingException, FileNotFoundException, JMzReaderException {
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

        String newFilePath = idFile.getAbsolutePath().replace(".csv", "") + "_CompIons_maxCompFragAnalysis.txt";
        File outputCSV = new File(newFilePath);
        PrintWriter resultWriter = new PrintWriter(outputCSV);
        //header for interim data analysis
        resultWriter.write("Scan-#,max comp. frag. ion m/z,max comp. frag. ion efficency [%],max comp. frag. ion SN \n");
        resultWriter.flush();
        //generate ScanLists for iteration through MS-scans
        ScanLists scanList = new ScanLists(run);

        //initialize progress bar
        ProgressBar.progressBar("Finding complementary (fragment) ions.", numberOfPSMs);
        int detectedMatches = 0;
        int detectedNonIntactMatches = 0;
        int detectedIntactMatches = 0;

        ArrayList<Double> compIonFormationEfficiency = new ArrayList<>();
        ArrayList<Double> compFragIonFormationEfficiency = new ArrayList<>();

        //loop through Scanner
        while (scanner.hasNext()) {

            String[] currentValues = scanner.next().split(",");
            //generate Peptide with all modifications
            //this sequence has . in between, remove them
            //remove trailing dots by using method in RandomTools
            String peptideSequence = RandomTools.sequenceOnly(currentValues[captionPositions.get("Peptide")]);
            //sometimes, X is in the sequence of the allosaurus output...if so, skip
            if (peptideSequence.contains("X"))
                continue;

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


            //TODO: to fix things, get collision energies. This IS WRONG, but fixing one thing at a time
            //double ms1IT = precursorScan.getIonInjectionTime();
            //double msnIT = msNScan.getIonInjectionTime();
            double ms1IT = precursorScan.getBasePeakIntensity();
            double msnIT = msNScan.getBasePeakIntensity();
            //for precursor SN, transform to MySpectrum
            MySpectrum precursorSpectrum;
            try {
                precursorSpectrum = MzXMLReadIn.mzXMLToMySpectrum(run, Integer.toString(previousMS1ScanNumber));
            } catch (JMzReaderException e) {
                throw new IllegalArgumentException("Spectrum couldn't be read in! ScanNumber: " + previousMS1ScanNumber);
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

            //create PSMInfo object storing all this information
            PSMInfo psmInfo = new PSMInfo(currentScanNumber, precursorZ, currentValues[captionPositions.get("Reference")],
                    modPeptideTMTUncleaved, allowedPPMDeviance, tmtPro0, isoPrecursorMZ, precursorSN, ms1IT, msnIT, modList, alkylationIn);

            //generate MS2 spectra objects to search through
            MySpectrum msnSpectrum;
            try {
                msnSpectrum = MzXMLReadIn.mzXMLToMySpectrum(run, currentScanNumber);
            } catch (JMzReaderException e) {
                throw new IllegalArgumentException("Spectrum couldn't be found! ScanNumber: " + previousMS1ScanNumber);
            }

            //next, create list of all theoretical complementary ions which have at least 1 cleaved TMTpro tag and are non redundant
            //this includes all full length ions & fragment ions
            ArrayList<TMTproCCompIon> compIonsList = TMTproCCompIon.compIonCreator(unmodPeptide, modList, tmtPro0, precursorZ);
            //check msn spectrum for the ions
            //if match, add to list of IonMatches
            ArrayList<IonMatch> ionMatches = new ArrayList<>();
            for (TMTproCCompIon compIon : compIonsList){
                //TODO: check if it's a problem that getMatchingPeak returns reference and not new peak
                Peak potentialMatchedPeak = msnSpectrum.getMatchingPeak(compIon.getIon().getMToZ(), allowedPPMDeviance);
                if(potentialMatchedPeak != null){
                    ionMatches.add(new IonMatch(compIon, potentialMatchedPeak));
                    detectedMatches++;
                    if(!compIon.isFullLength())
                        detectedNonIntactMatches++;
                    else {
                        detectedIntactMatches++;

                    }
                }
            }

            //ion matches is populated, calculate efficiencies
            //this is to determine the highest Comp Frag ion efficiency per spectrum
            double highestCompFragEfficiency = 0;
            double maxCompIonmz = 0;
            double maxSNCompFrag = 0;
            double efficiency = 0;

            for (IonMatch match : ionMatches){

                //do individually for full comp Ions and fragment ions
                if(match.getTmtProCCompIon().isFullLength()){
                    compIonFormationEfficiency.add(formationEfficiencyCalculator(ms1IT, msnIT, precursorSN, match.getPeak().getSignalToNoise(), precursorZ, match.getTmtProCCompIon().getIon().getCharge() ));
                }
                else
                    efficiency = formationEfficiencyCalculator(ms1IT, msnIT, precursorSN, match.getPeak().getSignalToNoise(), precursorZ, match.getTmtProCCompIon().getIon().getCharge());
                    compFragIonFormationEfficiency.add(efficiency);
                    double currentSN = match.getPeak().getSignalToNoise();
                    if(efficiency > highestCompFragEfficiency)
                        highestCompFragEfficiency = efficiency;
                    if(currentSN > maxSNCompFrag) {
                        maxSNCompFrag = currentSN;
                        maxCompIonmz = match.getPeak().getMass();
                    }
            }



            //output to file
            //intitally, used qsm string producer, but interested in comp ions and comp fragment ions
            //String output = qsmStringProducer(psmInfo, ionMatches);

            resultWriter.write(msnSpectrum.getScanNumber()+",");
            resultWriter.write(maxCompIonmz+",");
            resultWriter.write(highestCompFragEfficiency*100+",");
            resultWriter.write(maxSNCompFrag + "\n");
            resultWriter.flush();
            //at the end, advance progress bar
            ProgressBar.progress();
        }

        ProgressBar.close();
        resultWriter.flush();
        resultWriter.close();

        //todo: just for testing
        System.out.println("Number of MS2 spectra: " + scanList.getNumberMS2());
        System.out.println("Number of PSMs: " + numberOfPSMs);
        System.out.println("Number of detected comp. ions: " + detectedMatches);
        System.out.println("Number of detected comp.  ions: " + detectedIntactMatches);
        System.out.println("Number of detected comp. fragment ions: " + detectedNonIntactMatches);

        //calculate median efficiencies
        double sumIntact = 0;
        double sumFragment = 0;
        for (double eff : compIonFormationEfficiency){
            sumIntact += eff;
        }
        for (double eff : compFragIonFormationEfficiency){
            sumFragment += eff;
        }

        double meanIntact = sumIntact/compIonFormationEfficiency.size();
        double meanFrag = sumFragment/compFragIonFormationEfficiency.size();
        double medianIntact = RandomTools.calculateMedian(compIonFormationEfficiency);
        double medianFrag = RandomTools.calculateMedian(compFragIonFormationEfficiency);

        System.out.println("");


        System.out.println("Comp. ions mean formation efficiency: "+twoDec.format(meanIntact*100));
        System.out.println("Comp. ions median formation efficiency: "+twoDec.format(medianIntact*100));
        System.out.println("Comp. fragment ions mean formation efficiency: "+twoDec.format(meanFrag*100));
        System.out.println("Comp. fragment ions median formation efficiency: "+twoDec.format(medianFrag*100));





    }

    //generate inclusion list for MS3 based comp fragment ion generation
    //TODO: only handles TMTpro0 right now
    public static void targetedMS3ListGenerator(String sequenceLocationIn, String cysProtectionIn) throws IOException {
        //create output file
        String outputPath = sequenceLocationIn.replace(".fasta", "_MS3InclusionList.csv");
        File output = new File(outputPath);
        PrintWriter csvWriter;
        try{
            csvWriter = new PrintWriter(output);
        }
        catch (FileNotFoundException e){
            throw new FileNotFoundException("File not found! Filepath: "+outputPath);
        }
        StringBuilder sb = new StringBuilder();
        //write header
        csvWriter.write("Sequence,Precursor m/z,Precursor z,Comp. ion m/z,Comp. ion z\n");

        //generate peptide list
        ArrayList<String> pepSequenceList =  FastaTools.digestFasta(sequenceLocationIn, 0, "Trypsin");
        //generate Peptides and modify them accordingly
        //only care if peptide is between 6 and 30 AAs
        ArrayList<Peptide> unmodPepList = new ArrayList<>();
        for(String seq : pepSequenceList){
            if(seq.length()<6 || seq.length() > 30)
                continue;
            //skip weird X in FASTAs
            if(seq.contains("X"))
                continue;
            unmodPepList.add(new Peptide(seq, AminoAcid.getAminoAcidList()));
        }
        //handle static alkylation
        ArrayList<Modification> alkylationList = new ArrayList<>();
        if(cysProtectionIn.equals("IAA"))
            alkylationList.add(Modification.carbamidomethylation());
        else
            alkylationList.add(Modification.nemModification());
        //loop through unmod peptides and handle intact peptide & full length comp ion
        int handledPeptides = 0;
        for (Peptide pep : unmodPepList){
            //create intact peptide precursor
            ArrayList<Modification> intactModList = new ArrayList<>(alkylationList);
            //this is all lys
            intactModList.add(Modification.tmtPro0());
            //this is N-term
            intactModList.add(Modification.tmtPro0(1));
            Peptide intactPrecursor = pep.peptideModifier(intactModList);
            //determine likely charge state and add protons to sum formula
            int likelyChargeState = pep.getLikelyChargeState();
            SumFormula intactFormula = intactPrecursor.getSumFormula();
            for (int i = 0; i < likelyChargeState; i++){
                intactFormula = SumFormula.sumFormulaJoiner(SumFormula.getProtonFormula(), intactFormula);
            }
            Ion precursorIon = new Ion(intactFormula);
            //check if precursorIon is z=1, happens if end of FASTA is reached. skip
            if(precursorIon.getCharge() == 1)
                continue;
            //next, handle comp. ion
            //only interested in 1 cleaved tag: add cleaved tag to N-term and modify all lysines
            ArrayList<Modification> cleavedModList = new ArrayList<>(alkylationList);
            cleavedModList.add(Modification.tmtPro0Comp(1));
            cleavedModList.add(Modification.tmtPro0());
            Peptide compPeptide = pep.peptideModifier(cleavedModList);
            SumFormula cleavedFormula = compPeptide.getSumFormula();
            for (int i = 0; i < likelyChargeState; i++){
                cleavedFormula = SumFormula.sumFormulaJoiner(SumFormula.getProtonFormula(), cleavedFormula);
            }
            Ion compIon = new Ion(cleavedFormula);

            sb.append(intactPrecursor.getSequence()).append(",");
            sb.append(fiveDec.format(precursorIon.getMToZ())).append(",");
            sb.append(precursorIon.getCharge()).append(",");
            sb.append(fiveDec.format(compIon.getMToZ())).append(",");
            sb.append(compIon.getCharge()).append("\n");

            csvWriter.write(sb.toString());
            csvWriter.flush();
            sb.setLength(0);
            handledPeptides++;
        }
        csvWriter.close();
        System.out.println("Creation of Inclusion list complete! "+handledPeptides+" precursors added!");
    }



    //TODO: test
    //QSM: quantitative spectral match
    private static String qsmStringProducer(PSMInfo psmInfoIn, ArrayList<IonMatch> matchesIn) {
        //output file should act as container: store all complementary ions in there
        //******************************************************//
        //*********************format of file*******************//
        //regex to split individual entries: §
        //regex to split fields: <........>
        //regex to split individual properties: #
        //regex to split ion properties: ~
        //§<PSMINFO >[-10]ScanF[0]~PrecursorMZ[1]~PrecursorZ[2]~PrecursorIsoOffset[3]~Isolated MZ[4]~Peptide[5]~Reference[6]~AllowedDeviation[7]~TMTPro0(true/false; if false, 8 channels TMTPro)
        //<MODLIST >[-10]modName[0]~modFormula[1]~modPos[2]#nextMod
        //<COMPIONS>[-10]Sumformula[0]~detectedMass[1]~detectedRawInt[2]~detectedS/N[3]~tmtAdded[4]~formationEfficiency[5]#next ion
        //<COMPFRAG>[-10]Sumformula[0]~detectedMass[1]~detectedRawInt[2]~detectedS/N[3]~fragion(b4, y3,...)[4]~tmtAdded[5]~mixed(cleavage of multiple TMTs, false or true)[6]~formationEfficiency[7]#next ion
        //******************************************************//
        StringBuilder sb = new StringBuilder();
        //new entry
        //PSMInfo part
        sb.append("§").append("<PSMINFO >");
        sb.append(psmInfoIn.scanF).append("~"); //ScanF [0]
        sb.append(psmInfoIn.precursor.getSequence()).append("~"); //peptide sequence [1]
        sb.append(psmInfoIn.reference).append("~"); //reference [2]
        sb.append(psmInfoIn.alkylationType).append("~"); //NEM or IAA [3]
        sb.append(psmInfoIn.isTMTPro0).append("~"); //tmtpro0 [4] true: yes, false: TMTproC 8plex
        sb.append(psmInfoIn.allowedDeviation).append("~"); //allowed ppm deviation [5]
        sb.append(psmInfoIn.precursorIon.getMToZ()).append("~"); //theoretical precursor mz [6]
        sb.append(psmInfoIn.precursorIon.getCharge()).append("~"); //precursor charge [7]
        sb.append(psmInfoIn.precursorIsoOffset).append("~"); //precursor iso offset [8]
        sb.append(psmInfoIn.isolatedMZ).append("~"); //isolated mz [9]
        sb.append(psmInfoIn.precursorSN).append("~"); //precursor SN [10]
        sb.append(psmInfoIn.ms1InjectionTime).append("~"); // MS1 IT [11]
        sb.append(psmInfoIn.msnInjectionTime).append("~"); // MSn IT [12]
        // modlist part
        sb.append("<MODLIST >");
        String modSep = "";
        for(Modification mod : psmInfoIn.otherModifications){
            //skip alkylation, unimportant
            if(mod.getModificationName().equals("IAA")||mod.getModificationName().equals("NEM"))
                continue;
            sb.append(mod.getModificationName()).append("~"); //[0]
            sb.append(mod.getModificationFormula().getSumFormula()).append("~"); //[1]
            sb.append(mod.getPositionNumber()).append("~"); // [2]
            sb.append(modSep);
            modSep = "#";
        }
        //split matchesList in comp. ions and comp fragment ions
        ArrayList<IonMatch> compIons = new ArrayList<>();
        ArrayList<IonMatch> compFragIons = new ArrayList<>();
        for (IonMatch match : matchesIn){
            if(match.isFullLengthIon())
                compIons.add(match);
            else
                compFragIons.add(match);
        }
        // comp ion part
        sb.append("<COMPIONS>");
        String compSep = "";
        for(IonMatch match : compIons){
            sb.append(match.getTmtProCCompIon().getIon().getFormula().getSumFormula()).append("~");//sum formula
            sb.append(match.getTmtProCCompIon().getIon().getCharge()).append("~");//charge state
            sb.append(match.getTmtProCCompIon().getNumberOfCleavedLabels()).append("~");//number of cleaved Labels. 1 = classical TMTProIon
            sb.append(match.getPeak().getMass()).append("~");//detected m/z
            sb.append(match.getPeak().getIntensity()).append("~");//detected raw intensity of peak, and of peak alone! not of feature int! important with TMTproC 8plex
            sb.append(match.getPeak().getSignalToNoise()).append("~");//detected SN value of peak
            //which TMTtag is added - TMT0 or which specific channel?
            //note it might be better to separate TMTPro0 and TMTProC for now...with TMTProC, a complementary ion cluster object might be better
            //efficiency Calculation
            double formationEfficiency = formationEfficiencyCalculator(psmInfoIn.ms1InjectionTime, psmInfoIn.msnInjectionTime, psmInfoIn.precursorSN, match.getPeak().getSignalToNoise(),
                    psmInfoIn.precursorIon.getCharge(),match.getTmtProCCompIon().getIon().getCharge());
            sb.append(fiveDec.format(formationEfficiency)).append(compSep);
            compSep = "#";
        }
        // comp frag ion part
        sb.append("<COMPFRAG>");
        String compFragSep = "";
        for(IonMatch match : compFragIons){
            sb.append(match.getFragmentIon().getFormula()).append("~");//sum formula
            sb.append(match.getFragmentIon().getCharge()).append("~");
            sb.append(match.getFragmentIon().getNumberOfCleavedLabels()).append("~");//number of cleaved labels
            sb.append(match.getFragmentIon().getIonSeries()+match.getFragmentIon().getIonNumber()).append("~");//ion series
            sb.append(match.getPeak().getMass()).append("~");//detected m/z
            sb.append(match.getPeak().getIntensity()).append("~");//detected raw intensity of peak, and of peak alone! not of feature int! important with TMTproC 8plex
            sb.append(match.getPeak().getSignalToNoise()).append("~");//detected SN value of peak
            //which TMTtag is added - TMT0 or which specific channel?
            //note it might be better to separate TMTPro0 and TMTProC for now...with TMTProC, a complementary ion cluster object might be better
            //efficiency Calculation
            double formationEfficiency = formationEfficiencyCalculator(psmInfoIn.ms1InjectionTime, psmInfoIn.msnInjectionTime, psmInfoIn.precursorSN, match.getPeak().getSignalToNoise(),
                    psmInfoIn.precursorIon.getCharge(),match.getFragmentIon().getCharge());
            sb.append(fiveDec.format(formationEfficiency)).append(compFragSep);
            compFragSep = "#";
        }
        sb.append("\n");
        return sb.toString();
    }



    public static double formationEfficiencyCalculator(double ms1IT, double msnIT, double ms1SN, double msnSN, int precursorZ, int ionZ){
        //TODO: adjust values for the different resolutions
        //values out of BACIQ paper as discussed with Alex J.
        final double MS1ConversionFactor = 1.3;
        final double MSnConversionFactor = 2.1;
        double counter = ((msnSN * MSnConversionFactor)/msnIT)/ionZ;
        double denominator = ((ms1SN * MS1ConversionFactor)/ms1IT)/precursorZ;
        if(denominator == 0)
            return 0;
        return counter/denominator;
    }


}
