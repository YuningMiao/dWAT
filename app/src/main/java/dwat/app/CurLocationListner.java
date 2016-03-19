package dwat.app;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class CurLocationListner implements LocationListener{
    @Override
    public void onLocationChanged(Location location) {
        location.getLatitude();
        location.getLongitude();
        String myLoc = "Lat=" + location.getLatitude() + " Long=" + location.getLongitude();

        Log.e("MY CURRENT LOCATION", myLoc);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
