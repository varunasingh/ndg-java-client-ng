/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.model.*;
import br.org.indt.ndg.lwuit.ui.WaitingScreen;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.FileSystem;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.logging.Logger;
import br.org.indt.ndg.mobile.structures.question.TypeTime;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.location.Location;

/**
 *
 * @author kgomes
 */
public class PersistenceManager {

    private static PersistenceManager instance = null;
    private boolean error = false;
    private String resultId;
    private Vector vQuestions;

    private PersistenceManager(){
    }

    public static PersistenceManager getInstance(){
        if(instance==null){
            instance = new PersistenceManager();
        }
        return instance;
    }

    public String generateUniqueID() {
        Random rnd = new Random();
        long uniqueID = (((System.currentTimeMillis() >>>16)<<16)+rnd.nextLong());
        return Integer.toHexString((int) uniqueID);
    }

    public boolean getError() {
        return error;
    }

    public void save(Vector _vQuestions) {
        vQuestions = _vQuestions;

        WaitingScreen.show(Resources.SAVING_RESULT);
        SaveResultRunnable srr = new SaveResultRunnable();
        Thread t = new Thread(srr);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public void save2() {

        String surveyId = String.valueOf(SurveysControl.getInstance().getSurveyIdNumber());
        try {
            String surveyDir = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
            boolean isLocalFile = AppMIDlet.getInstance().getFileSystem().isLocalFile();
            String userId = AppMIDlet.getInstance().getIMEI();

            long currentTime = (new Date()).getTime();
            long timeTaken = currentTime - AppMIDlet.getInstance().getTimeTracker();

            String UID = generateUniqueID();

            String filename;  //check whether to create new file or use existing filename
            String fname;  //filename without root/survey directory part
            
            if (isLocalFile) {
                fname = AppMIDlet.getInstance().getFileSystem().getResultFilename();
                filename = Resources.ROOT_DIR + surveyDir + fname;
            }
            else {
                fname = "r_" + surveyId/*survey.getIdNumber()*/ + "_" + userId + "_" + UID + ".xml";
                filename = Resources.ROOT_DIR + surveyDir + fname;
            }

            resultId = extractResultId(fname);
            if (!isLocalFile) resultId = UID;

            FileConnection connection = (FileConnection) Connector.open(filename);
            if(!connection.exists()) connection.create();

            if (isLocalFile) {
                connection.delete();
                connection.create();
            }

            String displayName = getResultDisplayName(vQuestions);

            FileSystem fs = AppMIDlet.getInstance().getFileSystem();
            fs.storeFilename(displayName, fname);
            AppMIDlet.getInstance().setFileSystem(fs);

            OutputStream out = connection.openOutputStream();
            PrintStream output = new PrintStream(out);

            output.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
            output.print("<result ");

            if (isLocalFile) {
                output.print("r_id=\"" + resultId + "\" ");
                
            } else {
                output.print("r_id=\"" + UID + "\" ");

                // *****************************   PROTOCOL DEFINITION *************************
                //
                //****************************************************************************************************
                //Type of Msg |      SurveyID			  |      Number of  Msgs  |    	ResultID  	   | UserID
                //      9           9999999999                          99             a9b9c9d9       999999999999999
                //****************************************************************************************************
                //end of header
            }
            output.print("s_id=\"" + surveyId + "\" ");
            output.print("u_id=\"" + userId + "\" ");
            output.print("time=\"" + String.valueOf(timeTaken/1000)+ "\">");  //convert to seconds
            output.println();

            Location loc = AppMIDlet.getInstance().getLocation();

            if (loc != null) {
                if(loc.getQualifiedCoordinates() != null){
                    output.println("<latitude>" + loc.getQualifiedCoordinates().getLatitude() + "</latitude>");
                    output.println("<longitude>" + loc.getQualifiedCoordinates().getLongitude() + "</longitude>");
                }
            }

            output.println("<title>" + AppMIDlet.getInstance().u2x(displayName) + "</title>");

            int catId = 0;
            int qIndex = 0;
            NDGQuestion question = (NDGQuestion) vQuestions.elementAt(qIndex);
            while(qIndex < vQuestions.size()) {
                catId = Integer.parseInt(question.getCategoryId());
                /** Category **/
                output.print("<category " + "name=\"" + AppMIDlet.getInstance().u2x(question.getCategoryName()) + "\" ");
                output.println("id=\"" + question.getCategoryId()+ "\">");
                while(catId == Integer.parseInt(question.getCategoryId())){
                    /** Question **/
                    String type = getQuestionType(question);
                    output.print("<answer " + "type=\"" + type + "\" ");
                    output.print("id=\"" + question.getQuestionId() + "\" ");
                    output.print("visited=\"" + question.getVisited() + "\"");

                    if (question.getType().equals("_time")) {
                        if (((TimeQuestion)question).getConvention() == 1) {                            
                            output.print(" convention=\"" + "am" + "\"");
                        } else {
                            if (((TimeQuestion)question).getConvention() == 2) {
                                output.print(" convention=\"" + "pm" + "\"");                               
                            } else {

                                if (((TimeQuestion)question).getConvention() == 12) {                                    
                                    ((TimeQuestion)question).setConvention(1);
                                    output.print(" convention=\"" + "am" + "\"");
                                } else {
                                    if ( ((TimeQuestion)question).getConvention() == 24 ) {
                                        output.print(" convention=\"" + "24" + "\"");
                                    }
                                }
                            }
                        }
                    }
                    output.print(">");

                    output.println();

                    question.save(output);

                    output.println("</answer>");

                    qIndex++;
                    if (qIndex >= vQuestions.size()) break;
                    question = (NDGQuestion) vQuestions.elementAt(qIndex);
                }
                output.println("</category>");
            }
            output.println("</result>");

            out.close();
            connection.close();

        } catch( ConnectionNotFoundException e ) {
            error = true;
            Logger.getInstance().log(e.getMessage());
            AppMIDlet.getInstance().getGeneralAlert().showErrorExit(Resources.ECREATE_RESULT);
        } catch( IOException e ) {
            error = true;
            Logger.getInstance().log(e.getMessage());
            AppMIDlet.getInstance().getGeneralAlert().showErrorExit(Resources.EWRITE_RESULT);
        }
        catch(Exception e){
            Logger.getInstance().log(e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractResultId(String filename){
        String result = "";
        int i = filename.lastIndexOf('_');
        int z = filename.indexOf('.');
        if((i>0) && (z>0)){
            result = filename.substring(i+1,z);
        }
        return result;
    }

    private String getQuestionType(NDGQuestion question) {
        String result = "";
        if (question instanceof DescriptiveQuestion) {
                result = "_str";
            }
            else if (question instanceof NumericQuestion) {
                if (((NumericQuestion) question).isDecimal()) {
                    result = "_decimal";
                }
                else {
                    result = "_int";
                }
            }
            else if (question instanceof DateQuestion) {
                result = "_date";
            }
            else if (question instanceof TimeQuestion) {
                result = "_time";
            }
            else if (question instanceof ChoiceQuestion) {
                result = "_choice";
            }
            else if(question instanceof ImageQuestion){
                result = "_img";
            }
        return result;
    }

    private String getResultDisplayName(Vector vQuestions) {
        String result = "";
        String [] displayIds;
        displayIds = SurveysControl.getInstance().getSurveyDisplayIds();
        NDGQuestion q;
        for (int i = 0; i < vQuestions.size(); i++) {
            q = (NDGQuestion) vQuestions.elementAt(i);
            if ( (q.getCategoryId().equals(displayIds[0])) && (q.getQuestionId().equals(displayIds[1])) ) {
                if ((q instanceof ChoiceQuestion) || (q instanceof ImageQuestion)) {
                    result = resultId;
                }
                else if ( (q instanceof DescriptiveQuestion) || (q instanceof NumericQuestion) ) {
                    result = ((String) q.getAnswer().getValue());
                    if ( result.equals("") ) {
                        result = resultId;
                    }
                }
                else if (q instanceof DateQuestion) {
                    long datelong = Long.parseLong((String)q.getAnswer().getValue());
                    Date date = new Date(datelong);
                    result = date.toString();
                } else if (q instanceof TimeQuestion) {
                    long timelong = Long.parseLong((String)q.getAnswer().getValue());
                    Date time = new Date(timelong);
                    result = time.toString();
                }
            }
        }

        return result;
    }

    public boolean isEditing() {
        return AppMIDlet.getInstance().getFileSystem().isLocalFile();
    }

    class SaveResultRunnable implements Runnable {
        public void run() {
            PersistenceManager.getInstance().save2();

            // Refresh ResultList since a new result was created
            AppMIDlet.getInstance().getFileStores().resetQuestions();
            AppMIDlet.getInstance().getFileSystem().loadResultFiles();
            AppMIDlet.getInstance().setResultList(new br.org.indt.ndg.mobile.ResultList());
            AppMIDlet.getInstance().setDisplayable(AppMIDlet.getInstance().getResultList());
        }
    }

}