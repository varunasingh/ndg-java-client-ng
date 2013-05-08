package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.download.DownloadNewSurveys;
import com.sun.lwuit.Command;

/**
 *
 * @author mluz
 */
public class CheckNewSurveysCommand extends CommandControl {

    private static CheckNewSurveysCommand instance;

    protected Command createCommand() {
        return new Command(Resources.CHECK_NEW_SURVEYS);
    }

    protected void doAction(Object parameter) {
        DownloadNewSurveys.getInstance().check();
    }

    public static CheckNewSurveysCommand getInstance() {
        if (instance == null)
            instance = new CheckNewSurveysCommand();
        return instance;
    }
}
