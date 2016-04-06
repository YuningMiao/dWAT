package dwat.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.visual_recognition.v2.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v2.model.VisualClassification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    VisualRecognition service;

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
        setContentView(R.layout.activity_camera);

        curLoc = (String) getIntent().getSerializableExtra("location");

        picImg = (ImageView)findViewById(R.id.foodPic);

        screen = (RelativeLayout) findViewById(R.id.cameraScreen);
        screen.setOnTouchListener(new OnSwipeTouchListener(Camera_Main.this){
            public void onSwipeRight(){
                Intent intent = new Intent(Camera_Main.this, Suggestion_Screen.class);
                startActivity(intent);
            }
        });

        service = new VisualRecognition(VisualRecognition.VERSION_DATE_2015_12_02);
        service.setUsernameAndPassword("0bd21bc5-408e-4b92-9035-635ff00d83a9", "vBul4aWoQFIL");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(android.os.Environment.getExternalStorageDirectory(),"temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

        startActivityForResult(intent, CAMERA_REQUEST);

        ImageButton backBttn = (ImageButton) findViewById(R.id.backButton);
        ImageButton addBttn = (ImageButton) findViewById(R.id.addButton);

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
        TextView photoTags = (TextView) findViewById(R.id.photoTags);

        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            File f = new File(Environment.getExternalStorageDirectory().toString());
            for (File temp : f.listFiles()) {
                if (temp.getName().equals("temp.jpg")) {
                    f = temp;
                    break;
                }
            }
            try {
                Bitmap photo;
                BitmapFactory.Options photoOptions = new BitmapFactory.Options();
                photo = BitmapFactory.decodeFile(f.getAbsolutePath(), photoOptions);

                // make photo portrait and set placeholder
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
                picImg.setImageBitmap(photo);

                Log.e("TAG", "Classify using all the classifiers");
                VisualClassification result = service.classify(f);
                System.out.println(result);
                Log.e("TAG", String.valueOf(result) + " - hello");
                photoTags.setText(photoTags.getText().toString() + "\n" + result + " - hello");

                String path = android.os.Environment.getExternalStorageDirectory() + File.separator + "Phoenix" + File.separator + "default";
                f.delete();
                OutputStream outFile = null;
                File file = new File(path, String.valueOf(System.currentTimeMillis() + ".jpg"));


                try {
                    outFile = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outFile);
                    outFile.flush();
                    outFile.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
    }
}
