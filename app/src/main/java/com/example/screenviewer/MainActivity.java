package com.example.screenviewer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import com.example.screenviewer.client.ClientActivity;
import com.example.screenviewer.host.ScreenCastService;
import com.example.screenviewer.networkutils.NetworkUtilities;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    CardView _shareCardView;
    CardView _accessCardView;

    TextView _titleTextView;
    TextView _descriptionTextView;

    private MediaProjectionManager _projectionManager;

    public void initUI()
    {
        _shareCardView = findViewById(R.id.card_view_share);
        _accessCardView = findViewById(R.id.card_view_access);

        _titleTextView = _shareCardView.findViewById(R.id.share_title_text_view);
        _descriptionTextView = _shareCardView.findViewById(R.id.share_desc_text_view);

        updateUI(ScreenCastService.isStarted());

        _shareCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtilities.isHotspotEnabled(MainActivity.this))
                {
                    if (ScreenCastService.isStarted())
                    {
                        Intent i = new Intent(MainActivity.this, ScreenCastService.class);
                        stopService(i);
                        updateUI(false);
                    }
                    else
                    {
                        startActivityForResult(_projectionManager.createScreenCaptureIntent(),
                                Constants.MEDIA_PROJECTION_REQUEST_CODE);
                    }

                }
                else
                {
                    goToWifiHotspotSettings();
                }
            }
        });

        _accessCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtilities.isConnectedToWifi(MainActivity.this))
                {
                    Intent i = new Intent(MainActivity.this, ClientActivity.class);
                    startActivity(i);
                }
                else
                {
                    goToSelectWifiSettings();
                }
            }
        });
    }

    private void goToWifiHotspotSettings()
    {
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName(
                "com.android.settings",
                "com.android.settings.TetherSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( intent);
    }

    private void goToSelectWifiSettings()
    {
        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        _projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Constants.MEDIA_PROJECTION_REQUEST_CODE == requestCode &&
                RESULT_OK == resultCode)
        {
            Log.v(TAG,  "MediaProjectionManager request successful");
            Point p = getScreenSize();
            Bundle extras = new Bundle();
            extras.putInt(Constants.MEDIAPROJECTION_RESULT_CODE_KEY, resultCode);
            extras.putParcelable(Constants.MEDIAPROJECTION_RESULT_DATA_KEY, data);
            extras.putInt(Constants.SCREEN_WIDTH_KEY, p.x);
            extras.putInt(Constants.SCREEN_HEIGHT_KEY, p.y);
            extras.putInt(Constants.SCREEN_DENSITY_KEY, getScreenDensity());
            ScreenCastService.startActionScreenCast(this, extras);
            updateUI(true);
        }
    }

    private Point getScreenSize()
    {
        Display d = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        d.getRealSize(p);
        return p;
    }

    private void updateUI(boolean isScreenCastServiceStarted)
    {
        if (isScreenCastServiceStarted)
        {
            _accessCardView.setVisibility(View.INVISIBLE);
            _titleTextView.setText("Stop Sharing");
            _descriptionTextView.setText("Stop Screen sharing service");
        }
        else
        {
            _accessCardView.setVisibility(View.VISIBLE);
            _titleTextView.setText("Start Sharing");
            _descriptionTextView.setText("Start screen sharing using Wifi Hotspot");
        }
    }

    private int getScreenDensity()
    {
        return getResources().getDisplayMetrics().densityDpi;
    }
}
