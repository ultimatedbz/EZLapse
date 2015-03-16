package jycprogrammer.ultimatedbz.ezlapse;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;


public class LapseGridActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "lapse_grid_activity";
    private static final int REQUEST_PHOTO = 0;

    private Button create_lapse_button;
    public ArrayList<Lapse> mLapseGallery;
    public ArrayList<Lapse> mCurrentList;
    private GridView the_grid;
    private boolean results = false;
    private DeleteLapseAdapter deleteAdapter;


    public static final String EZdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EZLapse/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        //Parse files in EZLapse to recreate all Lapses
        //create empty Lapse Gallery
        mLapseGallery = LapseGallery.get(LapseGridActivity.this).getLapses();
        mCurrentList = mLapseGallery;
        //probably gotta do null checks for new peeps, do that later

        File f = new File(EZdirectory);
        File[] files = f.listFiles();
        if(files != null && files.length > 0 && mLapseGallery.size() == 0)
        for (File inFile : files)
            if (inFile.isDirectory() && !inFile.getName().equals("tmp")) { //ignore tmp
                //for every picture in subdirectory, put into Lapse
                File[] subFiles = inFile.listFiles();

                Lapse l = new Lapse(inFile.getName());
                for (File subFile : subFiles) {
                    String absolutePath = subFile.getAbsolutePath();
                    Photo photo = new Photo(absolutePath, new Date());
                    l.add(photo);
                }
                if (l.getPhotoNum() > 0) {
                    LapseGallery.get(LapseGridActivity.this).getLapses().add(l);
                }
            }

        updateView();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    }

    public void onItemClick(android.widget.AdapterView<?> adapterView, View view, int position, long id) {
        view.performLongClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(jycprogrammer.ultimatedbz.ezlapse.R.menu.menu_lapse_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_new:
                Intent i = new Intent(LapseGridActivity.this, FullscreenCamera.class);
                startActivityForResult(i, REQUEST_PHOTO);
                return true;
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_search:
                onSearchRequested();
                return true;
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_delete:
                supportInvalidateOptionsMenu();

                deleteAdapter = new DeleteLapseAdapter(null, mCurrentList);
                deleteAdapter.setAdapterView(the_grid);

                deleteAdapter.setOnItemClickListener(this);
                return true;
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_settings:
                Toast.makeText(getApplicationContext(), "Settings not implemented yet, too bad",
                Toast.LENGTH_SHORT).show();
                return true;
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_help:
                Toast.makeText(getApplicationContext(), "Help not implemented yet, you're on your own",
                Toast.LENGTH_SHORT).show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSearchRequested() {
        Log.v("Search Requested", "Search was invoked");
        return super.onSearchRequested();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.v("New Intent", "Got a search query");
            ArrayList<Lapse> stuff = doSearch(query);
            Log.v("New Intent", "Search is done");
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

    private void removeLapses(Set<Long> checked) {
        ArrayList<Lapse> temp = new ArrayList<Lapse>();
        for (Long it : checked)
            temp.add(mCurrentList.get(it.intValue()));
        for (Lapse it : temp) {
            Log.v("tracker","should only run once");
            it.deleteLapse();
            mLapseGallery.remove(it);
        }
    }

    private class DeleteLapseAdapter extends MultiChoiceBaseAdapter {
        private ArrayList<Lapse> lapses;

        public DeleteLapseAdapter(Bundle savedInstanceState, ArrayList<Lapse> items) {
            super(savedInstanceState);
            this.lapses = items;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(jycprogrammer.ultimatedbz.ezlapse.R.menu.menu_delete_lapse_grid, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode){
            updateView();
            setTitle("EZLapse");

        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_do_delete) {
                Set<Long> checked = getCheckedItems();
                if (!checked.isEmpty()) {
                    //TODO ask are you sure?!?!
                    removeLapses(checked);
                }
                finishActionMode();
                return true;
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }


        @Override
        public int getCount() {
            return lapses.size();
        }

        @Override
            public Lapse getItem(int position) {
            return lapses.get(position);
        }

        @Override
            public long getItemId(int position) {
            return position;
        }

        @Override
            protected View getViewImpl(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = LapseGridActivity.this.getLayoutInflater().
                inflate(jycprogrammer.ultimatedbz.ezlapse.R.layout.delete_lapse_icon_layout, parent, false);
            }

            //If want to change the layout of each icon, modify the lapse_icon_layout.xml file
            //and/or the activity_there_are_lapses_grid_lapses_grid.xml file


            /* Displays latest picture*/
            File imgFile = new  File(getItem(position).getLatest());

            if(imgFile.exists()){
                Bitmap myBitmap = get_from_file(imgFile.getAbsolutePath(), 175,175);
                //BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView picture = (ImageView) convertView.
                findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.grid_item_image);
                picture.setImageBitmap(myBitmap); //might be too big
                TextView text = (TextView) convertView.findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.grid_item_desc);
                text.setText(getItem(position).getTitle());

            }


            return convertView;
        }

    }

    private void updateView(){
        if(mCurrentList.size() > 0) {
            setContentView(jycprogrammer.ultimatedbz.ezlapse.R.layout.activity_there_are_lapses_grid);
            the_grid = (GridView) findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.main_grid);
            if(the_grid.getAdapter() != null) {
                Log.v(TAG,"Invalidating views");
                the_grid.invalidateViews();
                //((LapseAdapter) the_grid.getAdapter()).notifyDataSetChanged();
            }
            else {
                the_grid.setAdapter(new LapseAdapter(mCurrentList));
                the_grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        Intent i = new Intent(LapseGridActivity.this, PhotoGridActivity.class);
                        i.putExtra(PhotoGridActivity.EXTRA_LAPSE_ID, mCurrentList.get(position).getId());
                        startActivity(i);

                        /*Intent i = new Intent(LapseGridActivity.this, PhotoSlideshowActivity.class);
                        i.putExtra(PhotoSlideshowActivity.EXTRA_LAPSE_ID, mLapseGallery.get(position).getId());
                        startActivityForResult(i, REQUEST_PHOTO);*/
                        return true;
                    }
                });
                the_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //add to lapse
                        Intent i = new Intent(LapseGridActivity.this, FullscreenCamera.class);
                        i.putExtra(FullscreenCamera.EXTRA_LAPSE_ID, mCurrentList.get(position).getId());
                        startActivityForResult(i, REQUEST_PHOTO);
                        }
                    });
                }
        }else {
            setContentView(jycprogrammer.ultimatedbz.ezlapse.R.layout.activity_there_are_no_lapses);
            // Get EZLapse Button
            create_lapse_button = (Button) findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.no_ez_button);
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

    @Override
    public void onBackPressed() {
        if(results)
        {
            results = false;
            mCurrentList = mLapseGallery;
            updateView();
        }
        else
            super.onBackPressed();
    }

/*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
            deleteAdapter.save(outState);
    }
*/
    public void onActivityResult( int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK) return;
        if(requestCode == REQUEST_PHOTO){
            if((Boolean) data.getBooleanExtra(FullscreenCamera.EXTRA_PASS, false)) {
            updateView();
            }
        }
    }
    private void updateView(ArrayList<Lapse> terms){
        if(terms.size() > 0)
        {
            mCurrentList = terms;
            updateView();
        }else{
            setContentView(jycprogrammer.ultimatedbz.ezlapse.R.layout.no_search_results);
        }
        results = true;
        }

    private Bitmap get_from_file(String filepath, int width, int height)
    {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(filepath), width, height, false);
    }

    private class LapseAdapter extends ArrayAdapter<Lapse> {
        public LapseAdapter(ArrayList<Lapse> items) {
            super(LapseGridActivity.this, 0, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = LapseGridActivity.this.getLayoutInflater().
                        inflate(jycprogrammer.ultimatedbz.ezlapse.R.layout.lapse_icon_layout, parent, false);
            }

            //If want to change the layout of each icon, modify the lapse_icon_layout.xml file
            //and/or the activity_yes_lapse.xml file


            /* Displays latest picture*/
            File imgFile = new  File(getItem(position).getLatest());

            if(imgFile.exists()){
                Bitmap myBitmap = get_from_file(imgFile.getAbsolutePath(), 175,175);
                //BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView picture = (ImageView) convertView.
                        findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.grid_item_image);
                picture.setImageBitmap(myBitmap); //might be too big
                TextView text = (TextView) convertView.findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.grid_item_desc);
                text.setText(getItem(position).getTitle());

            }


            return convertView;
        }
    }

}


