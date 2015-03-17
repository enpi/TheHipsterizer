package com.codamasters.thehipsterizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHazeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.Rotation;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
    private String currentFilter = Parameters.EFFECT_NONE;
    private String currentFlash = Parameters.FLASH_MODE_OFF;
    private Context context;
    boolean isPreviewRunning;
    private GPUImageView view;
    private Bitmap bitmap;
    private Matrix matrix;
    private Camera.Size previewSize;
    private YuvImage yuvimage;
    private ByteArrayOutputStream baos;
    private byte[] jdata;
    private Bitmap rotatedBitmap;
    private GPUImageFilter actualFilter;


    public void setActualFilter(GPUImageFilter actualFilter) {
        this.actualFilter = actualFilter;
        view.setFilter(actualFilter);

    }

	public CameraPreview(Context context, Camera camera, GPUImageView view) {
		super(context);
        actualFilter = new IFNashvilleFilter(context);
        matrix = new Matrix();
        matrix.postRotate(90);
        mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
        isPreviewRunning = false;
        this.context = context;
        this.view = view;
        // deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setWillNotDraw(false);

    }

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			// create the surface and start camera preview
			if (mCamera == null) {
				mCamera.setPreviewDisplay(holder);

                mCamera.startPreview();
                isPreviewRunning = true;
			}
		} catch (IOException e) {
			Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void refreshCamera(Camera camera) {
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}
		// stop preview before making changes
		try {
			mCamera.stopPreview();
            isPreviewRunning = false;
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}
		// set preview size and make any resize, rotate or
		// reformatting changes here
		// start preview with new settings

		setCamera(camera);
		try {
			mCamera.setPreviewDisplay(mHolder);
			Camera.Parameters params = mCamera.getParameters();
			params.setColorEffect(currentFilter);
            params.setFlashMode(currentFlash);

            mCamera.setParameters(params);
			mCamera.startPreview();


            mCamera.setPreviewCallback(this);
            isPreviewRunning = true;
		} catch (Exception e) {
			Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
		}
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (isPreviewRunning)
        {
            mCamera.stopPreview();
            isPreviewRunning = false;
        }

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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

        refreshCamera(mCamera);

        mCamera.startPreview();
        isPreviewRunning = true;

    }

	public void setCamera(Camera camera) {
		//method to set a camera instance
		mCamera = camera;

	}

    public void setCurrentFlash(String currentFlash) {
        this.currentFlash = currentFlash;
    }


    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// mCamera.release();

	}



    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
            Parameters parameters = camera.getParameters();
            int imageFormat = parameters.getPreviewFormat();
            if (imageFormat == ImageFormat.NV21)
            {


                try {

                // Convert to JPG
                previewSize = camera.getParameters().getPreviewSize();
                yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                baos = new ByteArrayOutputStream();
                yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
                jdata = baos.toByteArray();

                bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);

                rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);

                view.setImage(rotatedBitmap);

                } catch (Error e) {
                    Log.e("Error", e.getLocalizedMessage());
                }

            }

            camera.addCallbackBuffer(data);
        }



}