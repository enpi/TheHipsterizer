package com.codamasters.thehipsterizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.Configuration;
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
    private int cameraId;
    private int orient;

    public void setActualFilter(GPUImageFilter actualFilter) {
        this.actualFilter = actualFilter;
        if(actualFilter != null)
            view.setFilter(actualFilter);
    }

    public void setMatrix(int degree, int cameraId) {
        matrix.postRotate(degree);
        this.cameraId = cameraId;
    }


    public CameraPreview(Context context, Camera camera, GPUImageView view) {
		super(context);
        actualFilter = new IFNashvilleFilter(context);
        matrix = new Matrix();
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

            Log.d("Mensaje", "Superficie creada");

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();

            orient = display.getOrientation();



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

        Log.d("Mensaje", "Camera actualizada");


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


            if(this!=null)
                mCamera.setPreviewCallback(this);

            mCamera.setPreviewDisplay(mHolder);
			Camera.Parameters params = mCamera.getParameters();
			params.setColorEffect(currentFilter);
            params.setPreviewSize(640,480);
            params.setFlashMode(currentFlash);
            mCamera.setParameters(params);


            mCamera.startPreview();

            isPreviewRunning = true;
		} catch (Exception e) {
			Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
		}
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Log.d("Mensaje", "Superficie cambiada");


        if (isPreviewRunning)
        {
            mCamera.stopPreview();
            isPreviewRunning = false;
    }

        try{

            configureCameraOrientation();

            refreshCamera(mCamera);
            mCamera.startPreview();
            isPreviewRunning = true;

        } catch (Exception e){
            Log.d("Error", "Error starting camera preview: " + e.getMessage());
        }

    }

    public void configureCameraOrientation(){


        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Log.d("Orientacion", "CAMBIANDO CONFIGURACION DE LA ORIENTACION DE LA CAMARA");

        int angle;
        switch (display.getRotation()) {
            case Surface.ROTATION_0: // This is display orientation
                if(cameraId==0) {
                    angle = 90; // This is camera orientation
                    matrix.postRotate(90);
                }
                else{
                    angle=180;
                    matrix.postRotate(180);
                }
                break;
            case Surface.ROTATION_90:
                angle = 0;
                matrix.postRotate(0);
                break;
            case Surface.ROTATION_180:
                angle = 270;
                matrix.postRotate(270);
                break;
            case Surface.ROTATION_270:
                angle = 180;
                matrix.postRotate(180);
                break;
            default:
                angle = 90;
                matrix.postRotate(0);
                break;
        }
        Log.v("Girando camera", "angle: " + angle);

        mCamera.setDisplayOrientation(angle);


        // En caso de girar 180 grados en un unico movimiento
        /*
        if(Math.abs(orient-display.getOrientation())>1 ){
            matrix.postRotate(180);
        }
        orient=display.getOrientation();*/
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
        Log.d("Mensaje", "Superficie destruida");


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
            //camera.addCallbackBuffer(data);
        }



}