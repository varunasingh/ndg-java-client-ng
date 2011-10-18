package br.org.indt.ndg.mobile;

import br.org.indt.ndg.lwuit.control.AES;
import br.org.indt.ndg.lwuit.control.ExitCommand;
import br.org.indt.ndg.lwuit.control.SurveysControl;

import br.org.indt.ndg.lwuit.model.Survey;
import br.org.indt.ndg.lwuit.ui.GeneralAlert;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaGroup;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaSurvey;
import br.org.indt.ndg.mobile.xmlhandle.Parser;
import com.nokia.xfolite.xml.dom.Document;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

public class FileStores {

    private Survey surveyStructure = null;
    private Document xformResult;
    private OpenRosaSurvey surveyModel = null;
    private OpenRosaGroup selectedGroup = null;

    private Parser parser=null;

    public FileStores() {
    }

    public boolean getError() {
        if (parser!=null) return parser.getError();
        else return false;
    }

    public void setSelectedGroup(OpenRosaGroup group){
        selectedGroup = group;
    }

    public OpenRosaGroup getSelectedGroup(){
        return selectedGroup;
    }

    public Survey getSurveyStructure() {
        return surveyStructure;
    }

    public void resetResultStructure(){
        xformResult = null;
    }

    public void loadSurvey(){
        surveyModel = new OpenRosaSurvey();
        surveyModel.initialize();
    }

    public OpenRosaSurvey getSurveyModel(){
        return surveyModel;
    }

    public void createSurveyStructure() {
        surveyStructure =  new Survey();
        surveyStructure.setTitle(AppMIDlet.getInstance().getFileSystem().getCurrentSurveyName());
        SurveysControl.getInstance().setSurvey((Survey) surveyStructure);
    }

    public void loadXFormResult(){
        xformResult = null;

        String resultFileName = AppMIDlet.getInstance().getFileSystem().getResultFilename();
        String dirName = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
        String resultPath = AppMIDlet.getInstance().getRootDir() + dirName + resultFileName;
        FileConnection fc;
        try {
            fc = (FileConnection) Connector.open(resultPath, Connector.READ);

            boolean encryption = false;
            if(AppMIDlet.getInstance().getSettings() != null) {
                if( AppMIDlet.getInstance().getSettings().getStructure().isEncryptionConfigured() )
                    encryption = AppMIDlet.getInstance().getSettings().getStructure().getEncryption();
            }
            InputStream is = fc.openInputStream();

            if( encryption ) {
                AES encrypter = new AES();
                try {
                    is = encrypter.decryptInputStreamToInputStream( is );
                } catch (Exception e) {
                    GeneralAlert.getInstance().addCommand( ExitCommand.getInstance());
                    GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.WRONG_KEY, GeneralAlert.ERROR );
                }
            }

            KXmlParser parser = new KXmlParser();
            parser.setInput(is, "UTF-8");

            xformResult = new Document();
            xformResult.parse(parser);

        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Document getXFormResult(){
        return xformResult;
    }
}
