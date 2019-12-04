package com.example.screenviewer.client;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.screenviewer.Constants;
import com.example.screenviewer.R;
import com.example.screenviewer.Utility.Utility;
import com.example.screenviewer.networkutils.NetworkUtilities;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientActivity extends AppCompatActivity {

    ImageView _displayView;
    ClientThread _clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        _displayView = findViewById(R.id.display_screen_cast);
        final String hostIp = NetworkUtilities.getServerHotspotIp(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    _clientThread = new ClientThread(hostIp, Constants.SERVER_PORT);
                    _clientThread.start();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Log.e("ClientActivity", "Error starting client thread");
                    Utility.showToast(ClientActivity.this, "Connected to server failed");
                }
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        if (_clientThread != null)
        {
            _clientThread.stopConnection();
        }

        super.onDestroy();
    }

    public class ClientThread extends Thread
    {
        private Socket _socket;
        private boolean _isConnected;
        private DataInputStream _dataInputStream;
        private Handler _handler;

        public ClientThread(String ip, int port) throws IOException
        {
            _handler = new Handler(Looper.getMainLooper());
            connectToServer(ip, port);
        }

        public void stopConnection()
        {
            _isConnected = false;
        }

        private void connectToServer(String ip, int port) throws IOException
        {
            Log.v("ClientActivity", "Connecting to Ip: " + ip + ", Port: " + port);
            _socket = new Socket(ip, port);
            _dataInputStream = new DataInputStream(_socket.getInputStream());
            _isConnected = true;
            Log.v("ClientActivity", "Socket created successfully. Ip: " + ip + ", Port: " + port);
        }

        private void listenToServer() throws IOException
        {
            int len = _dataInputStream.readInt();
            if (len > 0) {
                final byte[] bytes = new byte[len];
                _dataInputStream.readFully(bytes, 0, bytes.length);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        _displayView.setImageBitmap(bm);
                    }
                });
            }
        }

        private void closeSocket()
        {
            try
            {
                _socket.close();
                _dataInputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Utility.showToast(ClientActivity.this, "Disconnected to Server");
            Log.v("Clientivity", "Socket closed");
        }

        @Override
        public void run() {
            try
            {
                while (_isConnected) {
                    listenToServer();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                closeSocket();
            }
        }
    }
}
