package br.org.indt.ndg.lwuit.ui.renderers;

import br.org.indt.ndg.lwuit.model.CheckableItem;
import br.org.indt.ndg.lwuit.model.DisplayableItem;
import br.org.indt.ndg.lwuit.ui.NDGLookAndFeel;
import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
import com.sun.lwuit.Component;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.layouts.BorderLayout;


/**
 *
 * @author mluz, kgomes
 */

public class ResultListCellRenderer extends CheckableListCellRenderer {

    public ResultListCellRenderer(){
        super();
    }

    public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
        CheckableItem disp = (CheckableItem)value;
        removeAll();

        Component comp = null;
        if(index>0){
            prepareCheckBox(disp);
            comp = m_rendererCheckbox;
        } else {
            Label label = new Label(((DisplayableItem)disp).getDisplayableName());
            label.setAlignment(CENTER);
            comp = label;
        }

        if (isSelected) {
            setFocus(true);
            comp.getStyle().setFont(NDGStyleToolbox.getInstance().listStyle.selectedFont);
            comp.getStyle().setFgColor( NDGStyleToolbox.getInstance().listStyle.selectedFontColor );
            if( index > 0 ){
                Image arrow = NDGLookAndFeel.getRightContextMenuImage( getHeight() );
                Label larrow = new Label(arrow);
                larrow.getStyle().setMargin(0,0,0,10);
                addComponent(BorderLayout.EAST,larrow);
            }
            getStyle().setBgPainter(m_focusBGPainter);
        } else {
            setFocus(false);
            comp.getStyle().setFont(NDGStyleToolbox.getInstance().listStyle.unselectedFont);
            comp.getStyle().setFgColor( NDGStyleToolbox.getInstance().listStyle.unselectedFontColor );
            getStyle().setBgPainter(m_bgPainter);
        }

        addComponent(BorderLayout.CENTER, comp);

        return this;
    }
}
