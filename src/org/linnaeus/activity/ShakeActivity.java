package org.linnaeus.activity;

import android.app.Activity;
import android.hardware.*;
import android.os.Bundle;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 30.10.2010
 * Time: 16:36:31
 */

// Based on sample from article "How to detect shake motion in Android – part I"
// http://www.codeshogun.com/blog/2009/04/17/how-to-detect-shake-motion-in-android-part-i/
public class ShakeActivity extends Activity implements SensorEventListener {

    private SensorManager sensorMgr;    
    private long lastUpdate = -1;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

        boolean accelerometerSupported =
                sensorMgr.registerListener(this,
      			sensorMgr.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER),
                   		                   SensorManager.SENSOR_DELAY_GAME);

        if (!accelerometerSupported) {
            // Non accelerometer on this device.
            sensorMgr.unregisterListener(this);
        }
    }

    protected void onPause() {
        if (sensorMgr != null) {
            sensorMgr.unregisterListener(this,
                    sensorMgr.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER));
            sensorMgr = null;
            }
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor mySensor = event.sensor;

        if (mySensor.getType() == SensorManager.SENSOR_ACCELEROMETER) {

            long curTime = System.currentTimeMillis();

            // Only allow one update every 100ms.
       	    if ((curTime - lastUpdate) > 100) {
   	    		long diffTime = (curTime - lastUpdate);
   	    		lastUpdate = curTime;

               float x = event.values[SensorManager.DATA_X];
               float y = event.values[SensorManager.DATA_Y];
               float z = event.values[SensorManager.DATA_Z];

  	    		float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
  	    		if (speed > SHAKE_THRESHOLD) {
  	    			// yes, this is a shake action! Do something about it!
  	    			// MyMethod();
  	    		}
  	    		last_x = x;
  	    		last_y = y;
  	    		last_z = z;
      	    }
      	}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
