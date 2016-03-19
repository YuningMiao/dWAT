package dwat.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Suggestion_Screen extends Activity {
    private static final String TODO = "";
    String curDate;
    String curLoc;
    ListView sl1, sl2;
	RelativeLayout screen;
    String[] histValues = new String[]{"Food Item 1", "Food Item 2", "Food Item 3", "Food Item 4", "Food Item 5", "Food Item 6"};
    String[] locValues = new String[]{"Food based on Loc 1", "Food based on Loc 2", "Food based on Loc 3", "Food based on Loc 4", "Food based on Loc 5"};
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private String getCurDate() {
        Calendar c = Calendar.getInstance();

        curDate = c.get(Calendar.MONTH) + 1 + "-" + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.YEAR) + " ";
        curDate += c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
        return curDate;
    }

    private String getCurLocation() {
//        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        CurLocationListner locListner = new CurLocationListner();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return TODO;
//        }
//        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListner);
//        return curLoc;
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggest2);

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
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, histValues);

		sl1.setAdapter(adapter);
		sl1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				History newMeal;
				if(getCurLocation() == null) {
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate());
				}
				else
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate(), getCurLocation());
				Toast.makeText(getApplicationContext(), newMeal.getHist(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
				intent.putExtra("meal", (Serializable) newMeal);
				startActivity(intent);
			}
		});

		sl2 = (ListView) findViewById(R.id.suggestList2);
		ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, locValues);

		sl2.setAdapter(adapter2);
		sl2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				History newMeal;
				if(getCurLocation() == null) {
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate());
				}
				else
					newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate(), getCurLocation());
				Toast.makeText(getApplicationContext(), newMeal.getHist(), Toast.LENGTH_SHORT).show();

				Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
				intent.putExtra("meal", newMeal);
				startActivity(intent);
			}
		});

		ImageButton button = (ImageButton) findViewById(R.id.cameraButton);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), Camera_Main.class);
				startActivityForResult(intent, 0);
			}
		});
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Suggestion_Screen Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://dwat.app/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Suggestion_Screen Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://dwat.app/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}

	@Override
	public void onBackPressed() {
	}
}
