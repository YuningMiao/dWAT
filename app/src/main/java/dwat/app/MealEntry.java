package dwat.app;

import java.util.Date;

public class MealEntry{
    String foodItem;
    String location;
    Date date;
    int count;
    
    MealEntry(String food, String location, Date date, int count){
        this.foodItem = food;
        this.location = location;
        this.date = date;
        this.count = count;
    }
}
