package br.org.indt.ndg.lwuit.control;


import br.org.indt.ndg.lwuit.ui.GeneralAlert;
import br.org.indt.ndg.lwuit.ui.WaitingScreen;
import br.org.indt.ndg.lwuit.ui.openrosa.OpenRosaUtils;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaSurvey;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.logging.Logger;
import com.nokia.xfolite.xforms.dom.XFormsDocument;
import com.nokia.xfolite.xforms.submission.XFormsXMLSerializer;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author kgomes
 */
public class PersistenceManager {

    private static final int VERSION = 2;//1-without conditional categories;2-with conditional categories
    private static PersistenceManager instance = null;
    private boolean error = false;
    private String resultId;
    private XFormsDocument xFormDoc = null;
    private boolean encryption = false;
    private static String ORX_NAMESPACE = "http://openrosa.org/xforms/metadata";
    private static String INSTANCE_NAME = "orx:instanceID";
    private static String META_NAME = "orx:meta";
    private static String TIME_START_NAME = "orx:timeStart";
    private static String TIME_END_NAME = "orx:timeEnd";
    private static String DEVICE_ID_NAME = "orx:deviceID";
    private Date startDate;

    private PersistenceManager() {
    }

    public static PersistenceManager getInstance() {
        if (instance == null) {
            instance = new PersistenceManager();
        }
        return instance;
    }

    public boolean getError() {
        return error;
    }

    public boolean isEditing() {
        return AppMIDlet.getInstance().getFileSystem().isLocalFile();
    }

    public void saveOpenRosaResult(XFormsDocument document, Date startDate) {
        xFormDoc = document;
        this.startDate = startDate;

        WaitingScreen.show(Resources.SAVING_RESULT);
        SaveXFormResultRunnable srr = new SaveXFormResultRunnable();
        Thread t = new Thread(srr);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    private void encode(String surveyFilepath, String surveyFilename) throws IOException {
        FileConnection inputFile = null;
        InputStream in = null;
        FileConnection outputFile = null;
        OutputStream out = null;
        try {
            inputFile = (FileConnection) Connector.open(surveyFilepath);
            in = inputFile.openInputStream();
        } catch (IOException ex) {
        }
        try {
            String encrypedFilename = "e_" + surveyFilename;
            String encryptedSurveyFilepath = surveyFilepath + encrypedFilename;
            outputFile = (FileConnection) Connector.open(encryptedSurveyFilepath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.create();
            out = outputFile.openOutputStream();
        } catch (IOException ex) {
        }
        AES encrypter = new AES();
        try {
            encrypter.encrypt(in, out);
        } catch (Exception e) {
            GeneralAlert.getInstance().addCommand(ExitCommand.getInstance());
            GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.WRONG_KEY, GeneralAlert.ERROR);
        }
        in.close();
        out.close();
        inputFile.delete();
        inputFile.close();
        outputFile.rename(surveyFilename);
        outputFile.close();
    }

    private void saveResult() {
        if (AppMIDlet.getInstance().getSettings() != null
                && AppMIDlet.getInstance().getSettings().getStructure().isEncryptionConfigured()) {
            encryption = AppMIDlet.getInstance().getSettings().getStructure().getEncryption();
        }
        String surveyId = ""; //TODO get OpenRosa surveyId
        boolean isLocalFile = AppMIDlet.getInstance().getFileSystem().isLocalFile();
        String resultFilename = getResultFilename(surveyId, isLocalFile);
        String resultFilepath = getResultFilePath(resultFilename);
        resultId = extractResultId(resultFilename);

        try {
            persistOpenRosaResult(resultFilepath, resultFilename, isLocalFile);
        } catch (Exception e) {
            Logger.getInstance().log(e.getMessage());
            e.printStackTrace();
        } finally {
            System.gc();
        }
    }

    private void persistOpenRosaResult(String resultFilepath, String resultFilename, boolean fileExists) {
        if (xFormDoc == null) {
            throw new IllegalArgumentException("Tried to save OpenRosa survey when document not available");
        }
        Document instanceDocument = xFormDoc.getModel().getDefaultInstance().getDocument();
        if (!fileExists) {
            String firstResultFilename = ((OpenRosaSurvey)xFormDoc.getUserInterface()).getFirstTextAnswer();
            if(firstResultFilename != null) {
                resultFilepath = resultFilepath.substring(0, resultFilepath.indexOf(".xml")) + "_"+ firstResultFilename + ".xml";
            } else {
                resultFilepath = resultFilepath.substring(0, resultFilepath.indexOf(".xml")) + "_"+ resultFilename.substring(3);
            }
            addOpenRosaMetadata(instanceDocument);
        }

        XFormsXMLSerializer serializer = new XFormsXMLSerializer();
        try {
            FileConnection fCon = (FileConnection) Connector.open(resultFilepath);
            if (!fCon.exists()) {
                fCon.create();
            }
            OutputStream stream = fCon.openOutputStream();
            serializer.serialize(stream, instanceDocument.getDocumentElement(), null);
            stream.close();

            if (encryption) {
                encode(resultFilepath, resultFilename);
            }

            fCon.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param surveyId      Id of results survey
     * @param isLocalFile   Indicates whether file already exists or new name should be generated
     * @return  Filename without survey directory part
     */
    private String getResultFilename(String surveyId, boolean isLocalFile) {
        String fname;
        if (isLocalFile) { //check whether to create new file or use existing surveyFilePathWithBinaries
            fname = AppMIDlet.getInstance().getFileSystem().getResultFilename();
        } else {
            String UID = generateUniqueID();
            fname = "r_" + surveyId + "_" + UID + ".xml";
        }
        return fname;
    }

    private String getResultFilePath(String fname) {
        String surveyDir = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
        String filename;

        filename = AppMIDlet.getInstance().getFileSystem().getRoot() + surveyDir + fname;
        return filename;
    }

    private String extractResultId(String filename) {
        String result = "";
        int i = filename.lastIndexOf('_');
        int z = filename.indexOf('.');
        if ((i > 0) && (z > 0)) {
            result = filename.substring(i + 1, z);
        }
        return result;
    }

    private String generateUniqueID() {
        Random rnd = new Random();
        long uniqueID = (((System.currentTimeMillis() >>> 16) << 16) + rnd.nextLong());
        return Integer.toHexString((int) uniqueID);
    }

    /**
     * Adds required OpenRosa meta tags.
     * https://bitbucket.org/javarosa/javarosa/wiki/OpenRosaMetaDataSchema
     */
    private void addOpenRosaMetadata(Document instanceDocument) {

        Element docElem = instanceDocument.getDocumentElement();
        docElem.setAttribute("xmlns:orx", ORX_NAMESPACE);

        Element metaElem = instanceDocument.createElement(META_NAME);
        Element instanceElem = instanceDocument.createElement(INSTANCE_NAME);
        Element timeStartElem = instanceDocument.createElement(TIME_START_NAME);
        Element timeEndElem = instanceDocument.createElement(TIME_END_NAME);
        Element deviceIdElem = instanceDocument.createElement(DEVICE_ID_NAME);

        instanceElem.setText(generateUniqueID());
        deviceIdElem.setText(AppMIDlet.getInstance().getIMEI());
        timeStartElem.setText(OpenRosaUtils.getIsoTimestampString(startDate));
        timeEndElem.setText(OpenRosaUtils.getIsoTimestampString(new Date()));

        metaElem.appendChild(instanceElem);
        metaElem.appendChild(deviceIdElem);
        metaElem.appendChild(timeStartElem);
        metaElem.appendChild(timeEndElem);

        docElem.insertBefore(metaElem, docElem.getChild(0));
    }

    class SaveXFormResultRunnable implements Runnable {

        public void run() {
            PersistenceManager.getInstance().saveResult();
            AppMIDlet.getInstance().getFileSystem().loadResultFiles();
            AppMIDlet.getInstance().setResultList(new br.org.indt.ndg.mobile.ResultList());
            AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.ResultList.class);
        }
    }
}
