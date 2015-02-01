package jycprogrammer.ultimatedbz.ezlapse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by jeffreychen on 1/31/15.
 */
public class PhotoGridActivity extends ActionBarActivity{

    public static final String EXTRA_LAPSE_ID = "The ID of the lapse clicked";
    private ArrayList<Photo> mPhotoGallery;
    private UUID mLapseId;
    private GridView mGrid;
    private static final String DIALOG_IMAGE = "image";

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
        setContentView(R.layout.activity_photo_grid);
        mGrid = (GridView) findViewById(R.id.photo_grid);
        PhotoAdapter adapter = new PhotoAdapter(mPhotoGallery);
        mGrid.setAdapter(adapter);
        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_slideshow:
                Intent in = new Intent(PhotoGridActivity.this, PhotoSlideshowActivity.class);
                in.putExtra(PhotoSlideshowActivity.EXTRA_LAPSE_ID, mLapseId);
                startActivity(in);
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class PhotoAdapter extends ArrayAdapter<Photo> {
        public PhotoAdapter(ArrayList<Photo> items){super(PhotoGridActivity.this, 0, items);}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = PhotoGridActivity.this.getLayoutInflater()
                        .inflate(R.layout.photo_icon_layout, parent, false);

            }

            Bitmap myBitmap = BitmapFactory.decodeFile(getItem(position).getFilePath());
            ImageView picture = (ImageView) convertView.
                    findViewById(R.id.grid_item_image);
            picture.setImageBitmap(myBitmap);

            final int p = position;
            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                    ImageFragment.newInstance(getItem(p).getFilePath()).show(fm,DIALOG_IMAGE);
                }
            });


            return convertView;
        }
    }
}
