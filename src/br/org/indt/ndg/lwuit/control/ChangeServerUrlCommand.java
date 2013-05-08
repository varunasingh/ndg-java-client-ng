package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.ui.ServerUrlChange;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.Command;

public class ChangeServerUrlCommand extends CommandControl{

    private static ChangeServerUrlCommand instance;

    public static ChangeServerUrlCommand getInstance() {
        if (instance == null) {
            instance = new ChangeServerUrlCommand();
        }
        return instance;
    }

    protected Command createCommand() {
        return new Command( Resources.SERVERURL );
    }

    protected void doAction(Object param) {
        AppMIDlet.getInstance().setDisplayable( ServerUrlChange.class );
    }
}
