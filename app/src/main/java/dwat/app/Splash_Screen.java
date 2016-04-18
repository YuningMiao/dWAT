package dwat.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class Splash_Screen extends Activity{

    private static int SPLASH_TIME = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);


                Intent intent = new Intent(Splash_Screen.this, Suggestion_Screen.class);
                startActivity(intent);
                finish();

    }

}
