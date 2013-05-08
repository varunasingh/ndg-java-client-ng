package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.mobile.AppMIDlet;

import br.org.indt.ndg.lwuit.model.Survey;
import br.org.indt.ndg.mobile.FileSystem;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author mluz, mturiel, amartini
 */
public class SurveysControl {

    private static SurveysControl instance;
    private Survey currentSurvey = null;
    private String tmpOtherText;
    private String[] avaiableSurveyList;

    private boolean isSurveyChanged = false;

    public static SurveysControl getInstance() {
        if (instance == null) {
            instance = new SurveysControl();
        }
        return instance;
    }

    public void setSurvey(Survey survey) {
        currentSurvey = survey;
    }

    public Survey getSurvey() {
        return currentSurvey;
    }

    public String getSurveyTitle() {
        return AppMIDlet.getInstance().getFileStores().getSurveyStructure().getDisplayableName();
    }

    public void setAvaiableSurveyToDownload(String[] avaiableSurveyList) {
        this.avaiableSurveyList = avaiableSurveyList;
    }

    public String[] getAvailableSurveysToDownload() {
        return avaiableSurveyList;
    }

    public void setItemOtherText(String _val) {
        tmpOtherText = _val;
    }

    public String getItemOtherText() {
        return tmpOtherText;
    }

    public void deleteResults(boolean[] selectedFlags) {
        Vector filenamesToDelete = new Vector();
        FileSystem fs = AppMIDlet.getInstance().getFileSystem();
        fs.useResults(FileSystem.USE_NOT_SENT_RESULTS);

        // checkSelectedFiles
        for (int i = 0; i < selectedFlags.length; i++) {
            if (selectedFlags[i]) {
                filenamesToDelete.addElement(fs.getResultFilename(i));
            }
        }

        // deleteSelectedFiles
        String fName;

        Enumeration e = filenamesToDelete.elements();
        while (e.hasMoreElements()) {
            fName = (String) e.nextElement();
            fs.deleteFile(fName);
            fs.deleteDir("b_" + fName);
        }
    }

    public boolean isSurveyChanged(){
        return isSurveyChanged;
    }

    public void setSurveyChanged(boolean bVal){
        isSurveyChanged = bVal;
    }
}
