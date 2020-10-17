import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.IOException;
        import java.io.PrintWriter;
        import java.lang.reflect.Array;
        import java.nio.file.Path;
        import java.text.DecimalFormat;
        import java.util.ArrayList;
        import java.nio.file.Files;
        import java.util.Arrays;
        import java.util.HashSet;
        import java.util.Set;


/**
 * Created by Michael Stadlmeier on 6/11/2018.
 * This class should be able to enzymatically digest protein .FASTA-files
 */
public class FastaTools {
    public static DecimalFormat fourDec = new DecimalFormat("0.0000");

    public static ArrayList<String> digestSequence(String seqIn, String proteaseIn, int missedCleavagesIn) {
        ArrayList<String> peptideListOut = new ArrayList<>();

        //add int variable to track number of generated peptides
        int generatedPeptides = 0;

        //add size variable
        int sizeOfFasta = seqIn.length();

        //set amino acids after which sequence is cut
        ArrayList<String> cutAA = new ArrayList<>();
        switch (proteaseIn) {
            case "Trypsin":
                cutAA.add("K");
                cutAA.add("R");
                break;
            case "Test":
                cutAA.add("A");
                break;

            default:
                throw new IllegalArgumentException("cut AAs not set! Protease entered: " + proteaseIn);
        }

        //Put .FASTA String into list of individual Letters
        String[] allIndividualAAs = new String[sizeOfFasta];
        for (int a = 0; a < sizeOfFasta; a++) {
            allIndividualAAs[a] = Character.toString(seqIn.charAt(a));
        }
        //now check all the letters and see if it should have been cut. Save all the sequences for the peptides in new String ArrayList
        ArrayList<String> cutSequences = new ArrayList<>();
        int lastCutPosition = -1;
        int missedCounter = 0;
        int lastMissedPosition = 0;
        StringBuilder sb = new StringBuilder();
        //additional for loop required for all the different peptides with missed cleavages

        //make missed cleavages work
        for (int m = 0; m < missedCleavagesIn + 1; m++) {
            for (int a = 0; a < sizeOfFasta; a++) {
                //loop through all the AAs to be cut
                //if this occurs, a cut was detected
                //create new Sequence if no missed cleavages shall occur
                if (cutAA.contains(allIndividualAAs[a])) {
                    if (missedCounter < m) {
                        missedCounter++;
                        lastMissedPosition = a;
                        continue;
                    }
                    //let P inhibit cut!
                    if (proteaseIn.equals("Trypsin")) {
                        if (a < sizeOfFasta - 1) {
                            if (allIndividualAAs[a + 1].equals("P")) {
                                continue;
                            }
                        }
                    }

                    //empty sb; maybe unnecessary
                    sb.setLength(0);
                    //add all the letters to the Stringbuilder and put them into a String
                    for (int b = lastCutPosition + 1; b < a + 1; b++) {
                        sb.append(allIndividualAAs[b]);
                    }
                    //add the generated String to the ArrayList
                    //only do this if it's not already part of the list
                    if (!cutSequences.contains(sb.toString())) {
                        cutSequences.add(sb.toString());
                        generatedPeptides++;
                    }
                    //set all the variables correctly
                    if (m != 0) {
                        a = lastMissedPosition;
                    }
                    lastCutPosition = a;
                    missedCounter = 0;
                }

                //handle last peptide
                if (a == sizeOfFasta - 1) {
                    //if only missed cleavages are concerned, unnecessary to put out last peptide again
                    if (missedCounter < m)
                        continue;
                    sb.setLength(0);
                    for (int b = lastCutPosition + 1; b < a + 1; b++) {
                        sb.append(allIndividualAAs[b]);
                    }
                    //only do this if sb isn't empty
                    if (sb.length() != 0) {
                        cutSequences.add(sb.toString());
                        generatedPeptides++;
                    }
                }

            }
            lastCutPosition = -1;
            missedCounter = 0;
        }


        //System.out.println("Number of generated Peptides: "+generatedPeptides);
        peptideListOut.addAll(cutSequences);
        return peptideListOut;
    }

    //this class takes a complete .FASTA file in uniprot format (each sequence starting with >), converts them in individual sequences and then creates a Arraylist of Strings with the peptide sequences
    public static ArrayList<String> digestFasta(String fastaLocationIn, int maxAllowedMissedCleavages, String proteaseIn) throws IOException {
        long startTime = System.currentTimeMillis();
        ArrayList<String> out = new ArrayList<>();
        //read complete contents of Fasta File into String
        Path filePath = Path.of(fastaLocationIn);
        String contents = Files.readString(filePath);

        //split String in substrings according to >, which denotes new sequence
        String[] individiualSeqs = contents.split(">");
        //first entry of this String array is empty because every sequence starts with >
        //remove the first entry
        individiualSeqs = Arrays.copyOfRange(individiualSeqs, 1, individiualSeqs.length);
        ArrayList<String> proteins = new ArrayList<>();
        //each individualSeq has the fasta header still attached and is split by \n
        //remove the first entry and combine all the remaining lines into one string
        //use StringBuilder to combine the individual lines
        StringBuilder sb = new StringBuilder();
        for (String seq : individiualSeqs) {
            //split seq with \n
            String[] splitSeq = seq.split("\n");
            //splitSeq must have at least header and one line of sequence. if not, skip
            if (splitSeq.length < 2)
                continue;
            //ignore first line (header), then add all the others together
            for (int i = 1; i < splitSeq.length; i++) {
                sb.append(splitSeq[i]);
            }
            //add complete sequence to proteins
            proteins.add(sb.toString());
            sb.setLength(0);
        }
        int numberOfProteins = proteins.size();
        System.out.println("Detected proteins in .FASTA: " + numberOfProteins);
        int handledProteins = 0;
        int numberOfPeptides = 0;


        //now, each protein sequence can be handeled by the sequence digester, and the individual peptides can be given out
        for (String protein : proteins) {
            ArrayList<String> digestedPeptides = digestSequence(protein, proteaseIn, maxAllowedMissedCleavages);
            out.addAll(digestedPeptides);
            numberOfPeptides += digestedPeptides.size();
            handledProteins++;
            System.out.println("Handled " + handledProteins + " out of " + numberOfProteins + " proteins.");

        }

        System.out.println("Analysis complete: " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds passed.");
        System.out.println("Number of generated peptides: " + numberOfPeptides);
        return out;
    }

    //generate an inclusion list for a specific .FASTA
    public static void inclusionListFabricator(String filePathIn, String proteaseIn, int maxMissedCleavagesIn) throws IOException {
        long startTime = System.currentTimeMillis();
        //create output .csv File
        String outputFilePath = filePathIn.replace(".fasta", "_inclusionList.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }

        //Header
        StringBuilder sb = new StringBuilder();
        sb.append("Peptide").append(",");
        sb.append("Sumformula").append(",");
        sb.append("m/z").append(",");
        sb.append("charge state").append("\n");
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);

        //generate the Arraylist of Strings with the peptide sequences
        ArrayList<String> peptideStringList = digestFasta(filePathIn, maxMissedCleavagesIn, proteaseIn);
        //start an ArrayList of Peptides with the modified Peptides in the correct charge state
        ArrayList<Peptide> peptideList = new ArrayList<>();
        ArrayList<Modification> modList = new ArrayList<>();
        modList.add(Modification.nemModification());

        for (String peptideString : peptideStringList) {
            long pepStartTime = System.currentTimeMillis();
            //determine most likely charge state
            //every peptide has N-Terminus
            int numberOfCharges = 1;
            for (int i = 0; i < peptideString.length(); i++) {
                //for K, R and H, increase charge state by 1
                if (peptideString.charAt(i) == 'K' || peptideString.charAt(i) == 'R' || peptideString.charAt(i) == 'H')
                    numberOfCharges++;
            }

            //create peptide
            Peptide peptide = new Peptide(peptideString, AminoAcid.getAminoAcidList());
            //add NEM mod; if no C is present, should not modify it; M-Oxidation is not taken into account, dynamic mod
            peptide = peptide.peptideModifier(modList);
            //check some parameters: peptide length between 6 and 28; mass to charge between 350 and 1750
            //skip if not suitable
            int pepLength = peptide.getSequenceLength();
            double approxMassToCharge = peptide.getExactMass() / numberOfCharges;
            if (pepLength < 6 || pepLength > 28)
                continue;
            if (numberOfCharges > 5 || numberOfCharges < 2)
                continue;
            if (approxMassToCharge < 350 || approxMassToCharge > 1750)
                continue;

            //if these filters don't continue to next peptide, add charges to sum formula
            SumFormula pepFormula = peptide.getSumFormula();
            //loop through this to add the charges
            for (int i = 0; i < numberOfCharges; i++) {
                pepFormula = SumFormula.sumFormulaJoiner(pepFormula, SumFormula.getProtonFormula());
            }
            //add all the info to the sb
            sb.append(peptide.getSequence()).append(",");
            sb.append(pepFormula.getSumFormula()).append(",");
            double massToCharge = pepFormula.getExactMass() / numberOfCharges;
            sb.append(fourDec.format(massToCharge)).append(",");
            sb.append(numberOfCharges).append("\n");

            //write sb to pw
            pw.write(sb.toString());
            pw.flush();
            sb.setLength(0);
            System.out.println("Analysis peptide: " + peptide.getSequence() + " in " + ((System.currentTimeMillis() - pepStartTime)) + " ms.");
        }
        pw.flush();
        pw.close();
        System.out.println("Analysis complete: " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds passed.");
    }

    //this class takes a complete .FASTA file in uniprot format (each sequence starting with >), and converts them into a HashSet digested sequences
    public static HashSet<String> generatePeptidesOfFasta(String fastaLocationIn, int maxAllowedMissedCleavages, String proteaseIn) throws IOException {
        long startTime = System.currentTimeMillis();
        //HashSet<String> uniquePeptideSequences = new HashSet<>();
        HashSet<String> allUniquePeptideSequences = new HashSet<>();
        //read complete contents of Fasta File into String
        Path filePath = Path.of(fastaLocationIn);
        String contents = Files.readString(filePath);
        System.out.println("Read-in .FASTA file in " + (System.currentTimeMillis() - startTime) + " ms");

        //split String in substrings according to >, which denotes new sequence
        String[] individiualSeqs = contents.split(">");
        //first entry of this String array is empty because every sequence starts with >
        //remove the first entry
        individiualSeqs = Arrays.copyOfRange(individiualSeqs, 1, individiualSeqs.length);
        ArrayList<String> proteins = new ArrayList<>();
        //each individualSeq has the fasta header still attached and is split by \n
        //remove the first entry and combine all the remaining lines into one string
        //use StringBuilder to combine the individual lines
        StringBuilder sb = new StringBuilder();
        //show Progress bar for the copying to Protein ArrayList
        ProgressBar.progressBar("Generating protein list", individiualSeqs.length);
        for (String seq : individiualSeqs) {
            //split seq with \n
            String[] splitSeq = seq.split("\n");
            //splitSeq must have at least header and one line of sequence. if not, skip
            if (splitSeq.length < 2)
                continue;
            //ignore first line (header), then add all the others together
            for (int i = 1; i < splitSeq.length; i++) {
                sb.append(splitSeq[i]);
            }
            //add complete sequence to proteins
            proteins.add(sb.toString());
            //advance the progress bar
            ProgressBar.progress();
            sb.setLength(0);
        }
        int numberOfProteins = proteins.size();
        System.out.println("Detected proteins in .FASTA: " + numberOfProteins);
        //close progress bar
        ProgressBar.close();
        int handledProteins = 0;
        int numberOfPeptides = 0;

        //now, each protein sequence can be handeled by the sequence digester, and the individual peptides can be added to the Set
        //duplicates won't be added
        ProgressBar.progressBar("Digesting Proteins", proteins.size());
        for (String protein : proteins) {
            ArrayList<String> digestedPeptides = digestSequence(protein, proteaseIn, maxAllowedMissedCleavages);
            allUniquePeptideSequences.addAll(digestedPeptides);
            numberOfPeptides += digestedPeptides.size();
            ProgressBar.progress();
        }
        ProgressBar.close();
        System.out.println("Peptide list created: " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds passed.");
        System.out.println("Number of generated peptides: " + numberOfPeptides);
        //return uniquePeptideSequences;
        return allUniquePeptideSequences;
    }

    //shows which peptides are unique in a .FASTA file
    public static void peptideUniquenessChecker(String queryInLoc, String fastaToCheckLoc, int allowedMissedCleavagesQuery, int allowedMissedCleavagesFasta, String proteaseIn) throws IOException {
        //prepare list of peptides to check
        Path filePath = Path.of(queryInLoc);
        String contents = Files.readString(filePath);
        //contents is header + sequence with \n in between. skip first line and add rest together with stringbuilder
        String[] splitSequence = contents.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < splitSequence.length; i++) {
            sb.append(splitSequence[i]);
        }
        String querySequence = sb.toString();
        //digest protein
        ArrayList<String> queryPeptides = digestSequence(querySequence, proteaseIn, allowedMissedCleavagesQuery);
        //now, prepare HashSet of the target FASTA file
        HashSet<String> allPeptides = generatePeptidesOfFasta(fastaToCheckLoc, allowedMissedCleavagesFasta, proteaseIn);

        //prepare output file
        String outputFilePath = queryInLoc.replace(".fasta", "_uniquePeptides.csv");
        File outputFile = new File(outputFilePath);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! File Location: " + outputFilePath);
        }

        //Header
        sb.setLength(0);
        sb.append("Unique Peptide").append(",").append("\n");
        assert pw != null;
        pw.write(sb.toString());
        pw.flush();
        sb.setLength(0);

        //start checking if peptide is unique in set
        ProgressBar.progressBar("Checking Peptide Uniqueness", queryPeptides.size());
        int uniquePeptides = 0;
        for (String peptide : queryPeptides) {
            if (!allPeptides.contains(peptide)) {
                sb.append(peptide).append(",").append("\n");
                pw.write(sb.toString());
                pw.flush();
                uniquePeptides++;
                sb.setLength(0);
            }
            ProgressBar.progress();
        }
        ProgressBar.close();
        pw.close();
        System.out.println("Analysis complete! Number of unique peptides: " + uniquePeptides);

    }


}
