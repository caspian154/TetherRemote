/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import nl.timstegeman.tetherremote.model.Command;
import nl.timstegeman.tetherremote.model.Status;

/**
 *
 * @author tim
 */
public class BluetoothClient {

	private static final String TAG = BluetoothServer.class.getName();
	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothDevice bluetoothDevice;

	public BluetoothClient() {
	}

	public BluetoothDevice[] getBondedDevices() {
		return bluetoothAdapter.getBondedDevices().toArray(new BluetoothDevice[0]);
	}

	public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
		this.bluetoothDevice = bluetoothDevice;
	}

	public void setBluetoothDevice(String address) {
		for (BluetoothDevice dev : getBondedDevices()) {
			if (dev.getAddress().equalsIgnoreCase(address)) {
				setBluetoothDevice(dev);
				return;
			}
		}
	}

	public BluetoothDevice getBluetoothDevice() {
		return bluetoothDevice;
	}

	public Status send(Command command) {
		byte[] buf = new byte[128];
		int length;
		Status status = Status.FAILED;
		Log.v(TAG, "Trying to send command " + command);
		if (bluetoothAdapter.isEnabled()) {
			try {
				BluetoothSocket socket = bluetoothDevice.createRfcommSocketToServiceRecord(BluetoothConsts.BT_UUID);

				if (socket == null) {
					return Status.FAILED;
				}
				socket.connect();

				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();

				length = is.read(buf);
				if (new String(buf, 0, length).equals(BluetoothConsts.BT_NAME)) {
					Log.v(TAG, "Sending command " + command);
					os.write(command.name().getBytes());
					os.flush();

					length = is.read(buf);
					status = Status.valueOf(new String(buf, 0, length));
					Log.v(TAG, "Received status " + status);
				}
				os.close();
				is.close();

				socket.close();
			} catch (IOException ex) {
				Log.e(TAG, ex.getMessage(), ex);
				status = Status.FAILED;
			}
		}
		return status;
	}
}
