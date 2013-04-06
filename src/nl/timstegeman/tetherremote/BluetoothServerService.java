/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import nl.timstegeman.tetherremote.bluetooth.BluetoothServer;

/**
 *
 * @author tim
 */
public class BluetoothServerService extends Service {

	private static final String TAG = BluetoothServerService.class.getName();
	private final IBinder binder;
	private BluetoothServer bluetoothServer;

	public BluetoothServerService() {
		binder = new LocalBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		bluetoothServer = new BluetoothServer(getApplicationContext());
		bluetoothServer.startServer();
	}

	public class LocalBinder extends Binder {

		public BluetoothServerService getService() {
			return BluetoothServerService.this;
		}
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	

	@Override
	public void onDestroy() {
		bluetoothServer.stopServer();
		super.onDestroy();
	}
	
}
