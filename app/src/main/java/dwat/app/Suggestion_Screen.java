package dwat.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class Suggestion_Screen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private static final String TODO = "";
    String curDate;
    String curLoc;
    ListView suggestList;
	RelativeLayout screen;
	ArrayList<MealEntry> userHistory = new ArrayList<>();
	MealEntry[] mealEntries;
    //String[] histValues = new String[]{"Food Item 1", "Food Item 2", "Food Item 3", "Food Item 4", "Food Item 5", "Food Item 6"};
	ArrayList<String> locValues = new ArrayList<String>(/*Arrays.asList("Food based on loc 1", "Food based on loc 2", "Food based on loc 3", "Food based on loc 4", "Food based on loc 5")*/);
	ArrayAdapter<String> locationAdapter;
	ArrayList<String> locs;
	private GoogleApiClient mGoogleApiClient;
	Intent mealHistIntent;

    private String getCurDate() {
        Calendar c = Calendar.getInstance();

        curDate = c.get(Calendar.MONTH) + 1 + "-" + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.YEAR) + " ";
        curDate += c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
        return curDate;
    }

    private String getCurLocation() {
		if(curLoc == null || curLoc == "")
			return null;
		else
			return curLoc;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggest2);

		mGoogleApiClient = new GoogleApiClient
			.Builder( this )
			.enableAutoManage( this, 0, this )
			.addApi( Places.GEO_DATA_API )
			.addApi( Places.PLACE_DETECTION_API )
			.addConnectionCallbacks( this )
			.addOnConnectionFailedListener( this )
			.build();

		guessCurrentPlace();

		screen = (RelativeLayout) findViewById(R.id.suggestScreen);
		screen.setOnTouchListener(new OnSwipeTouchListener(Suggestion_Screen.this) {
			public void onSwipeRight() {
				Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
				startActivity(intent);
			}

			public void onSwipeLeft() {
				Intent intent = new Intent(Suggestion_Screen.this, Camera_Main.class);
				startActivity(intent);
			}
		});


		locationAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, locValues);
		suggestList = (ListView) findViewById(R.id.suggestList);
		suggestList.setAdapter(locationAdapter);
		suggestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				History newMeal;
				if (getCurLocation() == null) {
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate());
				} else
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate(), getCurLocation());
				Toast.makeText(getApplicationContext(), newMeal.getHist(), Toast.LENGTH_SHORT).show();

				mealHistIntent = new Intent(Suggestion_Screen.this, History_Screen.class);
				mealHistIntent.putExtra("meal", newMeal);
				startActivity(mealHistIntent);
			}
		});

		ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), Camera_Main.class);
				startActivityForResult(intent, 0);
			}
		});

		ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGoogleApiClient.isConnected()) {
					if (ContextCompat.checkSelfPermission(Suggestion_Screen.this,
							Manifest.permission.ACCESS_FINE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(Suggestion_Screen.this,
								new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
								1000);
					} else {
						guessCurrentPlace();
					}
				}
			}
		});

		new ReadMealEntries().execute(); //read old MealEntry items from userhist.dat
	}

	public void updateLocValues(final UserPreferences.FoodDescription[] newVals) {
		final ArrayList<MealEntry> ms = new ArrayList<>();
		for (int i=0;i<newVals.length;i++) {
			MealEntry m = new MealEntry(curLoc, new Date(), newVals[i]);
			boolean found = false;
			for(int j=0;j<ms.size();j++) {
				if(m.foods.size() > 0 && ms.get(j).foods.contains(m.foods.get(0))) {
					ms.get(j).modifiers.add(newVals[i].Modifiers);
					ms.get(j).badmodifiers.add(newVals[i].BadModifiers);
					found = true;
					break;
				}
			}
			if(!found) {
				ms.add(m);
			}
		}

		ms.addAll(userHistory);
		try {
			MealEntry[] sortedMes = ms.toArray(new MealEntry[ms.size()]);
			sortedMes = UserPreferences.userPreference(sortedMes, curLoc, new Date());
		} catch(Exception e) { Log.d("UPREF", e.toString()); }

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					locationAdapter.clear();
					for(int i=0;i<ms.size();i++) {
						for(String s : ms.get(i).foods) {
							locationAdapter.add(s + " (value=" + ms.get(i).value + ")");
							//locationAdapter.getCount()-1 = current index
						}
					}
					locationAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					Log.d("SERVCOMM", "Exception: " + e.toString());
				}
			}
		});
		Log.d("SERVCOMM", "locValues set: " + ms.size() + " items");
	}

	@Override
	protected void onStart(){
		super.onStart();
		if (mGoogleApiClient != null)
			mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
		super.onStop();
	}

	private void guessCurrentPlace() {
		PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace( mGoogleApiClient, null );
		result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
			@Override
			public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
				if (!likelyPlaces.getStatus().isSuccess()) {
					Log.e("TAG", "Place query didn't complete. Error: " + likelyPlaces.getStatus().toString());
					likelyPlaces.release();
					return;
				}
				try {
					locs = new ArrayList<String>();

					PlaceLikelihood placeLikelihood;
					String value;
					for(int i = 0; i < 5; i++){
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

					AlertDialog.Builder builder = new AlertDialog.Builder(Suggestion_Screen.this);
					builder.setTitle("Select your location");
					builder.setItems(cs, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//locValues.add("Meal based on " + locs.get(which));
							//locationAdapter.notifyDataSetChanged();
							curLoc = locs.get(which);

							UserPreferences up = new UserPreferences();
							up.RequestMenu(curLoc, Suggestion_Screen.this);

							Toast.makeText(getApplicationContext(), locs.get(which), Toast.LENGTH_LONG).show();
						}
					});

					AlertDialog alert = builder.create();
					alert.show();

				} catch (IllegalStateException e) {
					Log.w("TAG", "Fail to get place or its coordinates");
				}

				likelyPlaces.release();
			}
		});
	}

	@Override
	public void onBackPressed() {
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
					guessCurrentPlace();
				}
				break;
		}
	}

	public class ReadMealEntries extends AsyncTask<Object, String, Object> {
		protected Object doInBackground(Object... o) {
			int count = 0;
			final int maxCount = 1000;
			FileReader fr = null;
			try {
				File f = new File(getExternalFilesDir(null), "userhist.dat");
				f.delete();
				f.createNewFile();
				Log.d("UPREF", f.getAbsolutePath());
				fr = new FileReader(f);
				count = fr.read();
				Log.d("UPREF", "Count=" + count);
				if(count > 0) {
					mealEntries = new MealEntry[Math.min(count,maxCount)];
					for(int i=0;i<count;i++) {
						mealEntries[i%maxCount] = MealEntry.Deserialize(fr);
						Log.d("UPREF", mealEntries[i].toString());
					}
				} else {
					mealEntries = new MealEntry[0];
				}
			} catch(IOException e) {
				Log.d("UPREF", e.toString());
			} catch(Exception e) {
				Log.d("UPREF", e.toString());
			} finally {
				/*Intent menuIntent = new Intent(Suggestion_Screen.this, History_Screen.class);
				menuIntent.putExtra("mealEntries", mealEntries);
				startActivity(menuIntent);*/
				try {
					fr.close();
				} catch (IOException e) {}
			}
			Log.d("UPREF", "MealEntries deserialized: " + count + " objects read");

			return null;
		}
	}
}
