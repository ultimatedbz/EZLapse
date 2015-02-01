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
    private final String EZdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EZLapse/";

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private final String tempTitle = "";

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){
        public void onShutter(){
        }
    };
    private boolean firstPic = true;
    private UUID mLapseId;
    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback(){
        public void onPictureTaken(byte[] data, Camera camera){
            String filename = UUID.randomUUID().toString() + ".jpg";
            FileOutputStream os = null;
            final StringBuilder filePath = new StringBuilder("");
            boolean success = true;
            try{
                String directory = Environment.getExternalStorageDirectory().getAbsolutePath()  + "/EZLapse/tmp/";
                new File(directory).mkdirs();
                os = new FileOutputStream(directory + filename);
                filePath.append(directory + filename);
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

            if(success) {
                Log.v(TAG, "success");
                /*Create AlertDialog that writes into tempTitle*/
                /* picture is saved, do something with it, ask for title etc*/
                Log.v("PIc success", "Picture, " + firstPic);
                if(firstPic) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FullscreenCamera.this);
                    final EditText textbox = new EditText(FullscreenCamera.this);
                    alertDialogBuilder.setTitle("Add a title to your EZLapse")
                            .setView(textbox)
                            .setCancelable(true);
                    alertDialogBuilder.setPositiveButton(R.string.confirm,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int arg1) {
                                    String tempTitle = textbox.getText().toString();
                                    String LapseDirect = EZdirectory + tempTitle + "/";
                                    File LapseDir = new File(LapseDirect);
                                    LapseDir.mkdirs();
                                    File to = new File(LapseDirect, tempTitle + "-Photo1.jpg");
                                    File from = new File(filePath.toString());
                                    from.renameTo(to);
                                    Lapse newLapse = new Lapse(tempTitle, new Date(), to.getAbsolutePath());
                                    LapseGallery.get(getApplicationContext()).getLapses().add(newLapse);

                                    dialog.dismiss();
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra(EXTRA_PASS, true);
                                    setResult(Activity.RESULT_OK, returnIntent);
                                    finish();
                                }
                            });
                    alertDialogBuilder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra(EXTRA_PASS, false);
                                    setResult(RESULT_CANCELED, returnIntent);
                                    finish();
                                }
                            });
                    alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(EXTRA_PASS, true);
                            setResult(RESULT_CANCELED, returnIntent);
                            finish();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                else{
                    Lapse currentLapse = LapseGallery.get(getApplicationContext()).getLapse(mLapseId);
                    String title = currentLapse.getTitle();
                    int size = currentLapse.getPhotoNum();
                    File to = new File(EZdirectory + title + "/", title + "-Photo" + ++size + ".jpg");
                    File from = new File(filePath.toString());
                    from.renameTo(to);
                    currentLapse.add(new Photo(to.getPath(), new Date()));
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(EXTRA_PASS, true);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }else{
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
            //finish();
        }
    };

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        mCamera = null;

        setContentView(R.layout.activity_fullscreen_camera);
        View v = this.getWindow().getDecorView().findViewById(android.R.id.content);


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
                iv.setAlpha(.5f);
        }



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

                int mRotation = 90;

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
