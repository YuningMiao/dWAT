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
    public static MealEntry[] userHistory = new MealEntry[1000]; //constant 1000 entries max

    private static double valueFunction(MealEntry meal, /*String foodName,*/ String location, Date date, int maxCount){
        double value = 0;
        /*if (foodName != null && foodName.length() > 0){
            if(meal.food.equalsIgnoreCase(foodName)){
                value +=1;
            }
            else if(meal.food.toLowerCase().contains(foodName.toLowerCase())){
                value += 0.7;
            }
            else if(foodName.toLowerCase().contains(meal.food.toLowerCase())){
                value += 0.7;
            }
        }*/
        if (location != null && location.length() > 0){
            if(location.equalsIgnoreCase(meal.location)){
                value +=1;
            }
        }
        if(String.format("%tA",meal.date).equals(String.format("%tA",date))){
            value += 0.3;
        }
        int currentHour = Integer.parseInt(String.format("%tH",date));
        int mealHour = Integer.parseInt(String.format("%tH", meal.date));
        if(Math.abs(currentHour-mealHour) < 4){
            value += (4 - Math.abs(currentHour-mealHour))*0.1;
        }
        if(maxCount > 5)
            maxCount = maxCount/2;
        value += (double)meal.count/(double)maxCount;
        return value;
    }

    public static MealEntry[] userPreference(MealEntry []userHistory, String location, Date date){
        int maxCount = 4;
        int size = 0;
        for(int i=0;i<userHistory.length;i++) {
            MealEntry meal = userHistory[i];
            if(meal == null) {
                size = i;
                break;
            } else if (meal != null && meal.count > maxCount) {
                maxCount = meal.count;
            }
        }
        System.out.println("MaxCount: " + maxCount);
        for(int i = 0; i < size; i++){
            userHistory[i].value = valueFunction(userHistory[i], location, date, maxCount);
        }
        long start = System.currentTimeMillis();
        quickSort(userHistory, 0, size - 1);
        long end = System.currentTimeMillis();
        Log.d("UPREF", "User Pref quicksort took " + (end-start) + " msec");
        return userHistory;
    }

    private static void quickSort(MealEntry[] mes, int lowerIndex, int higherIndex) {
        int i = lowerIndex;
        int j = higherIndex;
        double pivot = mes[lowerIndex+(higherIndex-lowerIndex)/2].value;
        while (i <= j) {
            while (mes[i].value < pivot) {
                i++;
            }
            while (mes[j].value > pivot) {
                j--;
            }
            if (i <= j) {
                MealEntry tmp = mes[j];
                mes[j] = mes[i];
                mes[i] = tmp;
                i++;
                j--;
            }
        }
        if (lowerIndex < j)
            quickSort(mes, lowerIndex, j);
        if (i < higherIndex)
            quickSort(mes, i, higherIndex);
    }


}
