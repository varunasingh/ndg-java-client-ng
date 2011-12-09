package br.org.indt.ndg.lwuit.ui.openrosa.model;

import com.nokia.xfolite.xml.dom.Element;
import java.util.Vector;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaGroup {
    private Vector /*<OpenRosaQuestion>*/ questions = null;
    private Element element = null; //TODO can be remove
    private String label = null;
    private boolean isDefault = false; ///TODO remove

    private boolean changed = false;

    public OpenRosaGroup(){//creates default group
        questions = new Vector();
        label = "Defult";
        isDefault = true;
    }

    public OpenRosaGroup( Element elem ){
        element = elem;
        questions = new Vector();
        isDefault = false;
        label = "";
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged( boolean changed ) {
        this.changed = changed;
    }

    public void setGroupLabel( String label ){
         this.label = label;
    }

    public void addQuestion(OpenRosaQuestion question){
        questions.addElement( question );
    }

    public String toString(){
        return label;
    }

    public Vector getQuestions(){
        return questions;
    }

    public String getName() {
        return label;
    }
}
