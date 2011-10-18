package br.org.indt.ndg.lwuit.ui;

import br.org.indt.ndg.lwuit.control.OpenRosaBackCommand;
import br.org.indt.ndg.lwuit.control.OpenRosaInterviewSaveCommand;
import br.org.indt.ndg.lwuit.control.SaveResultsObserver;
import br.org.indt.ndg.lwuit.control.ShowGroupCommand;
import br.org.indt.ndg.lwuit.control.SurveysControl;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaGroup;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaSurvey;
import br.org.indt.ndg.lwuit.ui.renderers.SimpleListCellRenderer;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.DefaultListModel;
import com.sun.lwuit.plaf.Border;
import java.util.Date;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaGroupScreen extends Screen implements ActionListener, SaveResultsObserver{

    private OpenRosaSurvey surveyModel = null;
    private String title1;
    private String title2;
    private Date startDate;

    List groupList = null;

    protected void loadData() {
       surveyModel = AppMIDlet.getInstance().getFileStores().getSurveyModel();
       startDate = new Date();

       title1 = SurveysControl.getInstance().getSurveyTitle();
       title2 = Resources.NEW_INTERVIEW;
       setTitle( title1, title2 );
       startDate = new Date();
    }

    protected void customize() {

        form.removeAllCommands();
        form.removeAll();

        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        form.getContentPane().getStyle().setBorder(Border.createEmpty(), false);
        form.setScrollAnimationSpeed(500);
        form.setFocusScrolling(true);

        form.addCommand( OpenRosaBackCommand.getInstance().getCommand() );
        form.addCommand( OpenRosaInterviewSaveCommand.getInstance().getCommand() );
//        form.addCommand(OpenRosaInterviewSaveCommand.getInstance().getCommand());

        try {
            form.removeCommandListener(this);
        } catch (NullPointerException npe) {
        }
        form.addCommandListener(this);

        createGroupList();

    }

    private void createGroupList(){



        DefaultListModel listModel = new DefaultListModel( surveyModel.getGroups() );

        groupList = new List( listModel );
        groupList.setItemGap( 0 );
        groupList.addActionListener( this );
        groupList.setListCellRenderer( new SimpleListCellRenderer() );
        groupList.setFixedSelection( List.FIXED_NONE_CYCLIC );
        form.addComponent( groupList );
        form.setScrollable( false );
    }

    public void actionPerformed( ActionEvent ae ) {
        Object cmd = ae.getSource();
        if (cmd == OpenRosaBackCommand.getInstance().getCommand()) {
            GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_YES_NO, true);
            if(surveyModel.isChanged() && GeneralAlert.RESULT_YES ==  GeneralAlert.getInstance().show( Resources.CMD_SAVE,
                                             Resources.SAVE_SURVEY_QUESTION,
                                             GeneralAlert.CONFIRMATION)){
                saveSurvey();
            }else{
                OpenRosaBackCommand.getInstance().execute(null);
            }


            OpenRosaBackCommand.getInstance().execute( null );
        } else if ( cmd == groupList  ){
            OpenRosaGroup current = (OpenRosaGroup) surveyModel.getGroups().elementAt( groupList.getSelectedIndex() );
            ShowGroupCommand.getInstance().execute( current );
        } else if ( cmd == OpenRosaInterviewSaveCommand.getInstance().getCommand() ){
            saveSurvey();
        }
    }

    private void saveSurvey(){
        OpenRosaInterviewSaveCommand.getInstance().setObserver( this );
        OpenRosaInterviewSaveCommand.getInstance().setStartDate( startDate );
        OpenRosaInterviewSaveCommand.getInstance().execute( surveyModel.getOpenRosaDocument() );
    }

    public void onResultsSaved() {
        AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.ResultList.class);
    }
}
