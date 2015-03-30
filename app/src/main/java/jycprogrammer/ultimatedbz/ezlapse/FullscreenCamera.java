package jycprogrammer.ultimatedbz.ezlapse;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
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
    private static boolean inPreview = false;
    private static int currentCameraId = -5;
    View mView;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){
        public void onShutter(){
        }
    };

    private boolean firstPic = true;
    private boolean pictureTaken = false;
    private boolean alpha = true;
    private UUID mLapseId;
    private ImageView mImageView;
    private final StringBuilder filePath = new StringBuilder("");

    private SurfaceHolder.Callback SHCallback= new SurfaceHolder.Callback() {
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
            Log.v("tracker","surface changed");
            if (mCamera == null)
                return;

            /* Geny motion front back both are 90
            *  Actual Android phones front = 270, back = 90*/
            int cameraIndex = currentCameraId;
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(1, camInfo);
            int cameraRotationOffset = camInfo.orientation;

                /* Need to find better way of fixing camera orientation */
            if( cameraRotationOffset == 270)
                mCamera.setDisplayOrientation(90);

            Camera.Parameters p = mCamera.getParameters();

            p.set("jpeg-quality", 100);
            //p.set("orientation", "landscape");

            p.setPictureFormat(PixelFormat.JPEG);

            if( cameraRotationOffset == 270 && currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                p.set("rotation", 270);
            else
                p.set("rotation",90);
            mCamera.setParameters(p);
              /*In the future look at this http://stackoverflow.com/questions/6069122/camera-orientation-issue-in-android
              http://stackoverflow.com/questions/20064793/how-to-fix-camera-orientation
              http://stackoverflow.com/questions/11026615/captured-photo-orientation-is-changing-in-android/
              http://stackoverflow.com/questions/4645960/how-to-set-android-camera-orientation-properly  */

            try{
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                inPreview = true;
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
                inPreview = false;
            }
        }
    };


    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback(){
        public void onPictureTaken(byte[] data, Camera camera) {
            if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                data = flip(data);
            String filename = UUID.randomUUID().toString() + ".jpg";
            FileOutputStream os = null;
            filePath.setLength(0);
            boolean success = true;
            try{
                String directory = EZdirectory + "tmp/";
                File tmpF = new File(directory);
                tmpF.mkdirs();
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

                mView.findViewById(R.id.cancel_take).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.confirm_take).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.lapse_camera_takePictureButton).setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.change_camera).setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.preview_image_view).setVisibility(View.VISIBLE);
                ((ImageView) mView.findViewById(R.id.preview_image_view))
                        .setImageBitmap(BitmapFactory.decodeFile(EZdirectory + "tmp/" + filename));
                ((ImageView) mView.findViewById(R.id.preview_image_view))
                    .setScaleType(ImageView.ScaleType.FIT_XY);
                mView.findViewById(R.id.lapse_camera_surfaceView).setVisibility(View.INVISIBLE);

                if(!firstPic)
                    mView.findViewById(R.id.switch_overlay).setVisibility(View.VISIBLE);

                mImageView.setAlpha(0.01f);
                alpha = false;

            }else {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        }
    };

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mCamera = null;

        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        setContentView(R.layout.activity_fullscreen_camera);
        mView = this.getWindow().getDecorView().findViewById(android.R.id.content);

        mView.findViewById(R.id.cancel_take).setVisibility(View.INVISIBLE);
        mView.findViewById(R.id.confirm_take).setVisibility(View.INVISIBLE);
        mView.findViewById(R.id.switch_overlay).setVisibility(View.INVISIBLE);

        ImageButton switchOverlayButton = (ImageButton) mView.findViewById(R.id.switch_overlay);
        switchOverlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(alpha)
                    mImageView.setAlpha(0.01f);
                else{
                    mImageView.setAlpha(.5f);
                }
                alpha = !alpha;
            }
        });

        ImageButton cancelTakeButton = (ImageButton) mView.findViewById(R.id.cancel_take);
        cancelTakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mView.findViewById(R.id.cancel_take).setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.confirm_take).setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.lapse_camera_takePictureButton).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.switch_overlay).setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.change_camera).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.preview_image_view).setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.lapse_camera_surfaceView).setVisibility(View.VISIBLE);
                mImageView.setAlpha(0.5f);
                alpha = true;

                mCamera.startPreview();
                inPreview = true;
            }
        });

        ImageButton confirmTakeButton = (ImageButton) mView.findViewById(R.id.confirm_take);
        confirmTakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                    if (LapseDir.exists()) {
                                        Toast toast = Toast.makeText(getApplicationContext(), "Lapse with file name " +
                                                        tempTitle + " already exists ",
                                                Toast.LENGTH_SHORT);
                                        toast.show();
                                        //dialog.dismiss();
                                        //mCamera.startPreview();
                                        //inPreview = true;
                                        return;
                                    }
                                    LapseDir.mkdirs();
                                    File to = new File(LapseDirect, tempTitle + "-Photo1.jpg");
                                    File from = new File(filePath.toString());
                                    from.renameTo(to);
                                    Lapse newLapse = new Lapse(tempTitle, new Date(), to.getAbsolutePath());
                                    mLapseId = newLapse.getId();
                                    LapseGallery.get(getApplicationContext()).getLapses().add(newLapse);
                                    dialog.dismiss();

                                    pictureTaken = true;
                                    firstPic = false;
                                    mImageView.setImageBitmap(BitmapFactory.decodeFile(to.getAbsolutePath()));
                                    mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                    mImageView.setAlpha(.5f);

                                    mView.findViewById(R.id.cancel_take).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.confirm_take).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.lapse_camera_takePictureButton).setVisibility(View.VISIBLE);
                                    mView.findViewById(R.id.switch_overlay).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.change_camera).setVisibility(View.VISIBLE);
                                    mView.findViewById(R.id.preview_image_view).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.lapse_camera_surfaceView).setVisibility(View.VISIBLE);

                                    mCamera.startPreview();
                                    inPreview = true;
                                }
                            });
                    alertDialogBuilder.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {/*
                                    dialog.dismiss();

                                    mView.findViewById(R.id.cancel_take).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.confirm_take).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.lapse_camera_takePictureButton).setVisibility(View.VISIBLE);
                                    mView.findViewById(R.id.switch_overlay).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.change_camera).setVisibility(View.VISIBLE);
                                    mView.findViewById(R.id.preview_image_view).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.lapse_camera_surfaceView).setVisibility(View.VISIBLE);

                                    mCamera.startPreview();
                                    inPreview = true;*/
                                }
                            });
                    alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                           /* dialog.dismiss();

                            mView.findViewById(R.id.cancel_take).setVisibility(View.INVISIBLE);
                            mView.findViewById(R.id.confirm_take).setVisibility(View.INVISIBLE);
                            mView.findViewById(R.id.lapse_camera_takePictureButton).setVisibility(View.VISIBLE);
                            mView.findViewById(R.id.switch_overlay).setVisibility(View.INVISIBLE);
                            mView.findViewById(R.id.change_camera).setVisibility(View.VISIBLE);
                            mView.findViewById(R.id.preview_image_view).setVisibility(View.INVISIBLE);
                            mView.findViewById(R.id.lapse_camera_surfaceView).setVisibility(View.VISIBLE);
                            mCamera.startPreview();
                            inPreview = true;*/
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else{
                    Lapse currentLapse = LapseGallery.get(getApplicationContext()).getLapse(mLapseId);
                    String title = currentLapse.getTitle();
                    String number = currentLapse.getLatest().substring(currentLapse.getLatest().indexOf("-Photo") + 6, currentLapse.getLatest().indexOf(".jpg"));

                    File to = new File(EZdirectory + title + "/", title + "-Photo" + (Integer.parseInt(number) + 1) + ".jpg");
                    File from = new File(filePath.toString());
                    from.renameTo(to);
                    currentLapse.add(new Photo(to.getPath(), new Date()));
                    pictureTaken = true;
                    mCamera.startPreview();
                    mImageView.setAlpha(.5f);
                    inPreview = true;

                    mImageView.setImageBitmap(BitmapFactory.decodeFile(to.getAbsolutePath()));
                    mView.findViewById(R.id.cancel_take).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.confirm_take).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.lapse_camera_takePictureButton).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.switch_overlay).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.change_camera).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.preview_image_view).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.lapse_camera_surfaceView).setVisibility(View.VISIBLE);
                }
            }
        });


        mImageView = (ImageView) mView.findViewById(R.id.opaque_image_view);

        if(getIntent().getExtras()!=null &&
                    getIntent().getExtras().containsKey(EXTRA_LAPSE_ID))
            {
            firstPic = false;
            mLapseId = (UUID) getIntent().getExtras().getSerializable(EXTRA_LAPSE_ID);
            File imgFile = new File(LapseGallery.get(getApplicationContext()).getLapse(mLapseId)
                    .getLatest());
            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                mImageView.setImageBitmap(myBitmap);
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);

            }
                mImageView.setAlpha(.5f);
        }

        ImageButton changeCamera = (ImageButton) mView.findViewById(R.id.change_camera);
        changeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inPreview) {
                    mCamera.stopPreview();
                    inPreview = false;
                }
                /* if you don't release the current camera before switching, you app will crash */
                mCamera.release();

                //swap the id of the camera to be used
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                mCamera = Camera.open(currentCameraId);

                Camera.CameraInfo camInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(1, camInfo);
                int cameraRotationOffset = camInfo.orientation;

                /* TODO Need to find better way of fixing camera orientation */
                if (cameraRotationOffset == 270)
                    mCamera.setDisplayOrientation(90);

                Camera.Parameters p = mCamera.getParameters();

                p.set("jpeg-quality", 100);
                //p.set("orientation", "landscape");
                if (cameraRotationOffset == 270 && currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                    p.set("rotation", 270);
                else
                    p.set("rotation", 90);
                p.setPictureFormat(PixelFormat.JPEG);
                mCamera.setParameters(p);


                try {
                    //this step is critical or preview on new camera will no know where to render to
                    mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
                inPreview = true;
            }
        });

        ImageButton takePictureButton = (ImageButton) mView.findViewById(R.id.lapse_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCamera != null)
                    mCamera.takePicture(mShutterCallback, null, mJpegCallback);
            }
        });

        mSurfaceView = (SurfaceView) mView. findViewById(R.id.lapse_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(SHCallback);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fullscreen_camera, menu);
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

    @TargetApi(9)
    @Override
    public void onResume(){
        super.onResume();

        if (mCamera == null) {
            /* If left during confirm/cancel phase */
                mCamera = Camera.open(0);
                mCamera.startPreview();
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

    @Override
    public void onBackPressed() {
        if(pictureTaken)
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_PASS, true);
            returnIntent.putExtra(EXTRA_LAPSE_ID, mLapseId);
            setResult(Activity.RESULT_OK, returnIntent);
        }
        else{
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_PASS, false);
            setResult(Activity.RESULT_OK, returnIntent);
        }
        super.onBackPressed();
    }

    public byte[] flip(byte[] d)
    {
        /* Make bitmap*/
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(d, 0, d.length, options);

        /* Flip bitmap */
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap dst = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);

        /* Convert back */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        dst.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        return bos.toByteArray();
    }
}
