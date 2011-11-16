package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.control.BackCommand;
import br.org.indt.ndg.lwuit.ui.OpenRosaGroupScreen;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.Command;

/**
 *
 * @author damian.janicki
 */
public class BackToCategoryCommand extends BackCommand{

    private static BackToCategoryCommand instance = null;

    public static BackToCategoryCommand getInstance(){
        if(instance == null){
            instance = new BackToCategoryCommand();
        }
        return instance;
    }
    protected Command createCommand() {
        return new Command( Resources.NEWUI_BACK );
    }

    protected void doAction(Object parameter) {
        AppMIDlet.getInstance().setDisplayable( OpenRosaGroupScreen.class );
    }

}
