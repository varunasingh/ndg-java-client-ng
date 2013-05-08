package br.org.indt.ndg.mobile.settings;


import br.org.indt.ndg.mobile.Resources;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
//import javax.microedition.location.LocationException;

//new libraries imported for cellid capture logic. Note: CellID logic in Nokia phones uses Data (small text).
// A new dummy LocationUtil (which is part of Nokia phones) was created for project to run in Netbeans.
import com.nokia.mid.location.LocationUtil;
//import javax.microedition.lcdui.Command;
//import javax.microedition.lcdui.CommandListener;
//import javax.microedition.lcdui.Display;
//import javax.microedition.lcdui.Displayable;
//import javax.microedition.midlet.MIDlet;
//import javax.microedition.midlet.MIDletStateChangeException;
//import javax.microedition.lcdui.Form;
//import javax.microedition.location.Coordinates;
import javax.microedition.location.QualifiedCoordinates; // To check if coordinates are actually qualified.
//end of adding new libraries change.

public class LocationHandler implements LocationListener {

    private Criteria criteria = null;
    private LocationHolder lastLocation = new LocationHolder();
    private LocationProvider provider = null;
    volatile private boolean locationPresent = false;
    volatile private boolean updateProvider = false;;
    private int currentState = 0;
    private Thread tUpdate = null;
    
    //Adding a new thread which will keep running. Purpose to keep obtaining cell tower coordinates.
    Thread locationWatch;
    //end of thread initialisation. 

    public LocationHandler() {
    }

    public int connect() {
        lastLocation.set(null);
        locationPresent = false;
        currentState = 0;
        
        //New addition: To initiate the locationWatch thread.
        //Thread calls LocationWatch() and sets priority to Minimum (so that it can run in the background).
        provider = null; // Reset to null.
        
        if(locationWatch == null) { //locationWatch initialised in the main class.
            locationWatch = new Thread(new LocationWatch());    // New thread that starts new class LocationWatch().
            locationWatch.setPriority(Thread.MIN_PRIORITY);
            locationWatch.start();  // Will call class LocationWatch.run().
        }
        return currentState;
    }

    public void disconnect() {
        if (provider != null) {
            try {
                provider.setLocationListener(null, 0, 0, 0);
                provider.reset();
            } catch (SecurityException e) {
            }
        }
    }
    
    public void locationUpdated(LocationProvider lp, Location lctn) {
        if(currentState == LocationProvider.AVAILABLE &&  // result may be mistaken otherwise
                lctn != null &&
                lctn.getQualifiedCoordinates() != null) {
            lastLocation.set(lctn);
            locationPresent = true;
        } else {
            locationPresent = false;
        }
    }

    public void providerStateChanged(LocationProvider lp, int state) {
        currentState = state;
        locationPresent = false;
    }

    public boolean locationObtained() {
        return locationPresent;
    }
    
    public void updateServiceOn() {
        if (tUpdate == null || !tUpdate.isAlive()) {
            updateProvider = true;
            tUpdate = new Thread(new UpdateProvider());
            tUpdate.setPriority(Thread.MIN_PRIORITY);
            locationPresent = false;
            tUpdate.start();
        }
    }

    // Adding a new method to start a thread for the CellId method of location grab.
    // This will be initiated when user pressed Update Location button in the Survey.
    public void updateLocation() {
        
        tUpdate.run(); 
        
    }
    // End of change.
    
    public void updateServiceOff() {
        updateProvider = false;
//        if (tUpdate != null && tUpdate.isAlive()) {
//            tUpdate.interrupt(); //unevitable in order to take  proper care of bluetooth gps
//        }
    }
    
    public String getLocationString() {
        Location curLoc = getLocation();
        String locationString = "";

        if ( !locationPresent ) {
            locationString += Resources.CONNECTING + "\n\n\n\n\n\n";
        } else {
            locationString += Resources.CONNECTED + " \n";
                locationString += Resources.LATITUDE + curLoc.getQualifiedCoordinates().getLatitude() + "\n";
                locationString += Resources.LONGITUDE + curLoc.getQualifiedCoordinates().getLongitude() + "\n";
                locationString += Resources.ALTITUDE + curLoc.getQualifiedCoordinates().getAltitude() + "\n";
                locationString += Resources.HORIZONTAL_ACCU + curLoc.getQualifiedCoordinates().getHorizontalAccuracy() + "\n";
                locationString += Resources.VERTICAL_ACCU + curLoc.getQualifiedCoordinates().getVerticalAccuracy() + "\n";
        }
        return locationString;
    }

    public Location getLocation() {
        return lastLocation.get();
    }

    public static class LocationHolder {
        private Location loc;
        public synchronized Location get() { return loc; }
        public synchronized void set(Location l) { loc = l; }
    }

     /**
     * For Nokia J2ME feature phones that use network location they do not seem
     * to fire locationUpdated events.  This class will constantly request the 
     * location and fire an event when the location coordinates change
     */
    class LocationWatch implements Runnable {
        
        public boolean running = true;
        
        QualifiedCoordinates lastCoord;
        
        public void run () {
            while(running) {
                try {
                    int[] methods = {(Location.MTA_ASSISTED | Location.MTE_CELLID | Location.MTE_SHORTRANGE | Location.MTY_NETWORKBASED)};
                    // Retrieve the location provider
                    provider = LocationUtil.getLocationProvider(methods, null);
                    Location loc =provider.getLocation(50000);
                    QualifiedCoordinates coord = loc.getQualifiedCoordinates();
                    locationPresent = true;
                    currentState = LocationProvider.AVAILABLE;
                    
                    if(!coord.equals(lastCoord)) {
                        locationUpdated(provider, loc);
                    }
                    
                    // Change this value for the interval to the desired location update to happen.
                    // in ms. 60000=60 seconds=1 minute.
                    Thread.sleep(60000);
                }catch(Exception e) {}
            } // In our and this case, this loop runs indefinitly with sleep cycles.
            
        }
    }
    
// End of change.
    
// in case that of connecting bluetooth gps
        class UpdateProvider implements Runnable {

            public void run() {
                try {
// if location not obtained in
                    Thread.sleep(15000);
                    while (updateProvider && !locationPresent ) {

                        disconnect();
                        connect();
                        Thread.sleep(60000);
                    }
                } catch (InterruptedException ex) {
                    // we do nothing
                } finally {
                    tUpdate = null;
                }

            }

    }
}

