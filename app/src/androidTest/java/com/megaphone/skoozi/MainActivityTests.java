package com.megaphone.skoozi;

import android.test.ActivityInstrumentationTestCase2;

import com.megaphone.skoozi.nearby.MainActivity;

/**
 * Created by ahmadul.hassan on 2015-06-22.
 */
public class MainActivityTests extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainActivityTests() {
        super(MainActivity.class);
    }

    public void testActivityExists() {
        BaseActivity activity = getActivity();
        assertNotNull(activity);
    }

}
