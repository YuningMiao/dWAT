package dwat.app;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
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

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class History_Screen extends Activity {
//    Location latitude;
//    Location longitude;
//    SimpleDateFormat date;
//    public History_Screen(){
//        date = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
//    }
    ListView history;
    ArrayList<String> histValues = new ArrayList<String>(Arrays.asList("Meal 1", "Meal 2", "Meal 3", "Meal 4", "Meal 5"));
    ArrayAdapter<String> histAdpt;
    History hist;
    RelativeLayout screen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        UserPreferences up = new UserPreferences();
        up.RequestFoodDescription("McDonald's", "Buffalo Ranch McChicken", this);

        screen = (RelativeLayout) findViewById(R.id.histScreen);
        screen.setOnTouchListener(new OnSwipeTouchListener(History_Screen.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(History_Screen.this, Suggestion_Screen.class);
                startActivity(intent);
            }
        });

        history = (ListView) findViewById(R.id.histList);
        hist = (History) getIntent().getSerializableExtra("meal");
        if(hist != null && !hist.getHist().isEmpty() && hist.getHist().length() > 0)
            histValues.add(hist.getHist());

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

    public void updateFoodDescValues(UserPreferences.FoodDescription fd) {
        histValues.set(0, "FoodName: " + fd.FoodName);
        histValues.set(1, "FoodManf: " + fd.FoodManf);
        histValues.set(2, "ServingSize: " + fd.ServingSize);
        histValues.set(3, "Calories: " + fd.Calories);
        histValues.set(4, "TotalFat: " + fd.TotalFat);
        Log.d("SERVCOMM", "histValues about to be updated");
        histAdpt.notifyDataSetChanged();
        Log.d("SERVCOMM", "histValues updated");
    }

    @Override
    public void onBackPressed() {
    }

}
