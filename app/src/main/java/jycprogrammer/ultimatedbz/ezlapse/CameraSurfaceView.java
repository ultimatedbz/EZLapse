package jycprogrammer.ultimatedbz.ezlapse;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by jeffreychen on 6/27/15.
 */

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
public class CameraSurfaceView  extends SurfaceView implements SurfaceHolder.Callback{
    private final String TAG = "tracker";
    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;
    public Camera mCamera;
    private SurfaceHolder mHolder;


    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
            //for(Camera.Size str: mSupportedPreviewSizes)
            //    Log.v(TAG, str.width + "/" + str.height);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        Log.v("tracker", "onMeasure" + widthMeasureSpec + " " + heightMeasureSpec);
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        float ratio;
        if(mPreviewSize.height >= mPreviewSize.width)
            ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
        else
            ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

        // One of these methods should be used, second method squishes preview slightly
        setMeasuredDimension(width, (int) (width * ratio));

/*
        float camHeight = (int) (width * ratio);
        float newCamHeight;
        float newHeightRatio;

        if (camHeight < height) {
            newHeightRatio = (float) height / (float) camHeight;
            newCamHeight = (newHeightRatio * camHeight);
            //Log.e(TAG, camHeight + " " + height + " " + mPreviewSize.height + " " + newHeightRatio + " " + newCamHeight);
            setMeasuredDimension((int) (width * newHeightRatio), (int) newCamHeight);
            //Log.e(TAG, mPreviewSize.width + " | " + mPreviewSize.height + " | ratio - " + ratio + " | H_ratio - " + newHeightRatio + " | A_width - " + (width * newHeightRatio) + " | A_height - " + newCamHeight);
        } else {
            newCamHeight = camHeight;
            setMeasuredDimension(width, (int) newCamHeight);
            //Log.e(TAG, mPreviewSize.width + " | " + mPreviewSize.height + " | ratio - " + ratio + " | A_width - " + (width) + " | A_height - " + newCamHeight);
        }
*/
    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        //double targetRatio = (double) w / h;
        double targetRatio = (double) h / w;
        if (sizes == null)
            return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
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
        Log.v("tracker", "surface changed"+ format + " " + width + " " + height);
        if (mCamera == null)
            return;

            /* Geny motion front back both are 90
            *  Actual Android phones front = 270, back = 90*/

        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(1, camInfo);
        int cameraRotationOffset = camInfo.orientation;

        /* Need to find better way of fixing camera orientation */
        if (cameraRotationOffset == 270)
            mCamera.setDisplayOrientation(90);

        Camera.Parameters p = mCamera.getParameters();

        p.set("jpeg-quality", 100);

        p.setPictureFormat(PixelFormat.JPEG);

        if (cameraRotationOffset == 270 && FullscreenCamera.currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
            p.set("rotation", 270);
        else
            p.set("rotation", 90);


        Log.v("tracker", mPreviewSize.width + " " + mPreviewSize.height);
        p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        p.setPictureSize(mPreviewSize.width, mPreviewSize.height);
        mCamera.setParameters(p);

              /*In the future look at this for rotation
               http://stackoverflow.com/questions/6069122/camera-orientation-issue-in-android
              http://stackoverflow.com/questions/20064793/how-to-fix-camera-orientation
              http://stackoverflow.com/questions/11026615/captured-photo-orientation-is-changing-in-android/
              http://stackoverflow.com/questions/4645960/how-to-set-android-camera-orientation-properly  */

        try {
            mCamera.setPreviewDisplay(holder);
            Log.v("tracker", "start preview");
            mCamera.startPreview();
            Log.v("tracker", "preview started");
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


}


