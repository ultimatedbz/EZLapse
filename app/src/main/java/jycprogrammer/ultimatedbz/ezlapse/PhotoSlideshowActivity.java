package jycprogrammer.ultimatedbz.ezlapse;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.UUID;


public class PhotoSlideshowActivity extends ActionBarActivity {
    private ImageView image;
    private AnimationDrawable anim;
    private ArrayList<Photo> mPhotoGallery;
    private UUID mLapseId;
    private ImageButton pauseButton;
    private ImageButton playButton;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Animation a;

            a = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
            a.reset();
            currentButton.clearAnimation();
            currentButton.startAnimation(a);

            currentButton.setVisibility(View.INVISIBLE);
        }
    };

    private ImageButton currentButton;
    private Context mContext;

    public static final String EXTRA_LAPSE_ID = "anything";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        if(getIntent().getExtras()!=null &&
                getIntent().getExtras().containsKey(EXTRA_LAPSE_ID)){

            mLapseId = (UUID) getIntent().getExtras().getSerializable(EXTRA_LAPSE_ID);
            mPhotoGallery = LapseGallery.get(getApplicationContext()).
                    getLapse(mLapseId).getPhotos();
        }else{
            finish();
        }
        setContentView(R.layout.activity_photo_slideshow);
        anim = new AnimationDrawable();
        anim.setOneShot(false);
        for (Photo p : mPhotoGallery) {
            anim.addFrame(Drawable.createFromPath(p.getFilePath()), 250);
        }

        image = (ImageView) findViewById(R.id.imageView1);
        image.setBackgroundDrawable(anim);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentButton.setVisibility(View.VISIBLE);
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 5000);
            }
        });

        playButton = (ImageButton) findViewById(R.id.play_slideshow);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim.start();
                pauseButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.INVISIBLE);

                currentButton = pauseButton;
            }
        });
        playButton.setVisibility(View.INVISIBLE);

        pauseButton = (ImageButton) findViewById(R.id.pause_slideshow);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim.stop();
                playButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);

                currentButton = playButton;
            }
        });

        anim.start();
        currentButton = pauseButton;
        handler.postDelayed(runnable, 5000);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_slideshow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
