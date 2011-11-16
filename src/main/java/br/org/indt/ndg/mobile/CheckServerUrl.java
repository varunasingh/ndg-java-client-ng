package br.org.indt.ndg.mobile;

import br.org.indt.ndg.lwuit.ui.GeneralAlert;
import br.org.indt.ndg.lwuit.ui.WaitingScreen;
import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class CheckServerUrl implements Runnable {

    private String serverUrl;
    private Boolean operationCanceled = Boolean.FALSE;
    private Thread thread = null;

    public void checkUrl(String serverUrl)
    {
        this.serverUrl = serverUrl;

        setOperationAsNotCanceled();

        sleepCurrent(500);
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        if (serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        StringBuffer testUrl = new StringBuffer(serverUrl);
       testUrl.append(AppMIDlet.getInstance().getPropertyServerRoot()).append(NdgConsts.SERVLET_CHECK_URL);
        HttpConnection hc = null;
        try {
            hc = (HttpConnection) Connector.open(testUrl.toString());
            hc.setRequestMethod(HttpConnection.GET);
            if (hc.getResponseCode() == HttpConnection.HTTP_OK && serverIsNdg(hc.openDataInputStream())) {
                hideWaitingScreen();
                AppMIDlet.getInstance().getSettings().getStructure().setServerUrl(serverUrl);
                AppMIDlet.getInstance().getSettings().writeSettings();
                AppMIDlet.getInstance().showLoginScreen();

            } else {
                hideWaitingScreen();//for waiting screen dispose
                GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                GeneralAlert.getInstance().show(Resources.ERROR_CONNECTION, Resources.NO_NDG_SERVER , GeneralAlert.WARNING );
            }
        } catch (Exception ex) {
            hideWaitingScreen();//for waiting screen dispose
            GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
            GeneralAlert.getInstance().show(Resources.ERROR_CONNECTION, Resources.NO_SERVER , GeneralAlert.ERROR);
        } finally {
            if (hc != null) {
                try {
                    hc.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private void hideWaitingScreen() {
        WaitingScreen.dispose();
        sleepCurrent(200);//for waiting screen dispose
    }

   private void setOperationAsNotCanceled() {
        synchronized (operationCanceled) {
            operationCanceled = Boolean.FALSE;
        }
    }

   private void sleepCurrent(int ms) {
       try {
            Thread.sleep(ms);
       } catch (InterruptedException exc) {
       }
   }

    private boolean serverIsNdg(DataInputStream inputStream) throws IOException {
        byte[] response = new byte[1024];
        int bytesRead = inputStream.read(response);
        String responseString = new String(response, 0, bytesRead);
        return responseString.equals(NdgConsts.CHECK_URL_RESPONSE);
    }

}
