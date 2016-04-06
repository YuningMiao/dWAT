package dwat.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.MalformedURLException;
import java.util.*;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import dwat.app.Suggestion_Screen;

public class UserPreferences {
    public class FoodDescription {
        public String FoodManf;
        public String FoodName;
        public int ServingSize;
        public int Calories;
        public int CaloriesFromFat;
        public int TotalFat;
        public int SaturatedFat;
        public int TransFat;
        public int Cholesterol;
        public int Sodium;
        public int Carbohydrates;
        public int DietaryFiber;
        public int Sugars;
        public int Protein;

        public boolean isValidFoodDescription(JSONObject obj) {
            return obj.has("FoodManf") && obj.has("FoodName") && obj.has("ServingSize") &&
                    obj.has("Calories") && obj.has("CaloriesFromFat") && obj.has("TotalFat") &&
                    obj.has("SaturatedFat") && obj.has("TransFat") && obj.has("Cholesterol") &&
                    obj.has("Sodium") && obj.has("Carbohydrates") && obj.has("DietaryFiber") &&
                    obj.has("Sugars") && obj.has("Protein");
        }
    }
    private class SendServerMenuQuery extends AsyncTask<Object, String, Object> {
        protected Object doInBackground(Object... o) {
            int count = o.length;
            if(!(o[0] instanceof String)) return -1L;
            if(!(o[1] instanceof Suggestion_Screen)) return -1L;

            URL url;
            try {
                url = new URL(host);
            } catch(MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
            String message = (String)o[0];
            Suggestion_Screen ss = (Suggestion_Screen)o[1];

            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(message);
                writer.close();
                Log.d("SERVCOMM", "Sent message: " + message);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer result = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                        if(isCancelled()) return null;
                    }
                    String response = result.toString();
                    Log.d("SERVCOMM", "response: " + response);
                    JSONArray respArr = new JSONArray(response);
                    Log.d("SERVCOMM", "Menu response: " + respArr.getString(0));
                    String[] menu = new String[respArr.length()];
                    for(int i=0;i<respArr.length();i++) {
                        menu[i] = respArr.getString(i);
                    }
                    ss.updateLocValues(menu);
                    return menu;
                } else {
                    Log.d("SERVCOMM", "Bad response: " + connection.getResponseCode());
                }
            } catch (IOException | JSONException e) {
                Log.d("SERVCOMM", "Exception: " + e.getMessage());
            } finally {
                return null;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            Log.d("SERVCOMM", "Progress: " + progress[0]);
        }

        protected void onPostExecute(Long result) {
            Log.d("SERVCOMM", "Finished with " + result + " bytes downloaded");
        }
    }
    private class SendServerFoodDescRequest extends AsyncTask<Object, Object, Object> {
        protected Object doInBackground(Object... o) {
            int count = o.length;
            if(!(o[0] instanceof String)) return -1L;
            if(!(o[1] instanceof History_Screen)) return -1L;

            URL url;
            try {
                url = new URL(host);
            } catch(MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
            String message = (String)o[0];
            History_Screen hs = (History_Screen)o[1];

            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(message);
                writer.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer result = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                        if(isCancelled()) return null;
                    }
                    String response = result.toString();
                    Log.d("SERVCOMM", "response: " + response);
                    JSONObject respObj = new JSONObject(response);
                    FoodDescription fd = new FoodDescription();
                    if(fd.isValidFoodDescription(respObj)) {
                        fd.FoodManf = respObj.getString("FoodManf");
                        fd.FoodName = respObj.getString("FoodName");
                        fd.Calories = respObj.getInt("Calories");
                        fd.CaloriesFromFat = respObj.getInt("CaloriesFromFat");
                        fd.Carbohydrates = respObj.getInt("Carbohydrates");
                        fd.Cholesterol = respObj.getInt("Cholesterol");
                        fd.DietaryFiber = respObj.getInt("DietaryFiber");
                        fd.SaturatedFat = respObj.getInt("SaturatedFat");
                        fd.Sodium = respObj.getInt("Sodium");
                        fd.Sugars = respObj.getInt("Sugars");
                        fd.Protein = respObj.getInt("Protein");
                        fd.TotalFat = respObj.getInt("TotalFat");
                        fd.TransFat = respObj.getInt("TransFat");
                        fd.ServingSize = respObj.getInt("ServingSize");
                        hs.updateFoodDescValues(fd);
                        return fd;
                    }
                } else {
                    Log.d("SERVCOMM", "Bad response: " + connection.getResponseCode());
                }
            } catch (IOException | JSONException e) {
                Log.d("SERVCOMM", e.getMessage());
            } finally {
                return null;
            }
        }
    }

    private final String host = "http://dwat.us-2.evennode.com/";

    public void RequestMenu(String manufacturer, Suggestion_Screen ss) {
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put("data", "menu");
            reqObj.put("manf", manufacturer);

            new SendServerMenuQuery().execute((Object)reqObj.toString(), (Object)ss);
        } catch (JSONException e) {
            Log.d("SERVCOMM", "JSONException caught... manufacturer String broke something in JSONObject.put()");
        }
    }

    public void RequestFoodDescription(String manufacturer, String foodname, History_Screen hs) {
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put("data", "fooddesc");
            reqObj.put("foodname", foodname);
            reqObj.put("manf", manufacturer);

            new SendServerFoodDescRequest().execute((Object)reqObj.toString(), (Object)hs);
        } catch (JSONException e) {
            Log.d("SERVCOMM", "JSONException caught... foodname || manufacturer String broke something in JSONObject.put()");
        }
    }

    private static double valueFunction(MealEntry meal, String foodName, String location, Date date, int maxCount){
        double value = 0;
        if (foodName.length() > 0){
            if(meal.food.equalsIgnoreCase(foodName)){
                value +=1;
            }
            else if(meal.food.toLowerCase().contains(foodName.toLowerCase())){
                value += 0.7;
            }
            else if(foodName.toLowerCase().contains(meal.food.toLowerCase())){
                value += 0.7;
            }
        }
        if (location.length() > 0){
            if(location.equalsIgnoreCase(meal.location)){
                value +=1;
            }
        }
        if(String.format("%tA",meal.date).equals(String.format("%tA",date))){
            value += 0.3;
        }
        int currentHour = Integer.parseInt(String.format("%tH",date));
        int mealHour = Integer.parseInt(String.format("%tH",meal.date));
        if(Math.abs(currentHour-mealHour) < 4){
            value += (4 - Math.abs(currentHour-mealHour))*0.1;
        }
        if(maxCount > 5)
            maxCount = maxCount/2;
        value += (double)meal.count/(double)maxCount;
        System.out.println(meal.food + " " + (meal.count/maxCount));
        return value;
    }

    public static String userPreference(MealEntry []userHistory, String foodName, String location, Date date){
        int maxCount = 4;
        for(MealEntry meal : userHistory){
            if(meal.count > maxCount){
                maxCount = meal.count;
            }
        }
        System.out.println("MaxCount: " + maxCount);
        double[] values = new double[userHistory.length];
        double maxVal = -1;
        int maxInd = -1;
        for(int i = 0; i < userHistory.length; i++){
            values[i] = valueFunction(userHistory[i],foodName, location, date, maxCount);
            if(values[i] > maxVal){
                maxVal = values[i];
                maxInd = i;
            }
        }
        for (double val: values)
            System.out.print(val + " ");
        return userHistory[maxInd].food;
    }


}
