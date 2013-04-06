/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote.model;

/**
 *
 * @author tim
 */
public enum Status {

	UNKNOWN(-1),
	DISABLING(0),
	DISABLED(1),
	ENABLING(2),
	ENABLED(3),
	FAILED(4);
	private int statusId;

	public int getStatusId() {
		return statusId;
	}

	public static Status getStatusById(int id) {
		for (Status s : values()) {
			if (s.getStatusId() == id) {
				return s;
			}
		}
		return UNKNOWN;
	}

	Status(int statusId) {
		this.statusId = statusId;
	}
}
