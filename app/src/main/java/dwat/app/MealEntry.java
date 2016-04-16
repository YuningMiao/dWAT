package dwat.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class MealEntry implements Serializable {
    ArrayList<String> foods = new ArrayList<>();
    ArrayList<String[]> modifiers = new ArrayList<>(); //size modifiers
    ArrayList<String[]> badmodifiers = new ArrayList<>(); //strings to remove from modifiers to make it presentable to the user
    String location = "";
    Date date = null;
    int count = 0;
    double value = 0.0;

    MealEntry(String location, Date date, ServerQuery.FoodDescription fd) {
        this.location = location;
        this.date = date;
        String foodname = fd.FoodName;
        if(fd.HasModifiers) {
            for (String mod : fd.Modifiers) {
                foodname = foodname.replace(mod, "");
            }
        }
        if(!foods.contains(foodname)) {
            foods.add(foodname);
        }
        if (fd.HasModifiers){
            //add its modifier to the existing item
            modifiers.add(fd.Modifiers);
            badmodifiers.add(fd.BadModifiers);
        } else {
            modifiers.add(new String[0]);
            badmodifiers.add(new String[0]);
        }

    }

    MealEntry(String location, Date date) {
        this.location = location;
        this.date = date;
    }

    MealEntry() {
    }

    public void add(String food, String[] modifiers) {
        foods.add(food);
        this.modifiers.add(modifiers);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if(location != null) {
            sb.append(location);
            sb.append(",");
        } else {
            sb.append("location=null,");
        }
        if(date != null) {
            sb.append(date.toString());
            sb.append(",");
        } else {
            sb.append("date=null,");
        }
        sb.append(foods.size());
        sb.append(",");
        sb.append(modifiers.size());
        sb.append(",");
        sb.append(badmodifiers.size());
        return sb.toString();
    }

    public void Serialize(FileWriter fw) throws IOException, NullPointerException {
        if(foods == null || modifiers == null || badmodifiers == null || date == null || location == null) {
            throw new NullPointerException(toString());
        }
        fw.write(foods.size());
        for(String s : foods) {
            fw.write(s);
            fw.write('\r');
        }
        fw.write(modifiers.size());
        for(String[] arr : modifiers) {
            fw.write(arr.length);
            for(String s : arr) {
                fw.write(s);
                fw.write('\r');
            }
        }
        fw.write(badmodifiers.size());
        for(String[] arr : badmodifiers) {
            fw.write(arr.length);
            for(String s : arr) {
                fw.write(s);
                fw.write('\r');
            }
        }
        fw.write((char)(count&0xff));
        fw.write((char)((count>>8)&0xff));
        fw.write((char)((count>>16)&0xff));
        fw.write((char)((count>>24)&0xff));
        fw.write(location);
        fw.write('\r');
        char[] c = new char[8];
        long l = date.getTime();
        for(int i=0;i<8;i++) {
            c[i] = (char)((l >> (i*8))&(0xff));
        }
        fw.write(c);
    }

    public static MealEntry Deserialize(FileReader fr) throws IOException {
        MealEntry me = new MealEntry();
        int b;
        String tmp = "";
        int len = fr.read();
        while (me.foods.size() < len) {
            b = fr.read();
            tmp += (char)b;
            if(b == '\r') {
                me.foods.add(tmp);
                tmp = "";
            }
        }
        len = fr.read();
        while (me.modifiers.size() < len) {
            int len2 = fr.read();
            int curIndex = 0;
            String[] modifier = new String[len2];
            while (curIndex < len2) {
                b = fr.read();
                tmp += (char) b;
                if (b == '\r') {
                    modifier[curIndex] = tmp;
                    tmp = "";
                    curIndex++;
                }
            }
            me.modifiers.add(modifier);
        }
        len = fr.read();
        while (me.badmodifiers.size() < len) {
            int len2 = fr.read();
            int curIndex = 0;
            String[] badmodifier = new String[len2];
            while (curIndex < len2) {
                b = fr.read();
                tmp += (char) b;
                if (b == '\r') {
                    badmodifier[curIndex] = tmp;
                    tmp = "";
                    curIndex++;
                }
            }
            me.badmodifiers.add(badmodifier);
        }
        me.count = 0;
        for(int i=0;i<4;i++) {
            me.count |= (fr.read() << (i*8));
        }
        while ((b = fr.read()) != '\r') {
            me.location += (char)b;
        }
        long l = 0L;
        for(int i=0;i<8;i++) {
            l |= (fr.read() << (i*8));
        }
        me.date = new Date(l);
        return me;
    }
}
