package com.codamasters.thehipsterizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

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
    private OrientationEventListener mOrientationEventListener;
    private int mOrientation =  -1;
    private int gira=0;

    private static final int ORIENTATION_PORTRAIT_NORMAL =  1;
    private static final int ORIENTATION_PORTRAIT_INVERTED =  2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL =  3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED =  4;



    public void setActualFilter(GPUImageFilter actualFilter) {
        this.actualFilter = actualFilter;
        if(actualFilter != null)
            view.setFilter(actualFilter);
    }

    public void setMatrix(int degree, int cameraId) {
        matrix.postRotate(degree);
        this.cameraId = cameraId;

        Log.d("ID CAMERA REAL", this.cameraId+"");

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

        mOrientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {

                // determine our orientation based on sensor response
                int lastOrientation = mOrientation;

                if (orientation >= 315 || orientation < 45) {
                    if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                        mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                    }
                } else if (orientation < 315 && orientation >= 225) {
                    if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                        mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                    }
                } else if (orientation < 225 && orientation >= 135) {
                    if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                        mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                    }
                } else { // orientation <135 && orientation > 45
                    if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                        mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                    }
                }

                if (lastOrientation != mOrientation) {

                    if(gira%2==1)
                        setMatrix(180, cameraId);

                    gira++;

                }
            }
        };
    }

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			// create the surface and start camera preview

            Log.d("Mensaje", "Superficie creada");
            mOrientationEventListener.enable();

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();


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

        Log.d("ID CAMERA", cameraId+"");


        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Log.d("Orientacion", "CAMBIANDO CONFIGURACION DE LA ORIENTACION DE LA CAMARA");

        int angle;
        switch (display.getRotation()) {
            case Surface.ROTATION_0: // This is display orientation
                angle = 90; // This is camera orientation
                matrix.postRotate(90);

                if(cameraId==1){
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
                matrix.postRotate(90);
                break;
        }
        Log.v("Girando camera", "angle: " + angle);

        mCamera.setDisplayOrientation(angle);

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
        mOrientationEventListener.disable();

    }



    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
            Parameters parameters = camera.getParameters();
            int imageFormat = parameters.getPreviewFormat();
            if (imageFormat == ImageFormat.NV21)
            {

                try {

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