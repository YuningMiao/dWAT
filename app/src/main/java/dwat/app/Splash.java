package dwat.app;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class Splash extends Activity {

    private static int SPLASH_TIME = 3000;
//    protected  boolean _active = true;
//    protected int _splashTime = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

//        ImageView img = (ImageView) findViewById(R.id.splash_logo);
//        img.setImageResource(R.drawable.dw_logo_1);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                Intent intent = new Intent(Splash.this, Camera_Main.class);
               startActivity(intent);
                finish();
            }

        }, SPLASH_TIME);
//        Thread splashThread = new Thread(){
//            @Override
//            public void run(){
//                try {
//                    int waited = 0;
//                    while (_active && (waited < _splashTime)) {
//                        sleep(100);
//                        if (_active) {
//                            waited += 100;
//                        }
//                    }
//                } catch (Exception e) {
//
//                } finally {
//
//                    startActivity(new Intent(Splash.this,
//                            Camera_Main.class));
//                    finish();
//                }
//            };
//        };
//        splashThread.start();
    }
}
