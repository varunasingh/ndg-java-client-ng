package br.org.indt.ndg.lwuit.extended;

import br.org.indt.ndg.lwuit.ui.NDGLookAndFeel;
import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
import com.sun.lwuit.Component;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.Painter;
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.geom.Rectangle;
import com.sun.lwuit.painter.BackgroundPainter;

/**
 *
 * @author mluz
 */
public class RadioButton extends com.sun.lwuit.RadioButton implements FocusListener {

    protected final Painter focusBGPainter = new FocusBGPainter();
    protected final BackgroundPainter bgPainter = new BackgroundPainter(this);
    private boolean mUseMoreDetails = false;
    private Image image = null;

    public RadioButton(String text) {
        super(text);
        addFocusListener(this);
        getSelectedStyle().setFont(NDGStyleToolbox.getInstance().listStyle.selectedFont, false);
        getUnselectedStyle().setFont(NDGStyleToolbox.getInstance().listStyle.unselectedFont, false);
    }

    public RadioButton(String text, Image image){
        this(text);
        this.image = image;
    }

    public void focusGained(Component cmp) {
        getStyle().setBgPainter(focusBGPainter);
        getStyle().setFont( NDGStyleToolbox.getInstance().listStyle.selectedFont );
    }

    public void focusLost(Component cmp) {
        getStyle().setBgPainter(bgPainter);
        getStyle().setFont( NDGStyleToolbox.getInstance().listStyle.unselectedFont);
    }

    public void useMoreDetails(boolean _val) {
        mUseMoreDetails = _val;
    }

    public boolean hasMoreDetails() {
        return mUseMoreDetails;
    }

    public void paint(Graphics g) {
        super.paint(g);
        if ( isSelected() && hasMoreDetails() || image != null) {
            Image img = null;
            if(image == null){
                img = NDGLookAndFeel.getRightContextMenuImage(getWidth());
            }else{
                img = image;
            }
            int x = getX() + getWidth() - (int)(img.getWidth()*1.5);
            int y = getY() + (getHeight() - img.getHeight())/2;
            g.drawImage(img, x, y);
        }
    }

    class FocusBGPainter implements Painter {

        public void paint(Graphics g, Rectangle rect) {
            int width = rect.getSize().getWidth();
            int height = rect.getSize().getHeight();

            int endColor = NDGStyleToolbox.getInstance().listStyle.bgSelectedEndColor;
            int startColor = NDGStyleToolbox.getInstance().listStyle.bgSelectedStartColor;
            g.fillLinearGradient(startColor, endColor, rect.getX(), rect.getY(), width, height, false);

            int borderColor = NDGStyleToolbox.getInstance().listStyle.bgUnselectedColor;
            g.setColor(borderColor);
            g.fillRect(rect.getX(), rect.getY(), 1, 1);
            g.fillRect(rect.getX()+width-1, rect.getY(), 1, 1);
            g.fillRect(rect.getX(), rect.getY()+height-1, 1, 1);
            g.fillRect(rect.getX()+width-1, rect.getY()+height-1, 1, 1);
        }
    }

}
