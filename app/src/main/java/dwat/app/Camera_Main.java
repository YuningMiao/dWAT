package dwat.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Date;

public class Camera_Main extends FragmentActivity {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView picImg;
    String curDate;
    String curLoc;
    RelativeLayout screen;

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
        setContentView(R.layout.activity_camera);
        picImg = (ImageView)findViewById(R.id.foodPic);

        screen = (RelativeLayout) findViewById(R.id.cameraScreen);
        screen.setOnTouchListener(new OnSwipeTouchListener(Camera_Main.this){
            public void onSwipeRight(){
                Intent intent = new Intent(Camera_Main.this, Suggestion_Screen.class);
                startActivity(intent);
            }
        });


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(intent, 0);

        Button backBttn = (Button) findViewById(R.id.backButton);
        Button addBttn = (Button) findViewById(R.id.addButton);
        backBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(Camera_Main.this, Suggestion_Screen.class);
                startActivity(backIntent);
            }
        });
        addBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                History newMeal;
                if(getCurLocation() == null) {
                    newMeal = new History("Tags", getCurDate());
                }
                else
                    newMeal = new History("Tags", getCurDate(), getCurLocation());
                Toast.makeText(getApplicationContext(), newMeal.getHist(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Camera_Main.this, History_Screen.class);
                intent.putExtra("meal", newMeal);
                startActivity(intent);
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            picImg.setImageBitmap(photo);
        }

    }

    @Override
    public void onBackPressed() {
    }
}
