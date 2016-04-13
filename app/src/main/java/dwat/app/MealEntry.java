package dwat.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MealEntry {
    ArrayList<String> foods;
    ArrayList<String> modifiers; //size modifiers
    String location;
    Date date;
    int count;
    double value;

    MealEntry(String location, Date date) {
        this.location = location;
        this.date = date;
    }

    MealEntry() {
        this.foods = new ArrayList<>();
        this.modifiers = new ArrayList<>();
    }

    public void add(String food, String modifier) {
        foods.add(food);
        modifiers.add(modifier);
    }

    public String getFoodName() {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<foods.size();i++) {
            sb.append(getFoodName(i));
        }
        return sb.toString();
    }

    public String getFoodName(int i) {
        return modifiers.get(i) + " " + foods.get(i);
    }

    public void Serialize(FileWriter fw) {
        try {
            fw.write(foods.size());
            for(String s : foods) {
                fw.write(s);
                fw.write('\r');
            }
            fw.write(modifiers.size());
            for(String s : modifiers) {
                fw.write(s);
                fw.write('\r');
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
        } catch (IOException e) {

        }
    }

    public static MealEntry Deserialize(FileReader fr) {
        MealEntry me = new MealEntry();
        try {
            int b;
            String tmp = "";
            int len = fr.read();
            while ((b = fr.read()) != -1) {
                tmp += (char)b;
                if(b == '\r') {
                    me.foods.add(tmp);
                    tmp = "";
                    if(me.foods.size() >= len) break;
                }
            }
            len = fr.read();
            while ((b = fr.read()) != -1) {
                tmp += (char)b;
                if(b == '\r') {
                    me.modifiers.add(tmp);
                    tmp = "";
                    if(me.foods.size() >= len) break;
                }
            }
            me.count = 0;
            for(int i=0;i<4;i++) {
                me.count |= (fr.read() << (i*8));
            }
            while ((b = fr.read()) != -1) {
                me.location += (char)b;
                if(b == '\r') {
                    break;
                }
            }
            long l = 0L;
            for(int i=0;i<8;i++) {
                l |= (fr.read() << (i*8));
            }
            me.date = new Date(l);
            return me;
        } catch (IOException e) {

        } finally {
            return null;
        }
    }
}
