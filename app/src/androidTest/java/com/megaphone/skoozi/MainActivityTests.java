package com.megaphone.skoozi;

import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by ahmadul.hassan on 2015-06-22.
 */
public class MainActivityTests extends ActivityInstrumentationTestCase2<BaseActivity> {
    public MainActivityTests() {
        super(MainActivity.class);
    }

    public void testActivityExists() {
        BaseActivity activity = getActivity();
        assertNotNull(activity);
    }

}
