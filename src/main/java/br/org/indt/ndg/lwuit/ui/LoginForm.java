package br.org.indt.ndg.lwuit.ui;

import br.org.indt.ndg.lwuit.control.ExitCommand;
import br.org.indt.ndg.lwuit.control.LoginCommand;
import br.org.indt.ndg.lwuit.extended.DescriptiveField;
import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.httptransport.SecureHttpConnector;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.Border;

/**
 *
 * @author damian.janicki
 */
public class LoginForm extends Screen implements ActionListener{
    private DescriptiveField textAreaUser = null;
    private DescriptiveField textAreaPassword = null;


    protected void loadData() {// TODO return to last form
    }

    protected void customize() {
        setTitle(Resources.NEWUI_NOKIA_DATA_GATHERING, Resources.USER_AUTHENTICATION);
        form.removeAll();
        form.removeAllCommands();

        form.addCommand(ExitCommand.getInstance().getCommand());
        form.addCommand(LoginCommand.getInstance().getCommand());

        try{
            form.removeCommandListener(this);
        } catch (NullPointerException npe ) {
            //during first initialisation remove throws exception.
            //this ensure that we have registered listener once
        }
        form.addCommandListener(this);

        form.getStyle().setBorder(Border.createBevelLowered( NDGStyleToolbox.getInstance().focusLostColor,
                                                NDGStyleToolbox.getInstance().focusLostColor,
                                                NDGStyleToolbox.getInstance().focusLostColor,
                                                NDGStyleToolbox.getInstance().focusLostColor ));


        addUserArea();
        addPasswordArea();
    }

    private void addUserArea(){
        TextArea labelUser = UIUtils.createQuestionName( Resources.USERNAME );
        form.addComponent(labelUser);

        textAreaUser = new DescriptiveField(50);
        textAreaUser.setInputMode("abc");

        if(SecureHttpConnector.getCurrentUser() != null){
            textAreaUser.setText(SecureHttpConnector.getCurrentUser());
        }

        form.addComponent(textAreaUser);
    }

    private void addPasswordArea(){
        TextArea labelPassword = UIUtils.createQuestionName( Resources.PASSWORD );
        form.addComponent(labelPassword);

        textAreaPassword = new DescriptiveField(50);
        textAreaPassword.setConstraint(TextField.PASSWORD);
        textAreaPassword.setInputMode("abc");

        if(SecureHttpConnector.getCurrentPassword() != null){
            textAreaPassword.setText(SecureHttpConnector.getCurrentPassword());
        }

        form.addComponent(textAreaPassword);
    }

    public void actionPerformed(ActionEvent ae) {
        Object obj = ae.getSource();
        if(obj == LoginCommand.getInstance().getCommand()){
            String[] strTab = new String[2];
            strTab[0] = textAreaUser.getText();
            strTab[1] = textAreaPassword.getText();
            LoginCommand.getInstance().execute(strTab);
        }else if (obj == ExitCommand.getInstance().getCommand()) {
            ExitCommand.getInstance().execute(null);
        }
    }

}
