package org.linnaeus.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by IntelliJ IDEA.
 * User: Romchig
 * Date: 29.10.2010
 */
public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startActivity(new Intent(this, PaintAreaActivity.class));
    }
}
