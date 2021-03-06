package jycprogrammer.ultimatedbz.ezlapse;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.UUID;


public class FullscreenCamera extends ActionBarActivity {

    /* Member Variables */
    /* The way the camera works is that we first set mSurfaceView's camera in onResume.
    *  mSurfaceView then automatically calls onMeasure which sets the mPreviewSize. That is why
    *  inPreview is first set to true. We don't need tp call surfacechanged() because it does that
    *  for us, and if we do call it, the app will crash because mPreviewSize hasn't been set yet.
    *
    *  When we change cameras, we have to get it to set mPreviewSize correctly. This is done
    *  by ...manually calling onMeasure when changeCamera button is pressed. lol.
    *
    * We also need to manually call surfacechanged() in onResume() because on resume, we have to
    * restart the camera. we don't need to call onmeasure though because the camera is the same
    * so should be using the same parameters that were already set to be correct earlier on.
    * */

    private static final String TAG = "FullscreenCamera";
    public static final String EXTRA_PASS = "photo was passed";
    public static final String EXTRA_LAPSE_ID = "id of the lapse";
    public static boolean samsung = false;
    private final String EZdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EZLapse/";


    private CameraSurfaceView mSurfaceView;
    //janky but it's for first camera to not go in preview mode. This is so that onmeasure can be called
    // the surface view automatically calls onMeasure twice -> surfacechanged()
    public static boolean inPreview = true;
    public static int currentCameraId = 0;
    View mView;
    private boolean firstPic = true;
    private boolean pictureTaken = false;
    private boolean alpha = true;
    private UUID mLapseId;
    private ImageView mOverlay;
    private final StringBuilder filePath = new StringBuilder("");

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };



    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            /* This is put here because in Samsungs, shuttercallback calls stop preview. and
            then we need to start the preview before we make the surfaceview invisible*/
            mSurfaceView.mCamera.startPreview();
            inPreview = true;

            if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                data = flip(data);
            String filename = UUID.randomUUID().toString() + ".jpg";
            FileOutputStream os = null;
            filePath.setLength(0);
            boolean success = true;
            try {
                String directory = EZdirectory + "tmp/";
                File tmpF = new File(directory);
                tmpF.mkdirs();
                os = new FileOutputStream(directory + filename);
                filePath.append(directory + filename);
                os.write(data);
                os.close();

                ExifInterface exif=new ExifInterface(filePath.toString());
                Bitmap realImage = BitmapFactory.decodeByteArray(data , 0, data.length);
                Log.v(TAG, "exif: " + exif.getAttribute(ExifInterface.TAG_ORIENTATION));

                if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6") || (samsung &&exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0") )){
                    realImage=rotate(realImage, 90);
                }else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
                    realImage=rotate(realImage, 180);
                }else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
                    realImage=rotate(realImage, 180);
                }

                if(!exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("1")) {
                    os = new FileOutputStream(directory + filename);
                    realImage.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();

                }
            } catch (Exception e) {
                Log.e(TAG, "Error writing to file " + filename, e);
                Toast toast = Toast.makeText(getApplicationContext(), "Error writing to file " + filename,
                        Toast.LENGTH_SHORT);
                toast.show();

                success = false;
            } finally {
                try {
                    if (os != null)
                        os.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing the file " + filename, e);
                    Toast toast = Toast.makeText(getApplicationContext(), "Error closing the file " + filename,
                            Toast.LENGTH_SHORT);
                    toast.show();

                    success = false;
                }
            }

            if (success) {
                Log.d("tracker", "success "+ EZdirectory + "tmp/" + filename);
                mView.findViewById(R.id.cancel_take).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.confirm_take).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.lapse_camera_takePictureButton).setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.change_camera).setVisibility(View.INVISIBLE);
                mView.findViewById(R.id.preview_image_view).setVisibility(View.VISIBLE);

                int height = getResources().getDisplayMetrics().heightPixels /8;
                int width = getResources().getDisplayMetrics().widthPixels /8;
                ((ImageView) mView.findViewById(R.id.preview_image_view))
                        .setImageBitmap(LapseGridActivity.
                        decodeSampledBitmapFromResource(EZdirectory + "tmp/" + filename,width, height));

                ((ImageView) mView.findViewById(R.id.preview_image_view))
                        .setScaleType(ImageView.ScaleType.FIT_XY);
                mView.findViewById(R.id.lapse_camera_surfaceView).setVisibility(View.INVISIBLE);

                if (!firstPic)
                    mView.findViewById(R.id.switch_overlay).setVisibility(View.VISIBLE);

                mOverlay.setAlpha(0.01f);
                alpha = false;

            } else {
                Log.d("tracker", "fail");
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        }
    };

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
Log.v("tracker", "fullscreen on create");
        super.onCreate(savedInstanceState);

        String strManufacturer = android.os.Build.MANUFACTURER;
        if(strManufacturer.equals("samsung"))
            samsung = true;
        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        setContentView(R.layout.activity_fullscreen_camera);
        mView = this.getWindow().getDecorView().findViewById(android.R.id.content);
        mView.findViewById(R.id.lapse_camera_surfaceView);
        mSurfaceView = (CameraSurfaceView) mView.findViewById(R.id.lapse_camera_surfaceView);
        mSurfaceView.mCamera = null;

        mView.findViewById(R.id.cancel_take).setVisibility(View.INVISIBLE);
        mView.findViewById(R.id.confirm_take).setVisibility(View.INVISIBLE);
        mView.findViewById(R.id.switch_overlay).setVisibility(View.INVISIBLE);

        ImageButton switchOverlayButton = (ImageButton) mView.findViewById(R.id.switch_overlay);
        switchOverlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alpha)
                    mOverlay.setAlpha(0.01f);
                else {
                    mOverlay.setAlpha(.5f);
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
                mOverlay.setAlpha(0.5f);
                alpha = true;
            }
        });

        ImageButton confirmTakeButton = (ImageButton) mView.findViewById(R.id.confirm_take);
        confirmTakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstPic) {
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

                                    int height = getResources().getDisplayMetrics().heightPixels /8;
                                    int width = getResources().getDisplayMetrics().widthPixels /8;
                                    mOverlay.setImageBitmap(LapseGridActivity.
                                                    decodeSampledBitmapFromResource(to.getAbsolutePath(),width, height));

                                    mOverlay.setScaleType(ImageView.ScaleType.FIT_XY);
                                    mOverlay.setAlpha(.5f);

                                    mView.findViewById(R.id.cancel_take).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.confirm_take).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.lapse_camera_takePictureButton).setVisibility(View.VISIBLE);
                                    mView.findViewById(R.id.switch_overlay).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.change_camera).setVisibility(View.VISIBLE);
                                    mView.findViewById(R.id.preview_image_view).setVisibility(View.INVISIBLE);
                                    mView.findViewById(R.id.lapse_camera_surfaceView).setVisibility(View.VISIBLE);

                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    Lapse currentLapse = LapseGallery.get(getApplicationContext()).getLapse(mLapseId);
                    String title = currentLapse.getTitle();
                    String number = currentLapse.getLatest().substring(currentLapse.getLatest().indexOf("-Photo") + 6, currentLapse.getLatest().indexOf(".jpg"));

                    File to = new File(EZdirectory + title + "/", title + "-Photo" + (Integer.parseInt(number) + 1) + ".jpg");
                    File from = new File(filePath.toString());
                    from.renameTo(to);
                    currentLapse.add(new Photo(to.getPath(), new Date()));
                    pictureTaken = true;

                    mOverlay.setAlpha(.5f);


                    int height = getResources().getDisplayMetrics().heightPixels /8;
                    int width = getResources().getDisplayMetrics().widthPixels /8;
                    mOverlay.setImageBitmap(LapseGridActivity.
                            decodeSampledBitmapFromResource(to.getAbsolutePath(),width, height));
                    mView.findViewById(R.id.cancel_take).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.confirm_take).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.lapse_camera_takePictureButton).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.switch_overlay).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.change_camera).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.preview_image_view).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.lapse_camera_surfaceView).setVisibility(View.VISIBLE);

                    mSurfaceView.mCamera.startPreview();
                    inPreview = true;
                }
            }
        });


        mOverlay = (ImageView) mView.findViewById(R.id.opaque_image_view);
        // Sets the overlay to the latest lapse
        if (getIntent().getExtras() != null &&
                getIntent().getExtras().containsKey(EXTRA_LAPSE_ID)) {
            firstPic = false;
            mLapseId = (UUID) getIntent().getExtras().getSerializable(EXTRA_LAPSE_ID);
            File imgFile = new File(LapseGallery.get(getApplicationContext()).getLapse(mLapseId)
                    .getLatest());
            if (imgFile.exists()) {


                int height = getResources().getDisplayMetrics().heightPixels /4;
                int width = getResources().getDisplayMetrics().widthPixels /4;
                mOverlay.setImageBitmap(LapseGridActivity.
                        decodeSampledBitmapFromResource(imgFile.getAbsolutePath(),width, height));
                mOverlay.setScaleType(ImageView.ScaleType.FIT_XY);

            }
            mOverlay.setAlpha(.5f);
        }

        ImageButton changeCamera = (ImageButton) mView.findViewById(R.id.change_camera);
        changeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inPreview) {
                    mSurfaceView.mCamera.stopPreview();
                    inPreview = false;
                }
                /* if you don't release the current camera before switching, you app will crash */
                mSurfaceView.mCamera.release();
                Log.v("tracker","camera change");
                //swap the id of the camera to be used
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                mSurfaceView.setCamera(Camera.open(currentCameraId));
                //Log.v("tracker", "camera just set");
                //Log.v("tracker", "before " + mSurfaceView.mPreviewSize.width + " " + mSurfaceView.mPreviewSize.height);

                mSurfaceView.myMeasure(1073742424, 1073742800);
                //Log.v("tracker", "after " + mSurfaceView.mPreviewSize.width + " " + mSurfaceView.mPreviewSize.height);
                mSurfaceView.surfaceChanged(mSurfaceView.getHolder(),4,0,0);
            }
        });

        ImageButton takePictureButton = (ImageButton) mView.findViewById(R.id.lapse_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSurfaceView.mCamera != null) {
                    mSurfaceView.mCamera.takePicture(mShutterCallback, null, mJpegCallback);
                }
            }
        });

        //Log.v("tracker", "3");
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
    public void onResume() {
        super.onResume();

        if (mSurfaceView.mCamera == null) {
            Log.v("tracker", "resume");
            /* If left during confirm/cancel phase */
            mSurfaceView.setCamera(Camera.open(currentCameraId));
            if(!inPreview) {
                Log.v("tracker", "start prev");
                mSurfaceView.surfaceChanged(mSurfaceView.getHolder(),4,0,0);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("tracker", "onPause");
        if (mSurfaceView.mCamera != null) {
            mSurfaceView.mCamera.release();
            mSurfaceView.mCamera = null;
            inPreview = false;
            Log.v("tracker", "end prev");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("tracker", "onDestroy");
        inPreview = true;
    }

    @Override
    public void onBackPressed() {
        if (pictureTaken) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_PASS, true);
            returnIntent.putExtra(EXTRA_LAPSE_ID, mLapseId);
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_PASS, false);
            setResult(Activity.RESULT_OK, returnIntent);
        }
        super.onBackPressed();
    }

    public byte[] flip(byte[] d) {
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

    public Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

}