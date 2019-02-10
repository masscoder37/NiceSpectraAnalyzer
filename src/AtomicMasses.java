/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */

//set atomic masses here
    //reference for all the other classes
public class AtomicMasses {
    private static final Double H_MASS =  1.007825032;
    private static final Double PROTON = 1.00727647;
    private static final Double C_MASS = 12.0;
    private static final Double O_MASS =  15.99491462;
    private static final Double N_MASS = 14.00307400;
    private static final Double S_MASS = 31.97207117;
    private static final Double Na_MASS =  22.9897693;
    private static final Double Cx_MASS =   13.00335484;
    private static final Double F_MASS =  18.99840316;
    private static final Double P_MASS = 30.973763;
    private static final Double NEUTRON = Cx_MASS-C_MASS;

    public static Double getHMASS(){
        return H_MASS;
    }
    public static Double getPROTON(){
        return PROTON;
    }
    public static Double getCMASS(){
        return C_MASS;
    }
    public static Double getNMASS(){return N_MASS;}
    public static Double getOMASS(){
        return O_MASS;
    }
    public static Double getSMASS(){return S_MASS; }
    public static Double getNaMASS(){return  Na_MASS;}
    public static Double getCxMASS(){return  Cx_MASS;}
    public static Double getFMASS(){return F_MASS;}
    public static Double getNEUTRON(){return NEUTRON;}
    public static Double getPMASS() {return P_MASS;}
}
