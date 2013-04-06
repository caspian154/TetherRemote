/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import nl.timstegeman.tetherremote.bluetooth.BluetoothClient;
import nl.timstegeman.tetherremote.model.Command;
import nl.timstegeman.tetherremote.model.Mode;
import nl.timstegeman.tetherremote.model.Status;

public class TetherRemoteActivity extends PreferenceActivity
		implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

	private static final String TAG = TetherRemoteActivity.class.getName();
	private BluetoothClient client;
	private ListPreference clientOrServerPref;
	private ListPreference selectedServerPref;
	private CheckBoxPreference enableWifiTetherPref;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "TetherRemote started");
		addPreferencesFromResource(R.layout.preferences);
		client = new BluetoothClient();

		clientOrServerPref = (ListPreference) findPreference("client_or_server");
		clientOrServerPref.setOnPreferenceChangeListener(this);
		clientOrServerPref.setEntryValues(Mode.names());
		if (clientOrServerPref.getValue() == null){
			clientOrServerPref.setValueIndex(0);
		}

		selectedServerPref = (ListPreference) findPreference("selected_server");
		selectedServerPref.setOnPreferenceChangeListener(this);
		selectedServerPref.setOnPreferenceClickListener(this);
		updateDevices();
		client.setBluetoothDevice(selectedServerPref.getValue());

		enableWifiTetherPref = (CheckBoxPreference) findPreference("enable_wifi_tether");
		enableWifiTetherPref.setOnPreferenceChangeListener(this);
		enableWifiTetherPref.setChecked(false);

		updateSummary(clientOrServerPref, R.string.client_or_server_summary, clientOrServerPref.getValue());
		updateSummary(selectedServerPref, R.string.selected_server_summary, selectedServerPref.getValue());

		if (Mode.valueOf(clientOrServerPref.getValue()).equals(Mode.CLIENT)) {
			selectedServerPref.setEnabled(true);
			enableWifiTetherPref.setEnabled(client.getBluetoothDevice() != null);
		} else {
			startService(new Intent(this, BluetoothServerService.class));
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object obj) {
		if (preference == clientOrServerPref) {
			Mode mode = Mode.valueOf((String) obj);
			if (mode.equals(Mode.SERVER)) {
				startService(new Intent(this, BluetoothServerService.class));
			} else {
				stopService(new Intent(this, BluetoothServerService.class));
			}
			selectedServerPref.setEnabled(mode == Mode.CLIENT);
			enableWifiTetherPref.setEnabled(mode == Mode.CLIENT && client.getBluetoothDevice() != null);
			updateSummary(clientOrServerPref, R.string.client_or_server_summary, obj);
		} else if (preference == selectedServerPref) {
			client.setBluetoothDevice((String) obj);
			enableWifiTetherPref.setEnabled(obj != null);
			updateSummary(selectedServerPref, R.string.selected_server_summary, obj);
		} else if (preference == enableWifiTetherPref) {
			sendAsyncCommand((Boolean) obj ? Command.START : Command.STOP);
		}
		return true;
	}

	private void updateSummary(ListPreference listPreference, int summaryId, Object obj) {
		String value = getString(R.string.none);
		if (obj != null) {
			int index = listPreference.findIndexOfValue((String) obj);
			if (index >= 0) {
				value = (String) listPreference.getEntries()[index];
			}
		}
		listPreference.setSummary(String.format(getString(summaryId), value));
	}

	private void sendAsyncCommand(final Command command) {
		final ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.please_wait), true);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		final Thread asyncThread = new Thread() {

			@Override
			public void run() {
				int retry = 0;
				Status lastStatus = null;
				while (lastStatus == null) {
					Status status = client.send(command);
					switch (status) {
						case ENABLED:
						case DISABLED:
						case UNKNOWN:
							lastStatus = status;
							break;
						case FAILED:
							if (++retry >= 2) {
								lastStatus = status;
							} else {
								Log.d(TAG, "Failed...retrying (" + retry + ")");
							}
							break;
						case ENABLING:
						case DISABLING:
							try {
								sleep(400);
							} catch (InterruptedException ex) {
								lastStatus = Status.FAILED;
							}
					}
				}
				final Status status = lastStatus;
				runOnUiThread(new Runnable() {

					public void run() {
						enableWifiTetherPref.setChecked(status == Status.ENABLED);
						dialog.dismiss();
						switch (status) {
							case DISABLED:
								Toast.makeText(TetherRemoteActivity.this, R.string.stopped, Toast.LENGTH_SHORT).show();
								break;
							case ENABLED:
								Toast.makeText(TetherRemoteActivity.this, R.string.started, Toast.LENGTH_SHORT).show();
								break;
							case FAILED:
								Toast.makeText(TetherRemoteActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
								break;
						}
					}
				});
			}
		};

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			public void onCancel(DialogInterface dialog) {
				asyncThread.interrupt();
				enableWifiTetherPref.setChecked(false);
			}
		});


		asyncThread.start();
	}

	public boolean onPreferenceClick(Preference preference) {
		if (preference == selectedServerPref) {
			updateDevices();
			return true;
		}
		return false;
	}

	private void updateDevices() {
		BluetoothDevice[] bondedDevices = client.getBondedDevices();
		String[] deviceNames = new String[bondedDevices.length];
		String[] deviceAddresses = new String[bondedDevices.length];
		for (int i = 0; i < bondedDevices.length; i++) {
			deviceNames[i] = bondedDevices[i].getName();
			deviceAddresses[i] = bondedDevices[i].getAddress();
		}
		selectedServerPref.setEntries(deviceNames);
		selectedServerPref.setEntryValues(deviceAddresses);
	}
}