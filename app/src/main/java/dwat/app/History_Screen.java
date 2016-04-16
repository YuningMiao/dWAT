package dwat.app;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class History_Screen extends Activity {
//    Location latitude;
//    Location longitude;
//    SimpleDateFormat date;
//    public History_Screen(){
//        date = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
//    }
    ListView history;
    ArrayList<String> histValues = new ArrayList<String>(/*Arrays.asList("Meal 1", "Meal 2", "Meal 3", "Meal 4", "Meal 5")*/);
    ArrayAdapter<String> histAdpt;
    History hist;
    RelativeLayout screen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        screen = (RelativeLayout) findViewById(R.id.histScreen);
        screen.setOnTouchListener(new OnSwipeTouchListener(History_Screen.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(History_Screen.this, Suggestion_Screen.class);
                startActivity(intent);
            }
        });

        UserPreferences up = new UserPreferences();

        history = (ListView) findViewById(R.id.histList);
        hist = (History) getIntent().getSerializableExtra("meal");
        if(hist != null && !hist.getHist().isEmpty() && hist.getHist().length() > 0) {
            //histValues.add(hist.getHist());
            ServerQuery sq = new ServerQuery();
            sq.RequestFoodDescription(hist.getLocName(), hist.getMealName(), this);
        }

        histAdpt = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, histValues);

        history.setAdapter(histAdpt);

        ImageButton backBttn = (ImageButton) findViewById(R.id.homeButton);
        backBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(History_Screen.this, Suggestion_Screen.class);
                startActivity(backIntent);
            }
        });
    }

    public void updateFoodDescValues(ServerQuery.FoodDescription fd) {
        if(fd == null) return;

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
                        histValues.add(field.getName() + ": " + value);
                        break;
                }
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

        MealEntry m = new MealEntry("Current Location Here", new Date(), fd);

        new WriteMealEntries().execute((Object) (m));
    }

    @Override
    public void onBackPressed() {
    }

    public class WriteMealEntries extends AsyncTask<Object, String, Object> {
        protected Object doInBackground(Object... o) {
            int count = o.length;
            if(count > 0 && o[0] != null && o[0] instanceof MealEntry) {
                FileWriter fw = null;
                MealEntry[] mealEntries = (MealEntry[]) getIntent().getSerializableExtra("mealEntries");
                MealEntry m = (MealEntry) o[0];
                if(mealEntries == null) { Log.d("UPREF", "Meal Entries null in WriteMealEntries"); }
                int newCount = mealEntries == null ? 1 : mealEntries.length + 1;
                try {
                    File f = new File(getExternalFilesDir(null), "userhist.dat");
                    f.createNewFile();
                    Log.d("UPREF", f.getAbsolutePath());
                    fw = new FileWriter(f, false);
                    fw.write(newCount);
                    if(mealEntries != null) {
                        for(MealEntry me : mealEntries) {
                            me.Serialize(fw);
                        }
                    }
                    m.Serialize(fw);
                    fw.flush();
                    fw.close();
                } catch(IOException e) {
                    Log.d("UPREF", e.toString());
                } /*catch(Exception e) {
                    Log.d("UPREF", e.toString());
                } */finally {
                    try {
                        fw.close();
                    } catch (IOException e) {}
                }
                Log.d("UPREF", "MealEntry serialized " + newCount + " objects");
            }
            return null;
        }
    }

}
