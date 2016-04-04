package dwat.app;

import java.lang.String;
import java.util.*;

import android.util.Log;

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
    public void RequestMenu(String manufacturer) {
        try {
            HashMap<String, String> messageMap = new HashMap<String, String>();
            messageMap.put("data", "menu");
            messageMap.put("manf", manufacturer);
            JSONObject reqObj = new JSONObject(messageMap);
            String message = URLEncoder.encode(reqObj.toString(), "UTF-8");

            URL url = new URL("http://uxkkad247118.xlaresix.koding.io:3000/");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(message);
            writer.close();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = connection.getResponseMessage();
                JSONObject respObj = new JSONObject(response);
                Log.d("SERVCOMM", "response: " + response);
            } else {
                Log.d("SERVCOMM", "Bad response: " + connection.getResponseCode());
            }
        } catch (UnsupportedEncodingException e) {

        } catch (ProtocolException e) {

        } catch (IOException e) {

        } catch (JSONException e) {

        }
    }

    public void RequestFoodDescription(String manufacturer, String foodname) {
        try {
            HashMap<String, String> messageMap = new HashMap<String, String>();
            messageMap.put("data", "fooddesc");
            messageMap.put("foodname", foodname);
            messageMap.put("manf", manufacturer);
            JSONObject reqObj = new JSONObject(messageMap);

            String message = URLEncoder.encode(reqObj.toString(), "UTF-8");

            URL url = new URL("http://uxkkad247118.xlaresix.koding.io:3000/");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(message);
            writer.close();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = connection.getResponseMessage();
                JSONObject respObj = new JSONObject(response);
                Log.d("SERVCOMM", "response: " + response);
            } else {
                Log.d("SERVCOMM", "Bad response: " + connection.getResponseCode());
            }
        } catch (UnsupportedEncodingException e) {

        } catch (ProtocolException e) {

        } catch (IOException e) {

        } catch (JSONException e) {

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
