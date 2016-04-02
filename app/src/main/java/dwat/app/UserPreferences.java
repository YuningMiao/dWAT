package dwat.app;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class UserPreferences {
    public void send() {
        try {
            String message = URLEncoder.encode("my message", "UTF-8");

            URL url = new URL("http://uxkkad247118.xlaresix.koding.io:3000/");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            //connection.getHeaderFields().put()
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(message);
            writer.close();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = connection.getResponseMessage();
                Log.d("SERVCOMM", "response: " + response);
            } else {
                Log.d("SERVCOMM", "Bad response: " + connection.getResponseCode());
            }
        } catch (UnsupportedEncodingException e) {

        } catch (ProtocolException e) {

        } catch (IOException e) {

        }

    }



}
