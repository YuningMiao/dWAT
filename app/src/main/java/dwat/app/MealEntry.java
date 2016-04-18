package dwat.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class MealEntry implements Serializable {
    ArrayList<String> foods = new ArrayList<>();
    ArrayList<String[]> modifiers = new ArrayList<>(); //size modifiers
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
            String[] mods = new String[fd.Modifiers.length];
            for(int i=0;i<fd.Modifiers.length;i++) {
                mods[i] = fd.Modifiers[i];
                for(String badmod : fd.BadModifiers) {
                    mods[i] = mods[i].replace(badmod, "");
                }
            }
            modifiers.add(mods);
        } else {
            modifiers.add(new String[0]);
        }

    }

    MealEntry() {
    }

    public void addModifiers(String[] modifiers, String[] badmodifiers) {
        String[] newmods = new String[modifiers.length];
        for(int i=0;i<modifiers.length;i++) {
            newmods[i] = modifiers[i];
            for (String bad : badmodifiers) {
                newmods[i] = newmods[i].replace(bad, "");
            }
        }
        this.modifiers.add(newmods);
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
        return sb.toString();
    }
}
