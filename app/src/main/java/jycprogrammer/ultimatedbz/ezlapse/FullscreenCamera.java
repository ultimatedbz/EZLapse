package jycprogrammer.ultimatedbz.ezlapse;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class FullscreenCamera extends ActionBarActivity {

    private static final String TAG = "FullscreenCamera";
    public static final String EXTRA_PASS = "photo was passed";
    public static final String EXTRA_LAPSE_ID = "id of the lapse";

    private View mProgressContainer;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private String tempTitle;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){
        public void onShutter(){
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };
    private boolean firstPic = true;
    private UUID mLapseId;
    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback(){
        public void onPictureTaken(byte[] data, Camera camera){
            String filename = UUID.randomUUID().toString() + ".jpg";
            FileOutputStream os = null;
            String filePath = "";
            boolean success = true;
            try{
                String directory = Environment.getExternalStorageDirectory().getAbsolutePath()  + "/EZLapse/tmp/";
                new File(directory).mkdirs();
                os = new FileOutputStream(directory + filename);
                filePath = directory + filename;
                os.write(data);


            }catch (Exception e){
                Log.e(TAG, "Error writing to file " + filename, e);
                Toast toast = Toast.makeText(getApplicationContext(), "Error writing to file " + filename,
                        Toast.LENGTH_SHORT);
                toast.show();

                success = false;
            }finally{
                try{
                    if(os != null)
                        os.close();
                }catch( Exception e){
                    Log.e(TAG, "Error closing the file " + filename, e);
                    Toast toast = Toast.makeText(getApplicationContext(), "Error closing the file " + filename,
                            Toast.LENGTH_SHORT);
                    toast.show();

                    success = false;
                }
            }
            /*TODO add implementation
              if this if first photo
                title  and check and cancel, for now it always saves title
              else
                preview and check x*/

            //for now, we are just directly setting title
            if(success) {
                String EZdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EZLapse/";
                if (firstPic){
                    Log.v(TAG, "success");
                    /*Create AlertDialog that writes into tempTitle*/
                    /* picture is saved, do something with it, ask for title etc*/
                    openDialogBox();
                    //assume DialogBox got the title

                    //create new Lapse directory
                    //assume for now that they always put a valid title
                    tempTitle = "test";


                    Log.v(TAG, EZdirectory);
                    File LapseDir = new File(EZdirectory + tempTitle + "/");
                    Log.v(TAG, LapseDir.getPath());
                    LapseDir.mkdirs();

                    //move file from /tmp to directory
                    Log.v(TAG, filePath);
                    File to = new File(EZdirectory + tempTitle + "/", tempTitle + "-Photo1.jpg");
                    File from = new File(filePath);
                    from.renameTo(to);

                    Lapse newLapse = new Lapse(tempTitle, new Date(), filePath);
                    LapseGallery.get(getApplicationContext()).getLapses().add(newLapse);

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(EXTRA_PASS, true);

                    setResult(Activity.RESULT_OK, returnIntent);
                }
                else {
                    Lapse currentLapse = LapseGallery.get(getApplicationContext()).getLapse(mLapseId);
                    String title = currentLapse.getTitle();
                    int size = currentLapse.getPhotoNum();

                    File to = new File(EZdirectory + title + "/", title + "-Photo" + ++size + ".jpg");
                    File from = new File(filePath);
                    from.renameTo(to);
                                       
                    currentLapse.add(new Photo(to.getPath(), new Date()));
                }
            }else{
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
            }
            finish();
        }
    };

    private void openDialogBox(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Add a title to your EZLapse");

        final EditText input = new EditText(getApplicationContext());
        alertDialogBuilder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        Log.v(TAG, "positive button clicked");

                        dialog.dismiss();
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        mCamera = null;
        tempTitle = "";

        setContentView(R.layout.activity_fullscreen_camera);
        View v = this.getWindow().getDecorView().findViewById(android.R.id.content);
        mProgressContainer = v.findViewById(R.id.lapse_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        ImageView iv = (ImageView) v.findViewById(R.id.opaque_image_view);

        if(getIntent().getExtras()!=null &&
                    getIntent().getExtras().containsKey(EXTRA_LAPSE_ID))
            {
            firstPic = false;
            mLapseId = (UUID) getIntent().getExtras().getSerializable(EXTRA_LAPSE_ID);
            File imgFile = new File(LapseGallery.get(getApplicationContext()).getLapse(mLapseId)
                    .getLatest());
            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                iv.setImageBitmap(myBitmap);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);

            }

            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                AlphaAnimation alpha = new AlphaAnimation(0.7F, 0.7F);
                alpha.setDuration(0); // Make animation instant
                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                iv.startAnimation(alpha);
            } else
                iv.setAlpha(.5f);
        }

        /*else{
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                AlphaAnimation alpha = new AlphaAnimation(0F, 0F);
                alpha.setDuration(0); // Make animation instant
                alpha.setFillAfter(true); // Tell it to persist after the animation ends
                iv.startAnimation(alpha);
            } else
                iv.setAlpha(0f);
        }*/

        ImageButton takePictureButton = (ImageButton) v.findViewById(R.id.lapse_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCamera != null)
                    mCamera.takePicture(mShutterCallback, null, mJpegCallback);
            }
        });

        mSurfaceView = (SurfaceView) v. findViewById(R.id.lapse_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try{
                    if(mCamera != null){
                        mCamera.setPreviewDisplay(holder);
                    }
                }catch (IOException exception){
                    Log.e(TAG, "Error setting up preview display", exception);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mCamera == null)
                    return;

                Camera.Parameters parameters = mCamera.getParameters();


                Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

                int mRotation = display.getRotation();

                parameters.setRotation(mRotation); //set rotation to save the picture

                mCamera.setDisplayOrientation(mRotation); //set the rotation for preview camera

                mCamera.setParameters(parameters);

                Log.v(TAG, String.valueOf(display.getRotation()));


                    Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width,
                            height);
                    parameters.setPreviewSize(s.width, s.height);

                    s = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
                    parameters.setPictureSize(s.width, s.height);



                mCamera.setParameters(parameters);
                try{
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                }catch(Exception e){
                    Log.e(TAG, "Could not start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if( mCamera != null){
                    mCamera.stopPreview();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fullscreen_camera, menu);
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
    @TargetApi(9)
    @Override
    public void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            if (mCamera == null) {
                mCamera = Camera.open(0);
                Log.v(TAG, "on resume ran");
            }
        }else{
            if (mCamera == null) mCamera = Camera.open();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }


    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height){
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for(Camera.Size s : sizes){
            int area = s.width *s.height;
            if (area > largestArea){
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }




}
