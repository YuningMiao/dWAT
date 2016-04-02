package dwat.app;

import java.io.Serializable;

public class History implements Serializable{
    private String mealName;
    private String mealDate;
    private String locName;

    public History(String item, String date){
        mealName = item;
        mealDate = date;
        locName = null;
    }

    public History(String item, String date, String location){
        mealName = item;
        mealDate = date;
        locName = location;
    }

    public String getMealName(){ return mealName; }
    public String getMealDate(){ return mealDate; }
    public String getLocName(){ return locName; }

    public String getHist(){
        return mealName + "\n" + mealDate + "\n" + locName;
    }
}
