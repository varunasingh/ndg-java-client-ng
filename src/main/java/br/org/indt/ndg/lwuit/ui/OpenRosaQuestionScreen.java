package br.org.indt.ndg.lwuit.ui;

import br.org.indt.ndg.lwuit.control.BackToCategoryCommand;
import br.org.indt.ndg.lwuit.control.OpenFileBrowserCommand;
import br.org.indt.ndg.lwuit.control.RemovePhotoCommand;
import br.org.indt.ndg.lwuit.control.ShowPhotoCommand;
import br.org.indt.ndg.lwuit.control.SurveysControl;
import br.org.indt.ndg.lwuit.control.TakePhotoCommand;
import br.org.indt.ndg.lwuit.extended.CheckBox;
import br.org.indt.ndg.lwuit.extended.DateField;
import br.org.indt.ndg.lwuit.extended.DescriptiveField;
import br.org.indt.ndg.lwuit.extended.NumericField;
import br.org.indt.ndg.lwuit.extended.RadioButton;
import br.org.indt.ndg.lwuit.extended.TimeField;
import br.org.indt.ndg.lwuit.model.ImageData;
import br.org.indt.ndg.lwuit.ui.camera.CameraManagerListener;
import br.org.indt.ndg.lwuit.ui.camera.OpenRosaCameraManager;
import br.org.indt.ndg.lwuit.ui.openrosa.OpenRosaUtils;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaGroup;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaQuestion;
import br.org.indt.ndg.lwuit.ui.openrosa.model.OpenRosaSurvey;
import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.multimedia.Base64Coder;
import com.nokia.xfolite.xforms.dom.BoundElement;
import com.nokia.xfolite.xforms.dom.XFormsElement;
import com.nokia.xfolite.xforms.model.MIPExpr;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeDate;
import com.nokia.xfolite.xml.dom.Node;
import com.nokia.xfolite.xml.xpath.NodeSet;
import com.sun.lwuit.Button;
import com.sun.lwuit.ButtonGroup;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;
import com.sun.lwuit.plaf.Border;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaQuestionScreen extends Screen implements ActionListener{

    private String title1;
    private String title2;
    private OpenRosaGroup group = null;

    private Vector containers = null;

    protected void loadData() {
        group = AppMIDlet.getInstance().getFileStores().getSelectedGroup();

        title1 = SurveysControl.getInstance().getSurveyTitle();
        title2 = Resources.NEW_INTERVIEW;

        containers = new Vector();
    }

    protected void customize() {
        setTitle( title1, title2 );

        form.removeAllCommands();
        form.removeAll();

        form.setLayout( new BoxLayout( BoxLayout.Y_AXIS ) );
        form.getContentPane().getStyle().setBorder( Border.createEmpty(), false );
        form.setScrollAnimationSpeed( 500 );
        form.setFocusScrolling( true );

        form.addCommand( BackToCategoryCommand.getInstance().getCommand() );

        try {
        form.removeCommandListener( this );
        } catch (NullPointerException npe) {
        }
        form.addCommandListener( this );

        appendQuestions();
    }

    private void appendQuestions(){
        Vector questions = group.getQuestions();

        for ( int idx = 0; idx <  questions.size(); idx++ ){
            OpenRosaQuestion que = (OpenRosaQuestion)questions.elementAt( idx );
            ContainerUI comp = null;
            switch ( que.getType() ){
                case OpenRosaQuestion.TYPE_INPUT:
                    comp = createInput( que.getBoundElement() ) ;
                    break;
                case OpenRosaQuestion.TYPE_SELECT:
                    comp = new XfoilMultipleChoiceFieldUI( que.getBoundElement() );
                    break;
                case OpenRosaQuestion.TYPE_SELECT1:
                    comp = new XfoilExclusiveChoiceFieldUI( que.getBoundElement() );
                    break;
                case OpenRosaQuestion.TYPE_UPLOAD_IMAGE:
                    comp = new XfoilPhotoFieldUi( que.getBoundElement() );
                    break;
                default:
            }
            if(comp != null ){
                comp.setParent( this );
                containers.addElement( comp );
                form.addComponent( comp );
            }
        }
        refreshAll();
    }

    private ContainerUI createInput(BoundElement bindElem) {
        DataTypeBase a = bindElem.getDataType();
        ContainerUI question = null;

        if (a != null) {
            switch (a.getBaseTypeID()) {
                case DataTypeBase.XML_SCHEMAS_DATE:
                    question = new XfoilDateFieldUI(bindElem);
                    break;
                case DataTypeBase.XML_SCHEMAS_TIME:
                    question = new XfoilTimeFieldUI(bindElem);
                    break;
                case DataTypeBase.XML_SCHEMAS_STRING:
                    question = new XfoilDescriptiveFieldUI(bindElem);
                    break;
                case DataTypeBase.XML_SCHEMAS_DECIMAL:
                    question = new XfoilNumericFieldUI(bindElem, true);
                    break;
                case DataTypeBase.XML_SCHEMAS_INTEGER:
                    question = new XfoilNumericFieldUI(bindElem, false);
                    break;
                default:
                case DataTypeBase.XML_SCHEMAS_UNKNOWN:
                    question = new XfoilMockComponent(bindElem);
            }
        }
       return question;
    }

    public boolean commitValues() {
        boolean result = true;
        for (int i = 0; i < containers.size(); i++) {
            if (!((ContainerUI) containers.elementAt(i)).validate()) {
                result = false;
                ((ContainerUI) containers.elementAt(i)).showBadInputError();
                break;
            }
        }
        if(result == true) {
            for (int i = 0; i < containers.size(); i++) {
                ((ContainerUI) containers.elementAt(i)).commitValue();
            }
        }
        return result;
    }

    public boolean isFormChanged(){
        for (int i = 0; i < containers.size(); i++) {
            if (((ContainerUI) containers.elementAt(i)).isChanged()){
                return true;
            }
        }
        return false;
    }

    public void actionPerformed( ActionEvent ae ) {
        Object cmd = ae.getSource();
        if ( cmd == BackToCategoryCommand.getInstance().getCommand() ) {
            if( isFormChanged() ){
                if ( !commitValues() ) {
                    return;
                }
                group.setChanged( true );
            }
            BackToCategoryCommand.getInstance().execute( null );
        }
    }

    public void refreshAll(){
        for(int idx = 0; idx < containers.size(); idx++ ){
            ((ContainerUI)containers.elementAt( idx )).refresh();
        }
    }
}

abstract class ContainerUI extends Container implements FocusListener {

    protected TextArea qname;
    protected BoundElement element;
    protected OpenRosaQuestionScreen parentScreen = null;

    protected void commitValue(String input) {
        element.setStringValue(input);
        element.getModel().recalculate();

        if( parentScreen != null ){
            parentScreen.refreshAll();
        }
    }

    public abstract boolean isChanged();

    public abstract void commitValue();

    public void setEnabled( boolean enabled ){
        qname.setEnabled( enabled );
    }

    public void handleMoreDetails(Object cmd) {
    }

    public ContainerUI(BoundElement element) {

        getStyle().setBorder(Border.createBevelLowered(NDGStyleToolbox.getInstance().focusLostColor,
                NDGStyleToolbox.getInstance().focusLostColor,
                NDGStyleToolbox.getInstance().focusLostColor,
                NDGStyleToolbox.getInstance().focusLostColor));
        this.element = element;
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        addFocusListener(this);
    }

    protected void addQuestionName() {

        addComponent( new Label("") );
        qname = UIUtils.createQuestionName( OpenRosaSurvey.getResourceManager().tryGetLabelForElement( element ) );

        this.addComponent( qname );
    }

    public void refresh(){
        boolean relevant = element.getBooleanState( MIPExpr.RELEVANT );
        setEnabled( relevant );
    }
    public void setParent( OpenRosaQuestionScreen screen ){
        this.parentScreen = screen;
    }

    protected boolean validate(){
        commitValue();
        return element.getBooleanState( MIPExpr.CONSTRAINT );
    }

    public BoundElement getElement() {
        return element;
    }

    public void showBadInputError() {
        String constraint = element.getConstraintString();

        GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
        if (constraint != null && !constraint.equals( "" ) ) {
            GeneralAlert.getInstance().show(
                                        Resources.CONSTRAINTS,
                                        constraint,
                                        GeneralAlert.WARNING);

        } else {
            GeneralAlert.getInstance().show(
                    Resources.CONSTRAINTS,
                    Resources.OR_INVALID_INPUT,
                    GeneralAlert.WARNING);
        }
    }

    public void focusGained(Component cmpnt) {
        getStyle().setBorder(Border.createBevelLowered(NDGStyleToolbox.getInstance().focusGainColor,
                NDGStyleToolbox.getInstance().focusGainColor,
                NDGStyleToolbox.getInstance().focusGainColor,
                NDGStyleToolbox.getInstance().focusGainColor), false);
        refreshTheme();
    }

    public static boolean blockVal = true;
    public void focusLost(Component cmpnt) {
        if(!cmpnt.getComponentForm().isVisible()){
            return;
        }

        if ( !validate() && blockVal) {
            blockVal = false;
            cmpnt.requestFocus();
            showBadInputError();
            blockVal = true;
            return;
        }
        getStyle().setBorder(Border.createBevelLowered(NDGStyleToolbox.getInstance().focusLostColor,
                NDGStyleToolbox.getInstance().focusLostColor,
                NDGStyleToolbox.getInstance().focusLostColor,
                NDGStyleToolbox.getInstance().focusLostColor), false);
        refreshTheme();
    }

    protected String getValue(XFormsElement el) {
        XFormsElement valueEl = (XFormsElement) el.getUserData(XFormsElement.VALUE_KEY);
        if (valueEl != null) {
            return valueEl.getText();
        } else {
            return "";
        }
    }
}

class XfoilPhotoFieldUi extends ContainerUI implements  ActionListener, CameraManagerListener {

    private Container mImageContainer;
    private Button imageButton;
    private boolean isChanged = false;

    public XfoilPhotoFieldUi(BoundElement element){
        super(element);
        OpenRosaCameraManager.getInstance().reset();
        AppMIDlet.getInstance().setCurrentCameraManager(OpenRosaCameraManager.getInstance());

        addQuestionName();
        addPhotoContainer();
    }

    public void focusGained(Component cmpnt) {
        rebuildOptionMenu();
    }

    public void focusLost(Component cmpnt) {
        removePhotoCommands();
        super.focusLost(cmpnt);
    }

    private void rebuildOptionMenu(){
        removePhotoCommands();
        addPhotoCommands();
    }

    private void addPhotoCommands(){
        if ( OpenRosaCameraManager.getInstance().getImageArray() != null ) {
            getComponentForm().addCommand(RemovePhotoCommand.getInstance().getCommand());
            getComponentForm().addCommand(ShowPhotoCommand.getInstance().getCommand());
        }
        getComponentForm().addCommand(OpenFileBrowserCommand.getInstance().getCommand());
        getComponentForm().addCommand(TakePhotoCommand.getInstance().getCommand());
    }

    private void removePhotoCommands(){
        getComponentForm().removeCommand(OpenFileBrowserCommand.getInstance().getCommand());
        getComponentForm().removeCommand(TakePhotoCommand.getInstance().getCommand());
        getComponentForm().removeCommand(ShowPhotoCommand.getInstance().getCommand());
        getComponentForm().removeCommand(RemovePhotoCommand.getInstance().getCommand());
    }

    public boolean isChanged(){
        return isChanged;
    }

    public void commitValue() {
//        element.setStringValue(OpenRosaCameraManager.getInstance().getImageStringValue());
        commitValue( OpenRosaCameraManager.getInstance().getImageStringValue() );
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled( enabled );
        mImageContainer.setEnabled( enabled );
        imageButton.setEnabled( enabled );
    }

    protected boolean validate() {
        return true;
    }

    private void addPhotoContainer(){
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        mImageContainer = new Container (new FlowLayout());

        Image thumbnail = null;
        if(element.getStringValue() != null && !element.getStringValue().equals("")){
            byte[] byteArray = Base64Coder.decode(element.getStringValue());
            OpenRosaCameraManager.getInstance().setImageArray(byteArray);
        }

        imageButton = new Button();
        imageButton.setIcon(thumbnail);
        imageButton.addActionListener(this);
        imageButton.setAlignment(Component.LEFT);
        imageButton.setFocusable(true);
        imageButton.addFocusListener(this);
        mImageContainer.addComponent(imageButton);

        updateImageButton();

        addComponent(mImageContainer);
    }

    private void updateImageButton(){
        Image thumbnail = null;
        byte[] image = OpenRosaCameraManager.getInstance().getImageArray();
        if(image != null){
            Image img = Image.createImage(image, 0, image.length);
            thumbnail = img.scaled(ImageData.THUMBNAIL_SIZE, ImageData.THUMBNAIL_SIZE);
//            thumbnail = Camera.createThumbnail(img);
        }else{
            thumbnail = Screen.getRes().getImage("camera-icon");
        }

        imageButton.setIcon(thumbnail);
    }

    public void actionPerformed(ActionEvent cmd) {
        if ( cmd.getSource() instanceof Button ) {
            OpenRosaCameraManager.getInstance().sendPostProcessData(this);
            if(OpenRosaCameraManager.getInstance().getImageArray() == null){
                new ImageQuestionContextMenu(0, ImageQuestionContextMenu.TWO_ACTIONS_CONTEXT_MENU).show();
            }else{
                new ImageQuestionContextMenu(0, ImageQuestionContextMenu.FOUR_ACTIONS_CONTEXT_MENU).show();
            }
        }
    }

    public void update() {
        isChanged = true;
        updateImageButton();
        rebuildOptionMenu();
        getComponentForm().showBack();
    }
}

class XfoilDescriptiveFieldUI extends ContainerUI {

    DescriptiveField tfDesc = null;

    public XfoilDescriptiveFieldUI(BoundElement element) {
        super(element);
        addQuestionName();
        addDescriptionQuestion(element);
    }

    public void commitValue() {
        commitValue(tfDesc.getText());
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled( enabled );
        tfDesc.setEnabled( enabled );
    }

    public boolean isChanged(){
        if(tfDesc.getText().equals(element.getStringValue())){
            return false;
        }else{
            return true;
        }
    }

    private void addDescriptionQuestion(BoundElement bindElem) {
        String strValue = bindElem.getStringValue().trim();

        tfDesc = new DescriptiveField();
        tfDesc.setInputMode("Abc");
        tfDesc.setEditable(true);
        tfDesc.setFocusable(true);
        if (strValue != null) {
            tfDesc.setText(strValue);
        }
        tfDesc.addFocusListener(this);
        this.addComponent(tfDesc);
    }
}

class XfoilNumericFieldUI extends ContainerUI {

    NumericField nfNumber = null;

    public XfoilNumericFieldUI(BoundElement element, boolean isDecimal) {
        super(element);
        addQuestionName();
        addNumericQuestion(element, isDecimal);
    }

    public void commitValue() {
        commitValue(nfNumber.getText());
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled( enabled );
        nfNumber.setEnabled( enabled );
    }

    public boolean isChanged(){
        if(nfNumber.getText().equals(element.getStringValue())){
            return false;
        }else{
            return true;
        }
    }

    private void addNumericQuestion(BoundElement bindElem, boolean isDecimal) {
        String value = bindElem.getStringValue().trim();
        nfNumber = new NumericField(50, isDecimal);
        nfNumber.setFocusable(true);
        if (value != null) {
            nfNumber.setText(value);
        }
        nfNumber.addFocusListener(this);
        this.addComponent(nfNumber);
    }
}

class XfoilDateFieldUI extends ContainerUI {
    DateField dfDate;

    public XfoilDateFieldUI(BoundElement element) {
        super(element);
        addQuestionName();
        addDateQuestion(element);
    }

    public void commitValue() {
        commitValue(OpenRosaUtils.getStringFromDate(dfDate.getDate()));
    }

    public void setEnabled( boolean enabled ) {
        super.setEnabled( enabled );
        dfDate.setEnabled( enabled );
    }

    public boolean isChanged(){

        String dateStr = OpenRosaUtils.getStringFromDate(dfDate.getDate());
        if(dateStr.equals(element.getStringValue())){
            return false;
        }else{
            return true;
        }
    }

    private void addDateQuestion(BoundElement bindElem) {
        Date date = null;
        String value = bindElem.getStringValue().trim();

        if (value != null || value != "") {
            date = OpenRosaUtils.getDateFromString(value);
       }

        if(date == null){
            date = Calendar.getInstance().getTime();
        }

        dfDate = new DateField(AppMIDlet.getInstance().getSettings().getStructure().getDateFormatId(), '/');
        dfDate.setDate(date);
        dfDate.setEditable(true);
        dfDate.addFocusListener(this);

        addComponent(dfDate);
    }

}

class XfoilTimeFieldUI extends ContainerUI {
    TimeField dfTime;

    public XfoilTimeFieldUI(BoundElement element) {
        super(element);
        addQuestionName();
        addTimeQuestion(element);
    }

    public void commitValue() {
        Calendar cal = Calendar.getInstance();
        cal.setTime( dfTime.getTime() );
        commitValue( DataTypeDate.calendar2xsdTime( cal ) );
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled( enabled );
        dfTime.setEnabled( enabled );
    }

    private void addTimeQuestion(BoundElement bindElem) {
        String value = bindElem.getStringValue().trim();
        if (value != null && (value == null ? "" != null : !value.equals(""))) {
            dfTime = new TimeField( DataTypeDate.xsdTime2Calendar( value ).getTime(), TimeField.HHMM, ':');
        } else {
            dfTime = new TimeField(TimeField.HHMM);
        }
        dfTime.setEditable(true);
        addComponent(dfTime);
    }

    public boolean isChanged() {
        Date timeUi = dfTime.getTime();
        String savedTime = element.getStringValue();

        if( savedTime == null || savedTime.length() <=0 ) {
            return true;
        }

        Date timeXml = DataTypeDate.xsdTime2Calendar( savedTime ).getTime();
        if( timeUi.equals( timeXml ) ) {
            return false;
        }else{
            return true;
        }
    }
}

class XfoilMultipleChoiceFieldUI extends ContainerUI {

    private Vector cbs = new Vector();
    private String[] names = null;
    private String[] values = null;


    public XfoilMultipleChoiceFieldUI(BoundElement element) {
        super(element);
        addQuestionName();
        addSelectQuestion(element);
    }

    public void commitValue() {
        commitValue(getSelectedString());
    }

    private String getSelectedString(){
        String valStr = "";
        for (int i = 0; i < cbs.size(); i++) {
            CheckBox cb = (CheckBox)cbs.elementAt(i);
            if( cb.isSelected() ) {
                String tempVal = getValue( cb.getText() ) + " ";
                if( tempVal != null ){
                    valStr += tempVal + " ";
                }
            }
        }
        return valStr;
    }

    private String getValue( String label ){
        for(int i = 0; i < names.length; i++ ){
            if( names[i].equals( label ) ){
                return values[i];
            }
        }
        return null;
    }

    public void setEnabled( boolean enabled ) {
        super.setEnabled( enabled );
        for (int i = 0; i < cbs.size(); i++) {
            ((CheckBox)cbs.elementAt(i)).setEnabled( enabled );
        }
    }

    public boolean isChanged(){
        if(getSelectedString().equals(element.getStringValue())){
            return false;
        }else{
            return true;
        }
    }

    private void addSelectQuestion(BoundElement bindElem) {
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
        names = new String[length];
        values = new String[length];
        boolean[] selected = new boolean[length];


        String chosenVal = " " + bindElem.getStringValue() + " ";
        for (int i = 0; i < length; i++) {
            XFormsElement n = (XFormsElement) choices.item(i);
            String label = OpenRosaSurvey.getResourceManager().tryGetLabelForElement(n);
            String value = getValueForItemElement( n );

            if (!value.equals("") && chosenVal.indexOf(" " + value + " ") >= 0) {
                selected[i] = true;
            } else {
                selected[i] = false;
            }
            names[i] = label;
            values[i] = value;
        }

        for (int i = 0; i < length; i++) {
            CheckBox cb = new CheckBox(names[i]);
            cb.setSelected(selected[i]);
            cbs.addElement(cb);
            addComponent(cb);
        }
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
}

class XfoilExclusiveChoiceFieldUI extends ContainerUI {

    private ButtonGroup groupButton;
    private String[] values;

    public XfoilExclusiveChoiceFieldUI(BoundElement element) {
        super(element);
        addQuestionName();
        addSelect1Question(element);
    }

    public void commitValue() {
        commitValue(getSelectedString());
    }

    private String getSelectedString(){
        int index = groupButton.getSelectedIndex();
        if( index >= 0 && index < values.length ){
            return values[index];
        }
        return "";
    }

    public boolean isChanged(){
        if(getSelectedString().equals(element.getStringValue())){
            return false;
        }else{
            return true;
        }
    }

    public void setEnabled( boolean enabled ) {
        super.setEnabled( enabled );
        for ( int i = 0; i < groupButton.getButtonCount(); i++ ){
            groupButton.getRadioButton( i ).setEnabled( enabled );
        }
    }

    private void addSelect1Question(BoundElement bindElem) {
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
        String[] names = new String[length];
        values = new String[length];
        boolean[] selected = new boolean[length];

        String chosenVal = " " + bindElem.getStringValue() + " ";
        for (int i = 0; i < length; i++) {
            XFormsElement n = (XFormsElement) choices.item(i);
            String label = OpenRosaSurvey.getResourceManager().tryGetLabelForElement(n);
            String value = getValue(n);

            if (!value.equals("") && chosenVal.indexOf(" " + value + " ") >= 0) {
                selected[i] = true;
            } else {
                selected[i] = false;
            }
            names[i] = label;
            values[i] = value;
        }
        groupButton = new ButtonGroup();

        int totalChoices = names.length;
        String[] choicesStrings = new String[totalChoices];
        for (int i = 0; i < totalChoices; i++) {
            choicesStrings[i] = (String) names[i];
            RadioButton rb = new RadioButton(choicesStrings[i]);
            rb.useMoreDetails(values[i].equals("1"));
//            rb.setOtherText(""); // TODO this probably should not be commented! for test only! //Initializes with empty string
            rb.setSelected(selected[i]);
            //rb.addActionListener(new HandleMoreDetails()); // More Details
            //rb.addFocusListener(this); // Controls when changing to a new question
            groupButton.add(rb);
            addComponent(rb);
        }
    }
}

class XfoilMockComponent extends ContainerUI {

    public XfoilMockComponent(BoundElement element) {
        super(element);
        addQuestionName();
        addMockLabel();
    }

    public void commitValue() {
        // mock
    }

    protected boolean validate() {
        return true;
    }

    public boolean isChanged(){
        return false;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled( enabled );
        //do nothing
    }

    private void addMockLabel() {
        addComponent(UIUtils.createQuestionName(Resources.UNSUPPORTED_TYPE));
    }
}
