package org.linnaeus.activity;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by IntelliJ IDEA.
 * User: Romchig
 * Date: 29.10.2010
 */
public class Main extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
