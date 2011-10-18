package br.org.indt.ndg.lwuit.ui.openrosa.model;

import br.org.indt.ndg.lwuit.ui.openrosa.OpenRosaResourceManager;
import com.nokia.xfolite.xforms.dom.BoundElement;
import com.nokia.xfolite.xforms.model.datatypes.DataTypeBase;
import com.nokia.xfolite.xml.dom.Element;
import com.nokia.xfolite.xml.dom.WidgetFactory;
import com.nokia.xfolite.xml.xpath.XPathNSResolver;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaSurveyFactory implements WidgetFactory, XPathNSResolver{

    private OpenRosaSurvey surveyModel;

    public OpenRosaSurveyFactory( OpenRosaSurvey survey ){
        surveyModel = survey;
    }

    public void elementParsed( Element el ) {
        String tagName = el.getLocalName();
        if ( tagName.equals( "group" ) ) {
            addGroup( el );
        }
    }

    public void childrenParsed( Element el ) {
        BoundElement binding = null;
        if (el instanceof BoundElement) {
            binding = (BoundElement) el;
        }
        String tagName = binding.getLocalName();

        if ( tagName.equals( "input" ) || tagName.equals( "secret" )) {
            DataTypeBase a = binding.getDataType();
            if( a != null ){
                a.getBaseTypeID();
            }
            addQuestion( binding, OpenRosaQuestion.TYPE_INPUT );
        } else if ( tagName.equals( "select" ) ) {
            addQuestion( binding, OpenRosaQuestion.TYPE_SELECT );
        } else if ( tagName.equals( "range" ) ) {
            addQuestion( binding, OpenRosaQuestion.TYPE_INPUT );
        } else if ( tagName.equals( "upload" ) ) {
            String mediatype = el.getAttribute("mediatype");
            if(mediatype!=null && mediatype.indexOf("image") > -1) {
                addQuestion( binding, OpenRosaQuestion.TYPE_UPLOAD_IMAGE );
            }
        } else if ( tagName.equals( "select1" ) ) {
                addQuestion( binding, OpenRosaQuestion.TYPE_SELECT1 );
        } else if ( tagName.equals( "value" ) ) {
            addTextValue( el );
        } else if ( tagName.equals( "group" ) ) {
            surveyModel.setGroupLabel( OpenRosaSurvey.getResourceManager().tryGetLabelForElement( el ) );
        } else {
        }
    }

    public void addTextValue(Element el){
        Element parent = (Element)el.getParentNode();
        if(!parent.getNodeName().equals("text")){
            return;
        }

        String id = parent.getAttribute("id");
        String value = el.getText();

        OpenRosaSurvey.getResourceManager().put( id, value );
    }


    public void addGroup( Element el ){
        surveyModel.addGroup( new OpenRosaGroup( el ) );
    }

    public void addQuestion( BoundElement questionElement, int type ){
        surveyModel.addQuestion( new OpenRosaQuestion( questionElement, type));
    }

    public void removingElement( Element elmnt ) {
    }

    public void elementInitialized( Element elmnt ) {
    }

    public void childrenInitialized( Element elmnt ) {
    }

    public String lookupNamespaceURI( String string ) {
        return "";
    }

}
