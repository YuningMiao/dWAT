package dwat.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.visual_recognition.v2.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v2.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v2.model.VisualClassifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Camera_Main extends FragmentActivity {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView picImg;
    String curLoc;
    long time;
    RelativeLayout screen;
    File f;
    MealEntry buildingMeal = new MealEntry();

    ArrayList<String> tags = new ArrayList<>();
    HashMap<String, ArrayList<String>> modifierMap;
    ListView photoTags;
    ArrayAdapter<String> tagAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        modifierMap = (HashMap<String, ArrayList<String>>) getIntent().getSerializableExtra("modmap");
        buildingMeal = (MealEntry) getIntent().getSerializableExtra("meal");
        curLoc = getIntent().getStringExtra("location");
        time = getIntent().getLongExtra("time", 0L);

        picImg = (ImageView)findViewById(R.id.foodPic);

        screen = (RelativeLayout) findViewById(R.id.cameraScreen);
        screen.setOnTouchListener(new OnSwipeTouchListener(Camera_Main.this) {
            public void onSwipeLeft() {
                Intent swipeIntent = new Intent(Camera_Main.this, Main_Screen.class);
                swipeIntent.putExtra("meal", buildingMeal);
                swipeIntent.putExtra("location", curLoc);
                swipeIntent.putExtra("time", time);
                startActivity(swipeIntent);
            }
            public void onSwipeRight() {
                Intent swipeIntent = new Intent(Camera_Main.this, History_Screen.class);
                buildingMeal.location = curLoc;
                buildingMeal.date.add(new Date());
                swipeIntent.putExtra("meal", buildingMeal);
                swipeIntent.putExtra("location", buildingMeal.location);
                swipeIntent.putExtra("time", time);
                startActivity(swipeIntent);
            }
        });

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(android.os.Environment.getExternalStorageDirectory(),"temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

        startActivityForResult(intent, CAMERA_REQUEST);

        ImageButton backBttn = (ImageButton) findViewById(R.id.backButton);
        ImageButton addBttn = (ImageButton) findViewById(R.id.addButton);

        backBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(Camera_Main.this, Main_Screen.class);
                backIntent.putExtra("location", curLoc);
                backIntent.putExtra("time", time);
                startActivity(backIntent);
            }
        });

        addBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(Camera_Main.this, Camera_Main.class);
                addIntent.putExtra("meal", buildingMeal);
                addIntent.putExtra("time", time);
                startActivity(addIntent);
            }
        });

        tagAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, tags);
        photoTags = (ListView) findViewById(R.id.photoTags);

        photoTags.setAdapter(tagAdapter);
        photoTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(ServerQuery.menu != null) {
                    for(ServerQuery.FoodDescription fd : ServerQuery.menu) {
                        if(fd.FoodName.contains(tags.get(position))) {
                            String item = tags.get(position);
                            if(buildingMeal.foods.contains(item)) {
                                buildingMeal.remove(fd.FoodName);
                                photoTags.getChildAt(position).setBackgroundColor(getResources().getColor(android.R.color.transparent));
                            } else if(modifierMap.containsKey(item) && modifierMap.get(item).size() > 0) {
                                ArrayList<String> list = modifierMap.get(item);
                                makeAlert("Choose a size", item, DwatUtil.toArray(list), position);
                            }  else {
                                buildingMeal.add(fd.FoodName, fd.Modifiers);
                                photoTags.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.dwatBlue));
                            }
                            break;
                        }
                    }
                }

                /*Intent intent = new Intent(Camera_Main.this, History_Screen.class);
                intent.putExtra("meal", buildingMeal);
                startActivity(intent);*/
            }
        });
    }

    private void makeAlert(String title, final String foodname, final String[] choices, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Camera_Main.this);
        builder.setTitle(title);
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buildingMeal.add(foodname, choices[which]);
                photoTags.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.dwatBlue));
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
            try {
                Bitmap photo;
                BitmapFactory.Options photoOptions = new BitmapFactory.Options();
                photo = BitmapFactory.decodeFile(f.getAbsolutePath(), photoOptions);

                // make photo portrait and set placeholder
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
                picImg.setImageBitmap(photo);

                new visual().execute();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent(Camera_Main.this, Main_Screen.class);
        startActivity(backIntent);
    }

    public class visual extends AsyncTask<Void, Void, Integer> {
        VisualRecognition service;
        VisualClassification result;
        File image;
        ArrayList<String> results;
        ProgressDialog pd;

        protected Integer doInBackground(Void... params){
            try {
                service = new VisualRecognition(VisualRecognition.VERSION_DATE_2015_12_02);
                service.setUsernameAndPassword("0bd21bc5-408e-4b92-9035-635ff00d83a9", "vBul4aWoQFIL");


                File image = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                VisualClassifier vc1 = new VisualClassifier("FrenchFries_1081543305");
                VisualClassifier vc2 = new VisualClassifier("FiletOFish_1218850016");
                VisualClassifier vc3 = new VisualClassifier("HashBrowns_340212582");
                VisualClassifier vc4 = new VisualClassifier("BigMac_997392234");
                VisualClassifier vc5 = new VisualClassifier("BeefnCheddar_814647028");
                VisualClassifier vc6 = new VisualClassifier("CurlyFries_456201371");
                VisualClassifier vc7 = new VisualClassifier("RoastBeef_1621569146");
                VisualClassifier vc8 = new VisualClassifier("ChickenMcNuggets_894203528");
                //VisualClassifier vc9= new VisualClassifier("McNuggets_897931682");
                result = service.classify(image, vc1, vc2, vc3, vc4, vc5, vc6, vc7, vc8);
                String result1 = result.toString();
                Pattern pattern = Pattern.compile("\"name\": \"(.*?)\",");
                Matcher matcher = pattern.matcher(result1);
                results = new ArrayList<String>(20);
                while (matcher.find()) {
                    System.out.println(matcher.group(1));

                    results.add(matcher.group(1));
                }
            } catch(VerifyError e) { Log.d("CAMM", e.toString()); }
            return 1;
        }

        protected void onPreExecute(){
            //photoTags.setText("Analyzing");
            pd = ProgressDialog.show(Camera_Main.this, "Analyzing", "processing image");
        }

        protected void onPostExecute(Integer result){
            if(pd != null)
                pd.dismiss();

            tags.clear();
            if(results != null) {
                for (int i = 0; i < results.size(); i++) {
                    if(ServerQuery.menu != null) {
                        for(ServerQuery.FoodDescription fd : ServerQuery.menu) {
                            if(fd.FoodName.contains(results.get(i))) {
                                tags.add(results.get(i));
                                break;
                            }
                        }
                    }
                }
                tagAdapter.notifyDataSetChanged();
            }
            f.delete();
        }
    }
}
