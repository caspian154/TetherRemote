/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.google.ads.R;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import nl.timstegeman.tetherremote.model.Command;
import nl.timstegeman.tetherremote.model.Status;
import nl.timstegeman.tetherremote.tether.TetherController;

/**
 *
 * @author tim
 */
public class BluetoothServer extends Thread {

	private static final String TAG = BluetoothServer.class.getName();
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothServerSocket serverSocket;
	private TetherController tetherController;
	private boolean terminated;

	public BluetoothServer(Context context) {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		tetherController = new TetherController((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
	}

	@Override
	public void run() {
		Log.v(TAG, "Start listening");
		if (bluetoothAdapter.isEnabled()) {
			try {
				serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(BluetoothConsts.BT_NAME, BluetoothConsts.BT_UUID);
			} catch (IOException ex) {
				Log.d(TAG, "Error opening server socket");
			}
			while (!terminated) {
				try {
					BluetoothSocket socket = serverSocket.accept(15000);
					if (socket != null) {
						Log.v(TAG, "Socket accepted");
						new AcceptThread(socket).start();
					}
				} catch (IOException ex) {
					Log.d(TAG, "Error while reading socket", ex);
				}

				if (!bluetoothAdapter.isEnabled()) {
					stopServer();
				}
			}
		}
		Log.d(TAG, "Server stopped");
	}

	private class AcceptThread extends Thread {

		private BluetoothSocket socket;

		public AcceptThread(BluetoothSocket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			byte[] buf = new byte[128];
			int length;
			try {
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();

				os.write(BluetoothConsts.BT_NAME.getBytes());
				os.flush();
				
				length = is.read(buf);
				Command command = Command.valueOf(new String(buf, 0, length));
				Log.d(TAG, "Received command " + command);
				tetherController.onCommand(command);

				Status status = tetherController.getStatus();
				Log.d(TAG, "Sending status " + status);
				os.write(status.name().getBytes());
				os.flush();

				is.close();
				os.close();

				socket.close();
			} catch (IOException ex) {
				Log.d(TAG, "Error while reading socket", ex);
			}
		}
	}

	public boolean startServer() {
		if (!isAlive()) {
			Log.d(TAG, "Starting server");
			terminated = false;
			start();
			return true;
		}
		return false;
	}

	public boolean stopServer() {
		if (isAlive()) {
			Log.d(TAG, "Stop server");
			terminated = true;
			interrupt();
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException ex) {
				Log.e(TAG, "catched an exception while stopping server", ex);
			}
			return true;
		}
		return false;
	}
}
