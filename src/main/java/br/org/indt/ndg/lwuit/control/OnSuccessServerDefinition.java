package br.org.indt.ndg.lwuit.control;

import br.org.indt.ndg.mobile.AppMIDlet;

public class OnSuccessServerDefinition extends Event {


    protected void doAction(Object param) {
        AppMIDlet.getInstance().showEncryptionScreen();
    }
}
