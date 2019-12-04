package com.example.screenviewer.host;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.screenviewer.App;
import com.example.screenviewer.Constants;
import com.example.screenviewer.MainActivity;
import com.example.screenviewer.R;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ScreenCastService extends Service {

    public static boolean _isStarted = false;

    private MediaProjection _mediaProjection;
    private MediaProjectionManager _projectionManager;
    private ImageReader _imageReader;
    private Handler _imageHandler;

    private ServerThread _server;

    private int _displayWidth = 0;
    private int _displayHeight = 0;
    private int _screenDensity = 0;


    public ScreenCastService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("ScreenCastService,","Service start command");
        handleIntent(intent);
        startNotification();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.v("ScreenCasstService,","Stopping service");
        if (_server != null)
        {
            _server.stopServer();
        }
        stopMediaProjection();
        super.onDestroy();
    }

    public static void startActionScreenCast(Context context, Bundle extras)
    {

        Intent i = new Intent(context, ScreenCastService.class);
        i.putExtras(extras);
        ContextCompat.startForegroundService(context, i);
    }

    public static boolean isStarted()
    {
        return _isStarted;
    }

    private void startNotification()
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Screen Cast Service")
                .setContentText("Screen cast is running")
                .setSmallIcon(R.drawable.ic_screen_share)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void handleIntent(Intent intent)
    {
        if (intent != null)
        {
            Bundle extras = intent.getExtras();
            int resultCode = extras.getInt(Constants.MEDIAPROJECTION_RESULT_CODE_KEY);
            Intent data = extras.getParcelable(Constants.MEDIAPROJECTION_RESULT_DATA_KEY);
            _displayHeight = extras.getInt(Constants.SCREEN_HEIGHT_KEY);
            _displayWidth = extras.getInt(Constants.SCREEN_WIDTH_KEY);
            _screenDensity = extras.getInt(Constants.SCREEN_DENSITY_KEY);
            handleActionScreenCast(resultCode, data);
        }
    }

    private void handleActionScreenCast(int resultCode, Intent data)
    {
        _server = new ServerThread();
        _server.start();

        startMediaProjection(resultCode, data);
    }

    private void startMediaProjection(int resultCode, Intent data)
    {
        _isStarted = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                _imageHandler = new Handler();
                Looper.loop();
            }
        }).start();

        _projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        _mediaProjection = _projectionManager.getMediaProjection(resultCode, data);
        _imageReader = ImageReader.newInstance(_displayWidth, _displayHeight, PixelFormat.RGBA_8888, 2);
        _mediaProjection.createVirtualDisplay("ScreenCapture", _displayWidth, _displayHeight,
                _screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, _imageReader.getSurface(),
                null, _imageHandler);
        _imageReader.setOnImageAvailableListener(new ImageAvailableListener(_displayWidth, _displayHeight), _imageHandler);
    }

    private void stopMediaProjection()
    {
        _isStarted = false;
        _imageHandler.post(new Runnable() {
            @Override
            public void run() {
                if (_mediaProjection != null)
                {
                    _mediaProjection.stop();
                }
            }
        });
    }

    public class ImageAvailableListener implements ImageReader.OnImageAvailableListener
    {

        private final int COMPRESSION_QUALITY = 75;
        private final float RESIZE_RATIO = 50f / 100;

        private Bitmap _reusableBitmap;
        private int _displayWidth;
        private int _displayHeight;

        public ImageAvailableListener(int width, int height)
        {
            _displayWidth = width;
            _displayHeight = height;

        }

        private Bitmap resizeBitmap(Bitmap cleanBitmap, float ratio)
        {
            int origWidth = cleanBitmap.getWidth();
            int origHeight = cleanBitmap.getHeight();

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.preScale(ratio, ratio);

            return Bitmap.createBitmap(cleanBitmap, 0, 0, origWidth, origHeight, scaleMatrix, true);
        }

        @Override
        public void onImageAvailable(ImageReader imageReader)
        {
            Image image = null;

            Bitmap cleanBitmap = null;
            Bitmap resizedBitmap = null;

            ByteArrayOutputStream byteArrayOutputStream = null;

            try
            {
                image = imageReader.acquireLatestImage();
                if (image != null)
                {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * _displayWidth;


                    int width = (_displayWidth + rowPadding / pixelStride);
                    int height = _displayHeight;

                    if (width > image.getWidth())
                    {
                        if (_reusableBitmap == null)
                        {
                            _reusableBitmap = Bitmap.createBitmap(width, image.getHeight(), Bitmap.Config.ARGB_8888);
                        }
                        _reusableBitmap.copyPixelsFromBuffer(buffer);
                        cleanBitmap = Bitmap.createBitmap(_reusableBitmap, 0, 0, image.getWidth(), image.getHeight());
                    }
                    else
                    {
                        cleanBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        cleanBitmap.copyPixelsFromBuffer(buffer);
                    }

                    resizedBitmap = resizeBitmap(cleanBitmap, RESIZE_RATIO);
                    cleanBitmap.recycle();

                    byteArrayOutputStream = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream);
                    _server.broadcastMessage(byteArrayOutputStream.toByteArray());
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (resizedBitmap != null)
                {
                    resizedBitmap.recycle();
                }
                if (image != null)
                {
                    image.close();
                }
            }
        }
    }
}
