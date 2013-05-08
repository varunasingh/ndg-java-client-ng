package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.ui.GeneralAlert;
import br.org.indt.ndg.lwuit.ui.WaitingScreen;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.FileSystem;
import br.org.indt.ndg.mobile.NdgConsts;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.ResultList;
import com.sun.lwuit.Command;

/**
 *
 * @author mluz
 */
public class OpenSurveyCommand extends CommandControl {

    //private boolean inProcess = false;

    private static OpenSurveyCommand instance;

    protected Command createCommand() {
        return new Command(Resources.CMD_OPEN);
    }

    public static OpenSurveyCommand getInstance() {
        if (instance == null)
            instance = new OpenSurveyCommand();
        return instance;
    }

    protected void doAction(Object parameter) {
        int selectedIndex = ((Integer)parameter).intValue();

        AppMIDlet.getInstance().getFileSystem().useResults(FileSystem.USE_NOT_SENT_RESULTS);
        AppMIDlet.getInstance().getFileSystem().setSurveyCurrentIndex(selectedIndex);
        AppMIDlet.getInstance().getFileSystem().setResultCurrentIndex(selectedIndex);

        WaitingScreen.show(Resources.LOADING_SURVEYS);

        OpenSurveyRunnable osr = new OpenSurveyRunnable();
        Thread t = new Thread(osr);  //create new thread to compensate for waitingform
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    class OpenSurveyRunnable implements Runnable {

        public void run() {

            try { Thread.sleep(200); } catch (InterruptedException ex) { ex.printStackTrace(); }

            System.out.println("Name: "+ AppMIDlet.getInstance().getFileSystem().getRoot() + AppMIDlet.getInstance().getFileSystem().getSurveyDirName() + NdgConsts.SURVEY_NAME);

            try {
                AppMIDlet.getInstance().getFileStores().createSurveyStructure();
                AppMIDlet.getInstance().getFileSystem().loadResultFiles();
                if (!AppMIDlet.getInstance().getFileSystem().getError()) {
                    AppMIDlet.getInstance().getFileSystem().setResultListIndex(0);
                    AppMIDlet.getInstance().setResultList(new ResultList());
                    AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.ResultList.class);
                } else {
                    AppMIDlet.getInstance().getFileSystem().setError(false);
                    GeneralAlert.getInstance().addCommand( ExitCommand.getInstance());
                    GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.EPARSE_RESULT, GeneralAlert.ERROR );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
