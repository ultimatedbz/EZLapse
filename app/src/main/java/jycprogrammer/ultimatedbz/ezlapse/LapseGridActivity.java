package jycprogrammer.ultimatedbz.ezlapse;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import jycprogrammer.ultimatedbz.ezlapse.util.SearchableActivity;


public class LapseGridActivity extends ActionBarActivity {

    private static String TAG = "lapse_grid_activity";
    private static final int REQUEST_PHOTO = 0;

    private Button create_lapse_button;
    private ArrayList<Lapse> mLapseGallery;
    private GridView the_grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLapseGallery = LapseGallery.get(LapseGridActivity.this).getLapses();
        super.onCreate(savedInstanceState);
        updateView();
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lapse_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.action_new:
                Intent i = new Intent(LapseGridActivity.this, FullscreenCamera.class);
                startActivityForResult(i, REQUEST_PHOTO);

                return true;
            case R.id.action_search:
                onSearchRequested();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.v("New Intent", "Got a search query");
            ArrayList<Lapse> stuff = doSearch(query);
            updateView(stuff);
        }
    }

    private ArrayList<Lapse> doSearch(String query)
    {
        Log.v("Query", "Doing the search");
        ArrayList<Lapse> ret = new ArrayList<Lapse>();
        for(Lapse a : mLapseGallery)
        {
            if(a.getTitle().contains(query))
                ret.add(a);
        }
        return ret;
    }
    private class LapseAdapter extends ArrayAdapter<Lapse> {
        public LapseAdapter(ArrayList<Lapse> items) {
            super(LapseGridActivity.this, 0, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = LapseGridActivity.this.getLayoutInflater().
                        inflate(R.layout.lapse_icon_layout, parent, false);
            }

            //If want to change the layout of each icon, modify the lapse_icon_layout.xml file
            //and/or the activity_yes_lapse.xml file


            /* Displays latest picture*/
            Log.v(TAG,getItem(position).getLatest());
            File imgFile = new  File(getItem(position).getLatest());

            if(imgFile.exists()){
                Log.v(TAG, "IMAGE FILE EXISTS!");
                Bitmap myBitmap = get_from_file(imgFile.getAbsolutePath(), 150,150);
                //BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                Log.v(TAG, "Bitmap created");
                ImageView picture = (ImageView) convertView.
                        findViewById(R.id.grid_item_image);
                picture.setImageBitmap(myBitmap); //might be too big
                TextView text = (TextView) convertView.findViewById(R.id.grid_item_desc);
                text.setText(getItem(position).getTitle());

            }


            return convertView;
        }
    }


    private void openSettings(){
        Intent i = new Intent(LapseGridActivity.this, SettingsActivity.class);
        startActivity(i);
    }

    private void updateView(){
        Log.v(TAG, "view updated, size of gallery is: " + mLapseGallery.size());
        if(mLapseGallery.size() > 0) {
            setContentView(R.layout.activity_yes_lapse);
            the_grid = (GridView) findViewById(R.id.main_grid);
            if(the_grid.getAdapter() != null) {
                Log.v(TAG,"Invalidating views");
                the_grid.invalidateViews();
            }
            else {
                Log.v(TAG, "else statement");
                the_grid.setAdapter(new LapseAdapter(mLapseGallery));
                the_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //view every thing in lapse
                    }
                });

                the_grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        //add to lapse
                        Log.v(TAG,"1");
                        Intent i = new Intent(LapseGridActivity.this, FullscreenCamera.class);
                        Log.v(TAG,"2");
                        i.putExtra(FullscreenCamera.EXTRA_LAPSE_ID, mLapseGallery.get(position).getId());
                        Log.v(TAG,"3");
                        startActivity(i);
                        return false;
                    }
                });
                Log.v(TAG, "set");
            }

        }
        else {
            setContentView(R.layout.activity_no_lapse);
            // Get EZLapse Button
            create_lapse_button = (Button) findViewById(R.id.no_ez_button);
            create_lapse_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Camera needs an extra in case of add picture
                    Intent i = new Intent(LapseGridActivity.this, FullscreenCamera.class);
                    startActivityForResult(i, REQUEST_PHOTO);
                }
            });
        }
    }

    public void onActivityResult( int requestCode, int resultCode, Intent data){
        Log.v(TAG, "onACtivityResult");
        if(resultCode != Activity.RESULT_OK) return;
        if(requestCode == REQUEST_PHOTO){
            if((Boolean) data.getBooleanExtra(FullscreenCamera.EXTRA_PASS, false))
                updateView();
        }
    }
    private void updateView(ArrayList<Lapse> terms){
        if(terms.size() > 0)
        {
            LapseAdapter search_results = new LapseAdapter(terms);
            LapseAdapter all_pics = (LapseAdapter) the_grid.getAdapter();
            the_grid.setAdapter(search_results);
            updateView();
            the_grid.setAdapter(all_pics);
        }
        else
        {
            setContentView(R.layout.activity_lapse_grid);
        }
    }
    private Bitmap get_from_file(String filepath, int width, int height)
    {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(filepath), width, height, false);
    }
}
