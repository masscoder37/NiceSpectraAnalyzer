/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */

//set atomic masses here
    //reference for all the other classes
    //reference masses: https://www.ciaaw.org/atomic-masses.htm
public class AtomicMasses {
    private static final double H_MASS =  1.007825032;
    private static final double PROTON = 1.00727647;
    private static final double C_MASS = 12.0;
    private static final double O_MASS =  15.99491462;
    private static final double N_MASS = 14.00307400;
    private static final double S_MASS = 31.97207117;
    private static final double Na_MASS =  22.9897693;
    private static final double Cx_MASS =   13.00335484;
    private static final double F_MASS =  18.99840316;
    private static final double P_MASS = 30.973763;
    private static final double NEUTRON = 1.001826362; //weighted neutron mass according to 13C, 15N, 2H respective to their abundances
    private static final double NEUTRON_C = Cx_MASS-C_MASS;//1.00335484
    private static final double Hx_MASS = 2.014101778 ;
    private static final double Nx_MASS = 15.00010890;
    private static final double NEUTRON_H = Hx_MASS-H_MASS; //1.006276746
    private static final double NEUTRON_N = Nx_MASS-N_MASS; //0.9970349

    //mean of neutron masses: 1.002222162
    //abundance 13C : 0.011078 --> 0.74365
    //abundance 15N : 0.003663 --> 0.24589
    //abundance 2H : 0.00015575 --> 0.010455
    //total: 0.01489675
    //weighted Neutron mass: 1.001826362 (diff to CNeutron: 0.001528)

    public static double getHMASS(){
        return H_MASS;
    }
    public static double getPROTON(){
        return PROTON;
    }
    public static double getCMASS(){
        return C_MASS;
    }
    public static double getNMASS(){return N_MASS;}
    public static double getOMASS(){
        return O_MASS;
    }
    public static double getSMASS(){return S_MASS; }
    public static double getNaMASS(){return  Na_MASS;}
    public static double getCxMASS(){return  Cx_MASS;}
    public static double getFMASS(){return F_MASS;}
    public static double getNEUTRON(){return NEUTRON;}
    public static double getPMASS() {return P_MASS;}
}
