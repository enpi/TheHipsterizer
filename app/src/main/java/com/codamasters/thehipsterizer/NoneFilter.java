package com.codamasters.thehipsterizer;

import android.content.Context;
import android.graphics.BitmapFactory;

/**
 * Created by Juan on 10/04/2015.
 */
public class NoneFilter extends InstaFilter {

    public static final String SHADER =
            " void main()\n" +
            " { \n" +
            " }";

    public NoneFilter(Context context){
        super(SHADER, 0);
    }
}
