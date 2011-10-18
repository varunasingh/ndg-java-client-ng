package br.org.indt.ndg.lwuit.ui.openrosa.model;

import br.org.indt.ndg.lwuit.ui.GeneralAlert;
import br.org.indt.ndg.lwuit.ui.openrosa.OpenRosaResourceManager;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.NdgConsts;
import br.org.indt.ndg.mobile.Resources;
import com.nokia.xfolite.xforms.dom.UserInterface;
import com.nokia.xfolite.xforms.dom.XFormsDocument;
import com.nokia.xfolite.xforms.submission.MultipartFormDataSerializer;
import com.nokia.xfolite.xforms.submission.MultipartRelatedSerializer;
import com.nokia.xfolite.xforms.submission.XFormsXMLSerializer;
import com.nokia.xfolite.xml.dom.Document;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.WidgetFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaSurvey implements UserInterface{

    private Vector/*<OpenRosaCategory>*/ groups = null;
    private XFormsDocument openRosaDocument = null;


    private WidgetFactory surveyFactory = null;
    private static OpenRosaResourceManager resourceManager = new OpenRosaResourceManager();

    private OpenRosaGroup currentGroup = null;

    public OpenRosaSurvey(){

        groups = new Vector();
        currentGroup = new OpenRosaGroup();
        groups.addElement( currentGroup );

        openRosaDocument = new XFormsDocument();
        openRosaDocument.setStrictMode( false );
    }

    public Vector getGroups(){
        return groups;
    }

    public static OpenRosaResourceManager getResourceManager() {
        return resourceManager;
    }

    public XFormsDocument getOpenRosaDocument() {
        return openRosaDocument;
    }

    public boolean isChanged(){
        boolean changed = false;
        for( int idx = 0; idx < groups.size(); idx++ ){
            if( ( (OpenRosaGroup)groups.elementAt( idx )).isChanged() ){
                changed = true;
                break;
            }
        }
        return changed;
    }

///---------init document ----------------------------------
    public void initialize(){
        addResultData();

        String dirName = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
        String file = AppMIDlet.getInstance().getRootDir() + dirName + NdgConsts.SURVEY_NAME;
        load( file ) ;
    }

    public void addResultData(){
        Document resultData = AppMIDlet.getInstance().getFileStores().getXFormResult();

        if(openRosaDocument != null && resultData != null ){
            openRosaDocument.addInstance( resultData, "" );
        }
    }

    public void load( String url ) {
        if( url == null || url.equals( "" ) ){
            return; //Throw exception??
        }
        try {
            FileConnection fc = (FileConnection) Connector.open(url, Connector.READ);
            InputStream is = fc.openInputStream();

            loadDocument(is);

            is.close();
            fc.close();
        } catch (Exception ex){
            ex.printStackTrace();
            loadError();
        }
    }
    private void loadError(){
        GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
        GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.OR_FORM_LOADING_FAILURE, GeneralAlert.ERROR);
    }


    private void loadDocument(InputStream inputStream)  {
        KXmlParser parser = new KXmlParser();
        try {
            resourceManager.clear();
            surveyFactory = createSurveyFactory();

            parser.setInput(inputStream, "UTF-8");
            openRosaDocument.setRendererFactory(surveyFactory);
            openRosaDocument.setUserInterface(this);

            openRosaDocument.addSerializer(new XFormsXMLSerializer());
            openRosaDocument.addSerializer(new MultipartRelatedSerializer());
            openRosaDocument.addSerializer(new MultipartFormDataSerializer());

            openRosaDocument.parse(parser);

            removeEmptyGroup();
        }catch(IOException ex ){
            ex.printStackTrace();
        }catch (XmlPullParserException ex) {
            ex.printStackTrace();
        }
    }


    private void removeEmptyGroup(){
        for( int i = 0; i < groups.size(); i++ ){
            OpenRosaGroup gr = (OpenRosaGroup)groups.elementAt( i );
            if( gr.getQuestions().isEmpty() ){
                groups.removeElement( gr );
            }
        }
    }

    public WidgetFactory createSurveyFactory() {
        surveyFactory = new OpenRosaSurveyFactory( this );
        return surveyFactory;
    }


///---------create document ----------------------------------
    public void addGroup( OpenRosaGroup group ){
        currentGroup = group;
        groups.addElement( group );
    }

    public void addQuestion( OpenRosaQuestion question ){
        currentGroup.addQuestion( question );
    }

    public void addQuestionToDefault( OpenRosaQuestion question ){
        ( ( OpenRosaGroup ) groups.elementAt( 0 )).addQuestion( question );
    }

    public void setGroupLabel( String label ){
        currentGroup.setGroupLabel( label );
    }

///--------- inherited method ----------------------------------
    public void log( int i, String string, Element elmnt ) {
    }

    public void callSerially( Runnable r ) {
    }

    public void callParallel( Runnable r ) {
    }

    public void close() {
    }

    public void showMessage( String string ) {
    }


    public void setTitle( String string ) {
    }

    public String getProperty( String string ) {
        return "";
    }
 }
