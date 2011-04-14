package br.org.indt.ndg.lwuit.ui.openrosa;

import com.nokia.xfolite.xforms.dom.BoundElement;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author pawel.polanski
 */
public class OpenRosaConstraintHelper {

    private static OpenRosaConstraintHelper instance = null;

    public static OpenRosaConstraintHelper getInstance() {
        if(instance == null) {
            instance = new OpenRosaConstraintHelper();
        }
        return instance;
    }

    public boolean validateConstraint(String input, BoundElement element) {
        boolean result = false;
        String constraint = element.getConstraintString();
        try {
            int typeId = element.getDataType().getBaseTypeID();
            switch (typeId) {

                case DataTypeBase.XML_SCHEMAS_STRING:
                    result = validateString(constraint, input);
                    break;
                case DataTypeBase.XML_SCHEMAS_INTEGER:
                case DataTypeBase.XML_SCHEMAS_DECIMAL:
                    result = validateInt(constraint, input);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
             // do nthg
        }
        return result;
    }

    private boolean validateString(String constraint, String input) {
        //(. > 2 and . < 10)
        boolean result = false;
        if (constraint != null) {
            int min = Integer.parseInt(getLowConstraint(constraint));
            int max = Integer.parseInt(getHighConstraint(constraint));
            if (min >= 0 && max > 0) {
                if (input.length() >= min && input.length() <= max) {
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean validateInt(String constraint, String input) {
        //(. > 2 and . < 10)
        boolean result = true;
        if (constraint != null) {
            String min = getLowConstraint(constraint);
            String max = getHighConstraint(constraint);
            // lexical comparision, can exceed maxInt
            if( input.length() < min.length() || input.length() > max.length() ) {
                result = false;
            }
            if( input.length() == min.length() && input.compareTo(min) < 0  ) {
                result = false;
            }
            if( input.length() == max.length() && input.compareTo(max) > 0 ) {
                result = false;
            }
        }
        return result;
    }

    public boolean validateDate(String constraint, Date input) {
        //. > 03/09/2011 and . < 03/17/2011
        boolean result = false;
        if (constraint != null) {
            String min = getLowConstraint(constraint);
            String max = getHighConstraint(constraint);
            Date low = createDate(min);
            Date high = createDate(max);
            if (input.getTime() > low.getTime() && input.getTime() < high.getTime()) {
                result = true;
            }
        }
        return result;
    }

    private Date createDate(String date) {
        char separator = '/';
        String month = "", day = "", year = "";
        int firstSeparatorIndex = date.indexOf(separator);
        int secondSeparatorIndex = date.indexOf(separator, firstSeparatorIndex + 1);
        month = date.substring(0, firstSeparatorIndex);
        day = date.substring(firstSeparatorIndex + 1, secondSeparatorIndex);
        year = date.substring(secondSeparatorIndex + 1, date.length());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        return calendar.getTime();
    }

    public int getMaxStringLength(BoundElement element) {
        int result = 250; // default
        String constraint = element.getConstraintString();
        if (constraint != null) {
            try {
            int max = Integer.parseInt(getHighConstraint(constraint));
            result = max;
            }
            catch (Exception ex) {
                // do nthg
            }
        }
        return result;
    }

    public String getHighConstraint(String constraint) {
        String max = null;
        if (constraint != null) {
            int andPosition = constraint.indexOf("and");
            int endPosition = constraint.indexOf(")");
            max = constraint.substring(andPosition + 8,
                    endPosition).trim();
        }
        return max;
    }

    public String getLowConstraint(String constraint) {
        String min = null;
        if (constraint != null) {
            int andPosition = constraint.indexOf("and");
            min = constraint.substring(4, andPosition).trim();
        }
        return min;
    }

    public String getDateLowConstraint(String constraint){
        String constrStr = getLowConstraint(constraint);
        Date low = createDate(constrStr);
        return OpenRosaUtils.getUserFormatDate(low);
    }

    public String getDateHighConstraint(String constraint){
        String constrStr = getHighConstraint(constraint);
        Date highDate = createDate(constrStr);
        return OpenRosaUtils.getUserFormatDate(highDate);
    }
}
