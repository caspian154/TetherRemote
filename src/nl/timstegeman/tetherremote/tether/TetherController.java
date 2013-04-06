/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote.tether;

import android.net.wifi.WifiManager;
import nl.timstegeman.tetherremote.model.Command;
import nl.timstegeman.tetherremote.model.Status;

/**
 *
 * @author tim
 */
public class TetherController {

	private final WifiManager wifiManager;

	public TetherController(WifiManager wifiManager) {
		this.wifiManager = wifiManager;
	}

	public Status getStatus() {
		int wifiApEnabledState = WifiService.getWifiApState(wifiManager);
		return Status.getStatusById(wifiApEnabledState);
	}

	public void onCommand(Command command) {
		int wifiApEnabledState = WifiService.getWifiApState(wifiManager);
		switch (command) {
			case START:
				if (wifiApEnabledState != Status.ENABLED.getStatusId()
						&& wifiApEnabledState != Status.ENABLING.getStatusId()) {
					WifiService.setWifiApEnabled(wifiManager, true);
				}
				break;
			case STOP:
				if (wifiApEnabledState != Status.DISABLED.getStatusId()
						&& wifiApEnabledState != Status.DISABLING.getStatusId()) {
					WifiService.setWifiApEnabled(wifiManager, false);
				}
				break;
		}
	}
}
