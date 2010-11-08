package org.linnaeus.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by IntelliJ IDEA.
 * User: Romchig
 * Date: 29.10.2010
 */
public class MainActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGHT = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, PaintAreaActivity.class));
                MainActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGHT);
    }
}
