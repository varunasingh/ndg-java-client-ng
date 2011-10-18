package br.org.indt.ndg.lwuit.ui.openrosa.model;

import com.nokia.xfolite.xforms.dom.BoundElement;

/**
 *
 * @author damian.janicki
 */
public class OpenRosaQuestion {

    public static final int TYPE_INPUT = 0;
    public static final int TYPE_SELECT = 1;
    public static final int TYPE_SELECT1 = 2;
    public static final int TYPE_UPLOAD_IMAGE = 3;

    private int type;
    private BoundElement element;

    public OpenRosaQuestion( BoundElement element, int type ){
        this.element = element;
    }

    public BoundElement getBoundElement(){
        return element;
    }

    public int getType(){
        return type;
    }
}
