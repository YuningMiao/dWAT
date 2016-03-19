package dwat.app;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;


public class Splash_Screen extends Activity {

    private static int SPLASH_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

//        ImageView img = (ImageView) findViewById(R.id.splash_logo);
//        img.setImageResource(R.drawable.dw_logo_1);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                Intent intent = new Intent(Splash_Screen.this, Suggestion_Screen.class);
                startActivity(intent);
                finish();
            }

        }, SPLASH_TIME);
    }

    @Override
    public void onBackPressed() {
    }
}
