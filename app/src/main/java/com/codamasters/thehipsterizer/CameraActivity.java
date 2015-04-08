package com.codamasters.thehipsterizer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHazeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;


public class CameraActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private PictureCallback mPicture;
    private ImageButton capture;
    private LinearLayout filters;
    private ScrollView filtersScroll;
    private HorizontalScrollView horizontalFiltersScroll;
    private LinearLayout noneFilter,monoFilter,  sepiaFilter, aquaFilter, blackboardFilter, whiteboardFilter,
            posterizeFilter, negativeFilter, solarizeFilter;
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
    private GPUImageFilter actualFilter;
    private String currentFilter = "";
    private GPUImageView view;
    private Matrix matrix;
    private Bitmap auxImage;
    private ProgressBar progBar;
    private Handler mHandler;
    private int _cameraId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        myContext = this;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.hide();
        //RelativeLayout buttonsLayout = (RelativeLayout)findViewById(R.id.buttonsLayout);
        //buttonsLayout.bringToFront();
        initialize();
        mPreview.setWillNotDraw(false);
        mHandler = new Handler(Looper.getMainLooper());


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
        switch (item.getItemId()) {
            case R.id.action_switch:
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {

                    try {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                releaseCamera();
                                chooseCamera();
                                mPreview.refreshCamera(mCamera);

                            }
                        });
                    } catch (Exception e) {
                    e.printStackTrace();
                    }


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
            case R.id.action_home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.animator.animation3, R.animator.animation4);

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

    public void onResume() {
        super.onResume();

        Log.d("OnResume", "LLAMANDO BROZORZIOOOOOOOOOOOO");

        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
            }

            setContentView(R.layout.activity_camera);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            myContext = this;
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            //actionBar.hide();
            //RelativeLayout buttonsLayout = (RelativeLayout)findViewById(R.id.buttonsLayout);
            //buttonsLayout.bringToFront();
            initialize();
            mPreview.setWillNotDraw(false);
            mHandler = new Handler(Looper.getMainLooper());

            mCamera = Camera.open(findBackFacingCamera());
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);

        }
    }

    public void initialize() {
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        view = (GPUImageView) findViewById(R.id.live_filter_view);


        mPreview = new CameraPreview(myContext, mCamera, view);

        cameraPreview.addView(mPreview);

        capture = (ImageButton) findViewById(R.id.button_capture);
        capture.setOnClickListener(captureListener);

        filters = (LinearLayout) findViewById(R.id.button_filters);
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

        matrix = new Matrix();
        progBar = (ProgressBar) findViewById(R.id.loadingPanel);


    }


    public void filterNone(View v){

    }

    public void filterNashville(View v) {
        currentFilter = "nashville";
        mPreview.setActualFilter( new IFNashvilleFilter(this) );
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filter1977(View v) {
        currentFilter = "1977";
        mPreview.setActualFilter( new IF1977Filter(this) );
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterValencia(View v) {
        currentFilter = "valencia";
        mPreview.setActualFilter(new IFValenciaFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterAmaro(View v) {
        currentFilter = "amaro";
        mPreview.setActualFilter (new IFAmaroFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterBrannan(View v) {
        currentFilter = "brannan";
        mPreview.setActualFilter (new IFBrannanFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterEarlyBird(View v) {
        currentFilter = "earlybird";
        mPreview.setActualFilter (new IFEarlybirdFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterHefe(View v) {
        currentFilter = "hefe";
        mPreview.setActualFilter (new IFHefeFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterHudson(View v) {
        currentFilter = "hudson";
        mPreview.setActualFilter (new IFHudsonFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterInkwell(View v) {
        currentFilter = "inkwell";
        mPreview.setActualFilter (new IFInkwellFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterLomofi(View v) {
        currentFilter = "lomofi";
        mPreview.setActualFilter (new IFLomofiFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterLordKelvin(View v) {
        currentFilter = "lordkelvin";
        mPreview.setActualFilter(new IFLordKelvinFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterNormal(View v) {
        currentFilter = "normal";
        mPreview.setActualFilter(new IFNormalFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);

    }

    public void filterRise(View v) {
        currentFilter = "rise";
        mPreview.setActualFilter(new IFRiseFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterSierra(View v) {
        currentFilter = "sierra";
        mPreview.setActualFilter(new IFSierraFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterSutro(View v) {
        currentFilter = "sutro";
        mPreview.setActualFilter(new IFSutroFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterToaster(View v) {
        currentFilter = "toaster";
        mPreview.setActualFilter(new IFToasterFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterWalden(View v) {
        currentFilter = "walden";
        mPreview.setActualFilter(new IFWaldenFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterXproll(View v) {
        currentFilter = "xproll";
        mPreview.setActualFilter(new IFXproIIFilter(this));
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

    public void filterHaze(View v) {
        currentFilter = "haze";
        mPreview.setActualFilter(new GPUImageHazeFilter());
        menuFiltersLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);
    }

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
                _cameraId = 0;
                mPreview.setMatrix(180, _cameraId);
                mPicture = getPictureCallback();


            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {

                mCamera = Camera.open(cameraId);
                _cameraId = 1;
                mPreview.setMatrix(180, _cameraId);
                mPicture = getPictureCallback();



            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
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

                try {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                File pictureFileOut = getOutputMediaFile();
                                pictureFile = pictureFileOut;

                                FileOutputStream out = null;

                                out = new FileOutputStream(pictureFileOut);

                                auxImage = view.capture();
                                auxImage.compress(Bitmap.CompressFormat.PNG, 100, out);

                                try {
                                    if (out != null) {
                                        out.close();
                                        try {
                                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                            Uri uri = Uri.fromFile(pictureFileOut);
                                            mediaScanIntent.setData(uri);
                                            sendBroadcast(mediaScanIntent);
                                        } catch (Exception e) {
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                capturedImage.setImageBitmap(auxImage);
                                progBar.setVisibility(View.GONE);

                            } catch (Exception e) {
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                }

                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }


    OnClickListener captureListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            progBar.setVisibility(View.VISIBLE);
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
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview); // Alabado sea StackOverflow
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        //---save whatever you need to persist—

        outState.putInt("cameraId", cameraId);
        outState.putBoolean("cameraFront", cameraFront);
        outState.putString("filePath", filePath);
        outState.putString("currentFilter", currentFilter);
        outState.putInt("cameraIdOrient", _cameraId);

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);


        Log.d("broza", "CAMBIANDO ACTIVIDAD");

        if (savedInstanceState != null) {

            //sViewX = savedInstanceState.getInt("sViewX");
            //sViewY = savedInstanceState.getInt("sViewY");
            cameraId = savedInstanceState.getInt("cameraId");
            cameraFront = savedInstanceState.getBoolean("cameraFront");
            filePath = savedInstanceState.getString("filePath");
            currentFilter = savedInstanceState.getString("currentFilter");
            _cameraId = savedInstanceState.getInt("cameraIdOrient");


            //filtersScroll.scrollTo(sViewX, sViewY);
            //releaseCamera();
            mCamera = Camera.open(cameraId);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
            mPreview.setMatrix(0,cameraId);

            switch (currentFilter){
                case "nashville":   mPreview.setActualFilter(new IFNashvilleFilter(this));
                    break;
                case "1977"     :   mPreview.setActualFilter(new IF1977Filter(this));
                    break;
                case "valencia" :   mPreview.setActualFilter(new IFValenciaFilter(this));
                    break;
                case "amaro"    :   mPreview.setActualFilter(new IFAmaroFilter(this));
                    break;
                case "brannan"  :   mPreview.setActualFilter(new IFBrannanFilter(this));
                    break;
                case "earlybird":   mPreview.setActualFilter(new IFEarlybirdFilter(this));
                    break;
                case "hefe"     :   mPreview.setActualFilter(new IFHefeFilter(this));
                    break;
                case "hudson"   :   mPreview.setActualFilter(new IFHudsonFilter(this));
                    break;
                case "inkwell"  :   mPreview.setActualFilter(new IFInkwellFilter(this));
                    break;
                case "lomofi"   :   mPreview.setActualFilter(new IFLomofiFilter(this));
                    break;
                case "lordkelvin":  mPreview.setActualFilter(new IFLordKelvinFilter(this));
                    break;
                case "normal"   :   mPreview.setActualFilter(new IFNormalFilter(this));
                    break;
                case "rise"     :   mPreview.setActualFilter(new IFRiseFilter(this));
                    break;
                case "sierra"   :   mPreview.setActualFilter(new IFSierraFilter(this));
                    break;
                case "sutro"    :   mPreview.setActualFilter(new IFSutroFilter(this));
                    break;
                case "toaster"  :   mPreview.setActualFilter(new IFToasterFilter(this));
                    break;
                case "walden"   :   mPreview.setActualFilter(new IFWaldenFilter(this));
                    break;
                case "xproll"   :   mPreview.setActualFilter(new IFXproIIFilter(this));
                    break;
                case "haze"     :   mPreview.setActualFilter(new GPUImageHazeFilter());
                    break;
                default: mPreview.setActualFilter(null);
                    break;
            }


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
    }

}
