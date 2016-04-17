package dwat.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
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
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Camera_Main extends FragmentActivity {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView picImg;
    String curDate;
    String curLoc;
    RelativeLayout screen;
    File f;
    ArrayList<String> header = new ArrayList<String>();
    HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();

//    ArrayList<String> tags = new ArrayList<String>(Arrays.asList("Tag 1", "Tag 2", "Tag 3"));
    private static ExpandableListView photoTags;
    private static android.widget.ExpandableListAdapter tagAdapter;

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

        curLoc = getIntent().getStringExtra("location");

        picImg = (ImageView)findViewById(R.id.foodPic);

        screen = (RelativeLayout) findViewById(R.id.cameraScreen);
        screen.setOnTouchListener(new OnSwipeTouchListener(Camera_Main.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(Camera_Main.this, Suggestion_Screen.class);
                startActivity(intent);
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
                Intent backIntent = new Intent(Camera_Main.this, Suggestion_Screen.class);
                startActivity(backIntent);
            }
        });

        addBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                History newMeal;
                if (getCurLocation() == null) {
                    newMeal = new History("Tags", getCurDate());
                } else
                    newMeal = new History("Tags", getCurDate(), getCurLocation());
                Toast.makeText(getApplicationContext(), newMeal.getHist(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Camera_Main.this, History_Screen.class);
                intent.putExtra("meal", newMeal);
                startActivity(intent);
            }
        });

        //tagAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, tags);

        photoTags = (ExpandableListView) findViewById(R.id.photoTags);
        photoTags.setGroupIndicator(null);


        //photoTags.setAdapter(tagAdapter);
//        photoTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                History newMeal;
//                if (getCurLocation() == null) {
//                    newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate());
//                } else
//                    newMeal = new History(parent.getAdapter().getItem(position).toString(), getCurDate(), getCurLocation());
//                Toast.makeText(getApplicationContext(), newMeal.getHist(), Toast.LENGTH_SHORT).show();
//
//                Intent intent = new Intent(Camera_Main.this, History_Screen.class);
//                intent.putExtra("meal", newMeal);
//                startActivity(intent);
//            }
//        });
    }

    void setItems(String parent, List<String> children) {
        // Array list for child items
        List<String> child = new ArrayList<String>();

        // Hash map for both header and child


        // Adding headers to list
        header.add(parent);
        for(int i = 0; i < children.size(); i++){
            child.add(parent + " : " + i);
        }

        hashMap.put(header.get(header.size() - 1), child);


    }

    // Setting different listeners to expandablelistview
    void setListener() {

        // This listener will show toast on group click
        photoTags.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView listview, View view,
                                        int group_pos, long id) {

                Toast.makeText(Camera_Main.this,
                        "You clicked : " + tagAdapter.getGroup(group_pos),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // This listener will expand one group at one time
        // You can remove this listener for expanding all groups
        photoTags.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            // Default position
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousGroup)

                    // Collapse the expanded group
                    photoTags.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }

        });

        // This listener will show toast on child click
        photoTags.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView listview, View view,int groupPos, int childPos, long id) {
                Toast.makeText(Camera_Main.this, "You clicked : " + tagAdapter.getChild(groupPos, childPos),Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            f = new File(Environment.getExternalStorageDirectory().toString());
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

//                File image = new File(f.getAbsolutePath());

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
        Intent backIntent = new Intent(Camera_Main.this, Suggestion_Screen.class);
        startActivity(backIntent);
    }

    public class visual extends AsyncTask<Void, Void, Integer> {
        VisualRecognition service;
        VisualClassification result;
        File image;
        ArrayList<String> results;
        ProgressDialog pd;

        protected Integer doInBackground(Void... params){
            service = new VisualRecognition(VisualRecognition.VERSION_DATE_2015_12_02);
            service.setUsernameAndPassword("0bd21bc5-408e-4b92-9035-635ff00d83a9", "vBul4aWoQFIL");
            //image = new File(Environment.getExternalStorageDirectory().toString() + "temp.jpg");;
            //image = params[0].getAbsoluteFile();

            File image = new File(Environment.getExternalStorageDirectory().toString());
            for (File temp : image.listFiles()) {
                if (temp.getName().equals("temp.jpg")) {
                    image = temp;
                    break;
                }
            }
            result = service.classify(image);
            String result1 = result.toString();
            Pattern pattern = Pattern.compile("\"name\": \"(.*?)\",");
            Matcher matcher = pattern.matcher(result1);
            results = new ArrayList<String>(20);
            while (matcher.find()) {
                System.out.println(matcher.group(1));

                results.add(matcher.group(1));
            }
            return 1;
        }

        protected void onPreExecute(){
            //photoTags.setText("Analyzing");
            pd = ProgressDialog.show(Camera_Main.this, "Analyzing", "processing image");
        }

        protected void onPostExecute(Integer result){
            pd.dismiss();

//            tags.clear();
            List<String> children = new ArrayList<>();
            for(int i = 0; i < results.size(); i++){
                setItems(results.get(i), children);
            }
//
            tagAdapter = new dwat.app.ExpandableListAdapter(Camera_Main.this, header, hashMap);

            photoTags.setAdapter(tagAdapter);
            setListener();
            f.delete();
        }
    }
}
