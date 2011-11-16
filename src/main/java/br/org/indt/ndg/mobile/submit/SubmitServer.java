package br.org.indt.ndg.mobile.submit;

import br.org.indt.ndg.lwuit.control.AES;
import br.org.indt.ndg.lwuit.control.ExitCommand;
import br.org.indt.ndg.lwuit.ui.GeneralAlert;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.NdgConsts;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.ResultList;
import br.org.indt.ndg.mobile.SurveyList;
import br.org.indt.ndg.mobile.Utils;
import br.org.indt.ndg.mobile.httptransport.AuthorizationException;
import br.org.indt.ndg.mobile.httptransport.SecureHttpConnector;
import br.org.indt.ndg.mobile.logging.Logger;
import java.util.Hashtable;
import javax.microedition.io.HttpConnection;

public class SubmitServer {
    //These values are set in Settings.xml
    private static final int SERVER_CANNOT_WRITE_RESULT = -1;
    private static final int NO_SURVEY_IN_SERVER = 2;
    private static final int SUCCESS = 1;

    private static final String INPUT_NAME_TAG = "filename";

    private boolean m_canceled = false;
    private String m_servletUrl = "";
    private HttpMultipartPostRequest m_currentRequest = null;
    private final Vector m_filesNotSent = new Vector();

    public SubmitServer() {
    }

    public void cancel() {
        m_canceled = true;
        if (m_currentRequest != null) {
            m_currentRequest.cancel();
            m_currentRequest = null;
            m_filesNotSent.removeAllElements();
        }
    }

    public void submitResult(String resultFilename, String surveyId){
        Vector resultsToSend = new Vector();
        resultsToSend.addElement(resultFilename);
        send(resultsToSend, surveyId);
    }

    public void submit( Vector resultFilenames, String surveyId ) {
        send(resultFilenames, surveyId);
    }

    private void send(Vector resultFilenames, String surveyId){
        m_servletUrl = AppMIDlet.getInstance().getSettings().getStructure().getPostResultsUrl();
        Enumeration e = resultFilenames.elements();
        m_filesNotSent.removeAllElements();
        while ( e.hasMoreElements() && !m_canceled ) {
            String filename = null;
            String fileContents = "";
            filename = (String) e.nextElement();
            try {
                fileContents = loadFile(filename);
            } catch (IOException ioe) {
                m_filesNotSent.addElement(filename);
                continue;
            }
            if ( fileContents == null || fileContents.equals("") ) {
                m_filesNotSent.addElement(filename);
                continue;
            }

            try {
                Hashtable params = new Hashtable();
                params.put("surveyId", surveyId);
                HttpMultipartPostRequest request =  new HttpMultipartPostRequest( m_servletUrl, params,
                                INPUT_NAME_TAG, filename, "text/xml", fileContents.getBytes(), surveyId);

                int responseCode = request.send();

                if(responseCode == HttpConnection.HTTP_OK){
                    AppMIDlet.getInstance().getFileSystem().moveSentResult(filename);
                }else if(responseCode == HttpConnection.HTTP_UNAUTHORIZED){
                    GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                    GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, Resources.HTTP_UNAUTHORIZED + " Try login again", GeneralAlert.ERROR);//TODO localize
                    SecureHttpConnector.setAuthenticationFail();
                    AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.LoginForm.class);
                    return;
                }else {
                    m_filesNotSent.addElement(filename);
                    GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                    GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, String.valueOf(responseCode), GeneralAlert.ERROR);
                }

            } catch (IOException ioe) {
                if (!m_canceled){
                    m_filesNotSent.addElement(filename);
                    GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                    GeneralAlert.getInstance().showCodedAlert( Resources.NETWORK_FAILURE, ioe.getMessage() != null ? ioe.getMessage().trim() : "", GeneralAlert.ALARM );
                    break;
                }
            }catch(AuthorizationException ex){
                GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, Resources.HTTP_UNAUTHORIZED + " Try login again", GeneralAlert.ERROR);//TODO localize
                SecureHttpConnector.setAuthenticationFail();
                AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.LoginForm.class);
                return;
            }
        }
        if ( !m_filesNotSent.isEmpty() ) {
            StringBuffer filesNotSentFormatted = new StringBuffer();
            for ( int i = 0; i < m_filesNotSent.size(); i++ ) {
                filesNotSentFormatted.append((String) m_filesNotSent.elementAt(i)).append("\n");
            }
            GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
            GeneralAlert.getInstance().show(Resources.SEND_ERRORS,
                    Resources.RESULT_NOT_SENT + ":\n" + filesNotSentFormatted,
                    GeneralAlert.ALARM);
        }
        AppMIDlet.getInstance().setResultList(new ResultList());
        AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.ResultList.class);
    }

    private String loadFile(String _filename) throws IOException {
        String fileContents = "";
        FileConnection fc = null;
        DataInputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            String surveyRoot = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
            // result without binary data (if there is no binary data in the survey then it is the complete result file)
            String pathToSurveyWithoutBinary = AppMIDlet.getInstance().getRootDir() + surveyRoot + _filename;
            // result with binary data (possibly big very file), is available only if binary data actually exists in the survey
            String pathToSurveyWithData = AppMIDlet.getInstance().getRootDir() + surveyRoot + "b_" + _filename + "/" + "b_" + _filename;
            // try to open file with binary data, if it does not exist open result without binary data
            fc = (FileConnection) Connector.open( pathToSurveyWithData );
            if ( fc.exists() ) {
                Logger.getInstance().emul("Sending file with binary data", "");
            } else {
                fc = (FileConnection) Connector.open( pathToSurveyWithoutBinary );
                Logger.getInstance().emul("Sending file without binary data", "");
            }

            boolean encryption = false;
            if(AppMIDlet.getInstance().getSettings() != null) {
                if( AppMIDlet.getInstance().getSettings().getStructure().isEncryptionConfigured() )
                    encryption = AppMIDlet.getInstance().getSettings().getStructure().getEncryption();
            }

            inputStream = fc.openDataInputStream();

            if( encryption ) {
                AES encrypter = new AES();
                try {
                    inputStream = new DataInputStream( encrypter.decryptInputStreamToInputStream( inputStream ) );
                } catch (Exception e) {
                    GeneralAlert.getInstance().addCommand( ExitCommand.getInstance());
                    GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.WRONG_KEY, GeneralAlert.ERROR );
                }
            }

            outputStream = new ByteArrayOutputStream();

            int data = inputStream.read();
            while (data != -1) {
                outputStream.write((byte) data);
                data = inputStream.read();
            }
            fileContents = outputStream.toString();
        } finally {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
                if (fc != null)
                    fc.close();
        }
        return fileContents;
    }


}
