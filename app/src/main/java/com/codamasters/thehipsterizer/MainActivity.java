package com.codamasters.thehipsterizer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuView;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.Button;

import boofcv.abst.filter.derivative.ImageGradient;
import boofcv.android.ConvertBitmap;
import boofcv.android.ConvertNV21;
import boofcv.android.VisualizeImageData;
import boofcv.factory.filter.derivative.FactoryDerivative;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;


public class MainActivity extends ActionBarActivity implements Camera.PreviewCallback {

    private PictureCallback mPicture;
    private ImageButton capture, filters;
    private ScrollView filtersScroll;
    private HorizontalScrollView horizontalFiltersScroll;
    private Button noneFilter, sepiaFilter, aquaFilter, blackboardFilter, whiteboardFilter,
            posterizeFilter, negativeFilter, monoFilter, solarizeFilter;
    private ImageView capturedImage;
    private Uri fileUri;
    private String filePath;
    private Context myContext;
    private LinearLayout cameraPreview, menuFiltersLayout;
    private RelativeLayout buttonsLayout;
    private boolean cameraFront;
    private File pictureFile;
    private int sViewX, sViewY;
    private boolean screenState;
    private int cameraId = -1;
    private MenuItem flash;
    private Drawable autoflashicon, flashicon, noflashicon;
    private final int FLASH_AUTO = 0;
    private final int FLASH_ON = 1;
    private final int FLASH_OFF = 2;
    private int flashState = FLASH_OFF;

    // VARIABLES MÁGICAS

    // camera and display objects
    private Camera mCamera;
    private Visualization mDraw;
    private CameraPreview mPreview;

    // computes the image gradient
    private ImageGradient<ImageUInt8,ImageSInt16> gradient = FactoryDerivative.three(ImageUInt8.class, ImageSInt16.class);

    // Two images are needed to store the converted preview image to prevent a thread conflict from occurring
    private ImageUInt8 gray1,gray2;
    private ImageSInt16 derivX,derivY;

    // Android image data used for displaying the results
    private Bitmap output;
    // temporary storage that's needed when converting from BoofCV to Android image data types
    private byte[] storage;

    // Thread where image data is processed
    private ThreadProcess thread;

    // Object used for synchronizing gray images
    private final Object lockGray = new Object();
    // Object used for synchronizing output image
    private final Object lockOutput = new Object();




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.hide();
        initialize();
    }

    // Create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        switch (flashState){
            case FLASH_OFF:
                menu.findItem(R.id.action_flash).setIcon(noflashicon);
                return true;
            case FLASH_ON:
                menu.findItem(R.id.action_flash).setIcon(flashicon);
                return true;
            case FLASH_AUTO:
                menu.findItem(R.id.action_flash).setIcon(autoflashicon);
                return true;
            default:
                menu.findItem(R.id.action_flash).setIcon(noflashicon);
        }
        return true;
    }

    // Action Bar listener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Camera.Parameters p = mCamera.getParameters();
        ActionBar actionBar = getSupportActionBar();
        switch (item.getItemId()) {
            case R.id.action_switch:
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {

                    releaseCamera();
                    chooseCamera();

                    mCamera.stopPreview();


                    WindowManager wm = (WindowManager) myContext.getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();

                    if(display.getRotation() == Surface.ROTATION_0)
                    {
                        mCamera.setDisplayOrientation(90);
                    }

                    if(display.getRotation() == Surface.ROTATION_90)
                    {

                    }

                    if(display.getRotation() == Surface.ROTATION_180)
                    {

                    }

                    if(display.getRotation() == Surface.ROTATION_270)
                    {

                        mCamera.setDisplayOrientation(180);
                    }

                    mPreview.refreshCamera(mCamera);

                    mCamera.startPreview();


                } else {
                    Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                    toast.show();
                }
                return true;

            case R.id.action_flash:

                if (item.getIcon() == autoflashicon) {
                    mPreview.setCurrentFlash(Camera.Parameters.FLASH_MODE_ON);
                    mPreview.refreshCamera(mCamera);
                    flashState = FLASH_ON;
                }
                else if (item.getIcon() == flashicon) {
                    mPreview.setCurrentFlash(Camera.Parameters.FLASH_MODE_OFF);
                    mPreview.refreshCamera(mCamera);
                    flashState = FLASH_OFF;

                }
                else if(item.getIcon() == noflashicon) {
                    mPreview.setCurrentFlash(Camera.Parameters.FLASH_MODE_AUTO);
                    mPreview.refreshCamera(mCamera);
                    flashState = FLASH_AUTO;
                }
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int findFrontFacingCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }


    public void initialize() {


        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);
        mDraw = new Visualization(this);
        cameraPreview.addView(mDraw);

        capture = (ImageButton) findViewById(R.id.button_capture);
        capture.setOnClickListener(captureListener);

        noneFilter = (Button) findViewById(R.id.button_noneFilter);
        noneFilter.setOnClickListener(noneFilterListener);

        sepiaFilter = (Button) findViewById(R.id.button_sepiaFilter);
        sepiaFilter.setOnClickListener(sepiaFilterListener);

        aquaFilter = (Button) findViewById(R.id.button_aquaFilter);
        aquaFilter.setOnClickListener(aquaFilterListener);

        blackboardFilter = (Button) findViewById(R.id.button_blackboardFilter);
        blackboardFilter.setOnClickListener(blackboardFilterListener);

        whiteboardFilter = (Button) findViewById(R.id.button_whiteboardFilter);
        whiteboardFilter.setOnClickListener(whiteboardFilterListener);

        posterizeFilter = (Button) findViewById(R.id.button_posterizeFilter);
        posterizeFilter.setOnClickListener(posterizeFilterListener);

        negativeFilter = (Button) findViewById(R.id.button_negativeFilter);
        negativeFilter.setOnClickListener(negativeFilterListener);

        monoFilter = (Button) findViewById(R.id.button_monoFilter);
        monoFilter.setOnClickListener(monoFilterListener);

        solarizeFilter = (Button) findViewById(R.id.button_solarizeFilter);
        solarizeFilter.setOnClickListener(solarizeFilterListener);

        filters = (ImageButton) findViewById(R.id.button_filters);
        filters.setOnClickListener(filtersListener);

        capturedImage = (ImageView) findViewById(R.id.capturedImageView);
        capturedImage.setOnClickListener(capturedImageListener);

        filtersScroll = (ScrollView) findViewById(R.id.filtersScrollView);
        horizontalFiltersScroll = (HorizontalScrollView) findViewById(R.id.horizontalFiltersScrollView);
        menuFiltersLayout = (LinearLayout) findViewById(R.id.menuFiltersLayout);
        buttonsLayout = (RelativeLayout) findViewById(R.id.buttonsLayout);


        menuFiltersLayout.setVisibility(View.GONE);

        autoflashicon = getResources().getDrawable(R.drawable.autoflash);
        flashicon = getResources().getDrawable(R.drawable.flash);
        noflashicon = getResources().getDrawable(R.drawable.noflash);


    }

    OnClickListener noneFilterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.setCurrentFilter(Camera.Parameters.EFFECT_NONE);
            mPreview.refreshCamera(mCamera);
            menuFiltersLayout.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    };

    OnClickListener sepiaFilterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.setCurrentFilter(Camera.Parameters.EFFECT_SEPIA);
            mPreview.refreshCamera(mCamera);
            menuFiltersLayout.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    };

    OnClickListener aquaFilterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.setCurrentFilter(Camera.Parameters.EFFECT_AQUA);
            mPreview.refreshCamera(mCamera);
            menuFiltersLayout.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    };

    OnClickListener blackboardFilterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.setCurrentFilter(Camera.Parameters.EFFECT_BLACKBOARD);
            mPreview.refreshCamera(mCamera);
            menuFiltersLayout.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    };

    OnClickListener whiteboardFilterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.setCurrentFilter(Camera.Parameters.EFFECT_WHITEBOARD);
            mPreview.refreshCamera(mCamera);
            menuFiltersLayout.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    };

    OnClickListener posterizeFilterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.setCurrentFilter(Camera.Parameters.EFFECT_POSTERIZE);
            mPreview.refreshCamera(mCamera);
            menuFiltersLayout.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    };

    OnClickListener negativeFilterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.setCurrentFilter(Camera.Parameters.EFFECT_NEGATIVE);
            mPreview.refreshCamera(mCamera);
            menuFiltersLayout.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    };

    OnClickListener monoFilterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.setCurrentFilter(Camera.Parameters.EFFECT_MONO);
            mPreview.refreshCamera(mCamera);
            menuFiltersLayout.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    };

    OnClickListener solarizeFilterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.setCurrentFilter(Camera.Parameters.EFFECT_SOLARIZE);
            mPreview.refreshCamera(mCamera);
            menuFiltersLayout.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }
    };

    OnClickListener filtersListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            buttonsLayout.setVisibility(View.GONE);
            menuFiltersLayout.setVisibility(View.VISIBLE);
        }

    };

    OnClickListener capturedImageListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(pictureFile);
                File file = new File(uri.getPath());
                intent.setDataAndType(Uri.fromFile(file), "image/*");
                startActivity(intent);
            } catch(Exception e) {
            }
        }
    };


    public void chooseCamera() {
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {

                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }


    private boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private PictureCallback getPictureCallback() {
        PictureCallback picture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                pictureFile = getOutputMediaFile();

                if (pictureFile == null) {
                    return;
                }
                try {

                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);

                    fos.close();

                    try {
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri uri = Uri.fromFile(pictureFile);
                        mediaScanIntent.setData(uri);
                        sendBroadcast(mediaScanIntent);
                    } catch(Exception e) {
                    }


                    showImage();

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }

    private void loadMiniImage(){
        GetImageThumbnail getImageThumbnail = new GetImageThumbnail();
        Bitmap bitmap = null;
        try {
            bitmap = getImageThumbnail.getThumbnail(fileUri, this );

            int orientation = getResources().getConfiguration().orientation;

            if(orientation == Configuration.ORIENTATION_PORTRAIT ) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Matrix matrix = new Matrix();


                if (!cameraFront) {
                    matrix.postRotate(90);
                } else {
                    matrix.postRotate(-90);
                }

                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                bitmap = rotatedBitmap;

                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            }


        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        capturedImage.setImageBitmap(bitmap);
    }

    public void showImage(){
        fileUri = Uri.fromFile(pictureFile);
        filePath = fileUri.getPath();

        loadMiniImage();
    }

    OnClickListener captureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(null, null, mPicture);
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() , "DCIM/Camera");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        //---save whatever you need to persist—

        //outState.putInt("sViewX",filtersScroll.getScrollX());
        //outState.putInt("sViewY",filtersScroll.getScrollY());
        outState.putInt("cameraId", cameraId);
        outState.putBoolean("cameraFront", cameraFront);
        outState.putString("filePath", filePath);

        super.onSaveInstanceState(outState);

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //sViewX = savedInstanceState.getInt("sViewX");
        //sViewY = savedInstanceState.getInt("sViewY");
        cameraId = savedInstanceState.getInt("cameraId");
        cameraFront = savedInstanceState.getBoolean("cameraFront");
        filePath = savedInstanceState.getString("filePath");

        //filtersScroll.scrollTo(sViewX, sViewY);
        releaseCamera();
        mCamera = Camera.open(cameraId);
        mPicture = getPictureCallback();
        mPreview.refreshCamera(mCamera);

        if (filePath != null) {
            pictureFile = new File(filePath);
            fileUri = Uri.fromFile(pictureFile);
            GetImageThumbnail getImageThumbnail = new GetImageThumbnail();
            Bitmap bitmap = null;
            try {
                bitmap = getImageThumbnail.getThumbnail(fileUri, this);

            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            capturedImage.setImageBitmap(bitmap);
        }
    }

    // CODA MÁGICA

    @Override
    protected void onResume() {
        super.onResume();
        setUpAndConfigureCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop the camera preview and all processing
        if (mCamera != null){
            mPreview.setCamera(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;

            thread.stopThread();
            thread = null;
        }
    }

    /**
     * Sets up the camera if it is not already setup.
     */
    private void setUpAndConfigureCamera() {
        // Open and configure the camera
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
            }
            mCamera = Camera.open(findBackFacingCamera());
            mPicture = getPictureCallback();

            Camera.Parameters param = mCamera.getParameters();

            // Select the preview size closest to 320x240
            // Smaller images are recommended because some computer vision operations are very expensive
            List<Camera.Size> sizes = param.getSupportedPreviewSizes();
            Camera.Size s = sizes.get(closest(sizes,320,240));
            param.setPreviewSize(s.width,s.height);
            mCamera.setParameters(param);

            // declare image data
            gray1 = new ImageUInt8(s.width,s.height);
            gray2 = new ImageUInt8(s.width,s.height);
            derivX = new ImageSInt16(s.width,s.height);
            derivY = new ImageSInt16(s.width,s.height);
            output = Bitmap.createBitmap(s.width,s.height,Bitmap.Config.ARGB_8888 );
            storage = ConvertBitmap.declareStorage(output, storage);

            // start image processing thread
            thread = new ThreadProcess();
            thread.start();

            // Start the video feed by passing it to mPreview
            mPreview.refreshCamera(mCamera);
        }


    }

    /**
     * Goes through the size list and selects the one which is the closest specified size
     */
    public static int closest( List<Camera.Size> sizes , int width , int height ) {
        int best = -1;
        int bestScore = Integer.MAX_VALUE;

        for( int i = 0; i < sizes.size(); i++ ) {
            Camera.Size s = sizes.get(i);

            int dx = s.width-width;
            int dy = s.height-height;

            int score = dx*dx + dy*dy;
            if( score < bestScore ) {
                best = i;
                bestScore = score;
            }
        }

        return best;
    }

    /**
     * Called each time a new image arrives in the data stream.
     */
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

        // convert from NV21 format into gray scale
        synchronized (lockGray) {
            ConvertNV21.nv21ToGray(bytes, gray1.width, gray1.height, gray1);
        }

        // Can only do trivial amounts of image processing inside this function or else bad stuff happens.
        // To work around this issue most of the processing has been pushed onto a thread and the call below
        // tells the thread to wake up and process another image
        thread.interrupt();
    }

    /**
     * Draws on top of the video stream for visualizing computer vision results
     */
    private class Visualization extends SurfaceView {

        Activity activity;

        public Visualization(Activity context ) {
            super(context);
            this.activity = context;

            // This call is necessary, or else the
            // draw method will not be called.
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas){

            synchronized ( lockOutput ) {
                int w = canvas.getWidth();
                int h = canvas.getHeight();

                // fill the window and center it
                double scaleX = w/(double)output.getWidth();
                double scaleY = h/(double)output.getHeight();

                double scale = Math.min(scaleX,scaleY);
                double tranX = (w-scale*output.getWidth())/2;
                double tranY = (h-scale*output.getHeight())/2;

                canvas.translate((float)tranX,(float)tranY);
                canvas.scale((float)scale,(float)scale);

                // draw the image
                canvas.drawBitmap(output,0,0,null);
            }
        }
    }

    /**
     * External thread used to do more time consuming image processing
     */
    private class ThreadProcess extends Thread {

        // true if a request has been made to stop the thread
        volatile boolean stopRequested = false;
        // true if the thread is running and can process more data
        volatile boolean running = true;

        /**
         * Blocks until the thread has stopped
         */
        public void stopThread() {
            stopRequested = true;
            while( running ) {
                thread.interrupt();
                Thread.yield();
            }
        }

        @Override
        public void run() {

            while( !stopRequested ) {

                // Sleep until it has been told to wake up
                synchronized ( Thread.currentThread() ) {
                    try {
                        wait();
                    } catch (InterruptedException ignored) {}
                }

                // process the most recently converted image by swapping image buffered
                synchronized (lockGray) {
                    ImageUInt8 tmp = gray1;
                    gray1 = gray2;
                    gray2 = tmp;
                }

                // process the image and compute its gradient
                gradient.process(gray2,derivX,derivY);

                // render the output in a synthetic color image
                synchronized ( lockOutput ) {
                    VisualizeImageData.colorizeGradient(derivX, derivY, -1, output, storage);
                }
                mDraw.postInvalidate();
            }
            running = false;
        }
    }

}
