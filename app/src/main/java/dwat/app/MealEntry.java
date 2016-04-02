import java.util.*;

public class MealEntry{
    String foodItem;
    String location;
    Date date;
    int count;
    
    MealEntry(String food, String loc, Date dateInput, int cnt){
        foodItem = food;
        location = loc;
        date = dateInput;
        count = cnt;
    }
}
