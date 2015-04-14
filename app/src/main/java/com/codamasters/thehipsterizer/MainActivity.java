package com.codamasters.thehipsterizer;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.view.View.OnClickListener;
import android.widget.TextView;


/**
 * Created by Juan on 26/02/2015.
 */
public class MainActivity extends ActionBarActivity {

    private ImageButton cameraMode, galleryMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.hide();

        TextView tv = (TextView) findViewById(R.id.titleIntro);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/pacifico.ttf");
        tv.setTypeface(face);

        cameraMode = (ImageButton) findViewById(R.id.cameraMode);
        galleryMode = (ImageButton) findViewById(R.id.galleryMode);
        initalize();

    }

    public void initalize(){
        
        cameraMode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        cameraMode.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        cameraMode.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        cameraMode.getBackground().clearColorFilter();
                        cameraMode.invalidate();
                        break;
                    }
                }
                return false;
            }
        });


        galleryMode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        galleryMode.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        galleryMode.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        galleryMode.getBackground().clearColorFilter();
                        galleryMode.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }

    public void openCamera(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        overridePendingTransition(R.animator.animation1, R.animator.animation2);

    }

    public void openFilter(View view){
        Intent intent = new Intent(this, FilterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.animator.animation1, R.animator.animation2);

    }


}
