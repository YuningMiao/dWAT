package dwat.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MealEntry {
    String food;
    String location;
    Date date;
    int count;
    
    MealEntry(String food, String location, Date date, int count){
        this.food = food;
        this.location = location;
        this.date = date;
        this.count = count;
    }

    public void Serialize(FileOutputStream fos) {
        try {
            for (char c : food.toCharArray()) {
                fos.write(c);
            }
            fos.write('\r');
            for(char c : location.toCharArray()) {
                fos.write(c);
            }
            fos.write('\r');
            long dateL = date.getTime();
            fos.write((int)dateL);
            fos.write((int)(dateL >> 8));
            fos.write(count);
        } catch (IOException e) {

        }
    }

    public static MealEntry Deserialize(FileInputStream fis) {
        try {
            int b;
            String food = "", location = "";
            while ((b = fis.read()) != -1) {
                food += (char)b;
                if(b == '\r') break;
            }
            while ((b = fis.read()) != -1) {
                location += (char)b;
                if(b == '\r') break;
            }
            long dateF = fis.read();
            long dateL = fis.read();
            Date date = new Date(dateF << 8 | dateL);
            int count = fis.read();
            return new MealEntry(food, location, date, count);
        } catch (IOException e) {

        } finally {
            return null;
        }
    }
}
