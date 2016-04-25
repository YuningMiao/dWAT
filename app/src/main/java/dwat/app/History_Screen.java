package dwat.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class History_Screen extends Activity {
    ListView history;
    ArrayList<String> histValues = new ArrayList<>();
    ArrayAdapter<String> histAdpt;
    String location;
    MealEntry meal;
    RelativeLayout screen;
    long time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        screen = (RelativeLayout) findViewById(R.id.histScreen);
        screen.setOnTouchListener(new OnSwipeTouchListener(History_Screen.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(History_Screen.this, Main_Screen.class);
                intent.putExtra("location", location);
                intent.putExtra("time", time);
                startActivity(intent);
            }
        });

        history = (ListView) findViewById(R.id.histList);
        meal = (MealEntry) getIntent().getSerializableExtra("meal");
        if(meal != null && meal.location != null && meal.foods.size() > 0) {
            location = meal.location;
            ServerQuery sq = new ServerQuery();
            sq.RequestFoodDescription(meal.location, meal.foods, meal.modifiers, this);

            boolean mealEntryFound = false;
            for(MealEntry m : UserPreferences.userHistory) {
                if(meal.equals(m)) {
                    //the meal entries are the same
                    if (meal.date.size() > 0) m.date.add(meal.date.get(0));
                    mealEntryFound = true;
                    break;
                }
            }
            if(!mealEntryFound) {
                UserPreferences.userHistory.add(meal);
            }
        }

        int count = 0;
        for(int i=UserPreferences.userHistory.size()-1;i>=0;i--) {
            if(histValues.size() == 0) { histValues.add(""); }
            if(UserPreferences.userHistory.get(i) != null && count < 10) {
                histValues.add(UserPreferences.userHistory.get(i).toString());
                count ++;
            } else {
                break;
            }
        }
        time = getIntent().getLongExtra("time", 0L);
        histAdpt = new ArrayAdapter<>(this, R.layout.activity_listview, R.id.textView, histValues);

        history.setAdapter(histAdpt);

        history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0 && UserPreferences.userHistory.size() >= position && UserPreferences.userHistory.size()-position > -1) {
                    MealEntry m = UserPreferences.userHistory.get(UserPreferences.userHistory.size()-position);
                    ServerQuery sq = new ServerQuery();
                    sq.RequestFoodDescription(m.location, m.foods, m.modifiers, History_Screen.this);
                }
            }
        });

        ImageButton backBttn = (ImageButton) findViewById(R.id.homeButton);
        backBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(History_Screen.this, Main_Screen.class);
                if(meal != null)
                    backIntent.putExtra("location", location);
                backIntent.putExtra("time", time);
                startActivity(backIntent);
            }
        });

        new WriteMealEntries().execute();
    }

    public void updateFoodDescValues(ServerQuery.FoodDescription fd) {
        if(fd == null) return;

        String foodDesc = "";
        for (Field field : fd.getClass().getDeclaredFields()) {
            field.setAccessible(true); // You might want to set modifier to public first.
            Object value = null;
            try {
                value = field.get(fd);
            } catch (IllegalAccessException e) {
                Log.d("SERVCOMM", "Exception: " + e.getMessage());
            }
            if (value != null) {
                switch (field.getName()) {
                    case "Type":
                    case "this$0":
                    case "HasModifiers":
                    case "Modifiers":
                    case "BadModifiers":
                        break;
                    default:
                        foodDesc += field.getName() + ": " + value + "\n";
                        break;
                }
            }
        }

        histValues.clear();
        histValues.add(foodDesc);
        int count = 0;
        for(int i=UserPreferences.userHistory.size()-1;i>=0;i--) {
            if(UserPreferences.userHistory.get(i) != null && count < 10) {
                histValues.add(UserPreferences.userHistory.get(i).toString());
                count ++;
            } else {
                break;
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    histAdpt.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.d("SERVCOMM", "Exception: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    public class WriteMealEntries extends AsyncTask<Object, String, Object> {
        protected Object doInBackground(Object... o) {
            ObjectOutputStream out = null;
            int newCount = 0;
            try {
                File f = new File(getExternalFilesDir(null), "userhist.dat");
                f.createNewFile();
                out = new ObjectOutputStream(new FileOutputStream(f));
                Log.d("UPREF", f.getAbsolutePath());
                for(int i=0;i<UserPreferences.userHistory.size();i++) {
                    out.writeObject(UserPreferences.userHistory.get(i));
                    out.flush();
                    newCount++;
                }
            } catch(IOException e) {
                Log.d("UPREF", e.toString());
            } finally {
                try {
                    out.close();
                } catch (IOException e) { Log.d("UPREF", "Failed to close FileWriter"); }
            }
            Log.d("UPREF", "MealEntry serialized " + newCount + " objects");
            return -1L;
        }
    }

}
