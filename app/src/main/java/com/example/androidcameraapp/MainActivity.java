package com.example.androidcameraapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Button btn_record_video;
    Button btn_take_photo;
    private boolean isRecording = false;
    private static final String TAG = "Recorder";

    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;

    private static Context appContext;
    private MediaScannerConnection mediaScannerConnection;
    String fileName;

    //ProgressBar progressBar;
    LinearLayout progressLayout;
    //TextView textView;

    MediaRecorder mediaRecorder;

    Activity context;

    public ImageView imgShowGallery;//, img_display_image;
    private Camera Cam;
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ImageButton btn_take_photo = (ImageButton) findViewById(R.id.take_photo);

        //context = this;

        //Button btn_take_photo = (Button) findViewById(R.id.take_photo);
        btn_take_photo = (Button) findViewById(R.id.take_photo);
        final Button btn_upload_pic_to = (Button) findViewById(R.id.btn_upload_picture_to);
        btn_upload_pic_to.setVisibility(View.GONE);

        btn_record_video = (Button) findViewById(R.id.reoord_video);

       // img_display_image = (ImageView) findViewById(R.id.display_image);
       // img_display_image.setVisibility(View.GONE);

        imgShowGallery = (ImageView) findViewById(R.id.imageView);
        imgShowGallery.setVisibility(View.GONE);

        //progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressLayout = (LinearLayout) findViewById(R.id.progress_layout);
        //textView = (TextView) findViewById(R.id.txtLoading);
        //progressBar.setVisibility(View.GONE);
        //textView.setVisibility(View.GONE);

       /* imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takeCameraImage();
            }
        }); */

        // Checking camera availability
        if(!isDeviceSupportCamera()){
            Toast.makeText(getApplicationContext(), "Sorry! Your device doesn't support camera", Toast.LENGTH_LONG).show();
            // will close the app if the device doesn't have camera
            finish();
        }

        // COde:
        // Create an instance of Camera
        Cam = getCameraInstance();

        //
        cameraPreview = new CameraPreview(this, Cam);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
        preview.setKeepScreenOn(true);

//        context = this;
//        setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, Cam);

        btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//  <<<<              Cam.takePicture(null, null, picCallback);
                try {
                    takeFocusedPicture();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                imgShowGallery.setVisibility(View.VISIBLE);
                btn_upload_pic_to.setVisibility(View.VISIBLE);

                //progressBar.setVisibility(View.VISIBLE);
                progressLayout.setVisibility(View.VISIBLE);
                //textView.setVisibility(View.VISIBLE);

                //showGallery();

            }
        });

        imgShowGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showGallery();
                showGallery1();
            }
        });

        btn_record_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording) {
                    // stop recording and release camera
                    mediaRecorder.stop();
                    releaseMediaRecorder();
                    Cam.lock();

                    //
                    setVideoCaptureButtonText("Record Video");
                    isRecording = false;
                    // release Camera
                } else {
                    // initialize video camera
                    if(prepareVideoRecorder()) {
                        //
                        //
                        mediaRecorder.start();

                        //
                        setVideoCaptureButtonText("Stop");
                        isRecording = true;
                    } else {
                        //
                        releaseMediaRecorder();
                        //
                    }
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(Cam != null) {
            releaseCamera(); //
        }
        //releaseMediaRecorder(); //
//        releaseCamera(); //
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Cam == null) {
            Cam = Camera.open();
            Cam.startPreview();
            Cam.setErrorCallback(new Camera.ErrorCallback() {
                @Override
                public void onError(int error, Camera camera) {
                    Cam.release();
                    Cam = Camera.open();
                    Log.d("Camera died", "error camera");
                }
            });
        }
        if(Cam != null) {
            if(Build.VERSION.SDK_INT >= 14) {
                setCameraDisplayOrientation(context, Camera.CameraInfo.CAMERA_FACING_BACK, Cam);
                cameraPreview.setCamera(Cam);
            }
        }
    }

    private void releaseCamera(){
//        if(Cam != null){
        Cam.stopPreview();
        cameraPreview.setCamera(null);
        Cam.release();
        Cam = null;
//        }
    }

    private void setVideoCaptureButtonText(String title) {
        btn_record_video.setText(title);
    }

    /* Show image from gallery */
     void showGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture From:"), 100);
    }

    void showGallery1(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    private void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                //Toast.makeText(getApplicationContext(), "rotation in 0 deg: " + rotation, Toast.LENGTH_LONG).show();
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                //Toast.makeText(getApplicationContext(), "rotation in 90 deg: " + rotation, Toast.LENGTH_LONG).show();
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                //Toast.makeText(getApplicationContext(), "rotation in 180 deg: " + rotation, Toast.LENGTH_LONG).show();
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                //Toast.makeText(getApplicationContext(), "rotation in 270 deg: " + rotation, Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            //When an image is picked
            if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data){
                // Get the Image from data
                /*Uri selectedImage = data.getData();
                String[] filePathColumn = { Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                //Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();*/

               // img_display_image.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));

            } else if(resultCode == RESULT_CANCELED) {
                //User cancelled image capture
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

        /*if(resultCode == RESULT_OK) {
            // get the url from data
            //Uri SelectedImageUri = data.getData();
            Toast.makeText(getApplicationContext(), "You selected an image.", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "You cancelled image selection.", Toast.LENGTH_SHORT).show();
        }*/
    }

    private Camera.PictureCallback picCallback = new Camera.PictureCallback() {
        public static final String TAG = "ERROR Creating Media";

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if(pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            Toast.makeText(getApplicationContext(), "Picture Saved in Camera folder", Toast.LENGTH_LONG).show();

            //imgShowGallery.setVisibility(View.VISIBLE);
            try{
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            }catch (FileNotFoundException e){
                Log.d(TAG, "File Not found: " + e.getMessage());
            }catch (IOException e){
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }


            //progressBar.setVisibility(View.GONE);
            //progressLayout.setVisibility(View.GONE);
            //textView.setVisibility(View.GONE);
        }
    };

    /* private void rotateImage(Bitmap bitmap) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(String.valueOf(getOutputMediaFile(1)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                //rotateImage(bitmap, 90);
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            //case ExifInterface.ORIENTATION_ROTATE_270:
        }
        Bitmap rotatatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }*/

    public void takeFocusedPicture(){
        Cam.autoFocus(camAutoFocusCallback);
    }

    Camera.AutoFocusCallback camAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camAutoFocusCamera) {
            try {
                camAutoFocusCamera.takePicture(camShutterCallback, null, picCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Camera.ShutterCallback camShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // TODO code
        }
    };

    private boolean prepareVideoRecorder(){
        Cam = getCameraInstance();
        mediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        Cam.unlock();
        mediaRecorder.setCamera(Cam);

        // Step 2: set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e){
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /* */
    //private static Uri getOutputmediaFileUri(int type){
    private Uri getOutputmediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /* */
    //private static File getOutputMediaFile(int type){
    private File getOutputMediaFile(int type){
        //
        //

        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        //File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),"DCIM/Camera");
        //File mediaStorageDir = new File("/storage/emulated/0/DCIM/Camera");
        //File mediaStorageDir = new File("/sdcard/DCIM/Camera");
          //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Camera");
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        //
        // Device storage/emulated/0/DCIM/Camera  // Device storage/emulated/0/pictures/  // Device sdcard/emulated/0/

        //
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");


            // Refresh media Scanner:
            MediaScannerConnection.scanFile(this, new String[]{mediaFile.toString()}, new String[]{"image/jpg"}, null);
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");

            //Refresh media Scanner:
            MediaScannerConnection.scanFile(this, new String[]{mediaFile.toString()}, new String[]{"video/mp4"}, null);
        } else {
            return null;
        }

        return mediaFile;
    }


    private void releaseMediaRecorder(){
        if(mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            Cam.lock();
        }
    }


    /*
     * Check if device has camera hardware or not
     */
    private boolean isDeviceSupportCamera(){
        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Toast.makeText(getApplicationContext(),"Your device supports camera", Toast.LENGTH_LONG).show();
            //this device has a camera
            return true;
        }else {
            // no camera on this device
            return false;
        }
    }

    /* A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera cam = null;
        try{
            cam = Camera.open(); //attempt to get a Camera instance
        }catch (Exception e){
            // camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return cam;
    }

    /* Displays Menu in Overflow icon*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}