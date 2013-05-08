package br.org.indt.ndg.lwuit.ui.openrosa;

import java.util.Date;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaConstraintHelper {


    public static boolean validateDate(String constraint, Date input) {
        //. > 2010-03-01 and . < 2012-03-24
        if(constraint == null){
            return true;
        }

        String min = getLowConstraint(constraint);
        String max = getHighConstraint(constraint);
        Date low = OpenRosaUtils.getDateFromString(min);
        Date high = OpenRosaUtils.getDateFromString(max);

        if( low != null && input.getTime() < low.getTime() ){
            return false;
        }

        if( high != null && input.getTime() > high.getTime() ){
            return false;
        }

        return true;
    }

    private static String getLowConstraint(String constraint) {
        String min = null;
        if (constraint != null) {
            int startPosition = constraint.indexOf( ">=" );
            int andPosition = constraint.indexOf( "and" );
            if( andPosition < 0  ){
                andPosition = constraint.indexOf( ")" );
            }

            min = constraint.substring(startPosition + 2, andPosition).trim();
        }
        return min;
    }

    private static String getHighConstraint(String constraint) {
        String max = null;
        if (constraint != null) {
            int startPosition = constraint.indexOf("<=");
            int endPosition = constraint.indexOf(")");
            max = constraint.substring(startPosition + 2,
                    endPosition).trim();
        }
        return max;
    }
}
