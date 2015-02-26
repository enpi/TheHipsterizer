package com.codamasters.thehipsterizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
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
import android.view.WindowManager;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
    private String currentFilter = Parameters.EFFECT_NONE;
    private String currentFlash = Parameters.FLASH_MODE_OFF;
    private Context context;
    boolean isPreviewRunning;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
        isPreviewRunning = false;
        this.context = context;
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
            isPreviewRunning = true;
		} catch (Exception e) {
			Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

    public void setCurrentFilter(String currentFilter) {
        this.currentFilter = currentFilter ;
    }

    public String getCurrentFilter() {
        return currentFilter ;
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

    private byte[] cameraFrame;
    private byte[] buffer;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        cameraFrame = data;
        camera.addCallbackBuffer(data); //actually, addCallbackBuffer(buffer) has to be called once sowhere before you call mCamera.startPreview();
    }


    private ByteArrayOutputStream baos;
    private YuvImage yuvimage;
    private byte[] jdata;
    private Bitmap bmp;
    private Paint paint;

    @Override //from SurfaceView
    public void onDraw(Canvas canvas) {
        /*
        baos = new ByteArrayOutputStream();
        yuvimage=new YuvImage(cameraFrame, ImageFormat.NV21, prevX, prevY, null);

        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos); //width and height of the screen
        jdata = baos.toByteArray();

        bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);

        canvas.drawBitmap(bmp , 0, 0, paint);
        invalidate(); //to call ondraw again
        */
    }
}