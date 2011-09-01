

package br.org.indt.ndg.mobile.structures;

import br.org.indt.ndg.mobile.NdgConsts;
import java.util.Vector;

public class FileSystemSurveyStructure {

    private Vector surveyDirNames;
    private Vector surveyNames;
    private Vector surveyIds;

    private int currentIndex;

    public FileSystemSurveyStructure() {
        surveyNames = new Vector();
        surveyDirNames = new Vector();
        surveyIds = new Vector();
        currentIndex = 0;
    }

    public void setCurrentIndex(int _index) {
        currentIndex = _index;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void addFileName(String _file) {
        surveyDirNames.addElement(_file);
        String id = _file.substring(NdgConsts.XFORMS_SURVEY_DIR_PREFIX.length(), _file.length() - 1);
        surveyIds.addElement(id);
    }

    public void addName(String _name) {
        surveyNames.addElement(_name);
    }

    public void addId(String id)
    {
        surveyIds.addElement(id);
    }

    public Vector getNames() {
        return surveyNames;
    }

    public String getDirName() {
        return (String) surveyDirNames.elementAt(currentIndex);
    }

    public int getSurveysCount(){
        return surveyNames.size();
    }

    public void removeNameAndFileNameAtCurrentIndex() {
        surveyDirNames.removeElementAt(currentIndex);
        surveyNames.removeElementAt(currentIndex);
    }

    public String getCurrentName(){
        return (String)surveyNames.elementAt(currentIndex);
    }

    public String getCurrentId(){
        return (String)surveyIds.elementAt(currentIndex);
    }
}
