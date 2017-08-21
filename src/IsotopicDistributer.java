/**
 * Created by micha on 8/21/2017.
 */
public class IsotopicDistributer {
    private static final Double C12 = 0.988922;
    private static final Double C13 = 0.011078;
    private static final Double H1 = 0.99984427;
    private static final Double H2 = 0.00015575;
    private static final Double N14 = 0.996337;
    private static final Double N15 = 0.003663;
    private static final Double O16 = 0.9976206;
    private static final Double O17 = 0.000379;
    private static final Double S32 = 0.9504074;
    private static final Double S33 = 0.0074869;

    public static double abundanceAddNeutron (SumFormula sumFormulaIn){
        double abundance = 0;
        //get elemental quantities
        int quantC = sumFormulaIn.getCNumber();
        int quantH = sumFormulaIn.getHNumber();
        int quantN = sumFormulaIn.getNNumber();
        int quantO = sumFormulaIn.getONumber();
        int quantS = sumFormulaIn.getSNumber();

        double normalizationFactor = Math.pow(C12, quantC) * Math.pow(H1, quantH) * Math.pow(N14, quantN) * Math.pow(O16, quantO) * Math.pow(S32, quantS);
        double cTerm = quantC * C13 * Math.pow(C12, quantC - 1) * Math.pow(H1, quantH) * Math.pow(N14, quantN) * Math.pow(S32, quantS) * Math.pow(O16, quantO);
        double hTerm = quantH * H2 * Math.pow(H1, quantH - 1) * Math.pow(C12, quantC) * Math.pow(N14, quantN) * Math.pow(S32, quantS) * Math.pow(O16, quantO);
        double nTerm = quantN * N15 * Math.pow(N14, quantN - 1) * Math.pow(H1, quantH) * Math.pow(C12, quantC) * Math.pow(S32, quantS) * Math.pow(O16, quantO);
        double oTerm = quantO * O17 * Math.pow(O16, quantO - 1) * Math.pow(H1, quantH) * Math.pow(N14, quantN) * Math.pow(C12, quantC) * Math.pow(S32, quantS);
        double sTerm = quantS * S33 * Math.pow(S32, quantS - 1) * Math.pow(C12, quantC) * Math.pow(N14, quantN) * Math.pow(H1, quantH) * Math.pow(O16, quantO);

        double combinedTerms = cTerm + hTerm + nTerm + oTerm + sTerm;


        if (normalizationFactor == 0){
            throw new IllegalArgumentException("Normalization factor is 0!");
        }

        abundance = combinedTerms / normalizationFactor;

        return abundance;
    }
}
