package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.ui.WaitingScreen;
import br.org.indt.ndg.lwuit.ui.OpenRosaResultPreviewView;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.Utils;
import com.sun.lwuit.Command;

/**
 *
 * @author mluz
 */
public class ViewResultCommand extends CommandControl {

    private static ViewResultCommand instance;

    protected Command createCommand() {
        return new Command(Resources.NEWUI_VIEW_RESULT);
    }

    protected void doAction(Object parameter) {
        if (parameter == null || !(parameter instanceof Integer)) {
            throw new IllegalArgumentException("Parameter has to be a valid result index");
        }
        int selectedIndex = ((Integer)parameter).intValue();
        AppMIDlet.getInstance().getFileSystem().setResultCurrentIndex(selectedIndex);

        WaitingScreen.show(Resources.CMD_VIEW);

        ViewResultRunnable vrr = new ViewResultRunnable();
        Thread t = new Thread(vrr);  //create new thread to compensate for waitingform
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public static ViewResultCommand getInstance() {
        if (instance == null)
            instance = new ViewResultCommand();
        return instance;
    }

    class ViewResultRunnable implements Runnable {
        public void run() {
            try { Thread.sleep(200); } catch(Exception e){}
            if (AppMIDlet.getInstance().getFileSystem().getResultFilename() != null && Utils.isCurrentDirXForm() ) {
                AppMIDlet.getInstance().getFileStores().loadXFormResult();
                AppMIDlet.getInstance().getFileStores().loadSurvey();
                AppMIDlet.getInstance().setDisplayable(OpenRosaResultPreviewView.class);
            }
        }
    }
}
