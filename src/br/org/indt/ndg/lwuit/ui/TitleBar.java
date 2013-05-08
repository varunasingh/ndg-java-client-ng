package br.org.indt.ndg.lwuit.ui;

import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.Resources;
import br.org.indt.ndg.mobile.settings.LocationHandler;
import com.sun.lwuit.Display;
import com.sun.lwuit.Font;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.Image;
import com.sun.lwuit.Painter;
import com.sun.lwuit.geom.Rectangle;

public class TitleBar implements Painter {
    private static final String threeDots = "... ";
    private static final Image hr = Screen.getRes().getImage("bottom");
    private static final Image imageGPS = Screen.getRes().getImage("gps");
    private static final int gpsWidth = imageGPS.getWidth();
    private static final int TEXT_PADDING = 2;
    private static int logoWidth;
    private static int logoHeight;

    int textOffsetHorizontal;
    private String title1;
    private String title2;
    private int currentScreenWidth = 0;


    TitleBar(String title1, String title2) {
        this.title1 = title1;
        this.title2 = title2;
        logoWidth = Resources.logo.getWidth();
        logoHeight = Resources.logo.getHeight();
        textOffsetHorizontal = TEXT_PADDING + gpsWidth + TEXT_PADDING + logoWidth + TEXT_PADDING;
    }

    public int getPrefferedH() {
        return 2 * TEXT_PADDING + logoHeight;
    }

    public void paint(Graphics g, Rectangle rect) {
        int width = rect.getSize().getWidth();
        int height = rect.getSize().getHeight();
        currentScreenWidth = Display.getInstance().getDisplayWidth();

        g.fillLinearGradient(0xffffff, 0xe1e1e1, rect.getX(), rect.getY(), width, height - 3, false);

        g.drawImage( hr.scaled( Display.getInstance().getDisplayWidth(),
                                hr.getHeight()),
                                rect.getX(), rect.getY() + height - 2 );

        g.drawImage( Resources.logo, TEXT_PADDING + gpsWidth + TEXT_PADDING, ( height - logoHeight ) >> 1 );

        int spacingVertical =  ( height - NDGStyleToolbox.fontSmallHandler.getHeight() - NDGStyleToolbox.fontSmallHandler.getHeight() ) / 3;

        g.setFont( NDGStyleToolbox.fontSmallHandler );
        g.setColor( 0x007b7b7b );

        g.drawString( getFittingTitle( title1, currentScreenWidth - textOffsetHorizontal ),
                      textOffsetHorizontal, spacingVertical );
        g.drawString( getFittingTitle( title2, currentScreenWidth - textOffsetHorizontal ),
                      textOffsetHorizontal,
                      spacingVertical + NDGStyleToolbox.fontSmallHandler.getHeight() + spacingVertical );
        LocationHandler locationHandler = AppMIDlet.getInstance().getLocationHandler();
        if ( locationHandler != null && locationHandler.locationObtained() ) {
            g.drawImage(imageGPS, TEXT_PADDING, 22);
        }
    }

    private String getFittingTitle( String title, int availableWidth ) {

        Font font = NDGStyleToolbox.fontSmallHandler;
        if( font.stringWidth(title) < availableWidth ) {
            return title;
        }

        int index = 1;
        int wCharWidth = font.charWidth( 'W' );
        int pointsW = font.stringWidth( threeDots );
        while ( widthCheck( title, index, availableWidth - pointsW , wCharWidth, font ) ) {
            index++;
        }
        return title.substring( 0, index - 1 ) + threeDots;
    }

    private boolean widthCheck( String s, int length, int availableWidth,int wCharWidth, Font font ) {
        if ( length * wCharWidth < availableWidth ) {
            return true;
        }
        length = Math.min( s.length(), length );
        return font.substringWidth( s, 0, length ) < availableWidth;
    }

    public void setTitle1(String title1) {
        this.title1 = title1;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }
}
