package jycprogrammer.ultimatedbz.ezlapse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jeffreychen on 1/31/15.
 */
public class PhotoGridActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    public static final String EXTRA_LAPSE_ID = "The ID of the lapse clicked";
    public static final String EXTRA_EMPTY_LAPSE = "The Lapse is now empty";
    private static final int REQUEST_PHOTO = 0;

    private ArrayList<Photo> mPhotoGallery;
    private UUID mLapseId;
    private GridView mGrid;
    private static final String DIALOG_IMAGE = "image";
    private DeletePhotoAdapter deleteAdapter;
    private static final int REQUEST_DISPLAY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(getIntent().getExtras()!=null &&
                getIntent().getExtras().containsKey(EXTRA_LAPSE_ID)){

            mLapseId = (UUID) getIntent().getExtras().getSerializable(EXTRA_LAPSE_ID);
            mPhotoGallery = LapseGallery.get(getApplicationContext()).
                    getLapse(mLapseId).getPhotos();
            setTitle(LapseGallery.get(getApplicationContext()).
                    getLapse(mLapseId).getTitle());
        }else{
            finish();
        }
        updateView();
        /*
        setContentView(R.layout.activity_photo_grid);
        mGrid = (GridView) findViewById(R.id.photo_grid);
        PhotoAdapter adapter = new PhotoAdapter(mPhotoGallery);
        mGrid.setAdapter(adapter);
        mGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteAdapter = new DeletePhotoAdapter(PhotoGridActivity.this, null, mPhotoGallery);
                deleteAdapter.setAdapterView(mGrid);

                deleteAdapter.setOnItemClickListener(PhotoGridActivity.this);
                return true;
            }
        });
        */
    }

    public void onItemClick(android.widget.AdapterView<?> adapterView, View view, int position, long id) {
        view.performLongClick();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(jycprogrammer.ultimatedbz.ezlapse.R.menu.menu_photo_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_slideshow:
                Intent in = new Intent(PhotoGridActivity.this, PhotoSlideshowActivity.class);
                in.putExtra(PhotoSlideshowActivity.EXTRA_LAPSE_ID, mLapseId);
                startActivity(in);
                return true;
            case R.id.action_delete:
                deleteAdapter = new DeletePhotoAdapter(this, null, mPhotoGallery);
                deleteAdapter.setAdapterView(mGrid);

                deleteAdapter.setOnItemClickListener(this);
                return true;
            case jycprogrammer.ultimatedbz.ezlapse.R.id.action_new_photo:

                Intent i = new Intent(PhotoGridActivity.this, FullscreenCamera.class);
                i.putExtra(FullscreenCamera.EXTRA_LAPSE_ID, mLapseId);
                startActivityForResult(i, REQUEST_PHOTO);
                return true;
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

            Bitmap myBitmap = get_from_file(getItem(position).getFilePath(), 175, 175);
            ImageView picture = (ImageView) convertView.
                    findViewById(R.id.grid_item_image);
            picture.setImageBitmap(myBitmap);

            final int p = position;/*
            picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                /**
                * Starts an activity that lets users slide between pictures
                * *
                 Intent i = new Intent(PhotoGridActivity.this, ViewPagerFragmentActivity.class);
                i.putExtra(ViewPagerFragmentActivity.EXTRA_LAPSE_PHOTO_POSITION,p);
                i.putExtra(ViewPagerFragmentActivity.EXTRA_LAPSE_ID, mLapseId);

                startActivityForResult(i, REQUEST_DISPLAY);
                 }
            });*/

           return convertView;
        }
    }

    private void removePhotos(Set<Long> checked) {
        ArrayList<Photo> temp = new ArrayList<Photo>();
        Lapse currentLapse = null;
        LapseGallery lg = null;
        for (Long it : checked)
            temp.add(mPhotoGallery.get(it.intValue()));
        for (Photo it : temp) {
            Log.v("tracker", "should only run once");

            /* Delete Photo */
            lg = LapseGallery.get(getApplicationContext());
            currentLapse = lg.getLapse(mLapseId);
            currentLapse.deletePhoto(it);
            mPhotoGallery.remove(it);
        }

         /* If it's the last photo delete the lapse */
            // Implemented in Lapse.java
    }

    private class DeletePhotoAdapter extends MultiChoiceBaseAdapter {
        private ArrayList<Photo> photos;
        private AlertDialog mDialog;
        private AlertDialog.Builder mDialogBuilder;

        public DeletePhotoAdapter(Context c, Bundle savedInstanceState, ArrayList<Photo> items) {
            super(savedInstanceState);
            this.photos = items;
            mDialogBuilder = new AlertDialog.Builder(c)
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
        public void onDestroyActionMode(ActionMode mode) {
            updateView();
            setTitle(LapseGallery.get(getApplicationContext()).
                    getLapse(mLapseId).getTitle());
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_do_delete) {
                final Set<Long> checked = getCheckedItems();
                if (!checked.isEmpty()) {
                    //TODO ask are you sure?!?!
                    mDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removePhotos(checked);
                            updateView();
                        }
                    });
                    if (getCheckedItemCount() > 1)
                        mDialogBuilder.setMessage("Are you sure you want to delete "
                                + getCheckedItemCount() + " Photos?");
                    else
                        mDialogBuilder.setMessage("Are you sure you want to delete "
                                + getCheckedItemCount() + " Photo?");

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
            return photos.size();
        }

        @Override
        public Photo getItem(int position) {
            return photos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        protected View getViewImpl(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = PhotoGridActivity.this.getLayoutInflater()
                        .inflate(R.layout.delete_photo_layout, parent, false);

            }

            Bitmap myBitmap = get_from_file(getItem(position).getFilePath(), 175, 175);
            ImageView picture = (ImageView) convertView.
                    findViewById(R.id.grid_item_image);
            picture.setImageBitmap(myBitmap);

            return convertView;
        }

    }
    private Bitmap get_from_file(String filepath, int width, int height)
    {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(filepath), width, height, false);
    }

    private void updateView(){
        if(mPhotoGallery.size() == 0){
            Log.v("tracker", "mphotogallery is empty. should return");
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_EMPTY_LAPSE, true);
            returnIntent.putExtra(EXTRA_LAPSE_ID, mLapseId);
            setResult(RESULT_OK, returnIntent);
            finish();
            return;
        }

        Log.v("tracker", "mphotogallery not empty");
        setContentView(R.layout.activity_photo_grid);
        mGrid = (GridView) findViewById(R.id.photo_grid);
        setTitle(LapseGallery.get(getApplicationContext()).
                getLapse(mLapseId).getTitle());
        if(mGrid.getAdapter() != null) {
            mGrid.invalidateViews();
            //((LapseAdapter) the_grid.getAdapter()).notifyDataSetChanged();
        }else {
            PhotoAdapter adapter = new PhotoAdapter(mPhotoGallery);
            mGrid.setAdapter(adapter);
            mGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.v("tracker", "photo long click");
                    deleteAdapter = new DeletePhotoAdapter(PhotoGridActivity.this, null, mPhotoGallery);
                    deleteAdapter.setAdapterView(mGrid);

                    deleteAdapter.setOnItemClickListener(PhotoGridActivity.this);
                    mGrid.performItemClick(view, position, id);
                    return true;
                }
            });
        }
    }

    public void onActivityResult( int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK) return;
        if(requestCode == REQUEST_PHOTO){ //just finished taking a photo
            if((Boolean) data.getBooleanExtra(FullscreenCamera.EXTRA_PASS, false)) { // if a picture was actually taken, jump to the corresponding grid
                mGrid.invalidateViews();
            }
        }

    }
}

