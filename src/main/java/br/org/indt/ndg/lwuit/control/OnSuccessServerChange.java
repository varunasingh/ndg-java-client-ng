package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.lwuit.ui.SettingsForm;
import br.org.indt.ndg.mobile.AppMIDlet;

public class OnSuccessServerChange extends Event {

    protected void doAction(Object param) {
        AppMIDlet.getInstance().setDisplayable( SettingsForm.class );
    }
}
