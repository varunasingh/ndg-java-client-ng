/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.indt.ndg.mobile.submit;

/**
 *
 * @author Administrador
 */
public class TestConnection  {
    
    private static TestConnection tc = null;
    private Thread tTestConn = null;
    private Thread tWaitAck = null;
    private TestConnectionRunnable tcr = null;
    private boolean bWaitingForAck = false;
    private String strTimeStamp = "";

    public TestConnection() {
    }

    public static TestConnection getInstance() {
        if (tc == null) {
            tc = new TestConnection();
        }
        return tc;
    }

    public void doTest() {
            tcr = new TestConnectionRunnable();
            tTestConn = new Thread(tcr);
            tTestConn.start();
    }

    public void handleIncomingACKConnectionTest(String imei, String timeStamp) {
        if (bWaitingForAck) {
            // Checking timeStamp
            if (timeStamp.equals(strTimeStamp)) {
                // Sending Ack to server
                tcr = new TestConnectionRunnable();
                tTestConn = new Thread(tcr);
                tTestConn.start();

                bWaitingForAck = false;
            }
        }
    }
    public void cancel() {
        bWaitingForAck = false;
        tcr.setCanceled(true);
        tTestConn.interrupt();
        if (tWaitAck != null) {
            tWaitAck.interrupt();
        }
    }

}