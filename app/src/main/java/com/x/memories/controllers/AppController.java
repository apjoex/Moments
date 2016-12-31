package com.x.memories.controllers;

import android.content.Context;
import android.os.StrictMode;

import com.x.memories.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by AKINDE-PETERS on 9/4/2016.
 */
public class AppController extends android.app.Application {
    public static final String TAG = AppController.class.getSimpleName();

    Context context;
    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        context = getApplicationContext();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }
}
