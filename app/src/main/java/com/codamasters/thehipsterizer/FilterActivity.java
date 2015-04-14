package com.codamasters.thehipsterizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageCrosshatchFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHazeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRGBDilationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSobelEdgeDetection;
import jp.co.cyberagent.android.gpuimage.GPUImageView;


/**
 * Created by julio on 26/02/15.
 */
public class FilterActivity extends ActionBarActivity {

    static final int REQ_CODE_PICK_IMAGE = 1;
    private Bitmap galleryImage;

    private Bitmap auxImage;
    private Context context;

    private GPUImageView mEffectView;
    private TextureRenderer mTexRenderer = new TextureRenderer();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_filter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);


        /**
         * Initialise the renderer and tell it to only render when
         * Explicit requested with the RENDERMODE_WHEN_DIRTY option
         */
        mEffectView = (GPUImageView) findViewById(R.id.image_preview);


        pickImage();


    }

    public void pickImage() {
        /*
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
        */
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent , REQ_CODE_PICK_IMAGE );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK && imageReturnedIntent !=  null ) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = context.getContentResolver().openInputStream(selectedImage);
                        galleryImage = BitmapFactory.decodeStream(imageStream);
                        auxImage = galleryImage;
                        mEffectView.setImage(galleryImage);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    finish();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    galleryImage = null;
                    auxImage = null;

                }
        }
    }

    public void filterNone(View v){

    }

    public void filterNashville(View v) {
        mEffectView.setFilter(new IFNashvilleFilter(context));
    }

    public void filter1977(View v) {
        mEffectView.setFilter(new IF1977Filter(context));
    }

    public void filterValencia(View v) {
        mEffectView.setFilter(new IFValenciaFilter(context));
    }

    public void filterAmaro(View v) {
        mEffectView.setFilter(new IFAmaroFilter(context));
    }

    public void filterBrannan(View v) {
        mEffectView.setFilter(new IFBrannanFilter(context));
    }

    public void filterEarlyBird(View v) {
        mEffectView.setFilter(new IFEarlybirdFilter(context));
    }

    public void filterHefe(View v) {
        mEffectView.setFilter(new IFHefeFilter(context));
    }

    public void filterHudson(View v) {
        mEffectView.setFilter(new IFHudsonFilter(context));
    }

    public void filterInkwell(View v) {
        mEffectView.setFilter(new IFInkwellFilter(context));
    }

    public void filterLomofi(View v) {
        mEffectView.setFilter(new IFLomofiFilter(context));
    }

    public void filterLordKelvin(View v) {
        mEffectView.setFilter(new IFLordKelvinFilter(context));
    }

    public void filterNormal(View v) {
        mEffectView.setFilter(new IFNormalFilter(context));
    }

    public void filterRise(View v) {
        mEffectView.setFilter(new IFRiseFilter(context));
    }

    public void filterSierra(View v) {
        mEffectView.setFilter(new IFSierraFilter(context));
    }

    public void filterSutro(View v) {
        mEffectView.setFilter(new IFSutroFilter(context));
    }

    public void filterToaster(View v) {
        mEffectView.setFilter(new IFToasterFilter(context));
    }

    public void filterWalden(View v) {
        mEffectView.setFilter(new IFWaldenFilter(context));
    }

    public void filterXproll(View v) {
        mEffectView.setFilter(new IFXproIIFilter(context));
    }

    public void filterHaze(View v) {
        mEffectView.setFilter(new GPUImageHazeFilter());
    }

    // Create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filter, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {


        // Handle presses on the action bar items
        ActionBar actionBar = getSupportActionBar();
        switch (item.getItemId()) {
            case R.id.action_save:
                try {

                    new Thread(new Runnable() {
                        public void run() {
                            try {

                                File pictureFile = getOutputMediaFile();

                                FileOutputStream out = null;

                                out = new FileOutputStream(pictureFile);

                                auxImage =  mEffectView.capture();

                                auxImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                                try {
                                if (out != null) {
                                    out.close();
                                    try {
                                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        Uri uri = Uri.fromFile(pictureFile);
                                        mediaScanIntent.setData(uri);
                                        sendBroadcast(mediaScanIntent);
                                    } catch (Exception e) {
                                    }
                                }
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }


                            } catch(Exception e) {
                            }
                        }
                    }).start();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Toast.makeText(getApplicationContext(), "Imagen guardada",Toast.LENGTH_LONG).show();
                }

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

}
