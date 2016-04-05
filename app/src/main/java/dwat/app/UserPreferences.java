package dwat.app;

import java.lang.String;
import java.util.*;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class UserPreferences {
    public class NoSuchMenuException extends Exception { public NoSuchMenuException(String s) { super(s); }}
    public class NoSuchFoodException extends Exception {}
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

    private final String host = "dwat.us-2.evennode.com";

    public String[] RequestMenu(String manufacturer) throws NoSuchMenuException {
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put("data", "menu");
            reqObj.put("manf", manufacturer);

            String message = URLEncoder.encode(reqObj.toString(), "UTF-8");

            URL url = new URL(host);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(message);
            writer.close();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = connection.getResponseMessage();
                JSONArray respArr = new JSONArray(message);
                Log.d("SERVCOMM", "Menu response: " + respArr.getString(0));
                String[] menu = new String[respArr.length()];
                for(int i=0;i<respArr.length();i++) {
                    menu[i] = respArr.getString(i);
                }
                return menu;
            } else {
                Log.d("SERVCOMM", "Bad response: " + connection.getResponseCode());
                throw new NoSuchMenuException("Bad response " + connection.getResponseCode());
            }
        } catch (IOException | JSONException e) {
            Log.d("SERVCOMM", e.getMessage());
            throw new NoSuchMenuException(e.getMessage());
        }
    }

    public FoodDescription RequestFoodDescription(String manufacturer, String foodname) throws NoSuchFoodException {
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put("data", "fooddesc");
            reqObj.put("foodname", foodname);
            reqObj.put("manf", manufacturer);

            String message = URLEncoder.encode(reqObj.toString(), "UTF-8");

            URL url = new URL(host);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(message);
            writer.close();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = connection.getResponseMessage();
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
                    return fd;
                }
            } else {
                Log.d("SERVCOMM", "Bad response: " + connection.getResponseCode());
            }
        } catch (IOException | JSONException e) {
            Log.d("SERVCOMM", e.getMessage());
        } finally {
            throw new NoSuchFoodException();
        }
    }

    public static double valueFunction(MealEntry meal, String foodName, String location, Date date, int maxCount){
        double value = 0;
        if (foodName.length() > 0){
            if(meal.foodItem.equalsIgnoreCase(foodName)){
                value +=1;
            }
            else if(meal.foodItem.toLowerCase().contains(foodName.toLowerCase())){
                value += 0.7;
            }
            else if(foodName.toLowerCase().contains(meal.foodItem.toLowerCase())){
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
        System.out.println(meal.foodItem + " " + (meal.count/maxCount));
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
        return userHistory[maxInd].foodItem;
    }


}
