/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.FileSystem;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.XmlResultFile;
import br.org.indt.ndg.mobile.error.WaitingForm;
import br.org.indt.ndg.lwuit.ui.SentResultList;
import br.org.indt.ndg.lwuit.ui.CheckableListCellRenderer;
import com.nokia.mid.appl.cmd.Local;
import com.sun.lwuit.Command;
import java.util.Vector;

/**
 *
 * @author Alexandre Martini
 */
public class MoveToUnsentCommand extends CommandControl{
    private static MoveToUnsentCommand instance = new MoveToUnsentCommand();
//    private Vector selectedFiles = new Vector();

    private MoveToUnsentCommand(){}

    public static MoveToUnsentCommand getInstance(){
        return instance;
    }

    protected Command createCommand() {
        return new Command(Local.getText(Local.QTJ_CMD_MOVETOUNSENT));
    }

    protected void doAction(Object parameter) {
        SentResultList list = (SentResultList) parameter;
        FileSystem fs = AppMIDlet.getInstance().getFileSystem();
        Vector xmlResultFile = fs.getXmlSentFile();
        boolean[] listFlags = list.getRenderer().getSelectedFlags();
        int size = listFlags.length;
        for (int i=0; i < size; i++){
            if (listFlags[i]) {
                String xmlFileName = ((XmlResultFile) xmlResultFile.elementAt(i)).getFileName();
                if(xmlFileName.startsWith("s_") || xmlFileName.startsWith("p_")) {
                    fs.moveUnsentResult(xmlFileName);
                }
            }
        }
        list.getSentList().commandAction(Resources.CMD_MOVETOUNSENT, null);
    }

}
