package dwat.app;

import android.location.Location;

import java.security.Timestamp;
import java.text.SimpleDateFormat;

public class History_Screen {
    Location latitude;
    Location longitude;
    SimpleDateFormat date;
    public History_Screen(){
        date = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
    }
}
