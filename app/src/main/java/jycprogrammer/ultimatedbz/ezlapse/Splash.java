package jycprogrammer.ultimatedbz.ezlapse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by jeffreychen on 3/27/15.
 */
public class Splash extends Activity{
    private static boolean splashLoaded = false;

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2500;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if(!splashLoaded) {
            setContentView(R.layout.splash_activity);

            /* New Handler to start the Menu-Activity
             * and close this Splash-Screen after some seconds.*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    /* Create an Intent that will start the Menu-Activity. */
                    Intent mainIntent = new Intent(Splash.this, LapseGridActivity.class);
                    Splash.this.startActivity(mainIntent);
                    Splash.this.finish();
                }
            }, SPLASH_DISPLAY_LENGTH);
        }else{
            Intent goToMainActivity = new Intent(Splash.this, LapseGridActivity.class);
            goToMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(goToMainActivity);
            finish();
        }
    }

}
