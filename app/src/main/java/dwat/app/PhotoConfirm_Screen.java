package dwat.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class PhotoConfirm_Screen extends Activity{
    ImageView picImg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        picImg = (ImageView) findViewById(R.id.foodPic);
        BitmapDrawable drawable = (BitmapDrawable) getIntent().getSerializableExtra("foodImg");
        picImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        picImg.setImageDrawable(drawable);


    }
}
