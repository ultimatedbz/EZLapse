package jycprogrammer.ultimatedbz.ezlapse;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.UUID;


public class PhotoSlideshowActivity extends ActionBarActivity {
    private ImageView image;
    private AnimationDrawable anim;
    private ArrayList<Photo> mPhotoGallery;
    private UUID mLapseId;

    public static final String EXTRA_LAPSE_ID = "anything";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().getExtras()!=null &&
                getIntent().getExtras().containsKey(EXTRA_LAPSE_ID)){

            mLapseId = (UUID) getIntent().getExtras().getSerializable(EXTRA_LAPSE_ID);
            mPhotoGallery = LapseGallery.get(getApplicationContext()).
                    getLapse(mLapseId).getPhotos();
        }else{
            finish();
        }
        setContentView(R.layout.activity_photo_slideshow2);
        anim = new AnimationDrawable();
        for (Photo p : mPhotoGallery) {
            anim.addFrame(Drawable.createFromPath(p.getFilePath()), 500);
        }

        image = (ImageView) findViewById(R.id.imageView1);
        image.setBackgroundDrawable(anim);
    }

    @Override
    protected void onStart() {
        anim.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_slideshow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
