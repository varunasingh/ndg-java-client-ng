package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.mobile.CheckServerUrl;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.Command;

public class CheckServerUrlCommand extends CommandControl{

    private static CheckServerUrlCommand instance;

    private CheckServerUrl checkingTask;

    public static CheckServerUrlCommand getInstance() {
        if (instance == null) {
            instance = new CheckServerUrlCommand();
        }
        return instance;
    }

    protected Command createCommand() {
        return new Command( Resources.CHECK_SERVER );
    }

    protected void doAction(Object param) {
        checkingTask = (CheckServerUrl)param;
        checkingTask.checkUrl();
    }
}
