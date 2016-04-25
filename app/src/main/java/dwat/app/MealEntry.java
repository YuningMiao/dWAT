package dwat.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class MealEntry implements Serializable {
    ArrayList<String> foods = new ArrayList<>();
    ArrayList<String> modifiers = new ArrayList<>(); //size modifiers
    String location = "";
    ArrayList<Date> date = new ArrayList<>();
    double value = 0.0;

    MealEntry() {}

    public void add(String foodname, String modifier) {
        foods.add(foodname);
        modifiers.add(modifier);
    }

    public void remove(String foodname) {
        int index = foods.indexOf(foodname);
        if(index != -1) {
            foods.remove(index);
            modifiers.remove(index);
        }
    }

    public boolean equals(MealEntry m) {
        if(foods.size() != m.foods.size()) return false;

        boolean same = true;
        for(int i=0;i<foods.size();i++) {
            if(!foods.contains(m.foods.get(i))) {
                same = false;
                break;
            }
        }
        if(!same) return false;

        if(modifiers.size() != m.modifiers.size()) return false;
        for(int i=0;i<modifiers.size();i++) {
            if(!modifiers.contains(m.modifiers.get(i))) {
                same = false;
                break;
            }
        }
        if(!same) return false;
        else return true;
    }

    public int count() {
        return date.size();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if(location != null) {
            sb.append(location);
            sb.append(" - ");
        }
        for(int i=0;i<foods.size();i++) {
            sb.append(foods.get(i));
            if(i < modifiers.size() && modifiers.get(i) != null && !modifiers.get(i).equals("")) {
                sb.append(" (");
                sb.append(modifiers.get(i));
                sb.append(")");
            }
            if(i+1 < foods.size()) {
                sb.append(" & ");
            }
        }
        sb.append(" (x");
        sb.append(count());
        sb.append(")");
        /*sb.append(" --[");
        sb.append(value);
        sb.append("]");*/
        return sb.toString();
    }
}
