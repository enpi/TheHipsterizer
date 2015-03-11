package com.codamasters.thehipsterizer;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.view.View.OnClickListener;
import android.widget.TextView;


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
        getWindow().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.hide();

        TextView tv = (TextView) findViewById(R.id.titleIntro);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/pacifico.ttf");
        tv.setTypeface(face);
    }

    public void openCamera(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void openFilter(View view){
        Intent intent = new Intent(this, FilterActivity.class);
        startActivity(intent);
    }


}
