package com.sigelinde.WearSpiritLevel;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.lang.ref.WeakReference;
import java.util.List;

public class AccelerationSensorHandler implements SensorEventListener {
    private SensorManager mSensorManager = null;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] mRotationMatrix = new float[9];
    private float[] mAttitude = new float[3];
    boolean mIsGravityReady = false, mIsGeomagneticReady = false;
    private WeakReference<OnAccelerometerEventListener> mListener;

    private float mOffsetX = 0, mOffsetY = 0;
    private float mRotateX = 0, mRotateY = 0;

    public AccelerationSensorHandler(Activity activity, OnAccelerometerEventListener l)
    {
        mSensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
        mListener = new WeakReference<OnAccelerometerEventListener>(l);

        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        for(Sensor sensor : sensorList)
        {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            break;
        }

        sensorList = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        for(Sensor sensor : sensorList)
        {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            break;
        }
    }

    public void clean()
    {
        mSensorManager.unregisterListener(this);
        mIsGravityReady = false;
        mIsGeomagneticReady = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // nothing here for now
    }

    public float normalize(float angle)
    {
        while(angle >= Math.PI)angle -= Math.PI * 2;
        while(angle < -Math.PI)angle += Math.PI * 2;
        return angle;
    }
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        switch(event.sensor.getType()){
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values.clone();
                mIsGeomagneticReady = true;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values.clone();
                mIsGravityReady = true;
                break;
        }

        if(!mIsGravityReady || !mIsGeomagneticReady)return;

        //calculate normed acceleration
        SensorManager.getRotationMatrix(
                mRotationMatrix, null,
                mGravity, mGeomagnetic);

        SensorManager.getOrientation(
                mRotationMatrix,
                mAttitude);

        //calculate tilt angle
        mRotateX = -mAttitude[2];
        mRotateY = -mAttitude[1];

        //call listener
        OnAccelerometerEventListener l = mListener.get();
        if(l != null)
        {
            float rx = mRotateX + mOffsetX, ry = mRotateY + mOffsetY;
            rx = normalize(rx);
            ry = normalize(ry);
            l.OnAccelerometerEvent(mRotateX + mOffsetX, mRotateY + mOffsetY);
        }
    }

    public void setZero()
    {
        mOffsetX = -mRotateX;
        mOffsetY = -mRotateY;
    }
}
