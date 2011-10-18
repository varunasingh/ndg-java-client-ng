package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.mobile.Resources;
import com.nokia.xfolite.xforms.dom.XFormsDocument;
import com.sun.lwuit.Command;
import java.util.Date;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaInterviewSaveCommand extends CommandControl {

    private static OpenRosaInterviewSaveCommand instance = null;
    private SaveResultsObserver saveObserver = null;
    private Date startDate;

    protected Command createCommand() {
        return new Command(Resources.CMD_SAVE);
    }

    public void setObserver(SaveResultsObserver observer){
        saveObserver = observer;
    }

    protected void doAction(Object parameter) {
        XFormsDocument doc = (XFormsDocument)parameter;
        PersistenceManager.getInstance().saveOpenRosaResult( doc, saveObserver, startDate );
    }

    public static OpenRosaInterviewSaveCommand getInstance(){
        if(instance == null){
            instance = new OpenRosaInterviewSaveCommand();
        }
        return instance;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
