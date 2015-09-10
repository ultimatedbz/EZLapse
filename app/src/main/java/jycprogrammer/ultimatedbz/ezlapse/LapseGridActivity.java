package jycprogrammer.ultimatedbz.ezlapse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.UUID;


public class LapseGridActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "lapse_grid_activity";
    private static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_GRID = 1;
    private static final int RESULT_SETTINGS = 2;

    private Button create_lapse_button;
    public ArrayList<Lapse> mLapseGallery;
    public ArrayList<Lapse> mCurrentList;
    private ListView the_grid;
    private boolean results = false;
    private DeleteLapseAdapter deleteAdapter;

    private float scale = 0;// dp to pixel conversion


    public static final String EZdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EZLapse/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);


        scale = getResources().getDisplayMetrics().density;
        //Parse files in EZLapse to recreate all Lapses
        //create empty Lapse Gallery
        mLapseGallery = LapseGallery.get(LapseGridActivity.this).getLapses();
        mCurrentList = mLapseGallery;

        File f = new File(EZdirectory);
        File[] files = f.listFiles();
        if(files != null && files.length > 0 && mLapseGallery.size() == 0){
            for (File inFile : files) {
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
                       mLapseGallery.add(l);
                    }
                } else if(inFile.isDirectory()){
                    Lapse.deleteDirectory(inFile);
                }
            }
        }

        updateView();
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    }

    public void onItemClick(android.widget.AdapterView<?> adapterView, View view, int position, long id) {
        view.performLongClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(jycprogrammer.ultimatedbz.ezlapse.R.menu.menu_lapse_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_new:
                Intent i = new Intent(LapseGridActivity.this, FullscreenCamera.class);
                startActivityForResult(i, REQUEST_PHOTO);
                return true;
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_search:
                if(onSearchRequested())
                    Log.v("tracker","searched");
                else
                    Log.v("tracer", "not searched");

                return true;
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_delete:
                //supportInvalidateOptionsMenu();

                deleteAdapter = new DeleteLapseAdapter(this, null, mCurrentList);
                deleteAdapter.setAdapterView(the_grid);

                deleteAdapter.setOnItemClickListener(this);
                return true;/*
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_settings:
                Log.v("tracker", "0");
                Intent j = new Intent(this, PreferencesActivity.class);
                startActivityForResult(j, RESULT_SETTINGS);
                return true;*/
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_help:
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(LapseGridActivity.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.help_message)
                        .setTitle("Help");

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_about:
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder2 = new AlertDialog.Builder(LapseGridActivity.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder2.setMessage(R.string.about_message)
                        .setTitle("About");

                // 3. Get the AlertDialog from create()
                AlertDialog dialog2 = builder2.create();
                dialog2.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSearchRequested() {
        Log.v("tracker", "Search was invoked");
        return super.onSearchRequested();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            ArrayList<Lapse> stuff = doSearch(query);
            updateView(stuff);
        }
    }


    private ArrayList<Lapse> doSearch(String query)
    {
        Log.v("tracker","doSearch");
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
            it.deleteLapse();
            mCurrentList.remove(it);
            mLapseGallery.remove(it);
        }
    }

    private class DeleteLapseAdapter extends MultiChoiceBaseAdapter {
        private ArrayList<Lapse> lapses;
        private AlertDialog mDialog;
        private AlertDialog.Builder mDialogBuilder;

        public DeleteLapseAdapter(Context c, Bundle savedInstanceState, ArrayList<Lapse> items) {
            super(savedInstanceState);
            this.lapses = items;
            setTitle("Delete");
            mDialogBuilder =  new AlertDialog.Builder(c)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Delete")
                    .setPositiveButton("Yes", null)
                    .setNegativeButton("No", null);
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
                final Set<Long> checked = getCheckedItems();
                if (!checked.isEmpty()) {
                    mDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeLapses(checked);
                            updateView();
                        }
                    });
                    if (getCheckedItemCount() > 1)
                        mDialogBuilder.setMessage("Are you sure you want to delete "
                                + getCheckedItemCount() + " Lapses?");
                    else
                        mDialogBuilder.setMessage("Are you sure you want to delete "
                                + getCheckedItemCount() + " Lapse?");

                    mDialog = mDialogBuilder.create();
                    mDialog.show();
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
                convertView = LapseGridActivity.this.getLayoutInflater()
                .inflate(jycprogrammer.ultimatedbz.ezlapse.R.layout.delete_lapse_icon_layout, parent, false);
                TextView text = (TextView) convertView.findViewById(R.id.grid_item_desc);
                text.setText(getItem(position).getTitle());
                //inflate(R.layout.delete_lapse_icon_layout, parent, false);
            }

            /* Displays latest picture*/
            File imgFile = new  File(getItem(position).getLatest());

            if(imgFile.exists()){
                //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView picture = (ImageView) convertView.
                findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.grid_item_image);
                picture.setImageBitmap(decodeSampledBitmapFromResource(imgFile.getAbsolutePath(),(int) (120 * scale + 0.5f), (int) (120 * scale + 0.5f)));

                TextView text = (TextView) convertView.findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.grid_item_desc);
                text.setText(getItem(position).getTitle());

            }
            return convertView;
        }

    }

    public class CustomComparator implements Comparator<Lapse> {
        @Override
        public int compare(Lapse o1, Lapse o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    }

    private void updateView(){
        if(mCurrentList.size() > 0) { // filtered lapses or all lapses
            setContentView(R.layout.activity_there_are_lapses_grid);
            the_grid = (ListView) findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.main_grid);
            Collections.sort(mCurrentList, new CustomComparator());
            if(the_grid.getAdapter() != null) {
                the_grid.invalidateViews();
                //((LapseAdapter) the_grid.getAdapter()).notifyDataSetChanged();
            }
            else {
                the_grid.setAdapter(new LapseAdapter(mCurrentList));
                the_grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.v("tracker","switching to delete");
                        deleteAdapter = new DeleteLapseAdapter(LapseGridActivity.this, null, mCurrentList);
                        deleteAdapter.setAdapterView(the_grid);

                        //deleteAdapter.setOnItemClickListener(LapseGridActivity.this);

                        /*
                        the_grid.setAdapter(deleteAdapter);

                        deleteAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Log.v("tracker", "delete view on item click");
                                }
                        });
                        */
                        the_grid.performItemClick(view, position, id);
                        return true;
                    }
                });
                the_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        /*
                        Intent i = new Intent(LapseGridActivity.this, FullscreenCamera.class);
                        i.putExtra(FullscreenCamera.EXTRA_LAPSE_ID, mCurrentList.get(position).getId());
                        startActivityForResult(i, REQUEST_PHOTO);
                        */
                        Log.v("tracker", "grid item short click");
                        Intent i = new Intent(LapseGridActivity.this, PhotoGridActivity.class);
                        i.putExtra(PhotoGridActivity.EXTRA_LAPSE_ID, mCurrentList.get(position).getId());
                        startActivityForResult(i, REQUEST_GRID);
                        }
                    });
                }
        }else if(mCurrentList.size() != mLapseGallery.size()) { // no search results but there is at least one lapse
            onBackPressed();
        }else{ // no lapses at all
            setContentView(jycprogrammer.ultimatedbz.ezlapse.R.layout.activity_there_are_no_lapses);
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

    public void onActivityResult( int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK) return;
        if(requestCode == REQUEST_PHOTO){ //just finished taking a photo
            if((Boolean) data.getBooleanExtra(FullscreenCamera.EXTRA_PASS, false)) { // if a picture was actually taken, jump to the corresponding grid

                Intent i = new Intent(LapseGridActivity.this, PhotoGridActivity.class);
                UUID id = (UUID) data.getExtras().getSerializable(FullscreenCamera.EXTRA_LAPSE_ID);
                i.putExtra(PhotoGridActivity.EXTRA_LAPSE_ID, id);
                startActivityForResult(i, REQUEST_GRID);
                updateView();
            }
        }
        if( requestCode == REQUEST_GRID) { // just returned from grid

            if(data.getBooleanExtra(PhotoGridActivity.EXTRA_EMPTY_LAPSE, false)) // since we can delete from grid, if the whole lapse has no more photos, delete it
            {
                UUID id = (UUID) data.getExtras().getSerializable(PhotoGridActivity.EXTRA_LAPSE_ID);
                Lapse it = LapseGallery.get(LapseGridActivity.this).getLapse(id);
                mCurrentList.remove(it);
                mLapseGallery.remove(it);
                updateView();
           }
        }
        if( requestCode == RESULT_SETTINGS) {
            showUserSettings();
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

            /* Displays latest picture*/
            File imgFile = new  File(getItem(position).getLatest());
            if(imgFile.exists()){
                //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ImageView picture = (ImageView) convertView.
                        findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.grid_item_image);
                //picture.setImageBitmap(myBitmap); //might be too big

                picture.setImageBitmap(decodeSampledBitmapFromResource(imgFile.getAbsolutePath(),(int) (120 * scale + 0.5f), (int) (120 * scale + 0.5f)));
                TextView text = (TextView) convertView.findViewById(jycprogrammer.ultimatedbz.ezlapse.R.id.grid_item_desc);
                text.setText(getItem(position).getTitle());

            }


            return convertView;
        }
    }

    private void showUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        StringBuilder builder = new StringBuilder();

        builder.append("\n Username: "
                + sharedPrefs.getString("prefUsername", "NULL"));

        builder.append("\n Send report:"
                + sharedPrefs.getBoolean("prefSendReport", false));

        builder.append("\n Sync Frequency: "
                + sharedPrefs.getString("prefSyncFrequency", "NULL"));

       /* TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);

        settingsTextView.setText(builder.toString());*/
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        //Log.v("tracker", height + " " + width + " " + reqWidth + " " + reqHeight);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeFile(path,options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }
}


