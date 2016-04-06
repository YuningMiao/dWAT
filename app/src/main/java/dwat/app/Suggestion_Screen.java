package dwat.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class Suggestion_Screen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private static final String TODO = "";
    String curDate;
    String curLoc;
    ListView sl1, sl2;
	RelativeLayout screen;
    String[] histValues = new String[]{"Food Item 1", "Food Item 2", "Food Item 3", "Food Item 4", "Food Item 5", "Food Item 6"};
	ArrayList<String> locValues = new ArrayList<String>(Arrays.asList("Food based on loc 1", "Food based on loc 2", "Food based on loc 3", "Food based on loc 4", "Food based on loc 5"));
	ArrayAdapter<String> adapter, adapter2;
	private GoogleApiClient mGoogleApiClient;

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

		UserPreferences up = new UserPreferences();
		up.RequestMenu("McDonald's", this);

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
		screen.setOnTouchListener(new OnSwipeTouchListener(Suggestion_Screen.this){
			public void onSwipeRight(){
				Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
				startActivity(intent);
			}
			public void onSwipeLeft(){
				Intent intent = new Intent(Suggestion_Screen.this, Camera_Main.class);
				startActivity(intent);
			}
		});

		//final String curDate = new SimpleDateFormat("MM-dd-yyyy").format(new Date());

		sl1 = (ListView) findViewById(R.id.suggestList1);
		adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, histValues);

		sl1.setAdapter(adapter);
		sl1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				History newMeal;
				if (getCurLocation() == null) {
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate());
				} else
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate(), getCurLocation());
				Toast.makeText(getApplicationContext(), newMeal.getHist(), Toast.LENGTH_SHORT).show();

				Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
				intent.putExtra("meal", (Serializable) newMeal);
				startActivity(intent);
			}
		});

		sl2 = (ListView) findViewById(R.id.suggestList2);
		adapter2 = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, locValues);

		sl2.setAdapter(adapter2);
		sl2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				History newMeal;
				if (getCurLocation() == null) {
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate());
				} else
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate(), getCurLocation());
				Toast.makeText(getApplicationContext(), newMeal.getHist(), Toast.LENGTH_SHORT).show();

				Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
				intent.putExtra("meal", newMeal);
				startActivity(intent);
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
	}

	public void updateLocValues(String[] newVals) {
		int minLength = Math.min(locValues.size(), newVals.length);
		for(int i=0;i<minLength;i++) {
			locValues.set(i, newVals[i]);
		}
		//adapter2.notifyDataSetChanged();
		Log.d("SERVCOMM", "locValues updated");
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
					PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
					String content = "";
					if (placeLikelihood != null && placeLikelihood.getPlace() != null && !TextUtils.isEmpty(placeLikelihood.getPlace().getName()))
						content = placeLikelihood.getPlace().getName() + "";
					if (placeLikelihood != null)
						content += " " + (int) (placeLikelihood.getLikelihood() * 100) + "%";

					locValues.add("Meal based on " + content);
					adapter2.notifyDataSetChanged();
					curLoc = content;
					Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();

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
}
