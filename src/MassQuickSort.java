import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Michael Stadlmeier on 6/27/2017.
 */

//class to sort a Peak List with ascending masses
public class MassQuickSort {
    public static ArrayList<Peak> peakListQuickSort (ArrayList<Peak> peakListIn){
        qSort(peakListIn, 0, peakListIn.size()-1);
        return peakListIn;
    }

    private static void qSort(ArrayList<Peak> peakListIn, int left, int right){
        if(right > left){
            double pivot = peakListIn.get(right).getMass();
            int pivotIndex = partition(peakListIn, left, right, pivot);
            qSort(peakListIn, left, pivotIndex-1);
            qSort(peakListIn, pivotIndex+1,right);
        }
    }

    private static int partition(ArrayList<Peak> peakListIn, int left, int right, double pivot){
        int l = left;
        int r = right -1;
        while (l<=r){
            while (peakListIn.get(l).getMass()<pivot){
                l++;
            }
            while (r >=0 && peakListIn.get(r).getMass()>pivot){
                r--;
            }
            if (l<=r){
                if (l < r){
                    Collections.swap(peakListIn, l, r);
                }
                l++;r--;
            }
        }
        Collections.swap(peakListIn, l, right);
        return l;

    }
}
