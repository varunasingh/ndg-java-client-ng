package br.org.indt.ndg.mobile.submit;

import br.org.indt.ndg.lwuit.control.TestConnectionCommand;
import br.org.indt.ndg.lwuit.ui.GeneralAlert;
import br.org.indt.ndg.lwuit.ui.SurveyList;
import br.org.indt.ndg.lwuit.ui.WaitingScreen;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.httptransport.SecureHttpConnector;
import java.io.IOException;
import javax.microedition.io.HttpConnection;

public class TestConnectionRunnable implements Runnable {

    public static final int TEST_GPRS = 1;
    private boolean isCanceled = false;

    public void run() {
        testGPRS();
    }

    public void setCanceled(boolean _val) {
        isCanceled = _val;
    }

    private void testGPRS() {
        String urlServlet = AppMIDlet.getInstance().getSettings().getStructure().getTestConnectionUrl();
        HttpConnection httpConn = null;
        try {
            httpConn = SecureHttpConnector.open(urlServlet, HttpConnection.GET);
            int responseCode = httpConn.getResponseCode();
            //int responseCode = httpConn.HTTP_OK;
            hideWaitingScreen();
            if (!isCanceled) {
                if (responseCode == HttpConnection.HTTP_OK) {
                    GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                    GeneralAlert.getInstance().show(Resources.TESTING_CONNECTION, Resources.CONNECTION_OK, GeneralAlert.DIALOG_OK);
                    AppMIDlet.getInstance().setDisplayable(SurveyList.class);

                } else if (responseCode == HttpConnection.HTTP_UNAUTHORIZED) {
                    GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                    GeneralAlert.getInstance().showCodedAlert(Resources.TESTING_CONNECTION, String.valueOf(responseCode), GeneralAlert.INFO);
                    SecureHttpConnector.setAuthenticationFail();
                    AppMIDlet.getInstance().showLoginScreen(TestConnectionCommand.getInstance());
                } else {
                    GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                    GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.CONNECTION_FAILED, GeneralAlert.ERROR);
                }
            }

        } catch (IOException ex) {
            hideWaitingScreen();
            GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
            GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.CONNECTION_FAILED, GeneralAlert.ERROR);
        } finally {
        }
    }

    private void hideWaitingScreen() {
        WaitingScreen.dispose();
        sleepCurrent(200);//for waiting screen dispose
    }

    private void sleepCurrent(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException exc) {
        }
    }
}
