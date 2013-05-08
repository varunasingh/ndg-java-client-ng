package br.org.indt.ndg.lwuit.ui;

import br.org.indt.ndg.lwuit.control.SurveysControl;
import br.org.indt.ndg.lwuit.extended.DescriptiveField;
import br.org.indt.ndg.lwuit.ui.renderers.TitlePainterDialog;
import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Display;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.impl.midp.VirtualKeyboard;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.Style;

/**
 *
 * @author mturiel
 */
public class DetailsForm extends Screen implements ActionListener {

    private static DetailsForm df;
    private Dialog dialog;
    private TitlePainterDialog tp = new TitlePainterDialog();

    private static String title;
    private static String label;
    private static String otrText;
    private Command cmdOk;

    private DescriptiveField tfDesc;

    protected void loadData() {
        title = Resources.MORE_DETAILS;
        cmdOk = new Command(br.org.indt.ndg.mobile.Resources.OK);
    }

    protected void customize() {
        dialog = new Dialog();
        dialog.setDialogStyle(NDGStyleToolbox.getInstance().menuStyle.getBaseStyle());
        dialog.getTitleComponent().setPreferredH( NDGStyleToolbox.getInstance().dialogTitleStyle.unselectedFont.getHeight()
                                                + dialog.getTitleStyle().getPadding( Component.TOP )
                                                + dialog.getTitleStyle().getPadding( Component.BOTTOM )
                                                + dialog.getTitleStyle().getMargin( Component.TOP )
                                                + dialog.getTitleStyle().getMargin( Component.BOTTOM ) );
        dialog.setMenuCellRenderer(new MenuCellRenderer());
        dialog.getTitleStyle().setBgPainter(tp);
        dialog.setTitle(" ");
        tp.setTitle(title);
        dialog.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        Container c = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        c.setIsScrollVisible(false);

        c.addComponent( UIUtils.createTextArea( label, NDGStyleToolbox.getInstance().menuStyle.unselectedFont ) );
        tfDesc = new DescriptiveField(50);
        tfDesc.setText(otrText);
        tfDesc.setInputMode("Abc");
        tfDesc.setEditable(true);
        c.addComponent(tfDesc);
        dialog.addComponent(c);
        dialog.addCommand(cmdOk);
        dialog.addCommandListener(this);

        if(Display.getInstance().isTouchScreenDevice()) {
            VirtualKeyboard vkExtendedOk= new VirtualKeyboard() {
                //trigger cmdOK when 'OK' button is pressed on virtual keyboard
                protected void actionCommand(Command cmd) {
                    super.actionCommand(cmd);
                    if (cmd.getId() == OK) {
                        okPressed();
                    }
                }
            };

           //TODO add 'Clear' button to virtual keyboard if possible
            VirtualKeyboard.bindVirtualKeyboard(tfDesc, vkExtendedOk);
        }

        Style style = dialog.getSoftButtonStyle();
        style.setFont( NDGStyleToolbox.getInstance().menuStyle.unselectedFont );
        dialog.setSoftButtonStyle( style );
    }

    public void actionPerformed(ActionEvent evt) {
        Object cmd = evt.getSource();

        if (cmd == cmdOk) {
            okPressed();
        }
    }

    public static void show(String _label, String _otrText) {
        label = _label;
        otrText = _otrText;
        df = new DetailsForm();
        df.loadData();
        df.customize();
        df.showDialog();
    }

    public static void dispose() {
        df.dialog.dispose();
    }

    private void showDialog() {
        Container cont1 = dialog.getContentPane();
        int hi = 0;
        int wi = cont1.getPreferredW();

        for( int i = 0 ; i< dialog.getComponentCount() ; i ++ ){
            hi += dialog.getComponentAt(i).getPreferredH();
        }

        int disH = Display.getInstance().getDisplayHeight();
        int disW = Display.getInstance().getDisplayWidth();

        int H_Margin = hi < disH ? (disH - hi)/2 : 0;
        int V_Margin =  wi < disW ? (disW - wi)/2 : 0;
        dialog.show( H_Margin, H_Margin, V_Margin, V_Margin, true);
    }

    private void okPressed() {
            SurveysControl.getInstance().setItemOtherText(tfDesc.getText());
            otrText = tfDesc.getText();
            dialog.dispose();
    }
}
