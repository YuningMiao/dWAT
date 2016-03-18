package dwat.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Suggestion_Screen extends Activity{

    ListView sl1;
    String[] histValues = new String[] {"Food Item 1", "Food Item 2", "Food Item 3", "Food Item 4", "Food Item 5", "Food Item 6"};
    String[] locValues = new String[] {"Food based on Loc 1", "Food based on Loc 2", "Food based on Loc 3", "Food based on Loc 4", "Food based on Loc 5"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_suggest2);
        sl1 = (ListView) findViewById(R.id.suggestList1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, histValues);

        sl1.setAdapter(adapter);
        sl1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getApplicationContext(), parent.getAdapter().getItem(position).toString(), Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
//                startActivity(intent);
            }
        });
        sl1 = (ListView) findViewById(R.id.suggestList2);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.activity_listview, R.id.textView, locValues);

        sl1.setAdapter(adapter2);
        sl1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), parent.getAdapter().getItem(position).toString(), Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(Suggestion_Screen.this, History_Screen.class);
//                startActivity(intent);
            }
        });

        Button button = (Button) findViewById(R.id.take_pic_button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "Pressed button", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(v.getContext(), Camera_Main.class);
                startActivityForResult(intent, 0);
            }
        });
    }

}
