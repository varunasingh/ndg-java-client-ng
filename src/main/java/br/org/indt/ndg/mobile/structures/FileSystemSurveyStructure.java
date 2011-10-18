

package br.org.indt.ndg.mobile.structures;

import br.org.indt.ndg.mobile.NdgConsts;
import java.util.Vector;

public class FileSystemSurveyStructure {

    private Vector surveys;

    private int currentIndex;

    public FileSystemSurveyStructure() {
        surveys = new Vector();
        currentIndex = 0;
    }

    public void setCurrentIndex(int _index) {
        currentIndex = _index;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public Vector getNames() {
        Vector names = new Vector();
        for( int i = 0; i < surveys.size(); i++)
        {
            names.addElement(((SurveyStructure)surveys.elementAt(i)).surveyName);
        }
        return names;
    }

    public String getDirName() {
        return getCurrentSurvey().surveyDirName;
    }

    public int getSurveysCount(){
        return surveys.size();
    }

    public void removeSurveyAtCurrentIndex() {
        surveys.removeElementAt(currentIndex);
    }

    public String getCurrentName(){
        return getCurrentSurvey().surveyName;
    }

    public String getCurrentId(){
        return getCurrentSurvey().surveyId;
    }

    public void addSurveyInfo(String dirName, String name) {
        SurveyStructure newSurveyStructure = new SurveyStructure(dirName, name);
        newSurveyStructure.surveyDirName = dirName;
        surveys.addElement(newSurveyStructure);
    }
    
    private SurveyStructure getCurrentSurvey()
    {
        return (SurveyStructure) surveys.elementAt(currentIndex);
    }
    
    private class SurveyStructure
    {
        public SurveyStructure(String surveyDirName, String surveyName) 
        {
            this.surveyDirName = surveyDirName;
            this.surveyName = surveyName;
            this.surveyId = surveyDirName.substring(NdgConsts.XFORMS_SURVEY_DIR_PREFIX.length(), surveyDirName.length() - 1);
        }
        public String surveyDirName;
        public String surveyName;
        public String surveyId;
    }
}
