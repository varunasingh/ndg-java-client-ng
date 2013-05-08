package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.ui.OpenRosaGroupScreen;
import br.org.indt.ndg.lwuit.ui.WaitingScreen;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.Utils;
import com.sun.lwuit.Command;

/**
 *
 * @author mluz
 */
public class OpenResultCommand extends CommandControl {

    private static OpenResultCommand instance;

    protected Command createCommand() {
        return new Command(Resources.NEWUI_OPEN_RESULT);
    }

    protected void doAction(Object parameter) {
        if (parameter != null) {
            int selectedIndex = ((Integer)parameter).intValue();
            AppMIDlet.getInstance().getFileSystem().setResultCurrentIndex(selectedIndex);
        }

        WaitingScreen.show(Resources.PROCESSING);
        OpenResultRunnable orr = new OpenResultRunnable();
        Thread t = new Thread(orr);  //create new thread to compensate for waitingform
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public static OpenResultCommand getInstance() {
        if (instance == null)
            instance = new OpenResultCommand();
        return instance;
    }

    class OpenResultRunnable implements Runnable {
        public void run() {
            try { Thread.sleep(200); } catch(Exception e){}

            if (AppMIDlet.getInstance().getFileSystem().getResultFilename() != null) {
                AppMIDlet.getInstance().getFileSystem().setLocalFile(true);

                if( Utils.isCurrentDirXForm() ){
                    AppMIDlet.getInstance().getFileStores().loadXFormResult();
                    AppMIDlet.getInstance().getFileStores().loadSurvey();
                    AppMIDlet.getInstance().setDisplayable( OpenRosaGroupScreen.class );
                }
            }
        }
    }
}
