/*
 * This source file is proprietary property
 *  of Tim Stegeman <tim.stegeman@gmail.com>
 */
package nl.timstegeman.tetherremote.model;

/**
 *
 * @author tim
 */
public enum Mode {

	CLIENT,
	SERVER;
	
	public static String[] names(){
		String[] names = new String[values().length];
		for (int i = 0; i < values().length; i++) {
			names[i] = values()[i].name();
		}
		return names;
	}
}
