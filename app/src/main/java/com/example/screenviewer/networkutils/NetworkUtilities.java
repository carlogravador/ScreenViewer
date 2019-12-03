package com.example.screenviewer.networkutils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.lang.reflect.Method;

public class NetworkUtilities {

    public static boolean isHotspotEnabled(Context context)
    {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            final Method method = manager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(manager);
        } catch (final Throwable ignored) {

        }
        return false;
    }

    public static String getServerHotspotIp(Context context)
    {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = manager.getDhcpInfo();
        return formatIp(dhcpInfo.gateway);
    }

    public static boolean isConnectedToWifi(Context context)
    {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled())
        {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if( wifiInfo.getNetworkId() == -1 )
            {
                return false;
            }
            return true;
        }
        else {
            return false;
        }
    }

    private static String formatIp(int ip)
    {
        return String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff)
        );
    }

}
