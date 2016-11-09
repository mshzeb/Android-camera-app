package com.example.androidcameraapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by paklap on 23-Oct-16.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Camera Preview Error";
    private SurfaceHolder mHolder;
    private Camera Cam;

    List<Camera.Size> supportedPreviewSizes;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        Cam = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed
        mHolder = getHolder();
        mHolder.addCallback(this);
        //deprecated settings, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera setCamera) {
        Cam = setCamera;
        if(Cam != null) {
            supportedPreviewSizes = Cam.getParameters().getSupportedPictureSizes();
            requestLayout();

            Camera.Parameters params = Cam.getParameters();

            List<String> focusModes = params.getSupportedFocusModes();
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                Cam.setParameters(params);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder){
        // The surface has been created, now tell the camera where to draw the preview.
        try{
            //Cam.startPreview();
            Cam.setPreviewDisplay(holder);
            Cam.startPreview();
        }catch (IOException e){
            Log.d(TAG, "ERROR setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder){
        // release camera preview
        //Cam.stopPreview();
        if(Cam != null){
//            Cam.stopPreview();
            Cam.release();
            Cam = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h){
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or formatting it.

        if(mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try{
            Cam.stopPreview();
        }catch (Exception e){
            // ignore:
            e.printStackTrace();
        }

        //
        //

        //
        try{
            //Cam.startPreview();
            Cam.setPreviewDisplay(mHolder);
            Cam.startPreview();
        } catch (Exception e){
            Log.d(TAG, "ERROR starting camera preview: " + e.getMessage());
        }
    }

    /* private void setCameraDisplayOrientation(Camera.CameraInfo info, Camera camera) {
        //Camera.CameraInfo info = new Camera.CameraInfo();
        //Camera.getCameraInfo(cameraId, info);
        int rotation = context.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { //back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }*/

    /*protected void onResume()
    {
        //super.onResume();
        Cam = Camera.open();
        Cam.startPreview();
    }*/
    // private Camera mCamera; // Src: https://developer.android.com/guide/topics/media/camera.html
}