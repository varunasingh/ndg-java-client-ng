package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.ui.OpenRosaGroupScreen;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.Command;

/**
 *
 * @author damian.janicki
 */
public class SaveGroupCommand extends CommandControl{
    private static SaveGroupCommand instance;

    protected Command createCommand() {
        return new Command( Resources.JUST_SAVE );
    }


    public static SaveGroupCommand getInstance() {
        if (instance == null)
            instance = new SaveGroupCommand();
        return instance;
    }

    protected void doAction( Object parameter ) {
        AppMIDlet.getInstance().setDisplayable( OpenRosaGroupScreen.class );
    }
}
