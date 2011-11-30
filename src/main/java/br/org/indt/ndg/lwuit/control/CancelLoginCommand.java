
package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.ui.SurveyList;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.logging.Logger;
import com.sun.lwuit.Command;


public class CancelLoginCommand extends BackCommand{

    private static CancelLoginCommand instance;

    public static CancelLoginCommand getInstance() {
        if (instance == null)
            instance = new CancelLoginCommand();
        return instance;
    }

    protected Command createCommand() {
        return new Command(Resources.NEWUI_CANCEL);
    }

    protected void doAction(Object parameter) {
        Logger.getInstance().emul("action cancel");
        AppMIDlet.getInstance().setDisplayable(SurveyList.class);
    }

}
