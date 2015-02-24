package com.codamasters.thehipsterizer;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "CameraPreview";

    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.PreviewCallback previewCallback;
    boolean hidden;

    boolean isPreviewRunning;
    private Context context;
    private String currentFilter = Parameters.EFFECT_NONE;
    private String currentFlash = Parameters.FLASH_MODE_OFF;

    public CameraPreview(Context context, Camera.PreviewCallback previewCallback, boolean hidden ) {
        super(context);
        this.previewCallback = previewCallback;
        this.hidden = hidden;
        this.context = context;

        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width,height;
        if( hidden ) {
            // make the view small, effectively hiding it
            width=height=2;
        } else {
            // We purposely disregard child measurements so that the SurfaceView will center the camera
            // preview instead of stretching it.
            width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            Log.d(TAG, "Camera is null.  Bug else where in code. ");
            return;
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if( mCamera == null )
            return;

        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            Camera.Size size = mCamera.getParameters().getPreviewSize();
            int previewWidth = size.width;
            int previewHeight = size.height;

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                l = (width - scaledChildWidth) / 2;
                t = 0;
                r = (width + scaledChildWidth) / 2;
                b = height;
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                l = 0;
                t = (height - scaledChildHeight) / 2;
                r = width;
                b = (height + scaledChildHeight) / 2;
            }
            child.layout(l,t,r,b);
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


    public void setCurrentFlash(String currentFlash) {
        this.currentFlash = currentFlash;
    }


    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// mCamera.release();

	}

}