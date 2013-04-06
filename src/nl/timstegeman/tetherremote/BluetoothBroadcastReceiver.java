/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import nl.timstegeman.tetherremote.model.Mode;

/**
 *
 * @author tim
 */
public class BluetoothBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = BluetoothBroadcastReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Mode mode = Mode.valueOf(prefs.getString("client_or_server", Mode.CLIENT.name()));
		if (mode == Mode.SERVER) {
			if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				Log.d(TAG, "Starting BluetoothServerService from BroadcastReceiver");
				context.startService(new Intent(context, BluetoothServerService.class));
			}
		}
	}
}
