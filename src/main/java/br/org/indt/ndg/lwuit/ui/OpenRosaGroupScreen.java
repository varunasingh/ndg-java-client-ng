package br.org.indt.ndg.lwuit.ui;

import br.org.indt.ndg.lwuit.control.OpenRosaBackCommand;
import br.org.indt.ndg.lwuit.control.OpenRosaInterviewSaveCommand;
import br.org.indt.ndg.lwuit.control.ShowGroupCommand;
import br.org.indt.ndg.lwuit.control.SurveysControl;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaGroup;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaSurvey;
import br.org.indt.ndg.lwuit.ui.renderers.GroupsListCellRenderer;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.list.DefaultListModel;
import com.sun.lwuit.list.ListModel;
import java.util.Date;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaGroupScreen extends Screen implements ActionListener {

    private OpenRosaSurvey surveyModel = null;
    private String title1;
    private String title2 = Resources.NEW_INTERVIEW;
    private Date startDate;

    private List groupList = null;
    private ListModel underlyingModel;

    protected void loadData() {
       surveyModel = AppMIDlet.getInstance().getFileStores().getSurveyModel();
       startDate = new Date();
       title1 = SurveysControl.getInstance().getSurveyTitle();

    }

    protected void customize() {
        setTitle( title1, title2 );
        if( groupList != null ) {
           form.removeComponent( groupList );
        }
        form.removeAllCommands();

        form.setScrollAnimationSpeed(500);
        form.setFocusScrolling(true);

        form.addCommand( OpenRosaBackCommand.getInstance().getCommand() );
        form.addCommand( OpenRosaInterviewSaveCommand.getInstance().getCommand() );

        try {
            form.removeCommandListener(this);
        } catch (NullPointerException npe) {
        }
        form.addCommandListener(this);

        createGroupList();
    }

    private void createGroupList(){
        underlyingModel = new DefaultListModel( surveyModel.getGroups() );

        groupList = new List( underlyingModel );
        groupList.setItemGap( 0 );
        groupList.addActionListener( this );
        groupList.setListCellRenderer( new GroupsListCellRenderer() );
        form.addComponent( groupList );
    }

    public void actionPerformed( ActionEvent ae ) {
        Object cmd = ae.getSource();
        if (cmd == OpenRosaBackCommand.getInstance().getCommand()) {
            GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_YES_NO, true);
            if(surveyModel.isChanged() && GeneralAlert.RESULT_YES ==  GeneralAlert.getInstance().show( Resources.CMD_SAVE,
                                             Resources.SAVE_SURVEY_QUESTION,
                                             GeneralAlert.CONFIRMATION)){
                saveSurvey();
                OpenRosaBackCommand.getInstance().execute( null );
            }else{
                OpenRosaBackCommand.getInstance().execute(null);
            }
        } else if ( cmd == groupList  ){
            OpenRosaGroup current = (OpenRosaGroup) surveyModel.getGroups().elementAt( groupList.getSelectedIndex() );
            ShowGroupCommand.getInstance().execute( current );
        } else if ( cmd == OpenRosaInterviewSaveCommand.getInstance().getCommand() ){
            saveSurvey();
        }
    }

    private void saveSurvey(){
        OpenRosaInterviewSaveCommand.getInstance().setStartDate( startDate );
        OpenRosaInterviewSaveCommand.getInstance().execute( surveyModel.getOpenRosaDocument() );
    }
}
