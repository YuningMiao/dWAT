package dwat.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
                Intent intent = new Intent(History_Screen.this, Suggestion_Screen.class);
                intent.putExtra("location", hist.getLocName());
                intent.putExtra("time", time);
                startActivity(intent);
            }
        });

        history = (ListView) findViewById(R.id.histList);
        meal = (MealEntry) getIntent().getSerializableExtra("meal");
        if(meal != null && meal.location != null && meal.foods.size() > 0) {
            ServerQuery sq = new ServerQuery();
            sq.RequestFoodDescription(meal.location, meal.foods.get(0), this);
        }

        int count = 0;
        for(int i=UserPreferences.userHistory.size()-1;i>=0;i--) {
            if(UserPreferences.userHistory.get(i) != null && count < 10) {
                histValues.add(UserPreferences.userHistory.get(i).toString());
                count ++;
            } else {
                break;
            }
        }
        time = getIntent().getLongExtra("time", 0L);
        histAdpt = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, histValues);

        history.setAdapter(histAdpt);

        ImageButton backBttn = (ImageButton) findViewById(R.id.homeButton);
        backBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(History_Screen.this, Suggestion_Screen.class);
                backIntent.putExtra("location", hist.getLocName());
                backIntent.putExtra("time", time);
                startActivity(backIntent);
            }
        });

        new WriteMealEntries().execute(meal);
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
            int count = o.length;
            if(count > 0 && o[0] != null && o[0] instanceof MealEntry) {
                ObjectOutputStream out = null;
                MealEntry meal = (MealEntry) o[0];
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
                    if(meal.foods.size() > 0) {
                        out.writeObject(meal);
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
            }
            return null;
        }
    }

}
