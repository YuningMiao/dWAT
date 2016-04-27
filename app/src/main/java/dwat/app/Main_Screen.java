package dwat.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Main_Screen extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private CustomViewPager viewPager;
    private TabAdapter tabAdapter;
    private dwatTabLayout tabLayout;
    private static final String TODO = "";
    String curLoc;
    long timeFromOther;
    long timeNow;

    TabLayout.OnTabSelectedListener tabFunc;
    ListView list;

    ArrayList<String> locs;
    HashMap<String, ArrayList<String>> modifierMap = new HashMap<>();
    ArrayList<String> historyVals = new ArrayList<>();
    ArrayList<String> menuVals = new ArrayList<>();
    MealEntry[] historyMeals;
    ArrayAdapter<String> listAdapter;
    boolean commitOnClick = true;

    private GoogleApiClient mGoogleApiClient;

    MealEntry buildingMeal = new MealEntry();

    private boolean check5Minutes(){

        if(timeNow != 0 && timeFromOther != 0) {
            long difference = timeNow - timeFromOther;
            double minutes = (double)difference / (1000 * 60);
            if(minutes >=  4.99) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeNow = Calendar.getInstance().getTimeInMillis();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            curLoc = extras.getString("location");
            timeFromOther = extras.getLong("time");
        }

        mGoogleApiClient = new GoogleApiClient
                .Builder( this )
                .enableAutoManage( this, 0, this )
                .addApi( Places.GEO_DATA_API )
                .addApi( Places.PLACE_DETECTION_API )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .build();
        guessCurrentPlace(false);

        list = (ListView) findViewById(R.id.list);
        listAdapter = new ArrayAdapter<>(this, R.layout.activity_listview, R.id.textView, historyVals);
        list.setAdapter(listAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (commitOnClick && position < historyMeals.length) {
                    Intent intent = new Intent(Main_Screen.this, History_Screen.class);
                    int mealPos = historyMeals.length-1-position;
                    Log.d("MS", "Committing: " + historyMeals[mealPos].toString());
                    intent.putExtra("meal", historyMeals[mealPos]);
                    startActivity(intent);
                } else {
                    String item = listAdapter.getItem(position);
                    if(buildingMeal.foods.contains(item)) {
                        buildingMeal.remove(item);
                        list.getChildAt(position).setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    } else if (modifierMap.containsKey(item) && modifierMap.get(item).size() > 0) {
                        ArrayList<String> list = modifierMap.get(item);
                        makeAlert("Choose a size", item, DwatUtil.toArray(list), position);
                    } else {
                        buildingMeal.add(item, "");
                        list.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.dwatBlue));
                    }
                }
            }
        });

        RelativeLayout screen = (RelativeLayout) findViewById(R.id.root_view_main);
        screen.setOnTouchListener(new OnSwipeTouchListener(Main_Screen.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(Main_Screen.this, History_Screen.class);
                buildingMeal.location = curLoc;
                buildingMeal.date.add(new Date());
                intent.putExtra("meal", buildingMeal);
                intent.putExtra("time", timeNow);
                startActivity(intent);
            }

            public void onSwipeLeft() {
                Intent intent = new Intent(Main_Screen.this, Camera_Main.class);
                intent.putExtra("location", curLoc);
                intent.putExtra("meal", buildingMeal);
                intent.putExtra("modmap", modifierMap);
                intent.putExtra("time", timeNow);
                startActivity(intent);
            }
        });

        ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Camera_Main.class);
                intent.putExtra("meal", buildingMeal);
                intent.putExtra("modmap", modifierMap);
                intent.putExtra("location", curLoc);
                intent.putExtra("time", timeNow);
                startActivityForResult(intent, 0);
            }
        });

        ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected()) {
                    if (ContextCompat.checkSelfPermission(Main_Screen.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Main_Screen.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                1000);
                    } else {
                        timeNow = Calendar.getInstance().getTimeInMillis();
                        guessCurrentPlace(true);
                    }
                }
            }
        });

        viewPager = (CustomViewPager) findViewById(R.id.viewpager_main);
        tabLayout = (dwatTabLayout) findViewById(R.id.tab_layout);

        tabAdapter = new TabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);
        viewPager.setPagingEnabled(false);

        tabLayout.createTabs();
        tabFunc = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    listAdapter = new ArrayAdapter<>(Main_Screen.this, R.layout.activity_listview, R.id.textView, historyVals);
                    list.setAdapter(listAdapter);
                    list.setSelector(android.R.color.transparent);
                    commitOnClick = true;
                    Log.d("MS", "Tab changed to suggestion");
                } else if(tab.getPosition() == 1){
                    listAdapter = new ArrayAdapter<>(Main_Screen.this, R.layout.activity_listview, R.id.textView, menuVals);
                    list.setAdapter(listAdapter);
                    commitOnClick = false;
                    Log.d("MS", "Tab changed to menu");
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };

        tabLayout.setOnTabSelectedListener(tabFunc);

        new ReadMealEntries().execute();
    }

    protected void setMenuValues(ServerQuery.FoodDescription[] menu) {
        Log.d("SERVCOMM", "Updating Local Values From Menu");
        ServerQuery.menu.clear();
        modifierMap.clear();
        menuVals.clear();
        for(ServerQuery.FoodDescription fd : menu) {
            ServerQuery.menu.add(fd);
            if(menuVals.contains(fd.FoodName)) {
                ArrayList<String> modifiers = modifierMap.get(fd.FoodName);
                if(modifiers != null) {
                    modifiers.add(fd.Modifiers);
                }
            } else {
                menuVals.add(fd.FoodName);
                if(fd.HasModifiers) {
                    ArrayList<String> modifiers = new ArrayList<>();
                    modifiers.add(fd.Modifiers);
                    modifierMap.put(fd.FoodName, modifiers);
                }
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    listAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.d("SERVCOMM", "Exception: " + e.toString());
                }
            }
        });
    }

    protected void invokeUserPreferences() {
        ArrayList<MealEntry> locationPref = new ArrayList<MealEntry>();
        locationPref.addAll(UserPreferences.userHistory);
        for (int i = 0; i < locationPref.size(); i++) {
            if (!locationPref.get(i).location.equals(curLoc)) {
                locationPref.remove(i);
                i--;
            }
        }
        historyVals.clear();
        historyMeals = DwatUtil.toArray3(locationPref);
        historyMeals = UserPreferences.userPreference(historyMeals, curLoc, new Date());
        for (int i = historyMeals.length - 1; i >= Math.max(historyMeals.length - 10, 0); i--) {
            historyVals.add(historyMeals[i].toString());
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (historyVals.size() <= 0) {
                        tabFunc.onTabSelected(tabLayout.getTabAt(1));
                        Log.d("UPREF", "Changed tab to menu");
                    } else {
                        tabFunc.onTabSelected(tabLayout.getTabAt(0));
                        Log.d("UPREF", "Changed tab to suggestion");
                        listAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    Log.d("SERVCOMM", "Exception: " + e.toString());
                }
            }
        });

    }

    private void makeAlert(String title, final String foodname, final String[] choices, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Main_Screen.this);
        builder.setTitle(title);
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buildingMeal.add(foodname, choices[which]);
                list.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.dwatBlue));
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void guessCurrentPlace(final boolean refresh) {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace( mGoogleApiClient, null );
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                if (!likelyPlaces.getStatus().isSuccess()) {
                    Log.e("GEO", "Place query didn't complete. Error: " + likelyPlaces.getStatus().toString());
                    likelyPlaces.release();
                    return;
                }
                try {
                    locs = new ArrayList<String>();

                    PlaceLikelihood placeLikelihood;
                    String value;
                    for (int i = 0; i < 5; i++) {
                        placeLikelihood = likelyPlaces.get(i);
                        value = placeLikelihood.getPlace().getName().toString() + " ";
                        value += String.format("%.1f", placeLikelihood.getLikelihood() * 100);
                        locs.add(value);
                    }

					/*TODO REMOVE*/
                    locs.clear();
                    locs.add("McDonald's");
                    locs.add("Arby's");
                    locs.add("Chicken Express");
                    CharSequence[] cs = locs.toArray(new CharSequence[locs.size()]);

                    if(curLoc == null || refresh || check5Minutes()){
                        Log.e("TAG", "Location chosen: " + locs.get(0));
                        AlertDialog.Builder builder = new AlertDialog.Builder(Main_Screen.this);
                        builder.setTitle("Select your location");
                        builder.setItems(cs, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                curLoc = locs.get(which);

                                invokeUserPreferences();
                                ServerQuery sq = new ServerQuery();
                                sq.RequestMenu(curLoc, Main_Screen.this);

                                Toast.makeText(getApplicationContext(), locs.get(which), Toast.LENGTH_LONG).show();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.setCanceledOnTouchOutside(false);
                        alert.show();
                    } else {
                        setMenuValues(DwatUtil.toArray2(ServerQuery.menu));
                    }

                } catch (IllegalStateException e) {
                    Log.w("GEO", "Fail to get place or its coordinates");
                }

                likelyPlaces.release();
            }
        });
    }

    public class ReadMealEntries extends AsyncTask<Object, String, Object> {
        static final int maxCount = 1000;
        protected Object doInBackground(Object... o) {
            ObjectInputStream in = null;
            ArrayList<MealEntry> meals = null;
            try {
                File f = new File(getExternalFilesDir(null), "userhist.dat");
                //f.delete();
                f.createNewFile();
                Log.d("UPREF", f.getAbsolutePath());
                in = new ObjectInputStream(new FileInputStream(f));
                Object obj;
                meals = new ArrayList<>();
                while((obj=in.readObject()) != null) {
                    MealEntry m = (MealEntry)obj;
                    Log.d("UPREF", m.toString());
                    meals.add(m);
                }
            } catch(EOFException e) {
                //end of file
            } catch(IOException e) {
                Log.d("UPREF", e.toString());
            } catch(Exception e) {
                Log.d("UPREF", e.toString());
            } finally {
                try {
                    in.close();
                } catch (IOException | NullPointerException e) {}
            }
            if(meals != null) {
                UserPreferences.userHistory = meals;
            }
            Log.d("UPREF", "MealEntries deserialized: " + UserPreferences.userHistory.size() + " objects read");
            invokeUserPreferences();

            return null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("TAG", "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    guessCurrentPlace(false);
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
}
