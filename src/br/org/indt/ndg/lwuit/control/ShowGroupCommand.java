package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.ui.OpenRosaQuestionScreen;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaGroup;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.Command;

/**
 *
 * @author damian.janicki
 */
public class ShowGroupCommand extends CommandControl{

    private static ShowGroupCommand instance;

    protected Command createCommand() {
        return new Command( Resources.CMD_OPEN );
    }

    public static ShowGroupCommand getInstance() {
        if (instance == null)
            instance = new ShowGroupCommand();
        return instance;
    }

    protected void doAction(Object parameter) {
        OpenRosaGroup group = (OpenRosaGroup)parameter;
        AppMIDlet.getInstance().getFileStores().setSelectedGroup( group );
        AppMIDlet.getInstance().setDisplayable( OpenRosaQuestionScreen.class );
    }

}
