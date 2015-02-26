package com.codamasters.thehipsterizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.view.View.OnClickListener;


/**
 * Created by Juan on 26/02/2015.
 */
public class MainActivity extends ActionBarActivity {

    private ImageButton cameraMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.hide();
        cameraMode = (ImageButton) findViewById(R.id.cameraMode);
        cameraMode.setOnClickListener(cameraModeListener);
    }

    OnClickListener cameraModeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent("com.codamasters.thehipsterizer.CameraActivity");
            startActivity(i);
        }
    };


}
