package com.example.spice.smsecret;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;

import com.android.mms.transaction.HttpUtils;
import com.android.mms.transaction.TransactionSettings;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.PduParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by spice on 3/18/17.
 */

public class MmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DEBUG-MMS", "CHECKING MMS");
        ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        GenericPdu genericPdu = new PduParser(intent.getByteArrayExtra("data")).parse();
        Log.d("DEBUG-MMS",genericPdu.toString());
        TransactionSettings transactionSettings = new TransactionSettings(context, mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).getExtraInfo());
        //byte[] rawPdu = HttpUtils.httpConnection(context, "mms.tmn.pt", null, HttpUtils.HTTP_GET_METHOD, transactionSettings.isProxySet(), transactionSettings.getProxyAddress(), transactionSettings.getProxyPort());


    }

    private static void ensureRouteToHost(ConnectivityManager cm, String url, TransactionSettings settings) throws IOException {
        int inetAddr;
        if (settings.isProxySet()) {
            String proxyAddr = settings.getProxyAddress();
            inetAddr = lookupHost(proxyAddr);
            if (inetAddr == -1) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            } else {
                if (!cm.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_MMS, inetAddr))
                    throw new IOException("Cannot establish route to proxy " + inetAddr);
            }
        } else {
            Uri uri = Uri.parse(url);
            inetAddr = lookupHost(uri.getHost());
            if (inetAddr == -1) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            } else {
                if (!cm.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_MMS, inetAddr))
                    throw new IOException("Cannot establish route to " + inetAddr + " for " + url);
            }
        }
    }

    private static int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return -1;
        }
        byte[] addrBytes;
        int addr;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24) | ((addrBytes[2] & 0xff) << 16) | ((addrBytes[1] & 0xff) << 8) | (addrBytes[0] & 0xff);
        return addr;
    }

    private static void ensureRouteToHostFancy(ConnectivityManager cm, String url, TransactionSettings settings) throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InvocationTargetException {
        Method m = cm.getClass().getMethod("requestRouteToHostAddress", new Class[] { int.class, InetAddress.class });
        InetAddress inetAddr;
        if (settings.isProxySet()) {
            String proxyAddr = settings.getProxyAddress();
            try {
                inetAddr = InetAddress.getByName(proxyAddr);
            } catch (UnknownHostException e) {
                throw new IOException("Cannot establish route for " + url + ": Unknown proxy " + proxyAddr);
            }
            if (!(Boolean) m.invoke(cm, new Object[] { ConnectivityManager.TYPE_MOBILE_MMS, inetAddr }))
                throw new IOException("Cannot establish route to proxy " + inetAddr);
        } else {
            Uri uri = Uri.parse(url);
            try {
                inetAddr = InetAddress.getByName(uri.getHost());
            } catch (UnknownHostException e) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            }
            if (!(Boolean) m.invoke(cm, new Object[] { ConnectivityManager.TYPE_MOBILE_MMS, inetAddr }))
                throw new IOException("Cannot establish route to " + inetAddr + " for " + url);
        }
    }



}
