package br.org.indt.ndg.lwuit.ui;

import br.org.indt.ndg.lwuit.control.BackResultViewCommand;
import br.org.indt.ndg.lwuit.control.DeleteCurrentResultCommand;
import br.org.indt.ndg.lwuit.control.OpenResultCommand;
import br.org.indt.ndg.lwuit.control.SendResultCommand;
import br.org.indt.ndg.lwuit.control.SurveysControl;
import br.org.indt.ndg.lwuit.ui.openrosa.OpenRosaUtils;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaGroup;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaQuestion;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaSurvey;
import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.FileSystem;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.multimedia.Base64Coder;
import com.nokia.xfolite.xforms.dom.BoundElement;
import com.nokia.xfolite.xforms.dom.XFormsElement;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.sun.lwuit.Container;
import com.sun.lwuit.Font;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import java.util.Date;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaResultPreviewView extends Screen implements ActionListener {

    private String title1;
    private String title2 = Resources.RESULTS_LIST_TITLE;


    private Font questionFont = NDGStyleToolbox.fontMedium;
    private Font answerFont = NDGStyleToolbox.fontMedium;
    final public static int PREVIEW_PHOTO_SIZE = 80;

    private OpenRosaSurvey survey = null;


    protected void loadData() {
        title1 = SurveysControl.getInstance().getSurveyTitle();
        survey = AppMIDlet.getInstance().getFileStores().getSurveyModel();
    }

    protected void customize() {
        form.removeAllCommands();
        form.removeAll();

        form.setCyclicFocus(false);

        form.addCommand(BackResultViewCommand.getInstance().getCommand());
        if ( !(AppMIDlet.getInstance().getFileSystem().resultsInUse() == FileSystem.USE_SENT_RESULTS) ) {
            // Open option not available if view opened in SentResults mode
            form.addCommand(SendResultCommand.getInstance().getCommand());
            form.addCommand(DeleteCurrentResultCommand.getInstance().getCommand());
            form.addCommand(OpenResultCommand.getInstance().getCommand());
        }

        form.setSmoothScrolling(true);
        try {
            form.removeCommandListener(this);
        } catch (NullPointerException npe) {
            //during first initialisation remove throws exception.
            //this ensure that we have registered listener once
        }
        form.addCommandListener(this);
        setTitle(title1, title2);

        addQuestions();
    }


    public void addQuestions(){
        for(int idxGr = 0; idxGr < survey.getGroups().size(); idxGr++){
            OpenRosaGroup group = (OpenRosaGroup)survey.getGroups().elementAt( idxGr );

            for(int idxQue = 0; idxQue < group.getQuestions().size(); idxQue++){
                OpenRosaQuestion que = (OpenRosaQuestion)group.getQuestions().elementAt( idxQue );
                switch ( que.getType() ){
                    case OpenRosaQuestion.TYPE_INPUT:
                        addInputPreview( que.getBoundElement() );
                        break;
                    case OpenRosaQuestion.TYPE_SELECT:
                        addSelectPreview( que.getBoundElement() );
                        break;
                    case OpenRosaQuestion.TYPE_SELECT1:
                        addSelectPreview( que.getBoundElement() );
                        break;
                    case OpenRosaQuestion.TYPE_UPLOAD_IMAGE:
                        addPhotoPreview( que.getBoundElement() );
                        break;
                    default:
                }
            }
        }
    }

    public void actionPerformed(ActionEvent evt) {
        Object cmd = evt.getSource();
        if (cmd == OpenResultCommand.getInstance().getCommand()) {
            OpenResultCommand.getInstance().execute(null);
        } else if (cmd == BackResultViewCommand.getInstance().getCommand()) {
            BackResultViewCommand.getInstance().execute(null);
        } else if (cmd == DeleteCurrentResultCommand.getInstance().getCommand()) {
            DeleteCurrentResultCommand.getInstance().execute(null);
        } else if (cmd == SendResultCommand.getInstance().getCommand()) {
            SendResultCommand.getInstance().execute(null);
        }
    }


        public void addPhotoPreview(BoundElement element){
        String questionLabel = OpenRosaSurvey.getResourceManager().tryGetLabelForElement(element);

        Image samllImgage = null;
        if(element.getStringValue() != null && !element.getStringValue().equals("")){
            byte[] byteArray = Base64Coder.decode(element.getStringValue());
            Image img = Image.createImage(byteArray, 0, byteArray.length);
            samllImgage = img.scaled(PREVIEW_PHOTO_SIZE, PREVIEW_PHOTO_SIZE);
        }
        Container container = new Container( new BoxLayout( BoxLayout.Y_AXIS ) );
        container.addComponent( UIUtils.createTextArea( questionLabel + ":", questionFont, NDGStyleToolbox.getInstance().questionPreviewColor) );
        container.addComponent(new Label(samllImgage));
        form.addComponent(container);
    }

    public void addSelectPreview(BoundElement bindElem){
        
        NodeSet choices = new NodeSet();

        int count = bindElem.getChildCount();
        for (int idx = 0; idx < count; idx++) {
            Node nodeItem = bindElem.getChild(idx);
            if (nodeItem.getLocalName() != null
                    && nodeItem.getLocalName().compareTo("item") == 0) {
                choices.AddNode(nodeItem);
            }
        }

        int length = choices.getLength();

        String chosenVal = " " + bindElem.getStringValue() + " ";
        
        String result = "";
        
        for (int i = 0; i < length; i++) {
            XFormsElement n = (XFormsElement) choices.item(i);
            String value = getValueForItemElement(n);
            
            if (!value.equals("") && chosenVal.indexOf(" " + value + " ") >= 0) {
                result += "#" + OpenRosaSurvey.getResourceManager().tryGetLabelForElement(n) + " ";
            }

        }
        
        String questionLabel = OpenRosaSurvey.getResourceManager().tryGetLabelForElement( bindElem );

        addQuestionComponent( questionLabel, result );
    }
    
    
    private String getValueForItemElement( XFormsElement element ){
        for( int i = 0; i < element.getChildCount(); i++ ){
            Node nodeItem = element.getChild( i );
            if (nodeItem.getLocalName() != null
                    && nodeItem.getLocalName().compareTo("value") == 0) {
                return nodeItem.getText();
            }
        }
        return "";
    }

    public void addInputPreview(BoundElement element){

        String questionLabel = OpenRosaSurvey.getResourceManager().tryGetLabelForElement(element);
        String questionValue = "";

        DataTypeBase a = element.getDataType();
        if (a != null && a.getBaseTypeID() == DataTypeBase.XML_SCHEMAS_UNKNOWN) {
            questionValue = Resources.UNSUPPORTED_TYPE;
        } else{
            questionValue = element.getStringValue();
        }

        if(element.getDataType().getBaseTypeID() == DataTypeBase.XML_SCHEMAS_DATE){
            if(questionValue != null && !questionValue.equals( "" )){
                Date date = OpenRosaUtils.getDateFromString(questionValue);
                questionValue = OpenRosaUtils.getUserFormatDate(date);
            }
        }

        addQuestionComponent(questionLabel, questionValue);
    }

    private void addQuestionComponent(String label, String value){
        Container container = new Container( new BoxLayout( BoxLayout.Y_AXIS ) );
        container.addComponent( UIUtils.createTextArea( label + ":", questionFont, NDGStyleToolbox.getInstance().questionPreviewColor) );
        container.addComponent( UIUtils.createTextArea( value, answerFont, NDGStyleToolbox.getInstance().answerPreviewColor ) );

        form.addComponent(container);
    }
}



