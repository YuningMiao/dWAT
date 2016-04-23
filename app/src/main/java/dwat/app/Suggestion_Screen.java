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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class Suggestion_Screen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
	private static final String TODO = "";
	String curLoc;
	long timeFromOther;
	long timeNow;

	ListView suggestList;
//	private static ExpandableListAdapter adapter;

	RelativeLayout screen;
	ArrayList<String> locs;
	private GoogleApiClient mGoogleApiClient;

//	final ArrayList<String> headers = new ArrayList<>();
//	HashMap<String, List<String>> headerMap = new HashMap<>();
//	HashMap<String, String[]> modifiersMap = new HashMap<>();

	MealEntry buildingMeal = new MealEntry();

	private boolean check5Minutes(){

		if(timeNow != 0 && timeFromOther != 0) {
			long difference = timeNow - timeFromOther;
			double minutes = (double)difference / (1000 * 60);
			if(minutes >=  4.99) {
				Log.e("TAG", "check 5 minutes true");
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggest2);

		timeNow = Calendar.getInstance().getTimeInMillis();

//		Intent locationIntent = getIntent();
		Bundle extras = getIntent().getExtras();
		if(extras == null) {

		}
		else {
			curLoc = extras.getString("location");
			timeFromOther = extras.getLong("time");
		}
//			if(extras.containsKey("location"))
//				curLoc = extras.getString("location");
//			if(extras.containsKey("time"))
//				timeFromOther = extras.getLong("time");
//			Log.e("TAG", timeNow + " now ");
//			Log.e("TAG", timeFromOther + " other");
		//}

		mGoogleApiClient = new GoogleApiClient
				.Builder( this )
				.enableAutoManage( this, 0, this )
				.addApi( Places.GEO_DATA_API )
				.addApi( Places.PLACE_DETECTION_API )
				.addConnectionCallbacks( this )
				.addOnConnectionFailedListener( this )
				.build();

		guessCurrentPlace(false);

		screen = (RelativeLayout) findViewById(R.id.suggestScreen);
		screen.setOnTouchListener(new OnSwipeTouchListener(Suggestion_Screen.this) {
			public void onSwipeRight() {
				Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
				buildingMeal.date = new Date();
				buildingMeal.location = curLoc;
				intent.putExtra("meal", buildingMeal);
				intent.putExtra("time", timeNow);
				startActivity(intent);
			}

			public void onSwipeLeft() {
				Intent intent = new Intent(Suggestion_Screen.this, Camera_Main.class);
				intent.putExtra("location", curLoc);
				intent.putExtra("time", timeNow);
				startActivity(intent);
			}
		});

		suggestList = (ListView) findViewById(R.id.suggestList);
//		suggestList = (ListView) findViewById(R.id.suggestList);
//		suggestList.setGroupIndicator(null);
//		setListener();

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
					if (ContextCompat.checkSelfPermission(Suggestion_Screen.this,
							Manifest.permission.ACCESS_FINE_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(Suggestion_Screen.this,
								new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
								1000);
					} else {
						timeNow = Calendar.getInstance().getTimeInMillis();
						guessCurrentPlace(true);
					}
				}
			}
		});

		new ReadMealEntries().execute(); //read old MealEntry items from userhist.dat
	}

//	void forceUpdate(int groupIndex) {
//		suggestList.collapseGroup(groupIndex); //without this, the list will not update correctly
//		suggestList.expandGroup(groupIndex);   //needed for redraw
//	}

	boolean isChecked(String s) {
		return s != null && s.startsWith("\u2713 ");
	}

	void removeMealItem(String foodname, String modifier) {
		foodname = foodname.startsWith("\u2713 ") ? foodname.substring(2) : foodname;
		if(modifier != null)
			modifier = modifier.startsWith("\u2713 ") ? modifier.substring(2) : modifier;
		int index = buildingMeal.foods.indexOf(foodname);
		if(index > -1) {
			buildingMeal.foods.remove(index);
			buildingMeal.modifiers.remove(index);
		}
	}

	void addMealItem(String foodname, String modifier) {
		buildingMeal.foods.add(foodname);
		if (modifier != null)
			buildingMeal.modifiers.add(new String[]{modifier});
		else
			buildingMeal.modifiers.add(new String[0]);
	}

	//checkmark an element if not checkmarked
//	void checkmarkElement(int groupIndex, int childIndex) {
//		String parent = headers.get(groupIndex);
//		String new_parent = "\u2713 " + parent;
//		if(!parent.startsWith("\u2713 ")) {
//			headers.set(groupIndex, new_parent);
//		}
//		if(headerMap.get(parent) != null && headerMap.get(parent).size() > 0 && childIndex >= 0) {
//			String child = headerMap.get(parent).get(childIndex);
//			if(!child.startsWith("\u2713 ")) {
//				List children = headerMap.get(parent);
//				headerMap.put(new_parent, children);
//				headerMap.get(new_parent).set(childIndex, "\u2713 " + child);
//				headerMap.remove(parent);
//			}
//		}
//	}
//
//	void uncheckmarkElement(int groupIndex, int childIndex) {
//		String parent = headers.get(groupIndex);
//		String new_parent = parent.substring(2);
//		int numChildrenChecked = 0;
//		boolean hasValidChild = headerMap.get(parent) != null && headerMap.get(parent).size() > 0 && childIndex >= 0;
//		if(hasValidChild) {
//			String child = headerMap.get(parent).get(childIndex);
//			if(child.startsWith("\u2713 ")) {
//				List<String> children = headerMap.get(parent);
//				for(String s : children) {
//					if(s.startsWith("\u2713 ")) {
//						numChildrenChecked++;
//					}
//				}
//				if(numChildrenChecked == 1) {
//					headerMap.put(new_parent, children);
//					headerMap.get(new_parent).set(childIndex, child.substring(2));
//					headerMap.remove(parent);
//				} else {
//					headerMap.get(parent).set(childIndex, child.substring(2));
//				}
//			}
//		}
//		if(parent.startsWith("\u2713 ") && (numChildrenChecked == 1 || !hasValidChild)) {
//			headers.set(groupIndex, new_parent);
//		}
//	}

	void handleUpdate(int groupIndex, int childIndex) {

	}

/*
	void markOrUnmarkElement(String parent, String child, int groupIndex, int childIndex) {
		if(parent.startsWith("\u2713 ")) {
			int index = buildingMeal.foods.indexOf(parent.substring(2));
			String old_parent = buildingMeal.foods.get(index);
			String checked_parent = headers.get(groupIndex);
			String[] old_mods = buildingMeal.modifiers.get(index);
			buildingMeal.modifiers.remove(index);
			if(old_mods.length > 0) {
				List children = headerMap.get(checked_parent);
				if(!children.get(childIndex).toString().startsWith("\u2713 ")) {
					for(int i=0;i<children.size();i++) {
						if (children.get(i).toString().startsWith("\u2713 ")) {
							children.set(i, children.get(i).toString().substring(2));
						}
					}
					buildingMeal.modifiers.add(new String[]{child});
					headerMap.get(checked_parent).set(childIndex, "\u2713 " + child);
					return;
				} else {
					headerMap.put(old_foodname, children);
					headerMap.get(old_foodname).set(childIndex, old_mods[0]);
				}
			}
			headers.set(groupIndex, old_parent);
			headerMap.remove(checked_parent);
			buildingMeal.foods.remove(index);
		} else {
			String new_foodname = "\u2713 " + foodname;
			if (modifier == null) {
				buildingMeal.modifiers.add(new String[0]);
			} else {
				buildingMeal.modifiers.add(new String[]{modifier});
				List children = headerMap.get(foodname);
				headerMap.put(new_foodname, children);
				headerMap.get(new_foodname).set(childIndex, "\u2713 " + modifier);
			}
			headers.set(groupIndex, new_foodname);
			headerMap.remove(foodname);
			buildingMeal.foods.add(foodname);
		}
	}*/

	/*List<String> toList(String[] s) {
		List<String> newList = new ArrayList<>(s.length);
		for(int i=0;i<s.length;i++) {
			newList.add(s[i]);
		}
		return newList;
	}*/

//	String[] findModifiers(String header) {
//		header.replace("\u2713 ", "");
//		if(modifiersMap.containsKey(header)) {
//			return modifiersMap.get(header);
//		}
//		return null;
//	}

	// Setting different listeners to expandablelistview
//	void setListener() {
//
//		// This listener will show toast on group click
//		suggestList.setOnGroupClickListener(new OnGroupClickListener() {
//
//			@Override
//			public boolean onGroupClick(ExpandableListView listview, View view,
//										int group_pos, long id) {
//				String parent = adapter.getGroup(group_pos).toString();
//				String[] mods = findModifiers(parent);
//
//				if(mods != null && mods.length > 0) {
//					makeAlert("Select the size", parent, mods, group_pos, 0);
//				} else if (adapter.getChildrenCount(group_pos) <= 0) {
//					//no children
//					//markOrUnmarkElement(parent, null, group_pos, -1);
//					if (isChecked(parent)) {
//						uncheckmarkElement(group_pos, -1);
//						removeMealItem(parent, null);
//					} else {
//						checkmarkElement(group_pos, -1);
//						addMealItem(parent, null);
//					}
//				}
//
//				return false;
//			}
//		});
//
//		// This listener will expand one group at one time
//		// You can remove this listener for expanding all groups
//		suggestList
//				.setOnGroupExpandListener(new OnGroupExpandListener() {
//
//					// Default position
//					int previousGroup = -1;
//
//					@Override
//					public void onGroupExpand(int groupPosition) {
//						if (groupPosition != previousGroup)
//
//							// Collapse the expanded group
//							suggestList.collapseGroup(previousGroup);
//						previousGroup = groupPosition;
//					}
//
//				});
//
//		// This listener will show toast on child click
//		suggestList.setOnChildClickListener(new OnChildClickListener() {
//
//			@Override
//			public boolean onChildClick(ExpandableListView listview, View view,
//										int groupPos, int childPos, long id) {
//
//				String parent = adapter.getGroup(groupPos).toString();
//				String child = adapter.getChild(groupPos, childPos).toString();
//
//				String[] mods = findModifiers(child);
//
//				if (mods != null && mods.length > 0) {
//					makeAlert("Select a size", child, mods, groupPos, childPos);
//				} else {
//					if (isChecked(parent) && isChecked(child)) {
//						uncheckmarkElement(groupPos, childPos);
//						removeMealItem(parent, child);
//					} else {
//						checkmarkElement(groupPos, childPos);
//						addMealItem(parent, child);
//					}
//				}
//				forceUpdate(groupPos);
//
//
//
//				/*markOrUnmarkElement(parent, child, groupPos, childPos);*/
//
//				return false;
//			}
//		});
//	}


	public void updateLocValues(final ServerQuery.FoodDescription[] newVals) {
		final ArrayList<MealEntry> ms = new ArrayList<>();
//		headers.clear();
		for (int i=0;i<newVals.length;i++) {
			MealEntry m = new MealEntry(curLoc, new Date(), newVals[i]);
			boolean found = false;
			for(int j=0;j<ms.size();j++) {
				if(m.foods.size() > 0 && ms.get(j).foods.contains(m.foods.get(0))) {
					ms.get(j).addModifiers(newVals[i].Modifiers, newVals[i].BadModifiers);
					found = true;
					break;
				}
			}
			if(!found) {
				ms.add(m);
			}
		}

		ServerQuery.menu = ms;

		for(int i=0;i<Math.min(5,UserPreferences.userHistory.size());i++) {
			MealEntry m = UserPreferences.userHistory.get(i);
			String header = m.toString();
			ArrayList<String> mods = new ArrayList<>();
			for(int j=0;j<m.foods.size();j++) {
				mods.add(m.foods.get(j));
//				modifiersMap.put(m.foods.get(j), m.modifiers.get(j));
			}
//			headers.add(header);
//			headerMap.put(header, mods);
		}

		for(MealEntry m : ms) {
			if(m.foods.size() > 0) {
				String header = m.foods.get(0) /*+ " (" + m.value + ")"*/;
//				headers.add(header);
				ArrayList<String> mods = new ArrayList<String>();
				for(String[] s : m.modifiers) {
					for(String s2 : s) {
						mods.add(s2);
					}
				}
				//headerMap.put(header, mods);
				String[] sMods = mods.toArray(new String[mods.size()]);
//				modifiersMap.put(header, sMods);
			}
		}

		/*try {
			ArrayList<MealEntry> combined = new ArrayList<>();
			MealEntry[] userHistory = UserPreferences.userHistory.toArray(new MealEntry[UserPreferences.userHistory.size()]);
			userHistory = UserPreferences.userPreference(userHistory, curLoc, new Date());
			for(int i=0;i<Math.min(5,userHistory.length);i++) {
				if(userHistory[i] != null) {
					combined.add(userHistory[i]);
				}
			}
			for(MealEntry m : ServerQuery.menu) {
				if(m != null) {
					combined.add(m);
				}
			}
			MealEntry[] sortedMes = combined.toArray(new MealEntry[combined.size()]);
			sortedMes = UserPreferences.userPreference(sortedMes, curLoc, new Date());

		} catch (Exception e) {
			Log.d("UPREF", "Exception in UPREF: " + e.toString());
		}*/

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
//					adapter = new dwat.app.ExpandableListAdapter(Suggestion_Screen.this, headers, headerMap);
//					suggestList.setAdapter(adapter);
				} catch (Exception e) {
					Log.d("SERVCOMM", "Exception: " + e.toString());
				}
			}
		});
		Log.d("SERVCOMM", "locValues set: " + ms.size() + " items");
	}

	private void makeAlert(String title, final String foodname, final String[] choices, final int groupIndex, final int childIndex) {
		AlertDialog.Builder builder = new AlertDialog.Builder(Suggestion_Screen.this);
		builder.setTitle(title);
		builder.setItems(choices, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//markOrUnmarkElement(foodname, choices[which], groupIndex, childIndex);
//				checkmarkElement(groupIndex, childIndex);
				addMealItem(foodname, choices[which]);
//				forceUpdate(groupIndex);
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
					AlertDialog.Builder builder = new AlertDialog.Builder(Suggestion_Screen.this);
					builder.setTitle("Select your location");
					builder.setItems(cs, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//locValues.add("Meal based on " + locs.get(which));
							//locationAdapter.notifyDataSetChanged();
							curLoc = locs.get(which);

							ServerQuery sq = new ServerQuery();
//							sq.RequestMenu(curLoc, Suggestion_Screen.this);

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
					guessCurrentPlace(false);
				}
				break;
		}
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

			return null;
		}
	}
}
