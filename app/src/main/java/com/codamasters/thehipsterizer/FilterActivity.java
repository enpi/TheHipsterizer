package com.codamasters.thehipsterizer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.net.Uri;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.effect.EffectFactory;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by julio on 26/02/15.
 */
public class FilterActivity extends ActionBarActivity implements GLSurfaceView.Renderer {

    static final int REQ_CODE_PICK_IMAGE = 1;
    private Bitmap galleryImage;
    private Bitmap auxImage;
    private Context context;

    private GLSurfaceView mEffectView;
    private int[] mTextures = new int[2];
    private EffectContext mEffectContext;
    private Effect mEffect;
    private TextureRenderer mTexRenderer = new TextureRenderer();
    private int mImageWidth;
    private int mImageHeight;
    private boolean mInitialized = false;
    private int mCurrentEffect;

    public void setCurrentEffect(int effect) {
        mCurrentEffect = effect;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_filter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        /**
         * Initialise the renderer and tell it to only render when
         * Explicit requested with the RENDERMODE_WHEN_DIRTY option
         */
        mEffectView = (GLSurfaceView) findViewById(R.id.image_preview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mCurrentEffect = R.id.none;


        pickImage();


    }


    public static Bitmap applyGaussianBlur(Bitmap src) {
        //set gaussian blur configuration
        double[][] GaussianBlurConfig = new double[][]{
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };
        // create instance of Convolution matrix
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        // Apply Configuration
        convMatrix.applyConfig(GaussianBlurConfig);
        convMatrix.Factor = 16;
        convMatrix.Offset = 0;
        //return out put bitmap
        return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }

    public void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = context.getContentResolver().openInputStream(selectedImage);
                        galleryImage = BitmapFactory.decodeStream(imageStream);
                        auxImage = galleryImage;

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private void loadTextures(Bitmap bitmap) {
        // Generate textures
        GLES20.glGenTextures(2, mTextures, 0);

        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);

        // Upload to texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // Set texture parameters
        GLToolbox.initTexParams();
    }

    public void filterNegative(View v) {
        setCurrentEffect(R.id.negative);
        mEffectView.requestRender();
    }

    public void filterAutoFix(View v) {
        setCurrentEffect(R.id.autofix);
        mEffectView.requestRender();
    }


    public void filterBw(View v) {
        setCurrentEffect(R.id.bw);
        mEffectView.requestRender();
    }

    public void filterBrightness(View v) {
        setCurrentEffect(R.id.brightness);
        mEffectView.requestRender();
    }

    public void filterContrast(View v) {
        setCurrentEffect(R.id.contrast);
        mEffectView.requestRender();
    }

    public void filterCrossprocess(View v) {
        setCurrentEffect(R.id.crossprocess);
        mEffectView.requestRender();
    }

    public void filterDocumentary(View v) {
        setCurrentEffect(R.id.documentary);
        mEffectView.requestRender();
    }

    public void filterDuotone(View v) {
        setCurrentEffect(R.id.duotone);
        mEffectView.requestRender();
    }

    public void filterFillLight(View v) {
        setCurrentEffect(R.id.filllight);
        mEffectView.requestRender();
    }

    public void filterFishEye(View v) {
        setCurrentEffect(R.id.fisheye);
        mEffectView.requestRender();
    }

    public void filterFlipvert(View v) {
        setCurrentEffect(R.id.flipvert);
        mEffectView.requestRender();
    }

    public void filterFliphor(View v) {
        setCurrentEffect(R.id.fliphor);
        mEffectView.requestRender();
    }

    public void filterGrain(View v) {
        setCurrentEffect(R.id.grain);
        mEffectView.requestRender();
    }

    public void filterGrayscale(View v) {
        setCurrentEffect(R.id.grayscale);
        mEffectView.requestRender();
    }


    public void filterLomoish(View v) {
        setCurrentEffect(R.id.lomoish);
        mEffectView.requestRender();
    }


    public void filterPosterize(View v) {
        setCurrentEffect(R.id.posterize);
        mEffectView.requestRender();
    }


    public void filterRotate(View v) {
        setCurrentEffect(R.id.rotate);
        mEffectView.requestRender();
    }


    public void filterSaturate(View v) {
        setCurrentEffect(R.id.saturate);
        mEffectView.requestRender();
    }


    public void filterSepia(View v) {
        setCurrentEffect(R.id.sepia);
        mEffectView.requestRender();
    }

    public void filterSharpen(View v) {
        setCurrentEffect(R.id.sharpen);
        mEffectView.requestRender();
    }

    public void filterTemperature(View v) {
        setCurrentEffect(R.id.temperature);
        mEffectView.requestRender();
    }

    public void filterTint(View v) {
        setCurrentEffect(R.id.tint);
        mEffectView.requestRender();
    }

    public void filterVignette(View v) {
        setCurrentEffect(R.id.vignette);
        mEffectView.requestRender();
    }


    private void initEffect() {
        EffectFactory effectFactory = mEffectContext.getFactory();
        if (mEffect != null) {
            mEffect.release();
        }
        /**
         * Initialize the correct effect based on the selected menu/action item
         */
        switch (mCurrentEffect) {

            case R.id.none:
                break;

            case R.id.negative:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_NEGATIVE);
                break;

            case R.id.autofix:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_AUTOFIX);
                mEffect.setParameter("scale", 0.5f);
                break;

            case R.id.bw:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_BLACKWHITE);
                mEffect.setParameter("black", .1f);
                mEffect.setParameter("white", .7f);
                break;

            case R.id.brightness:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_BRIGHTNESS);
                mEffect.setParameter("brightness", 2.0f);
                break;

            case R.id.contrast:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_CONTRAST);
                mEffect.setParameter("contrast", 1.4f);
                break;

            case R.id.crossprocess:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_CROSSPROCESS);
                break;

            case R.id.documentary:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_DOCUMENTARY);
                break;

            case R.id.duotone:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_DUOTONE);
                mEffect.setParameter("first_color", Color.YELLOW);
                mEffect.setParameter("second_color", Color.DKGRAY);
                break;

            case R.id.filllight:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FILLLIGHT);
                mEffect.setParameter("strength", .8f);
                break;

            case R.id.fisheye:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FISHEYE);
                mEffect.setParameter("scale", .5f);
                break;

            case R.id.flipvert:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FLIP);
                mEffect.setParameter("vertical", true);
                break;

            case R.id.fliphor:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FLIP);
                mEffect.setParameter("horizontal", true);
                break;

            case R.id.grain:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_GRAIN);
                mEffect.setParameter("strength", 1.0f);
                break;

            case R.id.grayscale:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_GRAYSCALE);
                break;

            case R.id.lomoish:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_LOMOISH);
                break;

            case R.id.posterize:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_POSTERIZE);
                break;

            case R.id.rotate:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_ROTATE);
                mEffect.setParameter("angle", 180);
                break;

            case R.id.saturate:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SATURATE);
                mEffect.setParameter("scale", .5f);
                break;

            case R.id.sepia:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SEPIA);
                break;

            case R.id.sharpen:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SHARPEN);
                break;

            case R.id.temperature:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_TEMPERATURE);
                mEffect.setParameter("scale", .9f);
                break;

            case R.id.tint:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_TINT);
                mEffect.setParameter("tint", Color.MAGENTA);
                break;

            case R.id.vignette:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_VIGNETTE);
                mEffect.setParameter("scale", .5f);
                break;

            default:
                break;

        }
    }

    private void applyEffect() {
        mEffect.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
    }


    private void renderResult() {
        if (mCurrentEffect != R.id.none) {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[1]);
        } else {
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[0]);

        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mInitialized) {
            //Only need to do this once
            mEffectContext = EffectContext.createWithCurrentGlContext();
            mTexRenderer.init();
            loadTextures(galleryImage);
            mInitialized = true;
        }
        if (mCurrentEffect != R.id.none) {
            //if an effect is chosen initialize it and apply it to the texture
            initEffect();
            applyEffect();
        }
        renderResult();

        auxImage = createBitmapFromGLSurface((int) mEffectView.getX(),(int) mEffectView.getY(),mEffectView.getWidth(),mEffectView.getHeight(),gl);

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

                File pictureFile = getOutputMediaFile();

                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(pictureFile);
                    auxImage.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                            try {
                                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri uri = Uri.fromFile(pictureFile);
                                mediaScanIntent.setData(uri);
                                sendBroadcast(mediaScanIntent);
                            } catch(Exception e) {
                            }

                            Toast.makeText(getApplicationContext(), "Imagen guardada",Toast.LENGTH_LONG).show();

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return true;

            case R.id.action_home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

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

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
            throws OutOfMemoryError {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }
}
