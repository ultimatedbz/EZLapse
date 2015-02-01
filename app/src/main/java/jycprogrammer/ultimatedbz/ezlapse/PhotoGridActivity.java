package jycprogrammer.ultimatedbz.ezlapse;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by jeffreychen on 1/31/15.
 */
public class PhotoGridActivity extends ActionBarActivity{

    public static final String EXTRA_LAPSE_ID = "The ID of the lapse clicked";
    private ArrayList<Photo> mPhotoGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPhotoGallery = Lapse.get(PhotoGridActivity.this).getPhotos();
        super.onCreate(savedInstanceState);

        updateView();
    }

    private class PhotoAdapter extends ArrayAdapter<Photo> {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            mPhotoGallery = LapseGallery.get(PhotoGridActivity.this).getLapses();
            super.onCreate(savedInstanceState);

            updateView();
            setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        }
        public PhotoAdapter(ArrayList<Photo> items) {

            super(PhotoGridActivity.this, 0, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = PhotoGridActivity.this.getLayoutInflater().
                        inflate(R.layout.lapse_icon_layout, parent, false);
            }

            //If want to change the layout of each icon, modify the lapse_icon_layout.xml file
            //and/or the activity_yes_lapse.xml file


            /* Displays latest picture*/
            File imgFile = new  File(getItem(position).getLatest());

            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView picture = (ImageView) convertView.
                        findViewById(R.id.grid_item_image);
                picture.setImageBitmap(myBitmap);
            }


            return convertView;
        }
    }

}
