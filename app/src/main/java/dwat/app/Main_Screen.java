package dwat.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Main_Screen extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private ViewPager viewPager;
    private TabAdapter tabAdapter;
    private dwatTabLayout tabLayout;
    private static final String TODO = "";
    String curLoc;
    long timeFromOther;
    long timeNow;

    ListView list;

    RelativeLayout screen;
    ArrayList<String> locs;
    String[] locValues = new String[] {"Food based on loc 1", "Food based on loc 2", "Food based on loc 3", "Food based on loc 4", "Food based on loc 5"};
    ArrayAdapter<String> listAdapter;

    private GoogleApiClient mGoogleApiClient;

    MealEntry buildingMeal = new MealEntry();



    private boolean check5Minutes(){

        if(timeNow != 0 && timeFromOther != 0) {
            long difference = timeNow - timeFromOther;
            double minutes = (double)difference / (1000 * 60);
            if(minutes >=  4.99) {
//                Log.e("TAG", "check 5 minutes true");
                return true;
            }
        }
        return false;
    }

    private String getCurLocation() {
        if(curLoc == null || curLoc == "")
            return null;
        else
            return curLoc;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeNow = Calendar.getInstance().getTimeInMillis();
        Bundle extras = getIntent().getExtras();
        if (extras == null) {

        } else {
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

//        screen = (RelativeLayout) findViewById(R.id.root_view_main);
//        screen.setOnTouchListener(new OnSwipeTouchListener(Main_Screen.this) {
//            public void onSwipeRight() {
//                Intent intent = new Intent(Main_Screen.this, History_Screen.class);
//                buildingMeal.date = new Date();
//                buildingMeal.location = curLoc;
//                intent.putExtra("meal", buildingMeal);
//                intent.putExtra("time", timeNow);
//                startActivity(intent);
//            }
//
//            public void onSwipeLeft() {
//                Intent intent = new Intent(Main_Screen.this, Camera_Main.class);
//                intent.putExtra("location", curLoc);
//                intent.putExtra("time", timeNow);
//                startActivity(intent);
//            }
//        });

        list = (ListView) findViewById(R.id.list);
        List<String> array_list = new ArrayList<String>();
        array_list.add("Food item 1");
        array_list.add("Food item 2");
        array_list.add("Food item 3");
        array_list.add("Food item 4");
        array_list.add("Food item 5");
        listAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, array_list);
        list.setAdapter(listAdapter);


        ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Camera_Main.class);
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


        viewPager = (ViewPager) findViewById(R.id.viewpager_main);
        tabLayout = (dwatTabLayout) findViewById(R.id.tab_layout);

        tabAdapter = new TabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);

        tabLayout.createTabs();


        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) { //

                    list.setSelector(android.R.color.transparent);
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            History newMeal;
//                            if (getCurLocation() == null) {
//                                newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate());
//                            } else
//                                newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate(), getCurLocation());
                            Toast.makeText(getApplicationContext(), "clicked and going to hist", Toast.LENGTH_SHORT).show();
//
//                            Intent intent = new Intent(Main_Screen.this, History_Screen.class);
//                            intent.putExtra("meal", newMeal);
//                            startActivity(intent);
                        }
                    });
                } else if(tab.getPosition() == 1){
                    list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//
                    list.setSelector(R.color.dwatGreen);
//
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


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



                    if(curLoc == null || refresh == true || check5Minutes() == true){
                        Log.e("TAG", locs.get(0));
                        Log.e("TAG", "got to here");
                        AlertDialog.Builder builder = new AlertDialog.Builder(Main_Screen.this);
                        builder.setTitle("Select your location");
                        builder.setItems(cs, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //locValues.add("Meal based on " + locs.get(which));
                                //locationAdapter.notifyDataSetChanged();
                                curLoc = locs.get(which);

                                ServerQuery sq = new ServerQuery();
                                sq.RequestMenu(curLoc, Main_Screen.this);

                                Toast.makeText(getApplicationContext(), locs.get(which), Toast.LENGTH_LONG).show();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.setCanceledOnTouchOutside(false);
                        alert.show();
                    }

                } catch (IllegalStateException e) {
                    Log.w("GEO", "Fail to get place or its coordinates");
                }

                likelyPlaces.release();
            }
        });
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
