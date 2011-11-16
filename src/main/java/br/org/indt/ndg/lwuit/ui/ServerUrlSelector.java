package br.org.indt.ndg.lwuit.ui;

import br.org.indt.ndg.lwuit.control.CheckServerUrlCommand;
import br.org.indt.ndg.lwuit.control.ExitCommand;
import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;


public class ServerUrlSelector extends Screen implements ActionListener {

    private ClearTextField serverUrlTextArea;
    private final String defaultInput = "http://";

    public ServerUrlSelector(){
    }

    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == ExitCommand.getInstance().getCommand()) {
            ExitCommand.getInstance().execute(null);
        } else if ( src == CheckServerUrlCommand.getInstance().getCommand() ) {
            String newAddress = serverUrlTextArea.getText().trim();
            WaitingScreen.show( Resources.CONNECTING );
            CheckServerUrlCommand.getInstance().execute(newAddress);
        }
    }

    private void initForm() {
        addUrlLabel();
        addUrlTextArea();
        serverUrlTextArea.setFocus(true);
    }

    private void addUrlTextArea() {
        serverUrlTextArea = new ClearTextField();
        String input = AppMIDlet.getInstance().getPropertyServerUrl() != null ?
                       AppMIDlet.getInstance().getPropertyServerUrl() : defaultInput;
        serverUrlTextArea.setText(input);
        serverUrlTextArea.addActionListener(this);
        serverUrlTextArea.setInputMode("Abc");
        form.addComponent(serverUrlTextArea);
    }

    private void addUrlLabel() {
        Label serverUrlLabel = new Label( Resources.ENTER_SERVER_ADDRESS );
        serverUrlLabel.setAlignment(Label.CENTER);
        serverUrlLabel.getStyle().setPadding(10, 10, 1, 1);
        serverUrlLabel.getSelectedStyle().setFont( NDGStyleToolbox.fontSmall );
        serverUrlLabel.getUnselectedStyle().setFont( NDGStyleToolbox.fontSmall );
        form.addComponent(serverUrlLabel);
    }

    protected void loadData() {

    }

    protected void customize() {
        setTitle( Resources.NEWUI_NOKIA_DATA_GATHERING, Resources.SERVER_WIZARD );
        form.setLayout( new BoxLayout( BoxLayout.Y_AXIS) );

        form.addCommand(ExitCommand.getInstance().getCommand());
        form.addCommand(CheckServerUrlCommand.getInstance().getCommand());

        initForm();

        form.addCommandListener(this);
    }
}
