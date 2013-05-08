package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.ui.camera.ViewFinderForm;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.Command;

/**
 *
 * @author amartini
 */
public class TakePictureAgainCommand extends CommandControl{
    private static TakePictureAgainCommand instance;

    private TakePictureAgainCommand(){}

    protected Command createCommand() {
        return new Command(Resources.TRY_AGAIN);
    }

    protected void doAction(Object parameter) {
        AppMIDlet.getInstance().setDisplayable( ViewFinderForm.class );
    }

    public static TakePictureAgainCommand getInstance(){
        if(instance == null)
            instance = new TakePictureAgainCommand();
        return instance;
    }
}
