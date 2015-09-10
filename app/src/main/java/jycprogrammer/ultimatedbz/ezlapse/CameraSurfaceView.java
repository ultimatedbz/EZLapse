package jycprogrammer.ultimatedbz.ezlapse;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by jeffreychen on 6/27/15.
 */

//TODO TAP TO FOCUS:
// https://gist.github.com/mjurkus/21f7b9aa9b27a7661184
//http://stackoverflow.com/questions/18460647/android-setfocusarea-and-auto-focus
//http://stackoverflow.com/questions/17993751/whats-the-correct-way-to-implement-tap-to-focus-for-camera


/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
public class CameraSurfaceView  extends SurfaceView implements SurfaceHolder.Callback/*, Camera.AutoFocusCallback*/{
    private final String TAG = "tracker";
    Camera.Size mPreviewSize;
    Camera.Size mPictureSize;
    List<Camera.Size> mSupportedPreviewSizes;
    List<Camera.Size> mSupportedPictureSizes;
    public Camera mCamera;
    private SurfaceHolder mHolder;
    int suggestedMinWidth, suggestedMinHeight;

    private int focusAreaSize;
    private boolean previewRunning, cameraReleased, focusAreaSupported, meteringAreaSupported;
    private Matrix matrix;

    @TargetApi(14)
    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        /*
        focusAreaSize = (int) (96 * getResources().getDisplayMetrics().density + 0.5f);
        matrix = new Matrix();
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mCamera != null) {
                    Camera camera = mCamera;
                    camera.cancelAutoFocus();
                    Log.v("tracker", event.getX() + " " + event.getY());
                    Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);

                    Camera.Parameters parameters = camera.getParameters();
                    if (parameters.getFocusMode() != Camera.Parameters.FOCUS_MODE_AUTO) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    }
                    if (parameters.getMaxNumFocusAreas() > 0) {
                        List<Camera.Area> mylist = new ArrayList<Camera.Area>();
                        mylist.add(new Camera.Area(focusRect, 1000));
                        parameters.setFocusAreas(mylist);
                    }

                    try {
                        camera.cancelAutoFocus();
                        camera.setParameters(parameters);
                        camera.startPreview();
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                if (camera.getParameters().getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                                    Camera.Parameters parameters = camera.getParameters();
                                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                                    if (parameters.getMaxNumFocusAreas() > 0) {
                                        parameters.setFocusAreas(null);
                                    }
                                    camera.setParameters(parameters);
                                    camera.startPreview();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        */
    }

    public CameraSurfaceView(Context context) {
        super(context);
    }


    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

            Log.v(TAG, "preview");
            for(Camera.Size str: mSupportedPreviewSizes)
                Log.v(TAG, str.width + "/" + str.height);

            mSupportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();

            Log.v(TAG, "picture: ");
            for(Camera.Size str: mSupportedPictureSizes)
                Log.v(TAG, str.width + "/" + str.height);
/*
            if(mPreviewSize != null) {
                Log.v("tracker", "before " + mPreviewSize.width + " " + mPreviewSize.height);
                onMeasure(99999, 99999);
                Log.v("tracker", "after " + mPreviewSize.width + " " + mPreviewSize.height);
            }
            */
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.

        Log.v("tracker", "onMeasure: " + widthMeasureSpec + " " + heightMeasureSpec);

        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        suggestedMinHeight = height;
        suggestedMinWidth = width;
        Log.v(TAG, "width: " + width + " height: " + height);
        if (mSupportedPreviewSizes != null) {
            //mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, height, width);
            mPictureSize = getOptimalPreviewSize(mSupportedPictureSizes, height, width);
        }
        Log.v(TAG, mPreviewSize.width + " " + mPreviewSize.height);
        float ratio;
        if(mPreviewSize.height >= mPreviewSize.width)
            ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
        else
            ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

        // One of these methods should be used, second method squishes preview slightly
        setMeasuredDimension(width, (int) (width * ratio));
    }

    protected void myMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.

        Log.v("tracker", "myMeasure: " + widthMeasureSpec + " " + heightMeasureSpec);
        final int width = suggestedMinWidth;
        final int height = suggestedMinHeight;
        Log.v(TAG, "width: " + width + " height: " + height);
        if (mSupportedPreviewSizes != null) {
            //mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height); // 1920/1080
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, height, width); //1080 1920
            mPictureSize = getOptimalPreviewSize(mSupportedPictureSizes, height, width);
        }

        float ratio;
        if(mPreviewSize.height >= mPreviewSize.width)
            ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
        else
            ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

        Log.v(TAG, mPreviewSize.width + " " + mPreviewSize.height);
        // One of these methods should be used, second method squishes preview slightly
        setMeasuredDimension(width, (int) (width * ratio));
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        //double targetRatio = (double) w / h;
        double targetRatio = (double) h / w; //1080/1920
        if (sizes == null)
            return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h; //1080
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            //double ratio = (double) size.width / size.height;
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /* SurfaceHolder Callback Stuff */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("tracker", "surfacecreated");
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "Error setting up preview display", exception);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v("tracker", "surface changed "+ format + " " + width + " " + height);
        if (mCamera == null)
            return;

            /* Geny motion front back both are 90
            *  Actual Android phones front = 270, back = 90*/

        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(1, camInfo);
        int cameraRotationOffset = camInfo.orientation;

        /* Need to find better way of fixing camera orientation */
        if (cameraRotationOffset == 270)
            mCamera.setDisplayOrientation(90); // rotates clockwise 90 degrees

        Camera.Parameters p = mCamera.getParameters();
/*  okay */
        p.set("jpeg-quality", 100);
        // checks focus mode with getSupportedFocusModes() to see if the camera supports focusmodecontinuouspicture
        List<String> l = p.getSupportedFocusModes();

        if(l.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        Log.v("tracker", "camera rotation: "+cameraRotationOffset);
        p.setPictureFormat(PixelFormat.JPEG);
/* okay */


        if (cameraRotationOffset == 270 && FullscreenCamera.currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            p.set("rotation", 270);
            /* default is left of screen -> top of picture, right of screen -> bottom of picture .
                changing to 90 rotates picture 90 degrees counter clockwise so it becomes upside down.
                changing to 270 rotates 270 degrees cc, so it becomes right side up.

                HOWEVER, this is only for HTC. For samsung, front camera starts off as  left top, right bottom, but p.set("rotation", x) doesn't do anything
                I will need to have a samsung flag, and if it is samsung, rotate the front regardless because exif of front is always 0*/
            Log.v(TAG,"ran rotation 270");
        }
        else
            p.set("rotation", 90);



        p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        p.setPictureSize(mPictureSize.width, mPictureSize.height);
        Context context = getContext();
        CharSequence text = "width: " + mPictureSize.width + " height: " + mPictureSize.height;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        //toast.show();

        mCamera.setParameters(p);

              /*In the future look at this for rotation
               http://stackoverflow.com/questions/6069122/camera-orientation-issue-in-android
              http://stackoverflow.com/questions/20064793/how-to-fix-camera-orientation
              http://stackoverflow.com/questions/11026615/captured-photo-orientation-is-changing-in-android/
              http://stackoverflow.com/questions/4645960/how-to-set-android-camera-orientation-properly  */

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            FullscreenCamera.inPreview = true;
        } catch (Exception e) {
            Log.e(TAG, "Could not start preview", e);
            mCamera.release();
           mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            FullscreenCamera.inPreview = false;
        }
    }


    /**
     * On each tap event we will calculate focus area and metering area.
     * <p>
     * Metering area is slightly larger as it should contain more info for exposure calculation.
     * As it is very easy to over/under expose
     *
    @TargetApi(14)
    protected void focusOnTouch(MotionEvent event) {
        if (mCamera != null) {
            //cancel previous actions
            mCamera.cancelAutoFocus();
            Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
            Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f);

            Camera.Parameters parameters = null;
            try {
                parameters = mCamera.getParameters();
            } catch (Exception e) {
                Log.e("tracker",""+e);
            }

            // check if parameters are set (handle RuntimeException: getParameters failed (empty parameters))
            if (parameters != null) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                ArrayList<Camera.Area> a = new ArrayList<>();
                a.add( new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(a);

                if (meteringAreaSupported) {
                    ArrayList<Camera.Area> b = new ArrayList<>();
                    b.add( new Camera.Area(meteringRect, 1000));
                    parameters.setFocusAreas(b);
                    parameters.setMeteringAreas(b);
                }

                try {
                    mCamera.setParameters(parameters);
                    mCamera.autoFocus(this);
                } catch (Exception e) {
                    Log.e("tracker",""+e);
                }
            }
        }
    }

    /**
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000.
     * <p>
     * Rotate, scale and translate touch rectangle using matrix configured in
     * {@link SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)}
     *
    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, this.getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, this.getHeight() - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        Log.v("tracker", "area: " + areaSize + "left: " + left + "top: " + top);
        matrix.mapRect(rectF);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    @TargetApi(14)
    public void resumeContinuousAutofocus() {
        if (mCamera != null && focusAreaSupported) {
            mCamera.cancelAutoFocus();

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusAreas(null);

            List<String> supportedFocusModes = parameters.getSupportedFocusModes();


            String focusMode = null;
            if (supportedFocusModes.contains(parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                focusMode = parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
            } else if (supportedFocusModes.contains(parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                focusMode = parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
            }

            if (focusMode != null) {
                parameters.setFocusMode(focusMode);
                mCamera.setParameters(parameters);
            }
        }
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    @Override
    public void onAutoFocus(boolean focused, Camera camera) {

    }


*/

}


