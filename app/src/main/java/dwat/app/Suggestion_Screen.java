package dwat.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
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

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class Suggestion_Screen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
	private static final String TODO = "";
	String curDate;
	String curLoc;

	private static ExpandableListView suggestList;
	private static ExpandableListAdapter adapter;
	List listTitle;
	HashMap<String, List<String>> listDetail;
//	private ExpandableListAdapter adapter;

	RelativeLayout screen;
	/*ArrayList<MealEntry> userHistory = new ArrayList<>();
	MealEntry[] menu;
	MealEntry[] mealEntries;*/
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
				intent.putExtra("location", curLoc);
				startActivity(intent);
			}
		});

		suggestList = (ExpandableListView) findViewById(R.id.suggestList);
		suggestList.setGroupIndicator(null);

		setItems();
		setListener();

//		locationAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, locValues);
//		suggestList = (ExpandableListView) findViewById(R.id.suggestList);
//		suggestList.setAdapter((ExpandableListAdapter)locationAdapter);
//		suggestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				History newMeal;
//				if (getCurLocation() == null) {
//					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate());
//				} else
//					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate(), getCurLocation());
//				Toast.makeText(getApplicationContext(), newMeal.getHist(), Toast.LENGTH_SHORT).show();
//
//				Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
//				intent.putExtra("meal", newMeal);
//				startActivity(intent);
//			}
//		});

		ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), Camera_Main.class);
				intent.putExtra("location", curLoc);
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

	void setItems() {

		// Array list for header
		ArrayList<String> header = new ArrayList<String>();

		// Array list for child items
		List<String> child1 = new ArrayList<String>();
		List<String> child2 = new ArrayList<String>();
		List<String> child3 = new ArrayList<String>();
		List<String> child4 = new ArrayList<String>();

		// Hash map for both header and child
		HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();

		// Adding headers to list
		for (int i = 1; i < 5; i++) {
			header.add("Group " + i);

		}
		// Adding child data
		for (int i = 1; i < 5; i++) {
			child1.add("Group 1  - " + " : Child" + i);

		}
		// Adding child data
		for (int i = 1; i < 5; i++) {
			child2.add("Group 2  - " + " : Child" + i);

		}
		// Adding child data
		for (int i = 1; i < 6; i++) {
			child3.add("Group 3  - " + " : Child" + i);

		}
		// Adding child data
//		for (int i = 1; i < 7; i++) {
//			child4.add("Group 4  - " + " : Child" + i);
//
//		}

		// Adding header and childs to hash map
		hashMap.put(header.get(0), child1);
		hashMap.put(header.get(1), child2);
		hashMap.put(header.get(2), child3);
		hashMap.put(header.get(3), child4);

		adapter = new dwat.app.ExpandableListAdapter(Suggestion_Screen.this, header, hashMap);

		// Setting adpater over expandablelistview
		suggestList.setAdapter(adapter);


		// expand all lists for default // CHANGE IF WANT TO
		for(int i = 0; i < adapter.getGroupCount(); i++){
			suggestList.expandGroup(i);
		}
		Log.e("TAG", "got to here");
		Toast.makeText(Suggestion_Screen.this, "hello", Toast.LENGTH_LONG).show();
	}

	// Setting different listeners to expandablelistview
	void setListener() {

		// This listener will show toast on group click
		suggestList.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView listview, View view,
										int group_pos, long id) {

				Toast.makeText(Suggestion_Screen.this,
						"You clicked : " + adapter.getGroup(group_pos),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		});

		// This listener will expand one group at one time
		// You can remove this listener for expanding all groups
		suggestList
				.setOnGroupExpandListener(new OnGroupExpandListener() {

					// Default position
					int previousGroup = -1;

					@Override
					public void onGroupExpand(int groupPosition) {
						if (groupPosition != previousGroup)

							// Collapse the expanded group
							suggestList.collapseGroup(previousGroup);
						previousGroup = groupPosition;
					}

				});

		// This listener will show toast on child click
		suggestList.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView listview, View view,
										int groupPos, int childPos, long id) {
				Toast.makeText(
						Suggestion_Screen.this,
						"You clicked : " + adapter.getChild(groupPos, childPos),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		});
	}


	public void updateLocValues(final ServerQuery.FoodDescription[] newVals) {
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

		ServerQuery.menu = ms.toArray(new MealEntry[ms.size()]);
		try {
			ArrayList<MealEntry> combined = new ArrayList<>();
			for(MealEntry m : UserPreferences.userHistory) {
				if(m != null) {
					combined.add(m);
				}
			}
			for(MealEntry m : ServerQuery.menu) {
				if(m != null) {
					combined.add(m);
				}
			}
			MealEntry[] sortedMes = combined.toArray(new MealEntry[combined.size()]);
			sortedMes = UserPreferences.userPreference(sortedMes, curLoc, new Date());
		} catch(Exception e) { Log.d("UPREF", "Exception in UPREF: " + e.toString()); }

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					locationAdapter.clear();
					for (int i = 0; i < ms.size(); i++) {
						for (String s : ms.get(i).foods) {
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

	private void makeAlert(boolean canDismiss, String title, String[] choices) {
		AlertDialog.Builder builder = new AlertDialog.Builder(Suggestion_Screen.this);
		builder.setTitle(title);
		builder.setItems(choices, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		AlertDialog alert = builder.create();
		alert.show();

	}

	@Override
	protected void onStart() {
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

							ServerQuery sq = new ServerQuery();
							sq.RequestMenu(curLoc, Suggestion_Screen.this);

							Toast.makeText(getApplicationContext(), locs.get(which), Toast.LENGTH_LONG).show();
						}
					});

					AlertDialog alert = builder.create();
					alert.setCanceledOnTouchOutside(false);
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
					for(int i=0;i<count;i++) {
						UserPreferences.userHistory[i%maxCount] = MealEntry.Deserialize(fr);
						Log.d("UPREF", UserPreferences.userHistory[i].toString());
					}
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
