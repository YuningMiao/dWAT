package dwat.app;

import java.util.ArrayList;
import java.util.List;

public class DwatUtil {
    public static String[] toArray(List<String> list) {
        String[] arr = list.toArray(new String[list.size()]);
        return arr;
    }
    public static ServerQuery.FoodDescription[] toArray2(List<ServerQuery.FoodDescription> list) {
        ServerQuery.FoodDescription[] arr = list.toArray(new ServerQuery.FoodDescription[list.size()]);
        return arr;
    }
    public static MealEntry[] toArray3(List<MealEntry> list) {
        MealEntry[] arr = list.toArray(new MealEntry[list.size()]);
        return arr;
    }

    public static <T> ArrayList<T> toList(T[] arr) {
        ArrayList<T> list = new ArrayList<T>(arr.length);
        for(int i=0;i<arr.length;i++) {
            list.add(arr[i]);
        }
        return list;
    }
}
