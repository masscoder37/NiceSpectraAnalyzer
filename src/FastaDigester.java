import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Michael Stadlmeier on 6/11/2018.
 * This class should be able to enzymatically digest protein .FASTA-files
 *
 */
public class FastaDigester {
    public static ArrayList<String> digestFasta (String fastaIn, String proteaseIn, int missedCleavagesIn){
        ArrayList<String> peptideListOut = new ArrayList<>();

        //add int variable to track number of generated peptides
        int generatedPeptides = 0;

        //add size variable
        int sizeOfFasta = fastaIn.length();

        //set amino acids after which sequence is cut
        ArrayList<String> cutAA= new ArrayList<>();
        switch (proteaseIn){
            case "Trypsin":
                cutAA.add("K");
                cutAA.add("R");
                break;
            case "Test":
                cutAA.add("A");
                break;

            default:
                throw new IllegalArgumentException("cut AAs not set! Protease entered: "+proteaseIn);
        }

        //Put .FASTA String into list of individual Letters
        String[] allIndividualAAs = new String[sizeOfFasta];
        for (int a = 0; a < sizeOfFasta; a++ ){
            allIndividualAAs[a] = Character.toString(fastaIn.charAt(a));
        }
        //now check all the letters and see if it should have been cut. Save all the sequences for the peptides in new String ArrayList
        ArrayList<String> cutSequences = new ArrayList<>();
        int lastCutPosition = -1;
        int missedCounter = 0;
        int lastMissedPosition = 0;
        StringBuilder sb = new StringBuilder();
        //additional for loop required for all the different peptides with missed cleavages

        //TODO make missed cleavages work
        for (int m = 0; m<missedCleavagesIn+1; m++) {
            for (int a = 0; a < sizeOfFasta; a++) {
                //loop through all the AAs to be cut
                for (String toCut : cutAA) {
                    //if this occurs, a cut was detected
                    //create new Sequence if no missed cleavages shall occur
                    if (toCut.equals(allIndividualAAs[a])) {
                        if (missedCounter < m) {
                            missedCounter++;
                            lastMissedPosition = a;
                            continue;
                        }
                        //empty sb; maybe unnecessary
                        sb.setLength(0);
                        //add all the letters to the Stringbuilder and put them into a String
                        for (int b = lastCutPosition + 1; b < a + 1; b++) {
                            sb.append(allIndividualAAs[b]);
                        }
                        //add the generated String to the ArrayList
                        cutSequences.add(sb.toString());
                        //set all the variables correctly
                        lastCutPosition = a;
                        generatedPeptides++;
                        a = lastMissedPosition;
                        missedCounter = 0;
                    }
                }
                    //handle last peptide
                    if (a == sizeOfFasta-1){
                        sb.setLength(0);
                        for (int b = lastCutPosition + 1; b < a+1; b++) {
                            sb.append(allIndividualAAs[b]);
                        }
                            cutSequences.add(sb.toString());
                            generatedPeptides++;
                        }

                    }
        }


        System.out.println("Number of generated Peptides: "+generatedPeptides);
        peptideListOut.addAll(cutSequences);
        return  peptideListOut;
    }
}
