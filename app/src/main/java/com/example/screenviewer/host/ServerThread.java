package com.example.screenviewer.host;

import android.util.Log;

import com.example.screenviewer.Constants;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

public class ServerThread extends Thread {


    private final Hashtable<Socket, DataOutputStream> _clientList = new Hashtable<>();
    private ServerSocket _serverSocket;
    private boolean _isServerRunning = false;

    @Override
    public void run()
    {
        _isServerRunning = true;
        try
        {
            _serverSocket = new ServerSocket(Constants.SERVER_PORT);
            Log.v("ServerThread", "Server is up and running...");
            while (_isServerRunning)
            {
                acceptConnection();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void acceptConnection() throws IOException
    {
        Log.v("ServerThread", "Waiting for connections");
        Socket clientSocket =  _serverSocket.accept();
        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
        _clientList.put(clientSocket, outputStream);
        Log.v("ServerThread", "Received Client Connection");
    }

    private void clearResources()
    {
        Log.v("ServerThread", "Thread exiting. Clearing resources");
        try
        {
            for (Enumeration e =_clientList.keys(); e.hasMoreElements(); )
            {
                Socket socket = (Socket) e.nextElement();
                DataOutputStream outputStream = _clientList.get(socket);
                socket.close();
                outputStream.close();
            }
            _serverSocket.close();
            _clientList.clear();
            _serverSocket = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void removeClient(Socket s, DataOutputStream outputStream)
    {
        try
        {
            s.close();
            outputStream.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        _clientList.remove(s);
    }

    public void stopServer()
    {
        _isServerRunning = false;
        clearResources();
    }

    public void broadcastMessage(byte[] buffer)
    {
        synchronized (_clientList)
        {
            for (Enumeration e =_clientList.keys(); e.hasMoreElements(); )
            {
                Socket socket = (Socket) e.nextElement();
                DataOutputStream outputStream = _clientList.get(socket);
                try
                {
                    Log.v("ServerThread", "Writing to socket");
                    outputStream.writeInt(buffer.length);
                    outputStream.write(buffer, 0, buffer.length);
                }
                catch (IOException ex)
                {
                    removeClient(socket, outputStream);
                    ex.printStackTrace();
                }
            }
        }
    }
}
