import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class DnD {
    private static DecimalFormat fourDec = new DecimalFormat("0.0000");

    public static void willNimaDie(int numberOfSimulations) {
        Random bless = new Random();
        Random dedication = new Random();
        Random resistance = new Random();
        Random d20 = new Random();


        ProgressBar.progressBar("Will Nima Die?", numberOfSimulations);
        //Integer[] occurences = new Integer[20];
        int success = 0; //21 or more
        int exhaustion = 0; //<21 && >15
        int fail = 0; //<=15
        int bonus = 7;

        for (int i = 0; i < numberOfSimulations; i++){
            //create die rolls
            //bless
            int bless1 = bless.nextInt(4) +1;
            int bless2 = bless.nextInt(4) +1;
            //int bless2 = 1;
            int blessRoll = Math.max(bless1, bless2);
            //dedication
            int dedication1 = dedication.nextInt(4) +1;
            int dedication2 = dedication.nextInt(4) +1;
            //int dedication2 = 1;
            int dedicationRoll = Math.max(dedication1, dedication2);
            //resistance
            int resistanceRoll = resistance.nextInt(4) +1;
            //d20
            int d201 = d20.nextInt(20) +1;
            int d202 = d20.nextInt(20) +1;
            //int d202 = 1;
            int d20Roll = Math.max(d201, d202);

            //calculate result
            int result = d20Roll+blessRoll+dedicationRoll+resistanceRoll+bonus;
            if(result>=30)
                success++;
            else if(result<=24)
                fail++;
            else
                exhaustion++;
            //occurences[result-1] = occurences[result-1] +1;
            ProgressBar.progress();
        }

        ProgressBar.close();

        double successRate = (double) success / numberOfSimulations * 100;
        double failRate = (double) fail / numberOfSimulations * 100;
        double exhaustionRate = (double) exhaustion / numberOfSimulations * 100;

        System.out.println("Simulated rolls to unlock the Tagstahlring: " +numberOfSimulations +". This time, DC30.");
        System.out.println("Nima gets power and no exhaustion: "+fourDec.format(successRate)+ "%. This happened in " + success +" cases." );
        System.out.println("Nima gets power, but feels like shit: "+fourDec.format(exhaustionRate)+ "%. This happened in " + exhaustion +" cases." );
        System.out.println("Nima is putt: "+fourDec.format(failRate)+ "%. This happened in " + fail +" cases." );
    }
}
