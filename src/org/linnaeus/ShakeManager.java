package org.linnaeus;

import android.content.Context;
import android.hardware.*;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Immortality
 * Date: 30.10.2010
 * Time: 16:36:31
 */

public final class ShakeManager implements SensorEventListener {

    private static final int FORCE_THRESHOLD = 1000;
    private static final int TIME_THRESHOLD = 100;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 1000;
    private static final int SHAKE_COUNT = 3;

    private Context _context;

    private SensorManager _sensorMgr;
    private long _lastUpdate = -1;
    private float _last_x, _last_y, _last_z;


    private int _shakeCount = 0;
    private long _lastShake;
    private long _lastForce;

    private ArrayList<ShakeEventListener> _listeners;

    public ShakeManager(Context context) {
        _context = context;
        _listeners = new ArrayList<ShakeEventListener>();
        initSensorManager();
    }

    private void initSensorManager(){

        _sensorMgr = (SensorManager) _context.getSystemService(Context.SENSOR_SERVICE);

        boolean accelerometerSupported =
                _sensorMgr.registerListener(this,
      			_sensorMgr.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER),
                   		                    SensorManager.SENSOR_DELAY_GAME);

        if (!accelerometerSupported) {
            // Non accelerometer on this device.
            _sensorMgr.unregisterListener(this);
        }
    }

    public void addListener(ShakeEventListener listener){
        _listeners.add(listener);
    }

    public void onPause() {
        if (_sensorMgr != null) {
            _sensorMgr.unregisterListener(this,
                    _sensorMgr.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER));
            _sensorMgr = null;
        }
    }

    public void onResume(){
        initSensorManager();
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor mySensor = event.sensor;

        if (mySensor.getType() == SensorManager.SENSOR_ACCELEROMETER) {

            long curTime = System.currentTimeMillis();

            if ((curTime - _lastForce) > SHAKE_TIMEOUT) {
              _shakeCount = 0;
            }

            // Only allow one update every 100ms.
       	    if ((curTime - _lastUpdate) > TIME_THRESHOLD) {
   	    		long diffTime = (curTime - _lastUpdate);
   	    		_lastUpdate = curTime;

               float x = event.values[SensorManager.DATA_X];
               float y = event.values[SensorManager.DATA_Y];
               float z = event.values[SensorManager.DATA_Z];

  	    		float speed = Math.abs(x + y + z - _last_x - _last_y - _last_z)/ diffTime * 10000;

                if (speed > FORCE_THRESHOLD) {

                    if ((++_shakeCount >= SHAKE_COUNT) && (curTime - _lastShake > SHAKE_DURATION)) {
                      _lastShake = curTime;
                      _shakeCount = 0;
                      if (_sensorMgr != null) {
                          for(ShakeEventListener listener : _listeners) {
                            listener.onShakeEvent();
                          }
                      }
                    }

                    _lastForce = curTime;
  	    		}

                _lastForce = curTime;
                _last_x = x;
                _last_y = y;
                _last_z = z;
      	    }
      	}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public interface ShakeEventListener {
        public void onShakeEvent();
    }
}
