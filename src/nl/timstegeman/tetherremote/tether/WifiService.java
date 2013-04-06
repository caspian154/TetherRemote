/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote.tether;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author tim
 */
public class WifiService {

	private static final String TAG = WifiService.class.getName();

	public static boolean setWifiApEnabled(WifiManager wfm, boolean enabled) {
		try {
			Method setWifiApEnabled = wfm.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			return (Boolean) setWifiApEnabled.invoke(wfm, null, enabled);
		} catch (NoSuchMethodException ex) {
			Log.e(TAG, "setWifiApEnabled", ex);
		} catch (SecurityException ex) {
			Log.e(TAG, "setWifiApEnabled", ex);
		} catch (IllegalAccessException ex) {
			Log.e(TAG, "setWifiApEnabled", ex);
		} catch (InvocationTargetException ex) {
			Log.e(TAG, "setWifiApEnabled", ex);
		}
		return false;
	}

	public static int getWifiApState(WifiManager wfm) {
		try {
			Method getWifiApState = wfm.getClass().getMethod("getWifiApState");
			return (Integer) getWifiApState.invoke(wfm);
		} catch (NoSuchMethodException ex) {
			Log.e(TAG, "getWifiApState", ex);
		} catch (SecurityException ex) {
			Log.e(TAG, "getWifiApState", ex);
		} catch (IllegalAccessException ex) {
			Log.e(TAG, "getWifiApState", ex);
		} catch (InvocationTargetException ex) {
			Log.e(TAG, "getWifiApState", ex);
		}
		return -1;
	}
}
