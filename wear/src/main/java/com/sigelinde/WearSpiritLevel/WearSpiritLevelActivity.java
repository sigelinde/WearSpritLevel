package com.sigelinde.WearSpiritLevel;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sigelinde.WearSpiritLevel.R;

public class WearSpiritLevelActivity extends Activity implements OnAccelerometerEventListener, View.OnClickListener {

    private TextView mTextView;
    private AccelerationSensorHandler mAccelerationHandler;
    private ImageView mCenterDot;
    private TextView mDebugText;
    private Point mScreenSize = new Point();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editable_wear_face);
        mCenterDot = (ImageView) findViewById(R.id.center_dot);
        mCenterDot.setOnClickListener(this);
        findViewById(R.id.background).setOnClickListener(this);

        //mDebugText = (TextView) findViewById(R.id.debug_text);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // ウィンドウマネージャのインスタンス取得
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        disp.getSize(mScreenSize);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAccelerationHandler = new AccelerationSensorHandler(this, this);
    }

    @Override
    protected void onPause() {
        mAccelerationHandler.clean();
        mAccelerationHandler = null;

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onPause();
    }

    @Override
    public void onClick(View v) {
        mAccelerationHandler.setZero();

        Toast.makeText(this, R.string.set_zero, Toast.LENGTH_SHORT).show();
    }

    public void OnAccelerometerEvent(float rx, float ry)
    {
        rx = rx;
        ry = -ry;

        float dot_width = mCenterDot.getWidth() / 2.0f, dot_height = mCenterDot.getHeight() / 2.0f;
        float display_width = mScreenSize.x, display_height = mScreenSize.y;
        float angle_limit = (float)Math.PI / 2;
        float fixed_rx = rx;
        float fixed_ry = ry;

        float angle_length = (float)Math.sqrt(Math.pow(rx, 2) + Math.pow(ry, 2));
        if(angle_length > angle_limit)
        {
            fixed_rx *= angle_limit / angle_length;
            fixed_ry *= angle_limit / angle_length;
        }

        mCenterDot.setX(fixed_rx / (float)Math.PI * display_width + display_width / 2.0f - dot_width);
        mCenterDot.setY(fixed_ry / (float)Math.PI * display_height + display_height / 2.0f - dot_height);

        //mDebugText.setText(String.format("(%1.4f, %1.4f)", rx / Math.PI * 180, ry / Math.PI * 180));
    }
}
