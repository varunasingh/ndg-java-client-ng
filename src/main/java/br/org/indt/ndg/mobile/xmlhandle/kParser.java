package br.org.indt.ndg.mobile.xmlhandle;

import br.org.indt.ndg.lwuit.control.ExitCommand;
import br.org.indt.ndg.lwuit.ui.GeneralAlert;
import br.org.indt.ndg.mobile.logging.Logger;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.structures.FileSystemSurveyStructure;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import org.kxml2.io.*;
import org.xmlpull.v1.*;


public class kParser {
    private boolean error = false;
    private FileSystemSurveyStructure structure;

    public boolean getError() {
        return error;
    }

    public void parserSurveyFileInfo(String filepath) {
        FileConnection fc = null;
        InputStream is = null;
        try {
            fc = (FileConnection) Connector.open(filepath);
            is = fc.openInputStream();
             //Inicia o XMLParser
            KXmlParser parser = new KXmlParser();
            parser.setFeature(KXmlParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(new InputStreamReader(is, "UTF-8"));
            parser.nextTag();

            String name = null;
            if ( parser.getName().equals("xforms") || parser.getName().equals("html") ) {
                name = filepath; // filepath sa default survey name
                while ( parser.next() != KXmlParser.END_DOCUMENT ) {
                    // First occurance of unempty <title> tag or <data> with 'id' attrubute tag will act as survey name
                    if ( parser.getEventType() == KXmlParser.START_TAG &&
                            parser.getName().equals("title") )
                    {
                        String tempName = parser.nextText();
                        if ( tempName != null && !tempName.equals("") ) {
                            name = tempName;
                            break;
                        }
                    }
                    if ( parser.getEventType() == KXmlParser.START_TAG &&
                            parser.getName().equals("data") &&
                            parser.getAttributeValue("", "id") != null)
                    {
                        name = parser.getAttributeValue("", "id");
                        break;
                    }
                }
            } else {
                Logger.getInstance().log("Unrecognized survey: " + filepath);
            }
            if(name != null){
                structure.addName(name);
            }
        } catch(XmlPullParserException e) {
            Logger.getInstance().logException("XmlPullParserException[parserSurveyFileInfo]: " + e.getMessage());
            error = true;
        } catch(Exception e) {
            Logger.getInstance().logException("Exception[parserSurveyFileInfo]: " + e.getMessage());
            error = true;
            GeneralAlert.getInstance().addCommand( ExitCommand.getInstance());
            GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.EPARSE_GENERAL, GeneralAlert.ERROR );
        } finally {
            try {
                if ( is != null )
                    is.close();
                if ( fc != null )
                    fc.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setFileSystemSurveyStructure(FileSystemSurveyStructure _structure) {
        structure = _structure;
    }
}


