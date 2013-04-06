/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 *
 * @author tim
 */
public class BluetoothClientService extends Service {
	
	private final IBinder binder;

	public BluetoothClientService() {
		binder = new LocalBinder();
	}
	
	public class LocalBinder extends Binder {

		public BluetoothClientService getService() {
			return BluetoothClientService.this;
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
	
	
}
